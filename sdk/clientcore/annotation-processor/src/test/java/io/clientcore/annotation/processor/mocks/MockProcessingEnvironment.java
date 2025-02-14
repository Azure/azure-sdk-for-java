// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.annotation.processor.mocks;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * Mock implementation of {@link ProcessingEnvironment}.
 */
public class MockProcessingEnvironment implements ProcessingEnvironment {
    private final Filer filer;
    private int getFilerCount = 0;

    /**
     * Creates a new instance of {@link MockProcessingEnvironment}.
     *
     * @param filer The {@link Filer} to return from {@link #getFiler()}.
     */
    public MockProcessingEnvironment(Filer filer) {
        this.filer = filer;
    }

    @Override
    public Map<String, String> getOptions() {
        return Collections.emptyMap();
    }

    @Override
    public Messager getMessager() {
        return null;
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
        return null;
    }

    @Override
    public Types getTypeUtils() {
        return null;
    }

    @Override
    public SourceVersion getSourceVersion() {
        return null;
    }

    @Override
    public Locale getLocale() {
        return null;
    }
}
