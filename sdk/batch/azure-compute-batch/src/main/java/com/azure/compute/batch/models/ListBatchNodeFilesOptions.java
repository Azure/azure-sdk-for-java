package com.azure.compute.batch.models;

public class ListBatchNodeFilesOptions extends BatchListOptions {
    private Boolean recursive;

    /**
     * Gets a value indicating whether to list children of a directory.
     *
     * @return A value indicating whether to list children of a directory.
     */
    public Boolean getRecursive() {
        return recursive;
    }

    /**
     * Sets a value indicating whether to list children of a directory.
     *
     * @param recursive A value indicating whether to list children of a directory.
     */
    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }

}
