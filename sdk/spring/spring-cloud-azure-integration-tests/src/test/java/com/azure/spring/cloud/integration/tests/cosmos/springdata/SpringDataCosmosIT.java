// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests.cosmos.springdata;

import com.azure.spring.cloud.integration.tests.cosmos.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("cosmos-springdata")
public class SpringDataCosmosIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringDataCosmosIT.class);
    private static final String USER_ID = "testSpringDataCosmos";

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSpringDataCosmosOperation() {
        LOGGER.info("SpringDataCosmosIT begin.");
        User testUser = new User(USER_ID, "testFirstName", "testLastName", "test address line one");
        userRepository.save(testUser);
        Optional<User> user = userRepository.findById(USER_ID);
        Assertions.assertEquals(testUser, user.get());
        userRepository.delete(testUser);
        Assertions.assertFalse(userRepository.findById(USER_ID).isPresent());
        LOGGER.info("SpringDataCosmosIT end.");
    }
}
