// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin.common.domain;

import com.microsoft.spring.data.gremlin.annotation.Edge;
import com.microsoft.spring.data.gremlin.annotation.EdgeFrom;
import com.microsoft.spring.data.gremlin.annotation.EdgeTo;

import java.util.Objects;

@Edge
public class BookReference {

    private String id;

    @EdgeFrom
    private String fromSerialNumber;

    @EdgeTo
    private String toSerialNumber;

    public BookReference() {
    }

    public BookReference(String id, String fromSerialNumber, String toSerialNumber) {
        this.id = id;
        this.fromSerialNumber = fromSerialNumber;
        this.toSerialNumber = toSerialNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromSerialNumber() {
        return fromSerialNumber;
    }

    public void setFromSerialNumber(String fromSerialNumber) {
        this.fromSerialNumber = fromSerialNumber;
    }

    public String getToSerialNumber() {
        return toSerialNumber;
    }

    public void setToSerialNumber(String toSerialNumber) {
        this.toSerialNumber = toSerialNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BookReference that = (BookReference) o;
        return Objects.equals(id, that.id)
            && Objects.equals(fromSerialNumber, that.fromSerialNumber)
            && Objects.equals(toSerialNumber, that.toSerialNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fromSerialNumber, toSerialNumber);
    }
}
