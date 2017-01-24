/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import java.io.File;
import java.io.IOException;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.ApplicationGatewaySslCertificateInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of an application gateway SSL certificate.
 */
@Fluent()
public interface ApplicationGatewaySslCertificate extends
    Wrapper<ApplicationGatewaySslCertificateInner>,
    ChildResource<ApplicationGateway> {

    /**
     * @return the public data of the certificate
     */
    String publicData();

    /**
     * Grouping of application gateway SSL certificate definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of an application gateway SSL certificate.
         * @param <ParentT> the parent application gateway type
         */
        interface Blank<ParentT> extends WithData<ParentT> {
        }

        /** The final stage of an application gateway SSL certificate definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the definition
         * can be attached to the parent application gateway definition.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinition<ParentT> {
        }

        /**
         * The stage of an SSL certificate definition allowing to specify the contents of the SSL certificate.
         * @param <ParentT> the stage of the parent application gateway to return to after attaching
         */
        interface WithData<ParentT> {
            /**
             * Specifies the contents of the private key in the PFX (PKCS#12) format, not base64-encoded.
             * @param pfxData the contents of the private key in the PFX format
             * @return the next stage of the definition
             */
            WithPassword<ParentT> withPfxFromBytes(byte[] pfxData);

            /**
             * Specifies the PFX (PKCS#12) file to get the private key content from.
             * @param pfxFile a file in the PFX format
             * @return the next stage of the definition
             * @throws java.io.IOException when there are problems with the provided file
             */
            WithPassword<ParentT> withPfxFromFile(File pfxFile) throws IOException;
        }

        /**
         * The stage of an SSL certificate definition allowing to specify the password for the private key (PFX) content of the certificate.
         * @param <ParentT> the stage of the parent application gateway to return to after attaching
         */
        interface WithPassword<ParentT> {
            /**
             * Specifies the password currently used to protect the provided PFX content of the SSL certificate.
             * @param password a password
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withPfxPassword(String password);
        }
    }

    /** The entirety of an application gateway SSL certificate definition.
     * @param <ParentT> the stage of the parent application gateway definition to return to after attaching
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAttach<ParentT>,
        DefinitionStages.WithData<ParentT>,
        DefinitionStages.WithPassword<ParentT> {
    }

    /**
     * Grouping of application gateway SSL certificate update stages.
     */
    interface UpdateStages {
    }

    /**
     * The entirety of an application gateway SSL certificate update as part of an application gateway update.
     */
    interface Update extends
        Settable<ApplicationGateway.Update> {
    }

    /**
     * Grouping of application gateway SSL certificate definition stages applicable as part of an application gateway update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of an application gateway authentication certificate definition.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching
         */
        interface Blank<ParentT> extends WithAttach<ParentT> {
        }

        /** The final stage of an application gateway SSL certificate definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the definition
         * can be attached to the parent application gateway definition.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdate<ParentT> {
        }
    }

    /** The entirety of an application gateway SSL certificate definition as part of an application gateway update.
     * @param <ParentT> the stage of the parent application gateway definition to return to after attaching
     */
    interface UpdateDefinition<ParentT> extends
        UpdateDefinitionStages.Blank<ParentT>,
        UpdateDefinitionStages.WithAttach<ParentT> {
    }
}
