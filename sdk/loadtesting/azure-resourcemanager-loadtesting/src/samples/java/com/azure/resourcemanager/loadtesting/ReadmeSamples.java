// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.loadtesting;

import com.azure.resourcemanager.loadtesting.models.LoadTestResource;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.loadtesting.models.CheckQuotaAvailabilityResponse;
import com.azure.resourcemanager.loadtesting.models.EncryptionProperties;
import com.azure.resourcemanager.loadtesting.models.EncryptionPropertiesIdentity;
import com.azure.resourcemanager.loadtesting.models.ManagedServiceIdentity;
import com.azure.resourcemanager.loadtesting.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.loadtesting.models.QuotaBucketRequest;
import com.azure.resourcemanager.loadtesting.models.QuotaBucketRequestPropertiesDimensions;
import com.azure.resourcemanager.loadtesting.models.QuotaResource;
import com.azure.resourcemanager.loadtesting.models.Type;
import com.azure.resourcemanager.loadtesting.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/** Samples for LoadTests CreateOrUpdate. */
public final class ReadmeSamples {

    public static void authenticateClient() {
        // BEGIN: readme-sample-authn
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();
        LoadTestManager manager = LoadTestManager
            .authenticate(credential, profile);
        // END: readme-sample-authn
    }

    public static void createLoadTestResourceBasic(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        // BEGIN: readme-sample-createloadtestresource-basic
        LoadTestResource resource = manager
            .loadTests()
            .define("sample-loadtesting-resource")
            .withRegion(Region.US_WEST2)
            .withExistingResourceGroup("sample-rg")
            .create();
        // END: readme-sample-createloadtestresource-basic
    }


    public static void createLoadTestResourceEncryption(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        // BEGIN: readme-sample-createloadtestresource-encryption
        // map of user-assigned managed identities to be assigned to the loadtest resource
        Map<String, UserAssignedIdentity> map = new HashMap<String, UserAssignedIdentity>();
        map.put("/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/sample-rg/providers/microsoft.managedidentity/userassignedidentities/identity1", new UserAssignedIdentity());

        // encryption identity must be assigned to the load test resource, before using it
        LoadTestResource resource = manager
            .loadTests()
            .define("sample-loadtesting-resource")
            .withRegion(Region.US_WEST2)
            .withExistingResourceGroup("sample-rg")
            .withIdentity(
                new ManagedServiceIdentity()
                .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                .withUserAssignedIdentities(map)
            )
            .withEncryption(
                new EncryptionProperties()
                .withIdentity(
                    new EncryptionPropertiesIdentity()
                    .withResourceId("/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/sample-rg/providers/microsoft.managedidentity/userassignedidentities/identity1")
                    .withType(Type.USER_ASSIGNED)
                )
                .withKeyUrl("https://sample-kv.vault.azure.net/keys/cmkkey/2d1ccd5c50234ea2a0858fe148b69cde")
            )
            .create();
        // END: readme-sample-createloadtestresource-encryption
    }

    public static void getLoadTestResource(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        // BEGIN: readme-sample-getloadtestresource
        LoadTestResource resource = manager
            .loadTests()
            .getByResourceGroup("sample-rg", "sample-loadtesting-resource");
        // END: readme-sample-getloadtestresource
    }

    public static void updateLoadTestResourceEncryption(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        // BEGIN: readme-sample-updateloadtestresource-encryption
        LoadTestResource resource = manager
            .loadTests()
            .getByResourceGroup("sample-rg", "sample-loadtesting-resource");

        LoadTestResource resourcePostUpdate = resource
            .update()
            .withIdentity(
                new ManagedServiceIdentity()
                .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)
            )
            .withEncryption(
                new EncryptionProperties()
                .withIdentity(
                    new EncryptionPropertiesIdentity()
                    .withResourceId(null)
                    .withType(Type.SYSTEM_ASSIGNED)
                    // make sure that system-assigned managed identity is enabled on the resource and the identity has been granted required permissions to access the key.
                )
                .withKeyUrl("https://sample-kv.vault.azure.net/keys/cmkkey/2d1ccd5c50234ea2a0858fe148b69cde")
            )
            .apply();
        // END: readme-sample-updateloadtestresource-encryption
    }

    public static void updateLoadTestResourceManagedIdentity(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        // BEGIN: readme-sample-updateloadtestresource-mi
        Map<String, UserAssignedIdentity> map = new HashMap<String, UserAssignedIdentity>();
        // Note: the value of <identity1> set to null, removes the previously assigned managed identity from the load test resource
        map.put("/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/sample-rg/providers/microsoft.managedidentity/userassignedidentities/identity1", null);
        map.put("/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/sample-rg/providers/microsoft.managedidentity/userassignedidentities/identity2", new UserAssignedIdentity());

        LoadTestResource resource = manager
            .loadTests()
            .getByResourceGroup("sample-rg", "sample-loadtesting-resource");

        LoadTestResource resourcePostUpdate = resource
            .update()
            .withIdentity(
                new ManagedServiceIdentity()
                .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(map)
            )
            .apply();
        // END: readme-sample-updateloadtestresource-mi
    }

    public static void deleteLoadTestResource(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        // BEGIN: readme-sample-deleteloadtestresource
        manager
            .loadTests()
            .deleteByResourceGroup("sample-rg", "sample-loadtesting-resource");
        // END: readme-sample-deleteloadtestresource
    }

    public static void listAllQuotaBuckets(LoadTestManager manager) {
        // BEGIN: readme-sample-list-all-quota-buckets
        PagedIterable<QuotaResource> resource = manager
            .quotas()
            .list("westus2");

        for (QuotaResource quotaResource : resource) {
            // use the quotaResource
            System.out.println(quotaResource.limit());
        }
        // END: readme-sample-list-all-quota-buckets
    }

    public static void getQuotaBucket(LoadTestManager manager) {
        // BEGIN: readme-sample-get-quota-bucket
        QuotaResource resource = manager
            .quotas()
            .get("westus2", "maxConcurrentTestRuns");
        System.out.println(resource.limit());
        // END: readme-sample-get-quota-bucket
    }

    public static void checkQuotaAvailability(LoadTestManager manager) {
        // BEGIN: readme-sample-check-quota-availability
        QuotaResource resource = manager
            .quotas()
            .get("westus2", "maxConcurrentTestRuns");

        QuotaBucketRequestPropertiesDimensions dimensions = new QuotaBucketRequestPropertiesDimensions()
            .withLocation("westus2")
            .withSubscriptionId(manager.serviceClient().getSubscriptionId());

        QuotaBucketRequest request = new QuotaBucketRequest()
            .withCurrentQuota(resource.limit())
            .withCurrentUsage(resource.usage())
            .withNewQuota(resource.limit())
            .withDimensions(dimensions);

        CheckQuotaAvailabilityResponse availability = manager
            .quotas()
            .checkAvailability("westus2", "maxConcurrentTestRuns", request);

        System.out.println(availability.isAvailable());
        // END: readme-sample-check-quota-availability
    }

}
