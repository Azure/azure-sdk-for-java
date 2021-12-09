// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.metadata;

public final class Metadata {
    private String name;
    private String portal;
    private String gallery;
    private String graph;
    private String sqlManagement;
    private MetadataAuthentication authentication;
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
