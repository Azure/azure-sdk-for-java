/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.*
import com.microsoft.azure.storage.blob.models.*
import com.microsoft.rest.v2.Context
import com.microsoft.rest.v2.http.*
import com.microsoft.rest.v2.policy.RequestPolicy
import com.microsoft.rest.v2.policy.RequestPolicyFactory
import io.reactivex.Flowable
import io.reactivex.Single
import org.spockframework.lang.ISpecificationContext
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer
import java.time.OffsetDateTime

class APISpec extends Specification {
    @Shared
    Integer iterationNo = 0 // Used to generate stable container names for recording tests with multiple iterations.

    Integer entityNo = 0 // Used to generate stable container names for recording tests requiring multiple containers.

    @Shared
    ContainerURL cu

    // Fields used for conveniently creating blobs with data.
    static final String defaultText = "default"

    static final ByteBuffer defaultData = ByteBuffer.wrap(defaultText.bytes)

    static final Flowable<ByteBuffer> defaultFlowable = Flowable.just(defaultData)

    static defaultDataSize = defaultData.remaining()

    // If debugging is enabled, recordings cannot run as there can only be one proxy at a time.
    static boolean enableDebugging = false

    // Prefixes for blobs and containers
    static String containerPrefix = "jtc" // java test container

    static String blobPrefix = "javablob"

    /*
    The values below are used to create data-driven tests for access conditions.
     */
    static final OffsetDateTime oldDate = OffsetDateTime.now().minusDays(1)

    static final OffsetDateTime newDate = OffsetDateTime.now().plusDays(1)

    /*
    Note that this value is only used to check if we are depending on the received etag. This value will not actually
    be used.
     */
    static final String receivedEtag = "received"

    static final String garbageEtag = "garbage"

    /*
    Note that this value is only used to check if we are depending on the received etag. This value will not actually
    be used.
     */
    static final String receivedLeaseID = "received"

    static final String garbageLeaseID = UUID.randomUUID().toString()

    /*
    Credentials for various kinds of accounts.
     */
    static SharedKeyCredentials primaryCreds = getGenericCreds("")

    static ServiceURL primaryServiceURL = getGenericServiceURL(primaryCreds)

    static SharedKeyCredentials alternateCreds = getGenericCreds("SECONDARY_")

    /*
    URLs to various kinds of accounts.
     */
    static ServiceURL alternateServiceURL = getGenericServiceURL(alternateCreds)

    static ServiceURL blobStorageServiceURL = getGenericServiceURL(getGenericCreds("BLOB_STORAGE_"))

    static ServiceURL premiumServiceURL = getGenericServiceURL(getGenericCreds("PREMIUM_"))

    /*
    Constants for testing that the context parameter is properly passed to the pipeline.
     */
    static final String defaultContextKey = "Key"

    static final String defaultContextValue = "Value"

    static final Context defaultContext = new Context(defaultContextKey, defaultContextValue)

    static String getTestName(ISpecificationContext ctx) {
        return ctx.getCurrentFeature().name.replace(' ', '').toLowerCase()
    }

    def generateContainerName() {
        generateContainerName(specificationContext, iterationNo, entityNo++)
    }

    def generateBlobName() {
        generateBlobName(specificationContext, iterationNo, entityNo++)
    }

    /**
     * This function generates an entity name by concatenating the passed prefix, the name of the test requesting the
     * entity name, and some unique suffix. This ensures that the entity name is unique for each test so there are
     * no conflicts on the service. If we are not recording, we can just use the time. If we are recording, the suffix
     * must always be the same so we can match requests. To solve this, we use the entityNo for how many entities have
     * already been created by this test so far. This would sufficiently distinguish entities within a recording, but
     * could still yield duplicates on the service for data-driven tests. Therefore, we also add the iteration number
     * of the data driven tests.
     *
     * @param specificationContext
     *      Used to obtain the name of the test running.
     * @param prefix
     *      Used to group all entities created by these tests under common prefixes. Useful for listing.
     * @param iterationNo
     *      Indicates which iteration of a data-driven test is being executed.
     * @param entityNo
     *      Indicates how man entities have been created by the test so far. This distinguishes multiple containers
     *      or multiple blobs created by the same test. Only used when dealing with recordings.
     * @return
     */
    static String generateResourceName(ISpecificationContext specificationContext, String prefix, int iterationNo,
                                       int entityNo) {
        String suffix = ""
        suffix += System.currentTimeMillis() // For uniqueness between runs.
        suffix += entityNo // For easy identification of which call created this resource.
        return prefix + getTestName(specificationContext) + suffix
    }

    static int updateIterationNo(ISpecificationContext specificationContext, int iterationNo) {
        if (specificationContext.currentIteration.estimatedNumIterations > 1) {
            return iterationNo + 1
        } else {
            return 0
        }
    }

    static String generateContainerName(ISpecificationContext specificationContext, int iterationNo, int entityNo) {
        return generateResourceName(specificationContext, containerPrefix, iterationNo, entityNo)
    }

    static String generateBlobName(ISpecificationContext specificationContext, int iterationNo, int entityNo) {
        return generateResourceName(specificationContext, blobPrefix, iterationNo, entityNo)
    }

    static void setupFeatureRecording(String sceneName) {

    }

    static void scrubAuthHeader(String sceneName) {

    }

    static getGenericCreds(String accountType) {
        String accountName = System.getenv().get(accountType + "ACCOUNT_NAME")
        String accountKey = System.getenv().get(accountType + "ACCOUNT_KEY")
        if (accountName == null || accountKey == null) {
            System.out.println("Account name or key for the " + accountType + " account was null. Test's requiring " +
                    "these credentials will fail.")
            return null
        }
        return new SharedKeyCredentials(accountName, accountKey)
    }

    static HttpClient getHttpClient() {
        if (enableDebugging) {
            HttpClientConfiguration configuration = new HttpClientConfiguration(
                    new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)))
            return HttpClient.createDefault(configuration)
        } else return HttpClient.createDefault()
    }

    static ServiceURL getGenericServiceURL(SharedKeyCredentials creds) {
        PipelineOptions po = new PipelineOptions()
        po.withClient(getHttpClient())

        // Logging errors can be helpful for debugging in Travis.
        po.withLogger(new HttpPipelineLogger() {
            @Override
            HttpPipelineLogLevel minimumLogLevel() {
                HttpPipelineLogLevel.ERROR
            }

            @Override
            void log(HttpPipelineLogLevel httpPipelineLogLevel, String s, Object... objects) {
                System.out.println(String.format(s, objects))
            }
        })

        HttpPipeline pipeline = StorageURL.createPipeline(creds, po)

        return new ServiceURL(new URL("http://" + creds.getAccountName() + ".blob.core.windows.net"), pipeline)
    }

    static void cleanupContainers() throws MalformedURLException {
        // Create a new pipeline without any proxies
        HttpPipeline pipeline = StorageURL.createPipeline(primaryCreds, new PipelineOptions())

        ServiceURL serviceURL = new ServiceURL(
                new URL("http://" + System.getenv().get("ACCOUNT_NAME") + ".blob.core.windows.net"), pipeline)
        // There should not be more than 5000 containers from these tests
        for (ContainerItem c : serviceURL.listContainersSegment(null,
                new ListContainersOptions().withPrefix(containerPrefix), null).blockingGet()
                .body().containerItems()) {
            ContainerURL containerURL = serviceURL.createContainerURL(c.name())
            if (c.properties().leaseState().equals(LeaseStateType.LEASED)) {
                containerURL.breakLease(0, null, null).blockingGet()
            }
            containerURL.delete(null, null).blockingGet()
        }
    }

    /*
    Size must be an int because ByteBuffer sizes can only be an int. Long is not supported.
     */
    static ByteBuffer getRandomData(int size) {
        Random rand = new Random(getRandomSeed())
        byte[] data = new byte[size]
        rand.nextBytes(data)
        return ByteBuffer.wrap(data)
    }

    /*
    We only allow int because anything larger than 2GB (which would require a long) is left to stress/perf.
     */
    static File getRandomFile(int size) {
        File file = File.createTempFile(UUID.randomUUID().toString(), ".txt")
        file.deleteOnExit()
        FileOutputStream fos = new FileOutputStream(file)
        fos.write(getRandomData(size).array())
        fos.close()
        return file
    }

    static long getRandomSeed() {
        return System.currentTimeMillis()
    }

    def setupSpec() {
    }

    def cleanupSpec() {
        cleanupContainers()
    }

    def setup() {
        /*
        We'll let primary creds throw and crash if there are no credentials specified because everything else will fail.
         */
        primaryCreds = getGenericCreds("")
        primaryServiceURL = getGenericServiceURL(primaryCreds)

        /*
        It's feasible someone wants to test a specific subset of tests, so we'll still attempt to create each of the
        ServiceURLs separately. We don't really need to take any action here, as we've already reported to the user,
        so we just swallow the exception and let the relevant tests fail later. Perhaps we can add annotations or
        something in the future.
         */
        try {
            alternateCreds = getGenericCreds("SECONDARY_")
            alternateServiceURL = getGenericServiceURL(alternateCreds)
        }
        catch (Exception e) {
        }
        try {
            blobStorageServiceURL = getGenericServiceURL(getGenericCreds("BLOB_STORAGE_"))
        }
        catch (Exception e) {
        }
        try {
            premiumServiceURL = getGenericServiceURL(getGenericCreds("PREMIUM_"))
        }
        catch (Exception e) {
        }

        cu = primaryServiceURL.createContainerURL(generateContainerName())
        cu.create(null, null, null).blockingGet()
    }

    def cleanup() {
        // TODO: Scrub auth header here?
        iterationNo = updateIterationNo(specificationContext, iterationNo)
    }

    /**
     * This will retrieve the etag to be used in testing match conditions. The result will typically be assigned to
     * the ifMatch condition when testing success and the ifNoneMatch condition when testing failure.
     *
     * @param bu
     *      The URL to the blob to get the etag on.
     * @param match
     *      The ETag value for this test. If {@code receivedEtag} is passed, that will signal that the test is expecting
     *      the blob's actual etag for this test, so it is retrieved.
     * @return
     * The appropriate etag value to run the current test.
     */
    def setupBlobMatchCondition(BlobURL bu, String match) {
        if (match == receivedEtag) {
            BlobGetPropertiesHeaders headers = bu.getProperties(null, null).blockingGet().headers()
            return headers.eTag()
        } else {
            return match
        }
    }

    /**
     * This helper method will acquire a lease on a blob to prepare for testing leaseAccessConditions. We want to test
     * against a valid lease in both the success and failure cases to guarantee that the results actually indicate
     * proper setting of the header. If we pass null, though, we don't want to acquire a lease, as that will interfere
     * with other AC tests.
     *
     * @param bu
     *      The blob on which to acquire a lease.
     * @param leaseID
     *      The signalID. Values should only ever be {@code receivedLeaseID}, {@code garbageLeaseID}, or {@code null}.
     * @return
     * The actual leaseAccessConditions of the blob if recievedLeaseID is passed, otherwise whatever was passed will be
     * returned.
     */
    def setupBlobLeaseCondition(BlobURL bu, String leaseID) {
        BlobAcquireLeaseHeaders headers = null
        if (leaseID == receivedLeaseID || leaseID == garbageLeaseID) {
            headers = bu.acquireLease(null, -1, null, null).blockingGet().headers()
        }
        if (leaseID == receivedLeaseID) {
            return headers.leaseId()
        } else {
            return leaseID
        }
    }

    def setupContainerMatchCondition(ContainerURL cu, String match) {
        if (match == receivedEtag) {
            ContainerGetPropertiesHeaders headers = cu.getProperties(null, null).blockingGet().headers()
            return headers.eTag()
        } else {
            return match
        }
    }

    def setupContainerLeaseCondition(ContainerURL cu, String leaseID) {
        if (leaseID == receivedLeaseID) {
            ContainerAcquireLeaseHeaders headers =
                    cu.acquireLease(null, -1, null, null).blockingGet().headers()
            return headers.leaseId()
        } else {
            return leaseID
        }
    }

    def getMockRequest() {
        HttpHeaders headers = new HttpHeaders()
        headers.set(Constants.HeaderConstants.CONTENT_ENCODING, "en-US")
        URL url = new URL("http://devtest.blob.core.windows.net/test-container/test-blob")
        HttpRequest request = new HttpRequest(null, HttpMethod.POST, url, headers, null, null)
        return request
    }

    def waitForCopy(BlobURL bu, CopyStatusType status) {
        OffsetDateTime start = OffsetDateTime.now()
        while (status != CopyStatusType.SUCCESS) {
            status = bu.getProperties(null, null).blockingGet().headers().copyStatus()
            OffsetDateTime currentTime = OffsetDateTime.now()
            if (status == CopyStatusType.FAILED || currentTime.minusMinutes(1) == start) {
                throw new Exception("Copy failed or took too long")
            }
            sleep(1000)
        }
    }

    /**
     * Validates the presence of headers that are present on a large number of responses. These headers are generally
     * random and can really only be checked as not null.
     * @param headers
     *      The object (may be headers object or response object) that has properties which expose these common headers.
     * @return
     * Whether or not the header values are appropriate.
     */
    def validateBasicHeaders(Object headers) {
        return headers.class.getMethod("eTag").invoke(headers) != null &&
                headers.class.getMethod("lastModified").invoke(headers) != null &&
                headers.class.getMethod("requestId").invoke(headers) != null &&
                headers.class.getMethod("version").invoke(headers) != null &&
                headers.class.getMethod("date").invoke(headers) != null
    }

    def validateBlobHeaders(Object headers, String cacheControl, String contentDisposition, String contentEncoding,
                            String contentLangauge, byte[] contentMD5, String contentType) {
        return headers.class.getMethod("cacheControl").invoke(headers) == cacheControl &&
                headers.class.getMethod("contentDisposition").invoke(headers) == contentDisposition &&
                headers.class.getMethod("contentEncoding").invoke(headers) == contentEncoding &&
                headers.class.getMethod("contentLanguage").invoke(headers) == contentLangauge &&
                headers.class.getMethod("contentMD5").invoke(headers) == contentMD5 &&
                headers.class.getMethod("contentType").invoke(headers) == contentType

    }

    def enableSoftDelete() {
        primaryServiceURL.setProperties(new StorageServiceProperties()
                .withDeleteRetentionPolicy(new RetentionPolicy().withEnabled(true).withDays(2)), null)
                .blockingGet()
        sleep(30000) // Wait for the policy to take effect.
    }

    def disableSoftDelete() {
        primaryServiceURL.setProperties(new StorageServiceProperties()
                .withDeleteRetentionPolicy(new RetentionPolicy().withEnabled(false)), null).blockingGet()

        sleep(30000) // Wait for the policy to take effect.
    }



    /*
    This method returns a stub of an HttpResponse. This is for when we want to test policies in isolation but don't care
     about the status code, so we stub a response that always returns a given value for the status code. We never care
     about the number or nature of interactions with this stub.
     */

    def getStubResponse(int code) {
        return Stub(HttpResponse) {
            statusCode() >> code
        }
    }

    /*
    This is for stubbing responses that will actually go through the pipeline and autorest code. Autorest does not seem
    to play too nicely with mocked objects and the complex reflection stuff on both ends made it more difficult to work
    with than was worth it.
     */
    def getStubResponse(int code, Class responseHeadersType) {
        return new HttpResponse() {

            @Override
            int statusCode() {
                return code
            }

            @Override
            String headerValue(String s) {
                return null
            }

            @Override
            HttpHeaders headers() {
                return new HttpHeaders()
            }

            @Override
            Flowable<ByteBuffer> body() {
                return Flowable.empty()
            }

            @Override
            Single<byte[]> bodyAsByteArray() {
                return null
            }

            @Override
            Single<String> bodyAsString() {
                return null
            }

            @Override
            Object deserializedHeaders() {
                return responseHeadersType.getConstructor().newInstance()
            }

            @Override
            boolean isDecoded() {
                return true
            }
        }
    }

    /*
    This is for stubbing responses that will actually go through the pipeline and autorest code. Autorest does not seem
    to play too nicely with mocked objects and the complex reflection stuff on both ends made it more difficult to work
    with than was worth it. Because this type is just for BlobDownload, we don't need to accept a header type.
     */
    def getStubResponseForBlobDownload(int code, Flowable<ByteBuffer> body, String etag) {
        return new HttpResponse() {

            @Override
            int statusCode() {
                return code
            }

            @Override
            String headerValue(String s) {
                return null
            }

            @Override
            HttpHeaders headers() {
                return new HttpHeaders()
            }

            @Override
            Flowable<ByteBuffer> body() {
                return body
            }

            @Override
            Single<byte[]> bodyAsByteArray() {
                return null
            }

            @Override
            Single<String> bodyAsString() {
                return null
            }

            @Override
            Object deserializedHeaders() {
                def headers = new BlobDownloadHeaders()
                headers.withETag(etag)
                return headers
            }

            @Override
            boolean isDecoded() {
                return true
            }
        }
    }

    def getContextStubPolicy(int successCode, Class responseHeadersType) {
        return Mock(RequestPolicy) {
            sendAsync(_) >> { HttpRequest request ->
                if (!request.context().getData(defaultContextKey).isPresent()) {
                    return Single.error(new RuntimeException("Context key not present."))
                } else {
                    return Single.just(getStubResponse(successCode, responseHeadersType))
                }
            }
        }
    }

    def getStubFactory(RequestPolicy policy) {
        return Mock(RequestPolicyFactory) {
            create(*_) >> policy
        }
    }
}
