// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.test;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.TimeoutPolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.test.utils.ResourceNamer;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.test.policy.HttpDebugLoggingPolicy;
import com.azure.resourcemanager.test.policy.TextReplacementPolicy;
import com.azure.resourcemanager.test.utils.AuthFile;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
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
import java.security.AccessController;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Test base for resource manager SDK.
 */
public abstract class ResourceManagerTestBase extends TestBase {
    private static final String ZERO_SUBSCRIPTION = "00000000-0000-0000-0000-000000000000";
    private static final String ZERO_TENANT = "00000000-0000-0000-0000-000000000000";
    private static final String PLAYBACK_URI_BASE = "http://localhost:";
    private static final String AZURE_AUTH_LOCATION = "AZURE_AUTH_LOCATION";
    private static final String AZURE_TEST_LOG_LEVEL = "AZURE_TEST_LOG_LEVEL";
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
        new AzureEnvironment(Arrays.stream(AzureEnvironment.Endpoint.values())
            .collect(Collectors.toMap(AzureEnvironment.Endpoint::identifier, endpoint -> PLAYBACK_URI)))
    );
    private static final OutputStream EMPTY_OUTPUT_STREAM = new OutputStream() {
        @Override
        public void write(int b) {
        }
    };

    private static final ClientLogger LOGGER = new ClientLogger(ResourceManagerTestBase.class);
    private AzureProfile testProfile;
    private AuthFile testAuthFile;
    private boolean isSkipInPlayback;

    protected String generateRandomResourceName(String prefix, int maxLen) {
        return testResourceNamer.randomName(prefix, maxLen);
    }

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

    protected TokenCredential credentialFromFile() {
        return testAuthFile.getCredential();
    }

    protected String clientIdFromFile() {
        return testAuthFile.getClientId();
    }

    protected AzureProfile profile() {
        return testProfile;
    }

    protected boolean isPlaybackMode() {
        return getTestMode() == TestMode.PLAYBACK;
    }

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
            policies.add(new CookiePolicy());
            httpPipeline = buildHttpPipeline(
                null,
                testProfile,
                new HttpLogOptions().setLogLevel(httpLogDetailLevel),
                policies,
                interceptorManager.getPlaybackClient());
            textReplacementRules.put(PLAYBACK_URI_BASE + "1234", PLAYBACK_URI);
            addTextReplacementRules(textReplacementRules);
        } else {
            if (System.getenv(AZURE_AUTH_LOCATION) != null) { // Record mode
                final File credFile = new File(System.getenv(AZURE_AUTH_LOCATION));
                try {
                    testAuthFile = AuthFile.parse(credFile);
                } catch (IOException e) {
                    throw LOGGER.logExceptionAsError(new RuntimeException("Cannot parse auth file. Please check file format."));
                }
                credential = testAuthFile.getCredential();
                testProfile = new AzureProfile(testAuthFile.getTenantId(), testAuthFile.getSubscriptionId(), testAuthFile.getEnvironment());
            } else {
                Configuration configuration = Configuration.getGlobalConfiguration();
                String clientId = configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID);
                String tenantId = configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID);
                String clientSecret = configuration.get(Configuration.PROPERTY_AZURE_CLIENT_SECRET);
                String subscriptionId = configuration.get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID);
                if (clientId == null || tenantId == null || clientSecret == null || subscriptionId == null) {
                    throw LOGGER.logExceptionAsError(
                        new IllegalArgumentException("When running tests in record mode either 'AZURE_AUTH_LOCATION' or 'AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET and AZURE_SUBSCRIPTION_ID' needs to be set"));
                }

                credential = new ClientSecretCredentialBuilder()
                    .tenantId(tenantId)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .authorityHost(AzureEnvironment.AZURE.getActiveDirectoryEndpoint())
                    .build();
                testProfile = new AzureProfile(tenantId, subscriptionId, AzureEnvironment.AZURE);
            }

            List<HttpPipelinePolicy> policies = new ArrayList<>();
            policies.add(new TimeoutPolicy(Duration.ofMinutes(1)));
            policies.add(new CookiePolicy());
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
                generateHttpClientWithProxy(null, null));

            textReplacementRules.put(testProfile.getSubscriptionId(), ZERO_SUBSCRIPTION);
            textReplacementRules.put(testProfile.getTenantId(), ZERO_TENANT);
            textReplacementRules.put(AzureEnvironment.AZURE.getResourceManagerEndpoint(), PLAYBACK_URI + "/");
            textReplacementRules.put(AzureEnvironment.AZURE.getMicrosoftGraphEndpoint(), PLAYBACK_URI + "/");
            addTextReplacementRules(textReplacementRules);
        }
        initializeClients(httpPipeline, testProfile);
    }

    protected HttpClient generateHttpClientWithProxy(NettyAsyncHttpClientBuilder clientBuilder, ProxyOptions proxyOptions) {
        if (clientBuilder == null) {
            clientBuilder = new NettyAsyncHttpClientBuilder();
        }
        if (proxyOptions != null) {
            clientBuilder.proxy(proxyOptions);
        } else {
            try {
                System.setProperty(USE_SYSTEM_PROXY, VALUE_TRUE);
                List<Proxy> proxies = ProxySelector.getDefault().select(new URI(AzureEnvironment.AZURE.getResourceManagerEndpoint()));
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
    private void setAccessible(final Field field) {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            field.setAccessible(true);
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
    @SuppressWarnings("removal")
    protected <T> T buildManager(Class<T> manager, HttpPipeline httpPipeline, AzureProfile profile) {
        try {
            Constructor<T> constructor = manager.getDeclaredConstructor(httpPipeline.getClass(), profile.getClass());
            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                constructor.setAccessible(true);
                return null;
            });
            return constructor.newInstance(httpPipeline, profile);

        } catch (NoSuchMethodException
            | IllegalAccessException
            | InstantiationException
            | InvocationTargetException ex) {
            throw LOGGER.logExceptionAsError(new RuntimeException(ex));
        }
    }

    protected abstract HttpPipeline buildHttpPipeline(
        TokenCredential credential,
        AzureProfile profile,
        HttpLogOptions httpLogOptions,
        List<HttpPipelinePolicy> policies,
        HttpClient httpClient);

    protected abstract void initializeClients(HttpPipeline httpPipeline, AzureProfile profile);

    protected abstract void cleanUpResources();
}
