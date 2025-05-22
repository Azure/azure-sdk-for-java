// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.ListBlobContainersOptions;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.options.FindBlobsOptions;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Isolated
public class TimeoutTests {
    /*
     * This test class is marked as isolated to ensure that resource related issues do not interfere with the timeouts being tested.
     *
     * The custom http clients return a generic xml list of 5 blobs total.
     * The api call should return 2 pages, one page of 3 blobs and one page of 2 blobs.
     * Although each page is set to take 4 seconds to return, the timeout being set to 6 seconds should not cause the test to fail,
     * as the timeout should only be on the page request and not the entire stream of pages.
     */

    @Test
    public void listBlobsFlatWithTimeoutStillBackedByPagedStream() {
        BlobContainerClient containerClient
            = new BlobContainerClientBuilder().endpoint("https://account.blob.core.windows.net/")
                .credential(new MockTokenCredential())
                .containerName("foo")
                .httpClient(new ListBlobsWithTimeoutTestClient())
                .buildClient();

        assertEquals(2,
            containerClient.listBlobs(new ListBlobsOptions().setMaxResultsPerPage(3), Duration.ofSeconds(6))
                .streamByPage()
                .count());
    }

    @Test
    public void listBlobsHierWithTimeoutStillBackedByPagedStream() {
        BlobContainerClient containerClient
            = new BlobContainerClientBuilder().endpoint("https://account.blob.core.windows.net/")
                .credential(new MockTokenCredential())
                .containerName("foo")
                .httpClient(new ListBlobsWithTimeoutTestClient())
                .buildClient();

        assertEquals(2,
            containerClient
                .listBlobsByHierarchy("/", new ListBlobsOptions().setMaxResultsPerPage(3), Duration.ofSeconds(6))
                .streamByPage()
                .count());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-04-10")
    @Test
    public void findBlobsInContainerWithTimeoutStillBackedByPagedStream() {
        BlobContainerClient containerClient
            = new BlobContainerClientBuilder().endpoint("https://account.blob.core.windows.net/")
                .credential(new MockTokenCredential())
                .containerName("foo")
                .httpClient(new FindBlobsWithTimeoutClient())
                .buildClient();

        assertEquals(2,
            containerClient.findBlobsByTags(
                new FindBlobsOptions(String.format("\"%s\"='%s'", "dummyKey", "dummyValue")).setMaxResultsPerPage(3),
                Duration.ofSeconds(6), Context.NONE).streamByPage().count());
    }

    @Test
    public void listContainersWithTimeoutStillBackedByPagedStream() {
        BlobServiceClient serviceClient
            = new BlobServiceClientBuilder().endpoint("https://account.blob.core.windows.net/")
                .credential(new MockTokenCredential())
                .httpClient(new ListContainersWithTimeoutTestClient())
                .buildClient();

        assertEquals(2,
            serviceClient
                .listBlobContainers(new ListBlobContainersOptions().setMaxResultsPerPage(3), Duration.ofSeconds(6))
                .streamByPage()
                .count());

    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void findBlobsWithTimeoutStillBackedByPagedStream() {
        BlobServiceClient serviceClient
            = new BlobServiceClientBuilder().endpoint("https://account.blob.core.windows.net/")
                .credential(new MockTokenCredential())
                .httpClient(new FindBlobsWithTimeoutClient())
                .buildClient();

        assertEquals(2,
            serviceClient.findBlobsByTags(
                new FindBlobsOptions(String.format("\"%s\"='%s'", "dummyKey", "dummyValue")).setMaxResultsPerPage(3),
                Duration.ofSeconds(6), Context.NONE).streamByPage().count());
    }

    /*
     * Used for sync and async tests
     */

    private static HttpResponse responseHelper(HttpRequest request, String xml) {
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml");
        return new MockHttpResponse(request, 200, headers, xml.getBytes(StandardCharsets.UTF_8));
    }

    protected static final class ListBlobsWithTimeoutTestClient implements HttpClient {
        private String buildFirstResponse(Boolean useDelimiter) {
            String delimiterString = useDelimiter ? "<Delimiter>/</Delimiter>" : "";

            return "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<EnumerationResults ServiceEndpoint=\"https://account.blob.core.windows.net/\" ContainerName=\"foo\">"
                + "<MaxResults>3</MaxResults>" + delimiterString + "<Blobs>" + "<Blob>" + "<Name>blob1</Name>"
                + "</Blob>" + "<Blob>" + "<Name>blob2</Name>" + "</Blob>" + "<Blob>" + "<Name>blob3</Name>" + "</Blob>"
                + "</Blobs>" + "<NextMarker>MARKER--</NextMarker>" + "</EnumerationResults>";
        }

        private String buildSecondResponse(Boolean useDelimiter) {
            String delimiterString = useDelimiter ? "<Delimiter>/</Delimiter>" : "";

            return "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<EnumerationResults ServiceEndpoint=\"https://account.blob.core.windows.net/\" ContainerName=\"foo\">"
                + "<Marker>MARKER--</Marker>" + "<MaxResults>3</MaxResults>" + delimiterString + "<Blobs>" + "<Blob>"
                + "<Name>blob4</Name>" + "</Blob>" + "<Blob>" + "<Name>blob5</Name>" + "</Blob>" + "</Blobs>"
                + "<NextMarker/>" + "</EnumerationResults>";
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            String url = request.getUrl().toString();
            HttpResponse response;
            int delay = 4;

            if (url.contains("?restype=container&comp=list&maxresults=")) {
                // flat first request
                response = responseHelper(request, buildFirstResponse(false));
            } else if (url.contains("?restype=container&comp=list&marker=")) {
                // flat second request
                response = responseHelper(request, buildSecondResponse(false));
            } else if (url.contains("?restype=container&comp=list&delimiter=/&maxresults=")) {
                // hierarchy first request
                response = responseHelper(request, buildFirstResponse(true));
            } else if (url.contains("?restype=container&comp=list&delimiter=/&marker=")) {
                // hierarchy second request
                response = responseHelper(request, buildSecondResponse(true));
            } else {
                // fallback
                return Mono.just(new MockHttpResponse(request, 404));
            }

            return Mono.delay(Duration.ofSeconds(delay)).then(Mono.just(response));
        }
    }

    protected static final class FindBlobsWithTimeoutClient implements HttpClient {
        private String buildFirstResponse() {
            return "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<EnumerationResults ServiceEndpoint=\"https://account.blob.core.windows.net/\">"
                + "<Where>&quot;dummyKey&quot;=&apos;dummyValue&apos;</Where>" + "<MaxResults>3</MaxResults>"
                + "<Blobs>" + "<Blob>" + "<Name>blob1</Name>" + "<ContainerName>foo</ContainerName>" + "<Tags>"
                + "<TagSet>" + "<Tag>" + "<Key>dummyKey</Key>" + "<Value>dummyValue</Value>" + "</Tag>" + "</TagSet>"
                + "</Tags>" + "</Blob>" + "<Blob>" + "<Name>blob2</Name>" + "<ContainerName>foo</ContainerName>"
                + "<Tags>" + "<TagSet>" + "<Tag>" + "<Key>dummyKey</Key>" + "<Value>dummyValue</Value>" + "</Tag>"
                + "</TagSet>" + "</Tags>" + "</Blob>" + "<Blob>" + "<Name>blob3</Name>"
                + "<ContainerName>foo</ContainerName>" + "<Tags>" + "<TagSet>" + "<Tag>" + "<Key>dummyKey</Key>"
                + "<Value>dummyValue</Value>" + "</Tag>" + "</TagSet>" + "</Tags>" + "</Blob>" + "</Blobs>"
                + "<NextMarker>MARKER-</NextMarker>" + "</EnumerationResults>";
        }

        private String buildSecondResponse() {
            return "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<EnumerationResults ServiceEndpoint=\"https://account.blob.core.windows.net/\">"
                + "<Marker>MARKER-</Marker>" + "<Where>&quot;dummyKey&quot;=&apos;dummyValue&apos;</Where>"
                + "<MaxResults>3</MaxResults>" + "<Blobs>" + "<Blob>" + "<Name>blob4</Name>"
                + "<ContainerName>foo</ContainerName>" + "<Tags>" + "<TagSet>" + "<Tag>" + "<Key>dummyKey</Key>"
                + "<Value>dummyValue</Value>" + "</Tag>" + "</TagSet>" + "</Tags>" + "</Blob>" + "<Blob>"
                + "<Name>blob5</Name>" + "<ContainerName>foo</ContainerName>" + "<Tags>" + "<TagSet>" + "<Tag>"
                + "<Key>dummyKey</Key>" + "<Value>dummyValue</Value>" + "</Tag>" + "</TagSet>" + "</Tags>" + "</Blob>"
                + "</Blobs>" + "<NextMarker/>" + "</EnumerationResults>";
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            String url = request.getUrl().toString();
            HttpResponse response;
            int delay = 4;

            if (url.contains("marker")) {
                // second request
                response = responseHelper(request, buildSecondResponse());
            } else if (url.contains("?comp=blobs&where=%") || url.contains("?restype=container&comp=blobs&where=%")) {
                // first request
                response = responseHelper(request, buildFirstResponse());
            } else {
                // fallback
                return Mono.just(new MockHttpResponse(request, 404));
            }

            return Mono.delay(Duration.ofSeconds(delay)).then(Mono.just(response));
        }
    }

    protected static final class ListContainersWithTimeoutTestClient implements HttpClient {
        private String buildFirstResponse() {
            return "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<EnumerationResults ServiceEndpoint=\"https://account.blob.core.windows.net/\">"
                + "<MaxResults>3</MaxResults>" + "<Containers>" + "<Container>" + "<Name>container1</Name>"
                + "</Container>" + "<Container>" + "<Name>container2</Name>" + "</Container>" + "<Container>"
                + "<Name>container3</Name>" + "</Container>" + "</Containers>"
                + "<NextMarker>/marker/marker</NextMarker>" + "</EnumerationResults>";
        }

        private String buildSecondResponse() {
            return "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<EnumerationResults ServiceEndpoint=\"https://account.blob.core.windows.net/\">"
                + "<Marker>/marker/marker</Marker>" + "<MaxResults>3</MaxResults>" + "<Containers>" + "<Container>"
                + "<Name>container4</Name>" + "</Container>" + "<Container>" + "<Name>container5</Name>"
                + "</Container>" + "</Containers>" + "<NextMarker/>" + "</EnumerationResults>";
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            String url = request.getUrl().toString();
            HttpResponse response;
            int delay = 4;

            if (url.contains("?comp=list&maxresults=")) {
                // flat first request
                response = responseHelper(request, buildFirstResponse());
            } else if (url.contains("?comp=list&marker=")) {
                // flat second request
                response = responseHelper(request, buildSecondResponse());
            } else {
                // fallback
                return Mono.just(new MockHttpResponse(request, 404));
            }

            return Mono.delay(Duration.ofSeconds(delay)).then(Mono.just(response));
        }
    }

}
