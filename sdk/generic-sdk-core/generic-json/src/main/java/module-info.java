// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.generic.json  {
    exports com.generic.json;
    exports com.generic.json.implementation to com.generic.core;
    exports com.generic.json.implementation.jackson.core to com.generic.core;

    uses com.generic.json.JsonProvider;
}
