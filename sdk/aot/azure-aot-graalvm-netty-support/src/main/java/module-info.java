// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.aot.graalvm.netty.support {
    requires transitive com.azure.aot.graalvm.support;
    requires io.netty.all;
    exports com.azure.aot.graalvm.netty.support.implementation.features;
}
