package com.azure.search.documents.models.field;

import com.azure.search.documents.models.Field;
import com.azure.search.documents.models.fieldInfo.SearchableFieldInfo;
import com.azure.search.documents.models.fieldInfo.SimpleFieldInfo;

public class SimpleField {
    private SimpleFieldInfo nonSearchableField;
    private SearchableFieldInfo searchableField;

    public void setNonSearchableField(final SimpleFieldInfo nonSearchableField) {
        this.nonSearchableField = nonSearchableField;
        this.searchableField = null;
    }

    public void setSearchableField(SearchableFieldInfo searchableField) {
        this.searchableField =  searchableField;
        this.nonSearchableField = null;
    }

    public Field toField() {
        if (searchableField == null && nonSearchableField == null) {
            throw new RuntimeException();
        }
        if (nonSearchableField != null) {
            return nonSearchableField.toField().setSearchable(false);
        }
        return searchableField.toField().setSearchable(true);
    }
}
