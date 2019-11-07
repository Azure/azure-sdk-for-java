// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.implementation.entities.AccessPolicy;
import com.azure.core.implementation.entities.SignedIdentifierInner;
import com.azure.core.implementation.entities.SignedIdentifiersWrapper;
import com.azure.core.implementation.entities.Slideshow;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.FluxUtil;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RestProxyXMLTests {
    static class MockXMLHTTPClient implements HttpClient {
        private HttpResponse response(HttpRequest request, String resource) throws IOException, URISyntaxException {
            URL url = getClass().getClassLoader().getResource(resource);
            byte[] bytes = Files.readAllBytes(Paths.get(url.toURI()));
            HttpHeaders headers = new HttpHeaders().put("Content-Type", "application/xml");
            HttpResponse res = new MockHttpResponse(request, 200, headers, bytes);
            return res;
        }
        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            try {
                if (request.getUrl().toString().endsWith("GetContainerACLs")) {
                    return Mono.just(response(request, "GetContainerACLs.xml"));
                } else if (request.getUrl().toString().endsWith("GetXMLWithAttributes")) {
                    return Mono.just(response(request, "GetXMLWithAttributes.xml"));
                } else {
                    return Mono.<HttpResponse>just(new MockHttpResponse(request, 404));
                }
            } catch (IOException | URISyntaxException e) {
                return Mono.error(e);
            }
        }
    }

    @Host("http://unused")
    @ServiceInterface(name = "MyXMLService")
    interface MyXMLService {
        @Get("GetContainerACLs")
        SignedIdentifiersWrapper getContainerACLs();

        @Put("SetContainerACLs")
        void setContainerACLs(@BodyParam("application/xml") SignedIdentifiersWrapper signedIdentifiers);
    }

    @Test
    public void canReadXMLResponse() throws Exception {
        //
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new MockXMLHTTPClient())
            .build();

        //
        MyXMLService myXMLService = RestProxy.create(MyXMLService.class,
                pipeline,
                new JacksonAdapter());
        List<SignedIdentifierInner> identifiers = myXMLService.getContainerACLs().signedIdentifiers();
        assertNotNull(identifiers);
        assertNotEquals(0, identifiers.size());
    }

    static class MockXMLReceiverClient implements HttpClient {
        byte[] receivedBytes = null;

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (request.getUrl().toString().endsWith("SetContainerACLs")) {
                return FluxUtil.collectBytesInByteBufferStream(request.getBody())
                        .map(bytes -> {
                            receivedBytes = bytes;
                            return new MockHttpResponse(request, 200);
                        });
            } else {
                return Mono.<HttpResponse>just(new MockHttpResponse(request, 404));
            }
        }
    }

    @Test
    public void canWriteXMLRequest() throws Exception {
        URL url = getClass().getClassLoader().getResource("GetContainerACLs.xml");
        byte[] bytes = Files.readAllBytes(Paths.get(url.toURI()));
        HttpRequest request = new HttpRequest(HttpMethod.PUT, new URL("http://unused/SetContainerACLs"));
        request.setBody(bytes);

        SignedIdentifierInner si = new SignedIdentifierInner();
        si.withId("MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=");

        AccessPolicy ap = new AccessPolicy();
        ap.withStart(OffsetDateTime.parse("2009-09-28T08:49:37.0000000Z"));
        ap.withExpiry(OffsetDateTime.parse("2009-09-29T08:49:37.0000000Z"));
        ap.withPermission("rwd");

        si.withAccessPolicy(ap);
        List<SignedIdentifierInner> expectedAcls = Collections.singletonList(si);

        JacksonAdapter serializer = new JacksonAdapter();
        MockXMLReceiverClient httpClient = new MockXMLReceiverClient();
        //
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .build();
        //
        MyXMLService myXMLService = RestProxy.create(MyXMLService.class,
                pipeline,
                serializer);
        SignedIdentifiersWrapper wrapper = new SignedIdentifiersWrapper(expectedAcls);
        myXMLService.setContainerACLs(wrapper);

        SignedIdentifiersWrapper actualAclsWrapped = serializer.deserialize(
                new String(httpClient.receivedBytes, StandardCharsets.UTF_8),
                SignedIdentifiersWrapper.class,
                SerializerEncoding.XML);

        List<SignedIdentifierInner> actualAcls = actualAclsWrapped.signedIdentifiers();

        // Ideally we'd just check for "things that matter" about the XML-- e.g. the tag names, structure, and attributes needs to be the same,
        // but it doesn't matter if one document has a trailing newline or has UTF-8 in the header instead of utf-8, or if comments are missing.
        assertEquals(expectedAcls.size(), actualAcls.size());
        assertEquals(expectedAcls.get(0).id(), actualAcls.get(0).id());
        assertEquals(expectedAcls.get(0).accessPolicy().expiry(), actualAcls.get(0).accessPolicy().expiry());
        assertEquals(expectedAcls.get(0).accessPolicy().start(), actualAcls.get(0).accessPolicy().start());
        assertEquals(expectedAcls.get(0).accessPolicy().permission(), actualAcls.get(0).accessPolicy().permission());
    }

    @Host("http://unused")
    @ServiceInterface(name = "MyXMLServiceWithAttributes")
    public interface MyXMLServiceWithAttributes {
        @Get("GetXMLWithAttributes")
        Slideshow getSlideshow();
    }

    @Test
    public void canDeserializeXMLWithAttributes() throws Exception {
        JacksonAdapter serializer = new JacksonAdapter();
        //
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new MockXMLHTTPClient())
            .build();

        //
        MyXMLServiceWithAttributes myXMLService = RestProxy.create(
                MyXMLServiceWithAttributes.class,
                pipeline,
                serializer);

        Slideshow slideshow = myXMLService.getSlideshow();
        assertEquals("Sample Slide Show", slideshow.title());
        assertEquals("Date of publication", slideshow.date());
        assertEquals("Yours Truly", slideshow.author());
        assertEquals(2, slideshow.slides().length);

        assertEquals("all", slideshow.slides()[0].type());
        assertEquals("Wake up to WonderWidgets!", slideshow.slides()[0].title());
        assertEquals(0, slideshow.slides()[0].items().length);

        assertEquals("all", slideshow.slides()[1].type());
        assertEquals("Overview", slideshow.slides()[1].title());
        assertEquals(3, slideshow.slides()[1].items().length);
        assertEquals("Why WonderWidgets are great", slideshow.slides()[1].items()[0]);
        assertEquals("", slideshow.slides()[1].items()[1]);
        assertEquals("Who buys WonderWidgets", slideshow.slides()[1].items()[2]);

        String xml = serializer.serialize(slideshow, SerializerEncoding.XML);
        Slideshow newSlideshow = serializer.deserialize(xml, Slideshow.class, SerializerEncoding.XML);
        String newXML = serializer.serialize(newSlideshow, SerializerEncoding.XML);
        assertEquals(xml, newXML);
    }
}
