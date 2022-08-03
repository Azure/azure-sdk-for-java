// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.administration.models.ResourceDetails;

/**
 * The helper class to set the non-public properties of an {@link ResourceDetails} instance.
 */
public final class ResourceDetailsHelper {
    private static ResourceDetailsAccessor accessor;

    private ResourceDetailsHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link ResourceDetails} instance.
     */
    public interface ResourceDetailsAccessor {
        void setDocumentModelCount(ResourceDetails resourceDetails, int documentModelCount);
        void setDocumentModelLimit(ResourceDetails resourceDetails, int documentModelLimit);
    }

    /**
     * The method called from {@link ResourceDetails} to set it's accessor.
     *
     * @param resourceDetailsAccessor The accessor.
     */
    public static void setAccessor(final ResourceDetailsAccessor resourceDetailsAccessor) {
        accessor = resourceDetailsAccessor;
    }

    static void setDocumentModelCount(ResourceDetails resourceDetails, int documentModelCount) {
        accessor.setDocumentModelCount(resourceDetails, documentModelCount);
    }

    static void setDocumentModelLimit(ResourceDetails resourceDetails, int documentModelLimit) {
        accessor.setDocumentModelLimit(resourceDetails, documentModelLimit);
    }
}
