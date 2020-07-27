// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.multidatasource;

import com.azure.cosmos.multidatasource.primarydatasource.first.User;
import com.azure.cosmos.multidatasource.primarydatasource.first.UserRepository;
import com.azure.cosmos.multidatasource.secondarydatasource.first.Book;
import com.azure.cosmos.multidatasource.secondarydatasource.first.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 */
@SpringBootApplication
public class MultiDatasourceApplication implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;


    private final User user = new User("1024", "1024@geek.com", "1k", "Mars");
    private final Book book = new Book("9780792745488", "Zen and the Art of Motorcycle Maintenance", "Robert M. Pirsig");


    public static void main(String[] args) {
        SpringApplication.run(MultiDatasourceApplication.class, args);
    }

    @Override
    public void run(String... args) {
        final List<User> users = this.userRepository.findByEmailOrName(this.user.getEmail(), this.user.getName()).collectList().block();
        users.forEach(System.out::println);
        final Book book = this.bookRepository.findById("9780792745488").block();
        System.out.println(book);
    }

    @PostConstruct
    public void setup() {
        this.userRepository.save(user).block();
        this.bookRepository.save(book).block();

    }

    @PreDestroy
    public void cleanup() {
        this.userRepository.deleteAll().block();
        this.bookRepository.deleteAll().block();
    }
}
