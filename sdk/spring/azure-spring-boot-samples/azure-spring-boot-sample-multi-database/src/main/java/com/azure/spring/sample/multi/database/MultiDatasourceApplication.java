// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.sample.multi.database;

import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.azure.spring.sample.multi.database.database1.User;
import com.azure.spring.sample.multi.database.database1.UserRepository;
import com.azure.spring.sample.multi.database.database2.Book;
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
    @Qualifier("secondaryDatabase2Template")
    private CosmosTemplate secondaryDatabase2Template;

    @Autowired
    @Qualifier("primaryDatabase2Template")
    private ReactiveCosmosTemplate primaryDatabase2Template;

    private final User user = new User("1024", "1024@geek.com", "1k", "Mars");
    private UserForMYSQL user2;
    private static CosmosEntityInformation<User, String> userInfo = new CosmosEntityInformation<>(User.class);

    private final Book book = new Book("9780792745488", "Zen and the Art of Motorcycle Maintenance", "Robert M. Pirsig");
    private static CosmosEntityInformation<Book, String> bookInfo = new CosmosEntityInformation<>(Book.class);

    public static void main(String[] args) {
        SpringApplication.run(MultiDatasourceApplication.class, args);
    }

    public void run(String... var1) throws Exception {

        final List<User> users = this.userRepository.findByEmailOrName(this.user.getEmail(), this.user.getName()).collectList().block();
        users.forEach(System.out::println);
        users.stream().forEach(this::insertValueToMYSQL);

        Book bookGet = primaryDatabase2Template.findById(Book.class.getSimpleName(), book.getIbsn(), Book.class).block();
        System.out.println(bookGet);

        User result = secondaryDatabaseTemplate.findById(User.class.getSimpleName(),
            user.getId(), User.class);
        System.out.println(result);
        Book result2 = secondaryDatabase2Template.findById(Book.class.getSimpleName(),
            book.getIbsn(), Book.class);
        System.out.println(result2);

        userRepositoryForMYSQL.findAll().forEach(System.out::println);
    }

    public void insertValueToMYSQL(User user){
        user2 = new UserForMYSQL(user.getId(), user.getEmail(), user.getName(), user.getAddress());
        userRepositoryForMYSQL.save(user2);
    }

    @PostConstruct
    public void setup() {

        secondaryDatabaseTemplate.createContainerIfNotExists(userInfo);
        secondaryDatabaseTemplate.insert(User.class.getSimpleName(), user,
            new PartitionKey(user.getName()));
        secondaryDatabase2Template.createContainerIfNotExists(bookInfo);
        secondaryDatabase2Template.insert(Book.class.getSimpleName(), book,
            new PartitionKey(book.getName()));

        primaryDatabase2Template.createContainerIfNotExists(bookInfo).block();
        primaryDatabase2Template.insert(Book.class.getSimpleName(), book,
            new PartitionKey(book.getName())).block();
        this.userRepository.save(user).block();
    }

    @PreDestroy
    public void cleanup() {
        userRepositoryForMYSQL.delete(user2);
        secondaryDatabaseTemplate.deleteAll(User.class.getSimpleName() , User.class);
        secondaryDatabase2Template.deleteAll(Book.class.getSimpleName() , Book.class);
        primaryDatabase2Template.deleteAll(Book.class.getSimpleName(), Book.class).block();
        this.userRepository.deleteAll().block();
    }
}

