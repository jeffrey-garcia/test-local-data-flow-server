package com.example.jeffrey.localdataflowserver.LocalDataFlowServer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskConfig.class);

    private String definition;
    private String uri;
    private String name;

    public TaskConfig() {}

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDefinition() {
        return this.definition;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return this.uri;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}
