// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.discovery;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Base class for {@code azure-ai-discovery} recorded tests. Provides configured sync and async clients for both the
 * Discovery Workspace and Discovery Bookshelf services, wired for record/playback via the test proxy.
 */
class DiscoveryClientTestBase extends TestProxyTestBase {

    // Sanitized placeholder values. These are the values scrubbed into recordings and used during playback, so they
    // must stay in sync with the sanitized environment used by the recording-cleanup tooling.
    private static final String SANITIZED_WORKSPACE_ENDPOINT = "https://test-wkspc.workspace.discovery.azure.com";
    private static final String SANITIZED_BOOKSHELF_ENDPOINT = "https://test-bkshlf.bookshelf.discovery.azure.com";
    private static final String SANITIZED_PROJECT_NAME = "test-project";
    private static final String SANITIZED_INVESTIGATION_NAME = "test-invst";
    private static final String SANITIZED_KNOWLEDGE_BASE_NAME = "test-kb";
    private static final String SANITIZED_AGENT_NAME = "test-agent";
    private static final String SANITIZED_TOOL_ID
        = "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Discovery/tools/testtool";
    private static final String SANITIZED_NODE_POOL_ID
        = "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Discovery/supercomputers/test-sc/nodePools/nodepool1";
    private static final String SANITIZED_STORAGE_ASSET_ID
        = "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/test-rg/providers/microsoft.discovery/storagecontainers/test-storage/storageassets/test-sa";
    private static final String SANITIZED_USER_ASSIGNED_IDENTITY
        = "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-mi";

    // Regex-based sanitizer patterns (mirroring the Python conftest). These scrub structural, service-independent
    // values (subscription/GUIDs, service endpoints). Resource names (project/investigation/knowledge base/tool ids)
    // are intentionally NOT sanitized here; they are handled by the external recording-cleanup tooling.
    private static final String ZERO_GUID = "00000000-0000-0000-0000-000000000000";
    private static final String GUID_REGEX
        = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    private static final String WORKSPACE_ENDPOINT_REGEX
        = "https://[a-zA-Z0-9-]+[.]workspace[a-zA-Z0-9-]*[.]discovery[.]azure[.]com";
    private static final String BOOKSHELF_ENDPOINT_REGEX
        = "https://[a-zA-Z0-9-]+[.]bookshelf[a-zA-Z0-9-]*[.]discovery[.]azure[.]com";

    protected String workspaceEndpoint;
    protected String bookshelfEndpoint;
    protected String projectName;
    protected String investigationName;
    protected String knowledgeBaseName;
    protected String agentName;
    protected String toolId;
    protected String nodePoolId;
    protected String storageAssetId;
    protected String userAssignedIdentity;

    @Override
    protected void beforeTest() {
        Configuration configuration = Configuration.getGlobalConfiguration();
        // Fallback defaults mirror the sanitized values used when scrubbing recordings, so playback requests match
        // the cleaned recordings. Live/record runs override these via environment variables.
        workspaceEndpoint = configuration.get("AZURE_DISCOVERY_WORKSPACE_ENDPOINT", SANITIZED_WORKSPACE_ENDPOINT);
        bookshelfEndpoint = configuration.get("AZURE_DISCOVERY_BOOKSHELF_ENDPOINT", SANITIZED_BOOKSHELF_ENDPOINT);
        projectName = configuration.get("AZURE_DISCOVERY_PROJECT_NAME", SANITIZED_PROJECT_NAME);
        investigationName = configuration.get("AZURE_DISCOVERY_INVESTIGATION_NAME", SANITIZED_INVESTIGATION_NAME);
        knowledgeBaseName = configuration.get("KNOWLEDGE_BASE_NAME", SANITIZED_KNOWLEDGE_BASE_NAME);
        agentName = configuration.get("AGENT_NAME", SANITIZED_AGENT_NAME);
        toolId = configuration.get("TOOL_ID", SANITIZED_TOOL_ID);
        nodePoolId = configuration.get("NODE_POOL_ID", SANITIZED_NODE_POOL_ID);
        storageAssetId = configuration.get("STORAGE_ASSET_ID", SANITIZED_STORAGE_ASSET_ID);
        userAssignedIdentity = configuration.get("USER_ASSIGNED_IDENTITY", SANITIZED_USER_ASSIGNED_IDENTITY);
        registerSanitizers();
        registerMatchers();
    }

    /**
     * Registers a playback request matcher that compares method, URL (including the {@code api-version} query
     * parameter), and request body, excluding only volatile headers (auth tokens, dates, correlation ids) that
     * legitimately differ between the recorded and replayed requests.
     */
    private void registerMatchers() {
        if (getTestMode() != TestMode.PLAYBACK) {
            return;
        }
        interceptorManager.addMatchers(
            new CustomMatcher().setExcludedHeaders(Arrays.asList("Authorization", "Connection", "Content-Length",
                "Content-Type", "Date", "User-Agent", "traceparent", "x-ms-client-request-id", "x-ms-date", "Accept")));
    }

    /**
     * Registers test-proxy sanitizers, mirroring the Python conftest. These scrub structural values (subscription
     * IDs / GUIDs, service endpoints, the client-request-id header, and the bogus LRO {@code Location} header) from
     * recordings. Resource-name scrubbing is left to the external recording-cleanup tooling, which is the same
     * approach used by the other language SDKs. Runs for record and playback modes only.
     */
    private void registerSanitizers() {
        if (getTestMode() == TestMode.LIVE) {
            return;
        }
        List<TestProxySanitizer> sanitizers = new ArrayList<>();
        // NOTE: We intentionally do NOT blanket-sanitize every GUID. Resource identifiers such as conversation names
        // and long-running-operation ids are unique GUIDs that tests assert on; collapsing them all to a single value
        // both breaks those assertions and prevents playback URL matching for LRO polling. Sensitive subscription and
        // tenant identifiers are scrubbed structurally by the recording-cleanup tooling and the default sanitizers.
        // Workspace and Bookshelf service endpoints, in both URLs and bodies.
        sanitizers.add(
            new TestProxySanitizer(WORKSPACE_ENDPOINT_REGEX, SANITIZED_WORKSPACE_ENDPOINT, TestProxySanitizerType.URL));
        sanitizers.add(new TestProxySanitizer(WORKSPACE_ENDPOINT_REGEX, SANITIZED_WORKSPACE_ENDPOINT,
            TestProxySanitizerType.BODY_REGEX));
        sanitizers.add(
            new TestProxySanitizer(BOOKSHELF_ENDPOINT_REGEX, SANITIZED_BOOKSHELF_ENDPOINT, TestProxySanitizerType.URL));
        sanitizers.add(new TestProxySanitizer(BOOKSHELF_ENDPOINT_REGEX, SANITIZED_BOOKSHELF_ENDPOINT,
            TestProxySanitizerType.BODY_REGEX));
        // Client-request-id header.
        sanitizers.add(
            new TestProxySanitizer("x-ms-client-request-id", GUID_REGEX, ZERO_GUID, TestProxySanitizerType.HEADER));
        // Some LROs return a bogus "https://example.com" Location header; clear it so the poller uses
        // operation-location instead.
        sanitizers
            .add(new TestProxySanitizer("Location", "^https://example[.]com$", "", TestProxySanitizerType.HEADER));
        interceptorManager.addSanitizers(sanitizers);
        // Remove the default "$..name" body-key sanitizer (AZSDK3493); it would replace name fields with "Sanitized",
        // which collapses the unique resource names that several tests assert on.
        interceptorManager.removeSanitizers("AZSDK3493");
        // Remove the default operation-location header sanitizer (AZSDK2030); it rewrites the LRO poll URL to
        // "https://example.com", which breaks long-running-operation polling during playback. The Python and other
        // language test frameworks remove this same default sanitizer for the same reason.
        interceptorManager.removeSanitizers("AZSDK2030");
        // Remove the default "$..id" body-key sanitizer (AZSDK3430); it replaces operation "id" fields with
        // "Sanitized", which prevents getRunStatus/getOperationStatus from resolving the real operation id during
        // playback. Operation ids are not sensitive.
        interceptorManager.removeSanitizers("AZSDK3430");
    }

    // --- Workspace clients ---

    private WorkspaceClientBuilder workspaceClientBuilder() {
        WorkspaceClientBuilder builder = new WorkspaceClientBuilder().endpoint(workspaceEndpoint)
            .httpClient(getHttpClientOrUsePlayback(getHttpClients().findFirst().orElse(null)))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));
        return applyTestMode(builder);
    }

    protected ConversationsClient getConversationsClient() {
        return workspaceClientBuilder().buildConversationsClient();
    }

    protected ConversationsAsyncClient getConversationsAsyncClient() {
        return workspaceClientBuilder().buildConversationsAsyncClient();
    }

    protected InvestigationsClient getInvestigationsClient() {
        return workspaceClientBuilder().buildInvestigationsClient();
    }

    protected InvestigationsAsyncClient getInvestigationsAsyncClient() {
        return workspaceClientBuilder().buildInvestigationsAsyncClient();
    }

    protected TasksClient getTasksClient() {
        return workspaceClientBuilder().buildTasksClient();
    }

    protected TasksAsyncClient getTasksAsyncClient() {
        return workspaceClientBuilder().buildTasksAsyncClient();
    }

    protected ToolsClient getToolsClient() {
        return workspaceClientBuilder().buildToolsClient();
    }

    protected ToolsAsyncClient getToolsAsyncClient() {
        return workspaceClientBuilder().buildToolsAsyncClient();
    }

    // --- Bookshelf clients ---

    private BookshelfClientBuilder bookshelfClientBuilder() {
        BookshelfClientBuilder builder = new BookshelfClientBuilder().endpoint(bookshelfEndpoint)
            .httpClient(getHttpClientOrUsePlayback(getHttpClients().findFirst().orElse(null)))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));
        return applyTestMode(builder);
    }

    protected BookshelfClient getBookshelfClient() {
        return bookshelfClientBuilder().buildClient();
    }

    protected BookshelfAsyncClient getBookshelfAsyncClient() {
        return bookshelfClientBuilder().buildAsyncClient();
    }

    // --- helpers ---

    private WorkspaceClientBuilder applyTestMode(WorkspaceClientBuilder builder) {
        if (getTestMode() == TestMode.PLAYBACK) {
            builder.httpClient(interceptorManager.getPlaybackClient()).credential(fakeCredential());
        } else if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
                .credential(new DefaultAzureCredentialBuilder().build());
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }
        return builder;
    }

    private BookshelfClientBuilder applyTestMode(BookshelfClientBuilder builder) {
        if (getTestMode() == TestMode.PLAYBACK) {
            builder.httpClient(interceptorManager.getPlaybackClient()).credential(fakeCredential());
        } else if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
                .credential(new DefaultAzureCredentialBuilder().build());
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }
        return builder;
    }

    private static TokenCredential fakeCredential() {
        return request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX));
    }

    /**
     * Builds the full resource path for an investigation, matching the service's expected {@code investigationName}
     * path format.
     *
     * @param projectName the project name.
     * @param investigationName the investigation name.
     * @return the investigation resource path.
     */
    protected static String investigationPath(String projectName, String investigationName) {
        return "/projects/" + projectName + "/investigations/" + investigationName;
    }
}
