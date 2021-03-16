// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.perf;

class TestModelClass {
    private final String data;

    TestModelClass(String data) {
        this.data = data;
    }

    String getData() {
        return data;
    }
}
