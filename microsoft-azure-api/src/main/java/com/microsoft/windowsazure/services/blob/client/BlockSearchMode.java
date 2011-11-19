package com.microsoft.windowsazure.services.blob.client;

/**
 * Specifies which block lists should be searched to find a specified block.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public enum BlockSearchMode {
    /**
     * Specifies searching only the committed block list.
     */
    COMMITTED,

    /**
     * Specifies searching only the uncommitted block list.
     */
    UNCOMMITTED,

    /**
     * Specifies searching the uncommitted block list first, and if the block is not found, then search the committed
     * block list.
     */
    LATEST
}
