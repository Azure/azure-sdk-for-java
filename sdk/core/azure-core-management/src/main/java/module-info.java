// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.management {
    requires transitive com.azure.core;

    exports com.azure.core.management;
    exports com.azure.core.management.polling;
    exports com.azure.core.management.serializer;
    exports com.azure.core.management.exception;
    exports com.azure.core.management.profile;
    exports com.azure.core.management.provider;
    exports com.azure.core.management.http.policy;

    opens com.azure.core.management to
        com.fasterxml.jackson.databind,
        com.azure.core;
    opens com.azure.core.management.exception to
        com.fasterxml.jackson.databind,
        com.azure.core;
    opens com.azure.core.management.implementation.polling to
        com.fasterxml.jackson.databind;

    uses com.azure.core.http.HttpClientProvider;
}
