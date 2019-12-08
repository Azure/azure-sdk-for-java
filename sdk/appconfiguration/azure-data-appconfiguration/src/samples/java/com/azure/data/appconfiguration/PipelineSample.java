// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Sample demonstrates how to add a custom policy into the HTTP pipeline.
 */
class PipelineSample {
    /**
     * Runs the sample algorithm and demonstrates how to add a custom policy to the HTTP pipeline.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        final String connectionString = "endpoint={endpoint_value};id={id_value};name={secret_value}";
        final HttpMethodRequestTracker tracker = new HttpMethodRequestTracker();

        // Instantiate a client that will be used to call the service.
        // We add in a policy to track the type of HTTP method calls we make.
        // We also want to see the Header information of our HTTP requests, so we specify the detail level.
        final ConfigurationAsyncClient client = new ConfigurationClientBuilder()
                .connectionString(connectionString)
                .addPolicy(new HttpMethodRequestTrackingPolicy(tracker))
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.HEADERS))
                .buildAsyncClient();

        // Adding a couple of settings and then fetching all the settings in our repository.
        final List<ConfigurationSetting> settings = Flux.concat(
            client.addConfigurationSetting("hello", null, "world"),
            client.setConfigurationSetting("newSetting", null, "newValue"))
            .then(client.listConfigurationSettings(new SettingSelector().setKeys("*")).collectList())
            .block();

        // Cleaning up after ourselves by deleting the values.
        final Stream<ConfigurationSetting> stream = settings == null ? Stream.empty() : settings.stream();
        Flux.merge(stream.map(setting -> client.deleteConfigurationSettingWithResponse(setting, false))
            .collect(Collectors.toList())).blockLast();

        // Check what sort of HTTP method calls we made.
        tracker.print();
    }

    static class HttpMethodRequestTracker {
        private final ConcurrentHashMap<HttpMethod, Integer> tracker = new ConcurrentHashMap<>();

        void increment(HttpMethod key) {
            tracker.compute(key, (k, value) -> {
                if (value == null) {
                    return 1;
                } else {
                    return value + 1;
                }
            });
        }

        void print() {
            tracker.forEach((key, value) -> {
                System.out.printf(String.format("HTTP Method [%s], # of calls: %s", key, value));
            });
        }
    }

    /*
     * This policy tracks the number of HTTPMethod calls we make.
     */
    static class HttpMethodRequestTrackingPolicy implements HttpPipelinePolicy {
        private final HttpMethodRequestTracker tracker;

        HttpMethodRequestTrackingPolicy(HttpMethodRequestTracker tracker) {
            this.tracker = tracker;
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            tracker.increment(context.getHttpRequest().getHttpMethod());
            return next.process();
        }
    }
}
