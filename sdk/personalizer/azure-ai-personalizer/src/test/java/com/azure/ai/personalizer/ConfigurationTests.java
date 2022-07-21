package com.azure.ai.personalizer;

import com.azure.ai.personalizer.implementation.models.PolicyContract;
import com.azure.ai.personalizer.implementation.models.ServiceConfiguration;
import com.azure.core.credential.AzureKeyCredential;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigurationTests {
    @Test
    public final void ConfigurationTests() {
        Duration newExperimentalUnitDuration = Duration.ofHours(4);
        Duration modelExportFrequency = Duration.ofHours(3);
        double newDefaultReward = 1.0;
        String newRewardFunction = "average";
        float newExplorationPercentage = 0.2f;
        ServiceConfiguration properties = new ServiceConfiguration()
            .setRewardAggregation(newRewardFunction)
            .setModelExportFrequency(modelExportFrequency)
            .setDefaultReward((float)newDefaultReward)
            .setRewardWaitTime(newExperimentalUnitDuration)
            .setExplorationPercentage(newExplorationPercentage)
            .setLogRetentionDays(Integer.MAX_VALUE);
        PersonalizerAdminClient client = GetAdministrationClient();
        UpdateProperties(client, properties);
        GetProperties(client, properties);
        UpdateAndGetPolicy(client);
        ResetPolicy(client);
    }

    private PersonalizerAdminClient GetAdministrationClient() {
        return new PersonalizerClientBuilder()
            .credential(new AzureKeyCredential("{apikey}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAdminClient();
    }


    private void GetProperties(PersonalizerAdminClient client, ServiceConfiguration properties)
    {
        ServiceConfiguration result = client.getProperties();
        assertEquals(properties.getDefaultReward(), result.getDefaultReward());
        assertTrue(Math.abs(properties.getExplorationPercentage() - result.getExplorationPercentage()) < 1e-3);
        assertEquals(properties.getModelExportFrequency(), result.getModelExportFrequency());
        assertEquals(properties.getRewardAggregation(), result.getRewardAggregation());
        assertEquals(properties.getRewardWaitTime(), result.getRewardWaitTime());
    }

    private void UpdateProperties(PersonalizerAdminClient client, ServiceConfiguration properties)
    {
        ServiceConfiguration result = client.updateProperties(properties);
        assertEquals(properties.getDefaultReward(), result.getDefaultReward());
        assertTrue(Math.abs(properties.getExplorationPercentage() - result.getExplorationPercentage()) < 1e-3);
        assertEquals(properties.getModelExportFrequency(), result.getModelExportFrequency());
        assertEquals(properties.getRewardAggregation(), result.getRewardAggregation());
        assertEquals(properties.getRewardWaitTime(), result.getRewardWaitTime());
    }

    private void UpdateAndGetPolicy(PersonalizerAdminClient client)
    {
        var newPolicy = new PolicyContract()
            .setName("app1")
            .setArguments("--cb_explore_adf --quadratic GT --quadratic MR --quadratic GR --quadratic ME --quadratic OT --quadratic OE --quadratic OR --quadratic MS --quadratic GX --ignore A --cb_type ips --epsilon 0.2");
        PolicyContract updatedPolicy = client.updatePolicy(newPolicy);
        assertNotNull(updatedPolicy);
        assertEquals(newPolicy.getArguments(), updatedPolicy.getArguments());
        PolicyContract policy = client.getPolicy();
        // Only checking the first 190 chars because the epsilon has a float rounding addition when applied
        assertEquals(newPolicy.getArguments(), policy.getArguments().substring(0,190));
    }

    private void ResetPolicy(PersonalizerAdminClient client)
    {
        PolicyContract policy = client.resetPolicy();
        assertEquals("--cb_explore_adf --epsilon 0.2 --power_t 0 -l 0.001 --cb_type mtr -q ::",
            policy.getArguments());
    }}
