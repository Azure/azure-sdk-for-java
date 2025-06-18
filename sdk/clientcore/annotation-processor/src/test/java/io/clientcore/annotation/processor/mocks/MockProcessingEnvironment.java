// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.annotation.processor.mocks;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import javax.tools.Diagnostic;

/**
 * Mock implementation of {@link ProcessingEnvironment}.
 */
public class MockProcessingEnvironment implements ProcessingEnvironment {
    private final Filer filer;
    private final Elements elementUtils;
    private final Types typeUtils;
    private int getFilerCount = 0;

    /**
     * Creates a new instance of {@link MockProcessingEnvironment}.
     *
     * @param filer The {@link Filer} to return from {@link #getFiler()}.
     * @param elementUtils
     * @param typeUtils
     */
    public MockProcessingEnvironment(Filer filer, Elements elementUtils, Types typeUtils) {
        this.filer = filer;
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
    }

    @Override
    public Map<String, String> getOptions() {
        return Collections.emptyMap();
    }

    @Override
    public Messager getMessager() {
        return new MockMessager();
    }

    @Override
    public Filer getFiler() {
        getFilerCount++;
        return filer;
    }

    /**
     * Returns the number of times {@link #getFiler()} was called.
     *
     * @return The number of times {@link #getFiler()} was called.
     */
    public int getGetFilerCount() {
        return getFilerCount;
    }

    @Override
    public Elements getElementUtils() {
        return elementUtils;
    }

    @Override
    public Types getTypeUtils() {
        return typeUtils;
    }

    @Override
    public SourceVersion getSourceVersion() {
        return null;
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    private static class MockMessager implements Messager {
        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
            // No-op for mock
        }

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {
            // No-op for mock
        }

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a) {
            // No-op for mock
        }

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a,
            AnnotationValue v) {
            // No-op for mock
        }
    }
}
