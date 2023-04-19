// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import java.io.OutputStream;

/**
 * Result of opening an {@link OutputStream} to a datalake file.
 */
public class DataLakeFileOutputStreamResult {

    private final OutputStream outputStream;

    /**
     * Initializes a new instance of DataLakeFileOutputStreamResult.
     *
     * @param outputStream the {link @OutputStream} for the datalake file.
     */
    public DataLakeFileOutputStreamResult(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * Gets the {@link OutputStream} associated with the target file.
     * 
     * @return the {@link OutputStream} of the target file.
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }
}
