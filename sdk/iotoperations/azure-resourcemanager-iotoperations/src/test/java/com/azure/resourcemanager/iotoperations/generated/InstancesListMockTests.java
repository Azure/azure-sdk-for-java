// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.iotoperations.generated;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.resourcemanager.iotoperations.IoTOperationsManager;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocationType;
import com.azure.resourcemanager.iotoperations.models.InstanceResource;
import com.azure.resourcemanager.iotoperations.models.ManagedServiceIdentityType;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class InstancesListMockTests {
    @Test
    public void testList() throws Exception {
        String responseStr
            = "{\"value\":[{\"properties\":{\"description\":\"yebizikayuh\",\"provisioningState\":\"Succeeded\",\"version\":\"bs\",\"schemaRegistryRef\":{\"resourceId\":\"bbqwrvtldg\"}},\"extendedLocation\":{\"name\":\"fp\",\"type\":\"CustomLocation\"},\"identity\":{\"principalId\":\"ipaslthaqfxssmwu\",\"tenantId\":\"bdsrez\",\"type\":\"UserAssigned\",\"userAssignedIdentities\":{\"sibircgpi\":{\"principalId\":\"euyowqkd\",\"clientId\":\"t\"},\"i\":{\"principalId\":\"zimejzanlfzx\",\"clientId\":\"vrmbzono\"},\"frl\":{\"principalId\":\"jq\",\"clientId\":\"rgz\"}}},\"location\":\"szrnwo\",\"tags\":{\"cdhszf\":\"dfpwpjylwbtlhfls\",\"dvriiiojnal\":\"vfbgofeljagrqmqh\"},\"id\":\"hfkvtvsexsowuel\",\"name\":\"qhhahhxvrhmzkwpj\",\"type\":\"wws\"}]}";

        HttpClient httpClient
            = response -> Mono.just(new MockHttpResponse(response, 200, responseStr.getBytes(StandardCharsets.UTF_8)));
        IoTOperationsManager manager = IoTOperationsManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureEnvironment.AZURE));

        PagedIterable<InstanceResource> response = manager.instances().list(com.azure.core.util.Context.NONE);

        Assertions.assertEquals("szrnwo", response.iterator().next().location());
        Assertions.assertEquals("dfpwpjylwbtlhfls", response.iterator().next().tags().get("cdhszf"));
        Assertions.assertEquals("yebizikayuh", response.iterator().next().properties().description());
        Assertions.assertEquals("bbqwrvtldg", response.iterator().next().properties().schemaRegistryRef().resourceId());
        Assertions.assertEquals("fp", response.iterator().next().extendedLocation().name());
        Assertions.assertEquals(ExtendedLocationType.CUSTOM_LOCATION,
            response.iterator().next().extendedLocation().type());
        Assertions.assertEquals(ManagedServiceIdentityType.USER_ASSIGNED, response.iterator().next().identity().type());
    }
}
