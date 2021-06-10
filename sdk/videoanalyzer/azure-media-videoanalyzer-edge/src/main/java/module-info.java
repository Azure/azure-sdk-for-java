// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.media.videoanalyzer.edge {
    requires transitive com.azure.core;

    opens com.azure.media.videoanalyzer.edge.models to com.fasterxml.jackson.databind, com.azure.core;
}
