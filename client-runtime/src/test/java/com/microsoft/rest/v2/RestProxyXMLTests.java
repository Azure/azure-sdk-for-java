/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.rest.v2;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.reflect.TypeToken;
import com.microsoft.rest.v2.annotations.BodyParam;
import com.microsoft.rest.v2.annotations.GET;
import com.microsoft.rest.v2.annotations.Host;
import com.microsoft.rest.v2.annotations.PUT;
import com.microsoft.rest.v2.entities.AccessPolicy;
import com.microsoft.rest.v2.entities.SignedIdentifierInner;
import com.microsoft.rest.v2.entities.SignedIdentifiersWrapper;
import com.microsoft.rest.v2.entities.Slideshow;
import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.HttpHeaders;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.http.MockHttpResponse;
import com.microsoft.rest.v2.protocol.SerializerAdapter;
import com.microsoft.rest.v2.serializer.JacksonAdapter;
import org.joda.time.DateTime;
import org.junit.Test;
import rx.Single;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;


public class RestProxyXMLTests {
    static class MockXMLHTTPClient extends HttpClient {
        private HttpResponse response(String resource) throws IOException, URISyntaxException {
            URL url = getClass().getClassLoader().getResource(resource);
            byte[] bytes = Files.readAllBytes(Paths.get(url.toURI()));
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/xml");
            HttpResponse res = new MockHttpResponse(200, headers, bytes);
            return res;
        }

        @Override
        protected Single<HttpResponse> sendRequestInternalAsync(HttpRequest request) {
            try {
                if (request.url().endsWith("GetContainerACLs")) {
                    return Single.just(response("GetContainerACLs.xml"));
                } else if (request.url().endsWith("GetXMLWithAttributes")) {
                    return Single.just(response("GetXMLWithAttributes.xml"));
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
        SignedIdentifiersWrapper getContainerACLs();

        @PUT("SetContainerACLs")
        void setContainerACLs(@BodyParam("application/xml") SignedIdentifiersWrapper signedIdentifiers);
    }

    @Test
    public void canReadXMLResponse() throws Exception {
        MyXMLService myXMLService = RestProxy.create(MyXMLService.class, null, new MockXMLHTTPClient(), new JacksonAdapter());
        List<SignedIdentifierInner> identifiers = myXMLService.getContainerACLs().signedIdentifiers();
        assertNotNull(identifiers);
        assertNotEquals(0, identifiers.size());
    }

    static class MockXMLReceiverClient extends HttpClient {
        byte[] receivedBytes = null;

        @Override
        protected Single<HttpResponse> sendRequestInternalAsync(HttpRequest request) {
            try {
                if (request.url().endsWith("SetContainerACLs")) {
                    InputStream is = request.body().createInputStream();
                    receivedBytes = ByteStreams.toByteArray(is);
                    return Single.<HttpResponse>just(new MockHttpResponse(200));
                } else {
                    return Single.<HttpResponse>just(new MockHttpResponse(404));
                }
            } catch (IOException e) {
                return Single.error(e);
            }
        }
    }

    @Test
    public void canWriteXMLRequest() throws Exception {
        URL url = getClass().getClassLoader().getResource("GetContainerACLs.xml");
        byte[] bytes = Files.readAllBytes(Paths.get(url.toURI()));
        HttpRequest request = new HttpRequest("canWriteXMLRequest", "PUT", "http://unused/SetContainerACLs");
        request.withBody(bytes, "application/xml");

        SignedIdentifierInner si = new SignedIdentifierInner();
        si.withId("MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=");

        AccessPolicy ap = new AccessPolicy();
        ap.withStart(DateTime.parse("2009-09-28T08:49:37.0000000Z"));
        ap.withExpiry(DateTime.parse("2009-09-29T08:49:37.0000000Z"));
        ap.withPermission("rwd");

        si.withAccessPolicy(ap);
        List<SignedIdentifierInner> expectedAcls = Collections.singletonList(si);

        JacksonAdapter serializer = new JacksonAdapter();
        MockXMLReceiverClient httpClient = new MockXMLReceiverClient();
        MyXMLService myXMLService = RestProxy.create(MyXMLService.class, null, httpClient, serializer);
        SignedIdentifiersWrapper wrapper = new SignedIdentifiersWrapper(expectedAcls);
        myXMLService.setContainerACLs(wrapper);

        SignedIdentifiersWrapper actualAclsWrapped = serializer.deserialize(
                new String(httpClient.receivedBytes, Charsets.UTF_8),
                new TypeToken<SignedIdentifiersWrapper>() {}.getType(),
                SerializerAdapter.Encoding.XML);

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
    public interface MyXMLServiceWithAttributes {
        @GET("GetXMLWithAttributes")
        Slideshow getSlideshow();
    }

    @Test
    public void canDeserializeXMLWithAttributes() throws Exception {
        JacksonAdapter serializer = new JacksonAdapter();
        MyXMLServiceWithAttributes myXMLService = RestProxy.create(
                MyXMLServiceWithAttributes.class,
                null,
                new MockXMLHTTPClient(),
                serializer);

        Slideshow slideshow = myXMLService.getSlideshow();
        assertEquals("Sample Slide Show", slideshow.title);
        assertEquals("Date of publication", slideshow.date);
        assertEquals("Yours Truly", slideshow.author);
        assertEquals(2, slideshow.slides.length);

        assertEquals("all", slideshow.slides[0].type);
        assertEquals("Wake up to WonderWidgets!", slideshow.slides[0].title);
        assertNull(slideshow.slides[0].items);

        assertEquals("all", slideshow.slides[1].type);
        assertEquals("Overview", slideshow.slides[1].title);
        assertEquals(3, slideshow.slides[1].items.length);
        assertEquals("Why WonderWidgets are great", slideshow.slides[1].items[0]);
        assertEquals("", slideshow.slides[1].items[1]);
        assertEquals("Who buys WonderWidgets", slideshow.slides[1].items[2]);

        String xml = serializer.serialize(slideshow, SerializerAdapter.Encoding.XML);
        Slideshow newSlideshow = serializer.deserialize(xml, Slideshow.class, SerializerAdapter.Encoding.XML);
        String newXML = serializer.serialize(newSlideshow, SerializerAdapter.Encoding.XML);
        assertEquals(xml, newXML);
    }
}
