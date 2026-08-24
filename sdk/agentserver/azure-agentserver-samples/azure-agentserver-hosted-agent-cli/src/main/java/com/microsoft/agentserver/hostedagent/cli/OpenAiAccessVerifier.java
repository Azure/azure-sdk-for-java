// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.hostedagent.cli;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager;
import com.azure.resourcemanager.cognitiveservices.models.Account;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Everything related to making sure a hosted agent's managed identity can call Azure OpenAI: a proactive,
 * deploy-time RBAC check (and optional auto-grant) plus a runtime log scanner that recognises the same
 * failure if it slips through.
 *
 * <p>A hosted agent authenticates to Azure OpenAI with its own managed identity, which requires one of the
 * data-plane roles on the AI Services account. {@link #verify} uses the Azure Resource Manager Java SDKs —
 * no shelling out — to resolve the AI Services account and list the identity's role assignments right after
 * deploy, so a missing assignment is caught immediately instead of surfacing later as a 401/403 buried in
 * the container's console logs. {@link LogAdvisor} is the fallback for that later case: it watches streamed
 * log lines for the same authorization-failure signature and prints the identical remediation.</p>
 */
final class OpenAiAccessVerifier {

    private static final String ROLE = "Cognitive Services OpenAI User";

    private static final List<String> ACCEPTED_ROLES = Arrays.asList(
        ROLE, "Cognitive Services OpenAI Contributor");

    private static final Pattern GUID =
        Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private OpenAiAccessVerifier() {
    }

    /**
     * Checks whether {@code principalId} holds an Azure OpenAI data-plane role on the AI Services account
     * derived from {@code endpoint}. When it does not and {@code grant} is {@code true}, it attempts to create
     * the {@code Cognitive Services OpenAI User} role assignment for the identity; otherwise (or if the grant
     * fails) it prints the exact remediation. Any failure to run the check is reported as a non-fatal note.
     */
    static void verify(String endpoint, String principalId, String subscriptionId, boolean grant) {
        if (principalId == null) {
            printFix(endpoint, null, "⚠  Could not read the agent identity to verify Azure OpenAI access.");
            return;
        }
        if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
            skipped("no subscription configured — add 'subscription' to the config (or pass --subscription)");
            return;
        }
        String subscription = subscriptionId.trim();
        if (!GUID.matcher(subscription).matches()) {
            skipped("the configured subscription id '" + subscription + "' is not a valid GUID");
            return;
        }
        String accountName = accountName(endpoint);
        if (accountName == null) {
            skipped("could not determine the AI Services account from the endpoint");
            return;
        }

        try {
            TokenCredential credential = new DefaultAzureCredentialBuilder().build();
            AzureProfile profile = new AzureProfile(null, subscription, AzureEnvironment.AZURE);

            String accountId = resolveAccountId(credential, profile, accountName);
            if (accountId == null) {
                skipped("AI Services account '" + accountName + "' was not found in subscription "
                    + subscription);
                return;
            }

            AuthorizationManager authorization = AuthorizationManager.authenticate(credential, profile);
            String granted = grantedRole(authorization, principalId, accountId);

            if (granted != null) {
                System.out.printf("✓ Azure OpenAI access verified: agent identity holds '%s' on '%s'.%n",
                    granted, accountName);
                return;
            }

            if (grant) {
                grantAccess(endpoint, authorization, principalId, accountId, accountName);
            } else {
                printFix(endpoint, principalId,
                    "⚠  The agent identity is missing Azure OpenAI access — model calls will fail at runtime.");
            }
        } catch (RuntimeException e) {
            skipped("the permission check could not be completed (" + e.getMessage() + ")");
        }
    }

    /**
     * Attempts to assign the {@code Cognitive Services OpenAI User} role to the agent identity on the account,
     * printing a success line or — if the assignment cannot be created (typically a lack of
     * {@code Microsoft.Authorization/roleAssignments/write} permission) — the manual remediation.
     */
    private static void grantAccess(String endpoint, AuthorizationManager authorization, String principalId,
                                    String accountId, String accountName) {
        System.out.printf("Granting '%s' to the agent identity on '%s'…%n", ROLE, accountName);
        try {
            String roleDefinitionId = authorization.roleDefinitions().getByScopeAndRoleName(accountId, ROLE).id();
            authorization.roleAssignments()
                .define(UUID.randomUUID().toString())
                .forObjectId(principalId)
                .withRoleDefinition(roleDefinitionId)
                .withScope(accountId)
                .create();
            System.out.printf("✓ Granted '%s'. It can take a few minutes to propagate before the agent can "
                + "call Azure OpenAI.%n", ROLE);
        } catch (RuntimeException e) {
            printFix(endpoint, principalId,
                "⚠  Could not automatically grant Azure OpenAI access (" + e.getMessage()
                    + "). Grant it manually:");
        }
    }

    private static String resolveAccountId(TokenCredential credential, AzureProfile profile, String accountName) {
        CognitiveServicesManager manager = CognitiveServicesManager.authenticate(credential, profile);
        for (Account account : manager.accounts().list()) {
            if (accountName.equalsIgnoreCase(account.name())) {
                return account.id();
            }
        }
        return null;
    }

    /**
     * Returns the first accepted Azure OpenAI role the identity holds at or above the account scope (covering
     * assignments inherited from the resource group or subscription), or {@code null} if none is found.
     */
    private static String grantedRole(AuthorizationManager authorization, String principalId, String accountId) {
        for (RoleAssignment assignment : authorization.roleAssignments().listByServicePrincipal(principalId)) {
            String scope = assignment.scope();
            if (scope == null || !coversScope(scope, accountId)) {
                continue;
            }
            String roleName = roleName(authorization, scope, assignment.roleDefinitionId());
            if (roleName != null && isAccepted(roleName)) {
                return roleName;
            }
        }
        return null;
    }

    private static String roleName(AuthorizationManager authorization, String scope, String roleDefinitionId) {
        if (roleDefinitionId == null) {
            return null;
        }
        String guid = roleDefinitionId.substring(roleDefinitionId.lastIndexOf('/') + 1);
        try {
            return authorization.roleDefinitions().getByScope(scope, guid).roleName();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static boolean isAccepted(String roleName) {
        for (String accepted : ACCEPTED_ROLES) {
            if (accepted.equalsIgnoreCase(roleName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * True when {@code scope} is the account itself or one of its ancestors (subscription or resource group),
     * i.e. an assignment at {@code scope} applies to {@code accountId}.
     */
    static boolean coversScope(String scope, String accountId) {
        String s = scope.toLowerCase(Locale.ROOT);
        String a = accountId.toLowerCase(Locale.ROOT);
        if (a.equals(s)) {
            return true;
        }
        return a.startsWith(s) && a.charAt(s.length()) == '/';
    }

    private static void skipped(String reason) {
        System.out.printf("• Skipped the Azure OpenAI permission check (%s).%n", reason);
        System.out.printf("  If the agent returns 401/403 errors, grant its identity the '%s' role.%n", ROLE);
    }

    /**
     * Prints the actionable Azure OpenAI role-assignment remediation for a given agent identity and project
     * endpoint. {@code header} is the leading warning line describing why the fix is being shown.
     */
    static void printFix(String endpoint, String principalId, String header) {
        String account = accountName(endpoint);
        String bar = "────────────────────────────────────────────────────────────────────";

        System.err.println();
        System.err.println(bar);
        System.err.println(header);
        System.err.println();
        System.err.println("The hosted agent calls Azure OpenAI with its managed identity, which must hold the");
        System.err.println("'" + ROLE + "' role on the AI Services account.");
        System.err.println();
        System.err.println("  Agent identity (principal id): "
            + (principalId != null ? principalId : "<not provisioned yet — redeploy and retry>"));
        System.err.println("  AI Services account:           " + (account != null ? account : "<unknown>"));
        System.err.println();

        if (principalId == null) {
            System.err.println("The agent identity is not available yet. Once the agent version is active, re-run");
            System.err.println("this command to obtain the principal id and the exact role-assignment command.");
            System.err.println(bar);
            System.err.println();
            return;
        }

        System.err.println("To grant access, run:");
        System.err.println();
        System.err.println("  # Resolve the AI Services account resource id");
        if (account != null) {
            System.err.println("  scope=$(az cognitiveservices account list \\");
            System.err.println("    --query \"[?name=='" + account + "'].id | [0]\" -o tsv)");
        } else {
            System.err.println("  scope=\"/subscriptions/<sub>/resourceGroups/<rg>/providers"
                + "/Microsoft.CognitiveServices/accounts/<account>\"");
        }
        System.err.println();
        System.err.println("  # Assign the role to the agent identity");
        System.err.println("  az role assignment create \\");
        System.err.println("    --assignee-object-id " + principalId + " \\");
        System.err.println("    --assignee-principal-type ServicePrincipal \\");
        System.err.println("    --role \"" + ROLE + "\" \\");
        System.err.println("    --scope \"$scope\"");
        System.err.println();
        System.err.println("Role assignments can take a few minutes to propagate; retry the request afterwards.");
        System.err.println(bar);
        System.err.println();
    }

    /**
     * Extracts the AI Services account name (the first host label) from a project endpoint such as
     * {@code https://my-account.services.ai.azure.com/api/projects/my-project} → {@code my-account}.
     */
    static String accountName(String endpoint) {
        if (endpoint == null || endpoint.isEmpty()) {
            return null;
        }
        try {
            String host = URI.create(endpoint).getHost();
            if (host == null) {
                return null;
            }
            int dot = host.indexOf('.');
            return dot > 0 ? host.substring(0, dot) : host;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Scans hosted-agent container log output for the signature of a missing Azure OpenAI role assignment
     * and, when it sees one, prints the same remediation as {@link #verify}. This is the fallback for a
     * permission gap that was not (or could not be) caught at deploy time — for example when {@code verify}
     * was skipped because no subscription was configured.
     */
    static final class LogAdvisor {

        private final String endpoint;
        private final String principalId;
        private boolean advised;

        /**
         * @param endpoint    the Azure AI Foundry project endpoint (used to derive the AI Services account name).
         * @param principalId the agent managed identity's principal/object id, or {@code null} if unavailable.
         */
        LogAdvisor(String endpoint, String principalId) {
            this.endpoint = endpoint;
            this.principalId = principalId;
        }

        /**
         * Inspects a single streamed log line. On the first line that looks like an Azure OpenAI authorization
         * failure, prints the remediation once and returns {@code true}; otherwise returns {@code false}.
         */
        boolean inspect(String line) {
            if (advised || !looksLikeOpenAiAuthError(line)) {
                return false;
            }
            advised = true;
            printFix(endpoint, principalId, "⚠  Detected an authentication failure calling Azure OpenAI.");
            return true;
        }

        /**
         * Heuristic match for "the agent identity cannot call Azure OpenAI": an authorization failure
         * (401/403/forbidden/permission denied/access denied) that references an OpenAI / Cognitive Services
         * endpoint, or any explicit mention of the required role.
         */
        static boolean looksLikeOpenAiAuthError(String line) {
            if (line == null || line.isEmpty()) {
                return false;
            }
            String text = line.toLowerCase(Locale.ROOT);

            if (text.contains(ROLE.toLowerCase(Locale.ROOT))) {
                return true;
            }

            boolean authFailure = text.contains("401")
                || text.contains("403")
                || text.contains("unauthorized")
                || text.contains("forbidden")
                || text.contains("permissiondenied")
                || text.contains("permission denied")
                || text.contains("access denied")
                || text.contains("authenticationerror")
                || text.contains("not authorized")
                || text.contains("does not have authorization");

            boolean openAiTarget = text.contains("openai")
                || text.contains("cognitiveservices")
                || text.contains("cognitive services")
                || text.contains("services.ai.azure")
                || text.contains("/deployments/")
                || text.contains("chat/completions")
                || text.contains("responses");

            return authFailure && openAiTarget;
        }
    }
}
