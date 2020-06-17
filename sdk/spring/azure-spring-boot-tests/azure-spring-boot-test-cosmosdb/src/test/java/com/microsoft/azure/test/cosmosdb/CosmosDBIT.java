// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.test.cosmosdb;

import com.microsoft.azure.spring.autoconfigure.aad.AADAuthenticationFilterAutoConfiguration;
import com.microsoft.azure.test.utils.AppRunner;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class CosmosDBIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosDBIT.class);
    private static final String AZURE_COSMOSDB_ENDPOINT = System.getenv("AZURE_COSMOSDB_ENDPOINT");
    private static final String AZURE_COSMOSDB_ACCOUNT_KEY = System.getenv("AZURE_COSMOSDB_ACCOUNT_KEY");
    private static final String AZURE_COSMOSDB_DATABASE_NAME = System.getenv("AZURE_COSMOSDB_DATABASE_NAME");

    @Test(expected = NoSuchBeanDefinitionException.class)
    @Ignore
    public void testCosmosStarterIsolating() {
        try (AppRunner app = new AppRunner(DummyApp.class)) {
            //set properties
            app.property("azure.cosmosdb.uri", AZURE_COSMOSDB_ENDPOINT);
            app.property("azure.cosmosdb.key", AZURE_COSMOSDB_ACCOUNT_KEY);
            app.property("azure.cosmosdb.database", AZURE_COSMOSDB_DATABASE_NAME);
            app.property("azure.cosmosdb.populateQueryMetrics", String.valueOf(true));

            //start app
            app.start();
            app.getBean(AADAuthenticationFilterAutoConfiguration.class);
        }
    }

    @Test
    public void testCosmosOperation() {
        try (AppRunner app = new AppRunner(DummyApp.class)) {
            //set properties
            app.property("azure.cosmosdb.uri", AZURE_COSMOSDB_ENDPOINT);
            app.property("azure.cosmosdb.key", AZURE_COSMOSDB_ACCOUNT_KEY);
            app.property("azure.cosmosdb.database", AZURE_COSMOSDB_DATABASE_NAME);
            app.property("azure.cosmosdb.populateQueryMetrics", String.valueOf(true));

            //start app
            app.start();
            final UserRepository repository = app.getBean(UserRepository.class);
            final User testUser = new User("testId",
                "testFirstName",
                "testLastName",
                "test address line one");

            // Save the User class to Azure CosmosDB database.
            final Mono<User> saveUserMono = repository.save(testUser);
            final Flux<User> firstNameUserFlux = repository.findByFirstName("testFirstName");

            //  Nothing happens until we subscribe to these Monos.
            //  findById will not return the user as user is not present.
            final Mono<User> findByIdMono = repository.findById(testUser.getId());
            final User findByIdUser = findByIdMono.block();
            Assert.assertNull("User must be null", findByIdUser);

            final User savedUser = saveUserMono.block();
            Assert.assertNotNull("Saved user must not be null", savedUser);
            Assert.assertEquals("Saved user first name doesn't match",
                testUser.getFirstName(), savedUser.getFirstName());

            firstNameUserFlux.collectList().block();
            final Optional<User> optionalUserResult = repository.findById(testUser.getId()).blockOptional();
            Assert.assertTrue("Cannot find user.", optionalUserResult.isPresent());

            final User result = optionalUserResult.get();
            Assert.assertEquals("query result firstName doesn't match!",
                testUser.getFirstName(), result.getFirstName());
            Assert.assertEquals("query result lastName doesn't match!", testUser.getLastName(), result.getLastName());

            LOGGER.info("findOne in User collection get result: {}", result.toString());
        }
    }
}
