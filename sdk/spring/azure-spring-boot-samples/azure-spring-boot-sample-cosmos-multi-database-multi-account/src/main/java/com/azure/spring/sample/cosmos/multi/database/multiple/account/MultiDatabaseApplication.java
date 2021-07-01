// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.sample.cosmos.multi.database.multiple.account;

import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.azure.spring.sample.cosmos.multi.database.multiple.account.repository.cosmos.CosmosUser;
import com.azure.spring.sample.cosmos.multi.database.multiple.account.repository.cosmos.CosmosUserRepository;
import com.azure.spring.sample.cosmos.multi.database.multiple.account.repository.mysql.MysqlUser;
import com.azure.spring.sample.cosmos.multi.database.multiple.account.repository.mysql.MysqlUserRepository;
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
    private CosmosUserRepository cosmosUserRepository;

    @Autowired
    private MysqlUserRepository mysqlUserRepository;

    @Autowired
    @Qualifier("secondaryDatabaseTemplate")
    private CosmosTemplate secondaryDatabaseTemplate;

    @Autowired
    @Qualifier("primaryDatabaseTemplate")
    private ReactiveCosmosTemplate primaryDatabaseTemplate;

    private final CosmosUser cosmosUser = new CosmosUser("1024", "1024@geek.com", "1k", "Mars");
    private static CosmosEntityInformation<CosmosUser, String> userInfo = new CosmosEntityInformation<>(CosmosUser.class);

    public static void main(String[] args) {
        SpringApplication.run(MultiDatabaseApplication.class, args);
    }

    public void run(String... var1) throws Exception {

        CosmosUser cosmosUserGet = primaryDatabaseTemplate.findById(cosmosUser.getId(), cosmosUser.getClass()).block();
        // Same to this.cosmosUserRepository.findById(cosmosUser.getId()).block();
        MysqlUser mysqlUser = new MysqlUser(cosmosUserGet.getId(), cosmosUserGet.getEmail(), cosmosUserGet.getName(), cosmosUserGet.getAddress());
        mysqlUserRepository.save(mysqlUser);
        mysqlUserRepository.findAll().forEach(System.out::println);
        CosmosUser secondaryCosmosUserGet = secondaryDatabaseTemplate.findById(CosmosUser.class.getSimpleName(), cosmosUser.getId(), CosmosUser.class);
        System.out.println(secondaryCosmosUserGet);
    }


    @PostConstruct
    public void setup() {
        primaryDatabaseTemplate.createContainerIfNotExists(userInfo).block();
        primaryDatabaseTemplate.insert(CosmosUser.class.getSimpleName(), cosmosUser, new PartitionKey(cosmosUser.getName())).block();
        // Same to this.cosmosUserRepository.save(user).block();
        secondaryDatabaseTemplate.createContainerIfNotExists(userInfo);
        secondaryDatabaseTemplate.insert(CosmosUser.class.getSimpleName(), cosmosUser, new PartitionKey(cosmosUser.getName()));
   }

    @PreDestroy
    public void cleanup() {
        primaryDatabaseTemplate.deleteAll(CosmosUser.class.getSimpleName(), CosmosUser.class).block();
        // Same to this.cosmosUserRepository.deleteAll().block();
        secondaryDatabaseTemplate.deleteAll(CosmosUser.class.getSimpleName() , CosmosUser.class);
        mysqlUserRepository.deleteAll();
    }
}

