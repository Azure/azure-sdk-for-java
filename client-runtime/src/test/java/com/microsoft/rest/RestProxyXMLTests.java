/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.rest;

import com.google.common.io.CharStreams;
import com.google.common.reflect.TypeToken;
import com.microsoft.rest.annotations.GET;
import com.microsoft.rest.annotations.Host;
import com.microsoft.rest.entities.SignedIdentifierInner;
import com.microsoft.rest.http.HttpClient;
import com.microsoft.rest.http.HttpRequest;
import com.microsoft.rest.http.HttpResponse;
import com.microsoft.rest.http.MockHttpResponse;
import com.microsoft.rest.serializer.JacksonXMLAdapter;
import org.junit.Test;
import rx.Single;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class RestProxyXMLTests {
    class MockXMLHTTPClient extends HttpClient {
        @Override
        protected Single<HttpResponse> sendRequestInternalAsync(HttpRequest request) {
            try {
                if (request.url().endsWith("GetContainerACLs")) {
                    InputStream stream = getClass().getClassLoader().getResourceAsStream("GetContainerACLs.xml");
                    String xml = CharStreams.toString(new InputStreamReader(stream));
                    HttpResponse res = new MockHttpResponse(200, xml);
                    res.headers().set("Content-Type", "application/xml");
                    return Single.<HttpResponse>just(new MockHttpResponse(200, xml));
                } else {
                    return Single.<HttpResponse>just(new MockHttpResponse(404));
                }
            } catch (IOException e) {
                return Single.<HttpResponse>just(new MockHttpResponse(500));
            }
        }
    }

    @Host("http://unused")
    interface MyXMLService {
        @GET("GetContainerACLs")
        List<SignedIdentifierInner> getContainerACLs();
    }

    @Test
    public void canDeserializeXML() throws Exception {
        JacksonXMLAdapter jacksonXMLAdapter = new JacksonXMLAdapter();

        MyXMLService service = RestProxy.create(MyXMLService.class, null, new MockXMLHTTPClient(), jacksonXMLAdapter);
        List<SignedIdentifierInner> identifiers = service.getContainerACLs();
        assertNotNull(identifiers);
        assertNotEquals(0, identifiers.size());
    }
}
