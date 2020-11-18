// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

public class MembershipsTest {

    @Test
    public void nextLinkJsonTest() throws JsonProcessingException {
        final String aadJson = "{\"odata.nextLink\" : \"url\"}";
        final String graphJson = "{\"@odata.nextLink\" : \"url\"}";
        ObjectMapper mapper = new ObjectMapper();
        Memberships memberships = mapper.readValue(aadJson, Memberships.class);
        Assert.assertEquals(memberships.getOdataNextLink(), "url");
        memberships = mapper.readValue(graphJson, Memberships.class);
        Assert.assertEquals(memberships.getOdataNextLink(), "url");
    }
}
