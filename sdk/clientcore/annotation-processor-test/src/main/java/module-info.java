module annotation.processor.test {
    requires transitive io.clientcore.core;
    requires jdk.httpserver;

    // TODO: refactor Foo class to implement JsonSerializable
    exports io.clientcore.annotation.processor.test.implementation.models to io.clientcore.core;
}
