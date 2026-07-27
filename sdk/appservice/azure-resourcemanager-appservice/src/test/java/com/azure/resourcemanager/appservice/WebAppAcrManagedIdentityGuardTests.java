// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.appservice.models.PricingTier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

/**
 * Unit tests (no network / test-proxy) for the {@code withManagedIdentityCredentials()} guard: managed identity image
 * pull is an Azure Container Registry feature and must not be combined with a Docker Hub image.
 */
public class WebAppAcrManagedIdentityGuardTests {

    private static AppServiceManager manager() {
        TokenCredential credential
            = request -> Mono.just(new AccessToken("fake-token", OffsetDateTime.now().plusHours(1)));
        return AppServiceManager.authenticate(credential, new AzureProfile("00000000-0000-0000-0000-000000000000",
            "00000000-0000-0000-0000-000000000000", AzureEnvironment.AZURE));
    }

    @Test
    public void managedIdentityRejectedForDockerHubImage() {
        AppServiceManager manager = manager();
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> manager.webApps()
                .define("webapp")
                .withRegion(Region.US_WEST)
                .withNewResourceGroup("rg")
                .withNewLinuxPlan(PricingTier.BASIC_B1)
                .withPrivateDockerHubImage("nginx")
                .withManagedIdentityCredentials());
    }

    @Test
    public void managedIdentityAllowedForPrivateRegistryImage() {
        AppServiceManager manager = manager();
        // Builder chain stays local until create(); no exception expected for the private-registry (ACR) path.
        Assertions.assertDoesNotThrow(() -> manager.webApps()
            .define("webapp")
            .withRegion(Region.US_WEST)
            .withNewResourceGroup("rg")
            .withNewLinuxPlan(PricingTier.BASIC_B1)
            .withPrivateRegistryImage("samples/nginx:latest", "https://myregistry.azurecr.io")
            .withManagedIdentityCredentials());
    }
}
