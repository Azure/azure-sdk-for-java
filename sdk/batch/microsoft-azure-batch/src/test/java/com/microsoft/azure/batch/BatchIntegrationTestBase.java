// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch;

import com.microsoft.azure.batch.protocol.models.*;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.azure.core.util.Configuration;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.batch.auth.BatchApplicationTokenCredentials;
import com.microsoft.azure.batch.auth.BatchCredentials;
import com.microsoft.azure.batch.auth.BatchSharedKeyCredentials;
import com.microsoft.azure.management.resources.core.InterceptorManager;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.core.TestBase.TestMode;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceManagerThrottlingInterceptor;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.rest.LogLevel;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.RestException;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.interceptors.LoggingInterceptor;
import com.microsoft.rest.protocol.ResponseBuilder;
import com.microsoft.rest.protocol.SerializerAdapter;
import okhttp3.OkHttpClient;
import org.junit.Before;
import org.junit.After;
import org.junit.Rule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.rules.TestName;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * The base for batch dataplane tests.
 */
public class BatchIntegrationTestBase {
    public static final String RECORD_MODE = "RECORD";
    static BatchClient batchClient;
    static BatchClient alternativeBatchClient;
    static final int MAX_LEN_ID = 64;
    static Logger logger;

    public enum AuthMode {
        AAD, SharedKey
    }


    private static TestBase.TestMode testMode = null;
    private static final String PLAYBACK_URI_BASE = "http://localhost:";
    protected static String playbackUri = null;
    protected static String alternativePlaybackUri = null;


    private static void initTestMode() throws IOException {
        String azureTestMode =
            Configuration.getGlobalConfiguration().get("AZURE_TEST_MODE");
        if (azureTestMode != null) {
            if (azureTestMode.equalsIgnoreCase("Record")) {
                testMode = TestBase.TestMode.RECORD;
            } else if (azureTestMode.equalsIgnoreCase("Playback")) {
                testMode = TestBase.TestMode.PLAYBACK;
            } else {
                throw new IOException("Unknown AZURE_TEST_MODE: " + azureTestMode);
            }
        } else {
            testMode = TestBase.TestMode.PLAYBACK;
        }
    }

    private static void initPlaybackUri() throws IOException {
        if (isPlaybackMode()) {

            // 11080 and 11081 needs to be in sync with values in jetty.xml file
            playbackUri = PLAYBACK_URI_BASE + "11080";
            alternativePlaybackUri = PLAYBACK_URI_BASE + "11081";
        } else {
            playbackUri = PLAYBACK_URI_BASE + "1234";
        }
    }

    static boolean isPlaybackMode() {
        if (testMode == null) {
            try {
                initTestMode();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Can't init test mode.");
            }
        }
        return testMode == TestBase.TestMode.PLAYBACK;
    }

    static boolean isRecordMode() {
        return !isPlaybackMode();
    }

    private static void printThreadInfo(String what) {
        long id = Thread.currentThread().getId();
        String name = Thread.currentThread().getName();
        System.out.println(String.format("\n***\n*** [%s:%s] - %s\n***\n", name, id, what));
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        logger = Logger.getLogger("BatchIntegrationTestBase");
        printThreadInfo("beforeclass");
        initTestMode();
        initPlaybackUri();
    }

    @Rule
    public TestName testName = new TestName();

    protected InterceptorManager interceptorManager = null;

    static void createClient(AuthMode mode) {
        BatchCredentials credentials;

        if (mode == AuthMode.AAD) {
            credentials = getApplicationTokenCredentials();
        } else {
            credentials = getSharedKeyCredentials();
        }
        batchClient = BatchClient.open(credentials);
    }

    void createClientWithInterceptor(AuthMode mode) throws IOException {
        BatchCredentials credentials;

        interceptorManager = InterceptorManager.create(testName.getMethodName(), testMode);
        RestClient restClient;

        credentials = getCredentials(mode);

        if (isRecordMode()) {

            restClient = buildRestClient(new RestClient.Builder().withBaseUrl(credentials.baseUrl())
                    .withSerializerAdapter(new AzureJacksonAdapter())
                    .withCredentials(credentials)
                    .withResponseBuilderFactory(new ResponseBuilder.Factory() {
                        private final AzureResponseBuilder.Factory baseFactory = new AzureResponseBuilder.Factory();
                        @Override
                        public <T, E extends RestException> ResponseBuilder<T, E> newInstance(SerializerAdapter<?> serializerAdapter) {
                            return baseFactory.<T, E>newInstance(serializerAdapter).withThrowOnGet404(true);
                        }
                    })
                    .withLogLevel(LogLevel.NONE)
                    .withNetworkInterceptor(new LoggingInterceptor(LogLevel.BODY_AND_HEADERS))
                    .withNetworkInterceptor(interceptorManager.initInterceptor())
                    .withInterceptor(new ResourceManagerThrottlingInterceptor()));

            batchClient = BatchClient.open(restClient, credentials.baseUrl());
            alternativeBatchClient = batchClient;

        } else { // is Playback Mode
            System.setOut(new PrintStream(new OutputStream() {
                public void write(int b) {
                    // DO NOTHING
                }
            }));

            batchClient = BatchClient.open(buildPlaybackRestClient(credentials, playbackUri + "/"), playbackUri + "/");
            alternativeBatchClient = BatchClient.open(buildPlaybackRestClient(credentials, alternativePlaybackUri + "/"), alternativePlaybackUri + "/");

        }
    }

    private static BatchCredentials getCredentials(AuthMode mode) {
        BatchCredentials credentials;
        if (isRecordMode()) {
            if (mode == AuthMode.AAD) {
                credentials = getApplicationTokenCredentials();
            } else {
                credentials = getSharedKeyCredentials();
            }
        } else {
            credentials =  new BatchCredentials() {
                @Override
                public String baseUrl() {
                    return null;
                }

                @Override
                public void applyCredentialsFilter(OkHttpClient.Builder builder) {

                }
            };
        }
        return credentials;
    }

    private static BatchSharedKeyCredentials getSharedKeyCredentials() {
        return new BatchSharedKeyCredentials(System.getenv("AZURE_BATCH_ENDPOINT"),
                System.getenv("AZURE_BATCH_ACCOUNT"), System.getenv("AZURE_BATCH_ACCESS_KEY"));
    }

    private static BatchApplicationTokenCredentials getApplicationTokenCredentials() {
        return new BatchApplicationTokenCredentials(System.getenv("AZURE_BATCH_ENDPOINT"),
                System.getenv("CLIENT_ID"), System.getenv("APPLICATION_SECRET"), "microsoft.onmicrosoft.com", null,
                null);
    }

    @Before
    public void beforeMethod() throws Exception {
        printThreadInfo(String.format("%s: %s", "beforeTest", testName.getMethodName()));
        createClientWithInterceptor(AuthMode.AAD);
    }


    @After
    public void afterMethod() throws IOException {
        interceptorManager.finalizeInterceptor();
    }

    protected RestClient buildRestClient(RestClient.Builder builder) {
        return builder.build();
    }

    protected RestClient buildPlaybackRestClient(ServiceClientCredentials credentials, String baseUrl) throws IOException {
        return buildRestClient(new RestClient.Builder().withBaseUrl(baseUrl)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new ResponseBuilder.Factory() {
                    private final AzureResponseBuilder.Factory baseFactory = new AzureResponseBuilder.Factory();
                    @Override
                    public <T, E extends RestException> ResponseBuilder<T, E> newInstance(SerializerAdapter<?> serializerAdapter) {
                        return baseFactory.<T, E>newInstance(serializerAdapter).withThrowOnGet404(true);
                    }
                })
                .withCredentials(credentials)
                .withLogLevel(LogLevel.NONE)
                .withNetworkInterceptor(new LoggingInterceptor(LogLevel.BODY_AND_HEADERS))
                .withNetworkInterceptor(interceptorManager.initInterceptor())
                .withInterceptor(new ResourceManagerThrottlingInterceptor()));
    }


//    static CloudPool createIfNotExistPaaSPool(String poolId) throws Exception {
//        // Create a pool with 3 Small VMs
//        String poolVmSize = "Small";
//        int poolVmCount = 3;
//        String poolOsFamily = "4";
//        String poolOsVersion = "*";
//
//        // 10 minutes
//        long poolSteadyTimeoutInSeconds = 10 * 60 * 1000;
//
//        // Check if pool exists
//        if (!batchClient.poolOperations().existsPool(poolId)) {
//            // Need VNet to allow security to inject NSGs
//            AzureTokenCredentials token = new ApplicationTokenCredentials(
//                System.getenv("CLIENT_ID"),
//                System.getenv("TENANT_ID"),
//                System.getenv("APPLICATION_SECRET"),
//                AzureEnvironment.AZURE);
//            Azure azure = Azure.authenticate(token).withSubscription(System.getenv("AZURE_BATCH_SUBSCRIPTION_ID"));
//            String vnetName = "AzureBatchTestVnet";
//            String subnetName = "AzureBatchTestSubnet";
//            if (azure.networks().list().size() == 0) {
//                Network virtualNetwork = azure.networks().define(vnetName)
//                    .withRegion(System.getenv("AZURE_BATCH_REGION"))
//                    .withExistingResourceGroup(System.getenv("AZURE_BATCH_RESOURCE_GROUP"))
//                    .withAddressSpace("192.168.0.0/16")
//                    .withSubnet(subnetName, "192.168.1.0/24")
//                    .create();
//            }
//            String vNetResourceId = String.format(
//                "/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Network/virtualNetworks/%s/subnets/%s",
//                System.getenv("AZURE_BATCH_SUBSCRIPTION_ID"),
//                System.getenv("AZURE_BATCH_RESOURCE_GROUP"),
//                vnetName,
//                subnetName);
//            NetworkConfiguration networkConfiguration = new NetworkConfiguration().withSubnetId(vNetResourceId);
//            // Use PaaS VM with Windows
//            CloudServiceConfiguration configuration = new CloudServiceConfiguration();
//            configuration.withOsFamily(poolOsFamily).withOsVersion(poolOsVersion);
//
//            List<UserAccount> userList = new ArrayList<>();
//            userList.add(new UserAccount().withName("test-user").withPassword("kt#_gahr!@aGERDXA")
//                    .withElevationLevel(ElevationLevel.ADMIN));
//            PoolAddParameter addParameter = new PoolAddParameter().withId(poolId)
//                    .withTargetDedicatedNodes(poolVmCount).withVmSize(poolVmSize)
//                    .withCloudServiceConfiguration(configuration).withUserAccounts(userList)
//                    .withNetworkConfiguration(networkConfiguration);
//            batchClient.poolOperations().createPool(addParameter);
//        } else {
//            logger.log(createLogRecord(Level.INFO, String.format("The %s already exists.", poolId)));
//        }
//
//        long startTime = System.currentTimeMillis();
//        long elapsedTime = 0L;
//        boolean steady = false;
//        CloudPool pool;
//
//        // Wait for the VM to be allocated
//        while (elapsedTime < poolSteadyTimeoutInSeconds) {
//            pool = batchClient.poolOperations().getPool(poolId);
//            if (pool.allocationState() == AllocationState.STEADY) {
//                steady = true;
//                break;
//            }
//            System.out.println("wait 30 seconds for pool steady...");
//            Thread.sleep(30 * 1000);
//            elapsedTime = (new Date()).getTime() - startTime;
//        }
//
//        Assert.assertTrue("The pool did not reach a steady state in the allotted time", steady);
//
//        return batchClient.poolOperations().getPool(poolId);
//    }

    private static LogRecord createLogRecord(Level logLevel, String message) {
        return new LogRecord(Level.INFO,  message);
    }

    static NetworkConfiguration createNetworkConfiguration(){
        String vnetName = System.getenv("AZURE_VNET");
        String subnetName = System.getenv("AZURE_VNET_SUBNET");
        if(isRecordMode()) {
            AzureTokenCredentials token = new ApplicationTokenCredentials(
                System.getenv("CLIENT_ID"),
                "72f988bf-86f1-41af-91ab-2d7cd011db47",
                System.getenv("APPLICATION_SECRET"),
                AzureEnvironment.AZURE);
            Azure azure = Azure.authenticate(token).withSubscription(System.getenv("SUBSCRIPTION_ID"));
            if (azure.networks().list().size() == 0) {
                azure.networks().define(vnetName)
                    .withRegion(System.getenv("AZURE_BATCH_REGION"))
                    .withExistingResourceGroup(System.getenv("AZURE_VNET_RESOURCE_GROUP"))
                    .withAddressSpace(System.getenv("AZURE_VNET_ADDRESS_SPACE"))
                    .withSubnet(subnetName, System.getenv("AZURE_VNET_SUBNET_ADDRESS_SPACE"))
                    .create();
            }
        }
        String vNetResourceId = String.format(
            "/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Network/virtualNetworks/%s/subnets/%s",
            System.getenv("SUBSCRIPTION_ID"),
            System.getenv("AZURE_VNET_RESOURCE_GROUP"),
            vnetName,
            subnetName);
        return new NetworkConfiguration().withSubnetId(vNetResourceId);
    }

    static CloudPool createIfNotExistIaaSPool(String poolId) throws Exception {
        // Create a pool with 3 Small VMs
        String poolVmSize = "STANDARD_D1_V2";
        int poolVmCount = 1;

        // 10 minutes
        long poolSteadyTimeoutInSeconds = 10 * 60 * 1000;

        // Need VNet to allow security to inject NSGs
        NetworkConfiguration networkConfiguration = createNetworkConfiguration();
        // Check if pool exists
        if (!batchClient.poolOperations().existsPool(poolId)) {
            // Use IaaS VM with Ubuntu
            ImageReference imgRef = new ImageReference().withPublisher("Canonical").withOffer("UbuntuServer")
                    .withSku("16.04-LTS").withVersion("latest");
            VirtualMachineConfiguration configuration = new VirtualMachineConfiguration();
            configuration.withNodeAgentSKUId("batch.node.ubuntu 16.04").withImageReference(imgRef);

            List<UserAccount> userList = new ArrayList<>();
            userList.add(new UserAccount().withName("test-user").withPassword("kt#_gahr!@aGERDXA")
                    .withLinuxUserConfiguration(new LinuxUserConfiguration().withUid(5).withGid(5))
                    .withElevationLevel(ElevationLevel.ADMIN));
            PoolAddParameter addParameter = new PoolAddParameter().withId(poolId)
                    .withTargetDedicatedNodes(poolVmCount).withVmSize(poolVmSize)
                    .withVirtualMachineConfiguration(configuration).withUserAccounts(userList)
                    .withNetworkConfiguration(networkConfiguration);
            batchClient.poolOperations().createPool(addParameter);
        } else {
            logger.log(createLogRecord(Level.INFO, String.format("The %s already exists.", poolId)));
        }

        long startTime = System.currentTimeMillis();
        long elapsedTime = 0L;
        boolean steady = false;
        CloudPool pool;

        // Wait for the VM to be allocated
        while (elapsedTime < poolSteadyTimeoutInSeconds) {
            pool = batchClient.poolOperations().getPool(poolId);
            if (pool.allocationState() == AllocationState.STEADY) {
                steady = true;
                break;
            }
            System.out.println("wait 30 seconds for pool steady...");
            Thread.sleep(30 * 1000);
            elapsedTime = (new Date()).getTime() - startTime;
        }

        Assert.assertTrue("The pool did not reach a steady state in the allotted time", steady);

        return batchClient.poolOperations().getPool(poolId);
    }

    static String getStringIdWithUserNamePrefix(String name) {
        //'BatchUser' is the name used for Recording / Playing Back tests.
        // For Local testing, use your username here, to create your unique Batch resources and avoiding conflict in shared batch account.
        String userName = "BatchUser";
        StringBuilder out = new StringBuilder();
        int remainingSpace = MAX_LEN_ID - name.length();
        if (remainingSpace > 0) {
            if (userName.length() > remainingSpace) {
                out.append(userName.substring(0, remainingSpace));
            } else {
                out.append(userName);
            }
            out.append(name);
        } else {
            out.append(name.substring(0, MAX_LEN_ID));
        }
        return out.toString();
    }

    static CloudBlobContainer createBlobContainer(String storageAccountName, String storageAccountKey,
                                                  String containerName) throws URISyntaxException, StorageException {
        // Create storage credential from name and key
        StorageCredentials credentials = new StorageCredentialsAccountAndKey(storageAccountName, storageAccountKey);

        // Create storage account
        CloudStorageAccount storageAccount = new CloudStorageAccount(credentials, true);

        // Create the blob client
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

        // Get a reference to a container.
        // The container name must be lower case
        return blobClient.getContainerReference(containerName);
    }

    /**
     * Upload file to blob container and return sas key
     *
     * @param container
     *            blob container
     * @param fileName
     *            the file name of blob
     * @param filePath
     *            the local file path
     * @return SAS key for the uploaded file
     * @throws URISyntaxException
     * @throws IOException
     * @throws InvalidKeyException
     * @throws StorageException
     */
    static String uploadFileToCloud(CloudBlobContainer container, String fileName, String filePath)
            throws StorageException, URISyntaxException, IOException, InvalidKeyException {
        // Create the container if it does not exist.
        container.createIfNotExists();

        // Upload file
        CloudBlockBlob blob = container.getBlockBlobReference(fileName);
        File source = new File(filePath);
        blob.upload(new FileInputStream(source), source.length());

        // Create policy with 1 day read permission
        SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
        EnumSet<SharedAccessBlobPermissions> perEnumSet = EnumSet.of(SharedAccessBlobPermissions.READ);
        policy.setPermissions(perEnumSet);

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, 1);
        policy.setSharedAccessExpiryTime(c.getTime());

        // Create SAS key
        String sas = blob.generateSharedAccessSignature(policy, null);
        return blob.getUri() + "?" + sas;
    }

    /**
     * Wait all tasks under a specified job to be completed
     *
     * @param client
     *            batch client instance
     * @param jobId
     *            job id
     * @param expiryTimeInSeconds
     *            the waiting period
     * @return if task completed in time, return true, otherwise, return false
     * @throws BatchErrorException
     * @throws IOException
     * @throws InterruptedException
     */
    static boolean waitForTasksToComplete(BatchClient client, String jobId, int expiryTimeInSeconds)
            throws BatchErrorException, IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0L;

        while (elapsedTime < expiryTimeInSeconds * 1000) {
            List<CloudTask> taskCollection = client.taskOperations().listTasks(jobId,
                    new DetailLevel.Builder().withSelectClause("id, state").build());

            boolean allComplete = true;
            for (CloudTask task : taskCollection) {
                if (task.state() != TaskState.COMPLETED) {
                    allComplete = false;
                    break;
                }
            }

            if (allComplete) {
                // All tasks completed
                return true;
            }

            // Check again after 10 seconds
            Thread.sleep(10 * 1000);
            elapsedTime = (new Date()).getTime() - startTime;
        }

        // Timeout, return false
        return false;
    }

    static String generateContainerSasToken(CloudBlobContainer container) throws StorageException, InvalidKeyException {
        container.createIfNotExists();

        // Create policy with 1 day read permission
        SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
        EnumSet<SharedAccessBlobPermissions> perEnumSet = EnumSet.of(SharedAccessBlobPermissions.WRITE,
                SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.CREATE, SharedAccessBlobPermissions.LIST,
                SharedAccessBlobPermissions.DELETE);
        policy.setPermissions(perEnumSet);

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, 1);
        policy.setSharedAccessExpiryTime(c.getTime());

        // Create SAS key
        String sas = container.generateSharedAccessSignature(policy, null);
        return container.getUri() + "?" + sas;
    }

    static String getContentFromContainer(CloudBlobContainer container, String fileName)
            throws URISyntaxException, StorageException, IOException {
        CloudBlockBlob blockBlobReference = container.getBlockBlobReference(fileName);
        String s = blockBlobReference.downloadText();
        return s;
    }

    static void threadSleepInRecordMode(long millis) throws InterruptedException {
        // Called for long timeouts which should only happen in Record mode.
        // Speeds up the tests in Playback mode.
        if (isRecordMode()) {
            Thread.sleep(millis);
        }
    }

    String getTestMode() {
        if (testMode == null) {
            try {
                initTestMode();
            } catch (IOException e) {
                logger.warning("Unable to determine test mode. Defaulting to Playback.");
                testMode = TestMode.PLAYBACK;
            }
        }
        return testMode.toString();
    }

}
