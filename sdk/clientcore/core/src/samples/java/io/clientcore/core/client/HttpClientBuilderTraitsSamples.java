// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.client;

import io.clientcore.core.client.traits.EndpointTrait;
import io.clientcore.core.client.traits.KeyCredentialTrait;
import io.clientcore.core.credential.KeyCredential;
import io.clientcore.core.http.models.HttpLogOptions;
import io.clientcore.core.http.models.HttpRetryOptions;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.KeyCredentialPolicy;

import java.time.Duration;

public class HttpClientBuilderTraitsSamples {
    public static final class SimpleServiceClientBuilder extends HttpClientBuilderBase<SimpleServiceClientBuilder>
        implements EndpointTrait<SimpleServiceClientBuilder>, KeyCredentialTrait<SimpleServiceClientBuilder> {
        private String endpoint;
        private KeyCredential credential;

        @Override
        public SimpleServiceClientBuilder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        @Override
        public SimpleServiceClientBuilder credential(KeyCredential credential) {
            this.credential = credential;
            return this;
        }

        public SimpleServiceClient build() {
            HttpPipeline pipeline = modifyHttpPipelineBuilder(builder ->
                builder.policies(new KeyCredentialPolicy("api-version", this.credential)))
                .buildHttpPipeline();

            return new SimpleServiceClient(pipeline, this.endpoint);
        }
    }

    public static final class SimpleServiceClient {
        private SimpleServiceClient(HttpPipeline httpPipeline, String endpoint) {
        }
    }

    public static void useSimpleServiceClientBuilder() {
        SimpleServiceClient client = new SimpleServiceClientBuilder()
            .endpoint("https://example.com")
            .credential(new KeyCredential("key1"))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.HEADERS))
            .httpRetryOptions(new HttpRetryOptions(5, Duration.ofSeconds(1)))
            .build();
    }

    public static final class ComplexServiceClientBuilder extends HttpClientBuilderBase<ComplexServiceClientBuilder>
        implements EndpointTrait<ComplexServiceClientBuilder>, KeyCredentialTrait<ComplexServiceClientBuilder> {
        private KeyCredential credential;
        private ComplexServiceClientOptions clientOptions = new ComplexServiceClientOptions();

        @Override
        public ComplexServiceClientBuilder endpoint(String endpoint) {
            this.clientOptions.endpoint = endpoint;
            return this;
        }

        @Override
        public ComplexServiceClientBuilder credential(KeyCredential credential) {
            this.credential = credential;
            return this;
        }

        public ComplexServiceClientBuilder customStuff(Object customStuff) {
            this.clientOptions.customStuff = customStuff;
            return this;
        }

        public ComplexServiceClientBuilder moreCustomStuff(Object moreCustomStuff) {
            this.clientOptions.moreCustomStuff = moreCustomStuff;
            return this;
        }

        public ComplexServiceClient build() {
            HttpPipeline pipeline = modifyHttpPipelineBuilder(builder ->
                builder.policies(new KeyCredentialPolicy("api-version", this.credential)))
                .buildHttpPipeline();

            return new ComplexServiceClient(pipeline, clientOptions);
        }
    }

    public static final class ComplexServiceClientOptions {
        private String endpoint;
        private Object customStuff;
        private Object moreCustomStuff;
    }

    public static final class ComplexServiceClient {
        private ComplexServiceClient(HttpPipeline httpPipeline, ComplexServiceClientOptions clientOptions) {
        }
    }

    public static void useComplexServiceClientBuilder() {
        ComplexServiceClient client = new ComplexServiceClientBuilder()
            .endpoint("https://example.com")
            .credential(new KeyCredential("key1"))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.HEADERS))
            .httpRetryOptions(new HttpRetryOptions(5, Duration.ofSeconds(1)))
            .customStuff(new Object())
            .moreCustomStuff(new Object())
            .build();
    }
}
