// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.client.traits.HttpTrait;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import java.util.UUID;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.ListBlobContainersOptions;
import com.azure.storage.blob.specialized.BlobClientBase;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.StorageCommonTestUtils;
import com.azure.storage.common.test.shared.TestAccount;
import com.azure.storage.common.test.shared.TestDataFactory;
import com.azure.storage.common.test.shared.TestEnvironment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Base64;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlobNioTestBase extends TestProxyTestBase {
    protected static final TestEnvironment ENV = TestEnvironment.getInstance();
    protected static final TestDataFactory DATA = TestDataFactory.getInstance();

    // Used to generate stable container names for recording tests requiring multiple containers.
    private int entityNo = 0;

    // both sync and async clients point to same container
    protected BlobContainerClient cc;
    protected BlobContainerAsyncClient ccAsync;
    protected BlobServiceClient primaryBlobServiceClient;
    protected BlobServiceAsyncClient primaryBlobServiceAsyncClient;
    protected BlobServiceClient alternateBlobServiceClient;
    protected String containerName;
    protected String prefix;


    // The values below are used to create data-driven tests for access conditions.
    protected static final OffsetDateTime OLD_DATE = OffsetDateTime.now().minusDays(1);
    protected static final OffsetDateTime NEW_DATE = OffsetDateTime.now().plusDays(1);
    protected static final String GARBAGE_ETAG = "garbage";
    // Note that this value is only used to check if we depend on the received ETag. This value will not actually be
    // used.
    protected static final String RECEIVED_ETAG = "received";

    private static final ClientLogger LOGGER = new ClientLogger(BlobNioTestBase.class);

    @Override
    protected void beforeTest() {
        super.beforeTest();
        prefix = StorageCommonTestUtils.getCrc32(testContextManager.getTestPlaybackRecordingName());

        primaryBlobServiceClient = getServiceClient(ENV.getPrimaryAccount());
        primaryBlobServiceAsyncClient = getServiceAsyncClient(ENV.getPrimaryAccount());
        alternateBlobServiceClient = getServiceClient(ENV.getPrimaryAccount());

        containerName = generateContainerName();
        cc = primaryBlobServiceClient.getBlobContainerClient(containerName);
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(containerName);

        if (getTestMode() != TestMode.LIVE) {
            interceptorManager.addSanitizers(
                Collections.singletonList(new TestProxySanitizer("sig=(.*)", "REDACTED", TestProxySanitizerType.URL)));
            // Ignore changes to the order of query parameters and wholly ignore the 'sv' (service version) query parameter
            // in SAS tokens.
            interceptorManager.addMatchers(Collections.singletonList(new CustomMatcher()
                .setComparingBodies(false)
                .setExcludedHeaders(Arrays.asList("x-ms-copy-source", "If-Match", "x-ms-range", "If-Modified-Since",
                    "If-Unmodified-Since"))
                .setQueryOrderingIgnored(true)
                .setIgnoredQueryParameters(Arrays.asList("sv"))));
        }
    }

    @Override
    protected void afterTest() {
        super.afterTest();

        if (getTestMode() == TestMode.PLAYBACK) {
            return;
        }

        BlobServiceClient cleanupClient = getNonRecordingServiceClient();
        ListBlobContainersOptions options = new ListBlobContainersOptions().setPrefix(prefix);
        for (BlobContainerItem container : cleanupClient.listBlobContainers(options, Duration.ofSeconds(120))) {
            BlobContainerClient containerClient = cleanupClient.getBlobContainerClient(container.getName());

            containerClient.delete();
        }
    }

    protected BlobServiceClient getNonRecordingServiceClient() {
        return new BlobServiceClientBuilder()
            .httpClient(getHttpClient())
            .credential(ENV.getPrimaryAccount().getCredential())
            .endpoint(ENV.getPrimaryAccount().getBlobEndpoint())
            .buildClient();
    }

    protected BlobServiceClient getServiceClient(TestAccount account) {
        return getServiceClient(account.getCredential(), account.getBlobEndpoint());
    }

    protected BlobServiceClient getServiceClient(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        return getServiceClientBuilder(credential, endpoint, policies).buildClient();
    }

    protected BlobServiceAsyncClient getServiceAsyncClient(TestAccount account) {
        return getServiceClientBuilder(account.getCredential(), account.getBlobEndpoint())
            .buildAsyncClient();
    }

    protected BlobServiceClientBuilder getServiceClientBuilder(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
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

    protected Map<String, Object> initializeConfigMap(HttpPipelinePolicy... policies) {
        Map<String, Object> config = new HashMap<>();
        config.put(AzureFileSystem.AZURE_STORAGE_HTTP_CLIENT, getHttpClient());
        List<HttpPipelinePolicy> policyList = new ArrayList<>(Arrays.asList(policies));
        if (getTestMode() == TestMode.RECORD) {
            policyList.add(interceptorManager.getRecordPolicy());
        }
        config.put(AzureFileSystem.AZURE_STORAGE_HTTP_POLICIES, policyList.toArray(new HttpPipelinePolicy[0]));

        return config;
    }

    protected URI getFileSystemUri() {
        try {
            return new URI("azb://?endpoint=" + ENV.getPrimaryAccount().getBlobEndpoint());
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected String generateContainerName() {
        return generateResourceName(entityNo++);
    }

    protected String generateBlobName() {
        return generateResourceName(entityNo++);
    }

    private String generateResourceName(int entityNo) {
        return testResourceNamer.randomName(prefix + entityNo, 63);
    }

    protected AzureFileSystem createFS(Map<String, Object> config) {
        config.put(AzureFileSystem.AZURE_STORAGE_FILE_STORES, generateContainerName() + "," + generateContainerName());
        config.put(AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL,
            ENV.getPrimaryAccount().getCredential());

        try {
            return new AzureFileSystem(new AzureFileSystemProvider(), ENV.getPrimaryAccount().getBlobEndpoint(),
                config);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    protected byte[] getRandomByteArray(int size) {
        return StorageCommonTestUtils.getRandomByteArray(size, testResourceNamer);
    }

    /*
     Size must be an int because ByteBuffer sizes can only be an int. Long is not supported.
     */
    protected ByteBuffer getRandomData(int size) {
        return StorageCommonTestUtils.getRandomData(size, testResourceNamer);
    }

    /*
    We only allow int because anything larger than 2GB (which would require a long) is left to stress/perf.
     */
    protected File getRandomFile(byte[] bytes) {
        try {
            File file = File.createTempFile(CoreUtils.randomUuid().toString(), ".txt");
            file.deleteOnExit();
            Files.write(file.toPath(), bytes);

            return file;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static void dumpBufferContents(byte[] buffer1, byte[] buffer2) {
        LOGGER.info("Dumping array buffers:");

        String guid1 = UUID.randomUUID().toString();
        String guid2 = UUID.randomUUID().toString();

        LOGGER.info("Buffer1: " + guid1 + "\n" + Arrays.toString(buffer1) + "\n" + guid1);
        LOGGER.info("Buffer2: " + guid2 + "\n" + Arrays.toString(buffer2) + "\n" + guid1);

        String encodedBuffer1 = Base64.getEncoder().encodeToString(buffer1);
        String encodedBuffer2 = Base64.getEncoder().encodeToString(buffer2);

        LOGGER.info("Encoded Buffer1: " + guid1 + "\n" + encodedBuffer1 + "\n" + guid1);
        LOGGER.info("Encoded Buffer2: " + guid2 + "\n" + encodedBuffer2 + "\n" + guid2);
    }

    protected static void compareInputStreams(InputStream stream1, InputStream stream2, long count) {
        long pos = 0L;
        int defaultReadBuffer = 128 * Constants.KB;
        try (InputStream s1 = stream1; InputStream s2 = stream2) {
            // If the amount we are going to read is smaller than the default buffer size use that instead.
            int bufferSize = (int) Math.min(defaultReadBuffer, count);

            while (pos < count) {
                // Number of bytes we expect to read.
                int expectedReadCount = (int) Math.min(bufferSize, count - pos);
                byte[] buffer1 = new byte[expectedReadCount];
                byte[] buffer2 = new byte[expectedReadCount];

                int readCount1 = s1.read(buffer1);
                int readCount2 = s2.read(buffer2);

                if (!Arrays.equals(buffer1, buffer2)) {
                    dumpBufferContents(buffer1, buffer2);
                }

                // Use Arrays.equals as it is more optimized than Groovy/Spock's '==' for arrays.
                assertEquals(readCount1, readCount2);
                assertArraysEqual(buffer1, buffer2);

                pos += expectedReadCount;
            }

            int verificationRead = s2.read();
            assertEquals(count, pos);
            assertEquals(-1, verificationRead);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }


    protected String rootNameToContainerName(String root) {
        return root.substring(0, root.length() - 1);
    }

    protected BlobContainerClient rootNameToContainerClient(String root) {
        return primaryBlobServiceClient.getBlobContainerClient(rootNameToContainerName(root));
    }

    protected String getNonDefaultRootDir(FileSystem fs) {
        for (Path dir : fs.getRootDirectories()) {
            if (!dir.equals(((AzureFileSystem) fs).getDefaultDirectory())) {
                return dir.toString();
            }
        }
        throw new RuntimeException("File system only contains the default directory");
    }

    protected String getDefaultDir(FileSystem fs) {
        return ((AzureFileSystem) fs).getDefaultDirectory().toString();
    }

    protected String getPathWithDepth(int depth) {
        StringBuilder pathStr = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            pathStr.append(generateBlobName()).append(AzureFileSystem.PATH_SEPARATOR);
        }
        return pathStr.toString();
    }

    protected Response<BlockBlobItem> putDirectoryBlob(BlockBlobClient blobClient) {
        return blobClient.commitBlockListWithResponse(Collections.emptyList(), null,
            Collections.singletonMap(AzureResource.DIR_METADATA_MARKER, "true"), null, null, null, null);
    }

    /**
     * This will retrieve the etag to be used in testing match conditions. The result will typically be assigned to the
     * ifMatch condition when testing success and the ifNoneMatch condition when testing failure.
     *
     * @param bc The URL to the blob to get the etag on.
     * @param match The ETag value for this test. If {@code receivedEtag} is passed, that will signal that the test is
     * expecting the blob's actual etag for this test, so it is retrieved.
     * @return The appropriate etag value to run the current test.
     */
    protected String setupBlobMatchCondition(BlobClientBase bc, String match) {
        return RECEIVED_ETAG.equals(match) ? bc.getProperties().getETag() : match;
    }

    protected void checkBlobIsDir(BlobClient blobClient) {
        assertTrue(Boolean.parseBoolean(blobClient.getPropertiesWithResponse(null, null, null)
            .getValue().getMetadata().get(AzureResource.DIR_METADATA_MARKER)));
    }

    static class TestFileAttribute<T> implements FileAttribute<T> {
        private final String name;
        private final T value;

        TestFileAttribute(String name, T value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public T value() {
            return this.value;
        }
    }

    protected <T extends HttpTrait<T>, E extends Enum<E>> T instrument(T builder) {
        return StorageCommonTestUtils.instrument(builder, BlobServiceClientBuilder.getDefaultHttpLogOptions(),
            interceptorManager);
    }

    protected HttpClient getHttpClient() {
        return StorageCommonTestUtils.getHttpClient(interceptorManager);
    }
}
