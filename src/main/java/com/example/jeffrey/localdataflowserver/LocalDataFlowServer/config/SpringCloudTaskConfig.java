package com.example.jeffrey.localdataflowserver.LocalDataFlowServer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "spring-cloud-task")
public class SpringCloudTaskConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringCloudTaskConfig.class);

    private List<TaskConfig> tasks = new ArrayList<>();

    public SpringCloudTaskConfig() {}

    public void setTasks(List<TaskConfig> tasks) {
        this.tasks = tasks;
    }

    public List<TaskConfig> getTasks() {
        return tasks;
    }
}
