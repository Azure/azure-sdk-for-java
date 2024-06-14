// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.tests;

import com.azure.ai.vision.face.FaceAsyncClient;
import com.azure.ai.vision.face.FaceClient;
import com.azure.ai.vision.face.FaceClientBuilder;
import com.azure.ai.vision.face.FaceServiceVersion;
import com.azure.ai.vision.face.FaceSessionAsyncClient;
import com.azure.ai.vision.face.FaceSessionClient;
import com.azure.ai.vision.face.FaceSessionClientBuilder;
import com.azure.ai.vision.face.samples.utils.ConfigurationHelper;
import com.azure.ai.vision.face.tests.commands.CommandProvider;
import com.azure.ai.vision.face.tests.utils.TestUtils;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.KeyCredentialTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class FaceClientTestBase extends TestProxyTestBase {

    private static final HashMap<Class<?>, Function<FaceClientTestBase, HttpClient, FaceServiceVersion, Object>> TYPE_MAP = new HashMap<Class<?>, Function<FaceClientTestBase, HttpClient, FaceServiceVersion, Object>>() {{
            put(FaceClient.class, (testBase, httpClient, serviceVersion) ->
                testBase.getFaceClientBuilder(serviceVersion, httpClient, true).buildClient());
            put(FaceAsyncClient.class, (testBase, httpClient, serviceVersion) ->
                testBase.getFaceClientBuilder(serviceVersion, httpClient, false).buildAsyncClient());
            put(FaceSessionClient.class, (testBase, httpClient, serviceVersion) ->
                testBase.getFaceSessionClientBuilder(serviceVersion, httpClient, true).buildClient());
            put(FaceSessionAsyncClient.class, (testBase, httpClient, serviceVersion) ->
                testBase.getFaceSessionClientBuilder(serviceVersion, httpClient, false).buildAsyncClient());
        }};


    protected <TSyncClient, TAsyncClient, TCommand> Stream<Triple<String, FaceServiceVersion, Supplier<TCommand>>> createClientArgumentStream(
            Class<TSyncClient> clientClass,
            Class<TAsyncClient> asyncClientClass,
            CommandProvider<TSyncClient, TAsyncClient, TCommand>[] commandBuilders) {
        return getHttpClients()
                .flatMap(httpClient -> Arrays.stream(TestUtils.getServiceVersions())
                        .flatMap(serviceVersion -> Arrays.stream(commandBuilders)
                                .map(builderFunction -> Triple.of(
                                        httpClient.getClass().getSimpleName(), serviceVersion, new CommandProviderAdapter<>(
                                                httpClient, serviceVersion, clientClass, asyncClientClass, builderFunction)
                                ))));
    }

    @SuppressWarnings("unchecked")
    protected <T> T createTestClient(
            Class<T> clazz, HttpClient httpClient, FaceServiceVersion serviceVersion) {
        Function<FaceClientTestBase, HttpClient, FaceServiceVersion, Object> creator = TYPE_MAP.get(clazz);
        if (null == creator) {
            throw new IllegalArgumentException("No such client type: " + clazz);
        }

        return (T) creator.apply(this, httpClient, serviceVersion);
    }

    private FaceClientBuilder getFaceClientBuilder(
            FaceServiceVersion serviceVersion, HttpClient httpClient, boolean isSync) {
        return this.configureBuilder(new FaceClientBuilder().serviceVersion(serviceVersion), httpClient, isSync);
    }

    private FaceSessionClientBuilder getFaceSessionClientBuilder(
            FaceServiceVersion serviceVersion, HttpClient httpClient, boolean isSync) {
        return this.configureBuilder(new FaceSessionClientBuilder().serviceVersion(serviceVersion), httpClient, isSync);
    }

    private HttpClient createHttpClient(HttpClient httpClient, boolean isSync) {
        if (httpClient == null) {
            httpClient = HttpClient.createDefault();
        }

        AssertingHttpClientBuilder builder =  new AssertingHttpClientBuilder(httpClient)
                .skipRequest((ignored1, ignored2) -> false);

        if (isSync) {
            builder.assertSync();
        } else {
            builder.assertAsync();
        }

        return  builder.build();
    }

    private <T extends HttpTrait<T> & TokenCredentialTrait<T> & KeyCredentialTrait<T> & EndpointTrait<T>> T configureBuilder(
        T clientBuilder, HttpClient httpClient, boolean isSync) {

        clientBuilder.endpoint(ConfigurationHelper.getEndpoint())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        switch (getTestMode()) {
            case PLAYBACK:
                return configureForPlayBackMode(clientBuilder, httpClient, isSync);
            case RECORD:
                return configureForRecordMode(clientBuilder, httpClient, isSync);
            case LIVE:
                return configureForLiveMode(clientBuilder, httpClient, isSync);
            default:
                throw new IllegalStateException("Incorrect test mode:" + getTestMode());
        }
    }

    private <T extends HttpTrait<T> & TokenCredentialTrait<T> & KeyCredentialTrait<T>> T configureCredential(T clientBuilder) {
        String accountKey = ConfigurationHelper.getAccountKey();
        if (accountKey != null && !accountKey.isEmpty()) {
            return clientBuilder.credential(new KeyCredential(accountKey));
        }

        return clientBuilder.credential(new DefaultAzureCredentialBuilder().build());
    }

    private <T extends HttpTrait<T> & TokenCredentialTrait<T> & KeyCredentialTrait<T> & EndpointTrait<T>> T configureForPlayBackMode(
            T clientBuilder, HttpClient httpClient, boolean isSync) {
        addSanitizers(interceptorManager);
        return clientBuilder
            .endpoint("https://localhost:8080")
            .httpClient(createHttpClient(interceptorManager.getPlaybackClient(), isSync))
            .credential(new AzureKeyCredential("Fake"));
    }

    private <T extends HttpTrait<T> & TokenCredentialTrait<T> & KeyCredentialTrait<T> & EndpointTrait<T>> T configureForRecordMode(
            T clientBuilder, HttpClient httpClient, boolean isSync) {
        addSanitizers(interceptorManager);
        return configureCredential(clientBuilder)
            .endpoint(ConfigurationHelper.getEndpoint())
            .httpClient(createHttpClient(httpClient, isSync))
            .addPolicy(interceptorManager.getRecordPolicy());
    }

    private static void addSanitizers(InterceptorManager interceptorManager) {
        interceptorManager.addSanitizers(Arrays.asList(
                new TestProxySanitizer("Content-Type", "multipart/form-data.*", "multipart/form-data", TestProxySanitizerType.HEADER),
                new TestProxySanitizer("$..deviceCorrelationId", null, TestUtils.EMPTY_UUID, TestProxySanitizerType.BODY_KEY),
                new TestProxySanitizer("$..authToken", null, TestUtils.FAKE_TOKEN, TestProxySanitizerType.BODY_KEY)
            ));
    }

    private <T extends HttpTrait<T> & TokenCredentialTrait<T> & KeyCredentialTrait<T> & EndpointTrait<T>> T configureForLiveMode(
            T clientBuilder, HttpClient httpClient, boolean isSync) {
        return configureCredential(clientBuilder)
                .endpoint(ConfigurationHelper.getEndpoint())
                .httpClient(createHttpClient(httpClient, isSync));
    }

    @FunctionalInterface
    interface Function<T, U, V, R> {
        R apply(T t, U u, V v);
    }

    class CommandProviderAdapter<TSyncClient, TAsyncClient, TCommand> implements Supplier<TCommand> {
        private final Class<TSyncClient> clientClass;
        private final HttpClient httpClient;
        private final FaceServiceVersion serviceVersion;
        private final Class<TAsyncClient> asyncClientClass;
        private final CommandProvider<TSyncClient, TAsyncClient, TCommand> provider;

        CommandProviderAdapter(
                HttpClient httpClient,
                FaceServiceVersion serviceVersion,
                Class<TSyncClient> clientClass,
                Class<TAsyncClient> asyncClientClass,
                CommandProvider<TSyncClient, TAsyncClient, TCommand> provider) {
            this.httpClient = httpClient;
            this.asyncClientClass = asyncClientClass;
            this.serviceVersion = serviceVersion;
            this.clientClass = clientClass;
            this.provider = provider;
        }

        @Override
        public TCommand get() {
            TSyncClient syncClient = createTestClient(clientClass, httpClient, serviceVersion);
            TAsyncClient asyncClient = createTestClient(asyncClientClass, httpClient, serviceVersion);
            return provider.apply(syncClient, asyncClient);
        }

        @Override
        public String toString() {
            return this.provider.getTag();
        }
    }
}

