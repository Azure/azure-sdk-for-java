// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientTestBase extends TestProxyTestBase {

    private boolean sanitizersRemoved = false;

    protected AIProjectClientBuilder getClientBuilder(HttpClient httpClient) {

        AIProjectClientBuilder builder = new AIProjectClientBuilder()
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);
        TestMode testMode = getTestMode();
        if (testMode != TestMode.LIVE) {
            addCustomMatchers();
            addTestRecordCustomSanitizers();
            if (!sanitizersRemoved) {
                interceptorManager.removeSanitizers("AZSDK3430", "AZSDK3493", "AZSDK2015");
                sanitizersRemoved = true;
            }
        }

        if (testMode == TestMode.PLAYBACK) {
            builder.endpoint("https://localhost:8080").credential(new MockTokenCredential());
        } else if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
                .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT"))
                .credential(new DefaultAzureCredentialBuilder().build());
        } else {
            builder.endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT"))
                .credential(new DefaultAzureCredentialBuilder().build());
        }

        String version = Configuration.getGlobalConfiguration().get("SERVICE_VERSION");
        ProjectsServiceVersion serviceVersion
            = version != null ? ProjectsServiceVersion.valueOf(version) : ProjectsServiceVersion.V2025_05_15_PREVIEW;
        builder.serviceVersion(serviceVersion);
        return builder;
    }

    private void addTestRecordCustomSanitizers() {

        ArrayList<TestProxySanitizer> sanitizers = new ArrayList<>();
        sanitizers.add(new TestProxySanitizer("$..key", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
        sanitizers.add(new TestProxySanitizer("$..endpoint", "https://.+?/api/projects/.+?/", "https://REDACTED/",
            TestProxySanitizerType.URL));
        sanitizers.add(new TestProxySanitizer("Content-Type",
            "(^multipart\\/form-data; boundary=[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{2})",
            "multipart\\/form-data; boundary=BOUNDARY", TestProxySanitizerType.HEADER));

        interceptorManager.addSanitizers(sanitizers);

    }

    private void addCustomMatchers() {
        interceptorManager.addMatchers(new CustomMatcher().setExcludedHeaders(Arrays.asList("Cookie", "Set-Cookie")));
    }

    protected Path getPath(String fileName) throws FileNotFoundException, URISyntaxException {
        URL resource = ClientTestBase.class.getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new FileNotFoundException("File not found");
        }

        File file = new File(resource.toURI());
        return file.toPath();
    }
}
