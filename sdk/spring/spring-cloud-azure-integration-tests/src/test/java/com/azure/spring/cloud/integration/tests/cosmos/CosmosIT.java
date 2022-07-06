package com.azure.spring.cloud.integration.tests.cosmos;


import com.azure.cosmos.CosmosClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;


@ActiveProfiles("cosmos")
public class CosmosIT {

    @Autowired
    private static CosmosClient client;

    @Test
    public void testCosmosOperation() {
        Assertions.assertThrows(RuntimeException.class, () -> client.getDatabase("users"));
        client.createDatabase("users");
        Assertions.assertNotNull(client.getDatabase("users"));
    }
}
