import com.azure.search.documents.serializer.jackson.SearchJacksonSerializerProvider;

module com.azure.search.documents.serializer.jackson {
    requires com.azure.core.experimental;
    requires com.azure.core.serializer.json.jackson;

    exports com.azure.search.documents.serializer.jackson;

    provides com.azure.core.experimental.serializer.JsonSerializerProvider
        with SearchJacksonSerializerProvider;
}
