// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.analysers.models;

import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;

/**
 * An annotation and the fields selected for API review rendering.
 */
public class AnnotationRendererModel {
    private final AnnotationExpr annotation;

    private final NodeWithAnnotations<?> annotationParent;

    private final AnnotationRule rule;

    private final boolean showProperties;

    private final boolean addNewline;

    /**
     * Creates an annotation renderer model.
     *
     * @param annotation the annotation
     * @param nodeWithAnnotations the node with annotations
     * @param rule the rule
     * @param showAnnotationProperties the show annotation properties
     * @param addNewline the add newline
     */
    public AnnotationRendererModel(AnnotationExpr annotation, NodeWithAnnotations<?> nodeWithAnnotations,
        AnnotationRule rule, boolean showAnnotationProperties, boolean addNewline) {
        this.annotation = annotation;
        this.annotationParent = nodeWithAnnotations;
        this.rule = rule;

        // we override the showAnnotationProperties flag if the annotation rule specifies it
        this.showProperties
            = rule == null ? showAnnotationProperties : rule.isShowProperties().orElse(showAnnotationProperties);
        this.addNewline = rule == null ? addNewline : rule.isShowOnNewline().orElse(addNewline);
    }

    /**
     * Returns whether the annotation starts on a new line.
     *
     * @return true if enabled
     */
    public boolean isAddNewline() {
        return addNewline;
    }

    /**
     * Returns whether annotation properties are displayed.
     *
     * @return true if enabled
     */
    public boolean isShowProperties() {
        return showProperties;
    }

    /**
     * Returns the annotation.
     *
     * @return the annotation
     */
    public AnnotationExpr getAnnotation() {
        return annotation;
    }

    /**
     * Returns the annotation parent.
     *
     * @return the annotation parent
     */
    public NodeWithAnnotations<?> getAnnotationParent() {
        return annotationParent;
    }

    /**
     * Returns whether the annotation is hidden.
     *
     * @return true if enabled
     */
    public boolean isHidden() {
        return rule != null && rule.isHidden().orElse(false);
    }

    /**
     * Returns whether the annotation uses condensed rendering.
     *
     * @return true if enabled
     */
    public boolean isCondensed() {
        return rule != null && rule.isCondensed().orElse(false);
    }
}
