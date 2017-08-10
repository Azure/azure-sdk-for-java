/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

import com.microsoft.rest.LogLevel;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceResponseBuilder;
import com.microsoft.rest.serializer.JacksonAdapter;
import com.microsoft.rest.v2.annotations.GET;
import com.microsoft.rest.v2.annotations.Host;
import com.microsoft.rest.v2.annotations.HostParam;
import com.microsoft.rest.v2.annotations.PathParam;
import org.junit.Assert;
import org.junit.Test;

public class HttpBinTests {

    @Host("http://httpbin.org")
    public interface HttpBinService {
        @GET("bytes/{bytes}")
        byte[] getBytes(@PathParam("bytes") int bytes);
    }

    @Host("http://{host}.org")
    public interface ParametrizedHttpBinService {
        @GET("bytes/{bytes}")
        byte[] getBytes(@HostParam("host") String host, @PathParam("bytes") int bytes);
    }

    @Test
    public void getBytes() throws Exception {
        RestClient client = new RestClient.Builder()
                .withBaseUrl("http://localhost")
                .withLogLevel(LogLevel.BODY_AND_HEADERS)
                .withSerializerAdapter(new JacksonAdapter())
                .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
                .build();
        HttpBinService service = RestProxy.create(HttpBinService.class, client);

        byte[] bytes = service.getBytes(8);
        Assert.assertEquals(8, bytes.length);

        ParametrizedHttpBinService parametrizedService = RestProxy.create(ParametrizedHttpBinService.class, client);

        bytes = parametrizedService.getBytes("httpbin", 8);
        Assert.assertEquals(8, bytes.length);
    }
}
