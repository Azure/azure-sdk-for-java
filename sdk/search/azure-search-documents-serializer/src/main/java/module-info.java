// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.search.documents.serializer {
    requires transitive com.azure.core.experimental;

    exports com.azure.search.documents.serializer;

    uses com.azure.search.documents.serializer.SearchSerializerProvider;
}
