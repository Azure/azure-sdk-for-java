// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.implementation.models;

import com.azure.storage.file.datalake.models.FileOpenInputStreamResult;
import com.azure.storage.file.datalake.models.PathProperties;

import java.io.InputStream;

public class InternalFileOpenInputStreamResult implements FileOpenInputStreamResult {

    private final InputStream inputStream;
    private final PathProperties properties;

    public InternalFileOpenInputStreamResult(InputStream inputStream, PathProperties properties){
        this.inputStream = inputStream;
        this.properties = properties;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public PathProperties getProperties() {
        return properties;
    }
}
