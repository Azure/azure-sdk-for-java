// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager.redis {
    requires transitive com.azure.resourcemanager.resources;

    // export public APIs of redis
    exports com.azure.resourcemanager.redis;
    exports com.azure.resourcemanager.redis.fluent;
    exports com.azure.resourcemanager.redis.fluent.models;
    exports com.azure.resourcemanager.redis.models;

    // open packages specifically for azure core and jackson
    opens com.azure.resourcemanager.redis.fluent.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.redis.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
