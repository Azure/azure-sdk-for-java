// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.DocumentClassifierDetailsHelper;
import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;
import java.util.Map;

/** Document classifier information model. */
@Fluent
public final class DocumentClassifierDetails {
    /*
     * Unique document classifier name.
     */
    private String classifierId;

    /*
     * Document classifier description.
     */
    private String description;

    /*
     * Date and time (UTC) when the document classifier was created.
     */
    private OffsetDateTime createdDateTime;

    /*
     * Date and time (UTC) when the document classifier will expire.
     */
    private OffsetDateTime expirationDateTime;

    /*
     * API version used to create this document classifier.
     */
    private String serviceVersion;

    /*
     * List of document types to classify against.
     */
    private Map<String, ClassifierDocumentTypeDetails> documentTypes;

    /** Creates an instance of DocumentClassifierDetails class. */
    public DocumentClassifierDetails() {}

    /**
     * Get the classifierId property: Unique document classifier name.
     *
     * @return the classifierId value.
     */
    public String getClassifierId() {
        return this.classifierId;
    }

    /**
     * Set the classifierId property: Unique document classifier name.
     *
     * @param classifierId the classifierId value to set.
     */
    void setClassifierId(String classifierId) {
        this.classifierId = classifierId;
    }

    /**
     * Get the description property: Document classifier description.
     *
     * @return the description value.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Set the description property: Document classifier description.
     *
     * @param description the description value to set.
     */
    void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the createdDateTime property: Date and time (UTC) when the document classifier was created.
     *
     * @return the createdDateTime value.
     */
    public OffsetDateTime getCreatedOn() {
        return this.createdDateTime;
    }

    /**
     * Set the createdDateTime property: Date and time (UTC) when the document classifier was created.
     *
     * @param createdDateTime the createdDateTime value to set.
     */
    void setCreatedDateTime(OffsetDateTime createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    /**
     * Get the expirationDateTime property: Date and time (UTC) when the document classifier will expire.
     *
     * @return the expirationDateTime value.
     */
    public OffsetDateTime getExpiresOn() {
        return this.expirationDateTime;
    }

    /**
     * Set the expirationDateTime property: Date and time (UTC) when the document classifier will expire.
     *
     * @param expirationDateTime the expirationDateTime value to set.
     */
    void setExpirationDateTime(OffsetDateTime expirationDateTime) {
        this.expirationDateTime = expirationDateTime;
    }

    /**
     * Get the Service version used to create this document classifier.
     *
     * @return the serviceVersion value.
     */
    public String getServiceVersion() {
        return this.serviceVersion;
    }

    /**
     * Set the service version used to create this document classifier.
     *
     * @param serviceVersion the service version value to set.
     */
    void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    /**
     * Get list of document types to classify against along with their details.
     *
     * @return the docTypes value.
     */
    public Map<String, ClassifierDocumentTypeDetails> getDocumentTypes() {
        return this.documentTypes;
    }

    /**
     * Set the docTypes property: List of document types to classify against.
     *
     * @param documentTypes the docTypes value to set.
     */
    void setDocumentTypes(Map<String, ClassifierDocumentTypeDetails> documentTypes) {
        this.documentTypes = documentTypes;
    }

    static {
        DocumentClassifierDetailsHelper.setAccessor(new DocumentClassifierDetailsHelper.DocumentClassifierDetailsAccessor() {
            @Override
            public void setClassifierId(DocumentClassifierDetails documentClassifierDetails, String modelId) {
                documentClassifierDetails.setClassifierId(modelId);
            }

            @Override
            public void setServiceVersion(DocumentClassifierDetails documentClassifierDetails, String description) {
                documentClassifierDetails.setServiceVersion(description);
            }

            @Override
            public void setDescription(DocumentClassifierDetails documentClassifierDetails, String description) {
                documentClassifierDetails.setDescription(description);
            }

            @Override
            public void setCreatedOn(DocumentClassifierDetails documentClassifierDetails, OffsetDateTime createdDateTime) {
                documentClassifierDetails.setCreatedDateTime(createdDateTime);
            }

            @Override
            public void setExpiresOn(DocumentClassifierDetails documentClassifierDetails, OffsetDateTime createdDateTime) {
                documentClassifierDetails.setExpirationDateTime(createdDateTime);
            }
            @Override
            public void setDocTypes(DocumentClassifierDetails documentClassifierDetails, Map<String, ClassifierDocumentTypeDetails> docTypes) {
                documentClassifierDetails.setDocumentTypes(docTypes);
            }
        });
    }
}
