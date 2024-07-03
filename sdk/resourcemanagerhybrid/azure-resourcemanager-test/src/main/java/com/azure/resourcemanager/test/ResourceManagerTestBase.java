// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.test;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.serializer.json.jackson.JacksonJsonSerializerProvider;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.test.utils.ResourceNamer;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.test.policy.HttpDebugLoggingPolicy;
import com.azure.resourcemanager.test.policy.TextReplacementPolicy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Test base for resource manager SDK.
 * <p>
 * For LIVE/RECORD test, dev should
 * 1. Sign-in Azure with CLI ("az login").
 * 2. AZURE_TENANT_ID environment variable is set.
 * 3. AZURE_SUBSCRIPTION_ID environment variable is set.
 */
public abstract class ResourceManagerTestBase extends TestBase {
    private static final String ZERO_SUBSCRIPTION = "00000000-0000-0000-0000-000000000000";
    private static final String ZERO_TENANT = "00000000-0000-0000-0000-000000000000";
    private static final String PLAYBACK_URI_BASE = "http://localhost:";
    private static final String AZURE_TEST_LOG_LEVEL = "AZURE_TEST_LOG_LEVEL";
    private static final String AZURE_ARM_ENDPOINT = "AZURE_ARM_ENDPOINT";
    private static final String AZURE_LOCATION = "AZURE_LOCATION";
    private static final String AZURE_STACK_CLOUD = "AzureStackCloud";
    private static final String HTTPS_PROXY_HOST = "https.proxyHost";
    private static final String HTTPS_PROXY_PORT = "https.proxyPort";
    private static final String HTTP_PROXY_HOST = "http.proxyHost";
    private static final String HTTP_PROXY_PORT = "http.proxyPort";
    private static final String USE_SYSTEM_PROXY = "java.net.useSystemProxies";
    private static final String VALUE_TRUE = "true";
    private static final String PLAYBACK_URI = PLAYBACK_URI_BASE + "1234";
    private static final AzureProfile PLAYBACK_PROFILE = new AzureProfile(
        ZERO_TENANT,
        ZERO_SUBSCRIPTION,
        new AzureEnvironment(
            new HashMap<String, String>() {
                {
                    put("managementEndpointUrl", PLAYBACK_URI);
                    put("resourceManagerEndpointUrl", PLAYBACK_URI);
                    put("sqlManagementEndpointUrl", PLAYBACK_URI);
                    put("galleryEndpointUrl", PLAYBACK_URI);
                    put("activeDirectoryEndpointUrl", PLAYBACK_URI);
                    put("activeDirectoryResourceId", PLAYBACK_URI);
                    put("activeDirectoryGraphResourceId", PLAYBACK_URI);
                }}));
    private static final OutputStream EMPTY_OUTPUT_STREAM = new OutputStream() {
        @Override
        public void write(int b) {
        }
    };

    private static final ClientLogger LOGGER = new ClientLogger(ResourceManagerTestBase.class);
    private AzureProfile testProfile;
    private boolean isSkipInPlayback;

    /**
     * Generates a random resource name.
     *
     * @param prefix Prefix for the resource name.
     * @param maxLen Maximum length of the resource name.
     * @return A randomly generated resource name with a given prefix and maximum length.
     */
    protected String generateRandomResourceName(String prefix, int maxLen) {
        return testResourceNamer.randomName(prefix, maxLen);
    }

    /**
     * @return A randomly generated UUID.
     */
    protected String generateRandomUuid() {
        return testResourceNamer.randomUuid();
    }

    /**
     * @return random password
     */
    public static String password() {
        // do not record
        String password = new ResourceNamer("").randomName("Pa5$", 12);
        LOGGER.info("Password: {}", password);
        return password;
    }

    private static String sshPublicKey;

    /**
     * @return an SSH public key
     */
    public static String sshPublicKey() {
        if (sshPublicKey == null) {
            try {
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(1024);
                KeyPair pair = keyGen.generateKeyPair();
                PublicKey publicKey = pair.getPublic();

                RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
                ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(byteOs);
                dos.writeInt("ssh-rsa".getBytes(StandardCharsets.US_ASCII).length);
                dos.write("ssh-rsa".getBytes(StandardCharsets.US_ASCII));
                dos.writeInt(rsaPublicKey.getPublicExponent().toByteArray().length);
                dos.write(rsaPublicKey.getPublicExponent().toByteArray());
                dos.writeInt(rsaPublicKey.getModulus().toByteArray().length);
                dos.write(rsaPublicKey.getModulus().toByteArray());
                String publicKeyEncoded = new String(Base64.getEncoder().encode(byteOs.toByteArray()), StandardCharsets.US_ASCII);
                sshPublicKey = "ssh-rsa " + publicKeyEncoded;
            } catch (NoSuchAlgorithmException | IOException e) {
                throw LOGGER.logExceptionAsError(new IllegalStateException("failed to generate ssh key", e));
            }
        }
        return sshPublicKey;
    }

    /**
     * Return the configured Azure location from AZURE_LOCATION, or a default region.
     *
     * @param defaultLocation the default region to use
     * @return the region as a string
     */
    public static String locationOrDefault(String defaultLocation) {
        String location = System.getenv(AZURE_LOCATION);
        if (location == null) {
            return defaultLocation;
        }
        return location;
    }

    /**
     * Return the configured Azure location from AZURE_LOCATION, or a default region.
     *
     * @param defaultLocation the default region to use
     * @return the region
     */
    public static Region locationOrDefault(Region defaultLocation) {
        String location = System.getenv(AZURE_LOCATION);
        if (location == null) {
            return defaultLocation;
        }
        return Region.fromName(location);
    }

    /**
     * Loads a client ID from file.
     *
     * @return A client ID loaded from a file.
     */
    protected String clientIdFromFile() {
        String clientId = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_CLIENT_ID);
        return testResourceNamer.recordValueFromConfig(clientId);
    }

    /**
     * @return The test profile.
     */
    protected AzureProfile profile() {
        return testProfile;
    }

    /**
     * @return Whether the test mode is {@link TestMode#PLAYBACK}.
     */
    protected boolean isPlaybackMode() {
        return getTestMode() == TestMode.PLAYBACK;
    }

    /**
     * @return Whether the test should be skipped in playback.
     */
    protected boolean skipInPlayback() {
        if (isPlaybackMode()) {
            isSkipInPlayback = true;
        }
        return isSkipInPlayback;
    }

    @Override
    protected void beforeTest() {
        TokenCredential credential;
        HttpPipeline httpPipeline;
        Map<String, String> textReplacementRules = new HashMap<>();
        String logLevel = Configuration.getGlobalConfiguration().get(AZURE_TEST_LOG_LEVEL);
        HttpLogDetailLevel httpLogDetailLevel;

        try {
            httpLogDetailLevel = HttpLogDetailLevel.valueOf(logLevel);
        } catch (Exception e) {
            if (isPlaybackMode()) {
                httpLogDetailLevel = HttpLogDetailLevel.NONE;
                LOGGER.error("Environment variable '{}' has not been set yet. Using 'NONE' for PLAYBACK.", new Object[]{AZURE_TEST_LOG_LEVEL});
            } else {
                httpLogDetailLevel = HttpLogDetailLevel.BODY_AND_HEADERS;
                LOGGER.error("Environment variable '{}' has not been set yet. Using 'BODY_AND_HEADERS' for RECORD/LIVE.", new Object[]{AZURE_TEST_LOG_LEVEL});
            }
        }

        if (httpLogDetailLevel == HttpLogDetailLevel.NONE) {
            try {
                System.setOut(new PrintStream(EMPTY_OUTPUT_STREAM, false, Charset.defaultCharset().name()));
                System.setErr(new PrintStream(EMPTY_OUTPUT_STREAM, false, Charset.defaultCharset().name()));
            } catch (UnsupportedEncodingException e) {
            }
        }

        if (isPlaybackMode()) {
            if (interceptorManager.getRecordedData() == null) {
                skipInPlayback();
                return;
            }

            testProfile = PLAYBACK_PROFILE;
            List<HttpPipelinePolicy> policies = new ArrayList<>();
            policies.add(new TextReplacementPolicy(interceptorManager.getRecordedData(), textReplacementRules));
            httpPipeline = buildHttpPipeline(
                null,
                testProfile,
                new HttpLogOptions().setLogLevel(httpLogDetailLevel),
                policies,
                interceptorManager.getPlaybackClient());
            textReplacementRules.put(PLAYBACK_URI_BASE + "1234", PLAYBACK_URI);
            addTextReplacementRules(textReplacementRules);
        } else {
            Configuration configuration = Configuration.getGlobalConfiguration();
            String cloud = configuration.get(Configuration.PROPERTY_AZURE_CLOUD);
            AzureEnvironment environment;

            if (cloud == null || cloud.equalsIgnoreCase(AzureEnvironment.AZURE.toString())) {
                environment = AzureEnvironment.AZURE;
            } else if (cloud.equalsIgnoreCase(AZURE_STACK_CLOUD)) {
                String armEndpoint = System.getenv(AZURE_ARM_ENDPOINT);
                String location = System.getenv(AZURE_LOCATION);
                if (armEndpoint == null || location == null) {
                    throw LOGGER.logExceptionAsError(
                        new IllegalArgumentException("When running tests with AzureStackCloud in record mode AZURE_ARM_ENDPOINT and AZURE_LOCATION need to be set"));
                }

                HashMap<String, String> settings = getActiveDirectorySettings(armEndpoint);
                HashMap<String, String> endpoints = new HashMap<String, String>();

                endpoints.put("managementEndpointUrl", settings.get("audience"));
                endpoints.put("resourceManagerEndpointUrl", armEndpoint);
                endpoints.put("galleryEndpointUrl", settings.get("galleryEndpoint"));
                endpoints.put("activeDirectoryEndpointUrl", settings.get("login_endpoint"));
                endpoints.put("activeDirectoryResourceId", settings.get("audience"));
                endpoints.put("activeDirectoryGraphResourceId", settings.get("graphEndpoint"));
                endpoints.put("storageEndpointSuffix", armEndpoint.substring(armEndpoint.indexOf('.')));
                endpoints.put("keyVaultDnsSuffix", ".vault" + armEndpoint.substring(armEndpoint.indexOf('.')));

                environment = new AzureEnvironment(endpoints);
            } else {
                throw LOGGER.logExceptionAsError(
                    new IllegalArgumentException("Unsupported AZURE_CLOUD given: " + cloud));
            }

            String tenantId = Objects.requireNonNull(
                configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID),
                "'AZURE_TENANT_ID' environment variable cannot be null.");
            String subscriptionId = Objects.requireNonNull(
                configuration.get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID),
                "'AZURE_SUBSCRIPTION_ID' environment variable cannot be null.");
            credential = new DefaultAzureCredentialBuilder()
                .authorityHost(AzureEnvironment.AZURE.getActiveDirectoryEndpoint())
                .build();
            testProfile = new AzureProfile(tenantId, subscriptionId, AzureEnvironment.AZURE);

            List<HttpPipelinePolicy> policies = new ArrayList<>();
            if (!interceptorManager.isLiveMode() && !testContextManager.doNotRecordTest()) {
                policies.add(new TextReplacementPolicy(interceptorManager.getRecordedData(), textReplacementRules));
            }
            if (httpLogDetailLevel == HttpLogDetailLevel.BODY_AND_HEADERS) {
                policies.add(new HttpDebugLoggingPolicy());
                httpLogDetailLevel = HttpLogDetailLevel.NONE;
            }
            httpPipeline = buildHttpPipeline(
                credential,
                testProfile,
                new HttpLogOptions().setLogLevel(httpLogDetailLevel),
                policies,
                generateHttpClientWithProxy(null, null, environment));

            textReplacementRules.put(testProfile.getSubscriptionId(), ZERO_SUBSCRIPTION);
            textReplacementRules.put(testProfile.getTenantId(), ZERO_TENANT);
            textReplacementRules.put(environment.getResourceManagerEndpoint(), PLAYBACK_URI + "/");
            textReplacementRules.put(environment.getGraphEndpoint(), PLAYBACK_URI + "/");
            addTextReplacementRules(textReplacementRules);
        }
        initializeClients(httpPipeline, testProfile);
    }

    private HashMap<String, String> getActiveDirectorySettings(String armEndpoint) {
        HashMap<String, String> adSettings = new HashMap<String, String>();

        String metadataUrl = String.format("%s/metadata/endpoints?api-version=1.0", armEndpoint);
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, metadataUrl);
        httpRequest.setHeader("accept", "application/json");

        HttpResponse response = httpClient.send(httpRequest).block();

        if (response == null || response.getStatusCode() != 200) {
            throw LOGGER.logExceptionAsError(
                new RuntimeException("Failed to get ARM metadata, status code: " + (response != null ? response.getStatusCode() : "null")));
        }

        byte[] body = response.getBodyAsByteArray().block();

        if (body == null) {
            throw LOGGER.logExceptionAsError(
                new RuntimeException("Failed to get ARM metadata, body is null"));
        }

        JsonSerializer jsonSerializer = new JacksonJsonSerializerProvider().createInstance();
        ObjectNode responseJson = jsonSerializer.deserialize(new ByteArrayInputStream(body), TypeReference.createInstance(ObjectNode.class));

        adSettings.put("galleryEndpoint", responseJson.get("galleryEndpoint").asText());
        JsonNode authentication = responseJson.get("authentication");
        adSettings.put("login_endpoint", authentication.get("loginEndpoint").asText());
        adSettings.put("audience", authentication.get("audiences").get(0).asText());
        adSettings.put("graphEndpoint", responseJson.get("graphEndpoint").asText());

        return adSettings;
    }

    /**
     * Generates an {@link HttpClient} with a proxy.
     *
     * @param clientBuilder The HttpClient builder.
     * @param proxyOptions The proxy.
     * @param environment The Azure environment.
     * @return An HttpClient with a proxy.
     */
    protected HttpClient generateHttpClientWithProxy(NettyAsyncHttpClientBuilder clientBuilder, ProxyOptions proxyOptions, AzureEnvironment environment) {
        if (clientBuilder == null) {
            clientBuilder = new NettyAsyncHttpClientBuilder();
        }
        if (proxyOptions != null) {
            clientBuilder.proxy(proxyOptions);
        } else {
            try {
                System.setProperty(USE_SYSTEM_PROXY, VALUE_TRUE);
                List<Proxy> proxies = ProxySelector.getDefault().select(new URI(environment.getResourceManagerEndpoint()));
                if (!proxies.isEmpty()) {
                    for (Proxy proxy : proxies) {
                        if (proxy.address() instanceof InetSocketAddress) {
                            String host = ((InetSocketAddress) proxy.address()).getHostName();
                            int port = ((InetSocketAddress) proxy.address()).getPort();
                            switch (proxy.type()) {
                                case HTTP:
                                    return clientBuilder.proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress(host, port))).build();
                                case SOCKS:
                                    return clientBuilder.proxy(new ProxyOptions(ProxyOptions.Type.SOCKS5, new InetSocketAddress(host, port))).build();
                                default:
                            }
                        }
                    }
                }
                String host = null;
                int port = 0;
                if (System.getProperty(HTTPS_PROXY_HOST) != null && System.getProperty(HTTPS_PROXY_PORT) != null) {
                    host = System.getProperty(HTTPS_PROXY_HOST);
                    port = Integer.parseInt(System.getProperty(HTTPS_PROXY_PORT));
                } else if (System.getProperty(HTTP_PROXY_HOST) != null && System.getProperty(HTTP_PROXY_PORT) != null) {
                    host = System.getProperty(HTTP_PROXY_HOST);
                    port = Integer.parseInt(System.getProperty(HTTP_PROXY_PORT));
                }
                if (host != null) {
                    clientBuilder.proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress(host, port)));
                }
            } catch (URISyntaxException e) { }
        }
        return clientBuilder.build();
    }

    @Override
    protected void afterTest() {
        if (!isSkipInPlayback) {
            cleanUpResources();
        }
    }

    private void addTextReplacementRules(Map<String, String> rules) {
        for (Map.Entry<String, String> entry : rules.entrySet()) {
            interceptorManager.addTextReplacementRule(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Sets sdk context when running the tests
     *
     * @param internalContext the internal runtime context
     * @param objects the manager classes to change internal context
     * @param <T> the type of internal context
     * @throws RuntimeException when field cannot be found or set.
     */
    protected <T> void setInternalContext(T internalContext, Object... objects) {
        try {
            for (Object obj : objects) {
                for (final Field field : obj.getClass().getSuperclass().getDeclaredFields()) {
                    if (field.getName().equals("resourceManager")) {
                        setAccessible(field);
                        Field context = field.get(obj).getClass().getDeclaredField("internalContext");
                        setAccessible(context);
                        context.set(field.get(obj), internalContext);
                    }
                }
                for (Field field : obj.getClass().getDeclaredFields()) {
                    if (field.getName().equals("internalContext")) {
                        setAccessible(field);
                        field.set(obj, internalContext);
                    } else if (field.getName().contains("Manager")) {
                        setAccessible(field);
                        setInternalContext(internalContext, field.get(obj));
                    }
                }
            }
        } catch (IllegalAccessException ex) {
            throw LOGGER.logExceptionAsError(new RuntimeException(ex));
        } catch (NoSuchFieldException ex) {
            throw LOGGER.logExceptionAsError(new RuntimeException(ex));
        }
    }

    @SuppressWarnings("removal")
    private void setAccessible(final AccessibleObject accessibleObject) {
        java.security.AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            accessibleObject.setAccessible(true);
            return null;
        });
    }

    /**
     * Builds the manager with provided http pipeline and profile in general manner.
     *
     * @param manager the class of the manager
     * @param httpPipeline the http pipeline
     * @param profile the azure profile
     * @param <T> the type of the manager
     * @return the manager instance
     * @throws RuntimeException when field cannot be found or set.
     */
    protected <T> T buildManager(Class<T> manager, HttpPipeline httpPipeline, AzureProfile profile) {
        try {
            Constructor<T> constructor = manager.getDeclaredConstructor(httpPipeline.getClass(), profile.getClass());
            setAccessible(constructor);
            return constructor.newInstance(httpPipeline, profile);

        } catch (NoSuchMethodException
            | IllegalAccessException
            | InstantiationException
            | InvocationTargetException ex) {
            throw LOGGER.logExceptionAsError(new RuntimeException(ex));
        }
    }

    /**
     * Builds an HttpPipeline.
     *
     * @param credential The credentials to use in the pipeline.
     * @param profile The AzureProfile to use in the pipeline.
     * @param httpLogOptions The HTTP logging options to use in the pipeline.
     * @param policies Additional policies to use in the pipeline.
     * @param httpClient The HttpClient to use in the pipeline.
     * @return A new constructed HttpPipeline.
     */
    protected abstract HttpPipeline buildHttpPipeline(
        TokenCredential credential,
        AzureProfile profile,
        HttpLogOptions httpLogOptions,
        List<HttpPipelinePolicy> policies,
        HttpClient httpClient);

    /**
     * Initializes service clients used in testing.
     *
     * @param httpPipeline The HttpPipeline to use in the clients.
     * @param profile The AzureProfile to use in the clients.
     */
    protected abstract void initializeClients(HttpPipeline httpPipeline, AzureProfile profile);

    /**
     * Cleans up resources.
     */
    protected abstract void cleanUpResources();
}
