// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.common.LeaseVersion;
import com.azure.cosmos.implementation.changefeed.fullfidelity.ServiceItemLeaseV1;
import com.azure.cosmos.implementation.changefeed.incremental.ServiceItemLease;
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

        Lease lease1 = new ServiceItemLease()
            .withId("id1")
            .withLeaseToken("1")
            .withETag("etag1")
            .withOwner("Owner1")
            .withContinuationToken("12")
            .withTimestamp(timeNow)
            .withTs("122311231");

        Lease lease2 = new ServiceItemLease()
            .withId("id2")
            .withLeaseToken("2")
            .withETag("etag2")
            .withContinuationToken("22")
            .withTimestamp(timeNow)
            .withTs("122311232");

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

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void serviceItemLeaseV1Serialization() throws JsonProcessingException {
        Instant timeNow = Instant.now();
        String timeNowValue = timeNow.toString();

        Lease lease1 = new ServiceItemLeaseV1()
            .withId("id1")
            .withLeaseToken("1")
            .withETag("etag1")
            .withOwner("Owner1")
            .withContinuationToken("12")
            .withTimestamp(timeNow)
            .withVersion(LeaseVersion.EPK_RANGE_BASED_LEASE)
            .withTs("122311231");

        Lease lease2 = new ServiceItemLeaseV1()
            .withId("id2")
            .withLeaseToken("2")
            .withETag("etag2")
            .withContinuationToken("22")
            .withTimestamp(timeNow)
            .withVersion(LeaseVersion.EPK_RANGE_BASED_LEASE)
            .withTs("122311232");

        ObjectMapper mapper = new ObjectMapper();

        assertThat(mapper.writeValueAsString(lease1)).isEqualTo(
            String.format("%s%s%s",
                "{\"id\":\"id1\",\"_etag\":\"etag1\",\"LeaseToken\":\"1\",\"ContinuationToken\":\"12\",\"timestamp\":\"",
                timeNowValue,
                "\",\"Owner\":\"Owner1\",\"Version\":1}"));
        assertThat(mapper.writeValueAsString(lease2)).isEqualTo(
            String.format("%s%s%s",
                "{\"id\":\"id2\",\"_etag\":\"etag2\",\"LeaseToken\":\"2\",\"ContinuationToken\":\"22\",\"timestamp\":\"",
                timeNowValue,
                "\",\"Owner\":null,\"Version\":1}"));
    }
}
