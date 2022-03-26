// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.implementation.changefeed.Lease;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceItemLeaseTests {
    private static final int TIMEOUT = 2000;

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void serviceItemLeaseSerialization() throws JsonProcessingException {
        Instant timeNow = Instant.now();
        String timeNowValue = timeNow.toString();

        Lease lease1 = Lease.builder()
            .id("id1")
            .leaseToken("1")
            .etag("etag1")
            .owner("Owner1")
            .continuationToken("12")
            .timestamp(timeNowValue)
            .ts("122311231")
            .buildPartitionBasedLease();

        Lease lease2 = Lease.builder()
            .id("id2")
            .leaseToken("2")
            .etag("etag2")
            .continuationToken("22")
            .timestamp(timeNowValue)
            .ts("122311232")
            .buildPartitionBasedLease();

        ObjectMapper mapper = new ObjectMapper();

        assertThat(mapper.writeValueAsString(lease1)).isEqualTo(
            String.format("%s%s%s",
                "{\"id\":\"id1\",\"_etag\":\"etag1\",\"LeaseToken\":\"1\",\"ContinuationToken\":\"12\",\"timestamp\":\"",
                timeNowValue,
                "\",\"Owner\":\"Owner1\"}"));
        assertThat(mapper.writeValueAsString(lease2)).isEqualTo(
            String.format("%s%s%s",
                "{\"id\":\"id2\",\"_etag\":\"etag2\",\"LeaseToken\":\"2\",\"ContinuationToken\":\"22\",\"timestamp\":\"",
                timeNowValue,
                "\",\"Owner\":null}"));
    }
}
