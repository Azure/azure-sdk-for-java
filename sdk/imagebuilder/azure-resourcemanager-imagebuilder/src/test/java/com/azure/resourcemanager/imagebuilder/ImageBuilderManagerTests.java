// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.imagebuilder;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.imagebuilder.models.ImageTemplate;
import com.azure.resourcemanager.imagebuilder.models.ImageTemplateIdentity;
import com.azure.resourcemanager.imagebuilder.models.ImageTemplateManagedImageDistributor;
import com.azure.resourcemanager.imagebuilder.models.ImageTemplatePlatformImageSource;
import com.azure.resourcemanager.imagebuilder.models.ImageTemplateVmProfile;
import com.azure.resourcemanager.imagebuilder.models.ResourceIdentityType;
import com.azure.resourcemanager.imagebuilder.models.UserAssignedIdentity;
import com.azure.resourcemanager.msi.MsiManager;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ImageBuilderManagerTests extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_EAST;
    private String resourceGroupName = "rg" + randomPadding();
    private ImageBuilderManager imageBuilderManager = null;
    private MsiManager msiManager = null;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        imageBuilderManager = ImageBuilderManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        msiManager = MsiManager
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
                .withRegion(REGION)
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
    @LiveOnly
    public void testCreateImageTemplate() {
        ImageTemplate imageTemplate = null;
        Identity identity = null;
        try {
            String randomPadding = randomPadding();
            String templateName = "template" + randomPadding;
            String imageName = "image" + randomPadding;
            String identityName = "identity" + randomPadding;
            String imageId = resourceManager.resourceGroups().getByName(resourceGroupName).id() + "/providers/Microsoft.Compute/images/" + imageName;

            identity = msiManager.identities()
                .define(identityName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR)
                .create();
            Map<String, UserAssignedIdentity> userAssignedIdentities = new HashMap<>();
            userAssignedIdentities.put(identity.id(), new UserAssignedIdentity());

            imageTemplate = imageBuilderManager.virtualMachineImageTemplates()
                .define(templateName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withIdentity(
                    new ImageTemplateIdentity()
                        .withType(ResourceIdentityType.USER_ASSIGNED)
                        .withUserAssignedIdentities(userAssignedIdentities))
                .withDistribute(Arrays.asList(
                    new ImageTemplateManagedImageDistributor()
                        .withImageId(imageId)
                        .withLocation(REGION.name())
                        .withRunOutputName("runOutputManagedImage")
                    )
                )
                .withVmProfile(new ImageTemplateVmProfile().withVmSize("Standard_DS1_v2").withOsDiskSizeGB(32))
                .withSource(
                    new ImageTemplatePlatformImageSource()
                        .withPublisher("canonical")
                        .withOffer("0001-com-ubuntu-server-focal")
                        .withSku("20_04-lts-gen2")
                        .withVersion("latest"))
                .withBuildTimeoutInMinutes(0)
                .create();
            // @embedEnd
            imageTemplate.refresh();
            Assertions.assertEquals(imageTemplate.name(), templateName);
            Assertions.assertEquals(imageTemplate.name(), imageBuilderManager.virtualMachineImageTemplates().getById(imageTemplate.id()).name());
            Assertions.assertTrue(imageBuilderManager.virtualMachineImageTemplates().listByResourceGroup(resourceGroupName).stream().findAny().isPresent());
        } finally {
            if (imageTemplate != null) {
                imageBuilderManager.virtualMachineImageTemplates().deleteById(imageTemplate.id());
            }
            if (identity != null) {
                msiManager.identities().deleteById(identity.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
