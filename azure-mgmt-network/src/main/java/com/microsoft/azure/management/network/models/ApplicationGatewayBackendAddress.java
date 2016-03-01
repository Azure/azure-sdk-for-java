/**
 * Object]
 */

package com.microsoft.azure.management.network.models;


/**
 * Backend Address of application gateway.
 */
public class ApplicationGatewayBackendAddress {
    /**
     * Gets or sets the dns name.
     */
    private String fqdn;

    /**
     * Gets or sets the ip address.
     */
    private String ipAddress;

    /**
     * Get the fqdn value.
     *
     * @return the fqdn value
     */
    public String getFqdn() {
        return this.fqdn;
    }

    /**
     * Set the fqdn value.
     *
     * @param fqdn the fqdn value to set
     */
    public void setFqdn(String fqdn) {
        this.fqdn = fqdn;
    }

    /**
     * Get the ipAddress value.
     *
     * @return the ipAddress value
     */
    public String getIpAddress() {
        return this.ipAddress;
    }

    /**
     * Set the ipAddress value.
     *
     * @param ipAddress the ipAddress value to set
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

}
