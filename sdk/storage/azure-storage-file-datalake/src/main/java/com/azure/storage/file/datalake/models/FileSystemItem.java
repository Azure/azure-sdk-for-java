package com.azure.storage.file.datalake.models;

import java.util.Map;

public class FileSystemItem {

    private String name;
    private FileSystemProperties fileSystemProperties;
    private Map<String, String> metadata;

    public FileSystemItem(String name, FileSystemProperties properties, Map<String, String> metadata) {
        this.name = name;
        this.fileSystemProperties = properties;
        this.metadata = metadata;
    }

    public String getName() {
        return name;
    }

    public FileSystemProperties getFileSystemProperties() {
        return fileSystemProperties;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }
}
