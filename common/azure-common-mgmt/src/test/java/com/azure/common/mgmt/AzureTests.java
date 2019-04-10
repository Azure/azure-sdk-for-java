/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.mgmt;

import com.azure.common.mgmt.annotations.AzureHost;
import com.azure.common.annotations.GET;
import com.azure.common.annotations.HostParam;
import com.azure.common.annotations.PathParam;

public class AzureTests {

    @AzureHost("{vaultBaseUrl}")
    public interface HttpBinService {
        @GET("secrets/{secretName}")
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
//        Assert.assertEquals("http://vault1.vault.azure.net/secrets/{secretName}", service.getSecret("http://vault1.vault.azure.net", "secret1"));
//    }
}
