// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.workflows.customer;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.ConsistencyTestsBase;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ISessionToken;
import com.azure.cosmos.implementation.SessionTokenHelper;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.FeedResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CustomerWorkflowSessionTokenTest extends CustomerWorkflowTestBase {

    @Factory(dataProvider = "clientBuildersWithDirectTcpSession")
    public CustomerWorkflowSessionTokenTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"fi-customer-workflows"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        initializeSharedSinglePartitionContainer("Customer session-token workflow tests");
    }

    @AfterClass(groups = {"fi-customer-workflows"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        closeClient();
    }

    @Test(groups = {"fi-customer-workflows"}, timeOut = TIMEOUT)
    public void readManyWithAdvancedSessionTokenReturnsReadSessionNotAvailable() throws Exception {
        List<CosmosItemIdentity> itemIdentities = new ArrayList<>();
        String lastSessionToken = null;

        for (int index = 0; index < 3; index++) {
            TestObject item = TestObject.create("session-token-workflow");
            CosmosItemResponse<TestObject> createResponse = this.container.createItem(item).block();

            assertThat(createResponse).isNotNull();
            registerForCleanup(item);
            lastSessionToken = createResponse.getSessionToken();
            itemIdentities.add(new CosmosItemIdentity(partitionKey(item), item.getId()));
        }

        FeedResponse<TestObject> validReadManyResponse = this.container
            .readMany(itemIdentities, lastSessionToken, TestObject.class)
            .block();

        assertThat(validReadManyResponse).isNotNull();
        assertThat(validReadManyResponse.getResults()).hasSize(3);

        String advancedSessionToken = advanceSessionToken(lastSessionToken);

        try {
            this.container
                .readMany(itemIdentities, advancedSessionToken, TestObject.class)
                .block();

            fail("Should have hit read session not available error.");
        } catch (Exception error) {
            CosmosException cosmosException = Utils.as(error, CosmosException.class);

            assertThat(cosmosException).isNotNull();
            assertThat(cosmosException.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
            assertThat(cosmosException.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE);
            assertThat(cosmosException.getDiagnostics()).isNotNull();
        }
    }

    private static String advanceSessionToken(String originalSessionToken) throws Exception {
        String[] tokenParts = StringUtils.split(originalSessionToken, ":");
        ISessionToken sessionToken = SessionTokenHelper.parse(tokenParts[1]);
        ISessionToken modifiedSessionToken = ConsistencyTestsBase.createSessionToken(sessionToken, sessionToken.getLSN() + 1000000);

        return tokenParts[0] + ":" + modifiedSessionToken.convertToString();
    }
}
