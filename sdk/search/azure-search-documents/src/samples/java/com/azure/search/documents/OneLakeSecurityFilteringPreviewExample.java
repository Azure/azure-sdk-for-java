// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.util.BinaryData;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.IndexedOneLakeKnowledgeSource;
import com.azure.search.documents.indexes.models.IndexedOneLakeKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeSourceContentExtractionMode;
import com.azure.search.documents.indexes.models.KnowledgeSourceIngestionPermissionOption;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.models.CompletedSynchronizationState;
import com.azure.search.documents.knowledgebases.models.IndexedOneLakeKnowledgeSourceParams;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseIndexedOneLakeActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseIndexedOneLakeReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalMinimalReasoningEffort;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalOutputMode;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalSemanticIntent;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceIngestionParameters;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceStatus;
import com.azure.search.documents.knowledgebases.models.SynchronizationState;

import java.net.URI;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/**
 * Exercises OneLake permission ingestion and retrieval-time ACL enforcement for two existing users.
 *
 * <p>This is an opt-in, potentially billable integration sample, not a provisioning script. An administrator
 * must first prepare a dedicated, prepopulated fixture and enable the required Search/Fabric preview capabilities.
 * The Search service's existing managed identity must already be able to read the fixture and its permissions.
 * This sample neither grants access nor changes OneLake data, ACLs, RBAC, service configuration, or billing.
 * Creating the knowledge source does ingest the approved fixture into run-owned Search resources.</p>
 *
 * <p>The fixture must contain two small text documents in separately secured folders: user A can read only
 * document A, and user B can read only document B. Use existing OneLake security roles/permissions to establish
 * that arrangement outside this sample. Both documents must match the same distinctive fixture query.
 * Do not use shared production folders or shortcuts to unrelated content. The target must be a dedicated
 * folder named {@code java-onelake-acl-<fixture-name>} under the lakehouse's Files area
 * (optionally with child folders).</p>
 *
 * <p>Required configuration, in addition to {@code SEARCH_ENDPOINT} and {@code SEARCH_API_KEY}:</p>
 * <ul>
 * <li>{@code SEARCH_ONELAKE_FIXTURE_APPROVED=true}: operator approval of this exact fixture, ingestion, and costs.</li>
 * <li>{@code SEARCH_ONELAKE_WORKSPACE_ID}, {@code SEARCH_ONELAKE_LAKEHOUSE_ID}: existing fixture GUIDs.</li>
 * <li>{@code SEARCH_ONELAKE_TARGET_PATH}: the exact service-accepted folder filter for the dedicated fixture,
 * never the lakehouse/Files root. For example, {@code java-onelake-acl-fixture}; the sample also accepts a
 * {@code Files/} prefix if required by the fixture's connector path convention.</li>
 * <li>{@code SEARCH_ONELAKE_DOCUMENT_URL_PREFIX}: the exact service-returned document URL prefix for that
 * fixture, ending in {@code /}; this is evidence validation, not a query-time authorization filter.</li>
 * <li>{@code SEARCH_ONELAKE_USER_A_DOCUMENT_URL}, {@code SEARCH_ONELAKE_USER_B_DOCUMENT_URL}: exact, distinct
 * service-returned {@code docUrl} evidence IDs under that prefix, known in advance by the fixture owner.
 * Reference {@code id} values are request-local citation IDs, not stable document identities.</li>
 * <li>{@code SEARCH_ONELAKE_FIXTURE_QUERY}: a query that retrieves the authorized fixture document for either user.</li>
 * <li>{@code SEARCH_ONELAKE_USER_A_TOKEN}, {@code SEARCH_ONELAKE_USER_B_TOKEN}: externally acquired, short-lived
 * Entra user JWT access tokens accepted by the preview's query-source authorization flow, without a
 * {@code Bearer } prefix, with between one minute and two hours remaining. They must represent different
 * {@code oid} users in the same {@code tid} tenant.
 * Do not supply the Search API key, an ingestion identity token, or a Work IQ assertion here.</li>
 * </ul>
 *
 * <p>The local JWT checks catch accidental token reuse/expiration; they do not verify signatures, audience,
 * or permissions. The service validates authentication and enforces ACLs. Never log tokens or enable HTTP body
 * logging for this scenario. Only source/KB resources successfully created by this run are deleted, KB first.
 * Synchronization status is not an LRO: bounded status checks require a successful completed ingestion, not
 * merely {@code active}. Missing evidence, partial ingestion, or unauthorized evidence fails the sample closed;
 * a successful run is a fixture assertion, not a general proof of security for arbitrary queries.</p>
 */
public class OneLakeSecurityFilteringPreviewExample {
    /**
     * Runs the explicitly approved fixture without provisioning or modifying shared OneLake resources.
     *
     * @param args unused.
     */
    public static void main(String[] args) {
        Fixture fixture = Fixture.fromEnvironment(System::getenv);
        run(PreviewSampleSupport.indexClient(), PreviewSampleSupport::retrievalClient, fixture,
            OneLakeSecurityFilteringPreviewExample::pause, 60);
        System.out.println("Both fixture identities returned only their authorized OneLake document evidence.");
    }

    static void run(SearchIndexClient indexes, Function<String, KnowledgeBaseRetrievalClient> retrievalFactory,
        Fixture fixture, Runnable pause, int attempts) {
        String sourceName = PreviewSampleSupport.name("java-onelake-acl-ks");
        String baseName = PreviewSampleSupport.name("java-onelake-acl-kb");
        try (PreviewSampleSupport.Resources resources = new PreviewSampleSupport.Resources(indexes)) {
            // BEGIN: com.azure.search.documents.oneLakeSecurityFilteringPreview
            KnowledgeSourceIngestionParameters ingestion = new KnowledgeSourceIngestionParameters()
                .setContentExtractionMode(KnowledgeSourceContentExtractionMode.MINIMAL)
                .setDisableImageVerbalization(true)
                .setIngestionPermissionOptions(KnowledgeSourceIngestionPermissionOption.ONE_LAKE_PARENT_FOLDERS,
                    KnowledgeSourceIngestionPermissionOption.ONE_LAKE_LAKEHOUSE_ID);
            resources.createKnowledgeSource(new IndexedOneLakeKnowledgeSource(sourceName,
                new IndexedOneLakeKnowledgeSourceParameters(fixture.workspaceId, fixture.lakehouseId)
                    .setTargetPath(fixture.targetPath).setIngestionParameters(ingestion)));
            waitForIngestion(indexes, sourceName, pause, attempts);
            resources.createKnowledgeBase(new KnowledgeBase(baseName, new KnowledgeSourceReference(sourceName))
                    .setRetrievalReasoningEffort(new KnowledgeRetrievalMinimalReasoningEffort())
                    .setOutputMode(KnowledgeRetrievalOutputMode.EXTRACTIVE_DATA));

            KnowledgeBaseRetrievalClient retrieval = retrievalFactory.apply(baseName);
            retrieveAndVerify(retrieval, sourceName, fixture.query, fixture.tokenA, fixture.documentA);
            retrieveAndVerify(retrieval, sourceName, fixture.query, fixture.tokenB, fixture.documentB);
            // END: com.azure.search.documents.oneLakeSecurityFilteringPreview
        }
    }

    private static void retrieveAndVerify(KnowledgeBaseRetrievalClient retrieval, String sourceName,
        String query, String token, String expectedDocument) {
        KnowledgeBaseRetrievalOptions options = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent(query))
            .setIncludeActivity(true)
            .setKnowledgeSourceParams(Collections.singletonList(new IndexedOneLakeKnowledgeSourceParams(sourceName)
                .setAlwaysQuerySource(true).setFailOnError(true)
                .setIncludeReferences(true).setIncludeReferenceSourceData(false)));
        // The SDK sends this only as x-ms-query-source-authorization, not as the service credential or JSON.
        options.setQuerySourceAuthorization(token);
        verifyEvidence(retrieval.retrieve(options), sourceName, expectedDocument);
    }

    static void verifyEvidence(KnowledgeBaseRetrievalResult result, String sourceName, String expectedDocument) {
        if (result.getActivity() == null || result.getReferences() == null || result.getReferences().isEmpty()) {
            throw new IllegalStateException("Expected OneLake fixture activity and document evidence were missing.");
        }
        Set<Integer> sourceActivities = new HashSet<>();
        for (KnowledgeBaseActivityRecord activity : result.getActivity()) {
            if (activity.getError() != null) {
                throw new IllegalStateException("Retrieval reported an activity error; ACL verification is incomplete.");
            }
            if (activity instanceof KnowledgeBaseIndexedOneLakeActivityRecord) {
                KnowledgeBaseIndexedOneLakeActivityRecord oneLake = (KnowledgeBaseIndexedOneLakeActivityRecord) activity;
                if (!sourceName.equals(oneLake.getKnowledgeSourceName()) || !sourceActivities.add(oneLake.getId())) {
                    throw new IllegalStateException("Retrieval activity did not uniquely identify the owned source.");
                }
            }
        }
        for (KnowledgeBaseReference reference : result.getReferences()) {
            if (!(reference instanceof KnowledgeBaseIndexedOneLakeReference)
                || !sourceActivities.contains(reference.getActivitySource())
                || !expectedDocument.equals(((KnowledgeBaseIndexedOneLakeReference) reference).getDocumentUrl())) {
                throw new IllegalStateException("Unauthorized or unrecognized fixture evidence; ACL verification failed.");
            }
        }
    }

    private static void waitForIngestion(SearchIndexClient indexes, String sourceName, Runnable pause, int attempts) {
        for (int attempt = 0; attempt < attempts; attempt++) {
            KnowledgeSourceStatus status = indexes.getKnowledgeSourceStatus(sourceName);
            SynchronizationState current = status.getCurrentSynchronizationState();
            CompletedSynchronizationState completed = status.getLastSynchronizationState();
            if ((current != null && (current.getItemsUpdatesFailed() != 0 || current.getItemsSkipped() != 0
                || (current.getErrors() != null && !current.getErrors().isEmpty())))
                || (completed != null
                    && (completed.getItemsUpdatesFailed() != 0 || completed.getItemsSkipped() != 0))) {
                throw new IllegalStateException("OneLake fixture ingestion failed or skipped items; inspect source status.");
            }
            if ("active".equals(String.valueOf(status.getSynchronizationStatus()))
                && completed != null && completed.getEndTime() != null && completed.getItemsUpdatesProcessed() >= 2) {
                return;
            }
            if ("deleting".equals(String.valueOf(status.getSynchronizationStatus()))) {
                throw new IllegalStateException("The owned OneLake source is being deleted.");
            }
            if (attempt + 1 < attempts) {
                pause.run();
            }
        }
        throw new IllegalStateException("No successful completed fixture ingestion within the bounded status wait.");
    }

    private static void pause() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted waiting for OneLake fixture ingestion.", ex);
        }
    }

    static final class Fixture {
        private final String workspaceId;
        private final String lakehouseId;
        private final String targetPath;
        private final String query;
        private final String tokenA;
        private final String tokenB;
        private final String documentA;
        private final String documentB;

        private Fixture(Function<String, String> environment) {
            if (!"true".equals(environment.apply("SEARCH_ONELAKE_FIXTURE_APPROVED"))) {
                throw new IllegalArgumentException("Set SEARCH_ONELAKE_FIXTURE_APPROVED=true only for an approved fixture.");
            }
            workspaceId = guid(environment, "SEARCH_ONELAKE_WORKSPACE_ID");
            lakehouseId = guid(environment, "SEARCH_ONELAKE_LAKEHOUSE_ID");
            targetPath = required(environment, "SEARCH_ONELAKE_TARGET_PATH");
            if (!targetPath.matches("(Files/)?java-onelake-acl-[A-Za-z0-9_-]+(/[A-Za-z0-9_-]+)*")) {
                throw new IllegalArgumentException("SEARCH_ONELAKE_TARGET_PATH must identify a dedicated ACL fixture.");
            }
            String prefix = required(environment, "SEARCH_ONELAKE_DOCUMENT_URL_PREFIX");
            if (!validDocumentUrl(prefix) || !prefix.endsWith("/")) {
                throw new IllegalArgumentException("SEARCH_ONELAKE_DOCUMENT_URL_PREFIX must be an HTTPS folder URL.");
            }
            documentA = required(environment, "SEARCH_ONELAKE_USER_A_DOCUMENT_URL");
            documentB = required(environment, "SEARCH_ONELAKE_USER_B_DOCUMENT_URL");
            if (!validDocumentUrl(documentA) || !validDocumentUrl(documentB) || documentA.equals(documentB)
                || !documentA.startsWith(prefix) || !documentB.startsWith(prefix)
                || documentA.equals(prefix) || documentB.equals(prefix)) {
                throw new IllegalArgumentException("Supply two distinct fixture document URLs under the approved prefix.");
            }
            query = required(environment, "SEARCH_ONELAKE_FIXTURE_QUERY");
            tokenA = required(environment, "SEARCH_ONELAKE_USER_A_TOKEN");
            tokenB = required(environment, "SEARCH_ONELAKE_USER_B_TOKEN");
            Map<?, ?> claimsA = claims(tokenA);
            Map<?, ?> claimsB = claims(tokenB);
            if (tokenA.equals(tokenB) || claimsA.get("oid").equals(claimsB.get("oid"))
                || !claimsA.get("tid").equals(claimsB.get("tid"))) {
                throw new IllegalArgumentException("Supply distinct user tokens for two identities in the fixture tenant.");
            }
        }

        static Fixture fromEnvironment(Function<String, String> environment) {
            return new Fixture(environment);
        }

        private static String required(Function<String, String> environment, String name) {
            String value = environment.apply(name);
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException("Set " + name + " before running this sample.");
            }
            return value;
        }

        private static String guid(Function<String, String> environment, String name) {
            String value = required(environment, name);
            if (!value.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
                throw new IllegalArgumentException("Set " + name + " to the existing fixture GUID.");
            }
            return UUID.fromString(value).toString();
        }

        private static boolean validDocumentUrl(String value) {
            try {
                URI uri = URI.create(value);
                return "https".equals(uri.getScheme()) && uri.getHost() != null && uri.getRawUserInfo() == null
                    && uri.getRawQuery() == null && uri.getRawFragment() == null
                    && uri.normalize().equals(uri) && !value.contains("%");
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }

        private static Map<?, ?> claims(String token) {
            try {
                if (!token.matches("[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+")) {
                    throw new IllegalArgumentException("Invalid JWT format.");
                }
                String[] parts = token.split("\\.", -1);
                if (parts.length == 3 && !parts[0].isEmpty() && !parts[2].isEmpty()) {
                    Map<?, ?> claims = BinaryData.fromBytes(Base64.getUrlDecoder().decode(parts[1])).toObject(Map.class);
                    Object oid = claims.get("oid");
                    Object tid = claims.get("tid");
                    Object expiry = claims.get("exp");
                    long now = Instant.now().getEpochSecond();
                    if (oid instanceof String && !((String) oid).isEmpty()
                        && tid instanceof String && !((String) tid).isEmpty() && expiry instanceof Number
                        && ((Number) expiry).longValue() > now + 60
                        && ((Number) expiry).longValue() <= now + 7200) {
                        return claims;
                    }
                }
            } catch (RuntimeException ex) {
                // Never attach token parsing exceptions: their messages can contain credentials.
            }
            throw new IllegalArgumentException("Supply raw user JWTs with oid/tid and 1 minute to 2 hours remaining.");
        }
    }
}
