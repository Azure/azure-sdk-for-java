// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.models.ApplicationGatewaySslCertificateInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;
import java.io.File;
import java.io.IOException;

/** A client-side representation of an application gateway SSL certificate. */
@Fluent()
public interface ApplicationGatewaySslCertificate
    extends HasInnerModel<ApplicationGatewaySslCertificateInner>, ChildResource<ApplicationGateway> {

    /** @return the public data of the certificate */
    String publicData();

    /**
     * Get Secret Id of (base-64 encoded unencrypted pfx) 'Secret' or 'Certificate' object stored in KeyVault.
     *
     * @return the secret id
     */
    String keyVaultSecretId();

    /** Grouping of application gateway SSL certificate definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of an application gateway SSL certificate.
         *
         * @param <ParentT> the parent application gateway type
         */
        interface Blank<ParentT> extends WithData<ParentT> {
        }

        /**
         * The final stage of an application gateway SSL certificate definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the definition can be attached to the
         * parent application gateway definition.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching
         */
        interface WithAttach<ParentT> extends Attachable.InDefinition<ParentT> {
        }

        /**
         * The stage of an SSL certificate definition allowing to specify the contents of the SSL certificate.
         *
         * @param <ParentT> the stage of the parent application gateway to return to after attaching
         */
        interface WithData<ParentT> {
            /**
             * Specifies the contents of the private key in the PFX (PKCS#12) format, not base64-encoded.
             *
             * @param pfxData the contents of the private key in the PFX format
             * @return the next stage of the definition
             */
            WithPassword<ParentT> withPfxFromBytes(byte[] pfxData);

            /**
             * Specifies the PFX (PKCS#12) file to get the private key content from.
             *
             * @param pfxFile a file in the PFX format
             * @return the next stage of the definition
             * @throws java.io.IOException when there are problems with the provided file
             */
            WithPassword<ParentT> withPfxFromFile(File pfxFile) throws IOException;

            /**
             * Sepecifies the content of the private key using key vault.
             *
             * @param keyVaultSecretId the secret id of key vault
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withKeyVaultSecretId(String keyVaultSecretId);
        }

        /**
         * The stage of an SSL certificate definition allowing to specify the password for the private key (PFX) content
         * of the certificate.
         *
         * @param <ParentT> the stage of the parent application gateway to return to after attaching
         */
        interface WithPassword<ParentT> {
            /**
             * Specifies the password currently used to protect the provided PFX content of the SSL certificate.
             *
             * @param password a password
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withPfxPassword(String password);
        }
    }

    /**
     * The entirety of an application gateway SSL certificate definition.
     *
     * @param <ParentT> the stage of the parent application gateway definition to return to after attaching
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithAttach<ParentT>,
            DefinitionStages.WithData<ParentT>,
            DefinitionStages.WithPassword<ParentT> {
    }

    /** Grouping of application gateway SSL certificate update stages. */
    interface UpdateStages {
    }

    /** The entirety of an application gateway SSL certificate update as part of an application gateway update. */
    interface Update extends Settable<ApplicationGateway.Update> {
    }

    /**
     * Grouping of application gateway SSL certificate definition stages applicable as part of an application gateway
     * update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of an application gateway authentication certificate definition.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching
         */
        interface Blank<ParentT> extends WithData<ParentT> {
        }

        /**
         * The final stage of an application gateway SSL certificate definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the definition can be attached to the
         * parent application gateway definition.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching
         */
        interface WithAttach<ParentT> extends Attachable.InUpdate<ParentT> {
        }

        /**
         * The stage of an SSL certificate definition allowing to specify the contents of the SSL certificate.
         *
         * @param <ParentT> the stage of the parent application gateway to return to after attaching
         */
        interface WithData<ParentT> {
            /**
             * Specifies the contents of the private key in the PFX (PKCS#12) format, not base64-encoded.
             *
             * @param pfxData the contents of the private key in the PFX format
             * @return the next stage of the definition
             */
            WithPassword<ParentT> withPfxFromBytes(byte[] pfxData);

            /**
             * Specifies the PFX (PKCS#12) file to get the private key content from.
             *
             * @param pfxFile a file in the PFX format
             * @return the next stage of the definition
             * @throws java.io.IOException when there are problems with the provided file
             */
            WithPassword<ParentT> withPfxFromFile(File pfxFile) throws IOException;

            /**
             * Sepecifies the content of the private key using key vault.
             *
             * @param keyVaultSecretId the secret id of key vault
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withKeyVaultSecretId(String keyVaultSecretId);
        }

        /**
         * The stage of an SSL certificate definition allowing to specify the password for the private key (PFX) content
         * of the certificate.
         *
         * @param <ParentT> the stage of the parent application gateway to return to after attaching
         */
        interface WithPassword<ParentT> {
            /**
             * Specifies the password currently used to protect the provided PFX content of the SSL certificate.
             *
             * @param password a password
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withPfxPassword(String password);
        }
    }

    /**
     * The entirety of an application gateway SSL certificate definition as part of an application gateway update.
     *
     * @param <ParentT> the stage of the parent application gateway definition to return to after attaching
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithData<ParentT>,
            UpdateDefinitionStages.WithPassword<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }
}
