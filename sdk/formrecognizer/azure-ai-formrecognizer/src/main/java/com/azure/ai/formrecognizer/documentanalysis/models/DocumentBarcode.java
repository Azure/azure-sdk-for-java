// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.DocumentBarcodeHelper;

import java.util.List;

/** Model representing a barcode document field. */
public final class DocumentBarcode {
    /*
     * Barcode kind.
     */
    private DocumentBarcodeKind kind;

    /*
     * Barcode value
     */
    private String value;

    /*
     * Bounding polygon of the barcode.
     */
    private List<Point> polygon;

    /*
     * Location of the barcode in the reading order concatenated content.
     */
    private DocumentSpan span;

    /*
     * Confidence of correctly extracting the barcode.
     */
    private float confidence;

    /**
     * Get the kind property: Barcode kind.
     *
     * @return the kind value.
     */
    public DocumentBarcodeKind getKind() {
        return this.kind;
    }

    /**
     * Get the value property: Barcode value.
     *
     * @return the value value.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Get the polygon property: Bounding polygon of the barcode.
     *
     * @return the polygon value.
     */
    public List<Point> getBoundingPolygon() {
        return this.polygon;
    }

    /**
     * Set the polygon property: Bounding polygon of the barcode.
     *
     * @param polygon the polygon value to set.
     * @return the DocumentBarcode object itself.
     */
    void setPolygon(List<Point> polygon) {
        this.polygon = polygon;
    }

    /**
     * Get the span property: Location of the barcode in the reading order concatenated content.
     *
     * @return the span value.
     */
    public DocumentSpan getSpan() {
        return this.span;
    }

    /**
     * Get the confidence property: Confidence of correctly extracting the barcode.
     *
     * @return the confidence value.
     */
    public float getConfidence() {
        return this.confidence;
    }

    void setKind(DocumentBarcodeKind kind) {
        this.kind = kind;
    }

    void setValue(String value) {
        this.value = value;
    }

    void setSpan(DocumentSpan span) {
        this.span = span;
    }

    void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    static {
        DocumentBarcodeHelper.setAccessor(new DocumentBarcodeHelper.DocumentBarcodeAccessor() {
            @Override
            public void setSpan(DocumentBarcode documentBarcode, DocumentSpan span) {
                documentBarcode.setSpan(span);
            }

            @Override
            public void setKind(DocumentBarcode documentBarcode, DocumentBarcodeKind kind) {
                documentBarcode.setKind(kind);
            }

            @Override
            public void setConfidence(DocumentBarcode documentBarcode, float confidence) {
                documentBarcode.setConfidence(confidence);
            }

            @Override
            public void setValue(DocumentBarcode documentBarcode, String value) {
                documentBarcode.setValue(value);
            }

            @Override
            public void setBoundingPolygon(DocumentBarcode documentBarcode, List<Point> polygon) {
                documentBarcode.setPolygon(polygon);
            }
        });
    }
}
