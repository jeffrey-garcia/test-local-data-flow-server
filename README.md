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