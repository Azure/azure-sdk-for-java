// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.HttpConstants;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosHttp2SensitivityDetectorTests {

    @Test(groups = "unit")
    public void authorizationHeaderIsSensitive() {
        CosmosHttp2SensitivityDetector detector = CosmosHttp2SensitivityDetector.INSTANCE;

        assertThat(detector.isSensitive("authorization", "type=master&ver=1.0&sig=abc123"))
            .as("authorization header should be marked sensitive")
            .isTrue();
    }

    @Test(groups = "unit")
    public void authorizationHeaderIsCaseInsensitive() {
        CosmosHttp2SensitivityDetector detector = CosmosHttp2SensitivityDetector.INSTANCE;

        assertThat(detector.isSensitive("Authorization", "Bearer token123"))
            .as("Authorization (mixed case) should be marked sensitive")
            .isTrue();

        assertThat(detector.isSensitive("AUTHORIZATION", "Bearer token123"))
            .as("AUTHORIZATION (upper case) should be marked sensitive")
            .isTrue();
    }

    @Test(groups = "unit")
    public void xmsDateHeaderIsSensitive() {
        CosmosHttp2SensitivityDetector detector = CosmosHttp2SensitivityDetector.INSTANCE;

        assertThat(detector.isSensitive("x-ms-date", "Tue, 18 Mar 2025 03:00:00 GMT"))
            .as("x-ms-date should be marked sensitive (unique per-request)")
            .isTrue();

        assertThat(detector.isSensitive("X-MS-DATE", "Tue, 18 Mar 2025 03:00:00 GMT"))
            .as("X-MS-DATE (upper case) should be marked sensitive")
            .isTrue();
    }

    @Test(groups = "unit")
    public void partitionKeyHeaderIsSensitive() {
        CosmosHttp2SensitivityDetector detector = CosmosHttp2SensitivityDetector.INSTANCE;

        assertThat(detector.isSensitive("x-ms-documentdb-partitionkey", "[\"myPartitionKey\"]"))
            .as("x-ms-documentdb-partitionkey should be marked sensitive (high cardinality)")
            .isTrue();

        assertThat(detector.isSensitive("X-Ms-Documentdb-Partitionkey", "[\"key\"]"))
            .as("X-Ms-Documentdb-Partitionkey (mixed case) should be marked sensitive")
            .isTrue();
    }

    @Test(groups = "unit")
    public void allSensitiveHeaderConstants() {
        CosmosHttp2SensitivityDetector detector = CosmosHttp2SensitivityDetector.INSTANCE;

        assertThat(detector.isSensitive(HttpConstants.HttpHeaders.AUTHORIZATION, "sig=abc"))
            .as("HttpConstants.HttpHeaders.AUTHORIZATION should be marked sensitive")
            .isTrue();
        assertThat(detector.isSensitive(HttpConstants.HttpHeaders.X_DATE, "timestamp"))
            .as("HttpConstants.HttpHeaders.X_DATE should be marked sensitive")
            .isTrue();
        assertThat(detector.isSensitive(HttpConstants.HttpHeaders.PARTITION_KEY, "[\"pk\"]"))
            .as("HttpConstants.HttpHeaders.PARTITION_KEY should be marked sensitive")
            .isTrue();
    }

    @Test(groups = "unit")
    public void nonSensitiveHeadersAreNotSensitive() {
        CosmosHttp2SensitivityDetector detector = CosmosHttp2SensitivityDetector.INSTANCE;

        assertThat(detector.isSensitive("User-Agent", "cosmos-sdk/4.0"))
            .as("User-Agent should not be marked sensitive")
            .isFalse();

        assertThat(detector.isSensitive("x-ms-version", "2020-07-15"))
            .as("x-ms-version should not be marked sensitive")
            .isFalse();

        assertThat(detector.isSensitive("Content-Type", "application/json"))
            .as("Content-Type should not be marked sensitive")
            .isFalse();

        assertThat(detector.isSensitive("x-ms-activity-id", "some-guid"))
            .as("x-ms-activity-id should not be marked sensitive")
            .isFalse();

        assertThat(detector.isSensitive("x-ms-session-token", "0:1234"))
            .as("x-ms-session-token should not be marked sensitive")
            .isFalse();

        assertThat(detector.isSensitive("cache-control", "no-cache"))
            .as("cache-control should not be marked sensitive")
            .isFalse();
    }

    @Test(groups = "unit")
    public void singletonInstance() {
        assertThat(CosmosHttp2SensitivityDetector.INSTANCE)
            .as("INSTANCE should be a singleton")
            .isNotNull()
            .isSameAs(CosmosHttp2SensitivityDetector.INSTANCE);
    }
}
