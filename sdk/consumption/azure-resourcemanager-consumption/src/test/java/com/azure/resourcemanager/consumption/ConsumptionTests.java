// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.consumption;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.consumption.models.Budget;
import com.azure.resourcemanager.consumption.models.ReservationRecommendation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

public class ConsumptionTests extends TestBase {

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void availabilityTest() {
        ConsumptionManager manager = ConsumptionManager
            .configure().withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE));

        String billingScope = "/providers/Microsoft.Billing/<>";
        // no billing scope in our test env

        String subscriptionScope = "/subscriptions/" + Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID);
        List<Budget> budgets = manager.budgets().list(subscriptionScope).stream().collect(Collectors.toList());
        List<ReservationRecommendation> reservationRecommendations = manager.reservationRecommendations().list(subscriptionScope).stream().collect(Collectors.toList());
    }
}
