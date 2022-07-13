// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.administration.models.ResourceInfo;

/**
 * The helper class to set the non-public properties of an {@link ResourceInfo} instance.
 */
public final class ResourceInfoHelper {
    private static ResourceInfoAccessor accessor;

    private ResourceInfoHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link ResourceInfo} instance.
     */
    public interface ResourceInfoAccessor {
        void setDocumentModelCount(ResourceInfo resourceInfo, int documentModelCount);
        void setDocumentModelLimit(ResourceInfo resourceInfo, int documentModelLimit);
    }

    /**
     * The method called from {@link ResourceInfo} to set it's accessor.
     *
     * @param resourceInfoAccessor The accessor.
     */
    public static void setAccessor(final ResourceInfoAccessor resourceInfoAccessor) {
        accessor = resourceInfoAccessor;
    }

    static void setDocumentModelCount(ResourceInfo resourceInfo, int documentModelCount) {
        accessor.setDocumentModelCount(resourceInfo, documentModelCount);
    }

    static void setDocumentModelLimit(ResourceInfo resourceInfo, int documentModelLimit) {
        accessor.setDocumentModelLimit(resourceInfo, documentModelLimit);
    }
}
