// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.sample.multi.database;

import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
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
import java.util.List;

@SpringBootApplication
public class MultiDatasourceApplication implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRepositoryForMYSQL userRepositoryForMYSQL;

    @Autowired
    @Qualifier("secondaryDatabaseTemplate")
    private CosmosTemplate secondaryDatabaseTemplate;

    @Autowired
    @Qualifier("primaryDatabaseTemplate")
    private ReactiveCosmosTemplate primaryDatabaseTemplate;

    private final User user = new User("1024", "1024@geek.com", "1k", "Mars");
    private UserForMYSQL userForMYSQL;
    private static CosmosEntityInformation<User, String> userInfo = new CosmosEntityInformation<>(User.class);

    public static void main(String[] args) {
        SpringApplication.run(MultiDatasourceApplication.class, args);
    }

    public void run(String... var1) throws Exception {

        final List<User> users = this.userRepository.findByEmailOrName(this.user.getEmail(),
            this.user.getName()).collectList().block();
        users.stream().forEach(this::insertValueToMYSQL);
        User secondaryUserGet = secondaryDatabaseTemplate.findById(User.class.getSimpleName(), user.getId(), User.class);
        System.out.println(secondaryUserGet);
        userRepositoryForMYSQL.findAll().forEach(System.out::println);
    }

    public void insertValueToMYSQL(User user){
        userForMYSQL = new UserForMYSQL(user.getId(), user.getEmail(), user.getName(), user.getAddress());
        userRepositoryForMYSQL.save(userForMYSQL);
    }

    @PostConstruct
    public void setup() {

        primaryDatabaseTemplate.createContainerIfNotExists(userInfo).block();
        primaryDatabaseTemplate.insert(User.class.getSimpleName(), user,
            new PartitionKey(user.getName())).block(); // Same to this.userRepository.save(user).block();
        secondaryDatabaseTemplate.createContainerIfNotExists(userInfo);
        secondaryDatabaseTemplate.insert(User.class.getSimpleName(), user, new PartitionKey(user.getName()));
   }

    @PreDestroy
    public void cleanup() {
        primaryDatabaseTemplate.deleteAll(User.class.getSimpleName(),
            User.class).block(); // Same to this.userRepository.deleteAll().block();
        secondaryDatabaseTemplate.deleteAll(User.class.getSimpleName() , User.class);
        userRepositoryForMYSQL.deleteAll();
    }
}

