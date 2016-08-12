/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.microsoft.azure.keyvault.extensions.test;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;

public class KeyVaultClientIntegrationTestBase {

    /**
     * The client instance that should be used on tests.
     */
    protected static KeyVaultClient keyVaultClient;

    /**
     * Primary vault URI, used for keys and secrets tests.
     */
    public static String getVaultUri() {
        return getLiveVaultUri1();
    }

    /**
     * Secondary vault URI, used to verify ability to transparently authenticate
     * against a different resource.
     */
    public static String getSecondaryVaultUri() {
        return getLiveVaultUri2();
    }

    private static String getLiveVaultUri1() {
        return getenvOrDefault("keyvault.vaulturi", "https://javasdktestvault.vault.azure.net");
    }

    private static String getLiveVaultUri2() {
        return getenvOrDefault("keyvault.vaulturi.alt", "https://javasdktestvault2.vault.azure.net");
    }

    private static String getenvOrDefault(String varName, String defValue) {
        String value = System.getenv(varName);
        return value != null ? value : defValue;
    }

    protected static void createKeyVaultClient() throws Exception {
        keyVaultClient = new KeyVaultClient(createTestCredentials());
    }

    private static ServiceClientCredentials createTestCredentials() throws Exception {
        return new KeyVaultCredentials() {

            @Override
            public String doAuthenticate(String authorization, String resource, String scope) {
                try {
                    AuthenticationResult authResult = getAccessToken(authorization, resource);
                    return authResult.getAccessToken();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }

    private static AuthenticationResult getAccessToken(String authorization, String resource) throws Exception {

        String clientId = System.getenv("arm.clientid");
        
        if (clientId == null) {
            throw new Exception("Please inform arm.clientid in the environment settings.");
        }

        String clientKey = System.getenv("arm.clientkey");
        String username = System.getenv("arm.username");
        String password = System.getenv("arm.password");
        
        AuthenticationResult result = null;
        ExecutorService service = null;
        try {
            service = Executors.newFixedThreadPool(1);
            AuthenticationContext context = new AuthenticationContext(authorization, false, service);

            Future<AuthenticationResult> future = null;

            if (clientKey != null && password == null) {
                ClientCredential credentials = new ClientCredential(clientId, clientKey);
                future = context.acquireToken(resource, credentials, null);
            }

            if (password != null && clientKey == null) {
                future = context.acquireToken(resource, clientId, username, password, null);
            }

            if (future == null) {
                throw new Exception("Missing or ambiguous credentials - please inform exactly one of arm.clientkey or arm.password in the environment settings.");
            }

            result = future.get();
        } finally {
            service.shutdown();
        }

        if (result == null) {
            throw new RuntimeException("authentication result was null");
        }
        return result;
    }

    protected static ObjectWriter jsonWriter;
    protected static ObjectReader jsonReader;

    @BeforeClass
    public static void setup() throws Exception {
        createKeyVaultClient();
        jsonWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        jsonReader = new ObjectMapper().reader();
    }

    @AfterClass
    public static void cleanup() throws Exception {
    }

    @Before
    public void beforeTest() throws Exception {
        //setupTest(getClass().getSimpleName() + "-" + "??");
    }

    @After
    public void afterTest() throws Exception {
        //resetTest(getClass().getSimpleName() + "-" + "??");
    }

}