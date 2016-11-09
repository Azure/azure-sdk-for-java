/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.website;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.io.File;

/**
 * A Host name - SSL binding definition.
 */
@Fluent
public interface HostNameSslBinding extends
    Wrapper<HostNameSslState>,
    ChildResource<WebAppBase<?>> {
    /**
     * @return the SSL type
     */
    SslState sslState();

    /**
     * @return the virtual IP address assigned to the host name if IP based SSL is enabled
     */
    String virtualIP();

    /**
     * @return the SSL cert thumbprint.
     */
    String thumbprint();

    /**
     * The entirety of a domain contact definition.
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT> extends
            DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithSslType<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of security rule definition stages applicable as part of a network security group creation.
     */
    interface DefinitionStages {
        /**
         * The first stage of a security rule definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithCertificate<ParentT> {
        }

        interface WithCertificate<ParentT> {
            WithSslType<ParentT> withPfxCertificateToUpload(File pfxFile, String password);
//            WithSslType<ParentT> withNewAppServiceCertificateOrder(CertificateProductType productType, int validYears);
//            WithSslType<ParentT> withExistingAppServiceCertificate(AppServiceCertificate appServiceCertificate);
        }

        interface WithSslType<ParentT> {
            WithAttach<ParentT> withSniSSL();
            WithAttach<ParentT> withIpBasedSSL();
        }

        /** The final stage of the security rule definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the security rule definition
         * can be attached to the parent network security group definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
                Attachable.InDefinition<ParentT> {
        }
    }

    interface UpdateDefinition<ParentT> extends
            UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithSslType<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of security rule definition stages applicable as part of a network security group creation.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a security rule definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithCertificate<ParentT> {
        }

        interface WithCertificate<ParentT> {
            WithCertificate<ParentT> withPfxCertificateToUpload(File pfxFile, String password);
        }

        interface WithSslType<ParentT> {
            WithAttach<ParentT> withSniSSL();
            WithAttach<ParentT> withIpBasedSSL();
        }

        /** The final stage of the security rule definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the security rule definition
         * can be attached to the parent network security group definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
                Attachable.InUpdate<ParentT> {
        }
    }
 }
