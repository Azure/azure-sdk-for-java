// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation.accesshelpers;

import com.azure.ai.openai.models.PageableList;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/**
 * This class is used to access internal methods on OpenAIPageable... classes.
 */
public final class PageableListAccessHelper {

    private static PageableListAccessor accessor;

    /**
     * Type to be used to access the constructors of the OpenAIPageable classes.
     */
    public interface PageableListAccessor {

        /**
         * Create a new instance of {@link PageableList}.
         *
         * @param data the list of item in this page of data
         * @param firstId the id of the first item in the list
         * @param lastId the id of the last item in the list
         * @param hasMore whether there are more items that could be request
         * @return a new instance of {@link PageableList}
         * @param <T> the type of item for which a page of items is being created
         */
        <T> PageableList<T> create(List<T> data, String firstId, String lastId, boolean hasMore);
    }

    /**
     * Set the {@link PageableListAccessor}.
     *
     * @param pageableContentAccessor the accessor to set
     */
    public static void setAccessor(final PageableListAccessor pageableContentAccessor) {
        accessor = pageableContentAccessor;
    }

    /**
     * Create a new instance of {@link PageableList}.
     *
     * @param data the list of item in this page of data
     * @param firstId the id of the first item in the list
     * @param lastId the id of the last item in the list
     * @param hasMore whether there are more items that could be request
     * @return a new instance of {@link PageableList}
     * @param <T> the type of item for which a page of items is being created
     */
    public static <T> PageableList <T> create(List<T> data, String firstId, String lastId, boolean hasMore) {
        if (accessor == null) {
            new PageableList<>();
        }
        assert accessor != null;
        return accessor.create(data, firstId, lastId, hasMore);
    }

    /**
     * Create a new instance of {@link PageableList} from JSON.
     *
     * @param json the JSON to read from in the form of {@link BinaryData}
     * @param dataHandler the function to deserialize a single item type
     * @return a new instance of {@link PageableList}
     * @param <T> the type of item for which a page of items is being created
     * @throws IOException related to deserialization issues
     */
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
                    data = dataHandler.apply(reader);
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

    private PageableListAccessHelper() {
    }

}
