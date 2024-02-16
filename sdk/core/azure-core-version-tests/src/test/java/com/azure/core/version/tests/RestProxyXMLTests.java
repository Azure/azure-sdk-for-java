// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.version.tests;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.version.tests.models.AccessPolicy;
import com.azure.core.version.tests.models.SignedIdentifierInner;
import com.azure.core.version.tests.models.SignedIdentifiersWrapper;
import com.azure.core.version.tests.models.Slideshow;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RestProxyXMLTests {
    private static final SerializerAdapter ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();

    private static final String GET_CONTAINERS_ACLS = String.join("\n", "<?xml version=\"1.0\" encoding=\"utf-8\"?>",
        "<!--", "  ~ Copyright (c) Microsoft Corporation. All rights reserved.", "  ~ Licensed under the MIT License.",
        "  -->", "<SignedIdentifiers>", "   <SignedIdentifier>",
        "       <Id>MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=</Id>", "       <AccessPolicy>",
        "           <Start>2009-09-28T08:49:37.0000000Z</Start>",
        "           <Expiry>2009-09-29T08:49:37.0000000Z</Expiry>", "           <Permission>rwd</Permission>",
        "       </AccessPolicy>", "   </SignedIdentifier>", "</SignedIdentifiers>");

    private static final String GET_XML_WITH_ATTRIBUTES = String.join("\n",
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>", "<!--",
        "  ~ Copyright (c) Microsoft Corporation. All rights reserved.", "  ~ Licensed under the MIT License.", "  -->",
        "<slideshow title=\"Sample Slide Show\" date=\"Date of publication\" author=\"Yours Truly\">",
        "   <slide type=\"all\">", "       <title>Wake up to WonderWidgets!</title>", "   </slide>",
        "   <slide type=\"all\">", "       <title>Overview</title>", "       <item>Why WonderWidgets are great</item>",
        "       <item/>", "       <item>Who buys WonderWidgets</item>", "   </slide>", "</slideshow>");

    static class MockXMLHTTPClient implements HttpClient {
        private HttpResponse response(HttpRequest request, String xml) {
            HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml");
            return new MockHttpResponse(request, 200, headers, xml.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (request.getUrl().toString().endsWith("GetContainerACLs")) {
                return Mono.just(response(request, GET_CONTAINERS_ACLS));
            } else if (request.getUrl().toString().endsWith("GetXMLWithAttributes")) {
                return Mono.just(response(request, GET_XML_WITH_ATTRIBUTES));
            } else {
                return Mono.<HttpResponse>just(new MockHttpResponse(request, 404));
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
    public void canReadXMLResponse() {
        //
        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new MockXMLHTTPClient()).build();

        //
        MyXMLService myXMLService = RestProxy.create(MyXMLService.class, pipeline, ADAPTER);
        List<SignedIdentifierInner> identifiers = myXMLService.getContainerACLs().signedIdentifiers();
        assertNotNull(identifiers);
        assertNotEquals(0, identifiers.size());
    }

    static class MockXMLReceiverClient implements HttpClient {
        byte[] receivedBytes = null;

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (request.getUrl().toString().endsWith("SetContainerACLs")) {
                return FluxUtil.collectBytesInByteBufferStream(request.getBody()).map(bytes -> {
                    receivedBytes = bytes;
                    return new MockHttpResponse(request, 200);
                });
            } else {
                return Mono.<HttpResponse>just(new MockHttpResponse(request, 404));
            }
        }
    }

    @Test
    public void canWriteXMLRequest() throws IOException {
        SignedIdentifierInner si = new SignedIdentifierInner();
        si.withId("MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=");

        AccessPolicy ap = new AccessPolicy();
        ap.withStart(OffsetDateTime.parse("2009-09-28T08:49:37.0000000Z"));
        ap.withExpiry(OffsetDateTime.parse("2009-09-29T08:49:37.0000000Z"));
        ap.withPermission("rwd");

        si.withAccessPolicy(ap);
        List<SignedIdentifierInner> expectedAcls = Collections.singletonList(si);

        MockXMLReceiverClient httpClient = new MockXMLReceiverClient();
        //
        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(httpClient).build();
        //
        MyXMLService myXMLService = RestProxy.create(MyXMLService.class, pipeline, ADAPTER);
        SignedIdentifiersWrapper wrapper = new SignedIdentifiersWrapper(expectedAcls);
        myXMLService.setContainerACLs(wrapper);

        SignedIdentifiersWrapper actualAclsWrapped
            = ADAPTER.deserialize(new String(httpClient.receivedBytes, StandardCharsets.UTF_8),
                SignedIdentifiersWrapper.class, SerializerEncoding.XML);

        List<SignedIdentifierInner> actualAcls = actualAclsWrapped.signedIdentifiers();

        // Ideally we'd just check for "things that matter" about the XML-- e.g. the tag names, structure, and
        // attributes needs to be the same,
        // but it doesn't matter if one document has a trailing newline or has UTF-8 in the header instead of utf-8, or
        // if comments are missing.
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
        //
        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new MockXMLHTTPClient()).build();

        //
        MyXMLServiceWithAttributes myXMLService = RestProxy.create(MyXMLServiceWithAttributes.class, pipeline, ADAPTER);

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

        String xml = ADAPTER.serialize(slideshow, SerializerEncoding.XML);
        Slideshow newSlideshow = ADAPTER.deserialize(xml, Slideshow.class, SerializerEncoding.XML);
        String newXML = ADAPTER.serialize(newSlideshow, SerializerEncoding.XML);
        assertEquals(xml, newXML);
    }
}
