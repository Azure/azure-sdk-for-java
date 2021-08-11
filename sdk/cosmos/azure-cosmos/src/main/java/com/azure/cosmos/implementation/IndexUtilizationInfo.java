// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class IndexUtilizationInfo {

    static final IndexUtilizationInfo ZERO = new IndexUtilizationInfo(
        new ArrayList<>(), /* utilizedSingleIndexes */
        new ArrayList<>(), /* potentialSingleIndexes */
        new ArrayList<>(), /* utilizedCompositeIndexes */
        new ArrayList<>()); /* potentialCompositeIndexes */

    @JsonProperty(value = "UtilizedSingleIndexes", access = JsonProperty.Access.WRITE_ONLY)
    private List<SingleIndexUtilizationEntity> utilizedSingleIndexes;
    @JsonProperty(value = "PotentialSingleIndexes", access = JsonProperty.Access.WRITE_ONLY)
    private List<SingleIndexUtilizationEntity> potentialSingleIndexes;
    @JsonProperty(value = "UtilizedCompositeIndexes", access = JsonProperty.Access.WRITE_ONLY)
    private List<CompositeIndexUtilizationEntity> utilizedCompositeIndexes;
    @JsonProperty(value = "PotentialCompositeIndexes", access = JsonProperty.Access.WRITE_ONLY)
    private List<CompositeIndexUtilizationEntity> potentialCompositeIndexes;

    IndexUtilizationInfo() {
        super();
    }

    /**
     * @param utilizedSingleIndexes     -> The utilized single indexes list.
     * @param potentialSingleIndexes    -> The potential single indexes list.
     * @param utilizedCompositeIndexes  -> The potential composite indexes list.
     * @param potentialCompositeIndexes -> The utilized composite indexes list.
     */
    IndexUtilizationInfo(List<SingleIndexUtilizationEntity> utilizedSingleIndexes, List<SingleIndexUtilizationEntity> potentialSingleIndexes, List<CompositeIndexUtilizationEntity> utilizedCompositeIndexes, List<CompositeIndexUtilizationEntity> potentialCompositeIndexes) {
        this.utilizedSingleIndexes = utilizedSingleIndexes;
        this.potentialSingleIndexes = potentialSingleIndexes;
        this.utilizedCompositeIndexes = utilizedCompositeIndexes;
        this.potentialCompositeIndexes = potentialCompositeIndexes;
    }

    /**
     * @return utilizedSingleIndexes
     */
    public List<SingleIndexUtilizationEntity> getUtilizedSingleIndexes() {
        return utilizedSingleIndexes;
    }

    /**
     * @return potentialSingleIndexes
     */
    public List<SingleIndexUtilizationEntity> getPotentialSingleIndexes() {
        return potentialSingleIndexes;
    }

    /**
     * @return utilizedCompositeIndexes
     */
    public List<CompositeIndexUtilizationEntity> getUtilizedCompositeIndexes() {
        return utilizedCompositeIndexes;
    }

    /**
     * @return potentialCompositeIndexes
     */
    public List<CompositeIndexUtilizationEntity> getPotentialCompositeIndexes() {
        return potentialCompositeIndexes;
    }

    /**
     * @param utilizedSingleIndexesList
     */
    public void setUtilizedSingleIndexes(List<SingleIndexUtilizationEntity> utilizedSingleIndexesList) {
        this.utilizedSingleIndexes = utilizedSingleIndexesList;
    }

    /**
     * @param potentialSingleIndexesList
     */
    public void setPotentialSingleIndexes(List<SingleIndexUtilizationEntity> potentialSingleIndexesList) {
        this.potentialSingleIndexes = potentialSingleIndexesList;
    }

    /**
     * @param utilizedCompositeIndexesList
     */
    public void setUtilizedCompositeIndexes(List<CompositeIndexUtilizationEntity> utilizedCompositeIndexesList) {
        this.utilizedCompositeIndexes = utilizedCompositeIndexesList;
    }

    /**
     * @param potentialCompositeIndexesList
     */
    public void setPotentialCompositeIndexes(List<CompositeIndexUtilizationEntity> potentialCompositeIndexesList) {
        this.potentialCompositeIndexes = potentialCompositeIndexesList;
    }


    static IndexUtilizationInfo createFromCollection(
        Collection<IndexUtilizationInfo> indexUtilizationInfoCollection) {
        if (indexUtilizationInfoCollection == null) {
            throw new NullPointerException("indexUtilizationInfoCollection");
        }

        List<SingleIndexUtilizationEntity> utilizedSingleIndexes = new ArrayList<>();
        List<SingleIndexUtilizationEntity> potentialSingleIndexes = new ArrayList<>();
        List<CompositeIndexUtilizationEntity> utilizedCompositeIndexes = new ArrayList<>();
        List<CompositeIndexUtilizationEntity> potentialCompositeIndexes = new ArrayList<>();


        for (IndexUtilizationInfo indexUtilizationInfo : indexUtilizationInfoCollection) {
            if (indexUtilizationInfo == null) {
                throw new NullPointerException("queryPreparationTimesList can not have a null element");
            }

            utilizedSingleIndexes.addAll(indexUtilizationInfo.utilizedSingleIndexes);
            potentialSingleIndexes.addAll(indexUtilizationInfo.potentialSingleIndexes);
            utilizedCompositeIndexes.addAll(indexUtilizationInfo.utilizedCompositeIndexes);
            potentialCompositeIndexes.addAll(indexUtilizationInfo.potentialCompositeIndexes);
        }

        return new IndexUtilizationInfo(
            utilizedSingleIndexes,
            potentialSingleIndexes,
            utilizedCompositeIndexes,
            potentialCompositeIndexes);
    }

    static IndexUtilizationInfo createFromJSONString(String jsonString) {
        if (StringUtils.isEmpty(jsonString)) {
            throw new NullPointerException("jsonString");
        }
        ObjectMapper indexUtilizationInfoObjectMapper = new ObjectMapper();
        IndexUtilizationInfo indexUtilizationInfo = null;
        try {
            indexUtilizationInfo = indexUtilizationInfoObjectMapper.readValue(jsonString, IndexUtilizationInfo.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return indexUtilizationInfo;
    }
}

