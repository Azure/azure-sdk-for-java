// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.aot.graalvm.support {
    requires transitive org.graalvm.sdk;
    requires transitive svm;
    exports com.azure.aot.graalvm.support.implementation;
    exports com.azure.aot.graalvm.support.implementation.features;
}

