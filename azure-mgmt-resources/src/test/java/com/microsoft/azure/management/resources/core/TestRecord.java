/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.core;

import java.util.LinkedList;

public class TestRecord {
    public LinkedList<NetworkCallRecord> networkCallRecords;

    public LinkedList<String> variables;

    public TestRecord() {
        networkCallRecords = new LinkedList<>();
        variables = new LinkedList<>();
    }
}
