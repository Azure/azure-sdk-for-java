// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

function handler(event) {
    event.response.headers["X-Java-Sdk-Test"] = "edgeactions";
    return event;
}
