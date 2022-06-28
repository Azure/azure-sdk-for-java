// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.json  {
    exports com.azure.json;

    exports com.azure.json.implementation to com.azure.json.gson;
    exports com.azure.json.implementation.jackson.core.io to com.azure.core;
}
