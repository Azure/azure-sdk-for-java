// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ArtifactArchitecture;
import com.azure.containers.containerregistry.models.ArtifactManifestPlatform;
import com.azure.containers.containerregistry.models.ArtifactManifestProperties;
import com.azure.containers.containerregistry.models.ArtifactOperatingSystem;
import com.azure.containers.containerregistry.models.ArtifactTagProperties;
import com.azure.containers.containerregistry.models.ContainerRegistryAudience;
import com.azure.containers.containerregistry.models.ContainerRepositoryProperties;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.core.util.CoreUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.azure.containers.containerregistry.TestUtils.HELLO_WORLD_REPOSITORY_NAME;
import static com.azure.containers.containerregistry.TestUtils.LATEST_TAG_NAME;
import static com.azure.containers.containerregistry.TestUtils.PAGESIZE_1;
import static com.azure.containers.containerregistry.TestUtils.PAGESIZE_2;
import static com.azure.containers.containerregistry.TestUtils.REGISTRY_ENDPOINT;
import static com.azure.containers.containerregistry.TestUtils.REGISTRY_ENDPOINT_PLAYBACK;
import static com.azure.containers.containerregistry.TestUtils.V1_TAG_NAME;
import static com.azure.containers.containerregistry.TestUtils.getCredentialsByEndpoint;
import static com.azure.containers.containerregistry.TestUtils.isSorted;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContainerRegistryClientsTestBase extends TestBase {

    private static final Pattern JSON_PROPERTY_VALUE_REDACTION_PATTERN
        = Pattern.compile("(\".*_token\":\"(.*)\".*)");

    protected static ArtifactTagProperties tagWriteableProperties = new ArtifactTagProperties()
        .setDeleteEnabled(false)
        .setListEnabled(true)
        .setReadEnabled(true)
        .setWriteEnabled(true);

    protected static ArtifactTagProperties defaultTagProperties = new ArtifactTagProperties()
        .setDeleteEnabled(true)
        .setListEnabled(true)
        .setReadEnabled(true)
        .setWriteEnabled(true);

    protected static ArtifactManifestProperties manifestWriteableProperties = new ArtifactManifestProperties()
        .setDeleteEnabled(false)
        .setListEnabled(true)
        .setReadEnabled(true)
        .setWriteEnabled(true);

    protected static ArtifactManifestProperties defaultManifestProperties = new ArtifactManifestProperties()
        .setDeleteEnabled(true)
        .setListEnabled(true)
        .setReadEnabled(true)
        .setWriteEnabled(true);


    protected static ContainerRepositoryProperties repoWriteableProperties = new ContainerRepositoryProperties()
        .setDeleteEnabled(false)
        .setListEnabled(true)
        .setReadEnabled(true)
        .setWriteEnabled(true)
        .setTeleportEnabled(false);

    protected static ContainerRepositoryProperties defaultRepoWriteableProperties = new ContainerRepositoryProperties()
        .setDeleteEnabled(true)
        .setListEnabled(true)
        .setReadEnabled(true)
        .setWriteEnabled(true)
        .setTeleportEnabled(false);

    ContainerRegistryClientBuilder getContainerRegistryBuilder(HttpClient httpClient) {
        TokenCredential credential = getCredentialsByEndpoint(getTestMode(), REGISTRY_ENDPOINT);
        return getContainerRegistryBuilder(httpClient, credential);
    }

    ContainerRegistryClientBuilder getContainerRegistryBuilder(HttpClient httpClient, TokenCredential credential, String endpoint) {
        List<Function<String, String>> redactors = new ArrayList<>();
        redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));

        ContainerRegistryAudience audience = TestUtils.getAudience(endpoint);

        ContainerRegistryClientBuilder builder = new ContainerRegistryClientBuilder()
            .endpoint(getEndpoint(endpoint))
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .addPolicy(interceptorManager.getRecordPolicy(redactors))
            .credential(credential)
            .audience(audience);

           // builder.httpClient(new NettyAsyncHttpClientBuilder().proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888))).build());
        return builder;
    }

    ContainerRegistryClientBuilder getContainerRegistryBuilder(HttpClient httpClient, TokenCredential credential) {
        return getContainerRegistryBuilder(httpClient, credential, REGISTRY_ENDPOINT);
    }

    List<String> getChildArtifacts(Collection<ArtifactManifestPlatform> artifacts) {
        return artifacts.stream()
            .filter(artifact -> artifact.getArchitecture() != null)
            .map(s -> s.getDigest()).collect(Collectors.toList());
    }

    String getChildArtifactDigest(Collection<ArtifactManifestPlatform> artifacts) {
        return getChildArtifacts(artifacts).get(0);
    }

    void validateProperties(ContainerRepositoryProperties properties) {
        assertNotNull(properties);
        assertEquals(HELLO_WORLD_REPOSITORY_NAME, properties.getName());
        assertNotNull(properties.getCreatedOn());
        assertNotNull(properties.getLastUpdatedOn());
        assertNotNull(properties.getTagCount());
        assertNotNull(properties.getManifestCount());
        assertNotNull(properties.isDeleteEnabled());
        assertNotNull(properties.isListEnabled());
        assertNotNull(properties.isReadEnabled());
        assertNotNull(properties.isWriteEnabled());
        assertNotNull(properties.isTeleportEnabled());
        assertNotNull(properties.getRegistryLoginServer());
    }

    void validateProperties(Response<ContainerRepositoryProperties> response) {
        validateResponse(response);
        validateProperties(response.getValue());
    }

    void validateListArtifacts(Collection<ArtifactManifestProperties> artifacts) {
        assertTrue(artifacts.size() > 0);
        artifacts.forEach(props -> {
            assertNotNull(props.getDigest());
            assertNotNull(props.getCreatedOn());
            assertNotNull(props.getLastUpdatedOn());
            assertNotNull(props.isDeleteEnabled());
            assertNotNull(props.isListEnabled());
            assertNotNull(props.isReadEnabled());
            assertNotNull(props.isWriteEnabled());
        });

        assertTrue(artifacts.stream().anyMatch(prop -> prop.getTags() != null));
        assertTrue(artifacts.stream().anyMatch(a -> ArtifactArchitecture.AMD64.equals(a.getArchitecture())
            && ArtifactOperatingSystem.WINDOWS.equals(a.getOperatingSystem())));
        assertTrue(artifacts.stream().anyMatch(a -> ArtifactArchitecture.ARM.equals(a.getArchitecture())
            && ArtifactOperatingSystem.LINUX.equals(a.getOperatingSystem())));
    }

    boolean validateListArtifactsByPage(Collection<PagedResponse<ArtifactManifestProperties>> pagedResList) {
        return validateListArtifactsByPage(pagedResList, false);
    }

    boolean validateListArtifactsByPage(Collection<PagedResponse<ArtifactManifestProperties>> pagedResList, boolean isOrdered) {
        List<ArtifactManifestProperties> props = new ArrayList<>();
        pagedResList.forEach(res -> res.getValue().forEach(prop -> props.add(prop)));
        List<OffsetDateTime> lastUpdatedOn = props.stream().map(artifact -> artifact.getLastUpdatedOn()).collect(Collectors.toList());

        validateListArtifacts(props);
        return pagedResList.stream().allMatch(res -> res.getValue().size() <= PAGESIZE_2)
            && (!isOrdered || isSorted(lastUpdatedOn));
    }

    boolean validateListTags(Collection<PagedResponse<ArtifactTagProperties>> pagedResList, boolean isOrdered) {
        List<ArtifactTagProperties> props = new ArrayList<>();
        pagedResList.forEach(res -> res.getValue().forEach(prop -> props.add(prop)));
        List<OffsetDateTime> lastUpdatedOn = props.stream().map(artifact -> artifact.getLastUpdatedOn()).collect(Collectors.toList());

        return validateListTags(props)
            && pagedResList.stream().allMatch(res -> res.getValue().size() <= PAGESIZE_2)
            && (!isOrdered || isSorted(lastUpdatedOn));
    }

    boolean validateRepositories(Collection<String> repositories) {
        assertNotNull(repositories);
        return repositories.containsAll(Arrays.asList(TestUtils.HELLO_WORLD_REPOSITORY_NAME, TestUtils.ALPINE_REPOSITORY_NAME));
    }

    boolean validateRepositoriesByPage(Collection<PagedResponse<String>> pagedResList) {
        List<String> props = new ArrayList<>();
        pagedResList.forEach(res -> res.getValue().forEach(prop -> props.add(prop)));

        return pagedResList.stream().allMatch(res -> res.getValue().size() <= PAGESIZE_1)
            && validateRepositories(props);
    }

    void validateManifestProperties(Response<ArtifactManifestProperties> response, boolean hasTag, boolean isChild) {
        validateResponse(response);
        validateManifestProperties(response.getValue(), hasTag, isChild);
    }

    void validateManifestProperties(ArtifactManifestProperties props, boolean hasTag, boolean isChild) {
        assertNotNull(props);
        assertNotNull(props.getRepositoryName());
        assertNotNull(props.getRegistryLoginServer());
        assertNotNull(props.getDigest());
        assertNotNull(props.getLastUpdatedOn());
        assertNotNull(props.getCreatedOn());
        assertNotNull(props.getSizeInBytes());
        assertNotNull(props.isDeleteEnabled());
        assertNotNull(props.isListEnabled());
        assertNotNull(props.isReadEnabled());
        assertNotNull(props.isWriteEnabled());

        if (isChild) {
            assertNotNull(props.getArchitecture());
            assertNotNull(props.getOperatingSystem());
        } else {
            assertNotNull(props.getTags());
            assertNotNull(props.getRelatedArtifacts());
            props.getRelatedArtifacts().stream().forEach(prop -> {
                assertNotNull(prop.getDigest());
                assertNotNull(prop.getArchitecture());
                assertNotNull(prop.getOperatingSystem());
            });
        }
    }

    boolean validateListTags(Collection<ArtifactTagProperties> tags) {
        assertTrue(tags.size() > 0);
        tags.forEach(props -> {
            assertEquals(HELLO_WORLD_REPOSITORY_NAME, props.getRepositoryName());
            assertNotNull(props.getName());
            assertNotNull(props.getDigest());
            assertNotNull(props.getCreatedOn());
            assertNotNull(props.isDeleteEnabled());
            assertNotNull(props.isListEnabled());
            assertNotNull(props.isReadEnabled());
            assertNotNull(props.isWriteEnabled());
            assertNotNull(props.getLastUpdatedOn());
        });

        return tags.stream().anyMatch(tag -> V1_TAG_NAME.equals(tag.getName()))
            && tags.stream().anyMatch(tag -> LATEST_TAG_NAME.equals(tag.getName()));
    }

    void validateTagProperties(ArtifactTagProperties props, String tagName) {
        assertNotNull(props);
        assertNotNull(props.getLastUpdatedOn());
        assertNotNull(props.isDeleteEnabled());
        assertNotNull(props.isListEnabled());
        assertNotNull(props.isReadEnabled());
        assertNotNull(props.isWriteEnabled());
        assertNotNull(props.getDigest());
        assertNotNull(props.getRegistryLoginServer());
        assertEquals(tagName, props.getName());
        assertEquals(HELLO_WORLD_REPOSITORY_NAME, props.getRepositoryName());
        assertNotNull(props.getCreatedOn());
    }

    <T> void validateResponse(Response<T> response) {
        assertNotNull(response);
        assertNotNull(response.getStatusCode());
        assertNotNull(response.getHeaders());
        assertNotNull(response.getRequest());
        assertNotNull(response.getValue());
    }

    void validateTagProperties(Response<ArtifactTagProperties> response, String tagName) {
        validateResponse(response);
        validateTagProperties(response.getValue(), tagName);
    }

    void validateRepoContentProperties(ContainerRepositoryProperties properties) {
        assertNotNull(properties);
        assertEquals(false, properties.isDeleteEnabled(), "isDelete incorrect");
        assertEquals(true, properties.isListEnabled(), "isList incorrect");
        assertEquals(true, properties.isReadEnabled(), "isRead incorrect");
        assertEquals(true, properties.isWriteEnabled(), "isWrite incorrect");
        assertEquals(false, properties.isTeleportEnabled(), "isTeleport incorrect");
    }

    void validateTagContentProperties(ArtifactTagProperties properties) {
        assertNotNull(properties);
        assertEquals(false, properties.isDeleteEnabled(), "isDelete incorrect");
        assertEquals(true, properties.isListEnabled(), "isList incorrect");
        assertEquals(true, properties.isReadEnabled(), "isRead incorrect");
        assertEquals(true, properties.isWriteEnabled(), "isWrite incorrect");
    }

    void validateManifestContentProperties(ArtifactManifestProperties properties) {
        assertNotNull(properties);
        assertEquals(false, properties.isDeleteEnabled(), "isDelete incorrect");
        assertEquals(true, properties.isListEnabled(), "isList incorrect");
        assertEquals(true, properties.isReadEnabled(), "isRead incorrect");
        assertEquals(true, properties.isWriteEnabled(), "isWrite incorrect");
    }

    protected String getEndpoint(String endpoint) {
        return interceptorManager.isPlaybackMode() ? REGISTRY_ENDPOINT_PLAYBACK
            : endpoint;
    }

    private String redact(String content, Matcher matcher, String replacement) {
        while (matcher.find()) {
            if (matcher.groupCount() == 2) {
                String captureGroup = matcher.group(1);
                if (!CoreUtils.isNullOrEmpty(captureGroup)) {
                    content = content.replace(matcher.group(2), replacement);
                }
            }
        }

        return content;
    }
}
