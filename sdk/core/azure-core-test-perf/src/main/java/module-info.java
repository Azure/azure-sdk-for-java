// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.test.perf {
    requires transitive com.azure.core;
    requires reactor.core;
    requires jcommander;
    requires reactor.netty;
    requires io.netty.handler;
//    requires transitive com.fasterxml.jackson.core;
//    opens com.azure.core.test.perf to com.fasterxml.jackson.databind;
}
