// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.oracledatabase;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Context;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.resourcemanager.oracledatabase.fluent.models.OracleSubscriptionInner;
import com.azure.resourcemanager.oracledatabase.models.OracleSubscription;
import com.azure.resourcemanager.oracledatabase.models.OracleSubscriptionProperties;
import com.azure.resourcemanager.oracledatabase.models.Plan;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OracleDatabaseManagerTests extends TestProxyTestBase {
    private OracleDatabaseManager oracleDatabaseManager = null;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        oracleDatabaseManager = OracleDatabaseManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .authenticate(credential, profile);
    }

    @Test
    @LiveOnly
    public void testCreateOracleSubscription() {
        try {
            // @embedStart
            OracleSubscription oracleSubscription = oracleDatabaseManager.oracleSubscriptions()
                .createOrUpdate(
                    new OracleSubscriptionInner()
                        .withProperties(new OracleSubscriptionProperties())
                        .withPlan(
                            new Plan()
                                .withName("plan1")
                                .withPublisher("publisher1")
                                .withProduct("product1")
                                .withPromotionCode("fakeTokenPlaceholder")
                                .withVersion("alpha")),
                    Context.NONE);
            // @embedEnd
            Assertions.assertEquals("default", oracleSubscription.name());
            Assertions.assertEquals(oracleSubscription.name(), oracleDatabaseManager.oracleSubscriptions().get().name());
            Assertions.assertTrue(oracleDatabaseManager.oracleSubscriptions().list().stream().findAny().isPresent());
        } finally {
            oracleDatabaseManager.oracleSubscriptions().delete();
        }
    }
}
