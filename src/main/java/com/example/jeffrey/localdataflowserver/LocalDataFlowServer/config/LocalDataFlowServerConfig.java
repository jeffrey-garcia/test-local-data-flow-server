package com.example.jeffrey.localdataflowserver.LocalDataFlowServer.config;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@RefreshScope
@EnableConfigurationProperties(value = { SpringCloudTaskConfig.class})
@Configuration
public class LocalDataFlowServerConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalDataFlowServerConfig.class);

    @Autowired
    private SpringCloudTaskConfig springCloudTaskConfig;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().build();
    }

    @Autowired
    private Environment environment;

    @Bean
    public CommandLineRunner commandLineRunner(RestTemplate restTemplate) {
        return args -> {
            LOGGER.info("CommandLineRunner running");

            String port = environment.getProperty("local.server.port");

            try {
                Assert.assertNotNull("local server port is null", port);
                Assert.assertNotNull("task config is null", springCloudTaskConfig.getTasks());
                Assert.assertTrue("task config is empty", springCloudTaskConfig.getTasks().size() > 0);

                for (TaskConfig taskConfig: springCloudTaskConfig.getTasks()) {
                    LOGGER.info("definition: {}", taskConfig.getDefinition());
                    LOGGER.info("uri: {}", taskConfig.getUri());
                    LOGGER.info("name: {}", taskConfig.getName());

                    try {
                        Assert.assertNotNull("task definition is null", taskConfig.getDefinition());
                        Assert.assertNotNull("task uri is null", taskConfig.getUri());
                        Assert.assertNotNull("task name is null", taskConfig.getName());
                    } catch (AssertionError error) {
                        LOGGER.error(error.getMessage(), error);
                        continue;
                    }

                    createApp: {
                        LOGGER.info("registering app...");
                        // Invoke the restful API to local dataflow server and re-create the app definition
                        URI uri = URI.create("http://localhost:" + port + "/apps/task/" + taskConfig.getDefinition());
                        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
                        requestBody.add("uri", taskConfig.getUri());
                        try {
                            ResponseEntity<?> responseEntity = restTemplate.postForEntity(uri, requestBody, Object.class);
                            LOGGER.info("http status: {}", responseEntity.getStatusCode());
                        } catch (RestClientException e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }

                    createTask: {
                        LOGGER.info("creating task...");
                        // Invoke the restful API to local dataflow server and re-create the task
                        URI uri = URI.create("http://localhost:" + port + "/tasks/definitions");
                        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
                        requestBody.add("name", taskConfig.getName());
                        requestBody.add("definition", taskConfig.getDefinition());
                        try {
                            ResponseEntity<?> responseEntity = restTemplate.postForEntity(uri, requestBody, Object.class);
                            LOGGER.info("http status: {}", responseEntity.getStatusCode());
                        } catch (RestClientException e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                }

            } catch (AssertionError error) {
                LOGGER.error(error.getMessage(), error);
            }
        };
    }

    @Bean
    ApplicationListener applicationListener(CommandLineRunner commandLineRunner) {
        return new ApplicationListener<EnvironmentChangeEvent>() {
            @Override
            public void onApplicationEvent(EnvironmentChangeEvent environmentChangeEvent) {
                LOGGER.info("environment changed...");

                // all the beans with RefreshScope should have been re-initialized
                // invoke the CommandLineRunner bean to do the work that make the new values effectuate
                try {
                    commandLineRunner.run();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        };
    }

}
