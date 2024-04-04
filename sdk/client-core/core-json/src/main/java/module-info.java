// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module io.clientcore.core.json  {
    exports io.clientcore.core.json;
    exports io.clientcore.core.json.implementation.jackson.core to io.clientcore.core;
    exports io.clientcore.core.json.implementation.jackson.core.io to io.clientcore.core;

    uses io.clientcore.core.json.JsonProvider;
}
