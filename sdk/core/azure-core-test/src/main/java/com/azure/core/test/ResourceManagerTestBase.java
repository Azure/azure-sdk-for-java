// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.test;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HostPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.TimeoutPolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.utils.AuthFile;
import com.azure.core.test.utils.ResourceNamer;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.ClientSecretCredentialBuilder;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Base class for resource manager modules to run live and playback tests using {@link InterceptorManager}.
 */
public abstract class ResourceManagerTestBase extends TestBase {

    protected static final String ZERO_SUBSCRIPTION = "00000000-0000-0000-0000-000000000000";
    protected static final String ZERO_TENANT = "00000000-0000-0000-0000-000000000000";
    private static final String PLAYBACK_URI_BASE = "http://localhost:";
    private static final String AZURE_AUTH_LOCATION = "AZURE_AUTH_LOCATION";
    private static final String HTTPS_PROXY_HOST = "https.proxyHost";
    private static final String HTTPS_PROXY_PORT = "https.proxyPort";
    private static final String HTTP_PROXY_HOST = "http.proxyHost";
    private static final String HTTP_PROXY_PORT = "http.proxyPort";
    private static final String USE_SYSTEM_PROXY = "java.net.useSystemProxies";
    private static final String VALUE_TRUE = "true";
    protected String playbackUri;

    private final ClientLogger logger = new ClientLogger(ResourceManagerTestBase.class);

    protected String generateRandomResourceName(String prefix, int maxLen) {
        return testResourceNamer.randomName(prefix, maxLen);
    }

    protected String generateRandomPassword() {
        // do not record
        String password = new ResourceNamer("").randomName("Pa5$", 12);
        System.out.printf("Password: %s%n", password);
        return password;
    }

    @Override
    protected void beforeTest() {
        TokenCredential credential;
        HttpPipeline httpPipeline;
        AzureProfile profile;

        playbackUri = PLAYBACK_URI_BASE + "1234";
        if (getTestMode() == TestMode.PLAYBACK) {
            profile = new AzureProfile(
                ZERO_TENANT, ZERO_SUBSCRIPTION,
                new AzureEnvironment(
                    new HashMap<String, String>() {
                        {
                            put("managementEndpointUrl", playbackUri);
                            put("resourceManagerEndpointUrl", playbackUri);
                            put("sqlManagementEndpointUrl", playbackUri);
                            put("galleryEndpointUrl", playbackUri);
                            put("activeDirectoryEndpointUrl", playbackUri);
                            put("activeDirectoryResourceId", playbackUri);
                            put("activeDirectoryGraphResourceId", playbackUri);
                        }}));

            List<HttpPipelinePolicy> policies = new ArrayList<>();
            policies.add(interceptorManager.getRecordReplacementPolicy());
            policies.add(new HostPolicy(playbackUri + "/"));
            policies.add(new CookiePolicy());
            httpPipeline = buildHttpPipeline(null, profile, policies, interceptorManager.getPlaybackClient());

            interceptorManager.addTextReplacementRule(PLAYBACK_URI_BASE + "1234", playbackUri);
            System.out.println(playbackUri);
        } else {
            if (System.getenv(AZURE_AUTH_LOCATION) != null) { // Record mode
                final File credFile = new File(System.getenv(AZURE_AUTH_LOCATION));
                AuthFile authFile;
                try {
                    authFile = AuthFile.parse(credFile);
                } catch (IOException e) {
                    throw logger.logExceptionAsError(new RuntimeException("Cannot parse auth file. Please check file format."));
                }
                credential = authFile.getCredential();
                profile = new AzureProfile(authFile.getTenantId(), authFile.getSubscriptionId(), authFile.getEnvironment());
            } else {
                Configuration configuration = Configuration.getGlobalConfiguration();
                String clientId = configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID);
                String tenantId = configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID);
                String clientSecret = configuration.get(Configuration.PROPERTY_AZURE_CLIENT_SECRET);
                String subscriptionId = configuration.get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID);
                if (clientId == null || tenantId == null || clientSecret == null || subscriptionId == null) {
                    throw logger.logExceptionAsError(
                        new IllegalArgumentException("When running tests in record mode either 'AZURE_AUTH_LOCATION' or 'AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET and AZURE_SUBSCRIPTION_ID' needs to be set"));
                }

                credential = new ClientSecretCredentialBuilder()
                    .tenantId(tenantId)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .authorityHost(AzureEnvironment.AZURE.getActiveDirectoryEndpoint())
                    .build();
                profile = new AzureProfile(tenantId, subscriptionId, AzureEnvironment.AZURE);
            }

            List<HttpPipelinePolicy> policies = new ArrayList<>();
            policies.add(new TimeoutPolicy(Duration.ofMinutes(1)));
            policies.add(new CookiePolicy());
            if (!interceptorManager.isLiveMode()) {
                policies.add(interceptorManager.getRecordReplacementPolicy());
            }
            httpPipeline = buildHttpPipeline(
                credential, profile, policies, generateHttpClientWithProxy(null));

            interceptorManager.addTextReplacementRule(profile.getSubscriptionId(), ZERO_SUBSCRIPTION);
            interceptorManager.addTextReplacementRule(profile.getTenantId(), ZERO_TENANT);
            interceptorManager.addTextReplacementRule(AzureEnvironment.AZURE.getResourceManagerEndpoint(), playbackUri + "/");
            interceptorManager.addTextReplacementRule(AzureEnvironment.AZURE.getGraphEndpoint(), playbackUri + "/");
        }
        initializeClients(httpPipeline, profile);
    }

    private HttpClient generateHttpClientWithProxy(ProxyOptions proxyOptions) {
        NettyAsyncHttpClientBuilder clientBuilder = new NettyAsyncHttpClientBuilder();
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
        cleanUpResources();
    }

    protected abstract HttpPipeline buildHttpPipeline(
        TokenCredential credential, AzureProfile profile, List<HttpPipelinePolicy> policies, HttpClient httpClient);

    protected abstract void initializeClients(HttpPipeline httpPipeline, AzureProfile profile);

    protected abstract void cleanUpResources();
}
