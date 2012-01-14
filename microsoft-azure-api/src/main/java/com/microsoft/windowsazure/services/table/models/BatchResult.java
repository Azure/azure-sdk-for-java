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
