package com.azure.search.documents.models.fieldInfo;

import com.azure.search.documents.models.Field;
import com.azure.search.documents.models.utils.FieldUnion;
import com.azure.search.documents.models.utils.Pair;
import com.azure.search.documents.models.dataType.PrimitiveType;

public class SimpleFieldInfo {
    private CommonFieldInfo keyField;
    private Pair<CommonFieldInfo, NonKeyFieldInfo> nonKeyField;

    public CommonFieldInfo getKeyField() {
        return keyField;
    }

    public Pair<CommonFieldInfo, NonKeyFieldInfo> getNonKeyField() {
        return nonKeyField;
    }

    public void setNonKeyField(CommonFieldInfo commonFieldInfo, NonKeyFieldInfo nonKeyField) {
        this.nonKeyField = new Pair<CommonFieldInfo, NonKeyFieldInfo>(commonFieldInfo, nonKeyField);
        this.keyField = null;
    }

    public void setKeyField(CommonFieldInfo keyField) {
        this.keyField = keyField;
        nonKeyField = null;
    }

    public Field toField() {
        if (keyField == null && nonKeyField == null) {
            throw new RuntimeException();
        }
        if (keyField != null) {
            return keyField.toField().setKey(false).setType(PrimitiveType.EDM_STRING.toDataType());
        }
        return FieldUnion.union(nonKeyField.getLhs().toField(), nonKeyField.getRhs().toField());
    }
}
