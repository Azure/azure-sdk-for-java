// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.synapse.accesscontrol;

import com.azure.analytics.synapse.accesscontrol.models.ErrorContractException;
import com.azure.analytics.synapse.accesscontrol.models.RoleAssignmentDetails;
import com.azure.analytics.synapse.accesscontrol.models.RoleAssignmentOptions;
import com.azure.analytics.synapse.accesscontrol.models.SynapseRole;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.*;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.ClientSecretCredentialBuilder;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class AccessControlClientTestBase extends TestBase {

    static final String NAME = "name";
    static final String SYNAPSE_PROPERTIES = "azure-analytics-synapse-accesscontrol.properties";
    static final String VERSION = "version";
    private final HttpLogOptions httpLogOptions = new HttpLogOptions();
    private final Map<String, String> properties = CoreUtils.getProperties(SYNAPSE_PROPERTIES);
    private final String clientName = properties.getOrDefault(NAME, "UnknownName");
    private final String clientVersion = properties.getOrDefault(VERSION, "UnknownVersion");

    protected String getEndpoint() {
        String endpoint = interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get("AZURE_SYNAPSE_WORKSPACE_URL");
        Objects.requireNonNull(endpoint);
        return endpoint;
    }

    <T> T clientSetup(Function<HttpPipeline, T> clientBuilder) {
        TokenCredential credential = null;

        if (!interceptorManager.isPlaybackMode()) {
            String clientId = System.getenv("CLIENT_ID");
            String clientKey = System.getenv("CLIENT_SECRET");
            String tenantId = System.getenv("TENANT_ID");
            Objects.requireNonNull(clientId, "The client id cannot be null");
            Objects.requireNonNull(clientKey, "The client key cannot be null");
            Objects.requireNonNull(tenantId, "The tenant id cannot be null");
            credential = new ClientSecretCredentialBuilder()
                .clientSecret(clientKey)
                .clientId(clientId)
                .tenantId(tenantId)
                .build();
        }

        HttpClient httpClient;
        Configuration buildConfiguration = Configuration.getGlobalConfiguration().clone();

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion,
            buildConfiguration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddDatePolicy());

        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        if (credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(credential, AccessControlClientBuilder.DEFAULT_SCOPE));
        }

        policies.add(new RetryPolicy());

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        if (interceptorManager.isPlaybackMode()) {
            httpClient = interceptorManager.getPlaybackClient();
        } else {
            httpClient = new NettyAsyncHttpClientBuilder().wiretap(true).build();
        }
        policies.add(interceptorManager.getRecordPolicy());

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        T client;
        client = clientBuilder.apply(pipeline);

        return Objects.requireNonNull(client);
    }

    AccessControlClientBuilder getAccessControlClientBuilder(HttpPipeline httpPipeline) {
        return new AccessControlClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline);
    }

    public void sleepInRecordMode(long millis) {
        if (interceptorManager.isPlaybackMode()) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void assertRestException(Runnable exceptionThrower, Class<? extends HttpResponseException> expectedExceptionType, int expectedStatusCode) {
        try {
            exceptionThrower.run();
            fail();
        } catch (Throwable ex) {
            assertRestException(ex, expectedExceptionType, expectedStatusCode);
        }
    }

    static void assertRestException(Throwable exception, Class<? extends HttpResponseException> expectedExceptionType, int expectedStatusCode) {
        assertEquals(expectedExceptionType, exception.getClass());
        assertEquals(expectedStatusCode, ((HttpResponseException) exception).getResponse().getStatusCode());
    }

    /**
     * Helper method to verify that a command throws an IllegalArgumentException.
     *
     * @param exceptionThrower Command that should throw the exception
     */
    static <T> void assertRunnableThrowsException(Runnable exceptionThrower, Class<T> exception) {
        try {
            exceptionThrower.run();
            fail();
        } catch (Exception ex) {
            assertEquals(exception, ex.getClass());
        }
    }

    @Test
    public abstract void getRoleDefinitions();

    @Test
    public abstract void getRoleAssignment();

    @Test
    public abstract void createAndDeleteRoleAssignment();

    @Test
    public abstract void getCallerRoleAssignments();

    @Test
    public abstract void getRoleDefinitionsWithResponse();

    //@Test
    //public abstract void getRoleAssignmentWithResponse();

    //@Test
    //public abstract void createAndDeleteRoleAssignmentWithResponse();

    //@Test
    //public abstract void getCallerRoleAssignmentsWithResponse();

    @Test
    public abstract void setRoleAssignmentEmptyRoleId();

    @Test
    public abstract void setNullRoleAssignment();

    @Test
    public abstract void getRoleAssignmentNotFound();

    @Test
    public abstract void deleteRoleAssignmentNotFound();

    void validateRoleDefinitions(SynapseRole expectedRole, SynapseRole actualRole) {
        assertEquals(expectedRole.getName(), actualRole.getName());
        assertEquals(expectedRole.getId(), actualRole.getId());
        assertEquals(expectedRole.isBuiltIn(), actualRole.isBuiltIn());
    }

    void validateRoleAssignments(
        RoleAssignmentDetails expectedRoleAssignment,
        RoleAssignmentDetails actualRoleAssignment
    ) {
        assertEquals(expectedRoleAssignment.getId(), actualRoleAssignment.getId());
        assertEquals(expectedRoleAssignment.getPrincipalId(), actualRoleAssignment.getPrincipalId());
        assertEquals(expectedRoleAssignment.getRoleId(), actualRoleAssignment.getRoleId());
    }

    void validateRoleAssignments(
        RoleAssignmentOptions expected,
        RoleAssignmentDetails actual
    ) {
        assertEquals(expected.getPrincipalId(), actual.getPrincipalId());
        assertEquals(expected.getRoleId(), actual.getRoleId());
    }
}
