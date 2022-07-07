package com.azure.spring.cloud.integration.tests.cosmos;


import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest(classes = CosmosIT.class)
@ActiveProfiles("cosmos")
public class CosmosIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosIT.class);
    private final String databaseName = "products";
    private final String containerName = "users";
    private final String partitionKeyPath = "/country";
    @Value("${spring.cloud.azure.cosmos.endpoint1}")
    private String endpoint;
    @Value("${spring.cloud.azure.cosmos.key1}")
    private String key;

    @Test
    public void testCosmosOperation() {
        LOGGER.info("Start creating cosmos client");
        CosmosClient client = new CosmosClientBuilder()
            .endpoint(endpoint)
            .key(key)
            .buildClient();
        LOGGER.info("Finish creating cosmos client");
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
    }

}
