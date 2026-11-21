package com.azure.tools.apiview.processor.analysers.models;

import java.util.Optional;

public class AnnotationRule {

    private Optional<Boolean> hideAnnotation = Optional.empty();

    private Optional<Boolean> showProperties = Optional.empty();

    private Optional<Boolean> showOnNewline = Optional.empty();

    private Optional<Boolean> condensed = Optional.empty();

    public AnnotationRule setHidden(boolean hidden) {
        this.hideAnnotation = Optional.of(hidden);
        return this;
    }

    public Optional<Boolean> isHidden() {
        return hideAnnotation;
    }

    public AnnotationRule setShowProperties(boolean showProperties) {
        this.showProperties = Optional.of(showProperties);
        return this;
    }

    public Optional<Boolean> isShowProperties() {
        return showProperties;
    }

    public AnnotationRule setShowOnNewline(boolean showOnNewline) {
        this.showOnNewline = Optional.of(showOnNewline);
        return this;
    }

    public Optional<Boolean> isShowOnNewline() {
        return showOnNewline;
    }

    public AnnotationRule setCondensed(boolean condensed) {
        this.condensed = Optional.of(condensed);
        return this;
    }

    public Optional<Boolean> isCondensed() {
        return condensed;
    }
}
