// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.javadoc;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;

final class DocumentationClassifier {
    private final JavaParser parser = new JavaParser(new ParserConfiguration()
        .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_8)
        .setStoreTokens(false)
        .setAttributeComments(false)
        .setDetectOriginalLineSeparator(false));

    static boolean isEligible(String reason) {
        return "ast-equivalent".equals(reason);
    }

    String compare(String before, String after) {
        // javac translates Unicode escapes before recognizing comments.
        if (before.contains("\\u") || after.contains("\\u")) {
            return "unsupported-unicode-escape";
        }
        ParseResult<CompilationUnit> oldSource = parser.parse(before);
        ParseResult<CompilationUnit> newSource = parser.parse(after);
        if (!complete(oldSource) || !complete(newSource)) {
            return "parse-error";
        }
        if (before.equals(after)) {
            return "no-source-edit";
        }
        return oldSource.getResult().get().toString().equals(newSource.getResult().get().toString())
            ? "ast-equivalent" : "non-documentation-change";
    }

    private static boolean complete(ParseResult<CompilationUnit> source) {
        return source.isSuccessful() && source.getResult().isPresent();
    }
}
