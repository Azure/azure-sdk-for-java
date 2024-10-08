// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.hdinsight.generated;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.resourcemanager.hdinsight.HDInsightManager;
import com.azure.resourcemanager.hdinsight.models.PrivateLinkResourceListResult;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class PrivateLinkResourcesListByClusterWithResponseMockTests {
    @Test
    public void testListByClusterWithResponse() throws Exception {
        String responseStr
            = "{\"value\":[{\"properties\":{\"groupId\":\"pejtl\",\"requiredMembers\":[\"aonwivkcqhrxh\",\"knlccrmmkyup\",\"jubyqjfkakfq\"],\"requiredZoneNames\":[\"em\",\"il\"]},\"id\":\"dxjascowvfdj\",\"name\":\"pdxphlkksnmgzvyf\",\"type\":\"jd\"},{\"properties\":{\"groupId\":\"qnwsithuqolyah\",\"requiredMembers\":[\"wqulsutrjbhxykf\",\"y\"],\"requiredZoneNames\":[\"vqqugdrftbcv\",\"xreuquowtlj\",\"fwhreagkhyxv\",\"qtvbczsu\"]},\"id\":\"dgglmepjpfs\",\"name\":\"ykgsangpszng\",\"type\":\"fpgylkve\"},{\"properties\":{\"groupId\":\"jcngoadyed\",\"requiredMembers\":[\"gjfoknubnoitpkpz\",\"rgdg\",\"vcoqraswugyxpqi\"],\"requiredZoneNames\":[\"ialwv\",\"kbuhzaca\",\"ty\"]},\"id\":\"co\",\"name\":\"cujp\",\"type\":\"sxzakuejkm\"}]}";

        HttpClient httpClient
            = response -> Mono.just(new MockHttpResponse(response, 200, responseStr.getBytes(StandardCharsets.UTF_8)));
        HDInsightManager manager = HDInsightManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureEnvironment.AZURE));

        PrivateLinkResourceListResult response = manager.privateLinkResources()
            .listByClusterWithResponse("lnuwiguy", "lykwphvxz", com.azure.core.util.Context.NONE)
            .getValue();

        Assertions.assertEquals("em", response.value().get(0).requiredZoneNames().get(0));
    }
}
