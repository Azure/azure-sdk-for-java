// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ContentProperties;
import com.azure.containers.containerregistry.models.DeleteRepositoryResult;
import com.azure.containers.containerregistry.models.RegistryArtifactProperties;
import com.azure.containers.containerregistry.models.RepositoryProperties;
import com.azure.containers.containerregistry.models.TagProperties;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.azure.containers.containerregistry.TestUtils.AMD64_ARCHITECTURE;
import static com.azure.containers.containerregistry.TestUtils.ARM64_ARCHITECTURE;
import static com.azure.containers.containerregistry.TestUtils.HELLO_WORLD_REPOSITORY_NAME;
import static com.azure.containers.containerregistry.TestUtils.LATEST_TAG_NAME;
import static com.azure.containers.containerregistry.TestUtils.LINUX_OPERATING_SYSTEM;
import static com.azure.containers.containerregistry.TestUtils.SLEEP_TIME_IN_MILLISECONDS;
import static com.azure.containers.containerregistry.TestUtils.V1_TAG_NAME;
import static com.azure.containers.containerregistry.TestUtils.WINDOWS_OPERATING_SYSTEM;
import static com.azure.containers.containerregistry.TestUtils.getCredential;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContainerRegistryClientsTestBase extends TestBase {

    private static final String AZURE_CONTAINERREGISTRY_ENDPOINT = "CONTAINERREGISTRY_ENDPOINT";

    private static final Pattern JSON_PROPERTY_VALUE_REDACTION_PATTERN
        = Pattern.compile("(\".*_token\":\"(.*)\".*)");

    protected static ContentProperties writeableProperties = new ContentProperties()
        .setCanDelete(false)
        .setCanList(true)
        .setCanRead(true)
        .setCanWrite(true);

    protected static ContentProperties defaultProperties = new ContentProperties()
        .setCanDelete(true)
        .setCanList(true)
        .setCanRead(true)
        .setCanWrite(true);

    ContainerRegistryClientBuilder getContainerRegistryBuilder(HttpClient httpClient) {
        List<Function<String, String>> redactors = new ArrayList<>();
        redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));

        ContainerRegistryClientBuilder builder = new ContainerRegistryClientBuilder()
            .endpoint(getEndpoint())
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .addPolicy(interceptorManager.getRecordPolicy(redactors))
            .credential(getCredential(getTestMode()));

        return builder;
    }

    List<String> getChildArtifacts(ContainerRepositoryClient client) {
        ArrayList<RegistryArtifactProperties> artifacts = new ArrayList<>();
        client.listRegistryArtifacts().forEach(repo -> artifacts.add(repo));

        return getChildArtifacts(artifacts);
    }

    List<String> getChildArtifacts(Collection<RegistryArtifactProperties> artifacts) {
        return artifacts.stream().filter(artifact -> {
            ContentProperties props = artifact.getWriteableProperties();
            return props.hasCanDelete() && props.hasCanWrite() && artifact.getCpuArchitecture() != null;
        }).map(s -> s.getDigest()).collect(Collectors.toList());
    }

    String getChildArtifactDigest(Collection<RegistryArtifactProperties> artifacts) {
        return getChildArtifacts(artifacts).get(0);
    }

    void validateDeletedRepositoryResponse(DeleteRepositoryResult response) {
        assertNotNull(response);
        assertNotNull(response.getDeletedTags());
        assertNotNull(response.getDeletedRegistryArtifactDigests());
//        assertEquals(expectedArtifactCount, response.getDeletedRegistryArtifactDigests().size());
//        assertEquals(expectedTagCount, response.getDeletedTags().size());
    }

    void validateProperties(RepositoryProperties properties) {
        assertNotNull(properties);
        assertEquals(HELLO_WORLD_REPOSITORY_NAME, properties.getName());
        assertNotNull(properties.getCreatedOn());
        assertNotNull(properties.getLastUpdatedOn());
        assertNotNull(properties.getTagCount());
        assertNotNull(properties.getRegistryArtifactCount());
        assertNotNull(properties.getWriteableProperties());
    }

    void validateProperties(Response<RepositoryProperties> response) {
        validateResponse(response);
        validateProperties(response.getValue());
    }

    void validateListArtifacts(Collection<RegistryArtifactProperties> artifacts)
    {
        artifacts.forEach(props -> {
            assertNotNull(props.getDigest());
            assertNotNull(props.getCreatedOn());
            assertNotNull(props.getLastUpdatedOn());
            assertNotNull(props.getWriteableProperties());
        });

        assertTrue(artifacts.stream().anyMatch(prop -> prop.getTags() != null));
        assertTrue(artifacts.stream().anyMatch(a -> ARM64_ARCHITECTURE.equals(a.getCpuArchitecture())
            && LINUX_OPERATING_SYSTEM.equals(a.getOperatingSystem())));
        assertTrue(artifacts.stream().anyMatch(a -> AMD64_ARCHITECTURE.equals(a.getCpuArchitecture())
            && WINDOWS_OPERATING_SYSTEM.equals(a.getOperatingSystem())));
    }

    void validateArtifactProperties(Response<RegistryArtifactProperties> response, boolean hasTag, boolean isChild) {
        validateResponse(response);
        validateArtifactProperties(response.getValue(), hasTag, isChild);
    }

    void validateArtifactProperties(RegistryArtifactProperties props, boolean hasTag, boolean isChild) {
        assertNotNull(props);
        assertNotNull(props.getWriteableProperties());
        assertNotNull(props.getDigest());
        assertNotNull(props.getLastUpdatedOn());
        assertNotNull(props.getCreatedOn());
        assertNotNull(props.getSize());

        if (isChild) {
            assertNotNull(props.getCpuArchitecture());
            assertNotNull(props.getOperatingSystem());
        }
        else {
            assertNotNull(props.getTags());
            assertNotNull(props.getRegistryArtifacts());
            props.getRegistryArtifacts().stream().forEach(prop -> {
                assertNotNull(prop.getDigest());
                assertNotNull(prop.getCpuArchitecture());
                assertNotNull(prop.getOperatingSystem());
            });
        }
    }

    void validateListTags(Collection<TagProperties> tags)
    {
        tags.forEach(props -> {
            assertEquals(HELLO_WORLD_REPOSITORY_NAME, props.getRepository());
            assertNotNull(props.getName());
            assertNotNull(props.getDigest());
            assertNotNull(props.getCreatedOn());
            assertNotNull(props.getWriteableProperties());
            assertNotNull(props.getLastUpdatedOn());
        });

        tags.stream().anyMatch(tag -> V1_TAG_NAME.equals(tag.getName()));
        tags.stream().anyMatch(tag -> LATEST_TAG_NAME.equals(tag.getName()));
    }

    void validateTagProperties(TagProperties props, String tagName) {
        assertNotNull(props);
        assertNotNull(props.getLastUpdatedOn());
        assertNotNull(props.getWriteableProperties());
        assertNotNull(props.getDigest());
        assertEquals(tagName, props.getName());
        assertEquals(HELLO_WORLD_REPOSITORY_NAME, props.getRepository());
        assertNotNull(props.getCreatedOn());
    }

    <T> void validateResponse(Response<T> response) {
        assertNotNull(response);
        assertNotNull(response.getStatusCode());
        assertNotNull(response.getHeaders());
        assertNotNull(response.getRequest());
        assertNotNull(response.getValue());
    }

    void validateTagProperties(Response<TagProperties> response, String tagName) {
        validateResponse(response);
        validateTagProperties(response.getValue(), tagName);
    }

    void validateContentProperties(ContentProperties properties) {
        assertNotNull(properties);
        assertEquals(false, properties.hasCanDelete(), "canDelete incorrect");
        assertEquals(true, properties.hasCanList(), "canList incorrect");
        assertEquals(true, properties.hasCanRead(), "canRead incorrect");
        assertEquals(true, properties.hasCanWrite(), "canWrite incorrect");
    }

    void importImage(String repositoryName, List<String> tags) {
        TestUtils.importImage(getTestMode(), repositoryName, tags);
    }

    ContainerRepositoryClientBuilder getContainerRepositoryBuilder(String repositoryName, HttpClient httpClient) {
        List<Function<String, String>> redactors = new ArrayList<>();
        redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));

        ContainerRepositoryClientBuilder builder = new ContainerRepositoryClientBuilder()
            .endpoint(getEndpoint())
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY))
            .addPolicy(interceptorManager.getRecordPolicy(redactors))
            .repository(repositoryName)
            .credential(getCredential(getTestMode()));

        return builder;
    }

    protected String getEndpoint() {
        return interceptorManager.isPlaybackMode() ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get(AZURE_CONTAINERREGISTRY_ENDPOINT);
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

    void testDelay() {
        if (getTestMode() != TestMode.PLAYBACK) {
            // The service has a cache of 6 seconds, so we need to wait until we run this.
            try {
                Thread.sleep(SLEEP_TIME_IN_MILLISECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    Mono<Long> monoDelay() {
        return monoDelay(SLEEP_TIME_IN_MILLISECONDS);
    }

    Mono<Long> monoDelay(long delayInMs) {
        return TestUtils.monoDelay(getTestMode());
    }
}
