// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.RandomStringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.RandomUtils;
import com.azure.cosmos.implementation.encryption.api.CosmosEncryptionAlgorithm;
import com.azure.cosmos.implementation.encryption.api.DataEncryptionKey;
import com.azure.cosmos.implementation.encryption.api.EncryptionOptions;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DataEncryptionKeyPropertiesTest {

    @Test(groups = "unit")
    public void sameSerializationAsDotNet() throws Exception {
        byte[] bytes = TestUtils.getResourceAsByteArray("./encryption/dotnet/DataEncryptionKeyProperties.json");

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.readValue(bytes, ObjectNode.class);

        DataEncryptionKeyProperties dataEncryptionKeyProperties = Utils.getSimpleObjectMapper().readValue(bytes, DataEncryptionKeyProperties.class);
        objectNode.remove("_attachments");
        String expected = objectMapper.writeValueAsString(objectNode);
        String actual = objectMapper.writeValueAsString(dataEncryptionKeyProperties);
        assertThat(actual).isEqualTo(expected);
    }
}
