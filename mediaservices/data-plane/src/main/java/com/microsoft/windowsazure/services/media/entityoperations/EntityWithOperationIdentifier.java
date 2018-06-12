package com.microsoft.windowsazure.services.media.entityoperations;

public interface EntityWithOperationIdentifier {
    
    String getOperationId();
    
    void setOperationId(String string);

    boolean hasOperationIdentifier();

}
