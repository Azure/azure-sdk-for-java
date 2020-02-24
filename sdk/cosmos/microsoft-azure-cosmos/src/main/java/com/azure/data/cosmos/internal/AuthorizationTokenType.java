// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

public enum AuthorizationTokenType {
    Invalid,
    PrimaryMasterKey,
    PrimaryReadonlyMasterKey,
    SecondaryMasterKey,
    SecondaryReadonlyMasterKey,
    SystemReadOnly, 
    SystemReadWrite,
    SystemAll,
    ResourceToken
}
