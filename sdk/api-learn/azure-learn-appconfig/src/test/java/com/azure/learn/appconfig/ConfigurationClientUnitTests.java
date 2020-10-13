// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.learn.appconfig;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.learn.appconfig.implementation.AzureAppConfigurationImpl;
import com.azure.learn.appconfig.implementation.models.ErrorException;
import com.azure.learn.appconfig.implementation.models.GetKeyValueHeaders;
import com.azure.learn.appconfig.implementation.models.GetKeyValueResponse;
import com.azure.learn.appconfig.implementation.models.PutKeyValueHeaders;
import com.azure.learn.appconfig.implementation.models.PutKeyValueResponse;
import com.azure.learn.appconfig.models.ConfigurationSetting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigurationClientUnitTests {
    @ParameterizedTest
    @MethodSource("putAndGetConfigurationMethodSource")
    public void putAndGetConfiguration(ConfigurationSetting expected) {
        // arrange
        Map<String, ConfigurationSetting> configStore = new HashMap<>();

        AzureAppConfigurationImpl restApiClient = mock(AzureAppConfigurationImpl.class);
        when(restApiClient.putKeyValueWithResponseAsync(any(), any(), any(), any(), any(), any()))
            .then(a -> {
                configStore.put(a.getArgument(0), a.getArgument(4));
                return Mono.just(new PutKeyValueResponse(mock(HttpRequest.class), 200, mock(HttpHeaders.class), a.getArgument(4), mock(PutKeyValueHeaders.class)));
            });
        when(restApiClient.getKeyValueWithResponseAsync(any(), any(), any(), any(), any(), any(), any()))
            .then(a -> {
                if (configStore.containsKey(a.getArgument(0))) {
                    return Mono.just(new GetKeyValueResponse(mock(HttpRequest.class), configStore.containsKey(a.getArgument(0)) ? 200 : 404, mock(HttpHeaders.class), configStore.get(a.getArgument(0)), mock(GetKeyValueHeaders.class)));
                } else {
                    return Mono.error(new ErrorException("config not found", mock(HttpResponse.class)));
                }
            });

        ConfigurationClient client = new ConfigurationClient(new ConfigurationAsyncClient(restApiClient));

        // act
        try {
            ConfigurationSetting notFound = client.getConfigurationSetting(expected.getKey());
            fail();
        } catch (Throwable t) {
            // expected
            assertTrue(t instanceof ErrorException);
            assertEquals("config not found", t.getMessage());
        }
        ConfigurationSetting actual = client.putConfigurationSetting(expected);

        // assert
        assertEquals(expected.getKey(), actual.getKey());
        assertEquals(expected.getValue(), actual.getValue());

        // act
        actual = client.getConfigurationSetting(expected.getKey());

        // assert
        assertEquals(expected.getKey(), actual.getKey());
        assertEquals(expected.getValue(), actual.getValue());
    }

    private static Stream<ConfigurationSetting> putAndGetConfigurationMethodSource() {
        return Stream.of(new ConfigurationSetting().setKey("FontColor").setValue("Green"),
            new ConfigurationSetting().setKey("GreetingText").setValue("Hello World!"));
    }
}
