// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.AzureOpenAIModelName;
import com.azure.search.documents.indexes.models.AzureOpenAIVectorizerParameters;
import com.azure.search.documents.indexes.models.EntraAppAuthentication;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeBaseAzureOpenAIModel;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.indexes.models.WorkIQKnowledgeSource;
import com.azure.search.documents.indexes.models.WorkIQKnowledgeSourceParameters;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClientBuilder;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalSemanticIntent;
import com.azure.search.documents.knowledgebases.models.WorkIQKnowledgeSourceParams;

import java.util.Collections;
import java.util.UUID;

/**
 * Demonstrates a Work IQ knowledge source using a customer-owned Microsoft Entra application.
 *
 * <p>Set {@code SEARCH_ENDPOINT}, {@code SEARCH_API_KEY}, {@code SEARCH_WORK_IQ_APPLICATION_ID},
 * {@code SEARCH_WORK_IQ_FEDERATED_CREDENTIAL_ID}, {@code SEARCH_WORK_IQ_USER_ASSERTION},
 * {@code SEARCH_OPENAI_ENDPOINT}, {@code SEARCH_OPENAI_API_KEY}, {@code SEARCH_OPENAI_DEPLOYMENT_NAME}, and
 * {@code SEARCH_OPENAI_MODEL_NAME}. Set {@code SEARCH_WORK_IQ_TENANT_ID} only when the application is in a different
 * tenant from the Search service.</p>
 */
public class KnowledgeSourceWorkIqPreviewExample {
    public static void main(String[] args) {
        String endpoint = System.getenv("SEARCH_ENDPOINT");
        String apiKey = System.getenv("SEARCH_API_KEY");
        String tenantId = System.getenv("SEARCH_WORK_IQ_TENANT_ID");

        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder().endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildClient();

        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String knowledgeSourceName = "work-iq-source-" + suffix;
        String knowledgeBaseName = "work-iq-kb-" + suffix;
        boolean knowledgeSourceCreated = false;
        boolean knowledgeBaseCreated = false;

        try {
            EntraAppAuthentication entraAuthentication = new EntraAppAuthentication(
                System.getenv("SEARCH_WORK_IQ_APPLICATION_ID"),
                System.getenv("SEARCH_WORK_IQ_FEDERATED_CREDENTIAL_ID"));
            if (tenantId != null && !tenantId.isEmpty()) {
                entraAuthentication.setTenantId(tenantId);
            }

            // Federation is configured on the Entra application and Search service identity. The SDK doesn't accept a
            // client secret or a user-assigned identity resource ID for this Work IQ configuration.
            WorkIQKnowledgeSource knowledgeSource = new WorkIQKnowledgeSource(knowledgeSourceName,
                new WorkIQKnowledgeSourceParameters(entraAuthentication));
            WorkIQKnowledgeSource created
                = (WorkIQKnowledgeSource) searchIndexClient.createKnowledgeSource(knowledgeSource);
            knowledgeSourceCreated = true;

            EntraAppAuthentication persistedAuthentication
                = created.getWorkIQParameters().getEntraAppAuthentication();
            if (!entraAuthentication.getApplicationId().equals(persistedAuthentication.getApplicationId())
                || !entraAuthentication.getFederatedCredentialId()
                    .equals(persistedAuthentication.getFederatedCredentialId())
                || (tenantId == null && persistedAuthentication.getTenantId() != null)
                || (tenantId != null && !tenantId.equals(persistedAuthentication.getTenantId()))) {
                throw new IllegalStateException("The Work IQ Entra application configuration wasn't persisted.");
            }

            KnowledgeBase knowledgeBase
                = new KnowledgeBase(knowledgeBaseName, new KnowledgeSourceReference(knowledgeSourceName))
                    .setModels(new KnowledgeBaseAzureOpenAIModel(new AzureOpenAIVectorizerParameters()
                        .setResourceUrl(System.getenv("SEARCH_OPENAI_ENDPOINT"))
                        .setApiKey(System.getenv("SEARCH_OPENAI_API_KEY"))
                        .setDeploymentName(System.getenv("SEARCH_OPENAI_DEPLOYMENT_NAME"))
                        .setModelName(AzureOpenAIModelName.fromString(System.getenv("SEARCH_OPENAI_MODEL_NAME")))));
            searchIndexClient.createKnowledgeBase(knowledgeBase);
            knowledgeBaseCreated = true;

            KnowledgeBaseRetrievalClient retrievalClient = new KnowledgeBaseRetrievalClientBuilder().endpoint(endpoint)
                .credential(new AzureKeyCredential(apiKey))
                .knowledgeBaseName(knowledgeBaseName)
                .buildClient();
            WorkIQKnowledgeSourceParams sourceParams
                = new WorkIQKnowledgeSourceParams(knowledgeSourceName).setAlwaysQuerySource(true)
                    .setIncludeReferences(true);
            KnowledgeBaseRetrievalOptions options = new KnowledgeBaseRetrievalOptions()
                .setIntents(new KnowledgeRetrievalSemanticIntent("What work items are relevant?"))
                .setKnowledgeSourceParams(Collections.singletonList(sourceParams));

            // Search authentication is supplied by the client credential. The Work IQ user assertion is supplied
            // separately in x-ms-query-work-iq-source-authorization; it isn't a Search API key or access token.
            String workIqUserAssertion = System.getenv("SEARCH_WORK_IQ_USER_ASSERTION");
            KnowledgeBaseRetrievalResult result = retrievalClient.retrieve(options, null, workIqUserAssertion);
            if (result.getResponse() == null) {
                throw new IllegalStateException("Work IQ retrieval didn't return a response.");
            }
            System.out.println("Work IQ retrieval completed with " + result.getResponse().size() + " messages.");
        } finally {
            if (knowledgeBaseCreated) {
                searchIndexClient.deleteKnowledgeBase(knowledgeBaseName);
            }
            if (knowledgeSourceCreated) {
                searchIndexClient.deleteKnowledgeSource(knowledgeSourceName);
            }
        }
    }
}
