package com.azure.search.documents.models.fieldInfo;

import com.azure.search.documents.models.Field;

public class CommonFieldInfo {
    private final String name;
    private final Boolean isFilterable;
    private final Boolean isSortable;
    private final Boolean isFacetable;

    public CommonFieldInfo(final String name, final Boolean isFilterable, final Boolean isSortable,
        final Boolean isFacetable) {
        this.name = name;
        this.isFilterable = isFilterable;
        this.isSortable = isSortable;
        this.isFacetable = isFacetable;
    }


    public String getName() {
        return name;
    }

    public Boolean getFilterable() {
        return isFilterable;
    }

    public Boolean getSortable() {
        return isSortable;
    }

    public Boolean getFacetable() {
        return isFacetable;
    }

    public Field toField() {
        return new Field().setName(name).setFilterable(isFilterable).setSortable(isSortable)
            .setFacetable(isFacetable);
    }
}
