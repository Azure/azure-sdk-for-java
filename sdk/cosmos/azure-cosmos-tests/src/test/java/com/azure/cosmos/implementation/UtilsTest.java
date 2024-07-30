// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.apachecommons.lang.RandomStringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import java.util.List;
import java.util.stream.Collectors;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class UtilsTest {
    @Test(groups = {"unit"})
    public void parsingByteArrayAsObjectNode() {
        byte[] source = "{ 'a' : 'b' }".getBytes(StandardCharsets.UTF_8);
        ObjectNode objectNode = Utils.parse(source, ObjectNode.class, CosmosItemSerializer.DEFAULT_SERIALIZER);
        assertThat(objectNode.get("a").asText()).isEqualTo("b");
    }

    @Test(groups = {"unit"})
    public void parsingByteArrayAsJsonNode() {
        byte[] source = "5".getBytes(StandardCharsets.UTF_8);
        JsonNode jsonNode = Utils.parse(source, JsonNode.class, CosmosItemSerializer.DEFAULT_SERIALIZER);
        assertThat(jsonNode.asInt()).isEqualTo(5);
    }

    @Test(groups = {"unit"})
    public void errorMessageOnParsingByteArrayContainsOriginalContent() {
        byte[] source = RandomStringUtils.randomAlphabetic(600).getBytes(StandardCharsets.UTF_8);
        byte[] data = new byte[source.length - 2];
        System.arraycopy(source, 0, data, 0, data.length);

        try {
            Utils.parse(data, ObjectNode.class, CosmosItemSerializer.DEFAULT_SERIALIZER);
            fail("expected to fail");
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Failed to parse byte-array " + new String(data, StandardCharsets.UTF_8) + " to POJO.");
        }
    }

    @Test(groups = {"unit"})
    public void errorMessageOnParsingStringToJsonContainsOriginalContent() {
        String source = RandomStringUtils.randomAlphabetic(600);
        String data = source.substring(0, source.length() - 2);

        try {
            Utils.parse(data, ObjectNode.class);
            fail("expected to fail");
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Failed to parse string [" + data + "] to POJO.");
        }
    }

    @Test(groups = {"unit"})
    public void afterburnerBlackbirdChanges() {
        ObjectMapper objectMapper = Utils.getSimpleObjectMapper();

        List<String> moduleNames = objectMapper.getRegisteredModuleIds().stream()
            .map(Object::toString)
            .collect(Collectors.toList());

        int javaVersion = getJavaVersion();
        if (javaVersion == -1) {
            assertThat(moduleNames)
                .doesNotContain(AfterburnerModule.class.getName(), BlackbirdModule.class.getName());
        }
        else if (javaVersion < 11) {
            assertThat(moduleNames)
                .contains(AfterburnerModule.class.getName())
                .doesNotContain(BlackbirdModule.class.getName());
        }
        else {
            assertThat(moduleNames)
                .contains(BlackbirdModule.class.getName())
                .doesNotContain(AfterburnerModule.class.getName());
        }
    }

    private static int getJavaVersion() {
        int version = -1;
        try {
            String completeJavaVersion = System.getProperty("java.version");
            String[] versionElements = completeJavaVersion.split("\\.");
            int versionFirstPart = Integer.parseInt(versionElements[0]);
            // Java 8 or lower format is 1.6.0, 1.7.0, 1.7.0, 1.8.0
            // Java 9 or higher format is 9.0, 10.0, 11.0
            if (versionFirstPart == 1) {
                version = Integer.parseInt(versionElements[1]);
            } else {
                version = versionFirstPart;
            }
            return version;
        } catch (Exception ex) {
            // Consumed the exception we got during parsing
            // For unknown version we wil mark it as -1
            return version;
        }
    }
}
