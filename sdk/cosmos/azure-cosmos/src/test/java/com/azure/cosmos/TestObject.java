// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.rx.DocumentCrudTest;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TestObject {
    private String id;
    private String mypk;
    private List<List<Integer>> sgmts;
    private String stringProp;

    public TestObject() {
    }

    public TestObject(String id, String mypk, List<List<Integer>> sgmts, String stringProp) {
        this.id = id;
        this.mypk = mypk;
        this.sgmts = sgmts;
        this.stringProp = stringProp;
    }

    public static TestObject create() {
        return new TestObject(UUID.randomUUID().toString(), UUID.randomUUID().toString(), ImmutableList.of(ImmutableList.of(5)), UUID.randomUUID().toString());
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

    public List<List<Integer>> getSgmts() {
        return sgmts;
    }

    public void setSgmts(List<List<Integer>> sgmts) {
        this.sgmts = sgmts;
    }

    /**
     * Getter for property 'stringProp'.
     *
     * @return Value for property 'stringProp'.
     */
    public String getStringProp() {
        return stringProp;
    }

    /**
     * Setter for property 'stringProp'.
     *
     * @param stringProp Value to set for property 'stringProp'.
     */
    public void setStringProp(String stringProp) {
        this.stringProp = stringProp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestObject that = (TestObject) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(mypk, that.mypk) &&
            Objects.equals(sgmts, that.sgmts) &&
            Objects.equals(stringProp, that.stringProp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, mypk, sgmts, stringProp);
    }
}
