package com.azure.compute.batch.models;

public class ListBatchTaskFilesOptions extends BatchListOptions{
    private Boolean recursive;

    /**
     * Gets a value indicating whether to list children of the Task directory. This parameter can be used in combination with
     * the filter parameter to list specific type of files.
     *
     * @return A value indicating whether to list children of the Task directory.
     */
    public Boolean getRecursive() {
        return recursive;
    }

    /**
     * Sets a value indicating whether to list children of the Task directory. This parameter can be used in combination with
     * the filter parameter to list specific type of files.
     *
     * @param recursive A value indicating whether to list children of the Task directory.
     */
    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }

}
