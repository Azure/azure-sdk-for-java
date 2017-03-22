package com.microsoft.azure.management.compute;

/**
 * Created by alvab on 3/22/17.
 */
public enum ContainerServiceMasterProfileCount {
    MIN(1),
    MID(3),
    MAX(5);

    private int count;

    ContainerServiceMasterProfileCount(int count) {
        this.count = count;
    }

    public int count() {
        return this.count;
    }
}
