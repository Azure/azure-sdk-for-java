// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.appcontainers.generated;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.resourcemanager.appcontainers.ContainerAppsApiManager;
import com.azure.resourcemanager.appcontainers.models.JobExecution;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class ResourceProvidersJobExecutionWithResponseMockTests {
    @Test
    public void testJobExecutionWithResponse() throws Exception {
        String responseStr
            = "{\"name\":\"fmvlihcvjdrqc\",\"id\":\"idhftu\",\"type\":\"hdxlw\",\"properties\":{\"status\":\"Running\",\"startTime\":\"2021-02-13T08:09:39Z\",\"endTime\":\"2021-03-15T14:07:07Z\",\"template\":{\"containers\":[{\"image\":\"yixhafratqxmb\",\"name\":\"oum\",\"command\":[\"valqjrhuzgfxo\"],\"args\":[\"pusllywpv\",\"iotzbpdbollgryfq\",\"uasigr\"],\"env\":[{},{},{},{}],\"resources\":{}},{\"image\":\"nequy\",\"name\":\"jboq\",\"command\":[\"tqjkqevad\",\"mmwiuawvcmjz\",\"xiid\",\"scz\"],\"args\":[\"s\",\"oqiqazugamx\",\"krrcoiisbamnpp\",\"cekuz\"],\"env\":[{},{},{},{}],\"resources\":{}}],\"initContainers\":[{\"image\":\"xyfukzxuizhyhn\",\"name\":\"kpetiarxq\",\"command\":[\"xdukecpxd\",\"zvdhctmmkosz\",\"dblnsntrp\",\"aqkiofkb\"],\"args\":[\"hklbnldpvcbh\",\"ezyquw\"],\"env\":[{},{},{},{}],\"resources\":{}}]}}}";

        HttpClient httpClient
            = response -> Mono.just(new MockHttpResponse(response, 200, responseStr.getBytes(StandardCharsets.UTF_8)));
        ContainerAppsApiManager manager = ContainerAppsApiManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureEnvironment.AZURE));

        JobExecution response = manager.resourceProviders()
            .jobExecutionWithResponse("swzsnuyemlowuo", "hlxlnwyrmou", "blgmokzkltrfowtd",
                com.azure.core.util.Context.NONE)
            .getValue();

        Assertions.assertEquals("fmvlihcvjdrqc", response.name());
        Assertions.assertEquals("idhftu", response.id());
        Assertions.assertEquals("hdxlw", response.type());
        Assertions.assertEquals(OffsetDateTime.parse("2021-02-13T08:09:39Z"), response.startTime());
        Assertions.assertEquals(OffsetDateTime.parse("2021-03-15T14:07:07Z"), response.endTime());
        Assertions.assertEquals("yixhafratqxmb", response.template().containers().get(0).image());
        Assertions.assertEquals("oum", response.template().containers().get(0).name());
        Assertions.assertEquals("valqjrhuzgfxo", response.template().containers().get(0).command().get(0));
        Assertions.assertEquals("pusllywpv", response.template().containers().get(0).args().get(0));
        Assertions.assertEquals("xyfukzxuizhyhn", response.template().initContainers().get(0).image());
        Assertions.assertEquals("kpetiarxq", response.template().initContainers().get(0).name());
        Assertions.assertEquals("xdukecpxd", response.template().initContainers().get(0).command().get(0));
        Assertions.assertEquals("hklbnldpvcbh", response.template().initContainers().get(0).args().get(0));
    }
}
