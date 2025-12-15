// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification.batch;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.health.deidentification.DeidentificationClientBuilder;
import com.azure.health.deidentification.DeidentificationServiceVersion;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for Deid Services client tests.
 */
public class BatchOperationTestBase extends TestProxyTestBase {
    private boolean sanitizersRemoved = false;
    private static final String FAKE_STORAGE_ACCOUNT_SAS_URI
        = "https://fake_storage_account_sas_uri.blob.core.windows.net/container-sdk-dev-fakeid";
    protected static final String FAKE_JOB_NAME_WITH_NEXTLINK = "recordedJobWithNextLink";
    private static final String FAKE_NEXT_LINK
        = String.format("https://localhost:5020/jobs/%s/documents?api-version=%s&maxpagesize=2&continuationToken=1234",
            FAKE_JOB_NAME_WITH_NEXTLINK, DeidentificationServiceVersion.getLatest().getVersion());
    private static final String FAKE_CONTINUATION_TOKEN = "1234";

    protected DeidentificationClientBuilder getDeidServicesClientBuilder() {
        DeidentificationClientBuilder deidentificationClientBuilder = new DeidentificationClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("DEID_SERVICE_ENDPOINT", "https://localhost:8080"))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));
        if (interceptorManager.isPlaybackMode()) {
            List<String> excludedHeaders = new ArrayList<>();
            excludedHeaders.add("Connection");
            CustomMatcher customMatcher = new CustomMatcher().setExcludedHeaders(excludedHeaders);
            interceptorManager.addMatchers(customMatcher);
            deidentificationClientBuilder.httpClient(interceptorManager.getPlaybackClient())
                .credential(new MockTokenCredential());
        }
        if (interceptorManager.isRecordMode()) {
            deidentificationClientBuilder.addPolicy(interceptorManager.getRecordPolicy())
                .credential(new DefaultAzureCredentialBuilder().build())
                .httpClient(HttpClient.createDefault());
        }
        if (!interceptorManager.isLiveMode() && !sanitizersRemoved) {
            List<TestProxySanitizer> customSanitizer = new ArrayList<>();
            customSanitizer.add(new TestProxySanitizer("$..sourceLocation.location", "^(?!.*FAKE_STORAGE_ACCOUNT).*",
                FAKE_STORAGE_ACCOUNT_SAS_URI, TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..targetLocation.location", "^(?!.*FAKE_STORAGE_ACCOUNT).*",
                FAKE_STORAGE_ACCOUNT_SAS_URI, TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..nextLink", "^(?!.*fakedeidservice).*", FAKE_NEXT_LINK,
                TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("(?<=continuationToken=)[^&]+", FAKE_CONTINUATION_TOKEN,
                TestProxySanitizerType.URL));
            interceptorManager.addSanitizers(customSanitizer);
            interceptorManager.removeSanitizers("AZSDK3493", "AZSDK4001", "AZSDK3430", "AZSDK2003", "AZSDK2030");
            sanitizersRemoved = true;
        } else {
            deidentificationClientBuilder.credential(new DefaultAzureCredentialBuilder().build())
                .httpClient(HttpClient.createDefault());
        }
        return deidentificationClientBuilder;
    }

    String getJobName() {
        return testResourceNamer.randomName("job-", 16);
    }

    /**
     * Retrieves the storage account location URL based on the global configuration settings.
     * <p>
     * This method constructs the URL using the storage account name and storage container name
     * from the global configuration. The URL is in the following format:
     * {@code https://<STORAGE_ACCOUNT_NAME>.blob.core.windows.net/<STORAGE_CONTAINER_NAME>}
     * </p>
     *
     * @return The storage account location URL as a {@code String}.
     */
    String getStorageAccountLocation() {
        if (interceptorManager.isPlaybackMode()) {
            return FAKE_STORAGE_ACCOUNT_SAS_URI;
        }
        String sasUri = Configuration.getGlobalConfiguration().get("SAS_URI");
        if (sasUri != null && !sasUri.isEmpty()) {
            return sasUri;
        }
        return "https://" + Configuration.getGlobalConfiguration().get("HEALTHDATAAISERVICES_STORAGE_ACCOUNT_NAME")
            + ".blob.core.windows.net/"
            + Configuration.getGlobalConfiguration().get("HEALTHDATAAISERVICES_STORAGE_CONTAINER_NAME");
    }
}
