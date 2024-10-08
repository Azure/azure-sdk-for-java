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
import com.azure.resourcemanager.billing.models.AutoRenew;
import com.azure.resourcemanager.billing.models.Product;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class ProductsGetWithResponseMockTests {
    @Test
    public void testGetWithResponse() throws Exception {
        String responseStr
            = "{\"properties\":{\"autoRenew\":\"On\",\"availabilityId\":\"hwsrmegphwjy\",\"billingFrequency\":\"jdhlqtqjabw\",\"billingProfileId\":\"pjy\",\"billingProfileDisplayName\":\"qyirupsuyq\",\"customerId\":\"xnavxzpyaptex\",\"customerDisplayName\":\"lqhewhcchexc\",\"displayName\":\"my\",\"endDate\":\"wggmitdwolfmfaz\",\"invoiceSectionId\":\"cai\",\"invoiceSectionDisplayName\":\"pjttzfswohd\",\"lastCharge\":{\"currency\":\"ikkosqpliegemtn\",\"value\":28.761084},\"lastChargeDate\":\"uukydi\",\"productType\":\"ncr\",\"productTypeId\":\"tlrbzqtu\",\"skuId\":\"ajfay\",\"skuDescription\":\"ohdlpcix\",\"purchaseDate\":\"xnyhivhyujqxyfb\",\"quantity\":3145621458887697919,\"status\":\"Expiring\",\"tenantId\":\"abrdnovu\",\"reseller\":{\"resellerId\":\"wjohgcnrkmcivhww\",\"description\":\"ejh\"}},\"tags\":{\"cfqa\":\"fcfyzwkmrjfsq\",\"pasxwiic\":\"oveqowqodi\",\"x\":\"sbjhhadndow\"},\"id\":\"wsaxpbieehpvq\",\"name\":\"i\",\"type\":\"rrjprygjgyovc\"}";

        HttpClient httpClient
            = response -> Mono.just(new MockHttpResponse(response, 200, responseStr.getBytes(StandardCharsets.UTF_8)));
        BillingManager manager = BillingManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureEnvironment.AZURE));

        Product response = manager.products()
            .getWithResponse("argbmemopypc", "omowucjznnowp", com.azure.core.util.Context.NONE)
            .getValue();

        Assertions.assertEquals("fcfyzwkmrjfsq", response.tags().get("cfqa"));
        Assertions.assertEquals(AutoRenew.ON, response.properties().autoRenew());
    }
}
