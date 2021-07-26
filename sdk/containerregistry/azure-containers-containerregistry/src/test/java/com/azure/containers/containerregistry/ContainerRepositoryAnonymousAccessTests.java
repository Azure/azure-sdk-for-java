// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.core.http.HttpClient;
import com.azure.identity.AzureAuthorityHosts;
import org.junit.jupiter.api.Assumptions;
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
import static com.azure.containers.containerregistry.TestUtils.getAuthority;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContainerRepositoryAnonymousAccessTests extends ContainerRegistryClientsTestBase {
    @BeforeEach
    void beforeEach() {
        TestUtils.importImageAsync(getTestMode(), ANONYMOUS_REGISTRY_NAME, HELLO_WORLD_REPOSITORY_NAME, Arrays.asList("latest", "v1", "v2", "v3", "v4"), ANONYMOUS_REGISTRY_ENDPOINT).block();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void listAnonymousRepositories(HttpClient httpClient) {
        Assumptions.assumeFalse(ANONYMOUS_REGISTRY_ENDPOINT == null);
        Assumptions.assumeTrue(getAuthority(ANONYMOUS_REGISTRY_ENDPOINT).equals(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD));

        ContainerRegistryClient client = getContainerRegistryBuilder(httpClient, null, ANONYMOUS_REGISTRY_ENDPOINT).buildClient();
        List<String> repositories = client.listRepositoryNames().stream().collect(Collectors.toList());
        assertTrue(repositories.stream().anyMatch(HELLO_WORLD_REPOSITORY_NAME::equals));
    }
}
