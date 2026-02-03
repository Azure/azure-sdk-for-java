// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.test.utils;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.TestMode;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.AzureDeveloperCliCredentialBuilder;
import com.azure.identity.AzurePipelinesCredentialBuilder;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.EnvironmentCredentialBuilder;

import java.util.Iterator;

/**
 * Common utility functions for the tests.
 */
public final class TestUtilities {

    private TestUtilities() {

    }

    /**
     * Wrapper on the ResourceManagerUtils.InternalRuntimeContext.sleep, in case of record mode will not sleep, otherwise sleep.
     *
     * @param milliseconds time in milliseconds for which to sleep.
     * @param isRecordMode the value indicates whether it is record mode.
     */
    public static void sleep(int milliseconds, boolean isRecordMode) {
        if (isRecordMode) {
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Return the size of Iterable collection.
     *
     * @param iterable the Iterable collection.
     * @param <T> the type of the resource
     * @return the size of the collection.
     */
    public static <T> int getSize(Iterable<T> iterable) {
        int res = 0;
        Iterator<T> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            ++res;
        }
        return res;
    }

    /**
     * Return whether the Iterable collection is empty.
     *
     * @param iterable the Iterable collection.
     * @param <T> the type of the resource
     * @return if the collection is empty.
     */
    public static <T> boolean isEmpty(PagedIterable<T> iterable) {
        return !iterable.iterator().hasNext();
    }

    /**
     * Creates a comprehensive {@link TokenCredential} chain optimized for test environments.
     * <p>
     * This method constructs a credential chain that attempts multiple authentication methods
     * in a specific order, making it suitable for various testing scenarios including local
     * development and live tests in CI/CD pipelines.
     * </p>
     *
     * <strong>Azure Pipelines Configuration:</strong><br>
     * For Azure Pipelines authentication, the following environment variables must be set:
     * <ul>
     *   <li>{@code AZURESUBSCRIPTION_SERVICE_CONNECTION_ID}</li>
     *   <li>{@code AZURESUBSCRIPTION_CLIENT_ID}</li>
     *   <li>{@code AZURESUBSCRIPTION_TENANT_ID}</li>
     *   <li>{@code SYSTEM_ACCESSTOKEN}</li>
     * </ul>
     *
     * <strong>Local run Configuration:</strong><br>
     * For local run authentication, use Azure CLI for example, the following environment variables must be set:
     * <ul>
     *   <li>{@code AZURE_SUBSCRIPTION_ID}</li>
     *   <li>{@code AZURE_TENANT_ID}</li>
     * </ul>
     *
     * @param testMode {@link TestMode} that the test is running in, usually set through {@code AZURE_TEST_MODE} env var
     * @return a {@link TokenCredential} appropriate for the test environment:
     *         {@link MockTokenCredential} for playback mode, or a
     *         {@link com.azure.identity.ChainedTokenCredential} for live testing
     * @see MockTokenCredential
     * @see com.azure.identity.ChainedTokenCredential
     */
    public static TokenCredential getTokenCredentialForTest(TestMode testMode) {
        if (testMode == TestMode.LIVE) {
            Configuration config = Configuration.getGlobalConfiguration();

            ChainedTokenCredentialBuilder builder
                = new ChainedTokenCredentialBuilder().addLast(new EnvironmentCredentialBuilder().build())
                    .addLast(new AzureCliCredentialBuilder().build())
                    .addLast(new AzureDeveloperCliCredentialBuilder().build());

            String serviceConnectionId = config.get("AZURESUBSCRIPTION_SERVICE_CONNECTION_ID");
            String clientId = config.get("AZURESUBSCRIPTION_CLIENT_ID");
            String tenantId = config.get("AZURESUBSCRIPTION_TENANT_ID");
            String systemAccessToken = config.get("SYSTEM_ACCESSTOKEN");

            if (!CoreUtils.isNullOrEmpty(serviceConnectionId)
                && !CoreUtils.isNullOrEmpty(clientId)
                && !CoreUtils.isNullOrEmpty(tenantId)
                && !CoreUtils.isNullOrEmpty(systemAccessToken)) {

                builder.addLast(new AzurePipelinesCredentialBuilder().systemAccessToken(systemAccessToken)
                    .clientId(clientId)
                    .tenantId(tenantId)
                    .serviceConnectionId(serviceConnectionId)
                    .build());
            }

            builder.addLast(new AzurePowerShellCredentialBuilder().build());
            return builder.build();
        } else if (testMode == TestMode.RECORD) {
            return new DefaultAzureCredentialBuilder().build();
        } else {
            return new MockTokenCredential();
        }
    }
}
