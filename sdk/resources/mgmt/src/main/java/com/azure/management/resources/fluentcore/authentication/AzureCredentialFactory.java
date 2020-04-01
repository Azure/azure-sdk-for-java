/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.fluentcore.authentication;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.AuthorizationCodeCredentialBuilder;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.azure.identity.DeviceCodeInfo;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.identity.InteractiveBrowserCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.identity.UsernamePasswordCredentialBuilder;

import java.io.File;
import java.util.function.Consumer;

public class AzureCredentialFactory {

    private AzureCredentialFactory() {
    }

    public static TokenCredential fromUser(String username, String password, AzureEnvironment environment) {
        return new UsernamePasswordCredentialBuilder()
            .username(username)
            .password(password)
            .authorityHost(environment.getActiveDirectoryEndpoint())
            .build();
    }

    public static TokenCredential fromServicePrincipal(String tenantId, String clientId, String clientSecret, AzureEnvironment environment) {
        return new ClientSecretCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .authorityHost(environment.getActiveDirectoryEndpoint())
            .build();
    }

    public static TokenCredential fromServicePrincipalWithPemCertificate(String tenantId, String clientId, String pemCertificatePath, AzureEnvironment environment) {
        return new ClientCertificateCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .pemCertificate(pemCertificatePath)
            .authorityHost(environment.getActiveDirectoryEndpoint())
            .build();
    }

    public static TokenCredential fromServicePrincipalWithPfxCertificate(String tenantId, String clientId, String pfxCertificatePath, String pfxCertificatePassword, AzureEnvironment environment) {
        return new ClientCertificateCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .pfxCertificate(pfxCertificatePath, pfxCertificatePassword)
            .authorityHost(environment.getActiveDirectoryEndpoint())
            .build();
    }

    public static TokenCredential fromDevice(String clientId, Consumer<DeviceCodeInfo> deviceCodeInfoConsumer, AzureEnvironment environment) {
        return new DeviceCodeCredentialBuilder()
            .clientId(clientId)
            .challengeConsumer(deviceCodeInfoConsumer)
            .authorityHost(environment.getActiveDirectoryEndpoint())
            .build();
    }

    public static TokenCredential fromManagedIdentity(String clientId) {
        return new ManagedIdentityCredentialBuilder()
            .clientId(clientId)
            .build();
    }

    public static TokenCredential fromEnvironment(AzureEnvironment environment) {
        return new EnvironmentCredentialBuilder()
            .authorityHost(environment.getActiveDirectoryEndpoint())
            .build();
    }

    public static TokenCredential fromAuthorizationCode(String tenantId, String clientId, String authorizationCode, String redirectUrl, AzureEnvironment environment) {
        return new AuthorizationCodeCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .authorizationCode(authorizationCode)
            .redirectUrl(redirectUrl)
            .authorityHost(authorizationCode)
            .build();
    }

    public static TokenCredential fromInteractiveBrowser(String tenantId, String clientId, int port, AzureEnvironment environment) {
        return new InteractiveBrowserCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .port(port)
            .authorityHost(environment.getActiveDirectoryEndpoint())
            .build();
    }

    public static TokenCredential fromFile(File credentialFile) {
        //TODO migrate from ApplicationTokenCredential
        return null;
    }
}
