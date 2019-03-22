// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig;

import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.applicationconfig.models.KeyValueListFilter;
import com.azure.applicationconfig.models.RevisionFilter;
import com.azure.common.http.rest.RestException;
import com.microsoft.azure.core.InterceptorManager;
import com.microsoft.azure.core.TestMode;
import com.microsoft.azure.utils.SdkContext;
import com.azure.common.http.HttpClient;
import com.azure.common.http.policy.HttpLogDetailLevel;
import com.azure.common.http.rest.RestResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ConfigurationClientTest {
    private static final String PLAYBACK_URI_BASE = "http://localhost:";

    private final Logger logger = LoggerFactory.getLogger(ConfigurationClientTest.class);

    private InterceptorManager interceptorManager;
    private ConfigurationClient client;
    private String keyPrefix;

    @Rule
    public TestName testName = new TestName();

    @Before
    public void beforeTest() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        final TestMode testMode = getTestMode();

        interceptorManager = InterceptorManager.create(testName.getMethodName(), testMode);

        if (interceptorManager.isPlaybackMode()) {
            logger.info("PLAYBACK MODE");

            final String playbackUri = getPlaybackUri(testMode);
            final String connectionString = "endpoint=" + playbackUri + ";Id=0000000000000;Secret=MDAwMDAw";

            client = ConfigurationClient.builder()
                    .credentials(new ConfigurationClientCredentials(connectionString))
                    .httpClient(interceptorManager.getPlaybackClient())
                    .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                    .build();
        } else {
            logger.info("RECORD MODE");

            final String connectionString = System.getenv("AZCONFIG_CONNECTION_STRING");
            Objects.requireNonNull(connectionString, "AZCONFIG_CONNECTION_STRING expected to be set.");

            client = ConfigurationClient.builder()
                    .credentials(new ConfigurationClientCredentials(connectionString))
                    .httpClient(HttpClient.createDefault().wiretap(true))
                    .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                    .addPolicy(interceptorManager.getRecordPolicy())
                    .build();
        }

        keyPrefix = SdkContext.randomResourceName("key", 8);
    }

    private static String getPlaybackUri(TestMode testMode) throws IOException {
        if (testMode == TestMode.RECORD) {
            Properties mavenProps = new Properties();

            try (InputStream in = ConfigurationClientTest.class.getResourceAsStream("/maven.properties")) {
                if (in == null) {
                    throw new IOException(
                            "The file \"maven.properties\" has not been generated yet. Please execute \"mvn compile\" to generate the file.");
                }
                mavenProps.load(in);
            }

            String port = mavenProps.getProperty("playbackServerPort");
            // 11080 and 11081 needs to be in sync with values in jetty.xml file
            return PLAYBACK_URI_BASE + port;
        } else {
            return PLAYBACK_URI_BASE + "1234";
        }
    }

    private TestMode getTestMode() throws IllegalArgumentException {
        final String azureTestMode = System.getenv("AZURE_TEST_MODE");

        if (azureTestMode != null) {
            try {
                return TestMode.valueOf(azureTestMode.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                logger.error("Could not parse '{}' into TestEnum.", azureTestMode);
                throw e;
            }
        } else {
            logger.info("Environment variable 'AZURE_TEST_MODE' has not been set yet. Using 'Playback' mode.");
            return TestMode.PLAYBACK;
        }
    }

    @After
    public void afterTest() {
        cleanUpResources();
        interceptorManager.close();
    }

    private void cleanUpResources() {
        logger.info("Cleaning up created key values.");
        client.listKeyValues(new KeyValueListFilter().withKey(keyPrefix + "*"))
                .flatMap(configurationSetting -> {
                    logger.info("Deleting key:label [{}:{}]. isLocked? {}", configurationSetting.key(), configurationSetting.label(), configurationSetting.isLocked());

                    if (configurationSetting.isLocked()) {
                        return client.unlock(configurationSetting.key(), configurationSetting.label()).flatMap(response -> {
                            ConfigurationSetting kv = response.body();
                            return client.delete(kv.key(), kv.label(), null)
                                    .retryBackoff(3, Duration.ofSeconds(10));
                        });
                    } else {
                        return client.delete(configurationSetting.key(), configurationSetting.label(), null)
                                .retryBackoff(3, Duration.ofSeconds(10));
                    }
                })
                .blockLast();

        logger.info("Finished cleaning up values.");
    }

    /**
     * Verifies that we can add a ConfigurationSetting then delete it.
     */
    @Test
    public void addAndDeleteSetting() {
        final String key = SdkContext.randomResourceName(keyPrefix, 8);
        final Map<String, String> tags = new HashMap<>();
        tags.put("MyTag", "TagValue");
        final ConfigurationSetting newConfigurationSetting = new ConfigurationSetting()
                .withKey(key)
                .withValue("myNewValue5")
                .withTags(tags)
                .withContentType("text");

        StepVerifier.create(client.set(newConfigurationSetting))
                .assertNext(response -> assertConfigurationEquals(newConfigurationSetting, response))
                .verifyComplete();

        StepVerifier.create(client.delete(newConfigurationSetting.key()))
                .assertNext(response -> assertConfigurationEquals(newConfigurationSetting, response))
                .verifyComplete();
    }

    /**
     * Verifies that we can get the appropriate ConfigurationSetting when there are two settings with the same key but
     * different labels.
     */
    @Test
    public void getWithLabel() {
        final String label = "myLabel";
        final String key = SdkContext.randomResourceName(keyPrefix, 16);
        final ConfigurationSetting kv = new ConfigurationSetting().withKey(key).withValue("myValue").withLabel(label);
        final ConfigurationSetting kv2 = new ConfigurationSetting().withKey(key).withValue("someOtherValue").withLabel("myLabel2");

        StepVerifier.create(client.set(kv))
                .assertNext(response -> assertConfigurationEquals(kv, response))
                .verifyComplete();

        StepVerifier.create(client.set(kv2))
                .assertNext(response -> assertConfigurationEquals(kv2, response))
                .verifyComplete();

        StepVerifier.create(client.get(key, label))
                .assertNext(response -> assertConfigurationEquals(kv, response))
                .verifyComplete();
    }

    /**
     * Verifies that the service returns a 404 when the key-label pair does not exist.
     */
    @Test
    public void getNotFound() {
        final String label = "myLabel";
        final String key = SdkContext.randomResourceName(keyPrefix, 16);
        final ConfigurationSetting kv = new ConfigurationSetting().withKey(key).withValue("myValue").withLabel(label);

        StepVerifier.create(client.set(kv))
                .assertNext(response -> assertConfigurationEquals(kv, response))
                .verifyComplete();

        StepVerifier.create(client.get(key, "myNonExistingLabel"))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof RestException);
                    assertEquals(404, ((RestException) error).response().statusCode());
                })
                .verify();
    }

    /**
     * Verifies that we can lock and unlock a ConfigurationSetting.
     */
    @Test
    public void lockUnlockSetting() {
        final String keyName = SdkContext.randomResourceName(keyPrefix, 16);
        final ConfigurationSetting expected = new ConfigurationSetting().withKey(keyName).withValue("myKeyValue");
        final ConfigurationSetting updated = new ConfigurationSetting().withKey(keyName).withValue("Some new value");
        final ConfigurationSetting updated2 = new ConfigurationSetting().withKey(keyName).withValue("Some new value, again.");

        StepVerifier.create(client.set(expected))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .expectComplete()
                .verify();

        StepVerifier.create(client.lock(expected.key()))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .expectComplete()
                .verify();

        StepVerifier.create(client.set(updated))
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof RestException);
                    assertEquals(HttpResponseStatus.CONFLICT.code(), ((RestException) ex).response().statusCode());
                }).verify();

        StepVerifier.create(client.unlock(keyName).flatMap(response -> client.set(updated2)))
                .assertNext(response -> assertConfigurationEquals(updated2, response))
                .expectComplete()
                .verify();
    }

    /**
     * Verifies that a ConfigurationSetting can be added with a label, and that we can fetch that ConfigurationSetting from the service when
     * filtering by either its label or just its key.
     */
    @Test
    public void listWithKeyAndLabel() {
        final String value = "myValue";
        final String key = SdkContext.randomResourceName(keyPrefix, 16);
        final String label = SdkContext.randomResourceName("lbl", 8);
        final ConfigurationSetting expected = new ConfigurationSetting().withKey(key).withValue(value).withLabel(label);

        StepVerifier.create(client.set(expected))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .expectComplete()
                .verify();

        StepVerifier.create(client.listKeyValues(new KeyValueListFilter().withKey(key).withLabel(label)))
                .assertNext(configurationSetting -> assertConfigurationEquals(expected, configurationSetting))
                .expectComplete()
                .verify();

        StepVerifier.create(client.listKeyValues(new KeyValueListFilter().withKey(key)))
                .assertNext(configurationSetting -> assertConfigurationEquals(expected, configurationSetting))
                .expectComplete()
                .verify();
    }

    /**
     * Verifies that we can get all of the revisions for this ConfigurationSetting.
     */
    @Test
    public void listRevisions() {
        final String keyName = SdkContext.randomResourceName(keyPrefix, 16);
        final ConfigurationSetting original = new ConfigurationSetting().withKey(keyName).withValue("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting().withKey(keyName).withValue("anotherValue");
        final HashSet<String> expected = new HashSet<>();
        expected.add(original.value());
        expected.add(updated.value());

        // Create two different revisions of the same key.
        StepVerifier.create(client.set(original))
                .assertNext(response -> assertConfigurationEquals(original, response))
                .expectComplete()
                .verify();
        StepVerifier.create(client.set(updated))
                .assertNext(response -> assertConfigurationEquals(updated, response))
                .expectComplete()
                .verify();

        // Get all revisions for a key
        StepVerifier.create(client.listKeyValueRevisions(new RevisionFilter().withKey(keyPrefix + "*")))
                .assertNext(response -> {
                    assertEquals(keyName, response.key());
                    assertTrue(expected.remove(response.value()));
                })
                .assertNext(response -> {
                    assertEquals(keyName, response.key());
                    assertTrue(expected.remove(response.value()));
                })
                .verifyComplete();

        assertTrue(expected.isEmpty());
    }

    /**
     * Verifies that, given a ton of existing settings, we can list the ConfigurationSettings using pagination
     * (ie. where 'nextLink' has a URL pointing to the next page of results.)
     *
     * TODO (conniey): Remove the manual retry when issue is fixed: https://github.com/azure/azure-sdk-for-java/issues/3183
     */
    @Test
    public void listSettingsWithPagination() {
        final String label = "listed-label";
        final int numberExpected = 50;
        List<ConfigurationSetting> settings = IntStream.range(0, numberExpected)
                .mapToObj(value -> new ConfigurationSetting()
                        .withKey(keyPrefix + "-" + value)
                        .withValue("myValue")
                        .withLabel(label))
                .collect(Collectors.toList());

        List<Mono<RestResponse<ConfigurationSetting>>> results = new ArrayList<>();
        for (ConfigurationSetting setting : settings) {
            results.add(client.set(setting).retryBackoff(2, Duration.ofSeconds(30)));
        }

        KeyValueListFilter filter = new KeyValueListFilter().withLabel(label);

        Flux.merge(results).blockLast();
        StepVerifier.create(client.listKeyValues(filter))
                .expectNextCount(numberExpected)
                .expectComplete()
                .verify();
    }

    /**
     * Verifies the conditional "GET" scenario where the setting has yet to be updated, resulting in a 304. This GET
     * scenario will return a setting when the etag provided does not match the one of the current setting.
     */
    @Ignore("Getting a configuration setting only when the value has changed is not a common scenario.")
    @Test
    public void getSettingWhenValueNotUpdated() {
        final String key = SdkContext.randomResourceName(keyPrefix, 16);
        final ConfigurationSetting expected = new ConfigurationSetting().withKey(key).withValue("myValue");
        final ConfigurationSetting newExpected = new ConfigurationSetting().withKey(key).withValue("myNewValue");
        final RestResponse<ConfigurationSetting> block = client.add(expected).single().block();

        assertNotNull(block);
        assertConfigurationEquals(expected, block);

//        String etag = block.body().etag();
//        StepVerifier.create(client.get(key, null, etag))
//                .expectErrorSatisfies(ex -> {
//                    Assert.assertTrue(ex instanceof RestException);
//                    // etag has not changed, so getting 304 NotModified code according to service spec
//                    Assert.assertTrue(ex.getMessage().contains("304"));
//                })
//                .verify();

        StepVerifier.create(client.set(newExpected))
                .assertNext(response -> assertConfigurationEquals(newExpected, response))
                .expectComplete()
                .verify();
    }

    @Ignore("This test exists to clean up resources missed due to 429s.")
    @Test
    public void deleteAllSettings() {
        client.listKeyValues(new KeyValueListFilter().withKey("key*"))
                .flatMap(configurationSetting -> {
                    logger.info("Deleting key:label [{}:{}]. isLocked? {}", configurationSetting.key(), configurationSetting.label(), configurationSetting.isLocked());

                    if (configurationSetting.isLocked()) {
                        return client.unlock(configurationSetting.key(), configurationSetting.label()).flatMap(response -> {
                            ConfigurationSetting kv = response.body();
                            return client.delete(kv.key(), kv.label(), null);
                        });
                    } else {
                        return client.delete(configurationSetting.key(), configurationSetting.label(), null);
                    }
                }).blockLast();
    }

    private static void assertMapContainsLabel(HashMap<String, ConfigurationSetting> map,
                                               RestResponse<ConfigurationSetting> response) {
        assertNotNull(response);
        assertNotNull(response.body());

        ConfigurationSetting fetched = map.getOrDefault(response.body().label(), null);
        assertNotNull(fetched);
        assertConfigurationEquals(fetched, response);
    }

    private static void assertConfigurationEquals(ConfigurationSetting expected, RestResponse<ConfigurationSetting> response) {
        assertNotNull(response);
        assertEquals(200, response.statusCode());

        if (expected == null) {
            assertNull(response.body());
            return;
        }

        assertConfigurationEquals(expected, response.body());
    }

    private static void assertConfigurationEquals(ConfigurationSetting expected, ConfigurationSetting actual) {
        if (expected == null) {
            assertNull(actual);
            return;
        }

        assertNotNull(actual);
        assertEquals(expected.key(), actual.key());

        // This is because we have the "null" label which is deciphered in the service as "\0".
        if (ConfigurationSetting.NULL_LABEL.equals(expected.label())) {
            assertNull(actual.label());
        } else {
            assertEquals(expected.label(), actual.label());
        }

        assertEquals(expected.value(), actual.value());
        assertEquals(expected.contentType(), actual.contentType());

        if (expected.tags() != null) {
            assertEquals(expected.tags().size(), actual.tags().size());

            expected.tags().forEach((key, value) -> {
                assertTrue(actual.tags().containsKey(key));
                assertEquals(value, actual.tags().get(key));
            });
        }
    }
}
