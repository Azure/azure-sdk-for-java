package com.azure.ai.openai.assistants.implementation.accesshelpers;

import com.azure.ai.openai.assistants.models.AssistantFile;
import com.azure.ai.openai.assistants.models.PageableList;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

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

    public static <T> PageableList<T> create(BinaryData json, Function<JsonReader, List<T>> dataHandler) throws IOException {
        JsonReader jsonReader = JsonProviders.createReader(json.toStream());
        return jsonReader.readObject(reader -> {
            List<T> data = null;
            String firstId = null;
            String lastId = null;
            boolean hasMore = false;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("data".equals(fieldName)) {
                    data = dataHandler.apply(reader); //reader.readArray(reader1 -> AssistantFile.fromJson(reader1));
                } else if ("first_id".equals(fieldName)) {
                    firstId = reader.getString();
                } else if ("last_id".equals(fieldName)) {
                    lastId = reader.getString();
                } else if ("has_more".equals(fieldName)) {
                    hasMore = reader.getBoolean();
                } else {
                    reader.skipChildren();
                }
            }
            return create(data, firstId, lastId, hasMore);
        });
    }

    private PageableListAccessHelper(){
    }

}
