// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig;

import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.applicationconfig.models.Key;
import com.azure.applicationconfig.models.KeyLabelFilter;
import com.azure.applicationconfig.models.KeyValueFilter;
import com.azure.applicationconfig.models.KeyValueListFilter;
import com.azure.applicationconfig.models.RevisionFilter;
import com.microsoft.azure.core.InterceptorManager;
import com.microsoft.azure.core.TestMode;
import com.microsoft.azure.utils.SdkContext;
import com.microsoft.azure.v3.CloudError;
import com.microsoft.azure.v3.CloudException;
import com.microsoft.rest.v3.http.HttpClient;
import com.microsoft.rest.v3.http.HttpPipeline;
import com.microsoft.rest.v3.http.policy.HttpLogDetailLevel;
import com.microsoft.rest.v3.http.policy.HttpLoggingPolicy;
import com.microsoft.rest.v3.http.policy.HttpPipelinePolicy;
import com.microsoft.rest.v3.http.rest.RestResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.After;
import org.junit.Assert;
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
import java.net.ResponseCache;
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

public class ConfigurationClientTest {
    private static final String PLAYBACK_URI_BASE = "http://localhost:";

    private final Logger logger = LoggerFactory.getLogger(ConfigurationClientTest.class);

    private InterceptorManager interceptorManager;
    private ConfigurationClient client;
    private String keyPrefix;

    @Rule
    public TestName testName = new TestName();

    @Before
    public void beforeTest() throws Exception {
        final TestMode testMode = getTestMode();
        final String playbackUri = getPlaybackUri(testMode);
        final HttpPipelinePolicy loggingPolicy = new HttpLoggingPolicy(HttpLogDetailLevel.BODY_AND_HEADERS);
        final String connectionString;
        final HttpPipeline pipeline;

        interceptorManager = InterceptorManager.create(testName.getMethodName(), testMode);

        if (interceptorManager.isPlaybackMode()) {
            logger.info("PLAYBACK MODE");

            connectionString = "endpoint=" + playbackUri + ";Id=0000000000000;Secret=MDAwMDAw";

            List<HttpPipelinePolicy> policies = ConfigurationClient.getDefaultPolicies(connectionString);
            policies.add(loggingPolicy);

            pipeline = new HttpPipeline(interceptorManager.getPlaybackClient(), policies);
        } else {
            logger.info("RECORD MODE");

            connectionString = System.getenv("AZCONFIG_CONNECTION_STRING");
            Objects.requireNonNull(connectionString, "AZCONFIG_CONNECTION_STRING expected to be set.");

            List<HttpPipelinePolicy> policies = ConfigurationClient.getDefaultPolicies(connectionString);
            policies.add(interceptorManager.getRecordPolicy());
            policies.add(loggingPolicy);

            pipeline = new HttpPipeline(HttpClient.createDefault().wiretap(true), policies);
        }

        client = new ConfigurationClient(connectionString, pipeline);
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
                        return client.unlockKeyValue(configurationSetting.key(), configurationSetting.label()).flatMap(response -> {
                            ConfigurationSetting kv = response.body();
                            return client.deleteKeyValue(kv.key(), kv.label(), null)
                                    .retryBackoff(3, Duration.ofSeconds(10));
                        });
                    } else {
                        return client.deleteKeyValue(configurationSetting.key(), configurationSetting.label(), null)
                                .retryBackoff(3, Duration.ofSeconds(10));
                    }
                })
                .blockLast();

        logger.info("Finished cleaning up values.");
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

        StepVerifier.create(client.setKeyValue(expected))
                .assertNext(response -> assertEquals(expected, response))
                .expectComplete()
                .verify();

        StepVerifier.create(client.listKeyValues(new KeyValueListFilter().withKey(key).withLabel(label)))
                .assertNext(configurationSetting -> assertEquals(expected, configurationSetting))
                .expectComplete()
                .verify();

        StepVerifier.create(client.listKeyValues(new KeyValueListFilter().withKey(key)))
                .assertNext(configurationSetting -> assertEquals(expected, configurationSetting))
                .expectComplete()
                .verify();
    }

    @Test
    public void crudKeyValue() {
        final String key = SdkContext.randomResourceName(keyPrefix, 8);
        final ConfigurationSetting newConfigurationSetting = new ConfigurationSetting().withKey(key).withValue("myNewValue5");

        StepVerifier.create(client.setKeyValue(newConfigurationSetting))
                .assertNext(response -> assertEquals(newConfigurationSetting, response))
                .expectComplete()
                .verify();

        StepVerifier.create(client.deleteKeyValue(newConfigurationSetting.key()))
                .assertNext(response -> assertEquals(newConfigurationSetting, response))
                .expectComplete()
                .verify();
    }

    @Test
    public void getWithLabel() {
        final String label = "myLabel";
        final String key = SdkContext.randomResourceName(keyPrefix, 16);
        final ConfigurationSetting kv = new ConfigurationSetting().withKey(key).withValue("myValue").withLabel(label);

        StepVerifier.create(client.setKeyValue(kv))
                .assertNext(response -> assertEquals(kv, response))
                .expectComplete()
                .verify();

        StepVerifier.create(client.getKeyValue(key, "myLabel", null))
                .assertNext(response -> assertEquals(kv, response))
                .expectComplete()
                .verify();

        StepVerifier.create(client.getKeyValue(key, "myNonExistingLabel", null))
                .expectErrorSatisfies(error -> {
                    Assert.assertTrue(error instanceof CloudException);
                    Assert.assertEquals(404, ((CloudException) error).response().statusCode());
                })
                .verify();
    }

    @Test
    public void getWithEtag() {
        final String key = SdkContext.randomResourceName(keyPrefix, 16);
        final ConfigurationSetting expected = new ConfigurationSetting().withKey(key).withValue("myValue");
        final ConfigurationSetting newExpected = new ConfigurationSetting().withKey(key).withValue("myNewValue");
        final RestResponse<ConfigurationSetting> block = client.addKeyValue(expected).single().block();

        Assert.assertNotNull(block);
        assertEquals(expected, block);

        String etag = block.body().etag();
        StepVerifier.create(client.getKeyValue(key, null, etag))
                .expectErrorSatisfies(ex -> {
                    Assert.assertTrue(ex instanceof CloudException);
                    // etag has not changed, so getting 304 NotModified code according to service spec
                    Assert.assertTrue(ex.getMessage().contains("304"));
                })
                .verify();

        StepVerifier.create(client.setKeyValue(newExpected))
                .assertNext(response -> assertEquals(newExpected, response))
                .expectComplete()
                .verify();
    }

    @Test
    public void lockUnlockKeyValue() {
        final String keyName = SdkContext.randomResourceName(keyPrefix, 16);
        final ConfigurationSetting expected = new ConfigurationSetting().withKey(keyName).withValue("myKeyValue");
        final ConfigurationSetting updated = new ConfigurationSetting().withKey(keyName).withValue("Some new value");
        final ConfigurationSetting updated2 = new ConfigurationSetting().withKey(keyName).withValue("Some new value, again.");

        StepVerifier.create(client.setKeyValue(expected))
                .assertNext(response -> assertEquals(expected, response))
                .expectComplete()
                .verify();

        StepVerifier.create(client.lockKeyValue(expected.key()))
                .assertNext(response -> assertEquals(expected, response))
                .expectComplete()
                .verify();

        StepVerifier.create(client.setKeyValue(updated))
                .expectErrorSatisfies(ex -> {
                    Assert.assertTrue(ex instanceof CloudException);
                    Assert.assertEquals(HttpResponseStatus.CONFLICT.code(), ((CloudException) ex).response().statusCode());
                }).verify();

        StepVerifier.create(client.unlockKeyValue(keyName).flatMap(response -> client.setKeyValue(updated2)))
                .assertNext(response -> assertEquals(updated2, response))
                .expectComplete()
                .verify();
    }

    @Test
    public void listRevisions() {
        final String keyName = SdkContext.randomResourceName(keyPrefix, 16);
        final ConfigurationSetting original = new ConfigurationSetting().withKey(keyName).withValue("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting().withKey(keyName).withValue("anotherValue");
        final HashSet<String> expected = new HashSet<>();
        expected.add(original.value());
        expected.add(updated.value());

        // Create two different revisions of the same key.
        StepVerifier.create(client.setKeyValue(original))
                .assertNext(response -> assertEquals(original, response))
                .expectComplete()
                .verify();
        StepVerifier.create(client.setKeyValue(updated))
                .assertNext(response -> assertEquals(updated, response))
                .expectComplete()
                .verify();

        // Get all revisions for a key
        StepVerifier.create(client.listKeyValueRevisions(new RevisionFilter().withKey(keyPrefix + "*")))
                .assertNext(response -> {
                    Assert.assertEquals(keyName, response.key());
                    Assert.assertTrue(expected.remove(response.value()));
                })
                .assertNext(response -> {
                    Assert.assertEquals(keyName, response.key());
                    Assert.assertTrue(expected.remove(response.value()));
                })
                .expectComplete()
                .verify();

        Assert.assertTrue(expected.isEmpty());
    }

    @Test
    public void listLabels() {
        final String keyName = SdkContext.randomResourceName(keyPrefix, 16);
        final ConfigurationSetting value1 = new ConfigurationSetting().withKey(keyName).withValue("value1").withLabel(keyPrefix + "-lbl1");
        final ConfigurationSetting value2 = new ConfigurationSetting().withKey(keyName).withValue("value2").withLabel(keyPrefix + "-lbl2");
        final ConfigurationSetting value3 = new ConfigurationSetting().withKey(keyName).withValue("value3").withLabel(keyPrefix + "-lbl3");
        final KeyLabelFilter filter = new KeyLabelFilter()
                .withName(keyPrefix + "-lbl*")
                .withFields("name")
                .withFields("kv_count")
                .withFields("last_modifier");
        final HashMap<String, ConfigurationSetting> expected = new HashMap<>();
        expected.put(value1.label(), value1);
        expected.put(value2.label(), value2);
        expected.put(value3.label(), value3);

        StepVerifier.create(Flux.merge(
                client.setKeyValue(value1),
                client.setKeyValue(value2),
                client.setKeyValue(value3)))
                .assertNext(response -> assertMapContainsLabel(expected, response))
                .assertNext(response -> assertMapContainsLabel(expected, response))
                .assertNext(response -> assertMapContainsLabel(expected, response))
                .expectComplete()
                .verify();

        StepVerifier.create(client.listLabels(filter))
                .assertNext(label -> {
                    Assert.assertNotNull(label);
                    ConfigurationSetting value = expected.remove(label.name());
                    Assert.assertNotNull(value);
                    Assert.assertEquals(1, label.kvCount());
                })
                .assertNext(label -> {
                    Assert.assertNotNull(label);
                    ConfigurationSetting value = expected.remove(label.name());
                    Assert.assertNotNull(value);
                    Assert.assertEquals(1, label.kvCount());
                })
                .assertNext(label -> {
                    Assert.assertNotNull(label);
                    ConfigurationSetting value = expected.remove(label.name());
                    Assert.assertNotNull(value);
                    Assert.assertEquals(1, label.kvCount());
                })
                .expectComplete()
                .verify();

        Assert.assertTrue(expected.isEmpty());
    }

    @Test
    public void listKeys() {
        final Map<String, String> tags = new HashMap<>();
        final ConfigurationSetting key1 = new ConfigurationSetting().withKey(keyPrefix + "-1").withValue("value1").withLabel("label1").withContentType("testContentType").withTags(tags);
        final ConfigurationSetting key2 = new ConfigurationSetting().withKey(keyPrefix + "-2").withValue("value2").withLabel("label2");
        final ConfigurationSetting key3 = new ConfigurationSetting().withKey(keyPrefix + "-3").withValue("value3").withLabel("label3");
        final KeyLabelFilter filter = new KeyLabelFilter().withName(keyPrefix + "*");
        final HashMap<String, ConfigurationSetting> expected = new HashMap<>();

        StepVerifier.create(Flux.merge(
                client.setKeyValue(key1),
                client.setKeyValue(key2),
                client.setKeyValue(key3)
        ))
                .expectNextCount(3)
                .expectComplete()
                .verify();

        List<Key> keys = client.listKeys(filter).collectList().block();
        Assert.assertEquals(3, keys.size());
    }

    @Test
    public void listKeysWithPage() {
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
            results.add(client.setKeyValue(setting).retryBackoff(2, Duration.ofSeconds(30)));
        }

        KeyValueListFilter filter = new KeyValueListFilter().withLabel(label);

        Flux.merge(results).blockLast();
        StepVerifier.create(client.listKeyValues(filter))
                .expectNextCount(numberExpected)
                .expectComplete()
                .verify();
    }

    @Ignore("This test exists to clean up resources missed due to 429s.")
    @Test
    public void deleteAllKeys() {
        client.listKeyValues(new KeyValueListFilter().withKey("key*"))
                .flatMap(configurationSetting -> {
                    logger.info("Deleting key:label [{}:{}]. isLocked? {}", configurationSetting.key(), configurationSetting.label(), configurationSetting.isLocked());

                    if (configurationSetting.isLocked()) {
                        return client.unlockKeyValue(configurationSetting.key(), configurationSetting.label()).flatMap(response -> {
                            ConfigurationSetting kv = response.body();
                            return client.deleteKeyValue(kv.key(), kv.label(), null);
                        });
                    } else {
                        return client.deleteKeyValue(configurationSetting.key(), configurationSetting.label(), null);
                    }
                }).blockLast();
    }

    private static void assertMapContainsLabel(HashMap<String, ConfigurationSetting> map,
                                               RestResponse<ConfigurationSetting> response) {
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.body());

        ConfigurationSetting fetched = map.getOrDefault(response.body().label(), null);
        Assert.assertNotNull(fetched);
        assertEquals(fetched, response);
    }

    private static void assertEquals(ConfigurationSetting expected, RestResponse<ConfigurationSetting> response) {
        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.statusCode());

        if (expected == null) {
            Assert.assertNull(response.body());
            return;
        }

        assertEquals(expected, response.body());
    }

    private static void assertEquals(ConfigurationSetting expected, ConfigurationSetting actual) {
        if (expected == null) {
            Assert.assertNull(actual);
            return;
        }

        Assert.assertNotNull(actual);
        Assert.assertEquals(expected.key(), actual.key());

        // This is because we have the "null" label which is deciphered in the service as "\0".
        if (ConfigurationSetting.NULL_LABEL.equals(expected.label())) {
            Assert.assertNull(actual.label());
        } else {
            Assert.assertEquals(expected.label(), actual.label());
        }

        Assert.assertEquals(expected.value(), actual.value());
        Assert.assertEquals(expected.contentType(), actual.contentType());
    }
}
