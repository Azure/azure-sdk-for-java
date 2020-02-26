package com.azure.storage.quickquery.models;

public class BlobQuickQueryError {

    private boolean fatal;
    private String name;
    private String description;
    private long position;

    public BlobQuickQueryError(boolean fatal, String name, String description, long position) {
        this.fatal = fatal;
        this.name = name;
        this.description = description;
        this.position = position;
    }

    public boolean isFatal() {
        return fatal;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public long getPosition() {
        return position;
    }
}
