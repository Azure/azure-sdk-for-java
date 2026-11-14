// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.analysers.models;

import java.util.Optional;

/**
 * Rendering options for an annotation type.
 */
public class AnnotationRule {

    private Optional<Boolean> hideAnnotation = Optional.empty();

    private Optional<Boolean> showProperties = Optional.empty();

    private Optional<Boolean> showOnNewline = Optional.empty();

    private Optional<Boolean> condensed = Optional.empty();

    /**
     * Sets whether the annotation is hidden.
     *
     * @param hidden the hidden
     * @return this instance
     */
    public AnnotationRule setHidden(boolean hidden) {
        this.hideAnnotation = Optional.of(hidden);
        return this;
    }

    /**
     * Returns the override for hiding the annotation.
     *
     * @return the override, or empty to use the renderer default
     */
    public Optional<Boolean> isHidden() {
        return hideAnnotation;
    }

    /**
     * Sets whether annotation properties are displayed.
     *
     * @param showProperties the show properties
     * @return this instance
     */
    public AnnotationRule setShowProperties(boolean showProperties) {
        this.showProperties = Optional.of(showProperties);
        return this;
    }

    /**
     * Returns the override for displaying annotation properties.
     *
     * @return the override, or empty to use the renderer default
     */
    public Optional<Boolean> isShowProperties() {
        return showProperties;
    }

    /**
     * Sets whether the annotation starts on a new line.
     *
     * @param showOnNewline the show on newline
     * @return this instance
     */
    public AnnotationRule setShowOnNewline(boolean showOnNewline) {
        this.showOnNewline = Optional.of(showOnNewline);
        return this;
    }

    /**
     * Returns the override for starting on a new line.
     *
     * @return the override, or empty to use the renderer default
     */
    public Optional<Boolean> isShowOnNewline() {
        return showOnNewline;
    }

    /**
     * Sets whether the annotation uses condensed rendering.
     *
     * @param condensed the condensed
     * @return this instance
     */
    public AnnotationRule setCondensed(boolean condensed) {
        this.condensed = Optional.of(condensed);
        return this;
    }

    /**
     * Returns the override for condensed rendering.
     *
     * @return the override, or empty to use the renderer default
     */
    public Optional<Boolean> isCondensed() {
        return condensed;
    }
}
