package com.example.jeffrey.localdataflowserver.LocalDataFlowServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.dataflow.server.EnableDataFlowServer;

@SpringBootApplication
@EnableDataFlowServer
public class LocalDataFlowServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(LocalDataFlowServerApplication.class, args);
	}

}
