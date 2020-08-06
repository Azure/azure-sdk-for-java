// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.core.experimental.serializer.AvroSerializerProvider;

module com.azure.core.experimental {
    requires transitive com.azure.core;

    exports com.azure.core.experimental.serializer;
    exports com.azure.core.experimental.spatial;

    uses AvroSerializerProvider;
}
