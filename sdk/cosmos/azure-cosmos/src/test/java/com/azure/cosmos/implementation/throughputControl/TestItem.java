// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

public class TestItem {
    private String id;
    private String mypk;
    private String prop;

    public TestItem() {
    }

    public TestItem(String id, String mypk, String prop) {
        this.id = id;
        this.mypk = mypk;
        this.prop = prop;
    }

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

    public String getProp() {
        return prop;
    }

    public void setProp(String prop) {
        this.prop = prop;
    }
}
