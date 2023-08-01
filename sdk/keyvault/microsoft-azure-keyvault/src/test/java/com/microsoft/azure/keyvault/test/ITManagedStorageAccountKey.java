// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.UserTokenCredentials;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;
import com.microsoft.azure.keyvault.models.DeletedStorageBundle;
import com.microsoft.azure.keyvault.models.SasDefinitionAttributes;
import com.microsoft.azure.keyvault.models.SasDefinitionBundle;
import com.microsoft.azure.keyvault.models.SasTokenType;
import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.azure.keyvault.models.StorageAccountAttributes;
import com.microsoft.azure.keyvault.models.StorageAccountItem;
import com.microsoft.azure.keyvault.models.StorageBundle;
import com.microsoft.azure.management.graphrbac.RoleDefinition;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.keyvault.implementation.KeyVaultManager;
import com.microsoft.azure.management.resources.core.InterceptorManager;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceManagerThrottlingInterceptor;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.SharedAccessAccountPermissions;
import com.microsoft.azure.storage.SharedAccessAccountPolicy;
import com.microsoft.azure.storage.SharedAccessAccountResourceType;
import com.microsoft.azure.storage.SharedAccessAccountService;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.rest.LogLevel;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.interceptors.LoggingInterceptor;

import okhttp3.Interceptor;
import com.microsoft.azure.keyvault.test.resources.MockUserTokenCredentials;

public class ITManagedStorageAccountKey {

    private static TestBase.TestMode testMode = null;

    private KeyVaultManager keyVaultManager;
    private StorageManager storageManager;
    private GraphRbacManager graphRbacManager;
    private static KeyVaultClient keyVaultClient;

    private static final Region VAULT_REGION = Region.US_WEST_CENTRAL;

    // This is the default client ID that works across all Azure services - leave in
    // for testing.
    private static final String CLIENT_ID = "04b07795-8ddb-461a-bbee-02f9e1bf7b46";

    private String msakUser;
    private String msakPassword;
    private String resourceGroup;
    private String tenantId;
    private String subscriptionId;
    private String msakUserOid;
    private RoleDefinition keyVaultRole;

    private InterceptorManager interceptorManager = null;

    private static final String ZERO_SUBSCRIPTION = "00000000-0000-0000-0000-000000000000";
    private static final String ZERO_TENANT = "00000000-0000-0000-0000-000000000000";
    private static final String ZERO_OID = "00000000-0000-0000-0000-000000000000";
    private static final String ZERO_RESOURCE_GROUP = "rg-0";
    private static final String PLAYBACK_URI_BASE = "http://localhost:";
    private static String playbackUri = null;

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void beforeClass() throws IOException {
        initTestMode();
        initPlaybackUri();
    }

    @Before
    public void beforeTests() throws Exception {
        printThreadInfo(String.format("%s: %s", "beforeTest", testName.getMethodName()));

        interceptorManager = InterceptorManager.create(testName.getMethodName(), testMode);

        ServiceClientCredentials keyVaultCredentials = createTestCredentials();

        // Due to the nature of the services, we have to use User Authentication for testing
        // You must use a user with 2FA disabled for this to work.


        if (isRecordMode()) {
            // This needs to be set for playback.
            resourceGroup = System.getenv("msak.resourceGroup");

            // These need to be set for recording.
            msakUser = System.getenv("msak.user");
            msakPassword = System.getenv("msak.password");
            msakUserOid = System.getenv("msak.useroid");
            tenantId = System.getenv("arm.tenantid");
            subscriptionId = System.getenv("arm.subscriptionId");

            UserTokenCredentials credentials = new UserTokenCredentials(CLIENT_ID, tenantId, msakUser, msakPassword,
                    AzureEnvironment.AZURE);
            Interceptor interceptor = interceptorManager.initInterceptor();
            Interceptor loggingInterceptor = new LoggingInterceptor(LogLevel.BODY_AND_HEADERS);

            RestClient keyVaultRestClient = new RestClient.Builder().withBaseUrl("https://{vaultBaseUrl}")
                    .withSerializerAdapter(new AzureJacksonAdapter())
                    .withResponseBuilderFactory(new AzureResponseBuilder.Factory()).withCredentials(keyVaultCredentials)
                    .withLogLevel(LogLevel.NONE).withNetworkInterceptor(loggingInterceptor)
                    .withNetworkInterceptor(interceptor).withInterceptor(new ResourceManagerThrottlingInterceptor())
                    .build();

            keyVaultClient = new KeyVaultClient(keyVaultRestClient);
            credentials.withDefaultSubscriptionId(subscriptionId);

            RestClient restClient = new RestClient.Builder().withBaseUrl("https://management.azure.com")
                    .withSerializerAdapter(new AzureJacksonAdapter())
                    .withResponseBuilderFactory(new AzureResponseBuilder.Factory()).withCredentials(credentials)
                    .withLogLevel(LogLevel.NONE).withReadTimeout(3, TimeUnit.MINUTES)
                    .withNetworkInterceptor(loggingInterceptor).withNetworkInterceptor(interceptor)
                    .withInterceptor(new ResourceManagerThrottlingInterceptor()).build();

            String defaultSubscription = credentials.defaultSubscriptionId();
            interceptorManager.addTextReplacementRule(defaultSubscription, ZERO_SUBSCRIPTION);
            interceptorManager.addTextReplacementRule(credentials.domain(), ZERO_TENANT);
            interceptorManager.addTextReplacementRule("https://management.azure.com/", playbackUri + "/");
            interceptorManager.addTextReplacementRule("https://graph.windows.net/", playbackUri + "/");
            interceptorManager.addTextReplacementRule("vault.azure.net/", "vault.azure.net");
            interceptorManager.addTextReplacementRule(msakUserOid, ZERO_OID);
            interceptorManager.addTextReplacementRule(resourceGroup, ZERO_RESOURCE_GROUP);
            initializeClients(restClient, defaultSubscription, credentials.domain());
        } else {
            resourceGroup = ZERO_RESOURCE_GROUP;
            msakUserOid = ZERO_OID;
            UserTokenCredentials credentials = new MockUserTokenCredentials();
            keyVaultClient = new KeyVaultClient(buildPlaybackRestClient(keyVaultCredentials, playbackUri));
            RestClient restClient = buildPlaybackRestClient(credentials, playbackUri);

            initializeClients(restClient, ZERO_SUBSCRIPTION, ZERO_TENANT);
        }

        keyVaultRole = getKeyVaultRole();

    }

    @Test
    public void testCrudOperationsForManagedStorageAccountKey() {

        String msakUUID = null;
        String storageAccountName = null;
        String vaultName = null;
        String fileName = System.getProperty("user.dir")
                + "/src/test/java/com/microsoft/azure/keyvault/test/crudNames.json";

        if (isRecordMode()) {
            msakUUID = UUID.randomUUID().toString();
            storageAccountName = SdkContext.randomResourceName("sa", 15);
            vaultName = SdkContext.randomResourceName("vault", 15);
            JsonObject obj = new JsonObject();
            obj.addProperty("roleDefUUID", msakUUID);
            obj.addProperty("storageAccountName", storageAccountName);
            obj.addProperty("vaultName", vaultName);
            try {
                BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileName));
                fileWriter.write(obj.toString());
                fileWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            try {
                Gson gson = new Gson();
                BufferedReader br = new BufferedReader(new FileReader(fileName));
                JsonObject obj = gson.fromJson(br, JsonObject.class);
                msakUUID = obj.get("roleDefUUID").getAsString();
                storageAccountName = obj.get("storageAccountName").getAsString();
                vaultName = obj.get("vaultName").getAsString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        StorageAccount storageAccount = initStorageAccount(storageAccountName, msakUUID);

        Vault vault = initVault(vaultName);

        String vaultUri = vault.vaultUri();

        String activeKeyName = "key1";
        String regenerationPeriod = "P30D";
        StorageAccountAttributes attributes = new StorageAccountAttributes().withEnabled(true);
        keyVaultClient.setStorageAccount(vaultUri, storageAccount.name(), storageAccount.id(), activeKeyName, true,
                regenerationPeriod, attributes, null);
        List<StorageAccountItem> msaList = keyVaultClient.getStorageAccounts(vaultUri);
        Assert.assertEquals(1, msaList.size());

        StorageBundle bundle = keyVaultClient.getStorageAccount(vaultUri, storageAccount.name());
        Assert.assertTrue(bundle.id().contains(storageAccount.name()));
        Assert.assertEquals(activeKeyName, bundle.activeKeyName());
        Assert.assertEquals(regenerationPeriod, bundle.regenerationPeriod());
        Assert.assertTrue(bundle.autoRegenerateKey());

        String newActiveKeyName = "key2";
        String newRegenerationPeriod = "P60D";
        keyVaultClient.updateStorageAccount(vaultUri, storageAccount.name(), newActiveKeyName, false,
                newRegenerationPeriod, null, null);
        StorageBundle updatedBundle = keyVaultClient.getStorageAccount(vaultUri, storageAccount.name());
        Assert.assertTrue(updatedBundle.id().contains(storageAccount.name()));
        Assert.assertEquals(newActiveKeyName, updatedBundle.activeKeyName());
        Assert.assertEquals(newRegenerationPeriod, updatedBundle.regenerationPeriod());
        Assert.assertFalse(updatedBundle.autoRegenerateKey());

        DeletedStorageBundle deletedBundle = keyVaultClient.deleteStorageAccount(vaultUri, storageAccount.name());
        Assert.assertTrue(deletedBundle.id().contains(storageAccount.name()));
    }

    @Test
    public void testSetAndGetSasDefinitionForManagedStorageAccountKey()
            throws ParseException, URISyntaxException, StorageException, InvalidKeyException, IOException {

        String sasUUID = null;
        String storageAccountName = null;
        String vaultName = null;
        String fileName = System.getProperty("user.dir")
                + "/src/test/java/com/microsoft/azure/keyvault/test/sasNames.json";

        // Write names to file to save it
        if (isRecordMode()) {
            sasUUID = UUID.randomUUID().toString();
            storageAccountName = SdkContext.randomResourceName("sa", 15);
            vaultName = SdkContext.randomResourceName("vault", 15);
            JsonObject obj = new JsonObject();
            obj.addProperty("roleDefUUID", sasUUID);
            obj.addProperty("storageAccountName", storageAccountName);
            obj.addProperty("vaultName", vaultName);

            try {
                BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileName));
                fileWriter.write(obj.toString());
                fileWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

            try {
                Gson gson = new Gson();
                BufferedReader br = new BufferedReader(new FileReader(fileName));
                JsonObject obj = gson.fromJson(br, JsonObject.class);
                sasUUID = obj.get("roleDefUUID").getAsString();
                storageAccountName = obj.get("storageAccountName").getAsString();
                vaultName = obj.get("vaultName").getAsString();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        StorageAccount storageAccount = initStorageAccount(storageAccountName, sasUUID);
        Vault vault = initVault(vaultName);
        String vaultUri = vault.vaultUri();

        final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        String activeKeyName = "key1";
        String regenerationPeriod = "P30D";
        StorageAccountAttributes storageAttributes = new StorageAccountAttributes().withEnabled(true);
        keyVaultClient.setStorageAccount(vaultUri, storageAccount.name(), storageAccount.id(), activeKeyName, true,
                regenerationPeriod, storageAttributes, null);

        StorageCredentials sas = new StorageCredentialsAccountAndKey(storageAccount.name(), "00000001");
        // Note that the key passed in is just a dummy key such that we can generate the
        // correct signature for the template.
        CloudStorageAccount account = new CloudStorageAccount(sas);

        // Create a new policy
        SharedAccessAccountPolicy sharedAccessAccountPolicy = new SharedAccessAccountPolicy();
        sharedAccessAccountPolicy.setServices(EnumSet.of(SharedAccessAccountService.BLOB,
                SharedAccessAccountService.FILE, SharedAccessAccountService.QUEUE, SharedAccessAccountService.TABLE));
        sharedAccessAccountPolicy
                .setPermissions(EnumSet.of(SharedAccessAccountPermissions.ADD, SharedAccessAccountPermissions.CREATE,
                        SharedAccessAccountPermissions.DELETE, SharedAccessAccountPermissions.LIST,
                        SharedAccessAccountPermissions.PROCESS_MESSAGES, SharedAccessAccountPermissions.READ,
                        SharedAccessAccountPermissions.UPDATE, SharedAccessAccountPermissions.WRITE));
        sharedAccessAccountPolicy.setResourceTypes(EnumSet.of(SharedAccessAccountResourceType.CONTAINER,
                SharedAccessAccountResourceType.OBJECT, SharedAccessAccountResourceType.SERVICE)); // all resources
                                                                                                   // service,
                                                                                                   // container, object
        sharedAccessAccountPolicy.setSharedAccessExpiryTime(formatter.parse("01-01-2020"));

        // Generate a signature based off of the policy and account.
        String sasSignature = account.generateSharedAccessSignature(sharedAccessAccountPolicy);

        // Currently the key vault service cannot process the sr signature, so this
        // needs to be manually removed.
        sasSignature = sasSignature.substring(0, sasSignature.lastIndexOf("&"));

        String sasDefinitionName = "acctall";
        // Use the created template to create a sas definition in the vault.
        SasDefinitionAttributes attributes = new SasDefinitionAttributes().withEnabled(true);
        SasDefinitionBundle sasDefinition = keyVaultClient.setSasDefinition(vaultUri, storageAccount.name(),
                sasDefinitionName, sasSignature, SasTokenType.ACCOUNT, "PT2H", attributes, null);

        // Grab the secretName from the full secretId (the trailing characters after the
        // last slash)
        String sasSecretId = sasDefinition.secretId();
        String secretName = sasSecretId.substring(sasSecretId.lastIndexOf("/")).substring(1);
        SecretBundle acctSasToken = keyVaultClient.getSecret(vaultUri, secretName);
        Assert.assertNotNull(acctSasToken);

        SasDefinitionBundle retrievedSasDefinition = keyVaultClient.getSasDefinition(vaultUri, storageAccount.name(),
                sasDefinitionName);
        Assert.assertEquals(sasDefinition.id(), retrievedSasDefinition.id());
        Assert.assertEquals(sasDefinition.secretId(), retrievedSasDefinition.secretId());
        Assert.assertEquals(sasDefinition.validityPeriod(), retrievedSasDefinition.validityPeriod());
    }

    @After
    public void afterMethod() throws IOException {
        interceptorManager.finalizeInterceptor();
    }

    // Creates a new storage account for use with this account
    private StorageAccount initStorageAccount(final String storageAccountName, final String roleAccountUUID) {
        StorageAccount storageAccount = storageManager.storageAccounts().define(storageAccountName)
                .withRegion(VAULT_REGION).withExistingResourceGroup(resourceGroup).create();

        graphRbacManager.roleAssignments().define(roleAccountUUID).forObjectId("93c27d83-f79b-4cb2-8dd4-4aa716542e74")
                .withRoleDefinition(keyVaultRole.id()).withScope(storageAccount.id()).create();

        return storageAccount;
    }

    private Vault initVault(final String vaultName) {
        Vault vault = keyVaultManager.vaults().define(vaultName).withRegion(VAULT_REGION)
                .withExistingResourceGroup(resourceGroup).defineAccessPolicy()
                .forObjectId(msakUserOid).allowSecretAllPermissions()
                .allowStorageAllPermissions().attach().withDeploymentDisabled().create();
        return vault;
    }

    private void initializeClients(RestClient restClient, String defaultSubscription, String domain) {

        keyVaultManager = KeyVaultManager.authenticate(restClient, domain, defaultSubscription);

        storageManager = StorageManager.authenticate(restClient, defaultSubscription);

        graphRbacManager = GraphRbacManager.authenticate(restClient, domain);

    }

    // User Auth flow for acquiring token
    private AuthenticationResult getAccessToken(String authorization, String resource) throws Exception {
        AuthenticationResult result = null;
        ExecutorService service = null;
        try {
            service = Executors.newFixedThreadPool(1);
            AuthenticationContext context = new AuthenticationContext(authorization, false, service);
            Future<AuthenticationResult> future = null;
            future = context.acquireToken(resource, CLIENT_ID, msakUser, msakPassword, null);
            result = future.get();
        } finally {
            service.shutdown();
        }

        if (result == null) {
            throw new RuntimeException("authentication result was null");
        }
        return result;
    }

    // Creates serviceClientCredentials based on the credentials passed in.
    private ServiceClientCredentials createTestCredentials() {
        return new KeyVaultCredentials() {

            @Override
            public String doAuthenticate(String authorization, String resource, String scope) {
                try {

                    if (isRecordMode()) {
                        AuthenticationResult authResult = getAccessToken(authorization, resource);
                        return authResult.getAccessToken();
                    } else {
                        return "";
                    }

                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }

    private static void initPlaybackUri() throws IOException {
        if (isPlaybackMode()) {
            Properties mavenProps = new Properties();
            InputStream in = TestBase.class.getResourceAsStream("/maven.properties");
            if (in == null) {
                throw new IOException(
                        "The file \"maven.properties\" has not been generated yet. Please execute \"mvn compile\" to generate the file.");
            }
            mavenProps.load(in);
            String port = mavenProps.getProperty("playbackServerPort");
            // 11080 and 11081 needs to be in sync with values in jetty.xml file
            playbackUri = PLAYBACK_URI_BASE + port;
        } else {
            playbackUri = PLAYBACK_URI_BASE + "1234";
        }
    }

    private static void initTestMode() throws IOException {
        String azureTestMode = System.getenv("AZURE_TEST_MODE");
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

    public static boolean isPlaybackMode() {
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

    public static boolean isRecordMode() {
        return !isPlaybackMode();
    }

    private static void printThreadInfo(String what) {
        long id = Thread.currentThread().getId();
        String name = Thread.currentThread().getName();
        System.out.println(String.format("\n***\n*** [%s:%s] - %s\n***\n", name, id, what));
    }

    // find the role definition for "Storage Account Key Operator Service Role"
    private RoleDefinition getKeyVaultRole() {
        RoleDefinition keyVaultRole = null;
        PagedList<RoleDefinition> roleDefinitions = graphRbacManager.roleDefinitions().listByScope("\\");
        Iterator<RoleDefinition> roleDefs = roleDefinitions.iterator();
        while (roleDefs.hasNext()) {
            RoleDefinition definition = roleDefs.next();
            if (definition.roleName().equals("Storage Account Key Operator Service Role")) {
                keyVaultRole = definition;
                break;
            }
        }
        return keyVaultRole;
    }

    protected RestClient buildPlaybackRestClient(ServiceClientCredentials credentials, String baseUrl)
            throws IOException {
        return new RestClient.Builder().withBaseUrl(baseUrl)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory()).withCredentials(credentials)
                .withLogLevel(LogLevel.NONE).withNetworkInterceptor(new LoggingInterceptor(LogLevel.BODY_AND_HEADERS))
                .withNetworkInterceptor(interceptorManager.initInterceptor())
                .withInterceptor(new ResourceManagerThrottlingInterceptor()).build();
    }
}
