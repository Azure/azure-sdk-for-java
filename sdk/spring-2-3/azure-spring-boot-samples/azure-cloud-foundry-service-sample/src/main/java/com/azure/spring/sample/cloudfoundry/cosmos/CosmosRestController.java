// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.cloudfoundry.cosmos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@RestController
public class CosmosRestController {

    private static final Logger LOG = LoggerFactory.getLogger(CosmosRestController.class);

    private static final String CR = "</BR>";

    @Autowired
    private UserRepository repository;

    @RequestMapping(value = "/cosmos", method = RequestMethod.GET)
    @ResponseBody
    public String createUser(HttpServletResponse response) {
        final StringBuilder result = new StringBuilder();
        final User testUser = new User("testId", "testFirstName", "testLastName",
            "test address line one");

        LOG.debug("Deleting all records in repo... {}", CR);
        repository.deleteAll();

        LOG.debug("Saving new User object...");
        repository.save(testUser);

        LOG.debug("Retrieving User object...");
        final Optional<User> opUser = repository.findById(testUser.getId());
        Assert.state(opUser.isPresent(), "Can not find User.");

        final User user = opUser.get();
        Assert.state(user.getFirstName().equals(testUser.getFirstName()), "query FirstName unmatched!");
        Assert.state(user.getLastName().equals(testUser.getLastName()), "query LastName unmatched!");

        LOG.debug("UserRepository.findById() result: {}", user.toString());
        result.append("UserRepository.findById() result: ").append(user.toString()).append(CR);

        return result.toString();
    }
}
