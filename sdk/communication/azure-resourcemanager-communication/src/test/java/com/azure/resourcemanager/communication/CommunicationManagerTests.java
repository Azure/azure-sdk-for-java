package com.azure.resourcemanager.communication;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.communication.models.EmailServiceResource;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class CommunicationManagerTests extends TestBase {
    private static final Random RANDOM = new Random();
    private String resourceGroupName = "rg" + randomPadding();
    private CommunicationManager communicationManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        communicationManager = CommunicationManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        resourceManager = ResourceManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

        // use AZURE_RESOURCE_GROUP_NAME if run in LIVE CI
        String testResourceGroup = Configuration.getGlobalConfiguration().get("AZURE_RESOURCE_GROUP_NAME");
        testEnv = !CoreUtils.isNullOrEmpty(testResourceGroup);
        if (testEnv) {
            resourceGroupName = testResourceGroup;
        } else {
            resourceManager.resourceGroups()
                .define(resourceGroupName)
                .withRegion(Region.US_WEST2)
                .create();
        }
    }

    @Override
    protected void afterTest() {
        if (!testEnv) {
            resourceManager.resourceGroups().beginDeleteByName(resourceGroupName);
        }
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testCreateEmailServiceResource() {
        EmailServiceResource resource = null;
        try {
            String resourceName = "resource" + randomPadding();
            // @embedmeStart
            resource = communicationManager.emailServices()
                .define(resourceName)
                .withRegion("global")
                .withExistingResourceGroup(resourceGroupName)
                .withDataLocation("United States")
                .create();
            // @embedmeEnd
            resource.refresh();
            Assertions.assertEquals(resource.name(), resourceName);
            Assertions.assertEquals(resource.name(), communicationManager.emailServices().getById(resource.id()).name());
            Assertions.assertTrue(communicationManager.emailServices().list().stream().count() > 0);
        } finally {
            if (resource != null) {
                communicationManager.emailServices().deleteById(resource.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }

}
