// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.IterableStream;

/**
 * The {@link LinkedEntity} model.
 */
public interface LinkedEntity {
    /**
     * Get the name property: Entity Linking formal name.
     *
     * @return The name value.
     */
    String getName();

    /**
     * Get the linkedEntityMatches property: List of instances this entity appears in the text.
     *
     * @return The linkedEntityMatches value.
     */
    IterableStream<LinkedEntityMatch> getMatches();

    /**
     * Get the language property: Language used in the data source.
     *
     * @return The language value.
     */
    String getLanguage();

    /**
     * Get the id property: Unique identifier of the recognized entity from the data source.
     *
     * @return The id value.
     */
    String getDataSourceEntityId();
    /**
     * Get the url property: URL for the entity's page from the data source.
     *
     * @return The URL value.
     */
    String getUrl();

    /**
     * Get the dataSource property: Data source used to extract entity linking, such as Wiki/Bing etc.
     *
     * @return The dataSource value.
     */
    String getDataSource();
}
