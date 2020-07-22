import com.azure.search.documents.serializer.jackson.SearchJacksonSerializerProvider;

module com.azure.search.documents.serializer.jackson {
    requires com.azure.search.documents.serializer;
    requires com.azure.core.serializer.json.jackson;

    exports com.azure.search.documents.serializer.jackson;

    provides com.azure.search.documents.serializer.SearchSerializerProvider
        with SearchJacksonSerializerProvider;
}
