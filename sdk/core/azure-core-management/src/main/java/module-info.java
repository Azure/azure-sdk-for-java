// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.management {
    requires transitive com.azure.core;
    requires org.reactivestreams;

    opens com.azure.core.management to com.fasterxml.jackson.databind;
    opens com.azure.core.management.implementation.polling to com.fasterxml.jackson.databind;

    uses com.azure.core.http.HttpClientProvider;
}
