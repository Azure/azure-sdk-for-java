/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.google.common.io.BaseEncoding;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.CertificateType;
import com.microsoft.azure.management.graphrbac.Credential;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableRefreshableImpl;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import rx.Observable;

/**
 * Implementation for ServicePrincipal and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Fluent.Graph.RBAC")
class CredentialImpl<T>
        extends IndexableRefreshableImpl<Credential>
        implements
        Credential,
        Credential.Definition<T>,
        Credential.UpdateDefinition<T> {

    private String name;
    private PasswordCredentialInner passwordCredential;
    private KeyCredentialInner keyCredential;
    private HasCredential<?> parent;

    CredentialImpl(PasswordCredentialInner passwordCredential) {
        this.passwordCredential = passwordCredential;
        this.name = new String(BaseEncoding.base64().decode(passwordCredential.customKeyIdentifier()));
    }

    CredentialImpl(KeyCredentialInner keyCredential) {
        this.keyCredential = keyCredential;
        this.name = new String(BaseEncoding.base64().decode(keyCredential.customKeyIdentifier()));
    }

    CredentialImpl(String name, HasCredential<?> parent) {
        this.name = name;
        this.parent = parent;
    }

    @Override
    public Credential refresh() {
        throw new UnsupportedOperationException("Cannot refresh credentials.");
    }

    @Override
    public Observable<Credential> refreshAsync() {
        throw new UnsupportedOperationException("Cannot refresh credentials.");
    }

    @Override
    public String id() {
        if (passwordCredential != null) {
            return passwordCredential.keyId();
        } else if (keyCredential != null) {
            return keyCredential.keyId();
        }
        return null;
    }

    @Override
    public DateTime startDate() {
        if (passwordCredential != null) {
            return passwordCredential.startDate();
        } else if (keyCredential != null) {
            return keyCredential.startDate();
        }
        return null;
    }

    @Override
    public DateTime endDate() {
        if (passwordCredential != null) {
            return passwordCredential.endDate();
        } else if (keyCredential != null) {
            return keyCredential.endDate();
        }
        return null;
    }

    @Override
    public String value() {
        if (passwordCredential != null) {
            return passwordCredential.value();
        } else if (keyCredential != null) {
            return keyCredential.value();
        }
        return null;
    }


    @Override
    @SuppressWarnings("unchecked")
    public T attach() {
        parent.withCredential(this);
        return (T) parent;
    }

    @Override
    public CredentialImpl<T> withPassword(String password) {
        this.passwordCredential = new PasswordCredentialInner()
                .withValue(password)
                .withCustomKeyIdentifier(BaseEncoding.base64().encode(name.getBytes()))
                .withStartDate(DateTime.now())
                .withEndDate(DateTime.now().plusYears(1));
        return this;
    }

    @Override
    public CredentialImpl<T> withCertificate(String certificate) {
        this.keyCredential = new KeyCredentialInner()
                .withValue(certificate)
                .withUsage("Verify")
                .withCustomKeyIdentifier(BaseEncoding.base64().encode(name.getBytes()))
                .withStartDate(DateTime.now())
                .withEndDate(DateTime.now().plusYears(1));
        return this;
    }

    @Override
    public CredentialImpl<T> withStartDate(DateTime startDate) {
        DateTime original = startDate();
        if (passwordCredential != null) {
            passwordCredential.withStartDate(startDate);
        } else if (keyCredential != null) {
            keyCredential.withStartDate(startDate);
        }
        // Adjust end time
        withDuration(Duration.millis(endDate().getMillis() - original.getMillis()));
        return this;
    }

    @Override
    public CredentialImpl<T> withDuration(Duration duration) {
        if (passwordCredential != null) {
            passwordCredential.withEndDate(startDate().plusDays((int) duration.getStandardDays()));
        } else if (keyCredential != null) {
            keyCredential.withEndDate(startDate().plusDays((int) duration.getStandardDays()));
        }
        return this;
    }

    @Override
    public CredentialImpl<T> withType(CertificateType type) {
        keyCredential.withType(type.toString());
        return this;
    }

    @Override
    public CredentialImpl<T> withType(String type) {
        keyCredential.withType(type);
        return this;
    }

    PasswordCredentialInner passwordCredential() {
        return passwordCredential;
    }

    KeyCredentialInner certificateCredential() {
        return keyCredential;
    }
}
