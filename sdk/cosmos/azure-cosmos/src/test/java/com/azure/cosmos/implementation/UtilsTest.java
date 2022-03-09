// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.RandomStringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class UtilsTest {
    @Test(groups = {"unit"})
    public void parsingByteArrayAsObjectNode() {
        byte[] source = "{ 'a' : 'b' }".getBytes(StandardCharsets.UTF_8);
        ObjectNode objectNode = Utils.parse(source, ObjectNode.class);
        assertThat(objectNode.get("a").asText()).isEqualTo("b");
    }

    @Test(groups = {"unit"})
    public void parsingByteArrayAsJsonNode() {
        byte[] source = "5".getBytes(StandardCharsets.UTF_8);
        JsonNode jsonNode = Utils.parse(source, JsonNode.class);
        assertThat(jsonNode.asInt()).isEqualTo(5);
    }

    @Test(groups = {"unit"})
    public void errorMessageOnParsingByteArrayContainsOriginalContent() {
        byte[] source = RandomStringUtils.randomAlphabetic(600).getBytes(StandardCharsets.UTF_8);
        byte[] data = new byte[source.length - 2];
        System.arraycopy(source, 0, data, 0, data.length);

        try {
            Utils.parse(data, ObjectNode.class);
            fail("expected to fail");
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Failed to parse byte-array " + Arrays.toString(data) + " to POJO.");
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
    public void afterBurnerChanges() {
        ObjectMapper objectMapper = Utils.getSimpleObjectMapper();
        for (Object moduleName : objectMapper.getRegisteredModuleIds()) {
            if (moduleName.toString().contains("AfterburnerModule")) {
                int javaVersion = getJavaVersion();
                if (javaVersion == -1 || javaVersion >= 16) {
                    fail("AfterBurner should not be register for java " + javaVersion);
                }
            }
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
