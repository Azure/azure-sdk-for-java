module com.azure.search.documents.serializer.gson {
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.azure.search.documents.serializer;
    requires transitive com.azure.core.experimental;

    exports com.azure.search.documents.serializer.gson;

    provides com.azure.search.documents.serializer.SearchSerializerProvider
        with com.azure.search.documents.serializer.gson.SearchGsonSerializerProvider;
}
