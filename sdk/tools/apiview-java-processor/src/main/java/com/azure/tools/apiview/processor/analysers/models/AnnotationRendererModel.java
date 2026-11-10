package com.azure.tools.apiview.processor.analysers.models;

import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;

public class AnnotationRendererModel {
    private final AnnotationExpr annotation;

    private final NodeWithAnnotations<?> annotationParent;

    private final AnnotationRule rule;

    private final boolean showProperties;

    private final boolean addNewline;

    public AnnotationRendererModel(AnnotationExpr annotation,
                                   NodeWithAnnotations<?> nodeWithAnnotations,
                                   AnnotationRule rule,
                                   boolean showAnnotationProperties,
                                   boolean addNewline) {
        this.annotation = annotation;
        this.annotationParent = nodeWithAnnotations;
        this.rule = rule;

        // we override the showAnnotationProperties flag if the annotation rule specifies it
        this.showProperties = rule == null ? showAnnotationProperties : rule.isShowProperties().orElse(showAnnotationProperties);
        this.addNewline = rule == null ? addNewline : rule.isShowOnNewline().orElse(addNewline);
    }

    public boolean isAddNewline() {
        return addNewline;
    }

    public boolean isShowProperties() {
        return showProperties;
    }

    public AnnotationExpr getAnnotation() {
        return annotation;
    }

    public NodeWithAnnotations<?> getAnnotationParent() {
        return annotationParent;
    }

    public boolean isHidden() {
        return rule != null && rule.isHidden().orElse(false);
    }

    public boolean isCondensed() {
        return rule != null && rule.isCondensed().orElse(false);
    }
}
