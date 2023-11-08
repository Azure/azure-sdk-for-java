// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.client.traits.HttpTrait;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.ServiceVersion;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.ListBlobContainersOptions;
import com.azure.storage.blob.specialized.BlobClientBase;
import com.azure.storage.blob.specialized.BlobLeaseClient;
import com.azure.storage.blob.specialized.BlobLeaseClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.test.shared.ServiceVersionValidationPolicy;
import com.azure.storage.common.test.shared.TestAccount;
import com.azure.storage.common.test.shared.TestDataFactory;
import com.azure.storage.common.test.shared.TestEnvironment;
import okhttp3.ConnectionPool;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

public class BlobBatchTestBase extends TestProxyTestBase {
    protected static final TestEnvironment ENVIRONMENT = TestEnvironment.getInstance();
    protected static final TestDataFactory DATA = TestDataFactory.getInstance();
    private static final HttpClient NETTY_HTTP_CLIENT = new NettyAsyncHttpClientBuilder().build();
    private static final HttpClient OK_HTTP_CLIENT = new OkHttpAsyncHttpClientBuilder()
        .connectionPool(new ConnectionPool(50, 5, TimeUnit.MINUTES))
        .build();

    protected static final String RECEIVED_LEASE_ID = "received";
    protected static final String GARBAGE_LEASE_ID = UUID.randomUUID().toString();

    protected String prefix;

    private int entityNo = 0; // Used to generate stable container names for recording tests requiring multiple containers.


    protected BlobServiceClient primaryBlobServiceClient;
    protected BlobServiceAsyncClient primaryBlobServiceAsyncClient;
    protected BlobServiceClient versionedBlobServiceClient;

    @Override
    public void beforeTest() {
        super.beforeTest();
        prefix = getCrc32(testContextManager.getTestPlaybackRecordingName());

        if (getTestMode() != TestMode.LIVE) {
            interceptorManager.addSanitizers(Collections.singletonList(
                new TestProxySanitizer("sig=(.*)", "REDACTED", TestProxySanitizerType.URL)));
        }

        interceptorManager.addMatchers(Arrays.asList(new BodilessMatcher(),
            new CustomMatcher().setHeadersKeyOnlyMatch(Collections.singletonList("Content-Type"))
                .setQueryOrderingIgnored(true)
                .setIgnoredQueryParameters(Arrays.asList("sv"))));

        primaryBlobServiceClient = getServiceClient(ENVIRONMENT.getPrimaryAccount());
        primaryBlobServiceAsyncClient = getServiceAsyncClient(ENVIRONMENT.getPrimaryAccount());
        versionedBlobServiceClient = getServiceClient(ENVIRONMENT.getPrimaryAccount());
    }

    /**
     * Clean up the test queues and messages for the account.
     */
    @Override
    protected void afterTest() {
        super.afterTest();
        if (getTestMode() == TestMode.PLAYBACK) {
            return;
        }

        BlobServiceClient cleanupServiceClient = new BlobServiceClientBuilder()
            .connectionString(getPrimaryConnectionString())
            .buildClient();

        cleanupServiceClient.listBlobContainers(new ListBlobContainersOptions().setPrefix(prefix), null)
            .forEach(containerItem -> cleanupServiceClient.deleteBlobContainer(containerItem.getName()));
    }

    protected BlobServiceClient getOAuthServiceClient() {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint());

        instrument(builder);

        if (getTestMode() != TestMode.PLAYBACK) {
            // AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET
            return builder.credential(new EnvironmentCredentialBuilder().build()).buildClient();
        } else {
            // Running in playback, we don't have access to the AAD environment variables, just use SharedKeyCredential.
            return builder.credential(ENVIRONMENT.getPrimaryAccount().getCredential()).buildClient();
        }
    }

    protected BlobServiceClient getServiceClient(TestAccount account) {
        return getServiceClient(account.getCredential(), account.getBlobEndpoint());
    }

    protected BlobServiceClient getServiceClient(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        return getServiceClientBuilder(credential, endpoint, policies).buildClient();
    }

    protected BlobServiceClient getServiceClient(String sasToken, String endpoint) {
        return getServiceClientBuilder(null, endpoint).sasToken(sasToken).buildClient();
    }

    protected BlobServiceAsyncClient getServiceAsyncClient(TestAccount account) {
        return getServiceClientBuilder(account.getCredential(), account.getBlobEndpoint())
            .buildAsyncClient();
    }

    protected BlobServiceClientBuilder getServiceClientBuilder(StorageSharedKeyCredential credential,
        String endpoint, HttpPipelinePolicy... policies) {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .endpoint(endpoint);

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy);
        }

        instrument(builder);

        if (credential != null) {
            builder.credential(credential);
        }

        return builder;
    }

    protected BlobContainerClient getContainerClient(String sasToken, String endpoint) {
        return getContainerClientBuilder(endpoint).sasToken(sasToken).buildClient();
    }

    protected BlobContainerClientBuilder getContainerClientBuilder(String endpoint) {
        BlobContainerClientBuilder builder = new BlobContainerClientBuilder()
            .endpoint(endpoint);

        instrument(builder);

        return builder;
    }

    protected String generateContainerName() {
        return generateResourceName(entityNo++);
    }

    protected String generateBlobName() {
        return generateResourceName(entityNo++);
    }

    protected String generateResourceName(int entityNo) {
        return testResourceNamer.randomName(prefix + entityNo, 63);
    }

    /**
     * This helper method will acquire a lease on a blob to prepare for testing lease Id. We want to test against a
     * valid lease in both the success and failure cases to guarantee that the results actually indicate proper setting
     * of the header. If we pass null, though, we don't want to acquire a lease, as that will interfere with other AC
     * tests.
     *
     * @param bc The blob on which to acquire a lease.
     * @param leaseID The signalID. Values should only ever be {@code receivedLeaseID}, {@code garbageLeaseID}, or
     * {@code null}.
     * @return The actual lease Id of the blob if recievedLeaseID is passed, otherwise whatever was passed will be
     * returned.
     */
    protected String setupBlobLeaseCondition(BlobClientBase bc, String leaseID) {
        String responseLeaseId;
        if (RECEIVED_LEASE_ID.equals(leaseID) || GARBAGE_LEASE_ID.equals(leaseID)) {
            responseLeaseId = createLeaseClient(bc, testResourceNamer.randomUuid()).acquireLease(-1);
        } else {
            responseLeaseId = leaseID;
        }

        return responseLeaseId;
    }

    protected static BlobLeaseClient createLeaseClient(BlobClientBase blobClient) {
        return createLeaseClient(blobClient, null);
    }

    protected static BlobLeaseClient createLeaseClient(BlobClientBase blobClient, String leaseId) {
        return new BlobLeaseClientBuilder()
            .blobClient(blobClient)
            .leaseId(leaseId)
            .buildClient();
    }

    private static String getCrc32(String input) {
        CRC32 crc32 = new CRC32();
        crc32.update(input.getBytes(StandardCharsets.UTF_8));
        return String.format(Locale.US, "%08X", crc32.getValue()).toLowerCase();
    }

    @SuppressWarnings("unchecked")
    protected <T extends HttpTrait<T>, E extends Enum<E>> T instrument(T builder) {
        // Groovy style reflection. All our builders follow this pattern.
        builder.httpClient(getHttpClient());

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (ENVIRONMENT.getServiceVersion() != null) {
            try {
                Method serviceVersionMethod = Arrays.stream(builder.getClass().getDeclaredMethods())
                    .filter(method -> "serviceVersion".equals(method.getName())
                        && method.getParameterCount() == 1
                        && ServiceVersion.class.isAssignableFrom(method.getParameterTypes()[0]))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Unable to find serviceVersion method for builder: "
                        + builder.getClass()));
                Class<E> serviceVersionClass = (Class<E>) serviceVersionMethod.getParameterTypes()[0];
                ServiceVersion serviceVersion = (ServiceVersion) Enum.valueOf(serviceVersionClass,
                    ENVIRONMENT.getServiceVersion());
                serviceVersionMethod.invoke(builder, serviceVersion);
                builder.addPolicy(new ServiceVersionValidationPolicy(serviceVersion.getVersion()));
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }

        return builder;
    }

    protected HttpClient getHttpClient() {
        if (ENVIRONMENT.getTestMode() != TestMode.PLAYBACK) {
            switch (ENVIRONMENT.getHttpClientType()) {
                case NETTY:
                    return NETTY_HTTP_CLIENT;
                case OK_HTTP:
                    return OK_HTTP_CLIENT;
                default:
                    throw new IllegalArgumentException("Unknown http client type: " + ENVIRONMENT.getHttpClientType());
            }
        } else {
            return interceptorManager.getPlaybackClient();
        }
    }

    protected String getPrimaryConnectionString() {
        return ENVIRONMENT.getPrimaryAccount().getConnectionString();
    }

    protected static int getIterableSize(Iterable<?> iterable) {
        if (iterable instanceof Collection<?>) {
            return ((List<?>) iterable).size();
        } else {
            int size = 0;
            for (Object ignored : iterable) {
                size++;
            }

            return size;
        }
    }
}
