/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.management.apigeneration.Fluent;

/**
 * An interface representing a model that can attach a credential.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
interface HasCredential<T extends HasCredential<T>> {
    /**
     * Attach a credential to this model.
     * @param credential the credential to attach to
     * @return the interface itself
     */
    T withCertificateCredential(CertificateCredentialImpl<?> credential);
    /**
     * Attach a credential to this model.
     * @param credential the credential to attach to
     * @return the interface itself
     */
    T withPasswordCredential(PasswordCredentialImpl<?> credential);
}
