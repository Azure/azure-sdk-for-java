// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.util.Context;
import com.azure.data.appconfiguration.models.FeatureFlag;
import com.azure.data.appconfiguration.models.FeatureFlagSelector;
import reactor.core.publisher.Mono;

/**
 * Manages dedicated feature flag resources, not key-value-backed feature flag settings.
 */
public class FeatureFlagResourceSample {
    /**
     * Runs the synchronous sample.
     * @param args unused.
     */
    public static void main(String[] args) {
        String connectionString = System.getenv("AZURE_APP_CONFIG_CONNECTION_STRING");
        // BEGIN: readme-sample-featureFlagResources
        FeatureFlagClient client = new FeatureFlagClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        FeatureFlag created = client.addFeatureFlag(new FeatureFlag("MyFeature", true).setLabel("production"));
        FeatureFlag retrieved = client.getFeatureFlag(created.getName(), created.getLabel());
        retrieved.setEnabled(false);
        FeatureFlag updated = client.setFeatureFlagWithResponse(retrieved, true, Context.NONE).getValue();

        FeatureFlagSelector selector = new FeatureFlagSelector().setNameFilter("My*").setLabelFilter("production");
        client.listFeatureFlags(selector).forEach(flag -> System.out.println(flag.getName()));
        client.listFeatureFlagRevisions(selector).forEach(flag -> System.out.println(flag.getETag()));
        client.listLabels().forEach(label -> System.out.println(label.getName()));

        client.deleteFeatureFlagWithResponse(updated, true, Context.NONE);
        // END: readme-sample-featureFlagResources
    }

    /**
     * Creates a nonblocking sequence that writes, reads, and deletes a dedicated feature flag.
     * @param connectionString the App Configuration connection string.
     * @return the sequence; subscribe to execute it and handle any errors.
     */
    public Mono<Void> runAsync(String connectionString) {
        FeatureFlagAsyncClient client = new FeatureFlagClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();
        return client.setFeatureFlag(new FeatureFlag("MyAsyncFeature", true))
            .flatMap(flag -> client.getFeatureFlag(flag.getName()))
            .flatMap(flag -> client.deleteFeatureFlag(flag.getName()))
            .then();
    }

    /**
     * Illustrates asynchronous client construction and subscription.
     * @param connectionString the App Configuration connection string.
     */
    public void asyncSnippet(String connectionString) {
        // BEGIN: readme-sample-featureFlagResourcesAsync
        FeatureFlagAsyncClient client = new FeatureFlagClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();

        client.setFeatureFlag(new FeatureFlag("MyAsyncFeature", true))
            .flatMap(flag -> client.getFeatureFlag(flag.getName()))
            .flatMap(flag -> client.deleteFeatureFlag(flag.getName()))
            .subscribe(flag -> System.out.println(flag.getName()));
        // END: readme-sample-featureFlagResourcesAsync
    }
}
