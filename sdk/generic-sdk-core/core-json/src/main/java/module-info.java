// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module io.clientcore.json  {
    exports io.clientcore.json;
    exports io.clientcore.json.implementation.jackson.core to com.generic.core;
    exports io.clientcore.json.implementation.jackson.core.io to com.generic.core;

    uses io.clientcore.json.JsonProvider;
}
