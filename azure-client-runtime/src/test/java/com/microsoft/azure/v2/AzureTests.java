/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2;

import com.microsoft.azure.v2.annotations.AzureHost;
import com.microsoft.rest.v2.annotations.GET;
import com.microsoft.rest.v2.annotations.HostParam;
import com.microsoft.rest.v2.annotations.PathParam;

public class AzureTests {

    @AzureHost("{vaultBaseUrl}")
    public interface HttpBinService {
        @GET("secrets/{secretName}")
        String getSecret(@HostParam("vaultBaseUrl") String vaultBaseUrl, @PathParam("secretName") String secretName);
    }

// @AzureHost not yet supported.
//    @Test
//    public void getBytes() throws Exception {
//        RestClient client = new RestClient.Builder()
//                .withBaseUrl("http://localhost")
//                .withSerializerAdapter(new JacksonAdapter())
//                .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
//                .build();
//        HttpBinService service = RestProxy.create(HttpBinService.class, client);
//
//        Assert.assertEquals("http://vault1.vault.azure.net/secrets/{secretName}", service.getSecret("http://vault1.vault.azure.net", "secret1"));
//    }
}
