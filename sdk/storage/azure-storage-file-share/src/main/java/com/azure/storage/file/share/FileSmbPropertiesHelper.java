// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.HttpHeaders;

/**
 * A class used to allow access to the FileSmb constructor in other packages.
 */
public class FileSmbPropertiesHelper {

    /**
     * Creates a new FileSmbProperties object from HttpHeaders.
     *
     * @param httpHeaders The headers to construct FileSmbProperties from.
     * @return The new FileSmbProperties object.
     */
    public static FileSmbProperties createNewFileSmbProperties(HttpHeaders httpHeaders) {
        return new FileSmbProperties(httpHeaders);
    }

}
