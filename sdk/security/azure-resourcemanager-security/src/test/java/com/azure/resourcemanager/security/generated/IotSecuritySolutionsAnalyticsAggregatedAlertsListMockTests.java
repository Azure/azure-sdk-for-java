// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.security.generated;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.resourcemanager.security.SecurityManager;
import com.azure.resourcemanager.security.models.IoTSecurityAggregatedAlert;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class IotSecuritySolutionsAnalyticsAggregatedAlertsListMockTests {
    @Test
    public void testList() throws Exception {
        String responseStr
            = "{\"value\":[{\"properties\":{\"alertType\":\"valblhtjqv\",\"alertDisplayName\":\"vweht\",\"vendorName\":\"xhzzyse\",\"reportedSeverity\":\"Medium\",\"remediationSteps\":\"ivzrrryveimipsk\",\"description\":\"zatvfuzka\",\"count\":5392653388256869187,\"effectedResourceType\":\"ru\",\"systemSource\":\"igsyeipqdsmjt\",\"actionTaken\":\"qgdgkkile\",\"logAnalyticsQuery\":\"kcsmk\",\"topDevicesList\":[{\"deviceId\":\"bbaedorvvm\",\"alertsCount\":4509163906635599934,\"lastOccurrence\":\"gbdg\"},{\"deviceId\":\"mgxdgdhpabgd\",\"alertsCount\":487078818297817911,\"lastOccurrence\":\"vjsaqwotm\"},{\"deviceId\":\"llcolsrsxapte\",\"alertsCount\":6440980028700974745,\"lastOccurrence\":\"gjokjljnhvlqjbek\"},{\"deviceId\":\"eksnbksdqhjvyk\",\"alertsCount\":3845965226340462865,\"lastOccurrence\":\"khh\"}]},\"tags\":{\"avnwqj\":\"cpoq\",\"knlejjjkxybwfd\":\"g\"},\"id\":\"kjbztensvkzykj\",\"name\":\"jknsxfwu\",\"type\":\"hcdpkupnqrmgj\"}]}";

        HttpClient httpClient
            = response -> Mono.just(new MockHttpResponse(response, 200, responseStr.getBytes(StandardCharsets.UTF_8)));
        SecurityManager manager = SecurityManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureEnvironment.AZURE));

        PagedIterable<IoTSecurityAggregatedAlert> response = manager.iotSecuritySolutionsAnalyticsAggregatedAlerts()
            .list("hhkuuip", "dqq", 1276247393, com.azure.core.util.Context.NONE);

        Assertions.assertEquals("cpoq", response.iterator().next().tags().get("avnwqj"));
    }
}
