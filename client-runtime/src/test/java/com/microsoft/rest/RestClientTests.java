/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.rest;

import com.microsoft.rest.credentials.BasicAuthenticationCredentials;
import com.microsoft.rest.credentials.TokenCredentials;
import com.microsoft.rest.interceptors.UserAgentInterceptor;
import com.microsoft.rest.protocol.ResponseBuilder;
import com.microsoft.rest.protocol.SerializerAdapter;
import com.microsoft.rest.serializer.JacksonAdapter;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.junit.Assert;
import org.junit.Test;
import retrofit2.Converter;

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
        Assert.assertEquals("https://management.azure.com/", restClient.retrofit().baseUrl().toString());
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
            .withInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    return chain.proceed(chain.request());
                }
            })
            .withUserAgent("user")
            .withNetworkInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    return chain.proceed(chain.request());
                }
            })
            .withConnectionTimeout(100, TimeUnit.MINUTES)
            .build();
        RestClient newClient = restClient.newBuilder().build();
        Assert.assertEquals(restClient.retrofit().baseUrl().toString(), newClient.retrofit().baseUrl().toString());
        Assert.assertEquals(restClient.logLevel(), newClient.logLevel());
        Assert.assertEquals(restClient.logLevel().isPrettyJson(), newClient.logLevel().isPrettyJson());
        Assert.assertEquals(restClient.serializerAdapter(), newClient.serializerAdapter());
        Assert.assertEquals(restClient.responseBuilderFactory(), newClient.responseBuilderFactory());
        Assert.assertEquals(restClient.credentials(), newClient.credentials());
        for (Interceptor interceptor :
            newClient.httpClient().interceptors()) {
            if (interceptor instanceof UserAgentInterceptor) {
                Assert.assertEquals("user", ((UserAgentInterceptor) interceptor).userAgent());
            }
        }
        Assert.assertEquals(restClient.httpClient().interceptors().size(), newClient.httpClient().interceptors().size());
        Assert.assertEquals(restClient.httpClient().networkInterceptors().size(), newClient.httpClient().networkInterceptors().size());
        Assert.assertEquals(TimeUnit.MINUTES.toMillis(100), newClient.httpClient().connectTimeoutMillis());
    }

    @Test
    public void newBuilderClonesProperties() {
        RestClient restClient = new RestClient.Builder()
            .withBaseUrl("http://localhost")
            .withSerializerAdapter(new JacksonAdapter())
            .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
            .withCredentials(new TokenCredentials("Bearer", "token"))
            .withLogLevel(LogLevel.BASIC.withPrettyJson(true))
            .withInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    return chain.proceed(chain.request());
                }
            })
            .withUserAgent("user")
            .withNetworkInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    return chain.proceed(chain.request());
                }
            })
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
        Assert.assertNotEquals(restClient.retrofit().baseUrl().toString(), newClient.retrofit().baseUrl().toString());
        Assert.assertNotEquals(restClient.logLevel(), newClient.logLevel());
        Assert.assertNotEquals(restClient.logLevel().isPrettyJson(), newClient.logLevel().isPrettyJson());
        Assert.assertNotEquals(restClient.serializerAdapter(), newClient.serializerAdapter());
        Assert.assertNotEquals(restClient.responseBuilderFactory(), newClient.responseBuilderFactory());
        Assert.assertNotEquals(restClient.credentials(), newClient.credentials());
        for (Interceptor interceptor :
            restClient.httpClient().interceptors()) {
            if (interceptor instanceof UserAgentInterceptor) {
                Assert.assertEquals("user", ((UserAgentInterceptor) interceptor).userAgent());
            }
        }
        for (Interceptor interceptor :
            newClient.httpClient().interceptors()) {
            if (interceptor instanceof UserAgentInterceptor) {
                Assert.assertEquals("anotheruser", ((UserAgentInterceptor) interceptor).userAgent());
            }
        }
        Assert.assertNotEquals(restClient.httpClient().connectTimeoutMillis(), newClient.httpClient().connectTimeoutMillis());
    }
}
