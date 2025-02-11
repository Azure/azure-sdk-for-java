// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documenttranslator;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.Configuration;

public class BatchDocumentTranslationClientTestBase extends TestProxyTestBase {

    BatchDocumentTranslationClient getClient() {
        BatchDocumentTranslationClientBuilder builder
            = new BatchDocumentTranslationClientBuilder().endpoint(getEndpoint())
                .credential(new AzureKeyCredential(getKey()));

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder.buildClient();
    }

    private String getKey() {
        return interceptorManager.isPlaybackMode()
            ? "fakeKeyPlaceholder"
            : Configuration.getGlobalConfiguration().get("DOCUMENT_TRANSLATION_API_KEY");
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get("DOCUMENT_TRANSLATION_ENDPOINT");
    }
}
