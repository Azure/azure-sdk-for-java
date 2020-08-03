// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.Test;

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
