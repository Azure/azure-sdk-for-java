// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.DocumentAnnotationHelper;

import java.util.List;

/** An annotation object that represents a visual annotation in the document, such as checks ✓ and crosses X. */
public final class DocumentAnnotation {
    /*
     * Annotation kind.
     */
    private DocumentAnnotationKind kind;

    /*
     * Bounding polygon of the annotation.
     */
    private List<Point> polygon;

    /*
     * Confidence of correctly extracting the annotation.
     */
    private float confidence;

    /**
     * Get the kind property: Annotation kind.
     *
     * @return the kind value.
     */
    public DocumentAnnotationKind getKind() {
        return this.kind;
    }

    /**
     * Get the polygon property: Bounding polygon of the annotation.
     *
     * @return the polygon value.
     */
    public List<Point> getBoundingPolygon() {
        return this.polygon;
    }

    /**
     * Get the confidence property: Confidence of correctly extracting the annotation.
     *
     * @return the confidence value.
     */
    public float getConfidence() {
        return this.confidence;
    }

    void setKind(DocumentAnnotationKind kind) {
        this.kind = kind;
    }

    void setPolygon(List<Point> polygon) {
        this.polygon = polygon;
    }

    void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    static {
        DocumentAnnotationHelper.setAccessor(new DocumentAnnotationHelper.DocumentAnnotationAccessor() {
            @Override
            public void setPolygon(DocumentAnnotation documentAnnotation, List<Point> points) {
                documentAnnotation.setPolygon(points);
            }

            @Override
            public void setKind(DocumentAnnotation documentAnnotation, DocumentAnnotationKind kind) {
                documentAnnotation.setKind(kind);
            }

            @Override
            public void setConfidence(DocumentAnnotation documentAnnotation, float confidence) {
                documentAnnotation.setConfidence(confidence);
            }
        });
    }
}
