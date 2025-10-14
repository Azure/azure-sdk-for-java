// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.credential.TokenCredential;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzurePipelinesCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

class TokenCredentialUtil {

    /**
     * Gets a token credential for use in tests.
     * @param interceptorManager the interceptor manager
     * @return the TokenCredential
     */
    static TokenCredential getTestTokenCredential(InterceptorManager interceptorManager) {
        if (interceptorManager.isLiveMode()) {
            return getPipelineCredential();
        } else if (interceptorManager.isRecordMode()) {
            return new DefaultAzureCredentialBuilder().build();
        } else {
            return new MockTokenCredential();
        }
    }

    private static TokenCredential getPipelineCredential() {
        final String serviceConnectionId = getPropertyValue("AZURESUBSCRIPTION_SERVICE_CONNECTION_ID");
        final String clientId = getPropertyValue("AZURESUBSCRIPTION_CLIENT_ID");
        final String tenantId = getPropertyValue("AZURESUBSCRIPTION_TENANT_ID");
        final String systemAccessToken = getPropertyValue("SYSTEM_ACCESSTOKEN");

        if (CoreUtils.isNullOrEmpty(serviceConnectionId)
            || CoreUtils.isNullOrEmpty(clientId)
            || CoreUtils.isNullOrEmpty(tenantId)
            || CoreUtils.isNullOrEmpty(systemAccessToken)) {
            return null;
        }

        TokenCredential cred = new AzurePipelinesCredentialBuilder().systemAccessToken(systemAccessToken)
            .clientId(clientId)
            .tenantId(tenantId)
            .serviceConnectionId(serviceConnectionId)
            .build();

        return request -> Mono.defer(() -> cred.getToken(request)).subscribeOn(Schedulers.boundedElastic());
    }

    private static String getPropertyValue(String propertyName) {
        return Configuration.getGlobalConfiguration().get(propertyName, System.getenv(propertyName));
    }
}
