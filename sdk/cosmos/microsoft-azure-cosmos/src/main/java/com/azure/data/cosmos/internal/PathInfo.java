// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

/**
 * Represents a resource path's information in the Azure Cosmos DB database service.
 */
public final class PathInfo {
    public boolean isFeed;
    public String resourcePath;
    public String resourceIdOrFullName;
    public boolean isNameBased;

    public PathInfo(boolean isFeed, String resourcePath, String resourceIdOrFullName, boolean isNameBased) {
        this.isFeed = isFeed;
        this.resourcePath = resourcePath;
        this.resourceIdOrFullName = resourceIdOrFullName;
        this.isNameBased = isNameBased;
    }
}