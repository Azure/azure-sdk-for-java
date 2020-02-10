// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio.implementation.util;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.BlobHttpHeaders;

import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * RESERVED FOR INTERNAL USE
 */
public class Utility {
    public static <T extends Exception> T logError(ClientLogger logger, T e) {
        logger.error(e.getMessage());
        return e;
    }

    /*
    Note that this will remove the properties from the list of attributes as it finds them.
     */
    public static BlobHttpHeaders extractHttpHeaders(List<FileAttribute<?>> fileAttributes, ClientLogger logger) {
        BlobHttpHeaders headers = new BlobHttpHeaders();
        for (Iterator<FileAttribute<?>> it = fileAttributes.iterator(); it.hasNext(); ) {
            FileAttribute<?> attr = it.next();
            boolean propertyFound = true;
            switch (attr.name()) {
                case "Content-Type":
                    headers.setContentType(attr.value().toString());
                    break;
                case "Content-Language":
                    headers.setContentLanguage(attr.value().toString());
                    break;
                case "Content-Disposition":
                    headers.setContentDisposition(attr.value().toString());
                    break;
                case "Content-MD5":
                    if ((attr.value() instanceof byte[])) {
                        headers.setContentMd5((byte[]) attr.value());
                    } else {
                        throw Utility.logError(logger,
                            new UnsupportedOperationException("Cannot read Content-MD5 attribute as byte[]"));
                    }
                    break;
                case "Cache-Control":
                    headers.setCacheControl(attr.value().toString());
                    break;
                default:
                    propertyFound = false;
                    break;
            }

            if (propertyFound) {
                it.remove();
            }
        }

        return headers;
    }

    public static Map<String, String> convertAttributesToMetadata(List<FileAttribute<?>> fileAttributes) {
        Map<String, String> metadata = new HashMap<>();
        for (FileAttribute<?> attr : fileAttributes) {
            metadata.put(attr.name(), attr.value().toString());
        }
        return metadata;
    }
}
