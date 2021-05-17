// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.core.http.HttpClient;
import com.azure.core.test.implementation.ImplUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.containers.containerregistry.TestUtils.ANONYMOUS_REGISTRY_ENDPOINT;
import static com.azure.containers.containerregistry.TestUtils.ANONYMOUS_REGISTRY_NAME;
import static com.azure.containers.containerregistry.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.containers.containerregistry.TestUtils.HELLO_WORLD_REPOSITORY_NAME;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContainerRepositoryAnonymousAccessTests extends ContainerRegistryClientsTestBase {
    @BeforeEach
    void beforeEach() {
        TestUtils.importImageAsync(ImplUtils.getTestMode(), ANONYMOUS_REGISTRY_NAME, HELLO_WORLD_REPOSITORY_NAME, Arrays.asList("latest", "v1", "v2", "v3", "v4")).block();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void listAnonymousRepositories(HttpClient httpClient) {
        ContainerRegistryClient client = getContainerRegistryBuilder(httpClient, null, ANONYMOUS_REGISTRY_ENDPOINT).buildClient();
        List<String> repositories = client.listRepositoryNames().stream().collect(Collectors.toList());
        assertTrue(repositories.stream().anyMatch(a -> HELLO_WORLD_REPOSITORY_NAME.equals(a)));
    }
}
