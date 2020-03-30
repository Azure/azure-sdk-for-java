/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.graphrbac.implementation;

import com.azure.core.annotation.Fluent;

/**
 * An interface representing a model that can attach a credential.
 */
@Fluent
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
