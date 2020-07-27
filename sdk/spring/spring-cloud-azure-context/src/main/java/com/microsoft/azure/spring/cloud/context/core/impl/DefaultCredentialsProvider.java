/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.impl;

import com.microsoft.azure.credentials.AppServiceMSICredentials;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.credentials.MSICredentials;
import com.microsoft.azure.spring.cloud.context.core.api.CredentialsProvider;
import com.microsoft.azure.spring.cloud.context.core.config.AzureManagedIdentityProperties;
import com.microsoft.azure.spring.cloud.context.core.config.AzureProperties;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A {@link CredentialsProvider} implementation that provides credentials based on
 * user-provided properties and defaults.
 *
 * @author Warren Zhu
 */
public class DefaultCredentialsProvider implements CredentialsProvider {
    private static final Logger log = LoggerFactory.getLogger(DefaultCredentialsProvider.class);

    private static final String TEMP_CREDENTIAL_FILE_PREFIX = "azure";
    private static final String TEMP_CREDENTIAL_FILE_SUFFIX = "credential";
    private static final String ENV_MSI_ENDPOINT = "MSI_ENDPOINT";
    private static final String ENV_MSI_SECRET = "MSI_SECRET";

    private final AzureTokenCredentials credentials;

    public DefaultCredentialsProvider(AzureProperties azureProperties) {
        this.credentials = initCredentials(azureProperties);
    }

    private File createTempCredentialFile(@NonNull InputStream inputStream) throws IOException {
        File tempCredentialFile = File.createTempFile(TEMP_CREDENTIAL_FILE_PREFIX, TEMP_CREDENTIAL_FILE_SUFFIX);

        tempCredentialFile.deleteOnExit();
        FileUtils.copyInputStreamToFile(inputStream, tempCredentialFile);

        return tempCredentialFile;
    }

    private AzureTokenCredentials initCredentials(AzureProperties azureProperties) {
        if (azureProperties.isMsiEnabled()) {
            AzureTokenCredentials credentials = getMSIToken(azureProperties);
            credentials.withDefaultSubscriptionId(azureProperties.getSubscriptionId());
            return credentials;
        }

        try {
            DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
            InputStream inputStream =
                    resourceLoader.getResource(azureProperties.getCredentialFilePath()).getInputStream();
            File credentialFile = this.createTempCredentialFile(inputStream);

            return ApplicationTokenCredentials.fromFile(credentialFile);
        } catch (IOException e) {
            log.error("Credential file path not found.", e);
            throw new IllegalArgumentException("Credential file path not found", e);
        }
    }

    private boolean isAppService() {
        return StringUtils.hasText(System.getenv(ENV_MSI_ENDPOINT)) &&
                StringUtils.hasText(System.getenv(ENV_MSI_SECRET));
    }

    private AzureTokenCredentials getMSIToken(AzureProperties azureProperties) {
        AzureManagedIdentityProperties msiProps = azureProperties.getManagedIdentity();

        if (isAppService()) {
            AppServiceMSICredentials credentials = new AppServiceMSICredentials(azureProperties.getEnvironment());

            if (msiProps != null && StringUtils.hasText(msiProps.getClientId())) {
                credentials.withClientId(msiProps.getClientId());
            }

            return credentials;
        }

        MSICredentials msiCredentials = new MSICredentials();

        if (msiProps != null) {
            if (StringUtils.hasText(msiProps.getClientId())) {
                msiCredentials.withClientId(msiProps.getClientId());
            }

            if (StringUtils.hasText(msiProps.getObjectId())) {
                msiCredentials.withObjectId(msiProps.getObjectId());
            }
        }

        return msiCredentials;
    }

    @Override
    public AzureTokenCredentials getCredentials() {
        return this.credentials;
    }
}
