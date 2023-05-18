package com.azure.storage.blob.specialized

import com.azure.core.http.HttpClient
import com.azure.core.http.HttpHeaderName
import com.azure.core.http.HttpRequest
import com.azure.core.http.HttpResponse
import com.azure.core.test.utils.TestUtils
import com.azure.core.util.BinaryData
import com.azure.core.util.Context
import com.azure.core.util.UrlBuilder
import com.azure.storage.blob.APISpec
import com.azure.storage.blob.BlobClientBuilder
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.extensions.LiveOnly
import reactor.core.publisher.Mono

import java.nio.file.Files
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.IntStream
/**
 * Set of tests that use <a href="">HTTP fault injecting</a> to simulate scenarios where the network has random errors.
 */
@LiveOnly
class HttpFaultInjectingTests extends APISpec {
    private static final def UPSTREAM_URI_HEADER = HttpHeaderName.fromString("X-Upstream-Base-Uri")
    private static final def HTTP_FAULT_INJECTOR_RESPONSE_HEADER
        = HttpHeaderName.fromString("x-ms-faultinjector-response-option")

    /**
     * Tests downloading to file with fault injection.
     *
     * This test will upload a single blob of about 9MB and then download it in parallel 500 times. Each download will
     * have its file contents compared to the original blob data. The test only cares about files that were properly
     * downloaded, if a download fails with a network error it will be ignored. A requirement of 90% of files being
     * successfully downloaded is also a requirement to prevent a case where most files failed to download and passing,
     * hiding a true issue.
     */
    def "download to file with fault injection"() {
        setup:
        def realFileBytes = new byte[9 * Constants.MB - 1]
        ThreadLocalRandom.current().nextBytes(realFileBytes)

        def containerName = generateContainerName()
        def blobName = generateBlobName()
        getServiceClient(environment.primaryAccount).createBlobContainer(containerName).getBlobClient(blobName)
            .upload(BinaryData.fromBytes(realFileBytes), true)

        def downloadClient = new BlobClientBuilder()
            .connectionString(environment.primaryAccount.connectionString)
            .containerName(containerName)
            .blobName(blobName)
            .httpClient(new HttpFaultInjectingHttpClient(getHttpClient()))
            .buildClient()

        when:
        def successCount = new AtomicInteger()
        IntStream.range(0, 500).parallel().forEach {
            def downloadFile = File.createTempFile(UUID.randomUUID().toString(), ".txt")
            downloadFile.deleteOnExit()

            downloadClient.downloadToFile(downloadFile.getAbsolutePath())

            def actualFileBytes = Files.readAllBytes(downloadFile.toPath())
            TestUtils.assertArraysEqual(realFileBytes, actualFileBytes)
            successCount.incrementAndGet()
        }

        then:
        successCount.get() >= 450
    }

    // For now a local implementation is here in azure-storage-blob until this is released in azure-core-test.
    // Since this is a local definition with a clear set of configurations everything is simplified.
    private static final class HttpFaultInjectingHttpClient implements HttpClient {
        private final HttpClient wrappedHttpClient

        HttpFaultInjectingHttpClient(HttpClient wrappedHttpClient) {
            this.wrappedHttpClient = wrappedHttpClient
        }

        @Override
        Mono<HttpResponse> send(HttpRequest request) {
            return send(request, Context.NONE)
        }

        @Override
        Mono<HttpResponse> send(HttpRequest request, Context context) {
            URL originalUrl = request.getUrl()
            request.setHeader(UPSTREAM_URI_HEADER, originalUrl.toString()).setUrl(rewriteUrl(originalUrl))
            String faultType = faultInjectorHandling()
            request.setHeader(HTTP_FAULT_INJECTOR_RESPONSE_HEADER, faultType)

            return wrappedHttpClient.send(request, context)
                .map(response -> {
                    HttpRequest request1 = response.getRequest()
                    request1.getHeaders().remove(UPSTREAM_URI_HEADER)
                    request1.setUrl(originalUrl)

                    return response
                })
        }

        @Override
        HttpResponse sendSync(HttpRequest request, Context context) {
            URL originalUrl = request.getUrl()
            request.setHeader(UPSTREAM_URI_HEADER, originalUrl.toString()).setUrl(rewriteUrl(originalUrl))
            String faultType = faultInjectorHandling()
            request.setHeader(HTTP_FAULT_INJECTOR_RESPONSE_HEADER, faultType)

            HttpResponse response = wrappedHttpClient.sendSync(request, context)
            response.getRequest().setUrl(originalUrl)
            response.getRequest().getHeaders().remove(UPSTREAM_URI_HEADER)

            return response
        }

        private static URL rewriteUrl(URL originalUrl) {
            try {
                return UrlBuilder.parse(originalUrl)
                    .setScheme("https")
                    .setHost("localhost")
                    .setPort(7778)
                    .toUrl()
            } catch (MalformedURLException e) {
                throw new RuntimeException(e)
            }
        }

        private static String faultInjectorHandling() {
            // f: Full response
            // p: Partial Response (full headers, 50% of body), then wait indefinitely
            // pc: Partial Response (full headers, 50% of body), then close (TCP FIN)
            // pa: Partial Response (full headers, 50% of body), then abort (TCP RST)
            // pn: Partial Response (full headers, 50% of body), then finish normally
            // n: No response, then wait indefinitely
            // nc: No response, then close (TCP FIN)
            // na: No response, then abort (TCP RST)
            def random = ThreadLocalRandom.current().nextDouble()
            def choice = (int) (random * 100)

            if (choice >= 25) {
                // 75% of requests complete without error.
                return "f"
            } else if (choice >= 1) {
                if (random <= 0.34D) {
                    return "n"
                } else if (random <= 0.67D) {
                    return "nc"
                } else {
                    return "na"
                }
            } else {
                if (random <= 0.25D) {
                    return "p"
                } else if (random <= 0.50D) {
                    return "pc"
                } else if (random <= 0.75D) {
                    return "pa"
                } else {
                    return "pn"
                }
            }
        }
    }
}
