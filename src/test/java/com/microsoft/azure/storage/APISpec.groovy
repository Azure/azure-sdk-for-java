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

import com.microsoft.azure.storage.blob.BlobURL
import com.microsoft.azure.storage.blob.ContainerURL
import com.microsoft.azure.storage.blob.ETag
import com.microsoft.azure.storage.blob.ListContainersOptions
import com.microsoft.azure.storage.blob.PipelineOptions
import com.microsoft.azure.storage.blob.ServiceURL
import com.microsoft.azure.storage.blob.SharedKeyCredentials
import com.microsoft.azure.storage.blob.StorageURL
import com.microsoft.azure.storage.blob.models.BlobAcquireLeaseHeaders
import com.microsoft.azure.storage.blob.models.BlobGetPropertiesHeaders
import com.microsoft.azure.storage.blob.models.BlobStartCopyFromURLResponse
import com.microsoft.azure.storage.blob.models.ContainerItem
import com.microsoft.azure.storage.blob.models.ContainerAcquireLeaseHeaders
import com.microsoft.azure.storage.blob.models.ContainerGetPropertiesHeaders
import com.microsoft.azure.storage.blob.models.ContainerItem
import com.microsoft.azure.storage.blob.models.CopyStatusType
import com.microsoft.azure.storage.blob.models.LeaseStateType
import com.microsoft.azure.storage.blob.models.RetentionPolicy
import com.microsoft.azure.storage.blob.models.StorageServiceProperties
import com.microsoft.rest.v2.http.HttpClient
import com.microsoft.rest.v2.http.HttpClientConfiguration
import com.microsoft.rest.v2.http.HttpPipeline
import io.reactivex.Flowable
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

    @Shared
    String defaultText = "default"

    @Shared
    ByteBuffer defaultData = ByteBuffer.wrap(defaultText.bytes)

    @Shared
    Flowable<ByteBuffer> defaultFlowable = Flowable.just(defaultData)

    @Shared
    int defaultDataSize = defaultData.remaining()

    // If debugging is enabled, recordings cannot run as there can only be one proxy at a time.
    static final boolean enableDebugging = false

    static final String containerPrefix = "jtc" // java test container

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

    static ServiceURL blobStorageServiceURL = getGenericServiceURL(getGenericCreds("BLOB_STORAGE_"))

    static ServiceURL premiumServiceURL = getGenericServiceURL(getGenericCreds("PREMIUM_"))

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

    static HttpClient getHttpClient() {
        if (enableDebugging) {
            HttpClientConfiguration configuration = new HttpClientConfiguration(
                    new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)))
            return HttpClient.createDefault(configuration)
        }
        else return HttpClient.createDefault()
    }

    static ServiceURL getGenericServiceURL(SharedKeyCredentials creds) {
        PipelineOptions po = new PipelineOptions()
        po.client = getHttpClient()

        HttpPipeline pipeline = StorageURL.createPipeline(creds, po)

        return new ServiceURL(new URL("http://" + creds.getAccountName() + ".blob.core.windows.net"), pipeline)
    }

    static void cleanupContainers() throws MalformedURLException {
        // We don't need to clean up containers if we are playing back
        // Create a new pipeline without any proxies
        HttpPipeline pipeline = StorageURL.createPipeline(primaryCreds, new PipelineOptions())

        ServiceURL serviceURL = new ServiceURL(
                new URL("http://" + System.getenv().get("ACCOUNT_NAME") + ".blob.core.windows.net"), pipeline)
        // There should not be more than 5000 containers from these tests
        for (ContainerItem c : serviceURL.listContainersSegment(null,
                new ListContainersOptions(null, containerPrefix, null)).blockingGet()
                .body().containerItems()) {
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

    static File getRandomFile(long size) {
        File file = File.createTempFile(UUID.randomUUID().toString(), ".txt");
        file.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(getRandomData(size).array())
        fos.close();
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
        cu = primaryServiceURL.createContainerURL(generateContainerName())
        cu.create(null, null).blockingGet()
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
     *      The appropriate etag value to run the current test.
     */
    def setupBlobMatchCondition(BlobURL bu, ETag match) {
        if (match == receivedEtag) {
            BlobGetPropertiesHeaders headers = bu.getProperties(null).blockingGet().headers()
            return new ETag(headers.eTag())
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
     *      The actual leaseID of the blob if recievedLeaseID is passed, otherwise whatever was passed will be returned.
     */
    def setupBlobLeaseCondition(BlobURL bu, String leaseID) {
        BlobAcquireLeaseHeaders headers = null
        if (leaseID == receivedLeaseID || leaseID == garbageLeaseID) {
            headers = bu.acquireLease(null, -1, null).blockingGet().headers()
        }
        if (leaseID == receivedLeaseID) {
            return headers.leaseId()
        } else {
            return leaseID
        }
    }

    def setupContainerMatchCondition(ContainerURL cu, ETag match) {
        if (match == receivedEtag) {
            ContainerGetPropertiesHeaders headers = cu.getProperties(null).blockingGet().headers()
            return new ETag(headers.eTag())
        } else {
            return match
        }
    }

    def setupContainerLeaseCondition(ContainerURL cu, String leaseID) {
        if (leaseID == receivedLeaseID) {
            ContainerAcquireLeaseHeaders headers =
                    cu.acquireLease(null, -1, null).blockingGet().headers()
            return headers.leaseId()
        } else {
            return leaseID
        }
    }

    def waitForCopy(BlobURL bu, BlobStartCopyFromURLResponse response) {
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

    /**
     * Validates the presence of headers that are present on a large number of responses. These headers are generally
     * random and can really only be checked as not null.
     * @param headers
     *      The object (may be headers object or response object) that has properties which expose these common headers.
     * @return
     *      Whether or not the header values are appropriate.
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
                headers.class.getMethod("contentType").invoke(headers)  == contentType

    }

    def enableSoftDelete() {
        primaryServiceURL.setProperties(new StorageServiceProperties()
                .withDeleteRetentionPolicy(new RetentionPolicy().withEnabled(true).withDays(2)))
                .blockingGet()
        sleep(30000) // Wait for the policy to take effect.
    }

    def disableSoftDelete() {
        primaryServiceURL.setProperties(new StorageServiceProperties()
                .withDeleteRetentionPolicy(new RetentionPolicy().withEnabled(false))).blockingGet()

        sleep(30000) // Wait for the policy to take effect.
    }
}
