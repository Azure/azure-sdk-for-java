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

public class QueryEntitiesOptions extends TableServiceOptions {

    private List<String> selectFields = new ArrayList<String>();
    private String from;
    private Filter filter;
    private List<String> orderByFields = new ArrayList<String>();
    private Integer top;

    public String nextPartitionKey;
    public String nextRowKey;

    public String getNextPartitionKey() {
        return nextPartitionKey;
    }

    public QueryEntitiesOptions setNextPartitionKey(String nextPartitionKey) {
        this.nextPartitionKey = nextPartitionKey;
        return this;
    }

    public String getNextRowKey() {
        return nextRowKey;
    }

    public QueryEntitiesOptions setNextRowKey(String nextRowKey) {
        this.nextRowKey = nextRowKey;
        return this;
    }

    public List<String> getSelectFields() {
        return selectFields;
    }

    public QueryEntitiesOptions setSelectFields(List<String> selectFields) {
        this.selectFields = selectFields;
        return this;
    }

    public QueryEntitiesOptions addSelectField(String selectField) {
        this.selectFields.add(selectField);
        return this;
    }

    public String getFrom() {
        return from;
    }

    public QueryEntitiesOptions setFrom(String from) {
        this.from = from;
        return this;
    }

    public Filter getFilter() {
        return filter;
    }

    public QueryEntitiesOptions setFilter(Filter filter) {
        this.filter = filter;
        return this;
    }

    public List<String> getOrderByFields() {
        return orderByFields;
    }

    public QueryEntitiesOptions setOrderByFields(List<String> orderByFields) {
        this.orderByFields = orderByFields;
        return this;
    }

    public QueryEntitiesOptions addOrderByField(String orderByField) {
        this.orderByFields.add(orderByField);
        return this;
    }

    public Integer getTop() {
        return top;
    }

    public QueryEntitiesOptions setTop(Integer top) {
        this.top = top;
        return this;
    }
}
