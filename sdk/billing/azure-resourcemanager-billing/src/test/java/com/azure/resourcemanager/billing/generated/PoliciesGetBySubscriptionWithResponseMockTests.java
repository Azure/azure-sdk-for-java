// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.billing.generated;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.resourcemanager.billing.BillingManager;
import com.azure.resourcemanager.billing.models.PolicyType;
import com.azure.resourcemanager.billing.models.SubscriptionPolicy;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class PoliciesGetBySubscriptionWithResponseMockTests {
    @Test
    public void testGetBySubscriptionWithResponse() throws Exception {
        String responseStr
            = "{\"properties\":{\"provisioningState\":\"Pending\",\"policies\":[{\"name\":\"zdb\",\"value\":\"kdsbekvprkwpvxie\",\"policyType\":\"UserControlled\",\"scope\":\"gzshfa\"},{\"name\":\"e\",\"value\":\"aeiuex\",\"policyType\":\"SystemControlled\",\"scope\":\"mwdw\"},{\"name\":\"aeplpfre\",\"value\":\"izk\",\"policyType\":\"Other\",\"scope\":\"dl\"}]},\"tags\":{\"jsoxuuwuungdvv\":\"aobsgpdbhbdx\",\"inlgttvon\":\"drcpqu\",\"mitmtkcqixgqxs\":\"rpeli\"},\"id\":\"ev\",\"name\":\"huvupdsafqaghw\",\"type\":\"umecqyia\"}";

        HttpClient httpClient
            = response -> Mono.just(new MockHttpResponse(response, 200, responseStr.getBytes(StandardCharsets.UTF_8)));
        BillingManager manager = BillingManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureEnvironment.AZURE));

        SubscriptionPolicy response
            = manager.policies().getBySubscriptionWithResponse(com.azure.core.util.Context.NONE).getValue();

        Assertions.assertEquals("aobsgpdbhbdx", response.tags().get("jsoxuuwuungdvv"));
        Assertions.assertEquals("zdb", response.properties().policies().get(0).name());
        Assertions.assertEquals("kdsbekvprkwpvxie", response.properties().policies().get(0).value());
        Assertions.assertEquals(PolicyType.USER_CONTROLLED, response.properties().policies().get(0).policyType());
        Assertions.assertEquals("gzshfa", response.properties().policies().get(0).scope());
    }
}
