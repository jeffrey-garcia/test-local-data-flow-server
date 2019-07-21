# A demo project of Spring Cloud Dataflow Local Server
A spring-boot application running Spring Cloud Dataflow Server locally. 

Spring Cloud Dataflow is mainly used to create and orchestrate data 
pipelines for common use cases such as: 
- data ingest, 
- real-time analytics,  
- data import/export
<br/>

The data pipelines come in two flavors, streaming and batch data pipelines. 
In the first case, an unbounded amount of data is consumed or produced via 
messaging middleware, while in the second case the short-lived task processes 
a finite set of data and then terminate.
<br/>

This project will focus on the batch pipelines which runs at fixed schedule 
to export and upload data to legacy host system.
<br/>

### Motivations:
- Launching a task externally (via Spring Scheduler for example) but still 
keep track of the task execution via Spring Cloud Data Flow


### Key Architecture Consideration:
- Deployment Flexibility
    - The Spring Cloud Data Flow server uses Spring Cloud Deployer, to deploy 
    pipelines onto modern runtimes such as:
        - Cloud Foundry
        - Kubernetes
        - Apache Mesos
        - Apache YARN
        
- Easy Adoption
    - A selection of pre-built stream and task/batch starter apps for various 
    data integration and processing scenarios facilitate learning and 
    experimentation.
    - A simple stream pipeline DSL makes it easy to specify which apps to deploy 
    and how to connect outputs and inputs. A new composed task DSL was added in 
    v1.2.
    
- Customizable
    - Custom stream and task applications, targeting different middleware or 
    data services, can be built using the familiar Spring Boot style programming 
    model.
    
- Management Dashboard
    - The dashboard offers a graphical editor for building new pipelines interactively, 
    as well as views of deployable apps and running apps with metrics.
    
- Easy Integration
    - The Spring Could Data Flow server exposes a REST API for composing and deploying data pipelines. 
    - A separate shell makes it easy to work with the API from the command line.      
<br/>


### Pre-requisite:
The key components include:
- Spring Cloud Dataflow Server `v1.7.4`
- Dataflow Shell `v1.3.0`
- Target Runtime:
    - Cloud Foundry (for deployment)
    - local computer (for local development)
- Application written using Spring Batch and Spring Cloud Task
- Rabbit MQ (MAC) `v3.6.14`
- Redis (MAC) `v4.0.2`
- Embedded H2

<br/>


### Setup RabbitMQ:
To develop/debug locally, install RabbitMQ on MAC using Homebrew.

On a Mac with homebrew:
```sh        
brew install rabbitmq
```
Once completed, launch it with default settings.
```sh
/usr/local/Cellar/rabbitmq/3.6.14/sbin/rabbitmq-server
```

Default startup port will be `5672`.

<br/>


### Setup Local Spring Cloud Dataflow Server: 
Open the dashboard at:
- [http://localhost:9393/dashboard](http://localhost:9393/dashboard).

Bulk Import out-of-the-box stream applications for RabbitMQ using the URI:
- [http://bit.ly/Celsius-SR1-stream-applications-rabbit-maven](http://bit.ly/Celsius-SR1-stream-applications-rabbit-maven) 

<br/>


### Setup Local Spring Cloud Dataflow Shell:
Download the executable jar at:
- [HERE](https://repo.spring.io/libs-release/org/springframework/cloud/spring-cloud-dataflow-shell/1.3.0.RELEASE/)

To start the Data Flow Shell for the Data Flow server running in classic mode:
```
java -jar spring-cloud-dataflow-shell-1.3.0.RELEASE.jar
```

If the server has enabled authentication, login via following command (sample only, replace with your configuration):
```
dataflow config server --uri http://localhost:9393 --username admin --password password --skip-ssl-validation true
```

See [HERE](https://docs.spring.io/spring-cloud-dataflow/docs/current/reference/htmlsingle/#configuration-security-authentication-via-shell)

##### Registering a cloud task app:
Register an app from local file system
```
app register --name spring-cloud-task-sample --type task --uri file:///Users/example/mytask-x.y.z.jar
```

Register an app from Maven repository
```
app register --name spring-cloud-task-sample --type task --uri maven://{groupId}:{artifactoryId}:{version}
```

##### Creating a task definition:
```
task create mytask --definition "spring-cloud-task-sample"
```

##### Launching a task:
```
task luanch mytask
``` 

<br/>

### Task Scheduling
One way to launch a task using the task-launcher is to use the triggertask source. The triggertask 
source will emit a message with a TaskLaunchRequest object containing the required launch information.  
- See [HERE](https://docs.spring.io/spring-cloud-dataflow/docs/current/reference/htmlsingle/#spring-cloud-dataflow-launch-tasks-from-stream)

Register `task-launcher-local` by executing the app register command, as follows (for the Rabbit Binder, in this case):
```
app register --name task-launcher-local --type sink --uri maven://org.springframework.cloud.stream.app:task-launcher-local-sink-rabbit:jar:1.2.0.RELEASE
```

Register `triggertask` by running the app register command, as follows (for the Rabbit Binder, in this case):
```
app register --type source --name triggertask --uri maven://org.springframework.cloud.stream.app:triggertask-source-rabbit:1.2.0.RELEASE
```

For example, the command below will launch a task as a stream every 30 seconds:
```
stream create foo --definition "triggertask --triggertask.uri=file:///Users/example/mytask-x.y.z.jar --trigger.fixed-delay=30 | task-launcher-local --maven.remote-repositories.repo1.url=http://repo.spring.io/libs-release" --deploy
```

Note that `task-launcher-local` is for development purposes only in current release as of time of writing, once 
the dataflow server is restarted, the deployed streams will be lost and require manual re-creation and re-deployment.

<br/>

### Enable Authentication to the Dataflow server
See [HERE](https://docs.spring.io/spring-cloud-dataflow/docs/current/reference/htmlsingle/#configuration-security-basic-authentication)

To login with the designated Dataflow server, issue the following command:
```
dataflow config server --uri http://localhost:9393 --username admin --password password --skip-ssl-validation true
```
Replace the URL, username and password according to your server's configuration.

To validation the connection info with Dataflow server, issue the following command:
```
dataflow config info
```
If nothing is output to the console, there might be authentication problem with the server.

Outstanding:
- resolve the H2 console access issue when Spring Security is enabled

### Rest API:
Registering an app with type `task`:
```sh
curl 'http://localhost:9393/apps/task/spring-cloud-task-sample' -i -X POST  -u admin:password \
-d 'uri=maven%3A%2F%2F{groupId}%3A{artifactoryId}%3A{version}'
```

Creating a new task definition:
```sh
curl 'http://localhost:9393/tasks/definitions' -i -X POST  -u admin:password \
-d 'name=mytask&definition=spring-cloud-task-sample'
```

List all tasks definitions:
```sh
curl 'http://localhost:9393/tasks/definitions' -i -X GET -u admin:password
```

Retrieve task definition detail
```sh
curl 'http://localhost:9393/tasks/definitions/spring-cloud-task-1' -i -X GET -u admin:password
```

Launching a task
```sh
curl 'http://localhost:9393/tasks/executions' -i -X POST -d 'name=spring-cloud-task-1' -u admin:password
```

Verifying current tasks execution number
```sh
curl 'http://localhost:9393/tasks/executions/current' -i -X GET -u admin:password
```

### Auto-Scaling on Cloud Foundry

For a typical data-flow server deployed as single instance with 8GB memory on PCF, it's seen that
the hardware resources saturation happens at around 20 concurrent requests (CPU utilization over 200%),
where subsequent requests will encounter http 500 error and no more new task can be launched.
- To serve 200 concurrent requests, need at minimum 200/20 = 10 instances (total 80GB memory)
- To serve 400 concurrent requests, need at minimum 400/20 = 20 instances (total 160GB memory)

##### Approach
- setup 2 instances as initital deploy settings
- when all available instances encounter CPU spike (over threshold 200%), PCF auto create additional instances
- the next incoming request will be served in a round-robin fashion over the available instances

For example:
1. at beginning, only 2 default instances running
2. start of business hour
3. subsequent requests to data-flow will be shared among all available instances
4. if CPU utilization exceed above threshold, then initiate PCF to increase available instance by 1, go back to step (3)
5. if CPU utilization drop below threshold, then initiate PCF to decrease available instance by 1, go back to step (3)

Throughout the day, we should expect the number of running instances jumps from 2
to a higher number until reaching the limit of max auto-scalable instance configured.
Then after business hours or holiday, we should expect the number of running instances
drops back to 2.

##### Preparation
Download and install the app autoscaler plugin for CF cli, see
- https://docs.pivotal.io/pivotalcf/2-4/appsman-services/autoscaler/using-autoscaler-cli.html#install

Push the app to PCF
```sh
cf push -f manifest-dev.yml
```

Enable autoscaling for the PCF app
```sh
cf enable-autoscaling test-spring-dataflow-server
```

Configure with a dedicated autoscaling manifest
```sh
cf configure-autoscaling test-spring-dataflow-server autoscaler-manifest-dev.yml
```

##### Implementation
- limit the max concurrent execution in data-flow to 20 (as this is where the hardware saturation
is observed on PCF)
- setup auto-scaling rule where CPU min threshold is 30% and max threshold is 200%
- setup the maximum instances can be auto-created by PCF, for example if need to serve 400 concurrent
requests (400 agents refresh my performance computation), need at minimum 400/20 = 20 instances
(total 160GB memory), the more concurrent requests need to serve, the more instances is needed and
the more memory will be required
- liaise with platform team to calibrate the memory required to PROD CMP space in PCF.

See also:
https://docs.pivotal.io/pivotalcf/2-5/appsman-services/autoscaler/using-autoscaler.html#metric

##### Drawbacks
- note there will be startup time overheads to spin up this new instance ~30-90s
before it could serve new requests
- the H2 db is embedded to each instance, when the instance is dismissed the execution
log of the cloud task will be purged with the db file
`* increased difficulties in troubleshooting and problem investigation, particularly when the instance is already destroyed`
- the memory cost is high depending on the max auto-scalable instance and the memory
configured per instance, say 20 instances and 8GB each sums up to total 160 GB memory
`* if optimization at the code-level can reduce the running time and memory drastically (down to 5s per request with low memory consumption), then it should be where the investment goes to and the data-flow is reserved for the team performance task which runs as a system batch every 2 hours`

### References:

##### Spring Cloud Data Flow Server - Local (1.7.4)
https://docs.spring.io/spring-cloud-dataflow/docs/1.7.4.RELEASE/reference/htmlsingle/#getting-started

##### Spring Cloud Data Flow Server - Cloud Foundry (1.7.4)
https://docs.spring.io/spring-cloud-dataflow-server-cloudfoundry/docs/current/reference/htmlsingle/#getting-started

##### Spring Cloud Data Flow Examples
https://docs.spring.io/spring-cloud-dataflow-samples/docs/current/reference/htmlsingle/

##### Spring Cloud Data Flow - All Documents (new)
https://dataflow.spring.io/docs/

<br/>