package com.microsoft.azure.storage

import com.linkedin.flashback.SceneAccessLayer
import com.linkedin.flashback.factory.SceneFactory
import com.linkedin.flashback.matchrules.CompositeMatchRule
import com.linkedin.flashback.matchrules.MatchBody
import com.linkedin.flashback.matchrules.MatchMethod
import com.linkedin.flashback.matchrules.MatchRuleUtils
import com.linkedin.flashback.scene.SceneConfiguration
import com.linkedin.flashback.scene.SceneMode
import com.linkedin.flashback.smartproxy.FlashbackRunner
import com.microsoft.azure.storage.blob.ContainerURL
import com.microsoft.azure.storage.blob.ETag
import com.microsoft.azure.storage.blob.ListContainersOptions
import com.microsoft.azure.storage.blob.PipelineOptions
import com.microsoft.azure.storage.blob.ServiceURL
import com.microsoft.azure.storage.blob.SharedKeyCredentials
import com.microsoft.azure.storage.blob.StorageURL
import com.microsoft.azure.storage.blob.models.Container
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
    static final boolean enableDebugging = true

    static final boolean enableRecordings = false

    static final String containerPrefix = "javatestcontainer"

    static final String blobPrefix = "javablob"

    static final SceneMode sceneMode = SceneMode.RECORD

    static final String sceneDir =
            "C:\\Users\\frley\\Documents\\azure-storage-java-async\\azure-storage\\src\\test\\resources\\recordings\\"

    @Shared
    FlashbackRunner flashbackRunner = null

    @Shared
    CompositeMatchRule matchRule = constructMatchRule()

    /*
    The values below are used to create data-driven tests for access conditions.
     */
    // TODO: Change from joda time
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

    static CompositeMatchRule constructMatchRule() {
        CompositeMatchRule matchRule = new CompositeMatchRule()
        matchRule.addRule(new MatchBody())
        matchRule.addRule(new MatchMethod())

        /*
         We can ignore the access condition headers because they will be
         distinguished by the container name when the test unrolls.
         */
        HashSet<String> blacklistHeaders = new HashSet<>()
        blacklistHeaders.add("Authorization")
        blacklistHeaders.add("x-ms-date")
        blacklistHeaders.add("x-ms-client-request-id")
        blacklistHeaders.add("If-Modified-Since")
        blacklistHeaders.add("If-Unmodified-Since")
        blacklistHeaders.add("If-Match")
        blacklistHeaders.add("If-None-Match")
        matchRule.addRule(MatchRuleUtils.matchHeadersWithBlacklist(blacklistHeaders))

        HashSet<String> blacklistQuery = new HashSet<>()
        blacklistQuery.add("sig")

        matchRule.addRule(MatchRuleUtils.matchUriWithQueryBlacklist(blacklistQuery))

        return matchRule
    }

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
                                       int entityNo){
        String suffix = ""
        if (enableRecordings) {
            suffix += iterationNo
            suffix += entityNo
        }
        else {
            suffix = System.currentTimeMillis()
        }
        return prefix + getTestName(specificationContext) + suffix
    }

    static int updateIterationNo(ISpecificationContext specificationContext, int iterationNo){
        if (specificationContext.currentIteration.estimatedNumIterations > 1) {
            return iterationNo + 1
        }
        else {
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
        /*try (Scanner scanner = new Scanner(new FileInputStream(sceneDir + sceneName))) {
            while(scanner.hasNext("\"Authorization\" :")) {

            }
        } catch (IOException e) {

        }*/
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
        } else if (enableRecordings) {
            HttpClientConfiguration configuration = new HttpClientConfiguration(
                    new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 1234)))
            po.client = HttpClient.createDefault(configuration)
        }

        HttpPipeline pipeline = StorageURL.createPipeline(creds, po)

        return new ServiceURL(new URL("http://" + creds.getAccountName() + ".blob.core.windows.net"), pipeline)

    }

    static void cleanupContainers() throws MalformedURLException {
        // We don't need to clean up containers if we are playing back
        if (!(enableRecordings && sceneMode.equals(SceneMode.PLAYBACK))) {
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
    }

    static ByteBuffer getRandomData(long size) {
        Random rand = new Random(getRandomSeed())
        byte[] data = new byte[size]
        rand.nextBytes(data)
        return ByteBuffer.wrap(data)
    }

    static long getRandomSeed() {
        if (enableRecordings) {
            return 5
        }
        return System.currentTimeMillis()
    }

    def setupSpec() {
        if (enableRecordings) {
            try {
                /*
                 Set the scene to dummy scene in case we are in playback mode, which expecs the file to exist. This will
                 be overwritten in the feature setup.
                 */
                SceneConfiguration sceneConfig =
                        new SceneConfiguration(sceneDir, sceneMode, "dummyscene.txt")
                FlashbackRunner runner = new FlashbackRunner.Builder().host("localhost").port(1234)
                        .mode(sceneMode)
                        .sceneAccessLayer(new SceneAccessLayer(SceneFactory.create(sceneConfig), constructMatchRule()))
                        .build()
                runner.start()
                flashbackRunner = runner
            }
            catch (IOException | InterruptedException e) {
                throw new Error(e)
            }
        }
    }

    def cleanupSpec() {
        if (enableRecordings) {
            flashbackRunner.close()
        }
        cleanupContainers()
    }

    def setup() {
        // Set up feature recording.
        if (enableRecordings) {
            flashbackRunner.setScene(SceneFactory.create(
                    new SceneConfiguration(sceneDir, sceneMode, specificationContext.getCurrentIteration().name)))
            flashbackRunner.setMatchRule(matchRule)
        }
        cu = primaryServiceURL.createContainerURL(generateContainerName())
        cu.create(null, null).blockingGet()
    }

    def cleanup() {
        // TODO: Scrub auth header here?
        iterationNo = updateIterationNo(specificationContext, iterationNo)
    }
}
