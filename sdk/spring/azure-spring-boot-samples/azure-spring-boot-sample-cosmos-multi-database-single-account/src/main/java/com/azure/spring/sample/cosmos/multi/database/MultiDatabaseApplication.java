// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.sample.cosmos.multi.database;

import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.azure.spring.sample.cosmos.multi.database.repository.User;
import com.azure.spring.sample.cosmos.multi.database.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@SpringBootApplication
public class MultiDatabaseApplication implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Qualifier("database1Template")
    private ReactiveCosmosTemplate database1Template;

    @Autowired
    @Qualifier("database2Template")
    private ReactiveCosmosTemplate database2Template;

    private final User user = new User("1024", "1024@geek.com", "1k", "Mars");
    private static CosmosEntityInformation<User, String> userInfo = new CosmosEntityInformation<>(User.class);

    public static void main(String[] args) {
        SpringApplication.run(MultiDatabaseApplication.class, args);
    }

    public void run(String... var1) throws Exception {

        User database1UserGet = database1Template.findById(User.class.getSimpleName(),
            user.getId(), User.class).block(); // Same to userRepository.findById(user.getId()).block()
        System.out.println(database1UserGet);
        User database2UserGet = database2Template.findById(User.class.getSimpleName(), user.getId(), User.class).block();
        System.out.println(database2UserGet);
    }

    @PostConstruct
    public void setup() {
        database1Template.createContainerIfNotExists(userInfo).block();
        database1Template.insert(User.class.getSimpleName(), user,
            new PartitionKey(user.getName())).block();  // Same to this.userRepository.save(user).block();
        database2Template.createContainerIfNotExists(userInfo).block();
        database2Template.insert(User.class.getSimpleName(), user,
            new PartitionKey(user.getName())).block();
    }

    @PreDestroy
    public void cleanup() {
        database1Template.deleteAll(User.class.getSimpleName(),
            User.class).block(); // Same to this.userRepository.deleteAll().block();
        database2Template.deleteAll(User.class.getSimpleName(),
            User.class).block();
    }
}

