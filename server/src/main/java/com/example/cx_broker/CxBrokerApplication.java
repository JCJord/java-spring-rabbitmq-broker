package com.example.cx_broker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CxBrokerApplication {

	public static void printWorld() {
		System.out.println("World");
	}

	public static void main(String[] args) {
		SpringApplication.run(CxBrokerApplication.class, args);
		printWorld();
	}

}
