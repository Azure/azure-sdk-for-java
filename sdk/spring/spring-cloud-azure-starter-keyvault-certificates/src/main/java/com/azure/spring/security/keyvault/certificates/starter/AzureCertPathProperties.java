// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.security.keyvault.certificates.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * This is used to generate spring-configuration-metadata.json
 *
 * @see <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-configuration-metadata.html">Metadata</a>
 */
@EnableConfigurationProperties({ AzureCertPathProperties.class })
@ConfigurationProperties("azure.cert-path")
public class AzureCertPathProperties {

    /**
     * The path to put custom certificates
     */
    private String custom;

    /**
     * The path to put well-known certificates
     */
    private String wellKnown;

    /**
     * Gets the path to put custom certificates.
     *
     * @return the path to put custom certificates
     */
    public String getCustom() {
        return custom;
    }

    /**
     * Gets the path to put well-known certificates.
     *
     * @return the path to put well-known certificates
     */
    public String getWellKnown() {
        return wellKnown;
    }

    /**
     * Sets the path to put custom certificates.
     *
     * @param custom the path to put custom certificates
     */
    public void setCustom(String custom) {
        this.custom = custom;
    }

    /**
     * Sets the path to put well-known certificates.
     *
     * @param wellKnown the path to put well-known certificates
     */
    public void setWellKnown(String wellKnown) {
        this.wellKnown = wellKnown;
    }
}
