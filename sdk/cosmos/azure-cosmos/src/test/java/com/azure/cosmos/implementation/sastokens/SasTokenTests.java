// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.sastokens;

import com.azure.cosmos.models.SasTokenProperties;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

public class SasTokenTests {
    private final static String TEST_KEY = "C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==";
    private final static String TEST_EXPECTED_SASTOKEN = "type=sas&ver=1.0&sig=rQNGPRFDzgJbi9/W4fUmuLBGjgtSKjJCWsrtNO72NKk=,dXNlcjEKCi9kYnMvZGIxL2NvbGxzL2NvbGwxLwoKMAoxQzIwCjAKNjAKMApGRkZGRkZGRgowCg";
    private final static String TEST_EXPECTED_PAYLOAD = "user1\n" +
        "\n" +
        "/dbs/db1/colls/coll1/\n" +
        "\n" +
        "0\n" +
        "1C20\n" +
        "0\n" +
        "60\n" +
        "0\n" +
        "FFFFFFFF\n" +
        "0\n";

    @Test(groups = "unit")
    public void createSimpleSasToken() {
        SasTokenProperties sasTokenProperties = SasTokenProperties.create("user1", "db1", "coll1")
            .setStartTime(Instant.ofEpochMilli(0));
        assertThat("user1").isEqualTo(sasTokenProperties.getUser());
        assertThat("").isEqualTo(sasTokenProperties.getUserTag());
        assertThat("db1").isEqualTo(sasTokenProperties.getDatabaseName());
        assertThat("coll1").isEqualTo(sasTokenProperties.getContainerName());
        assertThat("").isEqualTo(sasTokenProperties.getUserTag());
        assertThat("").isEqualTo(sasTokenProperties.getUserTag());

        String sasTokenValue = sasTokenProperties.getSasTokenValueUsingHMAC(TEST_KEY);
        assertThat(TEST_EXPECTED_SASTOKEN)
            .isEqualTo(sasTokenValue);

        String[] tokenSegments = sasTokenValue.split("&");
        assertThat(3).isEqualTo(tokenSegments.length);

        String[] sasTokenParts = tokenSegments[2].split(",");
        assertThat(2).isEqualTo(sasTokenParts.length);

        byte[] decodedString = Base64.getDecoder().decode(sasTokenParts[1]);
        String sasTokenPayload = new String(decodedString, StandardCharsets.UTF_8);
        assertThat(TEST_EXPECTED_PAYLOAD)
            .isEqualTo(sasTokenPayload);
    }
}
