/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.ResourceManagementService;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.MockIntegrationTestBase;
import com.microsoft.windowsazure.core.Builder;
import com.microsoft.windowsazure.core.ServiceClient;
import com.microsoft.windowsazure.core.pipeline.apache.ApacheConfigurationProperties;
import com.microsoft.windowsazure.core.utils.KeyStoreType;
import com.microsoft.windowsazure.management.configuration.ARMManagementConfiguration;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.LoggingFilter;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;

import javax.naming.ServiceUnavailableException;
import java.net.URI;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class ResourceManagementIntegrationTestBase extends MockIntegrationTestBase{
    protected static ResourceManagementClient resourceManagementClient;

    protected static void createResourceManagementClient() throws Exception {
        Configuration config = createConfiguration();
        resourceManagementClient = ResourceManagementService.create(config);
        addClient((ServiceClient<?>) resourceManagementClient, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createResourceManagementClient();
                return null;
            }
        });
        addRegexRule("https://management.azure.com", MOCK_URI);
    }

    public static Configuration createConfiguration() throws Exception {
        String baseUri = System.getenv("arm.url");
        Configuration config = null;
        if (IS_MOCKED) {
            config = ManagementConfiguration.configure(
                    new URI(MOCK_URI),
                    MOCK_SUBSCRIPTION,
                    null,
                    null,
                    null
            );
            config = ARMManagementConfiguration.configure(
                    null,
                    config,
                    new URI(MOCK_URI),
                    MOCK_SUBSCRIPTION,
                    null);
        } else {
            config = ManagementConfiguration.configure(
                    baseUri != null ? new URI(baseUri) : null,
                    System.getenv(ManagementConfiguration.SUBSCRIPTION_ID),
                    System.getenv(ManagementConfiguration.KEYSTORE_PATH),
                    System.getenv(ManagementConfiguration.KEYSTORE_PASSWORD),
                    KeyStoreType.fromString(System.getenv(ManagementConfiguration.KEYSTORE_TYPE))
            );
            config = ARMManagementConfiguration.configure(
                    null,
                    config,
                    baseUri != null ? new URI(baseUri) : null,
                    System.getenv(ManagementConfiguration.SUBSCRIPTION_ID),
                    getAccessTokenFromUserCredentials(System.getenv("arm.username"), System.getenv("arm.password")).getAccessToken());
        }
        config.setProperty(ApacheConfigurationProperties.PROPERTY_RETRY_HANDLER, new DefaultHttpRequestRetryHandler());

        // add LoggingFilter to any pipeline that is created
        Builder.Registry builder = (Builder.Registry) config.getBuilder();
        builder.alter(ResourceManagementClient.class, Client.class, new Builder.Alteration<Client>() {
            @Override
            public Client alter(String profile, Client client, Builder builder, Map<String, Object> properties) {
                client.addFilter(new LoggingFilter());
                return client;
            }
        });
        return config;
    }

    protected static String randomString(int length) {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i=0; i<length; i++) {
            stringBuilder.append((char)('a' + random.nextInt(26)));
        }
        return stringBuilder.toString();
    }

    private static AuthenticationResult getAccessTokenFromUserCredentials(
            String username, String password) throws Exception {
        AuthenticationContext context = null;
        AuthenticationResult result = null;
        ExecutorService service = null;
        try {
            service = Executors.newFixedThreadPool(1);
            context = new AuthenticationContext(System.getenv("arm.aad.url") + System.getenv("arm.tenant"), false, service);
            Future<AuthenticationResult> future = context.acquireToken(
                    System.getenv(ManagementConfiguration.URI), System.getenv("arm.clientid"), username, password, null);
            result = future.get();
        } finally {
            service.shutdown();
        }

        if (result == null) {
            throw new ServiceUnavailableException(
                    "authentication result was null");
        }
        return result;
    }}