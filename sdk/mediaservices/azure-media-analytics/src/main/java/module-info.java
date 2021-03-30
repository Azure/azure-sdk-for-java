// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.media.analytics {
    requires transitive com.azure.core;
    requires azure.autorest.customization;

    exports com.azure.media.analytics;

    opens com.azure.media.analytics.models to com.fasterxml.jackson.databind, com.azure.core;
}
