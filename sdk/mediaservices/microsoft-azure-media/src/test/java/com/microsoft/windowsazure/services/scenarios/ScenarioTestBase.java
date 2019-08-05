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

package com.microsoft.windowsazure.services.scenarios;

import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.services.blob.BlobConfiguration;
import com.microsoft.windowsazure.services.media.MediaConfiguration;
import com.microsoft.windowsazure.services.media.authentication.AzureAdClientSymmetricKey;
import com.microsoft.windowsazure.services.media.authentication.AzureAdTokenCredentials;
import com.microsoft.windowsazure.services.media.authentication.AzureAdTokenProvider;
import com.microsoft.windowsazure.services.media.authentication.AzureEnvironments;
import com.microsoft.windowsazure.services.media.authentication.TokenProvider;
import com.microsoft.windowsazure.services.queue.QueueConfiguration;

public abstract class ScenarioTestBase {
    protected static Configuration config;
    protected static ExecutorService executorService;

    @Rule
    public SetupManager setupManager = new SetupManager();

    protected static void initializeConfig() {
    	executorService = Executors.newFixedThreadPool(5);
    	config = Configuration.getInstance();
    	
    	String tenant = config.getProperty("media.azuread.test.tenant").toString();
    	String clientId = config.getProperty("media.azuread.test.clientid").toString();
    	String clientKey = config.getProperty("media.azuread.test.clientkey").toString();
    	String apiserver = config.getProperty("media.azuread.test.account_api_uri").toString();
    	
    	// Setup Azure AD Credentials (in this case using username and password)
    	AzureAdTokenCredentials credentials = new AzureAdTokenCredentials(
    			tenant,
    			new AzureAdClientSymmetricKey(clientId, clientKey), 
    			AzureEnvironments.AZURE_CLOUD_ENVIRONMENT);
    	
    	TokenProvider provider = null;
    	
		try {
			provider = new AzureAdTokenProvider(credentials, executorService);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
    	
    	// configure the account endpoint and the token provider for injection
    	config.setProperty(MediaConfiguration.AZURE_AD_API_SERVER, apiserver);
    	config.setProperty(MediaConfiguration.AZURE_AD_TOKEN_PROVIDER, provider);

        overrideWithEnv(config, QueueConfiguration.ACCOUNT_KEY,
                "media.queue.account.key");
        overrideWithEnv(config, QueueConfiguration.ACCOUNT_NAME,
                "media.queue.account.name");
        overrideWithEnv(config, QueueConfiguration.URI, "media.queue.uri");
    }
    
    protected static void cleanupConfig() {
    	
        // shutdown the executor service required by ADAL4J
        executorService.shutdown();
	}

    private static void overrideWithEnv(Configuration config, String key) {
        String value = System.getenv(key);
        if (value == null)
            return;

        config.setProperty(key, value);
    }
    
    protected static void overrideWithEnv(Configuration config, String key,
            String enviromentKey) {
        String value = System.getenv(enviromentKey);
        if (value == null)
            return;

        config.setProperty(key, value);
    }

    protected void signalSetupStarting() {
        setupManager.startSetup();
    }

    protected void signalSetupFinished() {
        setupManager.endSetup();
    }

    protected class SetupManager implements MethodRule {
        private boolean shouldCapture;

        @Override
        public Statement apply(Statement base, FrameworkMethod method,
                Object target) {
            return new SetupManagerStatement(base);
        }

        public void startSetup() {
            this.shouldCapture = true;
        }

        public void endSetup() {
            this.shouldCapture = false;
        }

        private class SetupManagerStatement extends Statement {
            private final Statement next;

            public SetupManagerStatement(Statement base) {
                next = base;
            }

            @Override
            public void evaluate() throws Throwable {
                try {
                    next.evaluate();
                } catch (Throwable e) {
                    if (shouldCapture) {
                        // e.printStackTrace();
                        fail("Error occured during setup: " + e.getMessage());
                    } else {
                        throw e;
                    }
                }
            }
        }
    }	
}
