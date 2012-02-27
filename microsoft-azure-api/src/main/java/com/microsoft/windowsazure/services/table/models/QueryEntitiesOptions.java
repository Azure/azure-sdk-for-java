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

public class QueryEntitiesOptions extends TableServiceOptions {
    private Query query;
    public String nextPartitionKey;
    public String nextRowKey;

    @Override
    public QueryEntitiesOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public Query getQuery() {
        return query;
    }

    public QueryEntitiesOptions setQuery(Query query) {
        this.query = query;
        return this;
    }

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
}
