// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.provider.Arguments;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.azure.core.test.TestProxyTestBase.getHttpClients;

public class TestUtils {

    static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    static final String FAKE_API_KEY = "fakeKeyPlaceholder";

    /**
     * Returns a stream of arguments that includes all combinations of eligible {@link HttpClient HttpClients}
     * that should be tested.
     *
     * @return A stream of HttpClients to test.
     */
    static Stream<Arguments> getTestParameters() {
        List<Arguments> argumentsList = new ArrayList<>();
        getHttpClients()
            .forEach(httpClient -> argumentsList.add(Arguments.of(httpClient, AIProjectsServiceVersion.V1)));
        return argumentsList.stream();
    }
}
