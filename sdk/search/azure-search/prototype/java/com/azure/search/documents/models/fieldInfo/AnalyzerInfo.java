package com.azure.search.documents.models.fieldInfo;

import com.azure.search.documents.models.Field;
import com.azure.search.documents.models.analyzerName.StrictAnalyzerName;
import com.azure.search.documents.models.fieldInfo.DualAnalyzerInfo;

public class AnalyzerInfo {
    private StrictAnalyzerName strictAnalyzerName;
    private DualAnalyzerInfo dualAnalyzerInfo;

    public StrictAnalyzerName getStrictAnalyzerName() {
        return strictAnalyzerName;
    }

    public void setStrictAnalyzerName(final StrictAnalyzerName strictAnalyzerName) {
        this.strictAnalyzerName = strictAnalyzerName;
        this.dualAnalyzerInfo = null;
    }

    public DualAnalyzerInfo getDualAnalyzerInfo() {
        return dualAnalyzerInfo;
    }

    public void setDualAnalyzerInfo(final DualAnalyzerInfo dualAnalyzerInfo) {
        this.dualAnalyzerInfo = dualAnalyzerInfo;
        this.strictAnalyzerName = null;
    }

    public Field toField() {
        if (strictAnalyzerName != null) {
            return new Field().setAnalyzer(strictAnalyzerName.toAnalyzerName());
        }
        return new Field().setSearchAnalyzer(dualAnalyzerInfo.getSearchAnalyzer().toAnalyzerName())
            .setIndexAnalyzer(dualAnalyzerInfo.getIndexAnalyzer().toAnalyzerName());
    }
}
