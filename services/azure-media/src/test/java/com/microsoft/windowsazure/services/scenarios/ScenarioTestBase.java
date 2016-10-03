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

import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.services.blob.BlobConfiguration;
import com.microsoft.windowsazure.services.media.MediaConfiguration;
import com.microsoft.windowsazure.services.queue.QueueConfiguration;

public abstract class ScenarioTestBase {
    protected static Configuration config;

    @Rule
    public SetupManager setupManager = new SetupManager();

    protected static void initializeConfig() {
        config = new Configuration();

        overrideWithEnv(config, BlobConfiguration.ACCOUNT_NAME);
        overrideWithEnv(config, BlobConfiguration.ACCOUNT_KEY);
        overrideWithEnv(config, BlobConfiguration.URI);

        overrideWithEnv(config, QueueConfiguration.ACCOUNT_NAME);
        overrideWithEnv(config, QueueConfiguration.ACCOUNT_KEY);
        overrideWithEnv(config, QueueConfiguration.URI);

        overrideWithEnv(config, MediaConfiguration.URI);
        overrideWithEnv(config, MediaConfiguration.OAUTH_URI);
        overrideWithEnv(config, MediaConfiguration.OAUTH_CLIENT_ID);
        overrideWithEnv(config, MediaConfiguration.OAUTH_CLIENT_SECRET);
        overrideWithEnv(config, MediaConfiguration.OAUTH_SCOPE);
        
        config = Configuration.getInstance();
        overrideWithEnv(config, MediaConfiguration.URI);
        overrideWithEnv(config, MediaConfiguration.OAUTH_URI);
        overrideWithEnv(config, MediaConfiguration.OAUTH_CLIENT_ID);
        overrideWithEnv(config, MediaConfiguration.OAUTH_CLIENT_SECRET);
        overrideWithEnv(config, MediaConfiguration.OAUTH_SCOPE);

        overrideWithEnv(config, QueueConfiguration.ACCOUNT_KEY,
                "media.queue.account.key");
        overrideWithEnv(config, QueueConfiguration.ACCOUNT_NAME,
                "media.queue.account.name");
        overrideWithEnv(config, QueueConfiguration.URI, "media.queue.uri");
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
