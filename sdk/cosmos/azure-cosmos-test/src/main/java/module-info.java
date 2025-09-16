// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.cosmos.test {

    requires transitive com.azure.cosmos;
    requires reactor.core;

    exports com.azure.cosmos.test.faultinjection;
}
