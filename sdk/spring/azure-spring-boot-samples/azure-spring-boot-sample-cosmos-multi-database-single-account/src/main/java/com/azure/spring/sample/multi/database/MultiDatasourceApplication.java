// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.sample.multi.database;

import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.azure.spring.sample.multi.database.database1.User;
import com.azure.spring.sample.multi.database.database1.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@SpringBootApplication
public class MultiDatasourceApplication implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Qualifier("primaryDatabase1Template")
    private ReactiveCosmosTemplate primaryDatabase1Template;

    @Autowired
    @Qualifier("primaryDatabase2Template")
    private ReactiveCosmosTemplate primaryDatabase2Template;

    private final User user = new User("1024", "1024@geek.com", "1k", "Mars");
    private static CosmosEntityInformation<User, String> userInfo = new CosmosEntityInformation<>(User.class);

    public static void main(String[] args) {
        SpringApplication.run(MultiDatasourceApplication.class, args);
    }

    public void run(String... var1) throws Exception {

        User database1UserGet = primaryDatabase1Template.findById(User.class.getSimpleName(),
            user.getId(), User.class).block(); // Same to userRepository.findById(user.getId()).block();
        System.out.println(database1UserGet);
        User database2UserGet = primaryDatabase2Template.findById(User.class.getSimpleName(), user.getId(), User.class).block();
        System.out.println(database2UserGet);
    }

    @PostConstruct
    public void setup() {
        primaryDatabase1Template.createContainerIfNotExists(userInfo).block();
        primaryDatabase1Template.insert(User.class.getSimpleName(), user,
            new PartitionKey(user.getName())).block();  // Same to this.userRepository.save(user).block();
        primaryDatabase2Template.createContainerIfNotExists(userInfo).block();
        primaryDatabase2Template.insert(User.class.getSimpleName(), user,
            new PartitionKey(user.getName())).block();
    }

    @PreDestroy
    public void cleanup() {
        primaryDatabase1Template.deleteAll(User.class.getSimpleName(),
            User.class).block(); // Same to this.userRepository.deleteAll().block();
        primaryDatabase2Template.deleteAll(User.class.getSimpleName(),
            User.class).block();
    }
}

