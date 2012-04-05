/**
 * Copyright 2012 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.table.models;

import java.util.ArrayList;
import java.util.List;

public class Query {
    private List<String> selectFields = new ArrayList<String>();
    private String from;
    private Filter filter;
    private List<String> orderByFields = new ArrayList<String>();
    private Integer top;

    public List<String> getSelectFields() {
        return selectFields;
    }

    public Query setSelectFields(List<String> selectFields) {
        this.selectFields = selectFields;
        return this;
    }

    public Query addSelectField(String selectField) {
        this.selectFields.add(selectField);
        return this;
    }

    public String getFrom() {
        return from;
    }

    public Query setFrom(String from) {
        this.from = from;
        return this;
    }

    public Filter getFilter() {
        return filter;
    }

    public Query setFilter(Filter filter) {
        this.filter = filter;
        return this;
    }

    public List<String> getOrderByFields() {
        return orderByFields;
    }

    public Query setOrderByFields(List<String> orderByFields) {
        this.orderByFields = orderByFields;
        return this;
    }

    public Query addOrderByField(String orderByField) {
        this.orderByFields.add(orderByField);
        return this;
    }

    public Integer getTop() {
        return top;
    }

    public Query setTop(Integer top) {
        this.top = top;
        return this;
    }
}
