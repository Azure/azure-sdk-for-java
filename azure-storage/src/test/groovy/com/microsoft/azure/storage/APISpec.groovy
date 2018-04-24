package com.microsoft.azure.storage


import com.microsoft.azure.storage.blob.BlobURL
import com.microsoft.azure.storage.blob.ContainerURL
import com.microsoft.azure.storage.blob.ETag
import com.microsoft.azure.storage.blob.ListContainersOptions
import com.microsoft.azure.storage.blob.PipelineOptions
import com.microsoft.azure.storage.blob.ServiceURL
import com.microsoft.azure.storage.blob.SharedKeyCredentials
import com.microsoft.azure.storage.blob.StorageURL
import com.microsoft.azure.storage.blob.models.BlobsAcquireLeaseHeaders
import com.microsoft.azure.storage.blob.models.BlobsGetPropertiesHeaders
import com.microsoft.azure.storage.blob.models.BlobsStartCopyFromURLResponse
import com.microsoft.azure.storage.blob.models.Container
import com.microsoft.azure.storage.blob.models.ContainersAcquireLeaseHeaders
import com.microsoft.azure.storage.blob.models.ContainersGetPropertiesHeaders
import com.microsoft.azure.storage.blob.models.CopyStatusType
import com.microsoft.azure.storage.blob.models.LeaseStateType
import com.microsoft.rest.v2.http.HttpClient
import com.microsoft.rest.v2.http.HttpClientConfiguration
import com.microsoft.rest.v2.http.HttpPipeline
import org.spockframework.lang.ISpecificationContext
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer
import java.time.OffsetDateTime

class APISpec extends Specification {
    @Shared
    Integer iterationNo = 0 // Used to generate stable container names for recording tests with multiple iterations.

    Integer entityNo = 0 // Used to generate stable container names for recording tests requiring multiple containers.

    ContainerURL cu

    @Shared
    String defaultText = "default"

    @Shared
    ByteBuffer defaultData = ByteBuffer.wrap(defaultText.bytes)

    // If debugging is enabled, recordings cannot run as there can only be one proxy at a time.
    static final boolean enableDebugging = false

    static final String containerPrefix = "javatestcontainer"

    static final String blobPrefix = "javablob"

    /*
    The values below are used to create data-driven tests for access conditions.
     */
    static final OffsetDateTime oldDate = OffsetDateTime.now().minusDays(1)

    static final OffsetDateTime newDate = OffsetDateTime.now().plusDays(1)

    /*
    Note that this value is only used to check if we are depending on the received etag. This value will not actually
    be used.
     */
    static final ETag receivedEtag = new ETag("received")

    static final ETag garbageEtag = new ETag("garbage")

    /*
    Note that this value is only used to check if we are depending on the received etag. This value will not actually
    be used.
     */
    static final String receivedLeaseID = "received"

    static final String garbageLeaseID = UUID.randomUUID().toString()

    static SharedKeyCredentials primaryCreds = getGenericCreds("")

    static ServiceURL primaryServiceURL = getGenericServiceURL(primaryCreds)

    static SharedKeyCredentials alternateCreds = getGenericCreds("SECONDARY_")

    static ServiceURL alternateServiceURL = getGenericServiceURL(alternateCreds)

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
        return new SharedKeyCredentials(System.getenv().get(accountType + "ACCOUNT_NAME"),
                System.getenv().get(accountType + "ACCOUNT_KEY"))
    }

    static ServiceURL getGenericServiceURL(SharedKeyCredentials creds) {

        PipelineOptions po = new PipelineOptions()
        if (enableDebugging) {
            HttpClientConfiguration configuration = new HttpClientConfiguration(
                    new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)))
            po.client = HttpClient.createDefault(configuration)
        }

        HttpPipeline pipeline = StorageURL.createPipeline(creds, po)

        return new ServiceURL(new URL("http://" + creds.getAccountName() + ".blob.core.windows.net"), pipeline)

    }

    static void cleanupContainers() throws MalformedURLException {
        // We don't need to clean up containers if we are playing back
        // Create a new pipeline without any proxies
        HttpPipeline pipeline = StorageURL.createPipeline(primaryCreds, new PipelineOptions())

        ServiceURL serviceURL = new ServiceURL(
                new URL("http://" + System.getenv().get("ACCOUNT_NAME") + ".blob.core.windows.net"), pipeline)
        // There should not be more than 50000 containers from these tests
        for (Container c : serviceURL.listContainersSegment(null,
                new ListContainersOptions(null, containerPrefix, null)).blockingGet()
                .body().containers()) {
            ContainerURL containerURL = serviceURL.createContainerURL(c.name())
            if (c.properties().leaseState().equals(LeaseStateType.LEASED)) {
                containerURL.breakLease(0, null).blockingGet()
            }
            containerURL.delete(null).blockingGet()
        }
    }

    static ByteBuffer getRandomData(long size) {
        Random rand = new Random(getRandomSeed())
        byte[] data = new byte[size]
        rand.nextBytes(data)
        return ByteBuffer.wrap(data)
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
        cu = primaryServiceURL.createContainerURL(generateContainerName())
        cu.create(null, null).blockingGet()
    }

    def cleanup() {
        // TODO: Scrub auth header here?
        iterationNo = updateIterationNo(specificationContext, iterationNo)
    }

    def setupBlobMatchCondition(BlobURL bu, ETag match) {
        if (match == receivedEtag) {
            BlobsGetPropertiesHeaders headers = bu.getProperties(null).blockingGet().headers()
            return new ETag(headers.eTag())
        } else {
            return match
        }
    }

    def setupBlobLeaseCondition(BlobURL bu, String leaseID) {
        if (leaseID == receivedLeaseID) {
            BlobsAcquireLeaseHeaders headers =
                    bu.acquireLease(null, -1, null).blockingGet().headers()
            return headers.leaseId()
        } else {
            return leaseID
        }
    }

    def setupContainerMatchCondition(ContainerURL cu, ETag match) {
        if (match == receivedEtag) {
            ContainersGetPropertiesHeaders headers = cu.getProperties(null).blockingGet().headers()
            return new ETag(headers.eTag())
        } else {
            return match
        }
    }

    def setupContainerLeaseCondition(ContainerURL cu, String leaseID) {
        if (leaseID == receivedLeaseID) {
            ContainersAcquireLeaseHeaders headers =
                    cu.acquireLease(null, -1, null).blockingGet().headers()
            return headers.leaseId()
        } else {
            return leaseID
        }
    }

    def waitForCopy(BlobURL bu, BlobsStartCopyFromURLResponse response) {
        CopyStatusType status = response.headers().copyStatus()

        OffsetDateTime start = OffsetDateTime.now()
        while (status != CopyStatusType.SUCCESS) {
            status = bu.getProperties(null).blockingGet().headers().copyStatus()
            OffsetDateTime currentTime = OffsetDateTime.now()
            if (status == CopyStatusType.FAILED || currentTime.minusMinutes(1) == start) {
                throw new Exception("Copy failed or took too long")
            }
            sleep(1000)
        }
    }

    def validateBasicHeaders(Object headers) {
        return headers.class.getMethod("eTag").invoke(headers) != null &&
                headers.class.getMethod("lastModified").invoke(headers) != null &&
                headers.class.getMethod("requestId").invoke(headers) != null &&
                headers.class.getMethod("version").invoke(headers) != null &&
                headers.class.getMethod("dateProperty").invoke(headers) != null


    }
}
