// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.sample.multi.database;

import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
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
    @Qualifier("secondaryDatabaseTemplate")
    private CosmosTemplate secondaryDatabaseTemplate;

    private final User user = new User("123", "1024@geek.com", "1k", "Mars");
    private static CosmosEntityInformation<User, String> userInfo = new CosmosEntityInformation<>(User.class);;

    public static void main(String[] args) {
        SpringApplication.run(MultiDatasourceApplication.class, args);
    }

    public void run(String... var1) throws Exception {
        final List<User> users = this.userRepository.findByEmailOrName(this.user.getEmail(), this.user.getName()).collectList().block();
        users.forEach(System.out::println);
        User result = secondaryDatabaseTemplate.findById(User.class.getSimpleName(),
            user.getId(), User.class);
        System.out.println(result);
    }

    @PostConstruct
    public void setup() {

        secondaryDatabaseTemplate.createContainerIfNotExists(userInfo);
        secondaryDatabaseTemplate.insert(User.class.getSimpleName(), user,
            new PartitionKey(user.getName()));
        this.userRepository.save(user).block();
    }

    @PreDestroy
    public void cleanup() {
        secondaryDatabaseTemplate.deleteAll(User.class.getSimpleName() , User.class);
        this.userRepository.deleteAll().block();
    }
}

