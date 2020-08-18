module com.azure.resourcemanager.redis {
    requires transitive com.azure.resourcemanager.resources;

    exports com.azure.resourcemanager.redis;
    exports com.azure.resourcemanager.redis.fluent;
    exports com.azure.resourcemanager.redis.fluent.inner;
    exports com.azure.resourcemanager.redis.models;

    opens com.azure.resourcemanager.redis.fluent.inner to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.resourcemanager.redis.models to com.fasterxml.jackson.databind, com.azure.core;
}
