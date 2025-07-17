// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.annotation.processor.mocks;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.IOException;

/**
 * Mock implementation of {@link Filer} for testing purposes.
 */
public class MockFiler implements Filer {
    private final JavaFileObject sourceFile;

    /**
     * Creates an instance of {@link MockFiler}.
     *
     * @param sourceFile The source file to be used in the mock.
     */
    public MockFiler(JavaFileObject sourceFile) {
        this.sourceFile = sourceFile;
    }

    @Override
    public JavaFileObject createSourceFile(CharSequence name, Element... originatingElements) throws IOException {
        return sourceFile;
    }

    @Override
    public JavaFileObject createClassFile(CharSequence name, Element... originatingElements) throws IOException {
        return null;
    }

    @Override
    public FileObject createResource(JavaFileManager.Location location, CharSequence moduleAndPkg,
        CharSequence relativeName, Element... originatingElements) throws IOException {
        return null;
    }

    @Override
    public FileObject getResource(JavaFileManager.Location location, CharSequence moduleAndPkg,
        CharSequence relativeName) throws IOException {
        return null;
    }
}
