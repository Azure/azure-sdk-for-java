package com.azure.storage.blob;

import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.storage.common.test.shared.TestDataFactory;
import com.azure.storage.common.test.shared.TestEnvironment;
import okhttp3.ConnectionPool;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

public class BlobTestBase extends TestProxyTestBase {
    protected static final TestEnvironment ENVIRONMENT = TestEnvironment.getInstance();
    protected static final TestDataFactory DATA = TestDataFactory.getInstance();
    private static final HttpClient NETTY_HTTP_CLIENT = new NettyAsyncHttpClientBuilder().build();
    private static final HttpClient OK_HTTP_CLIENT = new OkHttpAsyncHttpClientBuilder()
        .connectionPool(new ConnectionPool(50, 5, TimeUnit.MINUTES))
        .build();

    Integer entityNo = 0; // Used to generate stable container names for recording tests requiring multiple containers.

    // both sync and async clients point to same container
    BlobContainerClient cc;
    BlobContainerClient ccPremium;
    BlobContainerAsyncClient ccAsync;

    /*
    The values below are used to create data-driven tests for access conditions.
     */
    static final OffsetDateTime oldDate = OffsetDateTime.now().minusDays(1);

    static final OffsetDateTime newDate = OffsetDateTime.now().plusDays(1);

    /*
    Note that this value is only used to check if we are depending on the received etag. This value will not actually
    be used.
     */
    static final String receivedEtag = "received";

    static final String garbageEtag = "garbage"

    /*
    Note that this value is only used to check if we are depending on the received etag. This value will not actually
    be used.
     */
    static final String receivedLeaseID = "received";

    static final String garbageLeaseID = UUID.randomUUID().toString();

    protected BlobServiceClient primaryBlobServiceClient;
    protected BlobServiceAsyncClient primaryBlobServiceAsyncClient;
    protected BlobServiceClient alternateBlobServiceClient;
    protected BlobServiceClient premiumBlobServiceClient;
    protected BlobServiceClient versionedBlobServiceClient;
    protected BlobServiceClient softDeleteServiceClient;

    protected String containerName;

    protected String prefix;

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
        alternateBlobServiceClient = getServiceClient(ENVIRONMENT.getSecondaryAccount());
        premiumBlobServiceClient = getServiceClient(ENVIRONMENT.getPrimaryAccount());
        versionedBlobServiceClient = getServiceClient(ENVIRONMENT.getVersionedAccount());
        softDeleteServiceClient = getServiceClient(ENVIRONMENT.getSoftDeleteAccount());

        containerName = generateContainerName();
        cc = primaryBlobServiceClient.getBlobContainerClient(containerName)
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(containerName)
        ignoreErrors({ cc.create() }, BlobErrorCode.CONTAINER_ALREADY_EXISTS)
    }

    private static String getCrc32(String input) {
        CRC32 crc32 = new CRC32();
        crc32.update(input.getBytes(StandardCharsets.UTF_8));
        return String.format(Locale.US, "%08X", crc32.getValue()).toLowerCase();
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
}
