// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link LinkedEntity} model.
 */
@Immutable
public final class LinkedEntity {
    /*
     * Entity Linking formal name.
     */
    private final String name;

    /*
     * List of instances this entity appears in the text.
     */
    private final IterableStream<LinkedEntityMatch> matches;

    /*
     * Language used in the data source.
     */
    private final String language;

    /*
     * Unique identifier of the recognized entity from the data source.
     */
    private final String dataSourceEntityId;

    /*
     * URL for the entity's page from the data source.
     */
    private final String url;

    /*
     * Data source used to extract entity linking, such as Wiki/Bing etc.
     */
    private final String dataSource;

    /**
     * Creates a {@link LinkedEntity} model that describes linked entity.
     *
     * @param name The entity Linking formal name.
     * @param matches A list of instances this entity appears in the text.
     * @param language The language used in the data source.
     * @param dataSourceEntityId Unique identifier of the recognized entity from the data source.
     * @param url URL for the entity's page from the data source.
     * @param dataSource The data source used to extract entity linking, such as Wiki/Bing etc.
     */
    public LinkedEntity(String name, IterableStream<LinkedEntityMatch> matches, String language,
                        String dataSourceEntityId, String url, String dataSource) {
        this.name = name;
        this.matches = matches;
        this.language = language;
        this.dataSourceEntityId = dataSourceEntityId;
        this.url = url;
        this.dataSource = dataSource;
    }

    /**
     * Get the name property: Entity Linking formal name.
     *
     * @return The name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the linked entities matched property: List of instances this entity appears in the text.
     *
     * @return The linked entities matched value.
     */
    public IterableStream<LinkedEntityMatch> getMatches() {
        return this.matches;
    }

    /**
     * Get the language property: Language used in the data source.
     *
     * @return The language value.
     */
    public String getLanguage() {
        return this.language;
    }

    /**
     * Get the id property: Unique identifier of the recognized entity from the data source.
     *
     * @return The id value.
     */
    public String getDataSourceEntityId() {
        return this.dataSourceEntityId;
    }

    /**
     * Get the url property: URL for the entity's page from the data source.
     *
     * @return The URL value.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Get the dataSource property: Data source used to extract entity linking, such as Wiki/Bing etc.
     *
     * @return The dataSource value.
     */
    public String getDataSource() {
        return this.dataSource;
    }
}
