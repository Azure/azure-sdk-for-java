// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AzureBlobContentSource;
import com.azure.ai.documentintelligence.models.AzureBlobFileListContentSource;
import com.azure.ai.documentintelligence.models.ClassifierDocumentTypeDetails;
import com.azure.core.test.TestProxyTestBase;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;

import static com.azure.ai.documentintelligence.TestUtils.BATCH_TRAINING_DATA_CONTAINER_NAME;
import static com.azure.ai.documentintelligence.TestUtils.BATCH_TRAINING_DATA_RESULT_CONTAINER_NAME;
import static com.azure.ai.documentintelligence.TestUtils.CLASSIFIER_TRAINING_DATA_CONTAINER_NAME;
import static com.azure.ai.documentintelligence.TestUtils.MULTIPAGE_TRAINING_DATA_CONTAINER_NAME;
import static com.azure.ai.documentintelligence.TestUtils.SELECTION_MARK_DATA_CONTAINER_NAME;
import static com.azure.ai.documentintelligence.TestUtils.STORAGE_ACCOUNT_NAME;
import static com.azure.ai.documentintelligence.TestUtils.TRAINING_DATA_CONTAINER_NAME;
import static com.azure.ai.documentintelligence.TestUtils.getTestTokenCredential;
import static com.azure.storage.common.sas.SasProtocol.HTTPS_ONLY;

public abstract class DocumentIntelligenceTestBase extends TestProxyTestBase {
    /**
     * Get the training data set SAS Url value based on the test running mode.
     *
     * @return the training data set Url
     */
    public String getTrainingFilesContainerUrl() {
        return getContainerSasUrl(TRAINING_DATA_CONTAINER_NAME);
    }

    /**
     * Get the multipage training data set SAS Url value based on the test running mode.
     *
     * @return the multipage training data set Url
     */
    public String getMultipageTrainingSasUri() {
        return getContainerSasUrl(MULTIPAGE_TRAINING_DATA_CONTAINER_NAME);
    }

    /**
     * Get the selection marks training data set SAS Url value based on the test running mode.
     *
     * @return the selection marks training data set Url
     */
    public String getSelectionMarkTrainingSasUri() {
        return getContainerSasUrl(SELECTION_MARK_DATA_CONTAINER_NAME);
    }

    /**
     * Get the training data set SAS Url value based on the test running mode.
     *
     * @return the training data set Url for classifiers
     */
    public String getClassifierTrainingFilesContainerUrl() {
        return getContainerSasUrl(CLASSIFIER_TRAINING_DATA_CONTAINER_NAME);
    }

    /**
     * Get the training data set SAS Url value based on the test running mode.
     *
     * @return the training data set Url
     */
    public String getBatchTrainingFilesResultContainerUrl() {
        return getContainerSasUrl(BATCH_TRAINING_DATA_RESULT_CONTAINER_NAME);
    }

    /**
     * Get the training data set SAS Url value based on the test running mode.
     *
     * @return the training data set Url
     */
    public String getBatchTrainingFilesContainerUrl() {
        return getContainerSasUrl(BATCH_TRAINING_DATA_CONTAINER_NAME);
    }

    /**
     * Get a blob container SAS URL.
     */
    private String getContainerSasUrl(String containerName) {
        if (interceptorManager.isPlaybackMode()) {
            return "https://isPlaybackmode";
        }

        BlobServiceClient blobServiceClient
            = new BlobServiceClientBuilder().endpoint("https://" + STORAGE_ACCOUNT_NAME + ".blob.core.windows.net/")
                .credential(getTestTokenCredential(testContextManager.getTestMode()))
                .buildClient();

        UserDelegationKey userDelegationKey = blobServiceClient.getUserDelegationKey(
            java.time.OffsetDateTime.now().minusMinutes(5), java.time.OffsetDateTime.now().plusHours(1));
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(
            java.time.OffsetDateTime.now().plusHours(3),
            new BlobContainerSasPermission().setReadPermission(true).setWritePermission(true).setListPermission(true))
                .setStartTime(java.time.OffsetDateTime.now().minusMinutes(5))
                .setProtocol(HTTPS_ONLY);

        String sas = blobServiceClient.getBlobContainerClient(containerName)
            .generateUserDelegationSas(sasValues, userDelegationKey);
        return "https://" + STORAGE_ACCOUNT_NAME + ".blob.core.windows.net/" + containerName + "?" + sas;
    }

    static ClassifierDocumentTypeDetails createBlobContentSource(String containerUrl, String prefix) {
        return new ClassifierDocumentTypeDetails()
            .setAzureBlobSource(new AzureBlobContentSource(containerUrl).setPrefix(prefix));
    }

    static ClassifierDocumentTypeDetails createBlobFileListContentSource(String containerUrl, String fileList) {
        return new ClassifierDocumentTypeDetails()
            .setAzureBlobFileListSource(new AzureBlobFileListContentSource(containerUrl, fileList));
    }
}
