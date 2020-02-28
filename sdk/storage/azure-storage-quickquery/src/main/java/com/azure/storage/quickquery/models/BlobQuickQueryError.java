// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery.models;

public class BlobQuickQueryError {

    private boolean fatal;
    private String name;
    private String description;
    private long position;

    public BlobQuickQueryError(boolean fatal, String name, String description, long position) {
        this.fatal = fatal;
        this.name = name;
        this.description = description;
        this.position = position;
    }

    public boolean isFatal() {
        return fatal;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public long getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return String.format("BlobQueryError. Is Fatal = %b, Error Name = %s, Error Description = %s, Error Position "
            + "(in source data blob) = %d.", this.fatal, this.name,this.description ,this.position);
    }

}

