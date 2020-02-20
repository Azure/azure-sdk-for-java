// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * The LinkedEntity model.
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
    private final List<LinkedEntityMatch> linkedEntityMatches;

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
     * Creates a {@code LinkedEntity} model that describes linked entity.
     *
     * @param name entity Linking formal name
     * @param linkedEntityMatches list of instances this entity appears in the text
     * @param language language used in the data source
     * @param dataSourceEntityId unique identifier of the recognized entity from the data source
     * @param url URL for the entity's page from the data source
     * @param dataSource data source used to extract entity linking, such as Wiki/Bing etc
     */
    public LinkedEntity(String name, List<LinkedEntityMatch> linkedEntityMatches, String language,
        String dataSourceEntityId, String url, String dataSource) {
        this.name = name;
        this.linkedEntityMatches = linkedEntityMatches;
        this.language = language;
        this.dataSourceEntityId = dataSourceEntityId;
        this.url = url;
        this.dataSource = dataSource;
    }

    /**
     * Get the name property: Entity Linking formal name.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the linkedEntityMatches property: List of instances this entity appears in the text.
     *
     * @return the linkedEntityMatches value.
     */
    public List<LinkedEntityMatch> getLinkedEntityMatches() {
        return this.linkedEntityMatches;
    }

    /**
     * Get the language property: Language used in the data source.
     *
     * @return the language value.
     */
    public String getLanguage() {
        return this.language;
    }

    /**
     * Get the id property: Unique identifier of the recognized entity from the data source.
     *
     * @return the id value.
     */
    public String getDataSourceEntityId() {
        return this.dataSourceEntityId;
    }

    /**
     * Get the url property: URL for the entity's page from the data source.
     *
     * @return the url value.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Get the dataSource property: Data source used to extract entity linking, such as Wiki/Bing etc.
     *
     * @return the dataSource value.
     */
    public String getDataSource() {
        return this.dataSource;
    }
}
