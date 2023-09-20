// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.client.traits.HttpTrait;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.test.shared.ServiceVersionValidationPolicy;
import com.azure.storage.common.test.shared.TestAccount;
import com.azure.storage.common.test.shared.TestDataFactory;
import com.azure.storage.common.test.shared.TestEnvironment;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.FileSystemItem;
import com.azure.storage.file.datalake.models.LeaseStateType;
import com.azure.storage.file.datalake.models.ListFileSystemsOptions;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathProperties;
import com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient;
import com.azure.storage.file.datalake.specialized.DataLakeLeaseClient;
import com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder;
import okhttp3.ConnectionPool;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.zip.CRC32;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Base class for Azure Storage File DataLake tests.
 */
public class DataLakeTestBase extends TestProxyTestBase {
    protected static final TestEnvironment ENVIRONMENT = TestEnvironment.getInstance();
    protected static final TestDataFactory DATA = TestDataFactory.getInstance();

    // The values below are used to create data-driven tests for access conditions.
    protected static final OffsetDateTime OLD_DATE = OffsetDateTime.now().minusDays(1);
    protected static final OffsetDateTime NEW_DATE = OffsetDateTime.now().plusDays(1);

    // Note that this value is only used to check if we're depending on the received etag. This value will not actually
    // be used.
    protected static final String RECEIVED_ETAG = "received";
    protected static final String GARBAGE_ETAG = "garbage";

    // Note that this value is only used to check if we're depending on the received lease ID. This value will not
    // actually be used.
    protected static final String RECEIVED_LEASE_ID = "received";
    protected static final String GARBAGE_LEASE_ID = CoreUtils.randomUuid().toString();

    protected static final String ENCRYPTION_SCOPE_STRING = "testscope1";

    private static final HttpClient NETTY_HTTP_CLIENT = new NettyAsyncHttpClientBuilder().build();
    private static final HttpClient OK_HTTP_CLIENT =
        new OkHttpAsyncHttpClientBuilder().connectionPool(new ConnectionPool(50, 5, TimeUnit.MINUTES)).build();

    protected static final HttpHeaderName X_MS_REQUEST_SERVER_ENCRYPTED =
        HttpHeaderName.fromString(Constants.HeaderConstants.REQUEST_SERVER_ENCRYPTED);
    protected static final HttpHeaderName X_MS_ENCRYPTION_KEY_SHA256 =
        HttpHeaderName.fromString(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256);
    protected static final HttpHeaderName X_MS_VERSION = HttpHeaderName.fromString("x-ms-version");
    protected static final HttpHeaderName X_MS_BLOB_SEQUENCE_NUMBER = HttpHeaderName.fromString("x-ms-blob-sequence-number");
    protected static final HttpHeaderName X_MS_COPY_COMPLETION_TIME = HttpHeaderName.fromString("x-ms-copy-completion-time");
    protected static final HttpHeaderName X_MS_COPY_STATUS_DESCRIPTION = HttpHeaderName.fromString("x-ms-copy-status-description");
    protected static final HttpHeaderName X_MS_COPY_ID = HttpHeaderName.fromString("x-ms-copy-id");
    protected static final HttpHeaderName X_MS_COPY_PROGRESS = HttpHeaderName.fromString("x-ms-copy-progress");
    protected static final HttpHeaderName X_MS_COPY_SOURCE = HttpHeaderName.fromString("x-ms-copy-source");
    protected static final HttpHeaderName X_MS_COPY_STATUS = HttpHeaderName.fromString("x-ms-copy-status");
    protected static final HttpHeaderName X_MS_LEASE_DURATION = HttpHeaderName.fromString("x-ms-lease-duration");
    protected static final HttpHeaderName X_MS_LEASE_STATE = HttpHeaderName.fromString("x-ms-lease-state");
    protected static final HttpHeaderName X_MS_LEASE_STATUS = HttpHeaderName.fromString("x-ms-lease-status");
    protected static final HttpHeaderName X_MS_BLOB_COMMITTED_BLOCK_COUNT =
        HttpHeaderName.fromString("x-ms-blob-committed-block-count");
    protected static final HttpHeaderName X_MS_SERVER_ENCRYPTED = HttpHeaderName.fromString("x-ms-server-encrypted");
    protected static final HttpHeaderName X_MS_BLOB_CONTENT_MD5 = HttpHeaderName.fromString("x-ms-blob-content-md5");
    protected static final HttpHeaderName X_MS_CREATION_TIME = HttpHeaderName.fromString("x-ms-creation-time");
    protected static final HttpHeaderName X_MS_REQUEST_ID = HttpHeaderName.fromString("x-ms-request-id");

    protected String prefix;

    // Clients for API tests
    protected DataLakeFileSystemClient dataLakeFileSystemClient;
    protected DataLakeFileSystemAsyncClient dataLakeFileSystemAsyncClient;
    protected DataLakeServiceClient primaryDataLakeServiceClient;
    protected DataLakeServiceAsyncClient primaryDataLakeServiceAsyncClient;
    protected String fileSystemName;
    protected int entityNo = 0;

    @Override
    public void beforeTest() {
        super.beforeTest();
        prefix = getCrc32(testContextManager.getTestPlaybackRecordingName());

        if (getTestMode() != TestMode.LIVE) {
            interceptorManager.addSanitizers(Arrays.asList(
                new TestProxySanitizer("sig=(.*)", "REDACTED", TestProxySanitizerType.URL),
                new TestProxySanitizer("x-ms-encryption-key", ".*", "REDACTED", TestProxySanitizerType.HEADER),
                new TestProxySanitizer("x-ms-rename-source", "sig=(.*)", "REDACTED", TestProxySanitizerType.HEADER)));
        }

        // Ignore changes to the order of query parameters and wholly ignore the 'sv' (service version) query parameter
        // in SAS tokens.
        // TODO (alzimmer): Once all Storage libraries are migrated to test proxy move this into the common parent.
        interceptorManager.addMatchers(Collections.singletonList(new CustomMatcher()
            .setComparingBodies(false)
            .setHeadersKeyOnlyMatch(Arrays.asList("x-ms-lease-id", "x-ms-proposed-lease-id", "If-Modified-Since",
                "If-Unmodified-Since", "x-ms-expiry-time", "x-ms-source-if-modified-since",
                "x-ms-source-if-unmodified-since", "x-ms-source-lease-id", "x-ms-encryption-key-sha256"))
            .setQueryOrderingIgnored(true)
            .setIgnoredQueryParameters(Arrays.asList("sv"))));

        primaryDataLakeServiceClient = getServiceClient(ENVIRONMENT.getDataLakeAccount());
        primaryDataLakeServiceAsyncClient = getServiceAsyncClient(ENVIRONMENT.getDataLakeAccount());

        fileSystemName = generateFileSystemName();
        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(fileSystemName);
        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(fileSystemName);
        dataLakeFileSystemClient.createIfNotExists();
    }

    private static String getCrc32(String input) {
        CRC32 crc32 = new CRC32();
        crc32.update(input.getBytes(StandardCharsets.UTF_8));
        return String.format(Locale.US, "%08X", crc32.getValue()).toLowerCase();
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

        DataLakeServiceClient cleanupClient = new DataLakeServiceClientBuilder()
            .httpClient(getHttpClient())
            .credential(ENVIRONMENT.getDataLakeAccount().getCredential())
            .endpoint(ENVIRONMENT.getDataLakeAccount().getDataLakeEndpoint())
            .buildClient();

        ListFileSystemsOptions options = new ListFileSystemsOptions().setPrefix(prefix);
        for (FileSystemItem fileSystem : cleanupClient.listFileSystems(options, null)) {
            DataLakeFileSystemClient fileSystemClient = cleanupClient.getFileSystemClient(fileSystem.getName());

            if (fileSystem.getProperties().getLeaseState() == LeaseStateType.LEASED) {
                createLeaseClient(fileSystemClient).breakLeaseWithResponse(0, null, null, null);
            }

            fileSystemClient.delete();
        }
    }

    protected DataLakeServiceClient getOAuthServiceClient() {
        DataLakeServiceClientBuilder builder = new DataLakeServiceClientBuilder()
            .endpoint(ENVIRONMENT.getDataLakeAccount().getDataLakeEndpoint());

        instrument(builder);

        Configuration configuration = Configuration.getGlobalConfiguration();
        if (!interceptorManager.isPlaybackMode()) {
            // Determine whether to use the environment credential based on the shared configurations or to use the
            // credential based on resource deployment.
            if (!CoreUtils.isNullOrEmpty(configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID))
                && !CoreUtils.isNullOrEmpty(configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID))
                && !CoreUtils.isNullOrEmpty(configuration.get(Configuration.PROPERTY_AZURE_CLIENT_SECRET))) {
                // AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET
                return builder.credential(new EnvironmentCredentialBuilder().build()).buildClient();
            } else {
                // STORAGE_TENANT_ID, STORAGE_CLIENT_ID, STORAGE_CLIENT_SECRET
                return builder.credential(new ClientSecretCredentialBuilder()
                    .tenantId(configuration.get("STORAGE_TENANT_ID"))
                    .clientId(configuration.get("STORAGE_CLIENT_ID"))
                    .clientSecret(configuration.get("STORAGE_CLIENT_SECRET"))
                    .build()).buildClient();
            }
        } else {
            // Running in playback, use the mock credential.
            return builder.credential(new MockTokenCredential()).buildClient();
        }
    }

    protected DataLakeServiceClient getServiceClient(TestAccount account) {
        return getServiceClient(account.getCredential(), account.getDataLakeEndpoint());
    }

    protected DataLakeServiceClient getServiceClient(StorageSharedKeyCredential credential, String endpoint) {
        return getServiceClientBuilder(credential, endpoint).buildClient();
    }

    protected DataLakeServiceClient getServiceClient(StorageSharedKeyCredential credential,
        String endpoint,
        HttpPipelinePolicy... policies) {
        return getServiceClientBuilder(credential, endpoint, policies).buildClient();
    }

    protected DataLakeServiceClient getServiceClient(String sasToken, String endpoint) {
        return getServiceClientBuilder(null, endpoint).sasToken(sasToken).buildClient();
    }

    protected DataLakeServiceAsyncClient getServiceAsyncClient(TestAccount account) {
        return getServiceClientBuilder(account.getCredential(), account.getDataLakeEndpoint()).buildAsyncClient();
    }

    /**
     * Some tests require extra configuration for retries when writing.
     * <p>
     * It is possible that tests which upload a reasonable amount of data with tight resource limits may cause the
     * service to silently close a connection without returning a response due to high read latency (the resource
     * constraints cause a latency between sending the headers and writing the body often due to waiting for buffer pool
     * buffers). Without configuring a retry timeout, the operation will hang indefinitely. This is always something
     * that must be configured by the customer.
     * <p>
     * Typically, this needs to be configured in retries so that we can retry the individual block writes rather than
     * the overall operation.
     * <p>
     * According to the following link, writes can take up to 10 minutes per MB before the service times out. In this
     * case, most of our instrumentation (e.g. CI pipelines) will timeout and fail anyway, so we don't want to wait that
     * long. The value is going to be a best guess and should be played with to allow test passes to succeed
     * <p>
     * https://docs.microsoft.com/rest/api/storageservices/setting-timeouts-for-blob-service-operations
     *
     * @param perRequestDataSize The amount of data expected to go out in each request. Will be used to calculate a
     * timeout value--about 20s/MB. Won't be less than 1 minute.
     */
    protected DataLakeServiceAsyncClient getPrimaryServiceClientForWrites(long perRequestDataSize) {
        int retryTimeout = Math.toIntExact((perRequestDataSize / Constants.MB) * 20);
        retryTimeout = Math.max(60, retryTimeout);
        return getServiceClientBuilder(ENVIRONMENT.getDataLakeAccount())
            .retryOptions(new RequestRetryOptions(null, null, retryTimeout, null, null, null))
            .buildAsyncClient();
    }

    protected DataLakeServiceClientBuilder getServiceClientBuilder(TestAccount account,
        HttpPipelinePolicy... policies) {
        return getServiceClientBuilder(account.getCredential(), account.getDataLakeEndpoint(), policies);
    }

    protected DataLakeServiceClientBuilder getServiceClientBuilder(StorageSharedKeyCredential credential,
        String endpoint,
        HttpPipelinePolicy... policies) {
        DataLakeServiceClientBuilder builder = new DataLakeServiceClientBuilder().endpoint(endpoint);

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy);
        }

        instrument(builder);

        if (credential != null) {
            builder.credential(credential);
        }

        return builder;
    }

    protected static DataLakeLeaseClient createLeaseClient(DataLakeFileClient pathClient) {
        return createLeaseClient(pathClient, null);
    }

    protected static DataLakeLeaseClient createLeaseClient(DataLakeFileClient pathClient, String leaseId) {
        return new DataLakeLeaseClientBuilder().fileClient(pathClient).leaseId(leaseId).buildClient();
    }

    protected static DataLakeLeaseAsyncClient createLeaseAsyncClient(DataLakeFileAsyncClient pathAsyncClient) {
        return createLeaseAsyncClient(pathAsyncClient, null);
    }

    protected static DataLakeLeaseAsyncClient createLeaseAsyncClient(DataLakeFileAsyncClient pathAsyncClient,
        String leaseId) {
        return new DataLakeLeaseClientBuilder().fileAsyncClient(pathAsyncClient).leaseId(leaseId).buildAsyncClient();
    }

    protected static DataLakeLeaseClient createLeaseClient(DataLakeDirectoryClient pathClient) {
        return createLeaseClient(pathClient, null);
    }

    protected static DataLakeLeaseClient createLeaseClient(DataLakeDirectoryClient pathClient, String leaseId) {
        return new DataLakeLeaseClientBuilder().directoryClient(pathClient).leaseId(leaseId).buildClient();
    }

    protected static DataLakeLeaseClient createLeaseClient(DataLakeFileSystemClient fileSystemClient) {
        return createLeaseClient(fileSystemClient, null);
    }

    protected static DataLakeLeaseClient createLeaseClient(DataLakeFileSystemClient fileSystemClient, String leaseId) {
        return new DataLakeLeaseClientBuilder().fileSystemClient(fileSystemClient).leaseId(leaseId).buildClient();
    }

    protected DataLakeFileClient getFileClient(StorageSharedKeyCredential credential,
        String endpoint,
        HttpPipelinePolicy... policies) {
        DataLakePathClientBuilder builder = new DataLakePathClientBuilder().endpoint(endpoint);

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy);
        }

        instrument(builder);

        return builder.credential(credential).buildFileClient();
    }

    protected DataLakePathClientBuilder getPathClientBuilder(StorageSharedKeyCredential credential, String endpoint) {
        DataLakePathClientBuilder builder = new DataLakePathClientBuilder().endpoint(endpoint);

        return instrument(builder).credential(credential);
    }

    protected DataLakeFileAsyncClient getFileAsyncClient(StorageSharedKeyCredential credential,
        String endpoint,
        HttpPipelinePolicy... policies) {
        DataLakePathClientBuilder builder = new DataLakePathClientBuilder().endpoint(endpoint);

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy);
        }

        return instrument(builder)
            .credential(credential)
            .buildFileAsyncClient();
    }

    protected DataLakeFileAsyncClient getFileAsyncClient(String sasToken, String endpoint, String pathName) {
        return instrument(new DataLakePathClientBuilder().endpoint(endpoint).pathName(pathName))
            .sasToken(sasToken)
            .buildFileAsyncClient();
    }

    protected DataLakeFileClient getFileClient(StorageSharedKeyCredential credential, String endpoint, String pathName) {
        DataLakePathClientBuilder builder = new DataLakePathClientBuilder().endpoint(endpoint).pathName(pathName);

        return instrument(builder)
            .credential(credential)
            .buildFileClient();
    }

    protected DataLakeFileClient getFileClient(String sasToken, String endpoint, String pathName) {
        return instrument(new DataLakePathClientBuilder().endpoint(endpoint).pathName(pathName))
            .sasToken(sasToken)
            .buildFileClient();
    }

    protected DataLakeDirectoryClient getDirectoryClient(StorageSharedKeyCredential credential,
        String endpoint,
        String pathName) {
        DataLakePathClientBuilder builder = new DataLakePathClientBuilder().endpoint(endpoint).pathName(pathName);

        return instrument(builder)
            .credential(credential)
            .buildDirectoryClient();
    }

    protected DataLakeDirectoryClient getDirectoryClient(StorageSharedKeyCredential credential,
        String endpoint,
        String pathName,
        HttpPipelinePolicy... policies) {
        DataLakePathClientBuilder builder = new DataLakePathClientBuilder().endpoint(endpoint).pathName(pathName);

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy);
        }

        return instrument(builder)
            .credential(credential)
            .buildDirectoryClient();
    }

    protected DataLakePathClientBuilder getPathClientBuilder(StorageSharedKeyCredential credential, String endpoint,
        String pathName) {
        return instrument(new DataLakePathClientBuilder().endpoint(endpoint).pathName(pathName))
            .credential(credential);
    }

    protected DataLakeDirectoryClient getDirectoryClient(String sasToken, String endpoint, String pathName) {
        DataLakePathClientBuilder builder = new DataLakePathClientBuilder().endpoint(endpoint).pathName(pathName);

        return instrument(builder)
            .sasToken(sasToken)
            .buildDirectoryClient();
    }

    protected DataLakeFileSystemClient getFileSystemClient(String sasToken, String endpoint) {
        return getFileSystemClientBuilder(endpoint).sasToken(sasToken).buildClient();
    }

    protected DataLakeFileSystemClientBuilder getFileSystemClientBuilder(String endpoint) {
        DataLakeFileSystemClientBuilder builder = new DataLakeFileSystemClientBuilder().endpoint(endpoint);

        return instrument(builder);
    }

    protected String generateFileSystemName() {
        return generateResourceName(entityNo++);
    }

    protected String generatePathName() {
        return generateResourceName(entityNo++);
    }

    private String generateResourceName(int entityNo) {
        return testResourceNamer.randomName(prefix + entityNo, 63);
    }

    protected byte[] getRandomByteArray(int size) {
        long seed = UUID.fromString(testResourceNamer.randomUuid()).getMostSignificantBits() & Long.MAX_VALUE;
        Random rand = new Random(seed);
        byte[] data = new byte[size];
        rand.nextBytes(data);
        return data;
    }


    // Size must be an int because ByteBuffer sizes can only be an int. Long is not supported.
    protected ByteBuffer getRandomData(int size) {
        return ByteBuffer.wrap(getRandomByteArray(size));
    }

    /**
     * Validates the presence of headers that are present on a large number of responses. These headers are generally
     * random and can really only be checked as not null.
     *
     * @param headers The object (may be headers object or response object) that has properties which expose these
     * common headers.
     */
    protected void validateBasicHeaders(HttpHeaders headers) {
        assertNotNull(headers.getValue(HttpHeaderName.ETAG));
        // Quotes should be scrubbed from etag header values
        assertFalse(headers.getValue(HttpHeaderName.ETAG).contains("\""));
        assertNotNull(headers.getValue(HttpHeaderName.LAST_MODIFIED));
        assertNotNull(headers.getValue(HttpHeaderName.DATE));
        assertNotNull(headers.getValue(X_MS_REQUEST_ID));
        assertNotNull(headers.getValue(X_MS_VERSION));
    }

    protected void validatePathProperties(Response<PathProperties> response,
        String cacheControl,
        String contentDisposition,
        String contentEncoding,
        String contentLanguage,
        byte[] contentMD5,
        String contentType) {

        assertEquals(cacheControl, response.getValue().getCacheControl());
        assertEquals(contentDisposition, response.getValue().getContentDisposition());
        assertEquals(contentEncoding, response.getValue().getContentEncoding());
        assertEquals(contentLanguage, response.getValue().getContentLanguage());
        assertEquals(contentType, response.getValue().getContentType());
        assertArraysEqual(contentMD5, response.getValue().getContentMd5());
    }

    protected String setupFileSystemLeaseCondition(DataLakeFileSystemClient fsc, String leaseID) {
        return Objects.equals(RECEIVED_LEASE_ID, leaseID) ? createLeaseClient(fsc).acquireLease(-1) : leaseID;
    }

    /**
     * This will retrieve the etag to be used in testing match conditions. The result will typically be assigned to
     * the ifMatch condition when testing success and the ifNoneMatch condition when testing failure.
     *
     * @param pc The URL to the path to get the etag on.
     * @param match The ETag value for this test. If {@code receivedEtag} is passed, that will signal that the test is
     * expecting the path's actual etag for this test, so it is retrieved.
     * @return The appropriate etag value to run the current test.
     */
    protected String setupPathMatchCondition(DataLakePathClient pc, String match) {
        return Objects.equals(RECEIVED_ETAG, match) ? pc.getProperties().getETag() : match;
    }

    protected String setupPathMatchCondition(DataLakePathAsyncClient pac, String match) {
        return Objects.equals(RECEIVED_ETAG, match) ? pac.getProperties().block().getETag() : match;
    }

    /**
     * This helper method will acquire a lease on a path to prepare for testing lease id. We want to test
     * against a valid lease in both the success and failure cases to guarantee that the results actually indicate
     * proper setting of the header. If we pass null, though, we don't want to acquire a lease, as that will interfere
     * with other AC tests.
     *
     * @param pc The path on which to acquire a lease.
     * @param leaseID The signalID. Values should only ever be {@code receivedLeaseID}, {@code garbageLeaseID}, or null.
     * @return The actual lease id of the path if recievedLeaseID is passed, otherwise whatever was passed will be
     * returned.
     */
    protected String setupPathLeaseCondition(DataLakePathClient pc, String leaseID) {
        String responseLeaseId = null;


        if (Objects.equals(RECEIVED_LEASE_ID, leaseID) || Objects.equals(GARBAGE_LEASE_ID, leaseID)) {
            responseLeaseId = (pc instanceof DataLakeFileClient)
                ? createLeaseClient((DataLakeFileClient) pc).acquireLease(-1)
                : createLeaseClient((DataLakeDirectoryClient) pc).acquireLease(-1);
        }

        return Objects.equals(RECEIVED_LEASE_ID, leaseID) ? responseLeaseId : leaseID;
    }

    protected String setupPathLeaseCondition(DataLakeFileAsyncClient fac, String leaseID) {
        String responseLeaseId = null;

        if (Objects.equals(RECEIVED_LEASE_ID, leaseID) || Objects.equals(GARBAGE_LEASE_ID, leaseID)) {
            responseLeaseId =
                new DataLakeLeaseClientBuilder().fileAsyncClient(fac).buildAsyncClient().acquireLease(-1).block();
        }

        return Objects.equals(RECEIVED_LEASE_ID, leaseID) ? responseLeaseId : leaseID;
    }

    protected static void compareACL(List<PathAccessControlEntry> expected, List<PathAccessControlEntry> actual) {
        assertEquals(expected.size(), actual.size());

        for (PathAccessControlEntry entry : expected) {
            entryIsInAcl(entry, actual);
        }
    }

    protected static void entryIsInAcl(PathAccessControlEntry entry, List<PathAccessControlEntry> acl) {
        for (PathAccessControlEntry e : acl) {
            if (e.isInDefaultScope() == entry.isInDefaultScope() && Objects.equals(e.getAccessControlType(),
                entry.getAccessControlType()) && Objects.equals(e.getEntityId(), entry.getEntityId()) && Objects.equals(
                e.getPermissions(), entry.getPermissions())) {
                return;
            }
        }

        fail("'entry' wasn't contained in the 'acl'");
    }

    // We only allow int because anything larger than 2GB (which would require a long) is left to stress/perf.
    protected File getRandomFile(int size) {
        try {
            File file = File.createTempFile(UUID.randomUUID().toString(), ".txt");
            file.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                if (size > Constants.MB) {
                    byte[] data = getRandomByteArray(Constants.MB);
                    int mbChunks = size / Constants.MB;
                    int remaining = size % Constants.MB;
                    for (int i = 0; i < mbChunks; i++) {
                        fos.write(data);
                    }

                    if (remaining > 0) {
                        fos.write(data, 0, remaining);
                    }
                } else {
                    fos.write(getRandomByteArray(size));
                }
                return file;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Compares two files for having equivalent content.
     *
     * @param file1 File used to upload data to the service
     * @param file2 File used to download data from the service
     * @param offset Write offset from the upload file
     * @param count Size of the download from the service
     */
    protected void compareFiles(File file1, File file2, long offset, long count) {
        long pos = 0L;
        int defaultBufferSize = 128 * Constants.KB;

        try (FileInputStream stream1 = new FileInputStream(file1);
             FileInputStream stream2 = new FileInputStream(file2)) {

            stream1.skip(offset);

            // If the amount we are going to read is smaller than the default buffer size use that instead.
            int bufferSize = (int) Math.min(defaultBufferSize, count);

            while (pos < count) {
                // Number of bytes we expect to read.
                int expectedReadCount = (int) Math.min(bufferSize, count - pos);
                byte[] buffer1 = new byte[expectedReadCount];
                byte[] buffer2 = new byte[expectedReadCount];

                int readCount1 = stream1.read(buffer1);
                int readCount2 = stream2.read(buffer2);

                assertEquals(readCount1, readCount2);
                assertArraysEqual(buffer1, buffer2);

                pos += expectedReadCount;
            }

            int verificationRead = stream2.read();
            assertEquals(count, pos);
            assertEquals(-1, verificationRead);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    // This is for stubbing responses that will actually go through the pipeline and autorest code. Autorest does not
    // seem to play too nicely with mocked objects and the complex reflection stuff on both ends made it more difficult
    // to work with than was worth it.
    protected static HttpResponse getStubResponse(int code, HttpRequest request) {
        return new MockHttpResponse(request, code);
    }

    /**
     * Compares the two timestamps to the minute
     *
     * @param expectedTime The expected time.
     * @param actualTime The actual time.
     */
    protected void compareDatesWithPrecision(OffsetDateTime expectedTime, OffsetDateTime actualTime) {
        assertEquals(expectedTime.truncatedTo(ChronoUnit.MINUTES), actualTime.truncatedTo(ChronoUnit.MINUTES));
    }

    /**
     * Injects one retry-able IOException failure per url.
     */
    protected static final class TransientFailureInjectingHttpPipelinePolicy implements HttpPipelinePolicy {

        private final ConcurrentHashMap<String, Boolean> failureTracker = new ConcurrentHashMap<>();

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext httpPipelineCallContext,
            HttpPipelineNextPolicy httpPipelineNextPolicy) {
            HttpRequest request = httpPipelineCallContext.getHttpRequest();
            String key = request.getUrl().toString();

            // Make sure that failure happens once per url.
            if (!failureTracker.computeIfAbsent(key, ignored -> false)) {
                return httpPipelineNextPolicy.process();
            }

            failureTracker.put(key, true);
            if (request.getBody() != null) {
                return request
                    .getBody()
                    .map(byteBuffer -> {
                        // Read a byte from each buffer to simulate that failure occurred in the middle of transfer.
                        byteBuffer.get();
                        return byteBuffer;
                    })
                    .reduce(0L, (a, byteBuffer) -> a + byteBuffer.remaining())
                    .flatMap(aLong -> Mono.error(new IOException("KABOOM!")));
            } else {
                return Mono.error(new IOException("KABOOM!"));
            }
        }
    }

    protected HttpPipelinePolicy getPerCallVersionPolicy() {
        return new HttpPipelinePolicy() {
            @Override
            public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
                context.getHttpRequest().setHeader(X_MS_VERSION, "2019-02-02");
                return next.process();
            }

            @Override
            public HttpPipelinePosition getPipelinePosition() {
                return HttpPipelinePosition.PER_CALL;
            }
        };
    }

    protected <T extends HttpTrait<T>, E extends Enum<E>> T instrument(T builder) {
        // Groovy style reflection. All our builders follow this pattern.
        builder.httpClient(getHttpClient());

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (ENVIRONMENT.getServiceVersion() != null) {
            String serviceVersion = ENVIRONMENT.getServiceVersion();
            boolean foundMatchingEnum = false;
            for (DataLakeServiceVersion version : DataLakeServiceVersion.values()) {
                if (version.name().equals(serviceVersion)) {
                    builder.addPolicy(new ServiceVersionValidationPolicy(version.getVersion()));
                    foundMatchingEnum = true;
                    break;
                }
            }

            if (!foundMatchingEnum) {
                throw new IllegalArgumentException(
                    "Unable to find matching DataLakeServiceVersion for service version: " + serviceVersion);
            }
        }

        builder.httpLogOptions(DataLakeServiceClientBuilder.getDefaultHttpLogOptions());

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

    protected static boolean olderThan(DataLakeServiceVersion targetVersion) {
        String targetServiceVersionFromEnvironment = ENVIRONMENT.getServiceVersion();
        DataLakeServiceVersion version = (targetServiceVersionFromEnvironment != null)
            ? Enum.valueOf(DataLakeServiceVersion.class, targetServiceVersionFromEnvironment)
            : DataLakeServiceVersion.getLatest();

        return version.ordinal() < targetVersion.ordinal();
    }

    protected static StorageSharedKeyCredential getDataLakeCredential() {
        return ENVIRONMENT.getDataLakeAccount().getCredential();
    }

    protected String getFileSystemUrl() {
        return dataLakeFileSystemClient.getFileSystemUrl();
    }

    protected static void assertExceptionStatusCodeAndMessage(Throwable throwable, int expectedStatusCode,
                                                           BlobErrorCode errMessage) {
        DataLakeStorageException exception = assertInstanceOf(DataLakeStorageException.class, throwable);
        assertEquals(expectedStatusCode, exception.getStatusCode());
        assertEquals(errMessage.toString(), exception.getErrorCode());
    }

    protected static <T> void assertAsyncResponseStatusCode(Mono<Response<T>> response, int expectedStatusCode) {
        StepVerifier.create(response)
            .assertNext(r -> assertEquals(expectedStatusCode, r.getStatusCode()))
            .verifyComplete();
    }

    public static byte[] convertInputStreamToByteArray(InputStream inputStream, int expectedSize) throws IOException {
        int b;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(expectedSize);
        byte[] buffer = new byte[8192];

        while ((b = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, b);
        }

        return outputStream.toByteArray();
    }

    /**
     * Temporary utility method that waits if running against a live service.
     * <p>
     * Remove this once {@code azure-core-test} has a static equivalent of
     * {@link TestBase#sleepIfRunningAgainstService(long)}.
     *
     * @param sleepMillis Milliseconds to sleep.
     */
    public static void sleepIfLiveTesting(long sleepMillis) {
        if (ENVIRONMENT.getTestMode() == TestMode.PLAYBACK) {
            return;
        }

        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Utility method that waits until either the "predicate" (in this case it's a
     * {@link Supplier Supplier&lt;Boolean&gt;}) returns true or the number of delays has been reached.
     *
     * @param delayMillis Amount of milliseconds for each delay.
     * @param numberOfDelays Number of delays.
     * @param predicate Predicate that determines if waiting should complete before the number of delays has been
     * reached.
     */
    public static void waitUntilPredicate(long delayMillis, int numberOfDelays, Supplier<Boolean> predicate) {
        for (int i = 0; i < numberOfDelays; i++) {
            if (predicate.get()) {
                return;
            }

            sleepIfLiveTesting(delayMillis);
        }
    }

    public static boolean isLiveMode() {
        return ENVIRONMENT.getTestMode() == TestMode.LIVE;
    }
}
