// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.models.RequestOptions;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;

/**
 * Synchronous Client to manage and query skillset, as well as manage other resources,
 * on a Cognitive Search service.
 */
public class SearchIndexerSkillsetClient {
    private final SearchIndexerSkillsetAsyncClient asyncClient;

    SearchIndexerSkillsetClient(SearchIndexerSkillsetAsyncClient searchServiceAsyncClient) {
        this.asyncClient = searchServiceAsyncClient;
    }

    /**
     * Creates a new skillset in an Azure Cognitive Search service.
     *
     * @param skillset definition of the skillset containing one or more cognitive skills
     * @return the created Skillset.
     */
    public SearchIndexerSkillset create(SearchIndexerSkillset skillset) {
        return createWithResponse(skillset, null, Context.NONE).getValue();
    }

    /**
     * Creates a new skillset in an Azure Cognitive Search service.
     *
     * @param skillset definition of the skillset containing one or more cognitive skills
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the created Skillset.
     */
    public Response<SearchIndexerSkillset> createWithResponse(SearchIndexerSkillset skillset,
        RequestOptions requestOptions, Context context) {
        return asyncClient.createWithResponse(skillset, requestOptions, context).block();
    }

    /**
     * Retrieves a skillset definition.
     *
     * @param skillsetName the name of the skillset to retrieve
     * @return the Skillset.
     */
    public SearchIndexerSkillset getSkillset(String skillsetName) {
        return getSkillsetWithResponse(skillsetName, null, Context.NONE).getValue();
    }

    /**
     * Retrieves a skillset definition.
     *
     * @param skillsetName the name of the skillset to retrieve
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the Skillset.
     */
    public Response<SearchIndexerSkillset> getSkillsetWithResponse(String skillsetName, RequestOptions requestOptions,
        Context context) {
        return asyncClient.getSkillsetWithResponse(skillsetName, requestOptions, context).block();
    }

    /**
     * Lists all skillsets available for an Azure Cognitive Search service.
     *
     * @return the list of skillsets.
     */
    public PagedIterable<SearchIndexerSkillset> listSkillsets() {
        return listSkillsets(null, Context.NONE);
    }

    /**
     * Lists all skillsets available for an Azure Cognitive Search service.
     *
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return the list of skillsets.
     */
    public PagedIterable<SearchIndexerSkillset> listSkillsets(RequestOptions requestOptions, Context context) {
        return new PagedIterable<>(asyncClient.listSkillsets(null, requestOptions, context));
    }

    /**
     * Creates a new Azure Cognitive Search skillset or updates a skillset if it already exists.
     *
     * @param skillset the {@link SearchIndexerSkillset} to create or update.
     * @return the skillset that was created or updated.
     */
    public SearchIndexerSkillset createOrUpdate(SearchIndexerSkillset skillset) {
        return createOrUpdateWithResponse(skillset, false, null, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search skillset or updates a skillset if it already exists.
     *
     * @param skillset the {@link SearchIndexerSkillset} to create or update.
     * @param onlyIfUnchanged {@code true} to update if the {@code skillset} is the same as the current service value.
     * {@code false} to always update existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the skillset that was created or updated.
     */
    public Response<SearchIndexerSkillset> createOrUpdateWithResponse(SearchIndexerSkillset skillset,
        boolean onlyIfUnchanged, RequestOptions requestOptions, Context context) {
        return asyncClient.createOrUpdateWithResponse(skillset, onlyIfUnchanged, requestOptions, context)
            .block();
    }

    /**
     * Deletes a cognitive skillset in an Azure Cognitive Search service.
     *
     * @param skillsetName the name of the skillset to delete
     */
    public void delete(String skillsetName) {
        deleteWithResponse(new SearchIndexerSkillset().setName(skillsetName), false, null, Context.NONE);
    }

    /**
     * Deletes a cognitive skillset in an Azure Cognitive Search service.
     *
     * @param skillset the {@link SearchIndexerSkillset} to delete.
     * @param onlyIfUnchanged {@code true} to delete if the {@code skillset} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response signalling completion.
     */
    public Response<Void> deleteWithResponse(SearchIndexerSkillset skillset, boolean onlyIfUnchanged,
        RequestOptions requestOptions, Context context) {
        String etag = onlyIfUnchanged ? skillset.getETag() : null;
        return asyncClient.deleteWithResponse(skillset.getName(), etag, requestOptions, context).block();
    }

    /**
     * List all SearchIndexerSkillset names from an Azure Cognitive Search service.
     *
     * @return a list of SearchIndexerSkillset names.
     */
    public PagedIterable<SearchIndexerSkillset> listSkillsetNames() {
        return listSkillsetNames(null, Context.NONE);
    }

    /**
     * List all SearchIndexerSkillset names from an Azure Cognitive Search service.
     *
     * @param requestOptions Additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return a list of SearchIndexerSkillset names
     */
    public PagedIterable<SearchIndexerSkillset> listSkillsetNames(RequestOptions requestOptions, Context context) {
        return new PagedIterable<>(asyncClient.listSkillsetNames(requestOptions, context));
    }
}
