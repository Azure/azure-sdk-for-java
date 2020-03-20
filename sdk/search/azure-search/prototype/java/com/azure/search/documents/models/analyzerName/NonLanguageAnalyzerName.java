package com.azure.search.documents.models.analyzerName;

import com.azure.core.util.ExpandableStringEnum;
import com.azure.search.documents.models.AnalyzerName;
import com.fasterxml.jackson.annotation.JsonCreator;

public class NonLanguageAnalyzerName extends ExpandableStringEnum<NonLanguageAnalyzerName> {
    public static final NonLanguageAnalyzerName KEYWORD = fromString(AnalyzerName.KEYWORD.toString());
    public static final NonLanguageAnalyzerName SIMPLE = fromString(AnalyzerName.SIMPLE.toString());
    public static final NonLanguageAnalyzerName STOP = fromString(AnalyzerName.STOP.toString());
    public static final NonLanguageAnalyzerName WHITESPACE = fromString(AnalyzerName.WHITESPACE.toString());

    /**
     * Creates or finds a AnalyzerName from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding AnalyzerName.
     */
    @JsonCreator
    private static NonLanguageAnalyzerName fromString(String name) {
        return fromString(name, NonLanguageAnalyzerName.class);
    }

    public AnalyzerName toAnalyzerName() {
        return AnalyzerName.fromString(this.toString());
    }
}
