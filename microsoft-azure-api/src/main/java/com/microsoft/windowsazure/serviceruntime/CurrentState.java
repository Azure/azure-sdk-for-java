/**
 * 
 */
package com.microsoft.windowsazure.serviceruntime;

/**
 * 
 */
class CurrentState {
    private final String clientId;

    public CurrentState(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }
}
