// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.applicationconfig.ConfigurationAsyncClient;
import com.azure.applicationconfig.credentials.ConfigurationClientCredentials;
import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.applicationconfig.models.SettingSelector;
import com.azure.common.http.HttpMethod;
import com.azure.common.http.HttpPipelineCallContext;
import com.azure.common.http.HttpPipelineNextPolicy;
import com.azure.common.http.HttpResponse;
import com.azure.common.http.policy.HttpLogDetailLevel;
import com.azure.common.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
     * @throws NoSuchAlgorithmException when credentials cannot be created because the service cannot resolve the
     * HMAC-SHA256 algorithm.
     * @throws InvalidKeyException when credentials cannot be created because the connection string is invalid.
     */
    public static void main(String[] args)  throws NoSuchAlgorithmException, InvalidKeyException {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        final String connectionString = "endpoint={endpoint_value};id={id_value};name={secret_value}";
        final HttpMethodRequestTracker tracker = new HttpMethodRequestTracker();

        // Instantiate a client that will be used to call the service.
        // We add in a policy to track the type of HTTP method calls we make.
        // We also want to see the Header information of our HTTP requests, so we specify the detail level.
        final ConfigurationAsyncClient client = ConfigurationAsyncClient.builder()
                .credentials(new ConfigurationClientCredentials(connectionString))
                .addPolicy(new HttpMethodRequestTrackingPolicy(tracker))
                .httpLogDetailLevel(HttpLogDetailLevel.HEADERS)
                .build();

        // Adding a couple of settings and then fetching all the settings in our repository.
        final List<ConfigurationSetting> settings = Flux.concat(client.addSetting(new ConfigurationSetting().key("hello").value("world")),
                client.setSetting(new ConfigurationSetting().key("newSetting").value("newValue")))
                .then(client.listSettings(new SettingSelector().key("*")).collectList())
                .block();

        // Cleaning up after ourselves by deleting the values.
        final Stream<ConfigurationSetting> stream = settings == null
                ? Stream.empty()
                : settings.stream();
        Flux.merge(stream.map(client::deleteSetting).collect(Collectors.toList())).blockLast();

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
                System.out.println(String.format("HTTP Method [%s], # of calls: %s", key, value));
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
            tracker.increment(context.httpRequest().httpMethod());
            return next.process();
        }
    }
}
