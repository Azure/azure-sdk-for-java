// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.administration.PersonalizerAdministrationClient;
import com.azure.ai.personalizer.administration.PersonalizerAdministrationClientBuilder;
import com.azure.ai.personalizer.administration.models.PersonalizerPolicy;
import com.azure.ai.personalizer.administration.models.PersonalizerServiceProperties;
import com.azure.ai.personalizer.models.PersonalizerAudience;
import com.azure.core.client.traits.AzureKeyCredentialTrait;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;

import static com.azure.ai.personalizer.TestUtils.INVALID_KEY;
import static com.azure.ai.personalizer.TestUtils.PERSONALIZER_API_KEY_MULTI_SLOT;
import static com.azure.ai.personalizer.TestUtils.PERSONALIZER_API_KEY_SINGLE_SLOT;
import static com.azure.ai.personalizer.TestUtils.PERSONALIZER_API_KEY_STATIC;
import static com.azure.ai.personalizer.TestUtils.PERSONALIZER_ENDPOINT_MULTI_SLOT;
import static com.azure.ai.personalizer.TestUtils.PERSONALIZER_ENDPOINT_SINGLE_SLOT;
import static com.azure.ai.personalizer.TestUtils.PERSONALIZER_ENDPOINT_STATIC;

public abstract class PersonalizerTestBase extends TestProxyTestBase {
    private PersonalizerAdministrationClientBuilder setBuilderProperties(HttpClient httpClient,
        PersonalizerServiceVersion serviceVersion, boolean isSingleSlot, boolean isStatic) {
        PersonalizerAdministrationClientBuilder builder = new PersonalizerAdministrationClientBuilder();

        String endpoint = getEndpoint(getTestMode(), isSingleSlot, isStatic);
        PersonalizerAudience audience = TestUtils.getAudience(endpoint);

        builder.endpoint(endpoint)
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .serviceVersion(serviceVersion)
            .audience(audience);

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (!interceptorManager.isLiveMode()) {
            // Removes `Location` and `id` sanitizer from the list of common sanitizers.
            interceptorManager.removeSanitizers("AZSDK2003", "AZSDK3430");
        }

        return setCredential(builder, getTestMode(), isSingleSlot, isStatic);
    }

    public PersonalizerClientBuilder setBuilderProperties(PersonalizerClientBuilder builder,
        HttpClient httpClient, PersonalizerServiceVersion serviceVersion, boolean isSingleSlot) {
        String endpoint = getEndpoint(getTestMode(), isSingleSlot, false);
        PersonalizerAudience audience = TestUtils.getAudience(endpoint);

        builder.endpoint(endpoint)
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion)
            .audience(audience);

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return setCredential(builder, getTestMode(), isSingleSlot, false);
    }

    public PersonalizerClientBuilder getPersonalizerClientBuilder(HttpClient httpClient,
        PersonalizerServiceVersion serviceVersion, boolean isSingleSlot) {
        PersonalizerClientBuilder builder = new PersonalizerClientBuilder();
        return setBuilderProperties(builder, httpClient, serviceVersion, isSingleSlot);
    }

    protected PersonalizerClient getClient(HttpClient httpClient, PersonalizerServiceVersion serviceVersion,
        boolean isSingleSlot) {
        PersonalizerAdministrationClient adminClient = getAdministrationClientBuilder(httpClient, serviceVersion,
            isSingleSlot)
            .buildClient();

        if (!isSingleSlot) {
            enableMultiSlot(adminClient);
        }

        return getPersonalizerClientBuilder(httpClient, serviceVersion, isSingleSlot)
            .buildClient();
    }

    protected PersonalizerAdministrationClientBuilder getAdministrationClientBuilder(HttpClient httpClient,
        PersonalizerServiceVersion serviceVersion, boolean isSingleSlot) {
        return setBuilderProperties(httpClient, serviceVersion, isSingleSlot, false);
    }

    protected PersonalizerAdministrationClientBuilder getStaticAdministrationClientBuilder(HttpClient httpClient,
        PersonalizerServiceVersion serviceVersion) {
        return setBuilderProperties(httpClient, serviceVersion, true, true); // Static is always single slot
    }

    private void enableMultiSlot(PersonalizerAdministrationClient adminClient) {
        PersonalizerPolicy policy = adminClient.getPolicy();
        if (policy.getArguments().contains("--ccb_explore_adf")) {
            return;
        }

        PersonalizerServiceProperties serviceProperties = adminClient.getServiceProperties();
        if (serviceProperties.isAutoOptimizationEnabled()) {
            serviceProperties.setIsAutoOptimizationEnabled(false);
            adminClient.updateProperties(serviceProperties);
            //sleep 30 seconds to allow settings to propagate
            sleepIfRunningAgainstService(30000);
        }

        adminClient.updatePolicy(new PersonalizerPolicy().setName("multislot")
            .setArguments("--ccb_explore_adf --epsilon 0.2 --power_t 0 -l 0.001 --cb_type mtr -q ::"));
        //sleep 30 seconds to allow settings to propagate
        sleepIfRunningAgainstService(30000);
    }

    private static String getEndpoint(TestMode testMode, boolean isSingleSlot, boolean isStatic) {
        if (testMode == TestMode.PLAYBACK) {
            return "https://fakeEndpoint.cognitiveservices.azure.com";
        } else if (isStatic) {
            return PERSONALIZER_ENDPOINT_STATIC;
        } else if (isSingleSlot) {
            return PERSONALIZER_ENDPOINT_SINGLE_SLOT;
        } else {
            return PERSONALIZER_ENDPOINT_MULTI_SLOT;
        }
    }

    private static <T extends AzureKeyCredentialTrait<T>> T setCredential(T builder, TestMode testMode,
        boolean isSingleSlot, boolean isStatic) {
        if (testMode == TestMode.PLAYBACK) {
            builder.credential(new AzureKeyCredential(INVALID_KEY));
        } else if (isStatic) {
            builder.credential(new AzureKeyCredential(PERSONALIZER_API_KEY_STATIC));
        } else if (isSingleSlot) {
            builder.credential(new AzureKeyCredential(PERSONALIZER_API_KEY_SINGLE_SLOT));
        } else {
            builder.credential(new AzureKeyCredential(PERSONALIZER_API_KEY_MULTI_SLOT));
        }

        return builder;
    }
}
