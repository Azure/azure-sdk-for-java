// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.sample.cosmos.multi.database;

import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.azure.spring.sample.cosmos.multi.database.repository1.User1;
import com.azure.spring.sample.cosmos.multi.database.repository1.User1Repository;
import com.azure.spring.sample.cosmos.multi.database.repository2.User2;
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
    private User1Repository user1Repository;

    @Autowired
    @Qualifier("database1Template")
    private ReactiveCosmosTemplate database1Template;

    @Autowired
    @Qualifier("database2Template")
    private ReactiveCosmosTemplate database2Template;

    private final User1 user1 = new User1("1024", "1024@geek.com", "1k", "Mars");
    private static CosmosEntityInformation<User1, String> user1Info = new CosmosEntityInformation<>(User1.class);

    private final User2 user2 = new User2("2048", "2048@geek.com", "2k", "Mars");
    private static CosmosEntityInformation<User2, String> user2Info = new CosmosEntityInformation<>(User2.class);


    public static void main(String[] args) {
        SpringApplication.run(MultiDatabaseApplication.class, args);
    }

    public void run(String... var1) throws Exception {

        User1 database1UserGet = database1Template.findById(User1.class.getSimpleName(), user1.getId(), User1.class).block();
        // Same to userRepository1.findById(user.getId()).block()
        System.out.println(database1UserGet);
        User2 database2UserGet = database2Template.findById(User2.class.getSimpleName(), user2.getId(), User2.class).block();
        System.out.println(database2UserGet);
    }

    @PostConstruct
    public void setup() {
        database1Template.createContainerIfNotExists(user1Info).block();
        database1Template.insert(User1.class.getSimpleName(), user1, new PartitionKey(user1.getName())).block();
        // Same to this.userRepository1.save(user).block();
        database2Template.createContainerIfNotExists(user2Info).block();
        database2Template.insert(User2.class.getSimpleName(), user2, new PartitionKey(user2.getName())).block();
    }

    @PreDestroy
    public void cleanup() {
        database1Template.deleteAll(User1.class.getSimpleName(), User1.class).block();
        // Same to this.userRepository1.deleteAll().block();
        database2Template.deleteAll(User2.class.getSimpleName(), User2.class).block();
    }
}
