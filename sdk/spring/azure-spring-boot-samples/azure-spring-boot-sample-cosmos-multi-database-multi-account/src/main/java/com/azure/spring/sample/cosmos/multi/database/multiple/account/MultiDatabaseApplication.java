// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.sample.cosmos.multi.database.multiple.account;

import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.azure.spring.sample.cosmos.multi.database.multiple.account.repository.cosmos.CosmosUser;
import com.azure.spring.sample.cosmos.multi.database.multiple.account.repository.cosmos.UserRepository;
import com.azure.spring.sample.cosmos.multi.database.multiple.account.repository.mysql.MysqlUser;
import com.azure.spring.sample.cosmos.multi.database.multiple.account.repository.mysql.MysqlUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

@SpringBootApplication
public class MultiDatabaseApplication implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MysqlUserRepository mysqlUserRepository;

    @Autowired
    @Qualifier("secondaryDatabaseTemplate")
    private CosmosTemplate secondaryDatabaseTemplate;

    @Autowired
    @Qualifier("primaryDatabaseTemplate")
    private ReactiveCosmosTemplate primaryDatabaseTemplate;

    private final CosmosUser cosmosUser = new CosmosUser("1024", "1024@geek.com", "1k", "Mars");
    private MysqlUser userForMYSQL;
    private static CosmosEntityInformation<CosmosUser, String> userInfo = new CosmosEntityInformation<>(CosmosUser.class);

    public static void main(String[] args) {
        SpringApplication.run(MultiDatabaseApplication.class, args);
    }

    public void run(String... var1) throws Exception {

        final List<CosmosUser> cosmosUsers = this.userRepository.findByEmailOrName(this.cosmosUser.getEmail(),
            this.cosmosUser.getName()).collectList().block();
        cosmosUsers.stream().forEach(this::insertValueToMYSQL);
        CosmosUser secondaryCosmosUserGet = secondaryDatabaseTemplate.findById(CosmosUser.class.getSimpleName(), cosmosUser.getId(), CosmosUser.class);
        System.out.println(secondaryCosmosUserGet);
        mysqlUserRepository.findAll().forEach(System.out::println);
    }

    public void insertValueToMYSQL(CosmosUser cosmosUser){
        userForMYSQL = new MysqlUser(cosmosUser.getId(), cosmosUser.getEmail(), cosmosUser.getName(), cosmosUser.getAddress());
        mysqlUserRepository.save(userForMYSQL);
    }

    @PostConstruct
    public void setup() {

        primaryDatabaseTemplate.createContainerIfNotExists(userInfo).block();
        primaryDatabaseTemplate.insert(CosmosUser.class.getSimpleName(), cosmosUser,
            new PartitionKey(cosmosUser.getName())).block(); // Same to this.userRepository.save(user).block();
        secondaryDatabaseTemplate.createContainerIfNotExists(userInfo);
        secondaryDatabaseTemplate.insert(CosmosUser.class.getSimpleName(), cosmosUser, new PartitionKey(cosmosUser.getName()));
   }

    @PreDestroy
    public void cleanup() {
        primaryDatabaseTemplate.deleteAll(CosmosUser.class.getSimpleName(),
            CosmosUser.class).block(); // Same to this.userRepository.deleteAll().block();
        secondaryDatabaseTemplate.deleteAll(CosmosUser.class.getSimpleName() , CosmosUser.class);
        mysqlUserRepository.deleteAll();
    }
}

