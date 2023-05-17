// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.DocumentPageHelper;
import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * Content and layout elements extracted from a page from the input.
 */
@Immutable
public final class DocumentPage {
    /*
     * 1-based page number in the input document.
     */
    private int pageNumber;

    /*
     * The general orientation of the content in clockwise direction, measured
     * in degrees between (-180, 180].
     */
    private Float angle;

    /*
     * The width of the image/PDF in pixels/inches, respectively.
     */
    private Float width;

    /*
     * The height of the image/PDF in pixels/inches, respectively.
     */
    private Float height;

    /*
     * The unit used by the width, height, and boundingBox properties. For
     * images, the unit is "pixel". For PDF, the unit is "inch".
     */
    private DocumentPageLengthUnit unit;

    /*
     * Location of the page in the reading order concatenated content.
     */
    private List<DocumentSpan> spans;

    /*
     * Extracted words from the page.
     */
    private List<DocumentWord> words;

    /*
     * Extracted selection marks from the page.
     */
    private List<DocumentSelectionMark> selectionMarks;

    /*
     * Extracted lines from the page, potentially containing both textual and
     * visual elements.
     */
    private List<DocumentLine> lines;

    private DocumentPageKind kind;

    /*
     * Extracted annotations from the page.
     */
    private List<DocumentAnnotation> annotations;

    /*
     * Extracted barcodes from the page.
     */
    private List<DocumentBarcode> barcodes;

    /*
     * Extracted formulas from the page.
     */
    private List<DocumentFormula> formulas;

    /*
     * Extracted images from the page.
     */
    private List<DocumentImage> images;

    /**
     * Get the 1-based page number in the input document.
     *
     * @return the pageNumber value.
     */
    public int getPageNumber() {
        return this.pageNumber;
    }

    /**
     * Set the pageNumber property: 1-based page number in the input document.
     *
     * @param pageNumber the pageNumber value to set.
     * @return the DocumentPage object itself.
     */
    private void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    /**
     * Get the general orientation of the content in clockwise direction, measured in degrees
     * between (-180, 180].
     *
     * @return the angle value.
     */
    public Float getAngle() {
        return this.angle;
    }

    /**
     * Set the angle property: The general orientation of the content in clockwise direction, measured in degrees
     * between (-180, 180].
     *
     * @param angle the angle value to set.
     * @return the DocumentPage object itself.
     */
    private void setAngle(Float angle) {
        this.angle = angle;
    }

    /**
     * Get the width of the image/PDF in pixels/inches, respectively.
     *
     * @return the width value.
     */
    public Float getWidth() {
        return this.width;
    }

    /**
     * Set the width property: The width of the image/PDF in pixels/inches, respectively.
     *
     * @param width the width value to set.
     * @return the DocumentPage object itself.
     */
    private void setWidth(Float width) {
        this.width = width;
    }

    /**
     * Get the height of the image/PDF in pixels/inches, respectively.
     *
     * @return the height value.
     */
    public Float getHeight() {
        return this.height;
    }

    /**
     * Set the height property: The height of the image/PDF in pixels/inches, respectively.
     *
     * @param height the height value to set.
     * @return the DocumentPage object itself.
     */
    private void setHeight(Float height) {
        this.height = height;
    }

    /**
     * Get the unit used by the width, height, and boundingBox properties. For images, the unit is
     * "pixel". For PDF, the unit is "inch".
     *
     * @return the unit value.
     */
    public DocumentPageLengthUnit getUnit() {
        return this.unit;
    }

    /**
     * Set the unit property: The unit used by the width, height, and boundingBox properties. For images, the unit is
     * "pixel". For PDF, the unit is "inch".
     *
     * @param unit the unit value to set.
     * @return the DocumentPage object itself.
     */
    private void setUnit(DocumentPageLengthUnit unit) {
        this.unit = unit;
    }

    /**
     * Get the location of the page in the reading order concatenated content.
     *
     * @return the spans value.
     */
    public List<DocumentSpan> getSpans() {
        return this.spans;
    }

    /**
     * Set the spans property: Location of the page in the reading order concatenated content.
     *
     * @param spans the spans value to set.
     * @return the DocumentPage object itself.
     */
    private void setSpans(List<DocumentSpan> spans) {
        this.spans = spans;
    }

    /**
     * Get the extracted words from the page.
     *
     * @return the words value.
     */
    public List<DocumentWord> getWords() {
        return this.words;
    }

    /**
     * Set the words property: Extracted words from the page.
     *
     * @param words the words value to set.
     * @return the DocumentPage object itself.
     */
    private void setWords(List<DocumentWord> words) {
        this.words = words;
    }

    /**
     * Get the extracted selection marks from the page.
     *
     * @return the selectionMarks value.
     */
    public List<DocumentSelectionMark> getSelectionMarks() {
        return this.selectionMarks;
    }

    /**
     * Set the selectionMarks property: Extracted selection marks from the page.
     *
     * @param selectionMarks the selectionMarks value to set.
     * @return the DocumentPage object itself.
     */
    private void setSelectionMarks(List<DocumentSelectionMark> selectionMarks) {
        this.selectionMarks = selectionMarks;
    }

    /**
     * Get the extracted lines from the page, potentially containing both textual and visual elements.
     *
     * @return the lines value.
     */
    public List<DocumentLine> getLines() {
        return this.lines;
    }

    /**
     * Set the lines property: Extracted lines from the page, potentially containing both textual and visual elements.
     *
     * @param lines the lines value to set.
     * @return the DocumentPage object itself.
     */
    private void setLines(List<DocumentLine> lines) {
        this.lines = lines;
    }

    /**
     * Get the kind of document page.
     *
     * @return the kind value.
     */
    public DocumentPageKind getKind() {
        return this.kind;
    }

    /**
     * Set the kind property: Kind of document page.
     *
     * @param kind the kind value to set.
     */
    void setKind(DocumentPageKind kind) {
        this.kind = kind;
    }

    /**
     * Get the extracted annotations from the page.
     *
     * @return the annotations value.
     */
    public List<DocumentAnnotation> getAnnotations() {
        return this.annotations;
    }

    /**
     * Set the annotations property: Extracted annotations from the page.
     *
     * @param annotations the annotations value to set.
     * @return the DocumentPage object itself.
     */
    void setAnnotations(List<DocumentAnnotation> annotations) {
        this.annotations = annotations;
    }

    /**
     * Get the extracted barcodes from the page.
     *
     * @return the barcodes value.
     */
    public List<DocumentBarcode> getBarcodes() {
        return this.barcodes;
    }

    /**
     * Set the barcodes property: Extracted barcodes from the page.
     *
     * @param barcodes the barcodes value to set.
     * @return the DocumentPage object itself.
     */
    void setBarcodes(List<DocumentBarcode> barcodes) {
        this.barcodes = barcodes;
    }

    /**
     * Get the extracted formulas from the page.
     *
     * @return the formulas value.
     */
    public List<DocumentFormula> getFormulas() {
        return this.formulas;
    }

    /**
     * Set the formulas property: Extracted formulas from the page.
     *
     * @param formulas the formulas value to set.
     * @return the DocumentPage object itself.
     */
    void setFormulas(List<DocumentFormula> formulas) {
        this.formulas = formulas;
    }

    /**
     * Get the extracted images from the page.
     *
     * @return the images value.
     */
    public List<DocumentImage> getImages() {
        return this.images;
    }

    /**
     * Set the images property: Extracted images from the page.
     *
     * @param images the images value to set.
     * @return the DocumentPage object itself.
     */
    void setImages(List<DocumentImage> images) {
        this.images = images;
    }

    static {
        DocumentPageHelper.setAccessor(new DocumentPageHelper.DocumentPageAccessor() {
            @Override
            public void setPageNumber(DocumentPage documentPage, int pageNumber) {
                documentPage.setPageNumber(pageNumber);
            }

            @Override
            public void setAngle(DocumentPage documentPage, Float angle) {
                documentPage.setAngle(angle);
            }

            @Override
            public void setWidth(DocumentPage documentPage, Float width) {
                documentPage.setWidth(width);
            }

            @Override
            public void setHeight(DocumentPage documentPage, Float height) {
                documentPage.setHeight(height);
            }

            @Override
            public void setUnit(DocumentPage documentPage, DocumentPageLengthUnit unit) {
                documentPage.setUnit(unit);
            }

            @Override
            public void setSpans(DocumentPage documentPage, List<DocumentSpan> spans) {
                documentPage.setSpans(spans);
            }

            @Override
            public void setWords(DocumentPage documentPage, List<DocumentWord> words) {
                documentPage.setWords(words);
            }

            @Override
            public void setSelectionMarks(DocumentPage documentPage, List<DocumentSelectionMark> selectionMarks) {
                documentPage.setSelectionMarks(selectionMarks);
            }

            @Override
            public void setLines(DocumentPage documentPage, List<DocumentLine> lines) {
                documentPage.setLines(lines);
            }

            @Override
            public void setKind(DocumentPage documentPage, DocumentPageKind pageKind) {
                documentPage.setKind(pageKind);
            }

            @Override
            public void setImages(DocumentPage documentPage, List<DocumentImage> images) {
                documentPage.setImages(images);
            }

            @Override
            public void setFormulas(DocumentPage documentPage, List<DocumentFormula> formulas) {
                documentPage.setFormulas(formulas);
            }

            @Override
            public void setBarcodes(DocumentPage documentPage, List<DocumentBarcode> barcodes) {
                documentPage.setBarcodes(barcodes);
            }

            @Override
            public void setAnnotations(DocumentPage documentPage, List<DocumentAnnotation> annotations) {
                documentPage.setAnnotations(annotations);
            }
        });
    }
}
