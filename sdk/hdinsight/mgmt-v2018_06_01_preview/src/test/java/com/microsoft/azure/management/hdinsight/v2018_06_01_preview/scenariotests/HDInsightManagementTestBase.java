/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.hdinsight.v2018_06_01_preview.scenariotests;

import com.microsoft.azure.arm.core.TestBase;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.implementation.HDInsightManager;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.utilities.HDInsightTestResourceManager;
import com.microsoft.azure.management.keyvault.Permissions;
import com.microsoft.azure.management.keyvault.implementation.VaultInner;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.rest.RestClient;
import org.junit.BeforeClass;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class HDInsightManagementTestBase extends TestBase {
    private static final String FAKE_CONFIG_NAME = "FakeTestConfig";
    private static final String REAL_CONFIG_NAME = "RealTestConfig";
    private static ResourceBundle TestConfig;
    private static ResourceBundle FakeTestConfig;

    public static String REGION;
    public static String CLUSTER_USERNAME;
    public static String CLUSTER_PASSWORD;
    public static String SSH_USERNAME;
    public static String SSH_PASSWORD;
    public static String ADLS_HOME_MOUNTPOINT;
    public static String CERT_PASSWORD;
    public static String CERT_CONTENT;
    public static String WORKSPACE_ID;
    public static String RESOURCE_GROUP_NAME_PREFIX;
    public static String STORAGE_ACCOUNT_NAME_PREFIX;
    public static String MANAGED_IDENTITY_NAME_PREFIX;
    public static String VAULT_NAME_PREFIX;
    public static String STORAGE_BLOB_SERVICE_ENDPOINT_SUFFIX;
    public static String STORAGE_ADLS_FILE_SYSTEM_ENDPOINT_SUFFIX;
    public static String CLIENT_OID;
    public static String HDI_ADLS_ACCOUNT_NAME;
    public static String HDI_ADLS_CLIENT_ID;

    /**
     * Entry point to Azure HDInsight resource management.
     */
    HDInsightManager hdInsightManager;

    /**
     * Entry point to manage other Azure resource that will be used during the test execution.
     */
    HDInsightTestResourceManager resourceManager;

    /**
     * region
     */
    final String region = REGION;

    /**
     * Tenant id
     */
    String tenantId;

    /**
     * Resource group
     */
    ResourceGroup resourceGroup;

    /**
     * Storage account
     */
    StorageAccount storageAccount;

    /**
     * Key vault
     */
    VaultInner vault = null;

    @BeforeClass
    public static void classSetup() throws IOException, IllegalAccessException {
        TestBase.beforeClass();
        FakeTestConfig = ResourceBundle.getBundle(FAKE_CONFIG_NAME);
        if (isRecordMode()) {
            try {
                TestConfig = ResourceBundle.getBundle(REAL_CONFIG_NAME);
            } catch (MissingResourceException ex) {
                System.out.println("Need a " + REAL_CONFIG_NAME + " file to run all test cases in Record mode.");
                System.out.println("Use " + FAKE_CONFIG_NAME + " file instead. Some test cases will be skipped.");
                TestConfig = FakeTestConfig;
            }
        } else {
            TestConfig = FakeTestConfig;
        }

        Field[] fields = HDInsightManagementTestBase.class.getDeclaredFields();
        for (String key : TestConfig.keySet()) {
            for (Field field : fields) {
                if (field.getName().equals(key)) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        field.set(null, TestConfig.getString(key));
                    }

                    break;
                }
            }
        }
    }

    @Override
    public void beforeTest() throws IOException {
        super.beforeTest();
        this.setupScrubber();
        this.createResources();
    }

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        tenantId = domain;
        resourceManager = new HDInsightTestResourceManager(restClient, defaultSubscription, domain);
        hdInsightManager = HDInsightManager.authenticate(restClient, defaultSubscription);
    }

    protected void createResources() {
        resourceGroup = resourceManager.createResourceGroup();
        storageAccount = resourceManager.createStorageAccount(resourceGroup.name());
    }

    @Override
    protected void cleanUpResources() {
        if (vault != null) {
            resourceManager.deleteVault(resourceGroup.name(), vault.name());
            if (vault.properties().enableSoftDelete()) {
                resourceManager.purgeDeletedVault(vault.name(), region);
            }
        }

        this.resourceManager.deleteResourceGroup(resourceGroup.name());
    }

    /**
     * Some test cases requires some prerequisites(client id, account name, etc.) to execute.
     * If those info are not provided, we call this record mode as partial and those test cases will be skipped.
     */
    boolean isPartialRecordMode() {
        return isRecordMode() && TestConfig == FakeTestConfig;
    }

    /**
     * Set vault permissions to some Azure resource. For example, user, group, service principal and MSI
     */
    VaultInner setPermissions(VaultInner vault, UUID objectId, Permissions permissions) {
        return resourceManager.setVaultPermissions(vault, resourceGroup.name(), objectId.toString(), permissions);
    }

    /***
     * Add text replacement rules for sensitive data.
     * Sensitive data will be replaced with some fake data in session records.
     */
    private void setupScrubber() {
        if (isPlaybackMode()) {
            return;
        }

        List<String> constantsToScrub = Arrays.asList(
            "HDI_ADLS_ACCOUNT_NAME",
            "HDI_ADLS_CLIENT_ID",
            "CLIENT_OID"
        );
        for (String key : constantsToScrub) {
            if (TestConfig.containsKey(key) && FakeTestConfig.containsKey(key)) {
                addTextReplacementRule(TestConfig.getString(key), FakeTestConfig.getString(key));
            }
        }
    }
}
