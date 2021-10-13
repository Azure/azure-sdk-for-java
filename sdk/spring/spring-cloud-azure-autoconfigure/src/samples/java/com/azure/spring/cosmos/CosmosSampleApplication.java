// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cosmos;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.spring.cloud.autoconfigure.cosmos.AzureCosmosProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Optional;

@SpringBootApplication
public class CosmosSampleApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosSampleApplication.class);

    @Autowired
    private UserRepository repository;

    @Autowired
    private AzureKeyCredential azureKeyCredential;

    @Autowired
    private AzureCosmosProperties properties;

    /**
     * The secondaryKey is used to rotate key for authorizing request.
     */
    @Value("${secondary-key}")
    private String secondaryKey;

    public static void main(String[] args) {
        SpringApplication.run(CosmosSampleApplication.class, args);
    }

    public void run(String... var1) {
        final User testUser = new User("testId", "testFirstName",
            "testLastName", "test address line one");

        // Save the User class to Azure Cosmos DB database.
        final Mono<User> saveUserMono = repository.save(testUser);

        final Flux<User> firstNameUserFlux = repository.findByFirstName("testFirstName");

        //  Nothing happens until we subscribe to these Monos.
        //  findById will not return the user as user is not present.
        final Mono<User> findByIdMono = repository.findById(testUser.getId());
        final User findByIdUser = findByIdMono.block();
        Assert.isNull(findByIdUser, "User must be null");

        final User savedUser = saveUserMono.block();
        Assert.state(savedUser != null, "Saved user must not be null");
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
        LOGGER.info("findOne in User collection get result: {}", result.toString());

        switchKey();
    }

    /**
     * Switch cosmos authorization key
     */
    private void switchKey() {
        azureKeyCredential.update(secondaryKey);
        LOGGER.info("Switch to secondary key.");

        final User testUserUpdated = new User("testIdUpdated", "testFirstNameUpdated",
            "testLastNameUpdated", "test address Updated line one");
        final User saveUserUpdated = repository.save(testUserUpdated).block();
        Assert.state(saveUserUpdated != null, "Saved updated user must not be null");
        Assert.state(saveUserUpdated.getFirstName().equals(testUserUpdated.getFirstName()),
            "Saved updated user first name doesn't match");

        final Optional<User> optionalUserUpdatedResult = repository.findById(testUserUpdated.getId()).blockOptional();
        Assert.isTrue(optionalUserUpdatedResult.isPresent(), "Cannot find updated user.");
        final User updatedResult = optionalUserUpdatedResult.get();
        Assert.state(updatedResult.getFirstName().equals(testUserUpdated.getFirstName()),
            "query updated result firstName doesn't match!");
        Assert.state(updatedResult.getLastName().equals(testUserUpdated.getLastName()),
            "query updated result lastName doesn't match!");

        azureKeyCredential.update(properties.getKey());
        LOGGER.info("Switch back to key.");
        final Optional<User> userOptional = repository.findById(testUserUpdated.getId()).blockOptional();
        Assert.isTrue(userOptional.isPresent(), "Cannot find updated user.");
        Assert.state(updatedResult.getFirstName().equals(testUserUpdated.getFirstName()),
            "query updated result firstName doesn't match!");
        LOGGER.info("Finished key switch.");
    }

    @PostConstruct
    public void setup() {
        // For this example, remove all of the existing records.
        this.repository.deleteAll().block();
    }
}
