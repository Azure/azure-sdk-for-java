// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.core.credential.AccessToken
import com.azure.core.credential.TokenRequestContext
import com.azure.core.http.HttpHeaders
import com.azure.core.http.HttpPipelineCallContext
import com.azure.core.http.HttpPipelineNextPolicy
import com.azure.core.http.HttpPipelinePosition
import com.azure.core.http.HttpResponse
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.test.TestMode
import com.azure.core.util.Context
import com.azure.identity.EnvironmentCredential
import com.azure.identity.EnvironmentCredentialBuilder
import com.azure.identity.implementation.IdentityClientOptions
import com.azure.storage.blob.BlobServiceClient
import com.azure.storage.blob.BlobServiceClientBuilder
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.test.shared.StorageSpec
import com.azure.storage.common.test.shared.TestAccount
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
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.function.BiFunction
import java.util.function.Function

class APISpec extends StorageSpec {

    Integer entityNo = 0 // Used to generate stable share names for recording tests requiring multiple shares.

    URL testFolder = getClass().getClassLoader().getResource("testfiles")

    // Primary Clients used for API tests
    ShareServiceClient primaryFileServiceClient
    ShareServiceAsyncClient primaryFileServiceAsyncClient
    ShareServiceClient premiumFileServiceClient
    ShareServiceAsyncClient premiumFileServiceAsyncClient

    /*
    Note that this value is only used to check if we are depending on the received etag. This value will not actually
    be used.
     */
    static final String receivedLeaseID = "received"

    static final String garbageLeaseID = UUID.randomUUID().toString()

    /**
     * Setup the File service clients commonly used for the API tests.
     */
    def setup() {
        primaryFileServiceClient = getServiceClient(env.primaryAccount)
        primaryFileServiceAsyncClient = getServiceAsyncClient(env.primaryAccount)

        premiumFileServiceClient = getServiceClient(env.premiumFileAccount)
        premiumFileServiceAsyncClient = getServiceAsyncClient(env.premiumFileAccount)
    }

    /**
     * Clean up the test shares, directories and files for the account.
     */
    def cleanup() {
        if (env.testMode != TestMode.PLAYBACK) {
            def cleanupFileServiceClient = new ShareServiceClientBuilder()
                .connectionString(env.primaryAccount.connectionString)
                .buildClient()
            for (def share : cleanupFileServiceClient.listShares(new ListSharesOptions().setPrefix(namer.getResourcePrefix()), null, Context.NONE)) {
                def shareClient = cleanupFileServiceClient.getShareClient(share.getName())

                if (share.getProperties().getLeaseState() == LeaseStateType.LEASED) {
                    createLeaseClient(shareClient).breakLeaseWithResponse(new ShareBreakLeaseOptions().setBreakPeriod(Duration.ofSeconds(0)), null, null)
                }

                shareClient.deleteWithResponse(new ShareDeleteOptions().setDeleteSnapshotsOptions(ShareSnapshotsDeleteOptionType.INCLUDE), null, null)
            }
        }
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

    ShareServiceAsyncClient getServiceAsyncClient(TestAccount account) {
        return getServiceAsyncClient(account.credential, account.fileEndpoint, null)
    }

    ShareServiceAsyncClient getServiceAsyncClient(StorageSharedKeyCredential credential, String endpoint,
                                        HttpPipelinePolicy... policies) {
        return getServiceClientBuilder(credential, endpoint, policies).buildAsyncClient()
    }

    ShareServiceClient getServiceClient(TestAccount account) {
        return getServiceClient(account.credential, account.fileEndpoint, null)
    }

    ShareServiceClient getServiceClient(StorageSharedKeyCredential credential, String endpoint,
                                       HttpPipelinePolicy... policies) {
        return getServiceClientBuilder(credential, endpoint, policies).buildClient()
    }

    def fileServiceBuilderHelper() {
        ShareServiceClientBuilder shareServiceClientBuilder = instrument(new ShareServiceClientBuilder())
        return shareServiceClientBuilder
            .connectionString(env.primaryAccount.connectionString)
    }

    ShareServiceClientBuilder getServiceClientBuilder(StorageSharedKeyCredential credential, String endpoint,
                                                     HttpPipelinePolicy... policies) {
        ShareServiceClientBuilder builder = new ShareServiceClientBuilder()
            .endpoint(endpoint)

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        instrument(builder)

        if (credential != null) {
            builder.credential(credential)
        }

        return builder
    }

    ShareClientBuilder getShareClientBuilder(String endpoint) {
        ShareClientBuilder builder = new ShareClientBuilder()
            .endpoint(endpoint)

        instrument(builder)

        return builder
    }

    def shareBuilderHelper(final String shareName) {
        return shareBuilderHelper(shareName, null)
    }

    def shareBuilderHelper(final String shareName, final String snapshot) {
        ShareClientBuilder builder = instrument(new ShareClientBuilder())
        return builder.connectionString(env.primaryAccount.connectionString)
            .shareName(shareName)
            .snapshot(snapshot)
    }

    def directoryBuilderHelper(final String shareName, final String directoryPath) {
        ShareFileClientBuilder builder = instrument(new ShareFileClientBuilder())
        return builder.connectionString(env.primaryAccount.connectionString)
            .shareName(shareName)
            .resourcePath(directoryPath)
    }

    ShareDirectoryClient getDirectoryClient(StorageSharedKeyCredential credential, String endpoint, HttpPipelinePolicy... policies) {
        ShareFileClientBuilder builder = new ShareFileClientBuilder()
            .endpoint(endpoint)

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        instrument(builder)

        if (credential != null) {
            builder.credential(credential)
        }

        return builder.buildDirectoryClient()
    }

    def fileBuilderHelper(final String shareName, final String filePath) {
        ShareFileClientBuilder builder = instrument(new ShareFileClientBuilder())
        return builder
            .connectionString(env.primaryAccount.connectionString)
            .shareName(shareName)
            .resourcePath(filePath)
    }

    ShareFileClient getFileClient(StorageSharedKeyCredential credential, String endpoint, HttpPipelinePolicy... policies) {
        ShareFileClientBuilder builder = new ShareFileClientBuilder()
            .endpoint(endpoint)

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        instrument(builder)

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
        if (env.testMode == TestMode.PLAYBACK) {
            return
        }

        sleep(milliseconds)
    }

    // Only sleep if test is running in live or record mode
    def sleepIfRecord(long milliseconds) {
        if (env.testMode != TestMode.PLAYBACK) {
            sleep(milliseconds)
        }
    }

    def getPollingDuration(long liveTestDurationInMillis) {
        return (env.testMode == TestMode.PLAYBACK) ? Duration.ofMillis(10) : Duration.ofMillis(liveTestDurationInMillis)
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
