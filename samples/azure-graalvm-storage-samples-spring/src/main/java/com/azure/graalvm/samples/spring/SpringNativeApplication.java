// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.graalvm.samples.spring;

import com.azure.graalvm.samples.spring.storage.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(proxyBeanMethods = false)
public class SpringNativeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringNativeApplication.class, args);
	}

	@Bean
	CommandLineRunner init(StorageService storageService) {
		return (args) -> {
			storageService.init();
		};
	}
}
