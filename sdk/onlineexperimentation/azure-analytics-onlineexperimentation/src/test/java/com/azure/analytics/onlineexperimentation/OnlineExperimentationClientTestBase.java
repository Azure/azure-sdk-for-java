// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

// The Java test files under 'generated' package are generated for your reference.
// If you wish to modify these files, please copy them out of the 'generated' package, and modify there.
// See https://aka.ms/azsdk/dpg/java/tests for guide on adding a test.

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

class OnlineExperimentationClientTestBase extends TestProxyTestBase {

    protected OnlineExperimentationClientBuilder getExperimentationClientBuilder() {
        OnlineExperimentationClientBuilder onlineExperimentationClientbuilder = new OnlineExperimentationClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration()
                .get("ONLINEEXPERIMENTATION_ENDPOINT", "https://testWorkspaceId.eastus2.exp.azure.net"))
            .httpClient(getHttpClientOrUsePlayback(getHttpClients().findFirst().orElse(null)))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        interceptorManager.removeSanitizers("AZSDK3430"); // don't sanitize "id" property in request body

        if (getTestMode() == TestMode.PLAYBACK) {
            onlineExperimentationClientbuilder.credential(new MockTokenCredential());
        } else if (getTestMode() == TestMode.RECORD) {
            onlineExperimentationClientbuilder.addPolicy(interceptorManager.getRecordPolicy())
                .credential(new DefaultAzureCredentialBuilder().build());
        } else if (getTestMode() == TestMode.LIVE) {
            onlineExperimentationClientbuilder.credential(new DefaultAzureCredentialBuilder().build());
        }

        return onlineExperimentationClientbuilder;
    }
}
