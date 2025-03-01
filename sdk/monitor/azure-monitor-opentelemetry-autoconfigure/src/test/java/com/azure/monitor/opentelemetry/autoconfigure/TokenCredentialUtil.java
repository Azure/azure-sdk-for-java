// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure;

import com.azure.core.credential.TokenCredential;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

class TokenCredentialUtil {

    /**
     * Gets a token credential for use in tests.
     * @param interceptorManager the interceptor manager
     * @return the TokenCredential
     */
    static TokenCredential getTestTokenCredential(InterceptorManager interceptorManager) {
        if (interceptorManager.isLiveMode()) {
            return new AzurePowerShellCredentialBuilder().build();
        } else if (interceptorManager.isRecordMode()) {
            return new DefaultAzureCredentialBuilder().build();
        } else {
            return new MockTokenCredential();
        }
    }

}
