module azure.security.keyvault.jca {
    requires java.logging;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires com.fasterxml.jackson.databind;

    exports com.azure.security.keyvault.jca;
    exports com.azure.security.keyvault.jca.model;
}
