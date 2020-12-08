// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

/**
 * POJO Class for usage in  {@link CosmosConflictsTest}
 */
public class ConflictTestPojo {
    private String id;
    private String mypk;
    private int regionId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMypk() {
        return mypk;
    }

    public void setMypk(String mypk) {
        this.mypk = mypk;
    }

    public int getRegionId() {
        return regionId;
    }

    public void setRegionId(int test) {
        this.regionId = test;
    }
}
