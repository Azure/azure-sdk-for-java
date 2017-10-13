/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.rest;

import com.microsoft.rest.annotations.GET;
import com.microsoft.rest.annotations.Host;
import com.microsoft.rest.entities.SignedIdentifierInner;
import com.microsoft.rest.http.HttpClient;
import com.microsoft.rest.http.HttpHeaders;
import com.microsoft.rest.http.HttpRequest;
import com.microsoft.rest.http.HttpResponse;
import com.microsoft.rest.http.MockHttpResponse;
import com.microsoft.rest.serializer.JacksonAdapter;
import org.junit.Test;
import rx.Single;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class RestProxyXMLTests {
    class MockXMLHTTPClient extends HttpClient {
        @Override
        protected Single<HttpResponse> sendRequestInternalAsync(HttpRequest request) {
            try {
                if (request.url().endsWith("GetContainerACLs")) {
                    URL url = getClass().getClassLoader().getResource("GetContainerACLs.xml");
                    byte[] bytes = Files.readAllBytes(Paths.get(url.toURI()));
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Content-Type", "application/xml");
                    HttpResponse res = new MockHttpResponse(200, headers, bytes);
                    return Single.just(res);
                } else {
                    return Single.<HttpResponse>just(new MockHttpResponse(404));
                }
            } catch (IOException | URISyntaxException e) {
                return Single.error(e);
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
        MyXMLService service = RestProxy.create(MyXMLService.class, null, new MockXMLHTTPClient(), new JacksonAdapter());
        List<SignedIdentifierInner> identifiers = service.getContainerACLs();
        assertNotNull(identifiers);
        assertNotEquals(0, identifiers.size());
    }
}
