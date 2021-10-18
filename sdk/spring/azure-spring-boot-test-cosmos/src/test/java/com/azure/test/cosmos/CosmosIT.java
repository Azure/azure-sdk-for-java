// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.cosmos;

import com.azure.spring.test.AppRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class CosmosIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosIT.class);
    private static final String AZURE_COSMOS_ENDPOINT = System.getenv("AZURE_COSMOS_ENDPOINT");
    private static final String AZURE_COSMOS_ACCOUNT_KEY = System.getenv("AZURE_COSMOS_ACCOUNT_KEY");
    private static final String AZURE_COSMOS_DATABASE_NAME = System.getenv("AZURE_COSMOS_DATABASE_NAME");

    @Test
    public void testCosmosOperation() {
        try (AppRunner app = new AppRunner(DummyApp.class)) {
            //set properties
            app.property("spring.cloud.azure.cosmos.endpoint", AZURE_COSMOS_ENDPOINT);
            app.property("spring.cloud.azure.cosmos.key", AZURE_COSMOS_ACCOUNT_KEY);
            app.property("spring.cloud.azure.cosmos.database", AZURE_COSMOS_DATABASE_NAME);
            app.property("spring.cloud.azure.cosmos.populateQueryMetrics", String.valueOf(true));

            //start app
            app.start();
            final UserRepository repository = app.getBean(UserRepository.class);
            final User testUser = new User(
                "testId",
                "testFirstName",
                "testLastName",
                "test address line one"
            );

            try {
                Mono<Void> voidMono = repository.delete(testUser);
                voidMono.block(); // Delete testUser if it already exists.
            } catch (Exception ignored) {

            }

            // Save the User class to Azure Cosmos DB database.
            final Mono<User> saveUserMono = repository.save(testUser);
            final Flux<User> firstNameUserFlux = repository.findByFirstName("testFirstName");

            //  Nothing happens until we subscribe to these Monos.
            //  findById will not return the user as user is not present.
            final Mono<User> findByIdMono = repository.findById(testUser.getId());
            final User findByIdUser = findByIdMono.block();
            Assertions.assertNull(findByIdUser, "User must be null");

            final User savedUser = saveUserMono.block();
            Assertions.assertNotNull(savedUser, "Saved user must not be null");
            Assertions.assertEquals(testUser.getFirstName(), savedUser.getFirstName(),
                "Saved user first name doesn't match");

            firstNameUserFlux.collectList().block();
            final Optional<User> optionalUserResult = repository.findById(testUser.getId()).blockOptional();
            Assertions.assertTrue(optionalUserResult.isPresent(), "Cannot find user.");

            final User result = optionalUserResult.get();
            Assertions.assertEquals(testUser.getFirstName(), result.getFirstName(),
                "query result firstName doesn't match!");
            Assertions.assertEquals(testUser.getLastName(), result.getLastName(),
                "query result lastName doesn't match!");

            LOGGER.info("findOne in User collection get result: {}", result);
        }
    }
}
