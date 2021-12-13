// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * Metadata class is based on response of https://management.azure.com/metadata/endpoints?api-version=2019-10-01
 *
 * I do not aware of existing schema for the metadata response.
 */

/**
 * The metadata of endpoint of Azure Resource Manager.
 */
public final class MetadataEndpoint {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String name;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String portal;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String gallery;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String graph;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String sqlManagement;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private MetadataAuthentication authentication;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private MetadataSuffixes suffixes;

    public String getName() {
        return name;
    }

    public String getPortal() {
        return portal;
    }

    public String getGallery() {
        return gallery;
    }

    public String getGraph() {
        return graph;
    }

    public MetadataAuthentication getAuthentication() {
        return authentication;
    }

    public MetadataSuffixes getSuffixes() {
        return suffixes;
    }

    public String getSqlManagement() {
        return sqlManagement;
    }
}
