// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.core.http.HttpHeaders
import com.azure.core.http.HttpPipelineCallContext
import com.azure.core.http.HttpPipelineNextPolicy
import com.azure.core.http.HttpPipelinePosition
import com.azure.core.http.HttpResponse
import com.azure.core.http.policy.HttpLogDetailLevel
import com.azure.core.http.policy.HttpLogOptions
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.test.TestMode
import com.azure.core.util.Configuration
import com.azure.core.util.Context
import com.azure.core.util.logging.ClientLogger
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.test.shared.StorageSpec
import com.azure.storage.file.share.models.LeaseStateType
import com.azure.storage.file.share.models.ListSharesOptions
import com.azure.storage.file.share.models.ShareSnapshotsDeleteOptionType
import com.azure.storage.file.share.options.ShareAcquireLeaseOptions
import com.azure.storage.file.share.options.ShareBreakLeaseOptions
import com.azure.storage.file.share.options.ShareDeleteOptions
import com.azure.storage.file.share.specialized.ShareLeaseAsyncClient
import com.azure.storage.file.share.specialized.ShareLeaseClient
import com.azure.storage.file.share.specialized.ShareLeaseClientBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.function.BiFunction
import java.util.function.Function

class APISpec extends StorageSpec {
    // Field common used for all APIs.
    static ClientLogger logger = new ClientLogger(APISpec.class)

    Integer entityNo = 0 // Used to generate stable share names for recording tests requiring multiple shares.

    URL testFolder = getClass().getClassLoader().getResource("testfiles")

    public static final String defaultEndpointTemplate = "https://%s.file.core.windows.net/"

    static def PREMIUM_STORAGE = "PREMIUM_STORAGE_FILE_"
    static StorageSharedKeyCredential premiumCredential

    static def PRIMARY_STORAGE = "AZURE_STORAGE_FILE_"
    protected static StorageSharedKeyCredential primaryCredential

    // Primary Clients used for API tests
    ShareServiceClient primaryFileServiceClient
    ShareServiceAsyncClient primaryFileServiceAsyncClient
    ShareServiceClient premiumFileServiceClient
    ShareServiceAsyncClient premiumFileServiceAsyncClient

    String connectionString

    /*
    Note that this value is only used to check if we are depending on the received etag. This value will not actually
    be used.
     */
    static final String receivedLeaseID = "received"

    static final String garbageLeaseID = UUID.randomUUID().toString()

    def defaultData = ByteBuffer.wrap("default".getBytes(StandardCharsets.UTF_8))
    def defaultFlux = Flux.just(defaultData)
    Long defaultDataLength = defaultData.remaining()

    /**
     * Setup the File service clients commonly used for the API tests.
     */
    def setup() {
        premiumCredential = getCredential(PREMIUM_STORAGE)
        primaryCredential = getCredential(PRIMARY_STORAGE)
        if (ENVIRONMENT.testMode != TestMode.PLAYBACK) {
            connectionString = Configuration.getGlobalConfiguration().get("AZURE_STORAGE_FILE_CONNECTION_STRING")
        } else {
            connectionString = "DefaultEndpointsProtocol=https;AccountName=teststorage;" +
                "AccountKey=atestaccountkey;EndpointSuffix=core.windows.net"
        }
        primaryFileServiceClient = setClient(primaryCredential)
        primaryFileServiceAsyncClient = setAsyncClient(primaryCredential)

        premiumFileServiceClient = setClient(premiumCredential)
        premiumFileServiceAsyncClient = setAsyncClient(premiumCredential)
    }

    /**
     * Clean up the test shares, directories and files for the account.
     */
    def cleanup() {
        def cleanupFileServiceClient = new ShareServiceClientBuilder()
            .connectionString(connectionString)
            .addPolicy(getRecordPolicy())
            .httpClient(getHttpClient())
            .buildClient()
        for (def share : cleanupFileServiceClient.listShares(new ListSharesOptions().setPrefix(namer.getResourcePrefix()), null, Context.NONE)) {
            def shareClient = cleanupFileServiceClient.getShareClient(share.getName())

            if (share.getProperties().getLeaseState() == LeaseStateType.LEASED) {
                createLeaseClient(shareClient).breakLeaseWithResponse(new ShareBreakLeaseOptions().setBreakPeriod(Duration.ofSeconds(0)), null, null)
            }

            shareClient.deleteWithResponse(new ShareDeleteOptions().setDeleteSnapshotsOptions(ShareSnapshotsDeleteOptionType.INCLUDE), null, null)
        }
    }

    // TODO (kasobol-msft) remove this after migration
    @Override
    protected shouldUseThisToRecord() {
        return true
    }

    private StorageSharedKeyCredential getCredential(String accountType) {
        String accountName
        String accountKey

        if (ENVIRONMENT.testMode != TestMode.PLAYBACK) {
            accountName = Configuration.getGlobalConfiguration().get(accountType + "ACCOUNT_NAME")
            accountKey = Configuration.getGlobalConfiguration().get(accountType + "ACCOUNT_KEY")
        } else {
            accountName = "azstoragesdkaccount"
            accountKey = "astorageaccountkey"
        }

        if (accountName == null || accountKey == null) {
            logger.warning("Account name or key for the {} account was null. Test's requiring these credentials will fail.", accountType)
            return null
        }

        return new StorageSharedKeyCredential(accountName, accountKey)
    }

    static boolean liveMode() {
        return ENVIRONMENT.testMode != TestMode.PLAYBACK
    }

    def generateShareName() {
        generateResourceName(entityNo++)
    }

    def generatePathName() {
        generateResourceName(entityNo++)
    }

    private String generateResourceName(int entityNo) {
        return namer.getRandomName(namer.getResourcePrefix() + entityNo, 63)
    }

    ShareServiceAsyncClient setAsyncClient(StorageSharedKeyCredential credential) {
        try {
            return getServiceAsyncClient(credential)
        } catch (Exception ignore) {
            return null
        }
    }

    ShareServiceAsyncClient getServiceAsyncClient(StorageSharedKeyCredential credential) {
        return getServiceAsyncClient(credential, String.format(defaultEndpointTemplate, credential.getAccountName()), null)
    }

    ShareServiceAsyncClient getServiceAsyncClient(StorageSharedKeyCredential credential, String endpoint,
                                        HttpPipelinePolicy... policies) {
        return getServiceClientBuilder(credential, endpoint, policies).buildAsyncClient()
    }

    ShareServiceClient setClient(StorageSharedKeyCredential credential) {
        try {
            return getServiceClient(credential)
        } catch (Exception ignore) {
            return null
        }
    }

    ShareServiceClient getServiceClient(StorageSharedKeyCredential credential) {
        // TODO : Remove this once its no longer preprod
//        if (credential == premiumCredential) {
//            return getServiceClient(credential, String.format("https://%s.file.preprod.core.windows.net/", credential.getAccountName()), null)
//        }
        return getServiceClient(credential, String.format(defaultEndpointTemplate, credential.getAccountName()), null)
    }

    ShareServiceClient getServiceClient(StorageSharedKeyCredential credential, String endpoint,
                                       HttpPipelinePolicy... policies) {
        return getServiceClientBuilder(credential, endpoint, policies).buildClient()
    }

    def fileServiceBuilderHelper() {
        ShareServiceClientBuilder shareServiceClientBuilder = new ShareServiceClientBuilder();
        shareServiceClientBuilder.addPolicy(getRecordPolicy())
        return shareServiceClientBuilder
            .connectionString(connectionString)
            .httpClient(getHttpClient())
    }

    ShareServiceClientBuilder getServiceClientBuilder(StorageSharedKeyCredential credential, String endpoint,
                                                     HttpPipelinePolicy... policies) {
        ShareServiceClientBuilder builder = new ShareServiceClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        builder.addPolicy(getRecordPolicy())

        if (credential != null) {
            builder.credential(credential)
        }

        return builder
    }

    ShareClientBuilder getShareClientBuilder(String endpoint) {
        ShareClientBuilder builder = new ShareClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())

        builder.addPolicy(getRecordPolicy())

        return builder
    }

    def shareBuilderHelper(final String shareName) {
        return shareBuilderHelper(shareName, null)
    }

    def shareBuilderHelper(final String shareName, final String snapshot) {
        ShareClientBuilder builder = new ShareClientBuilder()
        builder.addPolicy(getRecordPolicy())
        return builder.connectionString(connectionString)
            .shareName(shareName)
            .snapshot(snapshot)
            .httpClient(getHttpClient())
    }

    def directoryBuilderHelper(final String shareName, final String directoryPath) {
        ShareFileClientBuilder builder = new ShareFileClientBuilder()
        builder.addPolicy(getRecordPolicy())
        return builder.connectionString(connectionString)
            .shareName(shareName)
            .resourcePath(directoryPath)
            .httpClient(getHttpClient())
    }

    ShareDirectoryClient getDirectoryClient(StorageSharedKeyCredential credential, String endpoint, HttpPipelinePolicy... policies) {
        ShareFileClientBuilder builder = new ShareFileClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        builder.addPolicy(getRecordPolicy())

        if (credential != null) {
            builder.credential(credential)
        }

        return builder.buildDirectoryClient()
    }

    def fileBuilderHelper(final String shareName, final String filePath) {
        ShareFileClientBuilder builder = new ShareFileClientBuilder()
        builder.addPolicy(getRecordPolicy())
        return builder
            .connectionString(connectionString)
            .shareName(shareName)
            .resourcePath(filePath)
            .httpClient(getHttpClient())
    }

    ShareFileClient getFileClient(StorageSharedKeyCredential credential, String endpoint, HttpPipelinePolicy... policies) {
        ShareFileClientBuilder builder = new ShareFileClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        builder.addPolicy(getRecordPolicy())

        if (credential != null) {
            builder.credential(credential)
        }

        return builder.buildFileClient()
    }

    InputStream getInputStream(byte[] data) {
        return new ByteArrayInputStream(data)
    }

    static ShareLeaseClient createLeaseClient(ShareFileClient fileClient) {
        return createLeaseClient(fileClient, null)
    }

    static ShareLeaseClient createLeaseClient(ShareFileClient fileClient, String leaseId) {
        return new ShareLeaseClientBuilder()
            .fileClient(fileClient)
            .leaseId(leaseId)
            .buildClient()
    }

    static ShareLeaseAsyncClient createLeaseClient(ShareFileAsyncClient fileClient) {
        return createLeaseClient(fileClient, null)
    }

    static ShareLeaseAsyncClient createLeaseClient(ShareFileAsyncClient fileClient, String leaseId) {
        return new ShareLeaseClientBuilder()
            .fileAsyncClient(fileClient)
            .leaseId(leaseId)
            .buildAsyncClient()
    }

    static ShareLeaseClient createLeaseClient(ShareClient shareClient) {
        return createLeaseClient(shareClient, null)
    }

    static ShareLeaseClient createLeaseClient(ShareClient shareClient, String leaseId) {
        return new ShareLeaseClientBuilder()
            .shareClient(shareClient)
            .leaseId(leaseId)
            .buildClient()
    }

    static ShareLeaseAsyncClient createLeaseClient(ShareAsyncClient shareClient) {
        return createLeaseClient(shareClient, null)
    }

    static ShareLeaseAsyncClient createLeaseClient(ShareAsyncClient shareClient, String leaseId) {
        return new ShareLeaseClientBuilder()
            .shareAsyncClient(shareClient)
            .leaseId(leaseId)
            .buildAsyncClient()
    }

    /**
     * This helper method will acquire a lease on a blob to prepare for testing lease Id. We want to test
     * against a valid lease in both the success and failure cases to guarantee that the results actually indicate
     * proper setting of the header. If we pass null, though, we don't want to acquire a lease, as that will interfere
     * with other AC tests.
     *
     * @param fc
     *      The blob on which to acquire a lease.
     * @param leaseID
     *      The signalID. Values should only ever be {@code receivedLeaseID}, {@code garbageLeaseID}, or {@code null}.
     * @return
     * The actual lease Id of the blob if recievedLeaseID is passed, otherwise whatever was passed will be
     * returned.
     */
    def setupFileLeaseCondition(ShareFileClient fc, String leaseID) {
        String responseLeaseId = null
        if (leaseID == receivedLeaseID || leaseID == garbageLeaseID) {
            responseLeaseId = createLeaseClient(fc).acquireLease()
        }
        if (leaseID == receivedLeaseID) {
            return responseLeaseId
        } else {
            return leaseID
        }
    }

    void sleepIfLive(long milliseconds) {
        if (ENVIRONMENT.testMode == TestMode.PLAYBACK) {
            return
        }

        sleep(milliseconds)
    }

    // Only sleep if test is running in live or record mode
    def sleepIfRecord(long milliseconds) {
        if (ENVIRONMENT.testMode != TestMode.PLAYBACK) {
            sleep(milliseconds)
        }
    }

    def getPollingDuration(long liveTestDurationInMillis) {
        return (ENVIRONMENT.testMode == TestMode.PLAYBACK) ? Duration.ofMillis(10) : Duration.ofMillis(liveTestDurationInMillis)
    }

    /**
     * Validates the presence of headers that are present on a large number of responses. These headers are generally
     * random and can really only be checked as not null.
     * @param headers
     *      The object (may be headers object or response object) that has properties which expose these common headers.
     * @return
     * Whether or not the header values are appropriate.
     */
    def validateBasicHeaders(HttpHeaders headers) {
        return headers.getValue("etag") != null &&
            // Quotes should be scrubbed from etag header values
            !headers.getValue("etag").contains("\"") &&
            headers.getValue("last-modified") != null &&
            headers.getValue("x-ms-request-id") != null &&
            headers.getValue("x-ms-version") != null &&
            headers.getValue("date") != null
    }

    def setupShareLeaseCondition(ShareClient sc, String leaseID) {
        if (leaseID == receivedLeaseID) {
            return createLeaseClient(sc).acquireLeaseWithResponse(new ShareAcquireLeaseOptions().setDuration(-1), null, null).getValue()
        } else {
            return leaseID
        }
    }

    static boolean playbackMode() {
        return ENVIRONMENT.testMode == TestMode.PLAYBACK
    }

    def getPerCallVersionPolicy() {
        return new HttpPipelinePolicy() {
            @Override
            Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
                context.getHttpRequest().setHeader("x-ms-version","2017-11-09")
                return next.process()
            }
            @Override
            HttpPipelinePosition getPipelinePosition() {
                return HttpPipelinePosition.PER_CALL
            }
        }
    }

    /**
     * Injects one retry-able IOException failure per url.
     */
    class TransientFailureInjectingHttpPipelinePolicy implements HttpPipelinePolicy {

        private ConcurrentHashMap<String, Boolean> failureTracker = new ConcurrentHashMap<>();

        @Override
        Mono<HttpResponse> process(HttpPipelineCallContext httpPipelineCallContext, HttpPipelineNextPolicy httpPipelineNextPolicy) {
            def request = httpPipelineCallContext.httpRequest
            def key = request.url.toString()
            // Make sure that failure happens once per url.
            if (failureTracker.get(key, false)) {
                return httpPipelineNextPolicy.process()
            } else {
                failureTracker.put(key, true)
                return request.getBody().flatMap {
                    byteBuffer ->
                        // Read a byte from each buffer to simulate that failure occurred in the middle of transfer.
                        byteBuffer.get()
                        return Flux.just(byteBuffer)
                }.reduce( 0L, {
                        // Reduce in order to force processing of all buffers.
                    a, byteBuffer ->
                        return a + byteBuffer.remaining()
                } as BiFunction<Long, ByteBuffer, Long>
                ).flatMap ({
                    aLong ->
                        // Throw retry-able error.
                        return Mono.error(new IOException("KABOOM!"))
                } as Function<Long, Mono<HttpResponse>>)
            }
        }
    }
}
