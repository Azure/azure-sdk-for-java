// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests.cosmos;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("cosmos")
public class CosmosIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosIT.class);
    private final String databaseName = "TestDB";
    private final String containerName = "Users";

    @Autowired
    private CosmosClient client;

    @Test
    public void testCosmosOperation() {
        LOGGER.info("CosmosIT begin.");
        User testUser = new User(
            "testCosmos",
            "testFirstName",
            "testLastName",
            "test address line two"
        );
        CosmosContainer container = client.getDatabase(databaseName).getContainer(containerName);
        container.createItem(testUser);
        CosmosPagedIterable<User> users = container.queryItems("SELECT * FROM c WHERE c.id = 'testCosmos'",
            new CosmosQueryRequestOptions(),
            User.class);
        if (users.stream().iterator().hasNext()) {
            User user = users.stream().iterator().next();
            Assertions.assertEquals(user.toString(), "testFirstName testLastName, test address line two");
        }
        container.deleteItem(testUser, new CosmosItemRequestOptions());
        CosmosPagedIterable<User> usersDelete = container.queryItems("SELECT * FROM c WHERE c.id = 'testCosmos'",
            new CosmosQueryRequestOptions(),
            User.class);
        Assertions.assertFalse(usersDelete.iterator().hasNext());
        LOGGER.info("CosmosIT end.");
    }

}
