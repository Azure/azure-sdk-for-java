// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package sample.convert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Optional;

@SpringBootApplication
public class ConvertSampleApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertSampleApplication.class);

    @Autowired
    private UserRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(ConvertSampleApplication.class, args);
    }

    public void run(String... var1) throws Exception {

        final User testUser = new User("testId", "testFirstName", "testLastName",
            "test address line one");

        // Save the User class to Azure CosmosDB database.
        final Mono<User> saveUserMono = repository.save(testUser);

        final Flux<User> firstNameUserFlux = repository.findByFirstName("testFirstName");

        // Nothing happens until we subscribe to these Monos.
        // findById will not return the user as user is not present.
        final Mono<User> findByIdMono = repository.findById(testUser.getId());
        final User findByIdUser = findByIdMono.block();
        Assert.isNull(findByIdUser, "User must be null");

        final User savedUser = saveUserMono.block();
        Assert.state(savedUser != null,
            "Saved user must not be null");
        Assert.state(savedUser.getFirstName().equals(testUser.getFirstName()),
            "Saved user first name doesn't match");

        firstNameUserFlux.collectList().block();

        final Optional<User> optionalUserResult = repository.findById(testUser.getId()).blockOptional();
        Assert.isTrue(optionalUserResult.isPresent(), "Cannot find user.");

        final User result = optionalUserResult.get();
        Assert.state(result.getFirstName().equals(testUser.getFirstName()),
            "query result firstName doesn't match!");
        Assert.state(result.getLastName().equals(testUser.getLastName()),
            "query result lastName doesn't match!");

        LOGGER.info("findOne in User collection get result: {}", result.getFirstName());
    }

    @PostConstruct
    public void setup() {
        // For this example, remove all of the existing records.
        this.repository.deleteAll().block();
    }

}
