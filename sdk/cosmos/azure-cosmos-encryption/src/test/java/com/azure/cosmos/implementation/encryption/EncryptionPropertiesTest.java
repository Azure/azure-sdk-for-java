// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.apachecommons.lang.RandomStringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.RandomUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class EncryptionPropertiesTest {

    @Test(groups = "unit")
    public void fromJsonNode() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();

        objectNode.put("_ef", 1);
        objectNode.put("_ea", "myAlgo");
        objectNode.put("_en", "keyId");
        objectNode.put("_ed", "AwQ=");

        EncryptionProperties encryptionProperties = EncryptionProperties.fromObjectNode(objectNode);

        assertThat(encryptionProperties.getEncryptionFormatVersion()).isEqualTo(1);
        assertThat(encryptionProperties.getDataEncryptionKeyId()).isEqualTo("keyId");
        assertThat(encryptionProperties.getEncryptionAlgorithm()).isEqualTo("myAlgo");
        assertThat(encryptionProperties.getEncryptedData()).isEqualTo(new byte[]{3, 4});
    }

    @Test(groups = "unit")
    public void toJsonNode() throws Exception {
        EncryptionProperties encryptionProperties = new EncryptionProperties(1, "myAlgo", "keyId", new byte[]{3, 4});
        ObjectNode objectNode = encryptionProperties.toObjectNode();

        assertThat(objectNode.get("_ef").isInt()).isTrue();
        assertThat(objectNode.get("_ef").asInt()).isEqualTo(1);

        assertThat(objectNode.get("_en").isTextual()).isTrue();
        assertThat(objectNode.get("_en").asText()).isEqualTo("keyId");


        assertThat(objectNode.get("_ea").isTextual()).isTrue();
        assertThat(objectNode.get("_ea").asText()).isEqualTo("myAlgo");


        assertThat(objectNode.get("_ed").isBinary()).isTrue();
        assertThat(objectNode.get("_ed").binaryValue()).isEqualTo(new byte[] {3, 4});

        assertThat(objectNode.fieldNames()).toIterable().hasSize(4);
    }

    @Test(groups = "unit")
    public void serialize() throws Exception {
        EncryptionProperties encryptionProperties = new EncryptionProperties(1, "myAlgo", "2", new byte[]{3, 4});
        String encryptionPropertiesAsString = EncryptionProperties.getObjectWriter().writeValueAsString(encryptionProperties);

        assertThat(encryptionPropertiesAsString).isEqualTo("{\"_ef\":1,\"_ea\":\"myAlgo\",\"_en\":\"2\",\"_ed\":\"AwQ=\"}");
    }

    @Test(groups = "unit")
    public void deserialize() throws Exception {
        EncryptionProperties parsedEncryptionProperties = EncryptionProperties.getObjectReader().readValue("{\"_ef\":1,\"_en\":\"2\",\"_ea\":\"myAlgo\",\"_ed\":\"AwQ=\"}");

        assertThat(parsedEncryptionProperties.getEncryptionFormatVersion()).isEqualTo(1);
        assertThat(parsedEncryptionProperties.getDataEncryptionKeyId()).isEqualTo("2");
        assertThat(parsedEncryptionProperties.getEncryptedData()).isEqualTo(new byte[]{3, 4});
    }

    @Test(groups = "unit")
    public void e2e() throws Exception {
        EncryptionProperties encryptionProperties = new EncryptionProperties(
            RandomUtils.nextInt(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            RandomStringUtils.randomAlphabetic(10).getBytes(StandardCharsets.UTF_8));

        byte[] encryptionPropertiesAsBytes = EncryptionProperties.getObjectWriter().writeValueAsBytes(encryptionProperties);
        EncryptionProperties parsedEncryptionProperties = EncryptionProperties.getObjectReader().readValue(encryptionPropertiesAsBytes);

        assertThat(parsedEncryptionProperties.getEncryptionFormatVersion()).isEqualTo(encryptionProperties.getEncryptionFormatVersion());
        assertThat(parsedEncryptionProperties.getDataEncryptionKeyId()).isEqualTo(encryptionProperties.getDataEncryptionKeyId());
        assertThat(parsedEncryptionProperties.getEncryptedData()).isEqualTo(encryptionProperties.getEncryptedData());
    }
}

