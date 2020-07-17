import com.azure.search.documents.serializer.gson.SearchGsonSerializerProvider;

module com.azure.search.documents.serializer.gson {
    requires transitive com.azure.core.experimental;

    exports com.azure.search.documents.serializer.gson;

    provides com.azure.core.experimental.serializer.JsonSerializerProvider
        with SearchGsonSerializerProvider;
}
