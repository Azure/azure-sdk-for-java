package com.azure.spring.cloud.integration.tests.cosmos;


import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
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
    private final String databaseName = "products";
    private final String containerName = "users";
    private final String partitionKeyPath = "/country";

    @Autowired
    private CosmosClient client;

    @Test
    public void testCosmosOperation() {
        LOGGER.info("CosmosIT begin.");
        LOGGER.info("Start creating cosmos database");
        client.createDatabaseIfNotExists(databaseName);
        CosmosDatabase database = client.getDatabase(databaseName);
        Assertions.assertNotNull(database);
        LOGGER.info("Finish creating cosmos database");
        LOGGER.info("Start creating cosmos container");
        database.createContainerIfNotExists(containerName, partitionKeyPath);
        CosmosContainer container = database.getContainer(containerName);
        Assertions.assertNotNull(container);
        LOGGER.info("Finish creating cosmos container");
        final User testUser = new User(
            "testId",
            "testFirstName",
            "testLastName",
            "test address line one"
        );
        container.createItem(testUser);
        CosmosPagedIterable<User> users = container.queryItems("SELECT * FROM c", new CosmosQueryRequestOptions(),
            User.class);
        if (users.stream().iterator().hasNext()) {
            User user = users.stream().iterator().next();
            Assertions.assertEquals(user.toString(), "testFirstName testLastName, test address line one");
        }
        LOGGER.info("CosmosIT end.");
    }

}
