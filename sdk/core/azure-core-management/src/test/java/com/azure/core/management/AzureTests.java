// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import com.azure.core.annotation.Get;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
import com.azure.core.management.annotations.AzureHost;

public class AzureTests {

    @AzureHost("{vaultBaseUrl}")
    public interface HttpBinService {
        @Get("secrets/{secretName}")
        String getSecret(@HostParam("vaultBaseUrl") String vaultBaseUrl, @PathParam("secretName") String secretName);
    }

// @AzureHost not yet supported.
//    @Test
//    public void getBytes() throws Exception {
//        RestClient client = RestClient.newDefaultBuilder()
//                .withBaseUrl("http://localhost")
//                .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
//                .build();
//        HttpBinService service = RestProxy.create(HttpBinService.class, client);
//
//        Assertions.assertEquals("http://vault1.vault.azure.net/secrets/{secretName}", service.getSecret("http://vault1.vault.azure.net", "secret1"));
//    }
}
