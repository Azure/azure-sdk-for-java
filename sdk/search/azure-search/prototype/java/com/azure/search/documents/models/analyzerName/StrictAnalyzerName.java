package com.azure.search.documents.models.analyzerName;

import com.azure.core.util.ExpandableStringEnum;
import com.azure.search.documents.models.AnalyzerName;
import com.fasterxml.jackson.annotation.JsonCreator;

public class StrictAnalyzerName extends ExpandableStringEnum<StrictAnalyzerName> {
    public static StrictAnalyzerName languageAnalyzerName (LanguageAnalyzerName languageAnalyzerName) {
        return fromString(languageAnalyzerName.toString());
    }

    public static StrictAnalyzerName nonLanguageAnalyzerName (NonLanguageAnalyzerName nonLanguageAnalyzerName) {
        return fromString(nonLanguageAnalyzerName.toString());
    }

    /**
     * Creates or finds a AnalyzerName from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding AnalyzerName.
     */
    @JsonCreator
    private static StrictAnalyzerName fromString(String name) {
        return fromString(name, StrictAnalyzerName.class);
    }

    public AnalyzerName toAnalyzerName() {
        return AnalyzerName.fromString(this.toString());
    }
}
