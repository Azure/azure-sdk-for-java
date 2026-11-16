// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CertificateClientBuilderTest {
    private String vaultUrl;
    private String certificateName;
    private CertificateServiceVersion serviceVersion;

    @BeforeEach
    public void setUp() {
        vaultUrl = "https://key-vault-url.vault.azure.net/";
        certificateName = "TestCertificate";
        serviceVersion = CertificateServiceVersion.V7_3;
    }

    @Test
    public void buildSyncClientTest() {
        CertificateClient certificateClient = new CertificateClientBuilder().vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 200)))
            .buildClient();

        assertNotNull(certificateClient);
        assertEquals(CertificateClient.class.getSimpleName(), certificateClient.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientUsingDefaultApiVersionTest() {
        assertEquals("2026-04-01", CertificateServiceVersion.getLatest().getVersion());
        CertificateClient certificateClient = new CertificateClientBuilder().vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> {
                assertEquals("api-version=2026-04-01", request.getUrl().getQuery());
                return Mono.just(new MockHttpResponse(request, 404));
            })
            .buildClient();

        assertNotNull(certificateClient);
        assertEquals(CertificateClient.class.getSimpleName(), certificateClient.getClass().getSimpleName());
        assertThrows(HttpResponseException.class, () -> certificateClient.getCertificate(certificateName));
    }

    @Test
    public void buildAsyncClientTest() {
        CertificateAsyncClient certificateAsyncClient = new CertificateClientBuilder().vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 200)))
            .buildAsyncClient();

        assertNotNull(certificateAsyncClient);
        assertEquals(CertificateAsyncClient.class.getSimpleName(), certificateAsyncClient.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientUsingDefaultApiVersionTest() {
        assertEquals("2026-04-01", CertificateServiceVersion.getLatest().getVersion());
        CertificateAsyncClient certificateAsyncClient = new CertificateClientBuilder().vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> {
                assertEquals("api-version=2026-04-01", request.getUrl().getQuery());
                return Mono.just(new MockHttpResponse(request, 404));
            })
            .buildAsyncClient();

        assertNotNull(certificateAsyncClient);
        assertEquals(CertificateAsyncClient.class.getSimpleName(), certificateAsyncClient.getClass().getSimpleName());
        assertThrows(HttpResponseException.class, () -> certificateAsyncClient.getCertificate(certificateName).block());
    }

    @Test
    public void supportedServiceVersions() {
        assertArrayEquals(new String[] { "7.0", "7.1", "7.2", "7.3", "7.4", "7.5", "7.6", "2025-07-01", "2026-04-01" },
            Arrays.stream(CertificateServiceVersion.values())
                .map(CertificateServiceVersion::getVersion)
                .toArray(String[]::new));
    }

    @ParameterizedTest
    @EnumSource(CertificateServiceVersion.class)
    public void buildClientsUsingExplicitApiVersion(CertificateServiceVersion version) {
        CertificateClientBuilder builder = new CertificateClientBuilder().vaultUrl(vaultUrl)
            .serviceVersion(version)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> {
                assertEquals("api-version=" + version.getVersion(), request.getUrl().getQuery());
                return Mono.just(new MockHttpResponse(request, 404));
            });

        assertThrows(HttpResponseException.class, () -> builder.buildClient().getCertificate(certificateName));
        assertThrows(HttpResponseException.class,
            () -> builder.buildAsyncClient().getCertificate(certificateName).block());
    }

    @Test
    public void nullServiceVersionResetsToDefault() {
        CertificateClientBuilder builder = new CertificateClientBuilder().vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .serviceVersion(null)
            .credential(new TestUtils.TestCredential())
            .httpClient(request -> {
                assertEquals("api-version=2026-04-01", request.getUrl().getQuery());
                return Mono.just(new MockHttpResponse(request, 404));
            });

        assertThrows(HttpResponseException.class, () -> builder.buildClient().getCertificate(certificateName));
        assertThrows(HttpResponseException.class,
            () -> builder.buildAsyncClient().getCertificate(certificateName).block());
    }

    @Test
    public void emptyVaultUrlThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new CertificateClientBuilder().vaultUrl(""));
    }

    @Test
    public void nullCredentialThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CertificateClientBuilder().credential(null));
    }

    @Test
    public void clientOptionsIsPreferredOverLogOptions() {
        CertificateClient certificateClient = new CertificateClientBuilder().vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
            .clientOptions(new ClientOptions().setApplicationId("aNewApplication"))
            .httpClient(httpRequest -> {
                assertTrue(httpRequest.getHeaders().getValue(HttpHeaderName.USER_AGENT).contains("aNewApplication"));
                return Mono.error(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, () -> certificateClient.getCertificate(certificateName));
    }

    @Test
    public void applicationIdFallsBackToLogOptions() {
        CertificateClient certificateClient = new CertificateClientBuilder().vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
            .httpClient(httpRequest -> {
                assertTrue(httpRequest.getHeaders().getValue(HttpHeaderName.USER_AGENT).contains("anOldApplication"));
                return Mono.error(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, () -> certificateClient.getCertificate(certificateName));
    }

    @Test
    public void clientOptionHeadersAreAddedLast() {
        CertificateClient certificateClient = new CertificateClientBuilder().vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .clientOptions(
                new ClientOptions().setHeaders(Collections.singletonList(new Header("User-Agent", "custom"))))
            .httpClient(httpRequest -> {
                assertEquals("custom", httpRequest.getHeaders().getValue(HttpHeaderName.USER_AGENT));
                return Mono.error(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, () -> certificateClient.getCertificate(certificateName));
    }

    @Test
    public void bothRetryOptionsAndRetryPolicySet() {
        assertThrows(IllegalStateException.class,
            () -> new CertificateClientBuilder().vaultUrl(vaultUrl)
                .serviceVersion(serviceVersion)
                .credential(new TestUtils.TestCredential())
                .retryOptions(new RetryOptions(new ExponentialBackoffOptions()))
                .retryPolicy(new RetryPolicy())
                .httpClient(request -> Mono.just(new MockHttpResponse(request, 200)))
                .buildClient());
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    @Test
    public void addPerCallPolicy() {
        CertificateAsyncClient certificateAsyncClient = new CertificateClientBuilder().vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .addPolicy(new TestUtils.PerCallPolicy())
            .addPolicy(new TestUtils.PerRetryPolicy())
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 200)))
            .buildAsyncClient();

        HttpPipeline pipeline = certificateAsyncClient.getHttpPipeline();

        int retryPolicyPosition = -1, perCallPolicyPosition = -1, perRetryPolicyPosition = -1;

        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            if (pipeline.getPolicy(i).getClass() == RetryPolicy.class) {
                retryPolicyPosition = i;
            }

            if (pipeline.getPolicy(i).getClass() == TestUtils.PerCallPolicy.class) {
                perCallPolicyPosition = i;
            }

            if (pipeline.getPolicy(i).getClass() == TestUtils.PerRetryPolicy.class) {
                perRetryPolicyPosition = i;
            }
        }

        assertTrue(perCallPolicyPosition != -1);
        assertTrue(perCallPolicyPosition < retryPolicyPosition);
        assertTrue(retryPolicyPosition < perRetryPolicyPosition);
    }
}
