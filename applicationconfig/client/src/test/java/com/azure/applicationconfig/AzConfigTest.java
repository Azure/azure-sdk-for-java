// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig;

import com.azure.applicationconfig.models.Key;
import com.azure.applicationconfig.models.KeyLabelFilter;
import com.azure.applicationconfig.models.KeyValue;
import com.azure.applicationconfig.models.KeyValueFilter;
import com.azure.applicationconfig.models.KeyValueListFilter;
import com.azure.applicationconfig.models.RevisionFilter;
import com.microsoft.azure.core.InterceptorManager;
import com.microsoft.azure.core.TestMode;
import com.microsoft.azure.utils.SdkContext;
import com.microsoft.azure.v3.CloudException;
import com.microsoft.rest.v3.RestResponse;
import com.microsoft.rest.v3.http.HttpClient;
import com.microsoft.rest.v3.http.HttpPipeline;
import com.microsoft.rest.v3.http.HttpPipelineLogLevel;
import com.microsoft.rest.v3.http.HttpPipelineLogger;
import com.microsoft.rest.v3.http.HttpPipelineOptions;
import com.microsoft.rest.v3.http.Slf4jLogger;
import com.microsoft.rest.v3.http.policy.HttpLogDetailLevel;
import com.microsoft.rest.v3.http.policy.HttpLoggingPolicy;
import com.microsoft.rest.v3.http.policy.HttpPipelinePolicy;
import com.microsoft.rest.v3.http.policy.RetryPolicy;
import com.microsoft.rest.v3.http.policy.UserAgentPolicy;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static com.azure.applicationconfig.AzConfigClient.SDK_NAME;
import static com.azure.applicationconfig.AzConfigClient.SDK_VERSION;

public class AzConfigTest {
    private static final String PLAYBACK_URI_BASE = "http://localhost:";

    private final Logger logger = LoggerFactory.getLogger(AzConfigTest.class);

    private InterceptorManager interceptorManager;
    private AzConfigClient client;
    private String keyPrefix;
    private String playbackUri = null;
    private TestMode testMode = null;

    @Rule
    public TestName testName = new TestName();

    @Before
    public void beforeTest() throws Exception {
        initTestMode();
        initPlaybackUri();

        interceptorManager = InterceptorManager.create(testName.getMethodName(), testMode);
<<<<<<< HEAD

        ApplicationConfigCredentials credentials;
=======
        HttpPipelineOptions pipelineOptions = new HttpPipelineOptions(new Slf4jLogger(logger).withMinimumLogLevel(HttpPipelineLogLevel.INFO));
        AzConfigClient.AzConfigCredentials credentials;
>>>>>>> 74491aa9fe... Updating AzConfigTest to use HttpClient that enables wiretapping
        HttpPipeline pipeline;

        if (isPlaybackMode()) {
            logger.info("PLAYBACK MODE");

            connectionString = "endpoint=" + playbackUri + ";Id=0000000000000;Secret=MDAwMDAw";
            credentials = ApplicationConfigCredentials.parseConnectionString(connectionString);
            List<HttpPipelinePolicy> policies = getDefaultPolicies(credentials);

            pipeline = new HttpPipeline(
                    interceptorManager.initPlaybackClient(),
                    pipelineOptions,
                    policies.toArray(new HttpPipelinePolicy[0]));

            System.out.println(playbackUri);
        } else {
            logger.info("RECORD MODE");

            String connectionString = System.getenv("AZCONFIG_CONNECTION_STRING");
            Objects.requireNonNull(connectionString, "AZCONFIG_CONNECTION_STRING expected to be set.");

            credentials = AzConfigClient.AzConfigCredentials.parseConnectionString(connectionString);
            List<HttpPipelinePolicy> policies = getDefaultPolicies(credentials);
            policies.add(interceptorManager.initRecordPolicy());

            HttpClient httpClient = HttpClient.createDefault().wiretap(true);
            pipeline = new HttpPipeline(httpClient, pipelineOptions, policies.toArray(new HttpPipelinePolicy[0]));

            interceptorManager.addTextReplacementRule(credentials.baseUri().toString(), playbackUri);
        }
        client = AzConfigClient.create(connectionString, pipeline);
        keyPrefix = SdkContext.randomResourceName("key", 8);
    }

    private static List<HttpPipelinePolicy> getDefaultPolicies(ApplicationConfigCredentials credentials) {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(String.format("Azure-SDK-For-Java/%s (%s)", SDK_NAME, SDK_VERSION)));
        policies.add(new RequestIdPolicy());
        policies.add(new AzConfigCredentialsPolicy(credentials));
        policies.add(new RetryPolicy());
        policies.add(new HttpLoggingPolicy(HttpLogDetailLevel.BODY_AND_HEADERS));

        return policies;
    }

    private void initPlaybackUri() throws IOException {
        if (isPlaybackMode()) {
            Properties mavenProps = new Properties();
            InputStream in = AzConfigTest.class.getResourceAsStream("/maven.properties");
            if (in == null) {
                throw new IOException(
                        "The file \"maven.properties\" has not been generated yet. Please execute \"mvn compile\" to generate the file.");
            }
            mavenProps.load(in);
            String port = mavenProps.getProperty("playbackServerPort");
            // 11080 and 11081 needs to be in sync with values in jetty.xml file
            playbackUri = PLAYBACK_URI_BASE + port;
        } else {
            playbackUri = PLAYBACK_URI_BASE + "1234";
        }
    }

    private boolean isPlaybackMode() {
        if (testMode == null) {
            try {
                initTestMode();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                throw new RuntimeException("Can't init test mode.");
            }
        }
        return testMode == TestMode.PLAYBACK;
    }

    private void initTestMode() throws IllegalArgumentException {
        String azureTestMode = System.getenv("AZURE_TEST_MODE");

        if (azureTestMode != null) {
            try {
                testMode = TestMode.valueOf(azureTestMode.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                logger.error("Could not parse '{}' into TestEnum.", azureTestMode);
                throw e;
            }
        } else {
            logger.info("Environment variable 'AZURE_TEST_MODE' has not been set yet. Using 'Playback' mode.");
            testMode = TestMode.PLAYBACK;
        }
    }

    @After
    public void afterTest() throws IOException {
        cleanUpResources();
        interceptorManager.finalizeInterceptor();
    }

    private void cleanUpResources() {
        logger.info("Cleaning up created key values.");
        client.listKeyValues(new KeyValueListFilter().withKey(keyPrefix + "*"))
                .flatMap(keyValue -> {
                    logger.info("Deleting key:label [{}:{}]. isLocked? {}", keyValue.key(), keyValue.label(), keyValue.isLocked());

                    if (keyValue.isLocked()) {
                        return client.unlockKeyValue(keyValue.key(), keyValue.label(), null).flatMap(response -> {
                            KeyValue kv = response.body();
                            return client.deleteKeyValue(kv.key(), kv.label(), null);
                        });
                    } else {
                        return client.deleteKeyValue(keyValue.key(), keyValue.label(), null);
                    }
                }).blockLast();

        logger.info("Finished cleaning up values.");
    }

    @Test
    public void listWithKeyAndLabel() {
        String key = SdkContext.randomResourceName(keyPrefix, 16);
        String label = SdkContext.randomResourceName("lbl", 8);
        KeyValue kv = new KeyValue().withKey(key).withValue("myValue").withLabel(label);

        client.setKeyValue(kv).block();

        kv = client.listKeyValues(new KeyValueListFilter().withKey(key).withLabel(label)).blockFirst();
        Assert.assertEquals(key, kv.key());
        Assert.assertEquals(label, kv.label());

        kv = client.listKeyValues(new KeyValueListFilter().withKey(key)).blockFirst();
        Assert.assertEquals(key, kv.key());
        Assert.assertEquals(label, kv.label());
    }

    @Test
    public void crudKeyValue() {
        KeyValue newKeyValue = new KeyValue().withKey("myNewKey5").withValue("myNewValue5");
        KeyValue newKv = client.setKeyValue(newKeyValue).block().body();

        KeyValue kv = client.deleteKeyValue(newKeyValue.key()).block().body();
    }

    @Test
    public void getWithLabel() {
        String key = SdkContext.randomResourceName(keyPrefix, 16);
        KeyValue kv = new KeyValue().withKey(key).withValue("myValue").withLabel("myLabel");
        client.setKeyValue(kv).block();
        kv = client.getKeyValue(key, new KeyValueFilter().withLabel("myLabel")).block().body();
        Assert.assertNotNull(kv);
        Assert.assertEquals("myLabel", kv.label());
        Assert.assertEquals("myValue", kv.value());
        try {
            kv = client.getKeyValue(key, new KeyValueFilter().withLabel("myNonExistingLabel")).block().body();
            Assert.fail("Should not be able to get a keyValue with non-existent label");
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof CloudException);
            Assert.assertEquals(404, ((CloudException) ex).response().statusCode());
        }
    }

    @Test
    public void getWithEtag() {
        String key = SdkContext.randomResourceName(keyPrefix, 16);
        KeyValue kv = client.setKeyValue(new KeyValue().withKey(key).withValue("myValue")).block().body();
        String etag = kv.etag();
        try {
            RestResponse<Map<String, String>, KeyValue> response = client.getKeyValue(key, new KeyValueFilter().withIfNoneMatch(etag)).block();
            Assert.fail("Should have thrown an exception");
        } catch (Exception ex) {
            logger.info("Exception thrown.");
            Assert.assertTrue(ex instanceof CloudException);
            // etag has not changed, so getting 304 NotModified code according to service spec
            Assert.assertTrue(ex.getMessage().contains("304"));
        }
        kv = client.setKeyValue(new KeyValue().withKey(key).withValue("myValue")).block().body();
        Assert.assertEquals("myValue", kv.value());
    }

    @Test
    public void lockUnlockKeyValue() {
        String keyName = SdkContext.randomResourceName(keyPrefix, 16);
        KeyValue newKeyValue = new KeyValue().withKey(keyName).withValue("myKeyValue");
        KeyValue newKv = client.setKeyValue(newKeyValue).block().body();
        try {
            newKv = client.lockKeyValue(keyName).block().body();
            newKv.withValue("myNewKeyValue");
            client.setKeyValue(newKv).block();
            Assert.fail("Should not be able to modify locked value");
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof CloudException);
            Assert.assertEquals(HttpResponseStatus.CONFLICT.code(), ((CloudException) ex).response().statusCode());
        }
        client.unlockKeyValue(keyName).block();
        KeyValue updatedKv = new KeyValue().withKey(keyName).withValue("myUpdatedValue");
        newKeyValue = client.setKeyValue(updatedKv).block().body();
        Assert.assertEquals("myUpdatedValue", newKeyValue.value());
    }

    @Test
    public void listRevisions() {
        String keyName = SdkContext.randomResourceName(keyPrefix, 16);
        KeyValue newKeyValue = new KeyValue().withKey(keyName).withValue("myValue");

        newKeyValue = client.setKeyValue(newKeyValue, null).block().body();
        KeyValue updatedNewKeyValue = client.setKeyValue(newKeyValue.withValue("myNewValue"), null).block().body();

        // Get all revisions for a key
        Long revisions = client.listKeyValueRevisions(new RevisionFilter().withKey(keyPrefix + "*")).count().block();
        Assert.assertEquals(Long.valueOf(2L), revisions);
    }

    @Test
    public void listLabels() {
        String keyName = SdkContext.randomResourceName(keyPrefix, 16);
        String label1 = keyPrefix + "-lbl1";
        String label2 = keyPrefix + "-lbl2";
        String label3 = keyPrefix + "-lbl3";
        client.setKeyValue(new KeyValue().withKey(keyName).withValue("value1").withLabel(label1)).block();
        client.setKeyValue(new KeyValue().withKey(keyName).withValue("value2").withLabel(label2)).block();
        client.setKeyValue(new KeyValue().withKey(keyName).withValue("value3").withLabel(label3)).block();

//        List<Label> labels = client.listLabels(new KeyLabelFilter().withName(keyPrefix + "-lbl*")).blockFirst().items();
//        Assert.assertEquals(3, labels.size());
//        Assert.assertEquals(1, labels.get(0).kvCount());
//        Assert.assertTrue(Pattern.matches(keyPrefix + "-lbl\\d", labels.get(0).name()));
    }

    @Test
    public void listKeys() {
        String key1 = keyPrefix + "-1";
        String key2 = keyPrefix + "-2";
        String key3 = keyPrefix + "-3";
//        client.getKeyValue("key69076343820", null).block();
        Map<String, String> tags = new HashMap<>();
        client.setKeyValue(new KeyValue().withKey(key1).withValue("value1").withLabel("label1").withContentType("testContentType").withTags(tags)).block();
        client.setKeyValue(new KeyValue().withKey(key2).withValue("value2").withLabel("label2")).block();
        client.setKeyValue(new KeyValue().withKey(key3).withValue("value3").withLabel("label3")).block();

        List<Key> keys = client.listKeys(new KeyLabelFilter().withName(keyPrefix + "*")).collectList().block();
        Assert.assertEquals(3, keys.size());
    }
}
