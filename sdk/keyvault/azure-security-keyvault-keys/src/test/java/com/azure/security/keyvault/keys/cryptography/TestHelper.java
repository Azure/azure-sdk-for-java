// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.security.keyvault.keys.KeyServiceVersion;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

public class TestHelper extends TestBase {
    public static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    /**
     * Returns a stream of arguments that includes all combinations of eligible {@link HttpClient HttpClients} and
     * service versions that should be tested.
     *
     * @return A stream of HttpClient and service version combinations to test.
     */
    Stream<Arguments> getTestParameters() {
        // when this issues is closed, the newer version of junit will have better support for
        // cartesian product of arguments - https://github.com/junit-team/junit5/issues/1427
        List<Arguments> argumentsList = new ArrayList<>();

        getHttpClients()
            .forEach(httpClient -> {
                Arrays.stream(CryptographyServiceVersion.values()).filter(this::shouldServiceVersionBeTested)
                    .forEach(serviceVersion ->argumentsList.add(Arguments.of(httpClient, serviceVersion)));
            });
        return argumentsList.stream();
    }

    protected boolean shouldServiceVersionBeTested(CryptographyServiceVersion serviceVersion) {
        if (Configuration.getGlobalConfiguration().get(AZURE_TEST_SERVICE_VERSIONS) == null) {
            return CryptographyServiceVersion.getLatest().equals(serviceVersion);
        }
        return true;
    }
}
