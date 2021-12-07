module spring.cloud.azure.core {

    requires com.azure.core.amqp;
    requires org.slf4j;
    requires spring.core;
    requires com.azure.identity;
    requires com.azure.core.management;
    requires spring.beans;
    requires java.desktop;
    requires spring.context;
    requires static com.azure.storage.blob;
    requires static com.azure.storage.file.share;
}
