// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changeFeed;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseBuilder;
import com.azure.cosmos.implementation.changefeed.implementation.leaseManagement.ServiceItemLease;
import com.azure.cosmos.implementation.changefeed.implementation.leaseManagement.ServiceItemLeaseCore;
import com.azure.cosmos.implementation.changefeed.implementation.leaseManagement.ServiceItemLeaseEpk;
import com.azure.cosmos.implementation.changefeed.implementation.leaseManagement.ServiceItemLeaseVersion;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import com.azure.cosmos.implementation.routing.Range;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.testng.AssertJUnit.fail;

public class ServiceItemLeaseTests {

    @DataProvider(name = "leaseTypeArgProvider")
    public Object[][] leaseTypeArgProvider() {
        return new Object[][]{
                { ServiceItemLeaseVersion.EPKRangeBasedLease },
                { ServiceItemLeaseVersion.PartitionKeyRangeBasedLease },
        };
    }

    @Test(groups = "unit")
    public void leaseContinuationToken() {
        Lease epkBasedLease =
                Lease.builder()
                        .id("TestId-" + UUID.randomUUID())
                        .leaseToken("AA-BB")
                        .feedRange(new FeedRangeEpkImpl(new Range<>("AA", "BB", true, false)))
                        .buildEpkBasedLease();

        assertThat(epkBasedLease.getContinuationState("testResourceId").getFeedRange())
                .isInstanceOf(FeedRangeEpkImpl.class);

        Lease partitionBasedLease =
                Lease.builder()
                        .id("TestId-" + UUID.randomUUID())
                        .leaseToken("0")
                        .feedRange(FeedRangeEpkImpl.forFullRange())
                        .buildPartitionBasedLease();
        assertThat(partitionBasedLease.getContinuationState("testResourceId").getFeedRange())
                .isInstanceOf(FeedRangePartitionKeyRangeImpl.class);

    }

    @Test(groups = "unit", dataProvider = "leaseTypeArgProvider")
    public void serviceItemLeaseSerialization(ServiceItemLeaseVersion leaseVersion) throws JsonProcessingException {
        String leaseId = "TestLease-" + UUID.randomUUID();
        String timestamp = Instant.now().toString();
        String etag = "testEtag";
        String continuationToken = "testContinuationToken";
        String owner = "testOwner";
        String epkRangeMin = "AA";
        String epkRangeMax = "BB";
        String leaseToken = null;
        int version = 0;

        LeaseBuilder leaseBuilder = Lease.builder()
                .id(leaseId)
                .feedRange(new FeedRangeEpkImpl(new Range<>(epkRangeMin, epkRangeMax, true, false)))
                .etag(etag)
                .owner(owner)
                .timestamp(timestamp)
                .continuationToken(continuationToken)
                .properties(
                        new HashMap<String, String>(){{ put("testKey", "testValue"); }});

        Lease lease = null;
        if (leaseVersion == ServiceItemLeaseVersion.PartitionKeyRangeBasedLease) {
            leaseToken = "1";
            lease = leaseBuilder.leaseToken(leaseToken).buildPartitionBasedLease();
        } else if (leaseVersion == ServiceItemLeaseVersion.EPKRangeBasedLease) {
            leaseToken = epkRangeMin + "-" + epkRangeMax;
            version = 1;
            lease = leaseBuilder.leaseToken(leaseToken).buildEpkBasedLease();
        }
        String leaseJsonString = Utils.getSimpleObjectMapper().writeValueAsString(lease);
        String expectedJsonString = String.format(
                "{" +
                "\"id\":\"%s\"," +
                "\"_etag\":\"%s\"," +
                "\"LeaseToken\":\"%s\"," +
                "\"ContinuationToken\":\"%s\"," +
                "\"timestamp\":\"%s\"," +
                "\"Owner\":\"%s\"," +
                "\"version\":%s," +
                "\"feedRange\":{" +
                "\"Range\":{\"min\":\"%s\",\"max\":\"%s\",\"isMinInclusive\":true,\"isMaxInclusive\":false}" +
                "}" +
                "}",
                leaseId, etag, leaseToken, continuationToken, timestamp, owner, version, epkRangeMin, epkRangeMax);

        assertThat(expectedJsonString.equalsIgnoreCase(leaseJsonString)).isTrue();
    }

    @Test(groups = "unit", dataProvider = "leaseTypeArgProvider")
    public void serviceItemLeaseDeserialization(ServiceItemLeaseVersion leaseVersion) throws JsonProcessingException {
        String leaseId = "TestLease-" + UUID.randomUUID();
        String timestamp = Instant.now().toString();
        String etag = "testEtag";
        String continuationToken = "testContinuationToken";
        String owner = "testOwner";
        String epkRangeMin = "AA";
        String epkRangeMax = "BB";
        long ts = 122311231;

        String leaseToken = null;
        int version = 0;
        if (leaseVersion == ServiceItemLeaseVersion.PartitionKeyRangeBasedLease) {
            leaseToken = "1";
        } else if (leaseVersion == ServiceItemLeaseVersion.EPKRangeBasedLease) {
            leaseToken = epkRangeMin + "-" + epkRangeMax;
            version = 1;
        } else {
            fail("Unsupported lease type : " + leaseVersion);
        }

        String leaseJsonString = String.format(
                "{" +
                "\"id\":\"%s\"," +
                "\"_etag\":\"%s\"," +
                "\"LeaseToken\":\"%s\"," +
                "\"ContinuationToken\":\"%s\"," +
                "\"timestamp\":\"%s\"," +
                "\"Owner\":\"%s\"," +
                "\"version\":%s," +
                "\"feedRange\":{" +
                "\"Range\":{\"min\":\"%s\",\"max\":\"%s\",\"isMinInclusive\":true,\"isMaxInclusive\":false}" +
                "}," +
                "\"_ts\":%s" +
                "}",
                leaseId, etag, leaseToken, continuationToken, timestamp, owner, version, epkRangeMin, epkRangeMax, ts);

        Lease lease = Utils.getSimpleObjectMapper().readValue(leaseJsonString, ServiceItemLease.class);

        if (leaseVersion == ServiceItemLeaseVersion.PartitionKeyRangeBasedLease) {
            assertThat(lease).isInstanceOf(ServiceItemLeaseCore.class);
        } else if (leaseVersion == ServiceItemLeaseVersion.EPKRangeBasedLease) {
            assertThat(lease).isInstanceOf(ServiceItemLeaseEpk.class);
        }

        assertThat(lease.getId()).isEqualTo(leaseId);
        assertThat(lease.getLeaseToken()).isEqualTo(leaseToken);
        assertThat(lease.getConcurrencyToken()).isEqualTo(etag);
        assertThat(lease.getContinuationToken()).isEqualTo(continuationToken);
        assertThat(lease.getTimestamp()).isEqualTo(timestamp);
        assertThat(lease.getOwner()).isEqualTo(owner);

        assertThat(lease.getFeedRange()).isInstanceOf(FeedRangeEpkImpl.class);
        FeedRangeEpkImpl feedRangeEpk = (FeedRangeEpkImpl) lease.getFeedRange();
        assertThat(feedRangeEpk.getRange().getMin()).isEqualTo(epkRangeMin);
        assertThat(feedRangeEpk.getRange().getMax()).isEqualTo(epkRangeMax);
    }
}
