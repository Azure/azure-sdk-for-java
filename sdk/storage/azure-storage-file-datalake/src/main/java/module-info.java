// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.storage.file.datalake {
    requires transitive com.azure.storage.blob;

    requires com.azure.json;

    exports com.azure.storage.file.datalake;
    exports com.azure.storage.file.datalake.models;
    exports com.azure.storage.file.datalake.sas;
    exports com.azure.storage.file.datalake.specialized;
    exports com.azure.storage.file.datalake.options;

    exports com.azure.storage.file.datalake.implementation.util to
        com.azure.core;

    exports com.azure.storage.file.datalake.implementation.models to com.azure.core;

    opens com.azure.storage.file.datalake.implementation.models to com.azure.core;

}
