// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.common.domain;

import com.azure.spring.data.gremlin.annotation.Edge;
import com.azure.spring.data.gremlin.annotation.EdgeFrom;
import com.azure.spring.data.gremlin.annotation.EdgeTo;

@Edge
public class BookReference {

    private Integer id;

    @EdgeFrom
    private Integer fromSerialNumber;

    @EdgeTo
    private Integer toSerialNumber;

    public BookReference() {
    }

    public BookReference(Integer id, Integer fromSerialNumber, Integer toSerialNumber) {
        this.id = id;
        this.fromSerialNumber = fromSerialNumber;
        this.toSerialNumber = toSerialNumber;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFromSerialNumber() {
        return fromSerialNumber;
    }

    public void setFromSerialNumber(Integer fromSerialNumber) {
        this.fromSerialNumber = fromSerialNumber;
    }

    public Integer getToSerialNumber() {
        return toSerialNumber;
    }

    public void setToSerialNumber(Integer toSerialNumber) {
        this.toSerialNumber = toSerialNumber;
    }
}
