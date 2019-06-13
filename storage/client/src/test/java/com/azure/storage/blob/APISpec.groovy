// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.http.*
import com.azure.core.http.policy.HttpLogDetailLevel
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.util.Context
import com.azure.storage.blob.models.*
import com.microsoft.aad.adal4j.AuthenticationContext
import com.microsoft.aad.adal4j.ClientCredential
import org.junit.Assume
import org.spockframework.lang.ISpecificationContext
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import java.util.concurrent.Executors
import java.util.function.Supplier

class APISpec extends Specification {
    static final String RECORD_MODE = "RECORD"

    @Shared
    Integer iterationNo = 0 // Used to generate stable container names for recording tests with multiple iterations.

    Integer entityNo = 0 // Used to generate stable container names for recording tests requiring multiple containers.

    @Shared
    ContainerClient cu

    // Fields used for conveniently creating blobs with data.
    static final String defaultText = "default"

    static final ByteBuffer defaultData = ByteBuffer.wrap(defaultText.getBytes(StandardCharsets.UTF_8))

    static final Flux<ByteBuffer> defaultFlux = Flux.just(defaultData)

    static final Supplier<InputStream> defaultInputStream = new Supplier<InputStream>() {
        @Override
        InputStream get() {
            return new ByteArrayInputStream(defaultText.getBytes(StandardCharsets.UTF_8))
        }
    }

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
    @Shared
    static SharedKeyCredentials primaryCreds

    @Shared
    static SharedKeyCredentials alternateCreds

    /*
    URLs to various kinds of accounts.
     */
    BlobServiceClient primaryServiceURL

    @Shared
    static BlobServiceClient alternateServiceURL

    @Shared
    static BlobServiceClient blobStorageServiceURL

    @Shared
    static BlobServiceClient premiumServiceURL

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
        return prefix + getTestName(specificationContext).take(63 - suffix.length() - prefix.length()) + suffix
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

    static getEnvironmentVariable(String variable){
        String envVariable = System.getenv().get(variable)
        if(envVariable == null){
            envVariable =  ""
        }
        return envVariable
    }

    static getGenericCreds(String accountType) {
        String accountName = getEnvironmentVariable(accountType + "ACCOUNT_NAME")
        String accountKey = getEnvironmentVariable(accountType + "ACCOUNT_KEY")

        if (accountName == null || accountKey == null) {
            System.out.println("Account name or key for the " + accountType + " account was null. Test's requiring " +
                "these credentials will fail.")
            return null
        }
        return new SharedKeyCredentials(accountName, accountKey)
    }

    static HttpClient getHttpClient() {
        if (enableDebugging) {
            return HttpClient.createDefault().proxy(new Supplier<ProxyOptions>() {
                @Override
                ProxyOptions get() {
                    return new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888))
                }
            })
        } else return HttpClient.createDefault()
    }

    static BlobServiceClient getGenericServiceURL(SharedKeyCredentials creds) {
        // TODO: logging?

        return BlobServiceClient.blobServiceClientBuilder()
            .endpoint("https://" + creds.getAccountName() + ".blob.core.windows.net")
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BASIC)
            .credentials(primaryCreds)
            .buildClient()
    }

    static void cleanupContainers() throws MalformedURLException {
        BlobServiceClient serviceURL = BlobServiceClient.blobServiceClientBuilder()
            .endpoint("http://" + primaryCreds.accountName + ".blob.core.windows.net")
            .credentials(primaryCreds)
            .buildClient()
        // There should not be more than 5000 containers from these tests
        for (ContainerItem c : serviceURL.listContainersSegment(null,
            new ListContainersOptions().withPrefix(containerPrefix))) {
            ContainerClient containerURL = serviceURL.createContainerClient(c.name())
            if (c.properties().leaseState().equals(LeaseStateType.LEASED)) {
                containerURL.breakLease(0, null, null).block()
            }
            containerURL.delete()
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
        /*
        We'll let primary creds throw and crash if there are no credentials specified because everything else will fail.
         */
        primaryCreds = getGenericCreds("PRIMARY_STORAGE_")

        /*
        It's feasible someone wants to test a specific subset of tests, so we'll still attempt to create each of the
        ServiceURLs separately. We don't really need to take any action here, as we've already reported to the user,
        so we just swallow the exception and let the relevant tests fail later. Perhaps we can add annotations or
        something in the future.
         */
        try {
            alternateCreds = getGenericCreds("SECONDARY_STORAGE_")
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
            premiumServiceURL = getGenericServiceURL(getGenericCreds("PREMIUM_STORAGE_"))
        }
        catch (Exception e) {
        }
    }

    def cleanupSpec() {
        Assume.assumeTrue("The test only runs in Live mode.", getTestMode().equalsIgnoreCase(RECORD_MODE))
        cleanupContainers()
    }

    def setup() {
        Assume.assumeTrue("The test only runs in Live mode.", getTestMode().equalsIgnoreCase(RECORD_MODE))
        String containerName = generateContainerName()

        primaryServiceURL = getGenericServiceURL(primaryCreds)
        cu = primaryServiceURL.createContainerClient(containerName)
        cu.create()
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
    def setupBlobMatchCondition(BlobClient bu, String match) {
        if (match == receivedEtag) {
            BlobGetPropertiesHeaders headers = bu.getProperties(null, null)
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
    def setupBlobLeaseCondition(BlobClient bu, String leaseID) {
        String responseLeaseId = null
        if (leaseID == receivedLeaseID || leaseID == garbageLeaseID) {
            responseLeaseId = bu.acquireLease(null, -1, null, null)
        }
        if (leaseID == receivedLeaseID) {
            return responseLeaseId
        } else {
            return leaseID
        }
    }

    def setupContainerMatchCondition(ContainerClient cu, String match) {
        if (match == receivedEtag) {
            return cu.getProperties().eTag()
        } else {
            return match
        }
    }

    def setupContainerLeaseCondition(ContainerClient cu, String leaseID) {
        if (leaseID == receivedLeaseID) {
            return cu.acquireLease(null, -1).block().deserializedHeaders().leaseId()
        } else {
            return leaseID
        }
    }

    def getMockRequest() {
        HttpHeaders headers = new HttpHeaders()
        headers.set(Constants.HeaderConstants.CONTENT_ENCODING, "en-US")
        URL url = new URL("http://devtest.blob.core.windows.net/test-container/test-blob")
        HttpRequest request = new HttpRequest(HttpMethod.POST, url, headers, null)
        return request
    }

//    def waitForCopy(ContainerClient bu, CopyStatusType status) {
//        OffsetDateTime start = OffsetDateTime.now()
//        while (status != CopyStatusType.SUCCESS) {
//            status = bu.getProperties().
//            OffsetDateTime currentTime = OffsetDateTime.now()
//            if (status == CopyStatusType.FAILED || currentTime.minusMinutes(1) == start) {
//                throw new Exception("Copy failed or took too long")
//            }
//            sleep(1000)
//        }
//    }

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
            // Quotes should be scrubbed from etag header values
//            !((String)(headers.class.getMethod("eTag").invoke(headers))).contains("\"") &&
            headers.class.getMethod("lastModified").invoke(headers) != null &&
            headers.class.getMethod("requestId").invoke(headers) != null &&
            headers.class.getMethod("version").invoke(headers) != null &&
            headers.class.getMethod("dateProperty").invoke(headers) != null
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
            .deleteRetentionPolicy(new RetentionPolicy().enabled(true).days(2)))
        sleep(30000) // Wait for the policy to take effect.
    }

    def disableSoftDelete() {
        primaryServiceURL.setProperties(new StorageServiceProperties()
            .deleteRetentionPolicy(new RetentionPolicy().enabled(false)))

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
//    def getStubResponse(int code, Class responseHeadersType) {
//        return new HttpResponse() {
//
//            @Override
//            int statusCode() {
//                return code
//            }
//
//            @Override
//            String headerValue(String s) {
//                return null
//            }
//
//            @Override
//            HttpHeaders headers() {
//                return new HttpHeaders()
//            }
//
//            @Override
//            Flux<ByteBuffer> body() {
//                return Flowable.empty()
//            }
//
//            @Override
//            Mono<byte[]> bodyAsByteArray() {
//                return null
//            }
//
//            @Override
//            Mono<String> bodyAsString() {
//                return null
//            }
//
//            @Override
//            Mono<String> bodyAsString(Charset charset) {
//                return null
//            }
//
//            @Override
//            Object deserializedHeaders() {
//                def headers = responseHeadersType.getConstructor().newInstance()
//
//                // If the headers have an etag method, we need to set it to prevent postProcessResponse from breaking.
//                try {
//                    headers.getClass().getMethod("withETag", String.class).invoke(headers, "etag");
//                }
//                catch (NoSuchMethodException e) {
//                    // No op
//                }
//                return headers
//            }
//
//            @Override
//            boolean isDecoded() {
//                return true
//            }
//        }
//    }

    /*
    This is for stubbing responses that will actually go through the pipeline and autorest code. Autorest does not seem
    to play too nicely with mocked objects and the complex reflection stuff on both ends made it more difficult to work
    with than was worth it. Because this type is just for BlobDownload, we don't need to accept a header type.
     */
//    def getStubResponseForBlobDownload(int code, Flux<ByteBuffer> body, String etag) {
//        return new HttpResponse() {
//
//            @Override
//            int statusCode() {
//                return code
//            }
//
//            @Override
//            String headerValue(String s) {
//                return null
//            }
//
//            @Override
//            HttpHeaders headers() {
//                return new HttpHeaders()
//            }
//
//            @Override
//            Flux<ByteBuffer> body() {
//                return body
//            }
//
//            @Override
//            Mono<byte[]> bodyAsByteArray() {
//                return null
//            }
//
//            @Override
//            Mono<String> bodyAsString() {
//                return null
//            }
//
//            @Override
//            Mono<String> bodyAsString(Charset charset) {
//                return null
//            }
//
//            @Override
//            Object deserializedHeaders() {
//                def headers = new BlobDownloadHeaders()
//                headers.withETag(etag)
//                return headers
//            }
//
//            @Override
//            boolean isDecoded() {
//                return true
//            }
//        }
//    }

    def getContextStubPolicy(int successCode, Class responseHeadersType) {
        return Mock(HttpPipelinePolicy) {
            sendAsync(_) >> { HttpRequest request ->
                if (!request.context().getData(defaultContextKey).isPresent()) {
                    return Mono.error(new RuntimeException("Context key not present."))
                } else {
                    return Mono.just(getStubResponse(successCode, responseHeadersType))
                }
            }
        }
    }

    def getOAuthServiceURL() {
        String tenantId = getEnvironmentVariable("MICROSOFT_AD_TENANT_ID");
        String servicePrincipalId = getEnvironmentVariable("ARM_CLIENTID");
        String servicePrincipalKey = getEnvironmentVariable("ARM_CLIENTKEY");

        def authority = String.format("https://login.microsoftonline.com/%s/oauth2/token",tenantId);
        def credential = new ClientCredential(servicePrincipalId, servicePrincipalKey)
        def token = new AuthenticationContext(authority, false, Executors.newFixedThreadPool(1)).acquireToken("https://storage.azure.com", credential, null).get().accessToken

        return BlobServiceClient.blobServiceClientBuilder()
            .endpoint(String.format("https://%s.blob.core.windows.net/", primaryCreds.accountName))
            .credentials(new TokenCredentials(token))
            .buildClient()
    }

    def getTestMode(){
        String testMode =  System.getenv("AZURE_TEST_MODE")
        if(testMode == null){
            testMode =  "PLAYBACK"
        }
        return testMode
    }
}
