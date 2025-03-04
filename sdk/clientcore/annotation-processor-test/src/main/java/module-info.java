module annotation.processor.test {
    requires transitive io.clientcore.core;
    requires io.clientcore.http.okhttp3;

    opens io.clientcore.annotation.processor.test.implementation.models to io.clientcore.core;
}
