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

import com.microsoft.windowsazure.services.core.ServiceException;

public class BatchResult {
    private List<Entry> entries = new ArrayList<Entry>();

    public List<Entry> getEntries() {
        return entries;
    }

    public BatchResult setEntries(List<Entry> entries) {
        this.entries = entries;
        return this;
    }

    public static abstract class Entry {
    }

    public static class InsertEntity extends Entry {
        private Entity entity;

        public Entity getEntity() {
            return entity;
        }

        public InsertEntity setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }
    }

    public static class UpdateEntity extends Entry {
        private String etag;

        public String getEtag() {
            return etag;
        }

        public UpdateEntity setEtag(String etag) {
            this.etag = etag;
            return this;
        }
    }

    public static class DeleteEntity extends Entry {

    }

    public static class Error extends Entry {
        private ServiceException error;

        public ServiceException getError() {
            return error;
        }

        public Error setError(ServiceException error) {
            this.error = error;
            return this;
        }
    }
}
