module com.azure.spring.resourcemanager {

    requires transitive com.azure.spring.core;
    requires com.azure.resourcemanager;
    requires spring.core;

    exports com.azure.spring.resourcemanager.connectionstring;
    exports com.azure.spring.resourcemanager.provisioner.eventhubs;
    exports com.azure.spring.resourcemanager.provisioner.servicebus;
}
