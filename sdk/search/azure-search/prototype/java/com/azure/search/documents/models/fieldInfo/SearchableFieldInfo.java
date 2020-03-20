package com.azure.search.documents.models.fieldInfo;

import com.azure.search.documents.models.Field;
import com.azure.search.documents.models.utils.FieldUnion;
import java.util.List;

public class SearchableFieldInfo {
    private final SimpleFieldInfo simpleFieldInfo;
    private final AnalyzerInfo analyzerInfo;
    private final List<String> synonymMaps;

    public SearchableFieldInfo(final SimpleFieldInfo simpleFieldInfo,
        final AnalyzerInfo analyzerInfo, final List<String> synonymMaps) {
        // Check null
        this.simpleFieldInfo = simpleFieldInfo;
        this.analyzerInfo = analyzerInfo;
        this.synonymMaps = synonymMaps;
    }

    public AnalyzerInfo getAnalyzerInfo() {
        return analyzerInfo;
    }

    public List<String> getSynonymMaps() {
        return synonymMaps;
    }

    public SimpleFieldInfo getSimpleFieldInfo() {
        return simpleFieldInfo;
    }
    public Field toField() {
        return FieldUnion.union(simpleFieldInfo.toField(), analyzerInfo.toField()).setSynonymMaps(synonymMaps);
    }
}
