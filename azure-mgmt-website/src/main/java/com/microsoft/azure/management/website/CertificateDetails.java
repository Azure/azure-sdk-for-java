/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.website.implementation.CertificateDetailsInner;
import org.joda.time.DateTime;

/**
 * An immutable client-side representation of an Azure Web App.
 */
public interface CertificateDetails extends
        Wrapper<CertificateDetailsInner> {
    /**
     * @return Version.
     */
    Integer version();

    /**
     * @return Serial Number.
     */
    String serialNumber();

    /**
     * @return Thumbprint.
     */
    String thumbprint();

    /**
     * @return Subject.
     */
    String subject();

    /**
     * @return Valid from.
     */
    DateTime notBefore();

    /**
     * @return Valid to.
     */
    DateTime notAfter();

    /**
     * @return Signature Algorithm.
     */
    String signatureAlgorithm();

    /**
     * @return Issuer.
     */
    String issuer();

    /**
     * @return Raw certificate data.
     */
    String rawData();
}

