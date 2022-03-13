// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.util.AnalyzedDocumentHelper;

import java.util.List;
import java.util.Map;

/**
 * An object describing the location and semantic content of a document.
 */
public final class AnalyzedDocument {
    /*
     * AnalyzeDocument type.
     */
    private String docType;

    /*
     * Bounding regions covering the document.
     */
    private List<BoundingRegion> boundingRegions;

    /*
     * Location of the document in the reading order concatenated content.
     */
    private List<DocumentSpan> spans;

    /*
     * Dictionary of named field values.
     */
    private Map<String, DocumentField> fields;

    /*
     * Confidence of correctly extracting the document.
     */
    private float confidence;

    /**
     * Get the docType property: AnalyzeDocument type.
     *
     * @return the docType value.
     */
    public String getDocType() {
        return this.docType;
    }

    /**
     * Set the docType property: AnalyzeDocument type.
     *
     * @param docType the docType value to set.
     * @return the AnalyzeDocument object itself.
     */
    void setDocType(String docType) {
        this.docType = docType;
    }

    /**
     * Get the boundingRegions property: Bounding regions covering the document.
     *
     * @return the boundingRegions value.
     */
    public List<BoundingRegion> getBoundingRegions() {
        return this.boundingRegions;
    }

    /**
     * Set the boundingRegions property: Bounding regions covering the document.
     *
     * @param boundingRegions the boundingRegions value to set.
     * @return the AnalyzeDocument object itself.
     */
    void setBoundingRegions(List<BoundingRegion> boundingRegions) {
        this.boundingRegions = boundingRegions;
    }

    /**
     * Get the spans property: Location of the document in the reading order concatenated content.
     *
     * @return the spans value.
     */
    public List<DocumentSpan> getSpans() {
        return this.spans;
    }

    /**
     * Set the spans property: Location of the document in the reading order concatenated content.
     *
     * @param spans the spans value to set.
     * @return the AnalyzeDocument object itself.
     */
    void setSpans(List<DocumentSpan> spans) {
        this.spans = spans;
    }

    /**
     * Get the fields property: Dictionary of named field values.
     *
     * @return the fields value.
     */
    public Map<String, DocumentField> getFields() {
        return this.fields;
    }

    /**
     * Set the fields property: Dictionary of named field values.
     *
     * @param fields the fields value to set.
     * @return the AnalyzeDocument object itself.
     */
    void setFields(Map<String, DocumentField> fields) {
        this.fields = fields;
    }

    /**
     * Get the confidence property: Confidence of correctly extracting the document.
     *
     * @return the confidence value.
     */
    public float getConfidence() {
        return this.confidence;
    }

    /**
     * Set the confidence property: Confidence of correctly extracting the document.
     *
     * @param confidence the confidence value to set.
     * @return the AnalyzeDocument object itself.
     */
    void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    static {
        AnalyzedDocumentHelper.setAccessor(new AnalyzedDocumentHelper.AnalyzedDocumentAccessor() {
            @Override
            public void setDocType(AnalyzedDocument analyzedDocument, String docType) {
                analyzedDocument.setDocType(docType);
            }

            @Override
            public void setBoundingRegions(AnalyzedDocument analyzedDocument, List<BoundingRegion> boundingRegions) {
                analyzedDocument.setBoundingRegions(boundingRegions);
            }

            @Override
            public void setSpans(AnalyzedDocument analyzedDocument, List<DocumentSpan> spans) {
                analyzedDocument.setSpans(spans);
            }

            @Override
            public void setFields(AnalyzedDocument analyzedDocument, Map<String, DocumentField> fields) {
                analyzedDocument.setFields(fields);
            }

            @Override
            public void setConfidence(AnalyzedDocument analyzedDocument, float confidence) {
                analyzedDocument.setConfidence(confidence);
            }
        });
    }
}
