// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.deviceregistry.softwareupdate;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.ArrayList;
import java.util.List;

abstract class SoftwareUpdateTestBase extends TestProxyTestBase {
    protected static final String PLAYBACK_ENDPOINT = "fake.api.adu.microsoft.com";
    protected static final String PLAYBACK_MANIFEST_URL
        = "https://fake.blob.core.windows.net/container/manifest.json?sanitized";
    protected static final String PLAYBACK_PAYLOAD_URL
        = "https://fake.blob.core.windows.net/container/README.md?sanitized";

    private static final String SERVICE_HOST = "adugen3cuse-unhappy-2.api.dev.adu.microsoft.com";
    private static final String PLAYBACK_HOST = "fake.api.adu.microsoft.com";
    private static final String OPERATION_ID = "00000000-0000-0000-0000-000000000000";
    private static final String UUID_REGEX
        = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

    protected SoftwareUpdateClient softwareUpdateClient;
    protected DeviceClassesClient deviceClassesClient;

    @Override
    protected void beforeTest() {
        DeviceRegistrySoftwareUpdateClientBuilder builder
            = new DeviceRegistrySoftwareUpdateClientBuilder().endpoint(getEndpoint())
                .credential(getCredential())
                .httpClient(getTestHttpClient())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        if (!interceptorManager.isLiveMode()) {
            interceptorManager.removeSanitizers("AZSDK2003", "AZSDK2030", "AZSDK3430", "AZSDK3493");
        }

        if (interceptorManager.isRecordMode()) {
            addSanitizers();
        }

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        softwareUpdateClient = builder.buildSoftwareUpdateClient();
        deviceClassesClient = builder.buildDeviceClassesClient();
    }

    protected String getRequiredConfiguration(String name) {
        String value = Configuration.getGlobalConfiguration().get(name);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException(name + " must be set when recording or running live tests.");
        }
        return value;
    }

    protected long getRequiredLongConfiguration(String name) {
        String value = getRequiredConfiguration(name);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(name + " must contain a valid long value.", exception);
        }
    }

    private String getEndpoint() {
        if (getTestMode() == TestMode.PLAYBACK) {
            return PLAYBACK_ENDPOINT;
        }
        return getRequiredConfiguration("DEVICE_REGISTRY_SOFTWARE_UPDATE_ENDPOINT").replaceFirst("^https?://", "")
            .replaceFirst("/+$", "");
    }

    private TokenCredential getCredential() {
        if (getTestMode() == TestMode.PLAYBACK) {
            return new MockTokenCredential();
        }
        if (Configuration.getGlobalConfiguration().get("AZURE_IDENTITY_TEST_MODE", "default").equals("cli")) {
            return new AzureCliCredentialBuilder().build();
        }
        return new DefaultAzureCredentialBuilder().build();
    }

    private HttpClient getTestHttpClient() {
        if (getTestMode() == TestMode.PLAYBACK) {
            return interceptorManager.getPlaybackClient();
        }
        return getHttpClientOrUsePlayback(getHttpClients().findFirst().orElse(null));
    }

    private void addSanitizers() {
        List<TestProxySanitizer> sanitizers = new ArrayList<>();
        sanitizers.add(new TestProxySanitizer(SERVICE_HOST, PLAYBACK_HOST, TestProxySanitizerType.URL));
        sanitizers.add(new TestProxySanitizer(UUID_REGEX, OPERATION_ID, TestProxySanitizerType.URL));
        sanitizers.add(new TestProxySanitizer(
            "https://tlsbugbashdevtest\\.blob\\.core\\.windows\\.net/adu-import-contoso-toaster-1-0-20260720/"
                + "Contoso\\.Toaster\\.1\\.0/contoso\\.toaster\\.1\\.0\\.importmanifest\\.json[^\\\"]*",
            PLAYBACK_MANIFEST_URL, TestProxySanitizerType.BODY_REGEX));
        sanitizers.add(new TestProxySanitizer(
            "https://tlsbugbashdevtest\\.blob\\.core\\.windows\\.net/adu-import-contoso-toaster-1-0-20260720/"
                + "Contoso\\.Toaster\\.1\\.0/README\\.md[^\\\"]*",
            PLAYBACK_PAYLOAD_URL, TestProxySanitizerType.BODY_REGEX));
        sanitizers.add(new TestProxySanitizer("sig=([^&\\\"]+)", "sig=REDACTED", TestProxySanitizerType.URL));
        sanitizers
            .add(new TestProxySanitizer("operation-location", UUID_REGEX, OPERATION_ID, TestProxySanitizerType.HEADER));
        sanitizers.add(new TestProxySanitizer("Location", UUID_REGEX, OPERATION_ID, TestProxySanitizerType.HEADER));

        sanitizers.add(new TestProxySanitizer("$..operationId", null, OPERATION_ID, TestProxySanitizerType.BODY_KEY));
        sanitizers.add(new TestProxySanitizer("$..traceId", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
        sanitizers.add(new TestProxySanitizer("$..etag", null, "REDACTED", TestProxySanitizerType.BODY_KEY));

        for (String header : new String[] {
            "Authorization",
            "etag",
            "x-ms-request-id",
            "x-ms-client-request-id",
            "x-ms-correlation-request-id",
            "traceparent",
            "tracestate" }) {
            sanitizers.add(new TestProxySanitizer(header, null, "REDACTED", TestProxySanitizerType.HEADER));
        }
        interceptorManager.addSanitizers(sanitizers);
    }
}
