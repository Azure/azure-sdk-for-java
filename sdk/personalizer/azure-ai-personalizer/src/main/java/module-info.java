// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.ai.personalizer {
    requires transitive com.azure.core;
    requires com.azure.json;

    exports com.azure.ai.personalizer;
    exports com.azure.ai.personalizer.models;
    exports com.azure.ai.personalizer.administration.models;

    opens com.azure.ai.personalizer to com.azure.core;
    opens com.azure.ai.personalizer.administration.models to com.azure.core;
    opens com.azure.ai.personalizer.models to com.azure.core;
}
