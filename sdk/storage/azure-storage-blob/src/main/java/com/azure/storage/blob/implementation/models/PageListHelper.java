// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.models;

import com.azure.storage.blob.models.PageList;

public class PageListHelper {
    private static PageListAccessor accessor;

    public static String getNextMarker(PageList pageList) {
        return accessor.getNextMarker(pageList);
    }

    public static PageList setNextMarker(PageList pageList, String marker) {
        return accessor.setNextMarker(pageList, marker);
    }

    public static void setAccessor(PageListAccessor pageListAccessor) {
        accessor = pageListAccessor;
    }

    public interface PageListAccessor {
        String getNextMarker(PageList pageList);
        PageList setNextMarker(PageList pageList, String marker);
    }
}
