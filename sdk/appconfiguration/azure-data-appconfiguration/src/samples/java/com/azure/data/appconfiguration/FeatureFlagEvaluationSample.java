// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.implementation.models.FeatureFlag;
import com.azure.data.appconfiguration.implementation.models.FeatureFlagConditions;
import com.azure.data.appconfiguration.implementation.models.FeatureFlagFilter;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Sample demonstrates how to use the dedicated feature flag endpoints introduced in the 2026-05-01-preview API to
 * create, retrieve, list, toggle, and delete feature flags using Entra ID (Azure Active Directory) authentication.
 */
public class FeatureFlagEvaluationSample {
    /**
     * Runs the sample demonstrating feature flag operations with the new feature flag endpoints.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        // Use Entra ID authentication with DefaultAzureCredential.
        // Ensure you are logged in via `az login` or have appropriate environment credentials configured.
        String endpoint = Configuration.getGlobalConfiguration().get("AZ_CONFIG_ENDPOINT");

        ConfigurationClient client = new ConfigurationClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildClient();

        System.out.println("Beginning feature flag evaluation sample...");

        // 1. Create feature flags using the dedicated feature flag endpoints.
        FeatureFlagFilter percentageFilter = new FeatureFlagFilter("Microsoft.Percentage")
            .setParameters(Collections.singletonMap("Value", "50"));

        FeatureFlagFilter targetingFilter = new FeatureFlagFilter("Microsoft.Targeting")
            .setParameters(Collections.singletonMap("Audience", "beta-users"));

        FeatureFlag darkModeFlag = new FeatureFlag()
            .setEnabled(true)
            .setDescription("Controls whether dark mode is available to users")
            .setConditions(new FeatureFlagConditions()
                .setFilters(Arrays.asList(percentageFilter, targetingFilter)));

        FeatureFlag newCheckoutFlag = new FeatureFlag()
            .setEnabled(false)
            .setDescription("New streamlined checkout experience");

        // Add the feature flags (creates only if they don't already exist).
        FeatureFlag darkMode = client.addFeatureFlag("dark-mode", darkModeFlag);
        System.out.printf("Created feature flag 'dark-mode' - enabled: %s%n", darkMode.isEnabled());

        FeatureFlag newCheckout = client.addFeatureFlag("new-checkout-flow", newCheckoutFlag);
        System.out.printf("Created feature flag 'new-checkout-flow' - enabled: %s%n", newCheckout.isEnabled());

        // 2. Retrieve a single feature flag.
        darkMode = client.getFeatureFlag("dark-mode");

        if (darkMode.isEnabled()) {
            System.out.println("\nDark mode feature is ENABLED.");
            System.out.printf("  Description: %s%n", darkMode.getDescription());

            // Evaluate the flag's filter conditions against your application context.
            List<FeatureFlagFilter> filters = darkMode.getConditions().getFilters();
            if (filters != null) {
                System.out.printf("  Filters to evaluate (%d):%n", filters.size());
                for (FeatureFlagFilter filter : filters) {
                    System.out.printf("    - %s: %s%n", filter.getName(), filter.getParameters());
                }
            }
        } else {
            System.out.println("\nDark mode feature is DISABLED.");
        }

        // 3. List all feature flags.
        System.out.println("\nAll feature flags in the store:");
        client.listFeatureFlags().forEach(flag -> System.out.printf("  [%s] %s - enabled: %s%n",
            flag.getName(), flag.getDescription(), flag.isEnabled()));

        // 4. Toggle a feature flag — enable the new checkout flow.
        FeatureFlag updatedCheckout = new FeatureFlag()
            .setEnabled(true)
            .setDescription("New streamlined checkout experience");

        updatedCheckout = client.setFeatureFlag("new-checkout-flow", updatedCheckout);
        System.out.printf("%nToggled 'new-checkout-flow' to enabled: %s%n", updatedCheckout.isEnabled());

        // 5. Clean up — delete the feature flags.
        client.deleteFeatureFlag("dark-mode");
        client.deleteFeatureFlag("new-checkout-flow");
        System.out.println("Deleted feature flags.");

        System.out.println("\nEnd of feature flag evaluation sample.");
    }
}
