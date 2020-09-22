//------------------------------------------------------------
// Copyright (c) Microsoft Corporation.  All rights reserved.
//------------------------------------------------------------\

package com.azure.cosmos.implementation.directconnectivity.rntbd;

public enum RntbdConnectionEvent {

    READ_EOF,
    READ_FAILURE;

    public static final int SIZE = Integer.SIZE;
}
