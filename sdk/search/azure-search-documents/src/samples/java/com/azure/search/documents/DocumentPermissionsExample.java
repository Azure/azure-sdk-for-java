// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
import com.azure.search.documents.indexes.models.IndexerPermissionOption;
import com.azure.search.documents.indexes.models.IndexingParameters;
import com.azure.search.documents.indexes.models.IndexingParametersConfiguration;
import com.azure.search.documents.indexes.models.IndexerResyncBody;
import com.azure.search.documents.indexes.models.IndexerResyncOption;
import com.azure.search.documents.indexes.models.PermissionFilter;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexPermissionFilterOption;
import com.azure.search.documents.indexes.models.SearchIndexer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SearchIndexerDataUserAssignedIdentity;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClientBuilder;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalSemanticIntent;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SharePointConnectorAppRegistration;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

/**
 * Demonstrates permission schema, permission ingestion, and queries on an existing populated permission-aware index.
 *
 * <p>Set SEARCH_ENDPOINT, SEARCH_INDEX, SEARCH_DOCUMENT_KEY, KNOWLEDGE_BASE_NAME, and SEARCH_QUERY_SOURCE_TOKEN.
 * The query-source token must identify the end user and must never be logged. DefaultAzureCredential authenticates
 * the caller to Search; it does not replace the end user's query-source token. The caller needs Search Index Data
 * Reader for queries. Elevated reads are disabled in this example.</p>
 *
 * <p>Configuration methods below illustrate models to use when provisioning a permission-aware SharePoint flow.
 * They are not applied automatically: connector registration, federated credentials, supported source permissions,
 * indexer field mappings, and role assignments must be configured for your deployment before ingestion.</p>
 * <p>Pass {@code resync} and set SEARCH_INDEXER_NAME to request permission resynchronization of an existing indexer.
 * That operation requires Search Service Contributor rather than only data-reader permissions.</p>
 */
public class DocumentPermissionsExample {
    public static void main(String[] args) {
        String endpoint = System.getenv("SEARCH_ENDPOINT");
        if (args.length > 0 && "resync".equals(args[0])) {
            new SearchIndexerClientBuilder().endpoint(endpoint)
                .credential(new DefaultAzureCredentialBuilder().build()).buildClient()
                .resync(System.getenv("SEARCH_INDEXER_NAME"), permissionResync());
            return;
        }
        String querySourceToken = Objects.requireNonNull(System.getenv("SEARCH_QUERY_SOURCE_TOKEN"),
            "Set SEARCH_QUERY_SOURCE_TOKEN to a token identifying the end user.");
        SearchClient client = new SearchClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .indexName(System.getenv("SEARCH_INDEX"))
            .buildClient();

        client.search(new SearchOptions().setSearchText("hotel")
            .setQuerySourceAuthorization(querySourceToken)
            .setEnableElevatedRead(false))
            .forEach(result -> System.out.println(result.getAdditionalProperties()));
        System.out.println(client.getDocument(System.getenv("SEARCH_DOCUMENT_KEY"), querySourceToken, false,
            Collections.singletonList("id")).getAdditionalProperties());

        new KnowledgeBaseRetrievalClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .knowledgeBaseName(System.getenv("KNOWLEDGE_BASE_NAME"))
            .buildClient()
            .retrieve(new KnowledgeBaseRetrievalOptions()
                .setIntents(new KnowledgeRetrievalSemanticIntent("Which hotels can I access?")), querySourceToken)
            .getReferences()
            .forEach(reference -> System.out.println("Authorized reference: " + reference.getId()));
    }

    static SearchIndex permissionIndex(String name, String applicationId, String federatedCredentialId) {
        return new SearchIndex(name,
            new SearchField("id", SearchFieldDataType.STRING).setKey(true),
            new SearchField("content", SearchFieldDataType.STRING).setSearchable(true),
            new SearchField("userIds", SearchFieldDataType.collection(SearchFieldDataType.STRING))
                .setFilterable(true).setRetrievable(false).setPermissionFilter(PermissionFilter.USER_IDS),
            new SearchField("groupIds", SearchFieldDataType.collection(SearchFieldDataType.STRING))
                .setFilterable(true).setRetrievable(false).setPermissionFilter(PermissionFilter.GROUP_IDS),
            new SearchField("rbacScope", SearchFieldDataType.STRING)
                .setFilterable(true).setRetrievable(false).setPermissionFilter(PermissionFilter.RBAC_SCOPE),
            new SearchField("organizationAccessExpiration", SearchFieldDataType.DATE_TIME_OFFSET)
                .setFilterable(true).setOrganizationAccessExpiration(true),
            new SearchField("siteUrl", SearchFieldDataType.STRING).setFilterable(true).setSharepointSiteUrl(true))
            .setPermissionFilterOption(SearchIndexPermissionFilterOption.ENABLED)
            .setSharePointConnectorAppRegistration(
                new SharePointConnectorAppRegistration(applicationId, federatedCredentialId));
    }

    static SearchIndexerDataSourceConnection permissionDataSource(SearchIndexerDataSourceConnection source,
        String identityResourceId, String federatedIdentityClientId) {
        return source.setIndexerPermissionOptions(Arrays.asList(IndexerPermissionOption.USER_IDS,
            IndexerPermissionOption.GROUP_IDS))
            .setIdentity(new SearchIndexerDataUserAssignedIdentity(identityResourceId)
                .setFederatedIdentityClientId(federatedIdentityClientId));
    }

    static SearchIndexer permissionIndexer(String name, String sourceName, String indexName) {
        return new SearchIndexer(name, sourceName, indexName).setParameters(new IndexingParameters()
            .setConfiguration(new IndexingParametersConfiguration().setRefreshAllAcls(true)));
    }

    static IndexerResyncBody permissionResync() {
        return new IndexerResyncBody().setOptions(IndexerResyncOption.PERMISSIONS);
    }

    /**
     * Applies a reviewed permission configuration to a new index and its supported data source and indexer.
     * The resources remain on the service so ingestion can complete before running the query example.
     *
     * @param endpoint The Search endpoint.
     * @param index The permission-aware index, with field names matching your indexer mappings.
     * @param source The supported data source configured to ingest permissions.
     * @param indexer The indexer, with the appropriate source-to-index field mappings.
     */
    public static void createPermissionResources(String endpoint, SearchIndex index,
        SearchIndexerDataSourceConnection source, SearchIndexer indexer) {
        new SearchIndexClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build()).buildClient().createIndex(index);
        com.azure.search.documents.indexes.SearchIndexerClient client = new SearchIndexerClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build()).buildClient();
        client.createDataSourceConnection(source);
        client.createIndexer(indexer);
        System.out.println("Wait for permission ingestion before querying. Delete these sample resources when finished.");
    }
}
