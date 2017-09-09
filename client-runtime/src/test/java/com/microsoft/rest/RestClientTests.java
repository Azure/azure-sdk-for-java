/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.rest;

import com.microsoft.rest.credentials.BasicAuthenticationCredentials;
import com.microsoft.rest.credentials.TokenCredentials;
import com.microsoft.rest.protocol.ResponseBuilder;
import com.microsoft.rest.protocol.SerializerAdapter;
import com.microsoft.rest.serializer.JacksonAdapter;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.policy.RequestPolicy;
import org.junit.Assert;
import org.junit.Test;
import retrofit2.Converter;
import rx.Single;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RestClientTests {
    @Test
    public void defaultConfigs() {
        RestClient restClient = new RestClient.Builder()
                .withBaseUrl("https://management.azure.com/")
                .withSerializerAdapter(new JacksonAdapter())
                .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
                .build();
        Assert.assertEquals("https://management.azure.com/", restClient.baseURL());
        Assert.assertEquals(LogLevel.NONE, restClient.logLevel());
        Assert.assertTrue(restClient.responseBuilderFactory() instanceof ServiceResponseBuilder.Factory);
        Assert.assertTrue(restClient.serializerAdapter() instanceof JacksonAdapter);
        Assert.assertNull(restClient.credentials());
    }

    @Test
    public void newBuilderKeepsConfigs() {
        RestClient restClient = new RestClient.Builder()
            .withBaseUrl("http://localhost")
            .withSerializerAdapter(new JacksonAdapter())
            .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
            .withCredentials(new TokenCredentials("Bearer", "token"))
            .withLogLevel(LogLevel.BASIC)
            .addCustomPolicy(new RequestPolicy.Factory() {
                @Override
                public RequestPolicy create(final RequestPolicy next) {
                    return new RequestPolicy() {
                        @Override
                        public Single<HttpResponse> sendAsync(HttpRequest request) {
                            return next.sendAsync(request);
                        }
                    };
                }
            })
            .withUserAgent("user")
            .withConnectionTimeout(100, TimeUnit.MINUTES)
            .build();
        RestClient newClient = restClient.newBuilder().build();
        Assert.assertEquals(restClient.baseURL(), newClient.baseURL());
        Assert.assertEquals(restClient.logLevel(), newClient.logLevel());
        Assert.assertEquals(restClient.logLevel().isPrettyJson(), newClient.logLevel().isPrettyJson());
        Assert.assertEquals(restClient.serializerAdapter(), newClient.serializerAdapter());
        Assert.assertEquals(restClient.responseBuilderFactory(), newClient.responseBuilderFactory());
        Assert.assertEquals(restClient.credentials(), newClient.credentials());
//        for (RequestPolicy.Factory factory :
//            newClient.customPolicyFactories()) {
//            if (factory instanceof UserAgentPolicy) {
//                Assert.assertEquals("user", ((UserAgentPolicy) factory).userAgent());
//            }
//        }
        Assert.assertEquals(restClient.customPolicyFactories().size(), newClient.customPolicyFactories().size());
        Assert.assertEquals(TimeUnit.MINUTES.toMillis(100), newClient.connectionTimeoutMillis());
    }

    @Test
    public void newBuilderClonesProperties() {
        RestClient restClient = new RestClient.Builder()
            .withBaseUrl("http://localhost")
            .withSerializerAdapter(new JacksonAdapter())
            .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
            .withCredentials(new TokenCredentials("Bearer", "token"))
            .withLogLevel(LogLevel.BASIC.withPrettyJson(true))
            .addCustomPolicy(new RequestPolicy.Factory() {
                @Override
                public RequestPolicy create(final RequestPolicy next) {
                    return new RequestPolicy() {
                        @Override
                        public Single<HttpResponse> sendAsync(HttpRequest request) {
                            return next.sendAsync(request);
                        }
                    };
                }
            })
            .withUserAgent("user")
            .withConnectionTimeout(100, TimeUnit.MINUTES)
            .build();
        RestClient newClient = restClient.newBuilder()
            .withBaseUrl("https://contoso.com")
            .withCredentials(new BasicAuthenticationCredentials("user", "pass"))
            .withLogLevel(LogLevel.BODY_AND_HEADERS)
            .withUserAgent("anotheruser")
            .withConnectionTimeout(200, TimeUnit.SECONDS)
            .withSerializerAdapter(new SerializerAdapter<Object>() {
                @Override
                public Object serializer() {
                    return null;
                }

                @Override
                public Converter.Factory converterFactory() {
                    return retrofit2.converter.jackson.JacksonConverterFactory.create();
                }

                @Override
                public String serialize(Object object) throws IOException {
                    return null;
                }

                @Override
                public String serializeRaw(Object object) {
                    return null;
                }

                @Override
                public String serializeList(List<?> list, CollectionFormat format) {
                    return null;
                }

                @Override
                public <U> U deserialize(String value, Type type) throws IOException {
                    return null;
                }
            })
            .withResponseBuilderFactory(new ResponseBuilder.Factory() {
                @Override
                public <T, E extends RestException> ResponseBuilder<T, E> newInstance(SerializerAdapter<?> serializerAdapter) {
                    return null;
                }
            })
            .build();
        Assert.assertNotEquals(restClient.baseURL(), newClient.baseURL());
        Assert.assertNotEquals(restClient.logLevel(), newClient.logLevel());
        Assert.assertNotEquals(restClient.logLevel().isPrettyJson(), newClient.logLevel().isPrettyJson());
        Assert.assertNotEquals(restClient.serializerAdapter(), newClient.serializerAdapter());
        Assert.assertNotEquals(restClient.responseBuilderFactory(), newClient.responseBuilderFactory());
        Assert.assertNotEquals(restClient.credentials(), newClient.credentials());
//        for (Interceptor interceptor :
//            restClient.httpClient().interceptors()) {
//            if (interceptor instanceof UserAgentPolicy) {
//                Assert.assertEquals("user", ((UserAgentPolicy) interceptor).userAgent());
//            }
//        }
//        for (Interceptor interceptor :
//            newClient.httpClient().interceptors()) {
//            if (interceptor instanceof UserAgentPolicy) {
//                Assert.assertEquals("anotheruser", ((UserAgentPolicy) interceptor).userAgent());
//            }
//        }
        Assert.assertNotEquals(restClient.connectionTimeoutMillis(), newClient.connectionTimeoutMillis());
    }
}
