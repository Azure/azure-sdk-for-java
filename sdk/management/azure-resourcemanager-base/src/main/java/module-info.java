module azure.resourcemanager.base {
    requires transitive com.azure.core.management;

    exports com.azure.resourcemanager.base.utils;
    exports com.azure.resourcemanager.base.profile;
}
