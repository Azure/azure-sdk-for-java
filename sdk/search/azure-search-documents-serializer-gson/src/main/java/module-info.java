import com.azure.search.documents.serializer.gson.SearchGsonSerializerProvider;

module com.azure.search.documents.serializer.gson {
    requires com.azure.core.serializer.json.gson;
    requires com.azure.search.documents.serializer;

    exports com.azure.search.documents.serializer.gson;

    provides com.azure.search.documents.serializer.SearchSerializerProvider
        with SearchGsonSerializerProvider;
}
