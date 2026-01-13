// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.UtilsImpl;
import com.azure.containers.containerregistry.models.ArtifactArchitecture;
import com.azure.containers.containerregistry.models.ArtifactManifestPlatform;
import com.azure.containers.containerregistry.models.ArtifactManifestProperties;
import com.azure.containers.containerregistry.models.ArtifactOperatingSystem;
import com.azure.containers.containerregistry.models.ArtifactTagProperties;
import com.azure.containers.containerregistry.models.ContainerRepositoryProperties;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.containers.containerregistry.TestUtils.HELLO_WORLD_REPOSITORY_NAME;
import static com.azure.containers.containerregistry.TestUtils.LATEST_TAG_NAME;
import static com.azure.containers.containerregistry.TestUtils.PAGESIZE_1;
import static com.azure.containers.containerregistry.TestUtils.PAGESIZE_2;
import static com.azure.containers.containerregistry.TestUtils.REGISTRY_ENDPOINT;
import static com.azure.containers.containerregistry.TestUtils.REGISTRY_ENDPOINT_PLAYBACK;
import static com.azure.containers.containerregistry.TestUtils.V1_TAG_NAME;
import static com.azure.containers.containerregistry.TestUtils.getAuthority;
import static com.azure.containers.containerregistry.TestUtils.getCredentialByAuthority;
import static com.azure.containers.containerregistry.TestUtils.isSorted;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class ContainerRegistryClientsTestBase extends TestProxyTestBase {

    protected static final ArtifactTagProperties TAG_WRITEABLE_PROPERTIES
        = new ArtifactTagProperties().setDeleteEnabled(false)
            .setListEnabled(true)
            .setReadEnabled(true)
            .setWriteEnabled(true);

    protected static final ArtifactTagProperties DEFAULT_TAG_PROPERTIES
        = new ArtifactTagProperties().setDeleteEnabled(true)
            .setListEnabled(true)
            .setReadEnabled(true)
            .setWriteEnabled(true);

    protected static final ArtifactManifestProperties MANIFEST_WRITEABLE_PROPERTIES
        = new ArtifactManifestProperties().setDeleteEnabled(false)
            .setListEnabled(true)
            .setReadEnabled(true)
            .setWriteEnabled(true);

    protected static final ArtifactManifestProperties DEFAULT_MANIFEST_PROPERTIES
        = new ArtifactManifestProperties().setDeleteEnabled(true)
            .setListEnabled(true)
            .setReadEnabled(true)
            .setWriteEnabled(true);

    protected static final ContainerRepositoryProperties REPO_WRITEABLE_PROPERTIES
        = new ContainerRepositoryProperties().setDeleteEnabled(false)
            .setListEnabled(true)
            .setReadEnabled(true)
            .setWriteEnabled(true);
    //.setTeleportEnabled(false);

    protected static final ContainerRepositoryProperties DEFAULT_REPO_WRITEABLE_PROPERTIES
        = new ContainerRepositoryProperties().setDeleteEnabled(true)
            .setListEnabled(true)
            .setReadEnabled(true)
            .setWriteEnabled(true);
    //.setTeleportEnabled(false);

    @Override
    protected void beforeTest() {
        if (!interceptorManager.isLiveMode()) {
            List<TestProxySanitizer> sanitizers = new ArrayList<>();
            sanitizers.add(new TestProxySanitizer("token=(?<token>[^\\u0026]+)($|\\u0026)", "REDACTED",
                TestProxySanitizerType.BODY_REGEX).setGroupForReplace("token"));
            sanitizers.add(new TestProxySanitizer("service=(?<service>[^\\u0026]+)\\u0026", "REDACTED",
                TestProxySanitizerType.BODY_REGEX).setGroupForReplace("service"));
            sanitizers
                .add(new TestProxySanitizer("WWW-Authenticate", "realm=\\u0022https://(?<realm>[^\\u0022]+)\\u0022",
                    "REDACTED", TestProxySanitizerType.HEADER).setGroupForReplace("realm"));
            sanitizers.add(new TestProxySanitizer("WWW-Authenticate", "service=\\u0022(?<service>[^\\u0022]+)\\u0022",
                "REDACTED", TestProxySanitizerType.HEADER).setGroupForReplace("service"));

            interceptorManager.addSanitizers(sanitizers);
            interceptorManager.removeSanitizers("AZSDK3493", "AZSDK2003");
        }
    }

    ContainerRegistryClientBuilder getContainerRegistryBuilder(HttpClient httpClient) {
        return getContainerRegistryBuilder(httpClient, getCredentialByAuthority(getAuthority(REGISTRY_ENDPOINT)));
    }

    ContainerRegistryClientBuilder getContainerRegistryBuilder(HttpClient httpClient, TokenCredential credential,
        String endpoint) {
        ContainerRegistryClientBuilder builder = new ContainerRegistryClientBuilder().endpoint(getEndpoint(endpoint))
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .credential(credential);

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    ContainerRegistryClientBuilder getContainerRegistryBuilder(HttpClient httpClient, TokenCredential credential) {
        return getContainerRegistryBuilder(httpClient, credential, REGISTRY_ENDPOINT);
    }

    ContainerRegistryContentClientBuilder getContentClientBuilder(String repositoryName, HttpClient httpClient) {
        return getContentClientBuilder(repositoryName, httpClient,
            getCredentialByAuthority(getAuthority(REGISTRY_ENDPOINT)));
    }

    ContainerRegistryContentClientBuilder getContentClientBuilder(String repositoryName, HttpClient httpClient,
        TokenCredential credential) {
        ContainerRegistryContentClientBuilder builder
            = new ContainerRegistryContentClientBuilder().endpoint(getEndpoint(REGISTRY_ENDPOINT))
                .repositoryName(repositoryName)
                .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.HEADERS)
                    .addAllowedHttpHeaderName(UtilsImpl.DOCKER_DIGEST_HEADER_NAME)
                    .addAllowedHttpHeaderName(HttpHeaderName.RANGE)
                    .addAllowedHttpHeaderName(HttpHeaderName.LOCATION)
                    .addAllowedHeaderName("x-recording-id")
                    .addAllowedHeaderName("x-recording-upstream-base-uri")
                    .addAllowedHttpHeaderName(HttpHeaderName.CONTENT_RANGE))
                .credential(credential);

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }
        return builder;
    }

    List<String> getChildArtifacts(Collection<ArtifactManifestPlatform> artifacts) {
        return artifacts.stream()
            .filter(artifact -> artifact.getArchitecture() != null)
            .map(ArtifactManifestPlatform::getDigest)
            .collect(Collectors.toList());
    }

    String getChildArtifactDigest(Collection<ArtifactManifestPlatform> artifacts) {
        return getChildArtifacts(artifacts).get(0);
    }

    static void validateProperties(ContainerRepositoryProperties properties) {
        assertNotNull(properties);
        assertEquals(HELLO_WORLD_REPOSITORY_NAME, properties.getName());
        assertNotNull(properties.getCreatedOn());
        assertNotNull(properties.getLastUpdatedOn());
        assertTrue(properties.getTagCount() >= 0);
        assertTrue(properties.getManifestCount() >= 0);
        assertNotNull(properties.isDeleteEnabled());
        assertNotNull(properties.isListEnabled());
        assertNotNull(properties.isReadEnabled());
        assertNotNull(properties.isWriteEnabled());
        //assertNotNull(properties.isTeleportEnabled());
        assertNotNull(properties.getRegistryLoginServer());
    }

    static void validateProperties(Response<ContainerRepositoryProperties> response) {
        validateResponse(response);
        validateProperties(response.getValue());
    }

    static void validateListArtifacts(Collection<ArtifactManifestProperties> artifacts) {
        assertFalse(artifacts.isEmpty());
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

        assertTrue(artifacts.stream()
            .anyMatch(a -> ArtifactArchitecture.AMD64.equals(a.getArchitecture())
                && ArtifactOperatingSystem.WINDOWS.equals(a.getOperatingSystem())));
        assertTrue(artifacts.stream()
            .anyMatch(a -> ArtifactArchitecture.ARM.equals(a.getArchitecture())
                && ArtifactOperatingSystem.LINUX.equals(a.getOperatingSystem())));
    }

    static void validateListArtifactsByPage(Collection<PagedResponse<ArtifactManifestProperties>> pagedResList) {
        validateListArtifactsByPage(pagedResList, false);
    }

    static void validateListArtifactsByPage(Collection<PagedResponse<ArtifactManifestProperties>> pagedResList,
        boolean isOrdered) {
        List<ArtifactManifestProperties> props = new ArrayList<>();
        pagedResList.forEach(res -> props.addAll(res.getValue()));
        List<OffsetDateTime> lastUpdatedOn
            = props.stream().map(ArtifactManifestProperties::getLastUpdatedOn).collect(Collectors.toList());

        validateListArtifacts(props);
        assertTrue(pagedResList.stream().allMatch(res -> res.getValue().size() <= PAGESIZE_2),
            "All pages were expected to be less than or equal to " + PAGESIZE_2 + ", but received page sizes of: "
                + Arrays.toString(pagedResList.stream().mapToInt(res -> res.getValue().size()).toArray()));
        if (isOrdered) {
            assertTrue(isSorted(lastUpdatedOn), "Expected last updated on to return sorted, but was "
                + Arrays.toString(lastUpdatedOn.toArray(new OffsetDateTime[0])));
        }
    }

    static void validateListTags(Collection<PagedResponse<ArtifactTagProperties>> pagedResList, boolean isOrdered) {
        List<ArtifactTagProperties> props = new ArrayList<>();
        pagedResList.forEach(res -> props.addAll(res.getValue()));
        List<OffsetDateTime> lastUpdatedOn
            = props.stream().map(ArtifactTagProperties::getLastUpdatedOn).collect(Collectors.toList());

        validateListTags(props);
        assertTrue(pagedResList.stream().allMatch(res -> res.getValue().size() <= PAGESIZE_2),
            "All pages were expected to be less than or equal to " + PAGESIZE_2 + ", but received page sizes of: "
                + Arrays.toString(pagedResList.stream().mapToInt(res -> res.getValue().size()).toArray()));
        if (isOrdered) {
            assertTrue(isSorted(lastUpdatedOn), "Expected last updated on to return sorted, but was "
                + Arrays.toString(lastUpdatedOn.toArray(new OffsetDateTime[0])));
        }
    }

    static void validateRepositories(Collection<String> repositories, List<String> expected) {
        assertNotNull(repositories);
        assertTrue(repositories.containsAll(expected), "Expected repositories to contain '"
            + String.join(", ", expected) + "', but it didn't, was: '" + String.join(", ", repositories));
    }

    static void validateRepositoriesByPage(Collection<PagedResponse<String>> pagedResList, List<String> expected) {
        List<String> props = new ArrayList<>();
        pagedResList.forEach(res -> props.addAll(res.getValue()));

        assertTrue(pagedResList.stream().allMatch(res -> res.getValue().size() <= PAGESIZE_1),
            "All pages were expected to be less than or equal to " + PAGESIZE_1 + ", but received page sizes of: "
                + Arrays.toString(pagedResList.stream().mapToInt(res -> res.getValue().size()).toArray()));
        validateRepositories(props, expected);
    }

    static void validateManifestProperties(Response<ArtifactManifestProperties> response, boolean isChild) {
        validateResponse(response);
        validateManifestProperties(response.getValue(), isChild);
    }

    static void validateManifestProperties(ArtifactManifestProperties props, boolean isChild) {
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
            props.getRelatedArtifacts().forEach(prop -> {
                assertNotNull(prop.getDigest());
                assertNotNull(prop.getArchitecture());
                assertNotNull(prop.getOperatingSystem());
            });
        }
    }

    static void validateListTags(Collection<ArtifactTagProperties> tags) {
        assertFalse(tags.isEmpty());
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

        assertTrue(tags.stream().anyMatch(tag -> V1_TAG_NAME.equals(tag.getName())),
            "Expected tags to contain a tag with name '" + V1_TAG_NAME + "' but there wasn't.");
        assertTrue(tags.stream().anyMatch(tag -> LATEST_TAG_NAME.equals(tag.getName())),
            "Expected tags to contain a tag with name '" + LATEST_TAG_NAME + "' but there wasn't.");
    }

    static void validateTagProperties(ArtifactTagProperties props, String tagName) {
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

    static void validateResponse(Response<?> response) {
        assertNotNull(response);
        assertTrue(response.getStatusCode() >= 0);
        assertNotNull(response.getHeaders());
        assertNotNull(response.getRequest());
        assertNotNull(response.getValue());
    }

    static void validateTagProperties(Response<ArtifactTagProperties> response, String tagName) {
        validateResponse(response);
        validateTagProperties(response.getValue(), tagName);
    }

    static void validateRepoContentProperties(ContainerRepositoryProperties properties) {
        assertNotNull(properties);
        assertFalse(properties.isDeleteEnabled(), "isDelete incorrect");
        assertTrue(properties.isListEnabled(), "isList incorrect");
        assertTrue(properties.isReadEnabled(), "isRead incorrect");
        assertTrue(properties.isWriteEnabled(), "isWrite incorrect");
        //assertFalse(properties.isTeleportEnabled(), "isTeleport incorrect");
    }

    static void validateTagContentProperties(ArtifactTagProperties properties) {
        assertNotNull(properties);
        assertFalse(properties.isDeleteEnabled(), "isDelete incorrect");
        assertTrue(properties.isListEnabled(), "isList incorrect");
        assertTrue(properties.isReadEnabled(), "isRead incorrect");
        assertTrue(properties.isWriteEnabled(), "isWrite incorrect");
    }

    static void validateManifestContentProperties(ArtifactManifestProperties properties) {
        assertNotNull(properties);
        assertFalse(properties.isDeleteEnabled(), "isDelete incorrect");
        assertTrue(properties.isListEnabled(), "isList incorrect");
        assertTrue(properties.isReadEnabled(), "isRead incorrect");
        assertTrue(properties.isWriteEnabled(), "isWrite incorrect");
    }

    protected String getEndpoint(String endpoint) {
        return interceptorManager.isPlaybackMode() ? REGISTRY_ENDPOINT_PLAYBACK : endpoint;
    }
}
