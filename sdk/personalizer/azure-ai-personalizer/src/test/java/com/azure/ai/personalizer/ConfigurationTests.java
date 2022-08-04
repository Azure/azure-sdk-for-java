// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.models.PolicyContract;
import com.azure.ai.personalizer.models.ServiceConfiguration;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;

import static com.azure.ai.personalizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigurationTests extends PersonalizerTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.personalizer.TestUtils#getTestParameters")
    public final void configurationTests(HttpClient httpClient, PersonalizerServiceVersion serviceVersion) {
        Duration newExperimentalUnitDuration = Duration.ofHours(4);
        Duration modelExportFrequency = Duration.ofHours(3);
        double newDefaultReward = 1.0;
        String newRewardFunction = "average";
        float newExplorationPercentage = 0.2f;
        ServiceConfiguration properties = new ServiceConfiguration()
            .setRewardAggregation(newRewardFunction)
            .setModelExportFrequency(modelExportFrequency)
            .setDefaultReward((float) newDefaultReward)
            .setRewardWaitTime(newExperimentalUnitDuration)
            .setExplorationPercentage(newExplorationPercentage)
            .setLogRetentionDays(Integer.MAX_VALUE);
        PersonalizerAdminClient client = getAdministrationClient(httpClient, serviceVersion, true);
        updateProperties(client, properties);
        getProperties(client, properties);
        updateAndGetPolicy(client);
        resetPolicy(client);
    }

    private void getProperties(PersonalizerAdminClient client, ServiceConfiguration properties)
    {
        ServiceConfiguration result = client.getProperties();
        assertEquals(properties.getDefaultReward(), result.getDefaultReward());
        assertTrue(Math.abs(properties.getExplorationPercentage() - result.getExplorationPercentage()) < 1e-3);
        assertEquals(properties.getModelExportFrequency(), result.getModelExportFrequency());
        assertEquals(properties.getRewardAggregation(), result.getRewardAggregation());
        assertEquals(properties.getRewardWaitTime(), result.getRewardWaitTime());
    }

    private void updateProperties(PersonalizerAdminClient client, ServiceConfiguration properties)
    {
        ServiceConfiguration result = client.updateProperties(properties);
        assertEquals(properties.getDefaultReward(), result.getDefaultReward());
        assertTrue(Math.abs(properties.getExplorationPercentage() - result.getExplorationPercentage()) < 1e-3);
        assertEquals(properties.getModelExportFrequency(), result.getModelExportFrequency());
        assertEquals(properties.getRewardAggregation(), result.getRewardAggregation());
        assertEquals(properties.getRewardWaitTime(), result.getRewardWaitTime());
    }

    private void updateAndGetPolicy(PersonalizerAdminClient client)
    {
        PolicyContract newPolicy = new PolicyContract()
            .setName("app1")
            .setArguments("--cb_explore_adf --quadratic GT --quadratic MR --quadratic GR --quadratic ME --quadratic OT --quadratic OE --quadratic OR --quadratic MS --quadratic GX --ignore A --cb_type ips --epsilon 0.2");
        PolicyContract updatedPolicy = client.updatePolicy(newPolicy);
        assertNotNull(updatedPolicy);
        assertEquals(newPolicy.getArguments(), updatedPolicy.getArguments());
        PolicyContract policy = client.getPolicy();
        // Only checking the first 190 chars because the epsilon has a float rounding addition when applied
        assertEquals(newPolicy.getArguments(), policy.getArguments().substring(0, 190));
    }

    private void resetPolicy(PersonalizerAdminClient client)
    {
        PolicyContract policy = client.resetPolicy();
        assertEquals("--cb_explore_adf --epsilon 0.2 --power_t 0 -l 0.001 --cb_type mtr -q ::",
            policy.getArguments());
    }
}
