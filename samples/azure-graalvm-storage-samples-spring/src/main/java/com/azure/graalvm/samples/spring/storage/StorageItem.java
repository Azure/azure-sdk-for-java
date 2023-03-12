// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.graalvm.samples.spring.storage;

import java.io.InputStream;

/**
 * An interface designed to represent a single file stored in the backing {@link StorageService}.
 */
public interface StorageItem {

    /**
     * The filename of the file that was previously stored in the storage service.
     */
    String getFileName();

    /**
     * The size of the file that was previously stored in the storage service.
     */
    double getFileSize();

    /**
     * Returns the file content.
     */
    InputStream getContent();

    /**
     * Returns the content type of the file.
     */
    String getContentType();

    default DisplayMode getContentDisplayMode() {
        // we can key off of the filename extension as a first guess
        final String filename = getFileName();
        if (filename.contains(".")) {
            final String ext = filename.substring(filename.lastIndexOf(".") + 1);
            if (ext != null && !ext.isEmpty()) {
                switch (ext) {
                    case "gif":
                    case "jpg":
                    case "jpeg":
                    case "png": return DisplayMode.MODAL_POPUP;
                    case "pdf": return DisplayMode.NEW_BROWSER_TAB;
                    default: return DisplayMode.DOWNLOAD;
                }
            }
        }
        return DisplayMode.DOWNLOAD;
    }
}
