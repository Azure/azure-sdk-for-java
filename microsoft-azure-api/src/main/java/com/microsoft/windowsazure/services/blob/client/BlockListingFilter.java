package com.microsoft.windowsazure.services.blob.client;

/**
 * Specifies whether to list only committed blocks, only uncommitted blocks, or all blocks.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public enum BlockListingFilter {
    /**
     * List only committed blocks.
     */
    COMMITTED,

    /**
     * List only uncommitted blocks.
     */
    UNCOMMITTED,

    /**
     * List both committed and uncommitted blocks.
     */
    ALL
}
