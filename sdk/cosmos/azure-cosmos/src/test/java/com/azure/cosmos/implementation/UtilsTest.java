// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.RandomStringUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class UtilsTest {
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
}
