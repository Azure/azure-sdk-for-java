// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerregistry;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.HashMap;

public class ReadmeSamples {

    public void authenticate() {
        // BEGIN: com.azure.resourcemanager.containerregistry.authenticate
        String armEndpoint = "https://management.<region>.<your-domain>";
        AzureProfile profile = new AzureProfile(getAzureEnvironmentFromArmEndpoint(armEndpoint));
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();
        ContainerRegistryManager manager = ContainerRegistryManager
            .authenticate(credential, profile);
        // END: com.azure.resourcemanager.containerregistry.authenticate
    }

    // BEGIN: com.azure.resourcemanager.containerregistry.getazureenvironment
    private static AzureEnvironment getAzureEnvironmentFromArmEndpoint(String armEndpoint) {
        // Create HTTP client and request
        HttpClient httpClient = HttpClient.createDefault();

        HttpRequest request = new HttpRequest(HttpMethod.GET,
                String.format("%s/metadata/endpoints?api-version=2019-10-01", armEndpoint))
                .setHeader("accept", "application/json");

        // Execute the request and read the response
        HttpResponse response = httpClient.send(request).block();
        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatusCode());
        }
        String body = response.getBodyAsString().block();
        try {
            ArrayNode metadataArray = JacksonAdapter.createDefaultSerializerAdapter()
                    .deserialize(body, ArrayNode.class, SerializerEncoding.JSON);

            if (metadataArray == null || metadataArray.isEmpty()) {
                throw new RuntimeException("Failed to find metadata : " + body);
            }

            JsonNode metadata = metadataArray.iterator().next();
            AzureEnvironment azureEnvironment = new AzureEnvironment(new HashMap<String, String>() {
                {
                    put("managementEndpointUrl", metadata.at("/authentication/audiences/0").asText());
                    put("resourceManagerEndpointUrl", armEndpoint);
                    put("galleryEndpointUrl", metadata.at("/gallery").asText());
                    put("activeDirectoryEndpointUrl", metadata.at("/authentication/loginEndpoint").asText());
                    put("activeDirectoryResourceId", metadata.at("/authentication/audiences/0").asText());
                    put("activeDirectoryGraphResourceId", metadata.at("/graph").asText());
                    put("storageEndpointSuffix", "." + metadata.at("/suffixes/storage").asText());
                    put("keyVaultDnsSuffix", "." + metadata.at("/suffixes/keyVaultDns").asText());
                }
            });
            return azureEnvironment;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new RuntimeException(ioe);
        }
    }
    // END: com.azure.resourcemanager.containerregistry.getazureenvironment
}
