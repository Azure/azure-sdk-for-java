/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.Credential;
import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 * Implementation for ServicePrincipal and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Fluent.Graph.RBAC")
class CredentialImpl<T>
        implements
        Credential,
        Credential.Definition<T>,
        Credential.UpdateDefinition<T> {

    PasswordCredentialInner passwordCredential;
    KeyCredentialInner keyCredential;

    CredentialImpl(PasswordCredentialInner passwordCredential) {
    }

    CredentialImpl(KeyCredentialInner keyCredential) {
    }

    CredentialImpl() {
    }

    @Override
    public String id() {
        return null;
    }

    @Override
    public String key() {
        return null;
    }

    @Override
    public DateTime startDate() {
        return null;
    }

    @Override
    public DateTime endDate() {
        return null;
    }

    @Override
    public String keyId() {
        return null;
    }

    @Override
    public String value() {
        return null;
    }


    @Override
    public T attach() {
        return null;
    }

    @Override
    public CredentialImpl<T> withPassword(String password) {
        return null;
    }

    @Override
    public CredentialImpl<T> withCertificate(String certificate) {
        return null;
    }

    @Override
    public CredentialImpl<T> withStartDate(DateTime startDate) {
        return null;
    }

    @Override
    public CredentialImpl<T> withDuration(Duration duration) {
        return null;
    }
}
