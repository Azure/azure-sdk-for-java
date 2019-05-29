// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage

import com.microsoft.aad.adal4j.AuthenticationContext
import com.microsoft.aad.adal4j.ClientCredential
import com.microsoft.azure.management.resources.core.TestBase
import com.microsoft.azure.storage.blob.*
import com.microsoft.azure.storage.blob.models.*
import com.microsoft.azure.storage.interceptor.InterceptorManager
import com.microsoft.azure.storage.interceptor.TestResourceNamer
import com.microsoft.rest.v2.Context
import com.microsoft.rest.v2.http.*
import com.microsoft.rest.v2.policy.DecodingPolicyFactory
import com.microsoft.rest.v2.policy.RequestPolicy
import com.microsoft.rest.v2.policy.RequestPolicyFactory
import io.reactivex.Flowable
import io.reactivex.Single
import org.junit.Rule
import org.junit.rules.TestName
import org.spockframework.lang.ISpecificationContext
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer
import java.time.OffsetDateTime
import java.util.concurrent.Executors

class APISpec extends Specification {
    static final String RECORD_MODE = "RECORD"

    @Rule
    TestName testName = new TestName()

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
    @Shared
    static SharedKeyCredentials primaryCreds

    @Shared
    static SharedKeyCredentials alternateCreds

    /*
     * URLs to various kinds of accounts.
     * Removed the static shared property from URLs as these URLs are different instances in different test.
     */
    ServiceURL primaryServiceURL

    ServiceURL alternateServiceURL

    ServiceURL blobStorageServiceURL

    ServiceURL premiumServiceURL

    InterceptorManager interceptorManager

    /*
    Constants for testing that the context parameter is properly passed to the pipeline.
     */
    static final String defaultContextKey = "Key"

    static final String defaultContextValue = "Value"

    static final Context defaultContext = new Context(defaultContextKey, defaultContextValue)

    static int updateIterationNo(ISpecificationContext specificationContext, int iterationNo) {
        if (specificationContext.currentIteration.estimatedNumIterations > 1) {
            return iterationNo + 1
        } else {
            return 0
        }
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

        // Added the dummy creds when playback
        if (getTestModeType() == TestBase.TestMode.PLAYBACK) {
            return new SharedKeyCredentials(defaultText, defaultText)
        }

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
            HttpClientConfiguration configuration = new HttpClientConfiguration(
                    new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)))
            return HttpClient.createDefault(configuration)
        } else return HttpClient.createDefault()
    }

    static void cleanupContainers() throws MalformedURLException {
        if (getTestModeType() == TestBase.TestMode.PLAYBACK) {
            return
        }
        // Create a new pipeline without any proxies
        HttpPipeline pipeline = StorageURL.createPipeline(primaryCreds, new PipelineOptions())

        ServiceURL serviceURL = new ServiceURL(
                new URL("http://" + primaryCreds.accountName + ".blob.core.windows.net"), pipeline)
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

    static TestBase.TestMode getTestModeType() {
        TestBase.TestMode testModeType = TestBase.TestMode.PLAYBACK
        String testMode =  System.getenv("AZURE_TEST_MODE")
        if (testMode == "RECORD"){
            testModeType = TestBase.TestMode.RECORD
        }
        return testModeType
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
        } catch (Exception e){
        }
    }

    def setup() {
        // Initialize the interceptor manager.
        interceptorManager = InterceptorManager.create(testName.getMethodName(), getTestModeType())

        // Initialize the URL before methods as they carry along the test method information when record and playback.
        primaryServiceURL = getGenericServiceURL(primaryCreds)
        alternateServiceURL = getGenericServiceURL(alternateCreds)
        blobStorageServiceURL = getGenericServiceURL(getGenericCreds("BLOB_STORAGE_"))
        premiumServiceURL = getGenericServiceURL(getGenericCreds("PREMIUM_STORAGE_"))

        cu = primaryServiceURL.createContainerURL(generateContainerName())
        cu.create(null, null, null).blockingGet()
    }

    def cleanupSpec() {
        cleanupContainers()
    }

    def cleanup() {
        interceptorManager.finalizeInterceptor()
        // TODO: Scrub auth header here?
    }


    /**
     * This function generates an entity name by concating prefix with random string.
     * This ensures that the entity name is unique for each test so there are no conflicts on the service.
     * We will push the name into variables when recording, and pop up when playback to ensure we use the same request url.
     *
     * @param prefix
     *      Used to group all entities created by these tests under common prefixes. Useful for listing.
     * @return
     */
    def generateResourceName(String prefix) {
        return new TestResourceNamer(testName.getMethodName(), interceptorManager).randomName(prefix, 16)
    }

    def generateContainerName() {
        generateResourceName(containerPrefix)
    }

    def generateBlobName() {
        generateResourceName(blobPrefix)
    }

    def getGenericServiceURL(SharedKeyCredentials creds) {
        PipelineOptions po = new PipelineOptions()
        po.withClient(getHttpClient())

        // Logging errors can be helpful for debugging in Travis.
        po.withLogger(new HttpPipelineLogger() {
            @Override
            HttpPipelineLogLevel minimumLogLevel() {
                return HttpPipelineLogLevel.ERROR
            }

            @Override
            void log(HttpPipelineLogLevel httpPipelineLogLevel, String s, Object... objects) {
                System.out.println(String.format(s, objects))
            }
        })

        HttpPipeline newPipeline = createPipeline(creds, po)

        return new ServiceURL(new URL("http://" + creds.getAccountName() + ".blob.core.windows.net"), newPipeline)
    }

    HttpPipeline createPipeline(ICredentials credentials) {
        return createPipeline(credentials, new PipelineOptions())
    }

    HttpPipeline createPipeline(ICredentials credentials, PipelineOptions pipelineOptions) {
        /*
        PipelineOptions is mutable, but its fields refer to immutable objects. This method can pass the fields to other
        methods, but the PipelineOptions object itself can only be used for the duration of this call; it must not be
        passed to anything with a longer lifetime.
         */
        if (credentials == null) {
            credentials = new AnonymousCredentials()
        }
        if (pipelineOptions == null) {
            throw new IllegalArgumentException("pipelineOptions cannot be null. You must at least specify a client.")
        }

        // Closest to API goes first, closest to wire goes last.
        ArrayList<RequestPolicyFactory> factories = new ArrayList<>()
        factories.add(new TelemetryFactory(pipelineOptions.telemetryOptions()))
        factories.add(new RequestIDFactory())
        factories.add(new RequestRetryFactory(pipelineOptions.requestRetryOptions()))
        if (!(credentials instanceof AnonymousCredentials) && interceptorManager.recordMode) {
            factories.add(credentials)
        }
        factories.add(new DecodingPolicyFactory())
        factories.add(new LoggingFactory(pipelineOptions.loggingOptions()))

        if (getTestModeType() == TestBase.TestMode.RECORD) {
            factories.add(interceptorManager.initRecordPolicy())
            return HttpPipeline.build(new HttpPipelineOptions().withHttpClient(pipelineOptions.client())
                .withLogger(pipelineOptions.logger()), Arrays.asList(factories.toArray(new RequestPolicyFactory[factories.size()])))
        }

        return HttpPipeline.build(interceptorManager.initPlaybackClient(), factories.toArray(new RequestPolicyFactory[factories.size()]))
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
                // Quotes should be scrubbed from etag header values
                !((String)(headers.class.getMethod("eTag").invoke(headers))).contains("\"") &&
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
                def headers = responseHeadersType.getConstructor().newInstance()

                // If the headers have an etag method, we need to set it to prevent postProcessResponse from breaking.
                try {
                    headers.getClass().getMethod("withETag", String.class).invoke(headers, "etag");
                }
                catch (NoSuchMethodException e) {
                    // No op
                }
                return headers
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

    def getOAuthServiceURL() {
        ICredentials creds
        if (getTestModeType() == TestBase.TestMode.PLAYBACK) {
            creds = new AnonymousCredentials()
        } else {
            String tenantId = getEnvironmentVariable("MICROSOFT_AD_TENANT_ID");
            String servicePrincipalId = getEnvironmentVariable("ARM_CLIENTID");
            String servicePrincipalKey = getEnvironmentVariable("ARM_CLIENTKEY");

            def authority = String.format("https://login.microsoftonline.com/%s/oauth2/token", tenantId);
            def credential = new ClientCredential(servicePrincipalId, servicePrincipalKey)
            def token = new AuthenticationContext(authority, false, Executors.newFixedThreadPool(1)).acquireToken("https://storage.azure.com", credential, null).get().accessToken
            creds = new TokenCredentials(token)
        }
        return new ServiceURL(
                new URL(String.format("https://%s.blob.core.windows.net/", primaryCreds.accountName)),
                createPipeline(creds))
    }

    def getTestMode(){
        String testMode =  System.getenv("AZURE_TEST_MODE")
        if(testMode == null){
            testMode =  "PLAYBACK"
        }
        return testMode
    }

    def getCurrentTime() {
        return new TestResourceNamer(testName.getMethodName(), interceptorManager).getCurrentTime()
    }
}

