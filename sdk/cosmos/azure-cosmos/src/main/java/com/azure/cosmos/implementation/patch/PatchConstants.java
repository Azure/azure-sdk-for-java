// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.patch;

final class PatchConstants {

    // Properties
    static final String PropertyNames_OperationType = "op";
    static final String PropertyNames_Path = "path";
    static final String PropertyNames_Value = "value";

    // Operations
    static final String OperationTypeNames_Add = "add";
    static final String OperationTypeNames_Remove = "remove";
    static final String OperationTypeNames_Replace = "replace";
    static final String OperationTypeNames_Set = "set";
    static final String OperationTypeNames_Increment = "incr";

    static final String CONDITION = "condition";
    static final String OPERATIONS = "operations";
}
