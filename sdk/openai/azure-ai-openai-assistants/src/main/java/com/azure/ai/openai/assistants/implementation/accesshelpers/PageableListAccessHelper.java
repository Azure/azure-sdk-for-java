package com.azure.ai.openai.assistants.implementation.accesshelpers;

import com.azure.ai.openai.assistants.models.PageableList;

import java.util.List;

public final class PageableListAccessHelper {

    private static PageableListAccessor accessor;

    public interface PageableListAccessor {
         <T> PageableList<T> create(List<T> data, String firstId, String lastId, boolean hasMore);
    }

    public static void setAccessor(final PageableListAccessor pageableContentAccessor) {
        accessor = pageableContentAccessor;
    }

    public static <T> PageableList <T> create(List<T> data, String firstId, String lastId, boolean hasMore) {
        if(accessor == null) {
            new PageableList<>();
        }
        assert accessor != null;
        return accessor.create(data, firstId, lastId, hasMore);
    }
    private PageableListAccessHelper(){
    }

}
