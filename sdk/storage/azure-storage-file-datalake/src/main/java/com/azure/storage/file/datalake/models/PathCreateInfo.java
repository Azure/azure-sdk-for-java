// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.storage.file.datalake.implementation.models.PathCreateHeaders;

public class PathCreateInfo {

    private final String continuation;
    private final PathInfo pathInfo;

    public PathCreateInfo(PathCreateHeaders headers) {
        this.continuation = headers.getContinuation();
        this.pathInfo = new PathInfo(headers);
    }

    public String getContinuation() {
        return continuation;
    }

    public PathInfo getPathInfo() {
        return pathInfo;
    }
}
