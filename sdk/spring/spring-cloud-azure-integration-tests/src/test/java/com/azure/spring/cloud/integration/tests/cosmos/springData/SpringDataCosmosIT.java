// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests.cosmos.springData;

import com.azure.spring.cloud.integration.tests.cosmos.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles(value = {"spring-data-cosmos", "service-bus-jms"})
public class SpringDataCosmosIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringDataCosmosIT.class);
    private final String userId = "testSpringDataCosmos";

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSpringDataCosmosOperation() {
        LOGGER.info("SpringDataCosmosIT begin.");
        User testUser = new User(userId, "testFirstName", "testLastName", "test address line one");
        userRepository.save(testUser).block();
        User user = userRepository.findById(userId).block();
        Assertions.assertEquals(user.toString(), "testFirstName testLastName, test address line one");
        userRepository.delete(testUser).block();
        Assertions.assertNull(userRepository.findById(userId).block());
        LOGGER.info("SpringDataCosmosIT end.");
    }
}
