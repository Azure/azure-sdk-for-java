package com.azure.storage.file.datalake.implementation.util;

import com.azure.storage.file.datalake.models.FileSystemProperties;
import com.azure.storage.file.datalake.models.PathProperties;

public class AccessorUtility {

    private static PathPropertiesAccessor pathPropertiesAccessor;
    private static FileSystemPropertiesAccessor fileSystemPropertiesAccessor;

    public static FileSystemPropertiesAccessor getFileSystemPropertiesAccessor() {
        return fileSystemPropertiesAccessor;
    }

    public static void setFileSystemPropertiesAccessor(FileSystemPropertiesAccessor fileSystemPropertiesAccessor) {
        AccessorUtility.fileSystemPropertiesAccessor = fileSystemPropertiesAccessor;
    }

    public interface PathPropertiesAccessor {
        PathProperties setPathProperties(PathProperties properties, String encryptionScope);

    }

    public interface FileSystemPropertiesAccessor {
        FileSystemProperties setFileSystemProperties(FileSystemProperties properties, String encryptionScope, Boolean isOverride);
    }

    public static void setPathPropertiesAccessor(PathPropertiesAccessor accessor) {
        pathPropertiesAccessor = accessor;
    }

    public static PathPropertiesAccessor getPathPropertiesAccessor() {
        return pathPropertiesAccessor;
    }
}
