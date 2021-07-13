// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.identity;

/**
 * An interface to provide all credential related properties.
 */
public interface CredentialPropertiesProvider {

    String getTenantId();

    String getClientId();

    String getClientSecret();

    String getClientCertificatePath();

    String getUsername();

    String getPassword();

    String getAuthorityHost();

}
