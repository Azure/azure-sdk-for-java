package com.azure.spring.identity;

public interface CredentialPropertiesProvider {

    String getTenantId();

    String getClientId();

    String getClientSecret();

    String getClientCertificatePath();

    String getUsername();

    String getPassword();

    String getAuthorityHost();

}
