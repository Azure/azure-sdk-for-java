// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.DocumentFormulaHelper;
import com.azure.core.annotation.Fluent;

import java.util.List;

/** A formula object. */
@Fluent
public final class DocumentFormula {
    /*
     * Formula kind.
     */
    private DocumentFormulaKind kind;

    /*
     * LaTex expression describing the formula.
     */
    private String value;

    /*
     * Bounding polygon of the formula.
     */
    private List<Point> polygon;

    /*
     * Location of the formula in the reading order concatenated content.
     */
    private DocumentSpan span;

    /*
     * Confidence of correctly extracting the formula.
     */
    private float confidence;

    /**
     * Get the kind property: Formula kind.
     *
     * @return the kind value.
     */
    public DocumentFormulaKind getKind() {
        return this.kind;
    }

    /**
     * Get the value property: LaTex expression describing the formula.
     *
     * @return the value value.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Get the polygon property: Bounding polygon of the formula.
     *
     * @return the polygon value.
     */
    public List<Point> getBoundingPolygon() {
        return this.polygon;
    }

    /**
     * Set the polygon property: Bounding polygon of the formula.
     *
     * @param polygon the polygon value to set.
     * @return the DocumentFormula object itself.
     */
    void setPolygon(List<Point> polygon) {
        this.polygon = polygon;
    }

    /**
     * Get the span property: Location of the formula in the reading order concatenated content.
     *
     * @return the span value.
     */
    public DocumentSpan getSpan() {
        return this.span;
    }

    /**
     * Get the confidence property: Confidence of correctly extracting the formula.
     *
     * @return the confidence value.
     */
    public float getConfidence() {
        return this.confidence;
    }

    void setKind(DocumentFormulaKind kind) {
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
        DocumentFormulaHelper.setAccessor(new DocumentFormulaHelper.DocumentFormulaAccessor() {
            @Override
            public void setSpan(DocumentFormula documentFormula, DocumentSpan span) {
                documentFormula.setSpan(span);
            }

            @Override
            public void setKind(DocumentFormula documentFormula, DocumentFormulaKind kind) {
                documentFormula.setKind(kind);
            }

            @Override
            public void setConfidence(DocumentFormula documentFormula, float confidence) {
                documentFormula.setConfidence(confidence);
            }

            @Override
            public void setValue(DocumentFormula documentFormula, String value) {
                documentFormula.setValue(value);
            }

            @Override
            public void setPolygon(DocumentFormula documentFormula, List<Point> polygon) {
                documentFormula.setPolygon(polygon);
            }
        });
    }
}
