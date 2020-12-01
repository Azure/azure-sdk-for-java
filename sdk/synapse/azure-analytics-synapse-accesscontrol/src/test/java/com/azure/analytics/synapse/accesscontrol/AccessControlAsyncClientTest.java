// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.synapse.accesscontrol;

import com.azure.analytics.synapse.accesscontrol.models.ErrorContractException;
import com.azure.analytics.synapse.accesscontrol.models.RoleAssignmentDetails;
import com.azure.analytics.synapse.accesscontrol.models.RoleAssignmentOptions;
import com.azure.analytics.synapse.accesscontrol.models.SynapseRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccessControlAsyncClientTest extends AccessControlClientTestBase {

    private AccessControlAsyncClient client;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> getAccessControlClientBuilder(httpPipeline).buildAsyncClient());
    }

    @Test
    public void getRoleDefinitions() {
        List<SynapseRole> roleDefinitions = new ArrayList<>();
        client.getRoleDefinitions().subscribe(roleDefinitions::add);
        sleepInRecordMode(5000);

        for (SynapseRole expectedRole : roleDefinitions) {
            StepVerifier.create(client.getRoleDefinitionById(expectedRole.getId()))
                .assertNext(response -> validateRoleDefinitions(expectedRole, response));
        }
    }

    @Test
    public void getRoleDefinitionsWithResponse() {
        List<SynapseRole> roleDefinitions = new ArrayList<>();
        client.getRoleDefinitionsSinglePage().subscribe(response -> roleDefinitions.addAll(response.getValue()));
        sleepInRecordMode(5000);

        for (SynapseRole expectedRole : roleDefinitions) {
            StepVerifier.create(client.getRoleDefinitionByIdWithResponse(expectedRole.getId()))
                .assertNext(response -> validateRoleDefinitions(expectedRole, response.getValue()));
        }
    }

    @Test
    public void getRoleAssignment() {
        List<RoleAssignmentDetails> roleAssignments = new ArrayList<>();
        client.getRoleAssignments().subscribe(roleAssignments::addAll);
        sleepInRecordMode(5000);

        for (RoleAssignmentDetails expectedRoleAssignment: roleAssignments) {
            StepVerifier.create(client.getRoleAssignmentById(expectedRoleAssignment.getId()))
                .assertNext(response -> validateRoleAssignments(expectedRoleAssignment, response));
        }
    }

    @Test
    public void getRoleAssignmentWithResponse() {
        List<RoleAssignmentDetails> roleAssignments = new ArrayList<>();
        client.getRoleAssignmentsWithResponse(null, null, null)
            .subscribe(response -> roleAssignments.addAll(response.getValue()));
        sleepInRecordMode(5000);

        for (RoleAssignmentDetails expectedRoleAssignment: roleAssignments) {
            StepVerifier.create(client.getRoleAssignmentByIdWithResponse(expectedRoleAssignment.getId()))
                .assertNext(response -> validateRoleAssignments(expectedRoleAssignment, response.getValue()));
        }
    }

    @Test
    public void createAndDeleteRoleAssignment() {
        String sqlAdminId = "7af0c69a-a548-47d6-aea3-d00e69bd83aa";
        String principalId = "1f104909-2748-49b1-b082-f83466d88086";

        // Create role assignment.
        RoleAssignmentOptions createRoleAssignmentOptions = new RoleAssignmentOptions()
            .setRoleId(sqlAdminId)
            .setPrincipalId(principalId);
        AtomicReference<String> createdRoleAssignmentId = new AtomicReference<>();
        StepVerifier.create(client.createRoleAssignment(createRoleAssignmentOptions))
            .assertNext(response -> {
                validateRoleAssignments(createRoleAssignmentOptions, response);
                createdRoleAssignmentId.set(response.getId());
            })
            .verifyComplete();

        // Remove the role assignment.
        client.deleteRoleAssignmentById(createdRoleAssignmentId.get());
        sleepInRecordMode(5000);

        // Verify the role assignment doesn't exist.
        StepVerifier.create(client.getRoleAssignments())
            .assertNext(response ->
                assertFalse(response.stream().anyMatch(ra -> ra.getPrincipalId().equalsIgnoreCase(principalId)))
            );
    }

    @Test
    public void createAndDeleteRoleAssignmentWithResponse() {
        String sqlAdminId = "7af0c69a-a548-47d6-aea3-d00e69bd83aa";
        String principalId = "1f104909-2748-49b1-b082-f83466d88086";

        // Create role assignment.
        RoleAssignmentOptions createRoleAssignmentOptions = new RoleAssignmentOptions()
            .setRoleId(sqlAdminId)
            .setPrincipalId(principalId);
        AtomicReference<String> createdRoleAssignmentId = new AtomicReference<>();
        StepVerifier.create(client.createRoleAssignmentWithResponse(createRoleAssignmentOptions))
            .assertNext(response -> {
                validateRoleAssignments(createRoleAssignmentOptions, response.getValue());
                createdRoleAssignmentId.set(response.getValue().getId());
            })
            .verifyComplete();

        // Remove the role assignment.
        client.deleteRoleAssignmentByIdWithResponse(createdRoleAssignmentId.get());
        sleepInRecordMode(5000);

        // Verify the role assignment doesn't exist.
        StepVerifier.create(client.getRoleAssignmentsWithResponse(null, null, null))
            .assertNext(response ->
                assertFalse(response.getValue().stream().anyMatch(ra -> ra.getPrincipalId().equalsIgnoreCase(principalId)))
            );
    }

    @Test
    public void getCallerRoleAssignments() {
        StepVerifier.create(client.getCallerRoleAssignments())
            .assertNext(response -> assertTrue(response != null && response.size() > 0));
    }

    @Test
    public void getCallerRoleAssignmentsWithResponse() {
        StepVerifier.create(client.getCallerRoleAssignmentsWithResponse())
            .assertNext(response -> assertTrue(response != null && response.getValue() != null && response.getValue().size() > 0));
    }

    @Test
    public void setRoleAssignmentEmptyRoleId() {
        String sqlAdminId = "";
        String principalId = "1f104909-2748-49b1-b082-f83466d88086";

        // Create role assignment.
        RoleAssignmentOptions createRoleAssignmentOptions = new RoleAssignmentOptions()
            .setRoleId(sqlAdminId)
            .setPrincipalId(principalId);
        StepVerifier.create(client.createRoleAssignment(createRoleAssignmentOptions))
            .verifyErrorSatisfies(ex -> assertRestException(ex, ErrorContractException.class, HttpURLConnection.HTTP_BAD_REQUEST));
    }

    @Test
    public void setNullRoleAssignment() {
        StepVerifier.create(client.createRoleAssignment(null))
            .verifyError(ErrorContractException.class);
    }

    /**
     * Tests that an attempt to get a non-existing role assignment throws an error.
     */
    @Test
    public void getRoleAssignmentNotFound() {
        StepVerifier.create(client.getRoleAssignmentById("non-existing"))
            .verifyErrorSatisfies(ex -> assertRestException(ex, ErrorContractException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that an attempt to remove a non-existing role assignment throws an error.
     */
    @Disabled("The test case causes server side error.")
    public void deleteRoleAssignmentNotFound() {
        StepVerifier.create(client.deleteRoleAssignmentById("non-existing"))
            .verifyErrorSatisfies(ex -> assertRestException(ex, ErrorContractException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }
}
