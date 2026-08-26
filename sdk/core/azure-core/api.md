```java
maven {
    parent : com.azure:azure-client-sdk-parent:1.7.0
    properties : com.azure:azure-core:1.60.0-beta.1
    configuration {
        jacoco {
            min-line-coverage : 0.6
            min-branch-coverage : 0.6
        }
    }
    name : Microsoft Azure Java Core Library
    description : This package contains core types for Azure Java clients.
    dependencies {
        com.azure:azure-json 1.5.1
        com.azure:azure-xml 1.2.1
        com.fasterxml.jackson.core:jackson-annotations 2.18.9
        com.fasterxml.jackson.core:jackson-core 2.18.9
        com.fasterxml.jackson.core:jackson-databind 2.18.9
        com.fasterxml.jackson.datatype:jackson-datatype-jsr310 2.18.9
        org.slf4j:slf4j-api 1.7.36
        io.projectreactor:reactor-core 3.7.19
        com.google.code.findbugs:jsr305 3.0.2
    }
}
module com.azure.core {
    requires transitive com.azure.json;
    requires transitive com.azure.xml;
    requires transitive reactor.core;
    requires transitive org.reactivestreams;
    requires transitive org.slf4j;
    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.fasterxml.jackson.datatype.jsr310;
    exports com.azure.core.annotation;
    exports com.azure.core.client.traits;
    exports com.azure.core.credential;
    exports com.azure.core.cryptography;
    exports com.azure.core.exception;
    exports com.azure.core.http;
    exports com.azure.core.http.policy;
    exports com.azure.core.http.rest;
    exports com.azure.core.models;
    exports com.azure.core.util;
    exports com.azure.core.util.builder;
    exports com.azure.core.util.io;
    exports com.azure.core.util.logging;
    exports com.azure.core.util.paging;
    exports com.azure.core.util.polling;
    exports com.azure.core.util.serializer;
    exports com.azure.core.util.tracing;
    exports com.azure.core.util.metrics;
    exports com.azure.core.implementation to com.azure.core.serializer.json.jackson, com.azure.core.serializer.json.gson, com.azure.core.experimental, com.azure.core.http.vertx;
    exports com.azure.core.implementation.jackson to com.azure.core.management, com.azure.core.serializer.json.jackson;
    exports com.azure.core.implementation.util to com.azure.http.netty, com.azure.core.http.okhttp, com.azure.core.http.jdk.httpclient, com.azure.core.http.vertx, com.azure.core.serializer.json.jackson;
    exports com.azure.core.util.polling.implementation to com.azure.core.experimental;
    opens com.azure.core.credential to com.fasterxml.jackson.databind;
    opens com.azure.core.http to com.fasterxml.jackson.databind;
    opens com.azure.core.models to com.fasterxml.jackson.databind;
    opens com.azure.core.util to com.fasterxml.jackson.databind;
    opens com.azure.core.util.logging to com.fasterxml.jackson.databind;
    opens com.azure.core.util.polling to com.fasterxml.jackson.databind;
    opens com.azure.core.util.polling.implementation to com.fasterxml.jackson.databind;
    opens com.azure.core.util.serializer to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation.logging to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation.serializer to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation.jackson to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation.util to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation.http.rest to com.fasterxml.jackson.databind;
    opens com.azure.core.http.rest to com.fasterxml.jackson.databind;
    uses com.azure.core.http.HttpClientProvider;
    uses com.azure.core.http.policy.BeforeRetryPolicyProvider;
    uses com.azure.core.http.policy.AfterRetryPolicyProvider;
    uses com.azure.core.util.serializer.JsonSerializerProvider;
    uses com.azure.core.util.serializer.MemberNameConverterProvider;
    uses com.azure.core.util.tracing.Tracer;
    uses com.azure.core.util.metrics.MeterProvider;
    uses com.azure.core.util.tracing.TracerProvider;
}
package com.azure.core.annotation {
    @Retention(RUNTIME)
    @Target(PARAMETER)
    public @annotation BodyParam {
        String value()
    }
    @Retention(RUNTIME)
    @Target(METHOD)
    public @annotation Delete {
        String value()
    }
    @Retention(RUNTIME)
    @Target(METHOD)
    public @annotation ExpectedResponses {
        int[] value()
    }
    @Retention(SOURCE)
    @Target(TYPE)
    public @annotation Fluent {
    }
    @Retention(RUNTIME)
    @Target(PARAMETER)
    public @annotation FormParam {
        String value()
        boolean encoded() default false
    }
    @Retention(SOURCE)
    @Target({ METHOD, CONSTRUCTOR, FIELD })
    public @annotation Generated {
    }
    @Retention(RUNTIME)
    @Target(METHOD)
    public @annotation Get {
        String value()
    }
    @Retention(RUNTIME)
    @Target(METHOD)
    public @annotation Head {
        String value()
    }
    @Retention(RUNTIME)
    @Target(FIELD)
    public @annotation HeaderCollection {
        String value()
    }
    @Retention(RUNTIME)
    @Target(PARAMETER)
    public @annotation HeaderParam {
        String value()
    }
    @Retention(RUNTIME)
    @Target(METHOD)
    public @annotation Headers {
        String[] value()
    }
    @Retention(RUNTIME)
    @Target(TYPE)
    public @annotation Host {
        String value() default ""
    }
    @Retention(RUNTIME)
    @Target(PARAMETER)
    public @annotation HostParam {
        String value()
        boolean encoded() default true
    }
    @Retention(SOURCE)
    @Target(TYPE)
    public @annotation Immutable {
    }
    @Retention(RUNTIME)
    @Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.FIELD })
    public @annotation JsonFlatten {
    }
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @annotation Options {
        String value()
    }
    @Retention(RUNTIME)
    @Target(METHOD)
    public @annotation Patch {
        String value()
    }
    @Retention(RUNTIME)
    @Target(PARAMETER)
    public @annotation PathParam {
        String value()
        boolean encoded() default false
    }
    @Retention(RUNTIME)
    @Target(METHOD)
    public @annotation Post {
        String value()
    }
    @Retention(RUNTIME)
    @Target(METHOD)
    public @annotation Put {
        String value()
    }
    @Retention(RUNTIME)
    @Target(PARAMETER)
    public @annotation QueryParam {
        String value()
        boolean encoded() default false
        boolean multipleQueryParams() default false
    }
    @Deprecated
    @Retention(RUNTIME)
    @Target(METHOD)
    public @annotation ResumeOperation {
    }
    public enum ReturnType {
        SINGLE,
        COLLECTION,
        LONG_RUNNING_OPERATION;
    }
    @Retention(RUNTIME)
    @Target(METHOD)
    public @annotation ReturnValueWireType {
        Class<?> value()
    }
    @Retention(CLASS)
    @Target(TYPE)
    public @annotation ServiceClient {
        Class<?> builder()
        boolean isAsync() default false
        Class<?>[] serviceInterfaces() default {  }
    }
    @Retention(RUNTIME)
    @Target(TYPE)
    public @annotation ServiceClientBuilder {
        Class<?>[] serviceClients()
        ServiceClientProtocol protocol() default ServiceClientProtocol.HTTP
    }
    public enum ServiceClientProtocol {
        HTTP,
        AMQP;
    }
    @Retention(RUNTIME)
    @Target(TYPE)
    public @annotation ServiceInterface {
        String name()
    }
    @Retention(CLASS)
    @Target(METHOD)
    public @annotation ServiceMethod {
        ReturnType returns()
    }
    @Repeatable(UnexpectedResponseExceptionTypes)
    @Retention(RUNTIME)
    @Target(METHOD)
    public @annotation UnexpectedResponseExceptionType {
        Class<? extends HttpResponseException> value()
        int[] code() default {  }
    }
    @Retention(RUNTIME)
    @Target(METHOD)
    public @annotation UnexpectedResponseExceptionTypes {
        UnexpectedResponseExceptionType[] value()
    }
}
package com.azure.core.client.traits {
    public interface AzureKeyCredentialTrait<T extends AzureKeyCredentialTrait<T>> {
        T credential(AzureKeyCredential credential)
    }
    public interface AzureNamedKeyCredentialTrait<T extends AzureNamedKeyCredentialTrait<T>> {
        T credential(AzureNamedKeyCredential credential)
    }
    public interface AzureSasCredentialTrait<T extends AzureSasCredentialTrait<T>> {
        T credential(AzureSasCredential credential)
    }
    public interface ConfigurationTrait<T extends ConfigurationTrait<T>> {
        T configuration(Configuration configuration)
    }
    public interface ConnectionStringTrait<T extends ConnectionStringTrait<T>> {
        T connectionString(String connectionString)
    }
    public interface EndpointTrait<T extends EndpointTrait<T>> {
        T endpoint(String endpoint)
    }
    public interface HttpTrait<T extends HttpTrait<T>> {
        T addPolicy(HttpPipelinePolicy pipelinePolicy)
        T clientOptions(ClientOptions clientOptions)
        T httpClient(HttpClient httpClient)
        T httpLogOptions(HttpLogOptions logOptions)
        T pipeline(HttpPipeline pipeline)
        T retryOptions(RetryOptions retryOptions)
    }
    public interface KeyCredentialTrait<T> {
        T credential(KeyCredential credential)
    }
    public interface TokenCredentialTrait<T extends TokenCredentialTrait<T>> {
        T credential(TokenCredential credential)
    }
}
package com.azure.core.credential {
    public class AccessToken {
        public AccessToken(String token, OffsetDateTime expiresAt)
        public AccessToken(String token, OffsetDateTime expiresAt, OffsetDateTime refreshAt)
        public AccessToken(String token, OffsetDateTime expiresAt, OffsetDateTime refreshAt, String tokenType)
        public Duration getDurationUntilExpiration()
        public boolean isExpired()
        public OffsetDateTime getExpiresAt()
        public OffsetDateTime getRefreshAt()
        public String getToken()
        public String getTokenType()
    }
    public final class AccessTokenCache {
        public AccessTokenCache(TokenCredential tokenCredential)
        public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext, boolean refreshOnContextChange)
        public AccessToken getTokenSync(TokenRequestContext tokenRequestContext, boolean refreshOnContextChange)
    }
    public final class AzureKeyCredential extends KeyCredential {
        public AzureKeyCredential(String key)
        @Override public AzureKeyCredential update(String key)
    }
    @Immutable
    public final class AzureNamedKey {
        public String getKey()
        public String getName()
    }
    public final class AzureNamedKeyCredential {
        public AzureNamedKeyCredential(String name, String key)
        public AzureNamedKey getAzureNamedKey()
        public AzureNamedKeyCredential update(String name, String key)
    }
    public final class AzureSasCredential {
        public AzureSasCredential(String signature)
        public AzureSasCredential(String signature, Function<String, String> signatureEncoder)
        public String getSignature()
        public AzureSasCredential update(String signature)
    }
    public class BasicAuthenticationCredential implements TokenCredential {
        public BasicAuthenticationCredential(String username, String password)
        @Override public Mono<AccessToken> getToken(TokenRequestContext request)
        @Override public AccessToken getTokenSync(TokenRequestContext request)
    }
    public class KeyCredential {
        public KeyCredential(String key)
        public String getKey()
        public KeyCredential update(String key)
    }
    public class ProofOfPossessionOptions {
        public ProofOfPossessionOptions()
        public String getProofOfPossessionNonce()
        public ProofOfPossessionOptions setProofOfPossessionNonce(String proofOfPossessionNonce)
        public HttpMethod getRequestMethod()
        public ProofOfPossessionOptions setRequestMethod(HttpMethod requestMethod)
        public URL getRequestUrl()
        public ProofOfPossessionOptions setRequestUrl(URL requestUrl)
    }
    public class SimpleTokenCache {
        public SimpleTokenCache(Supplier<Mono<AccessToken>> tokenSupplier)
        public Mono<AccessToken> getToken()
    }
    @FunctionalInterface
    public interface TokenCredential {
        Mono<AccessToken> getToken(TokenRequestContext request)
        default AccessToken getTokenSync(TokenRequestContext request)
    }
    public class TokenRequestContext {
        public TokenRequestContext()
        public TokenRequestContext addScopes(String... scopes)
        public boolean isCaeEnabled()
        public TokenRequestContext setCaeEnabled(boolean enableCae)
        public String getClaims()
        public TokenRequestContext setClaims(String claims)
        public ProofOfPossessionOptions getProofOfPossessionOptions()
        public TokenRequestContext setProofOfPossessionOptions(ProofOfPossessionOptions proofOfPossessionOptions)
        public List<String> getScopes()
        public TokenRequestContext setScopes(List<String> scopes)
        public String getTenantId()
        public TokenRequestContext setTenantId(String tenantId)
    }
}
package com.azure.core.cryptography {
    public interface AsyncKeyEncryptionKey {
        Mono<String> getKeyId()
        Mono<byte[]> unwrapKey(String algorithm, byte[] encryptedKey)
        Mono<byte[]> wrapKey(String algorithm, byte[] key)
    }
    public interface AsyncKeyEncryptionKeyResolver {
        Mono<? extends AsyncKeyEncryptionKey> buildAsyncKeyEncryptionKey(String keyId)
    }
    public interface KeyEncryptionKey {
        String getKeyId()
        byte[] unwrapKey(String algorithm, byte[] encryptedKey)
        byte[] wrapKey(String algorithm, byte[] key)
    }
    public interface KeyEncryptionKeyResolver {
        KeyEncryptionKey buildKeyEncryptionKey(String keyId)
    }
}
package com.azure.core.exception {
    public class AzureException extends RuntimeException {
        public AzureException()
        public AzureException(String message)
        public AzureException(Throwable cause)
        public AzureException(String message, Throwable cause)
        public AzureException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    }
    public class ClientAuthenticationException extends HttpResponseException {
        public ClientAuthenticationException(String message, HttpResponse response)
        public ClientAuthenticationException(String message, HttpResponse response, Object value)
        public ClientAuthenticationException(String message, HttpResponse response, Throwable cause)
    }
    public class DecodeException extends HttpResponseException {
        public DecodeException(String message, HttpResponse response)
        public DecodeException(String message, HttpResponse response, Object value)
        public DecodeException(String message, HttpResponse response, Throwable cause)
    }
    public class HttpRequestException extends AzureException {
        public HttpRequestException(HttpRequest request)
        public HttpRequestException(String message, HttpRequest request)
        public HttpRequestException(HttpRequest request, Throwable cause)
        public HttpRequestException(String message, HttpRequest request, Throwable cause)
        public HttpRequestException(String message, HttpRequest request, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
        public HttpRequest getRequest()
    }
    public class HttpResponseException extends AzureException {
        public HttpResponseException(HttpResponse response)
        public HttpResponseException(String message, HttpResponse response)
        public HttpResponseException(HttpResponse response, Throwable cause)
        public HttpResponseException(String message, HttpResponse response, Object value)
        public HttpResponseException(String message, HttpResponse response, Throwable cause)
        public HttpResponseException(String message, HttpResponse response, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
        public HttpResponse getResponse()
        public Object getValue()
    }
    public class ResourceExistsException extends HttpResponseException {
        public ResourceExistsException(String message, HttpResponse response)
        public ResourceExistsException(String message, HttpResponse response, Object value)
        public ResourceExistsException(String message, HttpResponse response, Throwable cause)
    }
    public class ResourceModifiedException extends HttpResponseException {
        public ResourceModifiedException(String message, HttpResponse response)
        public ResourceModifiedException(String message, HttpResponse response, Object value)
        public ResourceModifiedException(String message, HttpResponse response, Throwable cause)
    }
    public class ResourceNotFoundException extends HttpResponseException {
        public ResourceNotFoundException(String message, HttpResponse response)
        public ResourceNotFoundException(String message, HttpResponse response, Object value)
        public ResourceNotFoundException(String message, HttpResponse response, Throwable cause)
    }
    public class ServiceResponseException extends AzureException {
        public ServiceResponseException(String message)
        public ServiceResponseException(String message, Throwable cause)
    }
    public class TooManyRedirectsException extends HttpResponseException {
        public TooManyRedirectsException(String message, HttpResponse response)
        public TooManyRedirectsException(String message, HttpResponse response, Object value)
        public TooManyRedirectsException(String message, HttpResponse response, Throwable cause)
    }
    public final class UnexpectedLengthException extends IllegalStateException {
        public UnexpectedLengthException(String message, long bytesRead, long bytesExpected)
        public long getBytesExpected()
        public long getBytesRead()
    }
}
package com.azure.core.http {
    public final class ContentType {
        public static final String APPLICATION_JSON = "application/json";
        public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
        public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    }
    @Immutable
    public final class HttpAuthorization {
        public HttpAuthorization(String scheme, String parameter)
        public String getParameter()
        public String getScheme()
        @Override public String toString()
    }
    public interface HttpClient {
        static HttpClient createDefault()
        static HttpClient createDefault(HttpClientOptions clientOptions)
        Mono<HttpResponse> send(HttpRequest request)
        default Mono<HttpResponse> send(HttpRequest request, Context context)
        default HttpResponse sendSync(HttpRequest request, Context context)
    }
    @FunctionalInterface
    public interface HttpClientProvider {
        HttpClient createInstance()
        default HttpClient createInstance(HttpClientOptions clientOptions)
    }
    public class HttpHeader extends Header {
        public HttpHeader(String name, String value)
        public HttpHeader(String name, List<String> values)
    }
    public final class HttpHeaderName extends ExpandableStringEnum<HttpHeaderName> {
        public static final HttpHeaderName ACCEPT = fromString("Accept");
        public static final HttpHeaderName ACCEPT_CHARSET = fromString("Accept-Charset");
        public static final HttpHeaderName ACCESS_CONTROL_ALLOW_CREDENTIALS = fromString("Access-Control-Allow-Credentials");
        public static final HttpHeaderName ACCESS_CONTROL_ALLOW_HEADERS = fromString("Access-Control-Allow-Headers");
        public static final HttpHeaderName ACCESS_CONTROL_ALLOW_METHODS = fromString("Access-Control-Allow-Methods");
        public static final HttpHeaderName ACCESS_CONTROL_ALLOW_ORIGIN = fromString("Access-Control-Allow-Origin");
        public static final HttpHeaderName ACCESS_CONTROL_EXPOSE_HEADERS = fromString("Access-Control-Expose-Headers");
        public static final HttpHeaderName ACCESS_CONTROL_MAX_AGE = fromString("Access-Control-Max-Age");
        public static final HttpHeaderName ACCEPT_DATETIME = fromString("Accept-Datetime");
        public static final HttpHeaderName ACCEPT_ENCODING = fromString("Accept-Encoding");
        public static final HttpHeaderName ACCEPT_LANGUAGE = fromString("Accept-Language");
        public static final HttpHeaderName ACCEPT_PATCH = fromString("Accept-Patch");
        public static final HttpHeaderName ACCEPT_RANGES = fromString("Accept-Ranges");
        public static final HttpHeaderName AGE = fromString("Age");
        public static final HttpHeaderName ALLOW = fromString("Allow");
        public static final HttpHeaderName AUTHORIZATION = fromString("Authorization");
        public static final HttpHeaderName AZURE_ASYNCOPERATION = fromString("Azure-AsyncOperation");
        public static final HttpHeaderName CACHE_CONTROL = fromString("Cache-Control");
        public static final HttpHeaderName CONNECTION = fromString("Connection");
        public static final HttpHeaderName CONTENT_DISPOSITION = fromString("Content-Disposition");
        public static final HttpHeaderName CONTENT_ENCODING = fromString("Content-Encoding");
        public static final HttpHeaderName CONTENT_LANGUAGE = fromString("Content-Language");
        public static final HttpHeaderName CONTENT_LENGTH = fromString("Content-Length");
        public static final HttpHeaderName CONTENT_LOCATION = fromString("Content-Location");
        public static final HttpHeaderName CONTENT_MD5 = fromString("Content-MD5");
        public static final HttpHeaderName CONTENT_RANGE = fromString("Content-Range");
        public static final HttpHeaderName CONTENT_TYPE = fromString("Content-Type");
        public static final HttpHeaderName COOKIE = fromString("Cookie");
        public static final HttpHeaderName DATE = fromString("Date");
        public static final HttpHeaderName ETAG = fromString("ETag");
        public static final HttpHeaderName EXPECT = fromString("Expect");
        public static final HttpHeaderName EXPIRES = fromString("Expires");
        public static final HttpHeaderName FORWARDED = fromString("Forwarded");
        public static final HttpHeaderName FROM = fromString("From");
        public static final HttpHeaderName HOST = fromString("Host");
        public static final HttpHeaderName HTTP2_SETTINGS = fromString("HTTP2-Settings");
        public static final HttpHeaderName IF_MATCH = fromString("If-Match");
        public static final HttpHeaderName IF_MODIFIED_SINCE = fromString("If-Modified-Since");
        public static final HttpHeaderName IF_NONE_MATCH = fromString("If-None-Match");
        public static final HttpHeaderName IF_RANGE = fromString("If-Range");
        public static final HttpHeaderName IF_UNMODIFIED_SINCE = fromString("If-Unmodified-Since");
        public static final HttpHeaderName LAST_MODIFIED = fromString("Last-Modified");
        public static final HttpHeaderName LINK = fromString("Link");
        public static final HttpHeaderName LOCATION = fromString("Location");
        public static final HttpHeaderName MAX_FORWARDS = fromString("Max-Forwards");
        public static final HttpHeaderName OPERATION_LOCATION = fromString("Operation-Location");
        public static final HttpHeaderName ORIGIN = fromString("Origin");
        public static final HttpHeaderName PRAGMA = fromString("Pragma");
        public static final HttpHeaderName PREFER = fromString("Prefer");
        public static final HttpHeaderName PREFERENCE_APPLIED = fromString("Preference-Applied");
        public static final HttpHeaderName PROXY_AUTHENTICATE = fromString("Proxy-Authenticate");
        public static final HttpHeaderName PROXY_AUTHORIZATION = fromString("Proxy-Authorization");
        public static final HttpHeaderName RANGE = fromString("Range");
        public static final HttpHeaderName REFERER = fromString("Referer");
        public static final HttpHeaderName RETRY_AFTER = fromString("Retry-After");
        public static final HttpHeaderName RETRY_AFTER_MS = fromString("retry-after-ms");
        public static final HttpHeaderName SERVER = fromString("Server");
        public static final HttpHeaderName SET_COOKIE = fromString("Set-Cookie");
        public static final HttpHeaderName STRICT_TRANSPORT_SECURITY = fromString("Strict-Transport-Security");
        public static final HttpHeaderName TE = fromString("TE");
        public static final HttpHeaderName TRAILER = fromString("Trailer");
        public static final HttpHeaderName TRANSFER_ENCODING = fromString("Transfer-Encoding");
        public static final HttpHeaderName USER_AGENT = fromString("User-Agent");
        public static final HttpHeaderName UPGRADE = fromString("Upgrade");
        public static final HttpHeaderName VARY = fromString("Vary");
        public static final HttpHeaderName VIA = fromString("Via");
        public static final HttpHeaderName WARNING = fromString("Warning");
        public static final HttpHeaderName WWW_AUTHENTICATE = fromString("WWW-Authenticate");
        public static final HttpHeaderName X_MS_CLIENT_ID = fromString("x-ms-client-id");
        public static final HttpHeaderName X_MS_CLIENT_REQUEST_ID = fromString("x-ms-client-request-id");
        public static final HttpHeaderName X_MS_DATE = fromString("x-ms-date");
        public static final HttpHeaderName X_MS_REQUEST_ID = fromString("x-ms-request-id");
        public static final HttpHeaderName X_MS_RETRY_AFTER_MS = fromString("x-ms-retry-after-ms");
        public static final HttpHeaderName TRACEPARENT = fromString("traceparent");
        @Deprecated public HttpHeaderName()
        public String getCaseInsensitiveName()
        public String getCaseSensitiveName()
        @Override public boolean equals(Object obj)
        public static HttpHeaderName fromString(String name)
        @Override public int hashCode()
    }
    public class HttpHeaders implements Iterable<HttpHeader> {
        public HttpHeaders()
        public HttpHeaders(Map<String, String> headers)
        public HttpHeaders(Iterable<HttpHeader> headers)
        public HttpHeaders(int initialCapacity)
        @Deprecated public HttpHeader get(String name)
        public HttpHeader get(HttpHeaderName name)
        @Deprecated public HttpHeaders set(String name, String value)
        public HttpHeaders set(HttpHeaderName name, String value)
        @Deprecated public HttpHeaders set(String name, List<String> values)
        public HttpHeaders set(HttpHeaderName name, List<String> values)
        @Deprecated public HttpHeaders add(String name, String value)
        public HttpHeaders add(HttpHeaderName name, String value)
        public HttpHeaders setAll(Map<String, List<String>> headers)
        public HttpHeaders setAllHttpHeaders(HttpHeaders headers)
        @Override public Iterator<HttpHeader> iterator()
        @Deprecated public HttpHeaders put(String name, String value)
        @Deprecated public HttpHeader remove(String name)
        public HttpHeader remove(HttpHeaderName name)
        public int getSize()
        public Stream<HttpHeader> stream()
        public Map<String, String> toMap()
        @Override public String toString()
        @Deprecated public String getValue(String name)
        public String getValue(HttpHeaderName name)
        @Deprecated public String[] getValues(String name)
        public String[] getValues(HttpHeaderName name)
    }
    public enum HttpMethod {
        GET,
        PUT,
        POST,
        PATCH,
        DELETE,
        HEAD,
        OPTIONS,
        TRACE,
        CONNECT;
    }
    public final class HttpPipeline {
        public HttpClient getHttpClient()
        public HttpPipelinePolicy getPolicy(int index)
        public int getPolicyCount()
        public Mono<HttpResponse> send(HttpRequest request)
        public Mono<HttpResponse> send(HttpPipelineCallContext context)
        public Mono<HttpResponse> send(HttpRequest request, Context data)
        public HttpResponse sendSync(HttpRequest request, Context data)
        public Tracer getTracer()
    }
    public class HttpPipelineBuilder {
        public HttpPipelineBuilder()
        public HttpPipelineBuilder clientOptions(ClientOptions clientOptions)
        public HttpPipelineBuilder httpClient(HttpClient httpClient)
        public HttpPipelineBuilder policies(HttpPipelinePolicy... policies)
        public HttpPipelineBuilder tracer(Tracer tracer)
        public HttpPipeline build()
    }
    public final class HttpPipelineCallContext {
        public Context getContext()
        public Optional<Object> getData(String key)
        public void setData(String key, Object value)
        public HttpRequest getHttpRequest()
        public HttpPipelineCallContext setHttpRequest(HttpRequest request)
    }
    public class HttpPipelineNextPolicy {
        @Override public HttpPipelineNextPolicy clone()
        public Mono<HttpResponse> process()
    }
    public class HttpPipelineNextSyncPolicy {
        @Override public HttpPipelineNextSyncPolicy clone()
        public HttpResponse processSync()
    }
    public enum HttpPipelinePosition {
        PER_CALL,
        PER_RETRY;
    }
    @Immutable
    public final class HttpRange {
        public HttpRange(long offset)
        public HttpRange(long offset, Long length)
        @Override public boolean equals(Object obj)
        @Override public int hashCode()
        public Long getLength()
        public long getOffset()
        @Override public String toString()
    }
    public class HttpRequest {
        public HttpRequest(HttpMethod httpMethod, URL url)
        public HttpRequest(HttpMethod httpMethod, String url)
        public HttpRequest(HttpMethod httpMethod, URL url, HttpHeaders headers)
        public HttpRequest(HttpMethod httpMethod, URL url, HttpHeaders headers, Flux<ByteBuffer> body)
        public HttpRequest(HttpMethod httpMethod, URL url, HttpHeaders headers, BinaryData body)
        public Flux<ByteBuffer> getBody()
        public HttpRequest setBody(String content)
        public HttpRequest setBody(byte[] content)
        public HttpRequest setBody(Flux<ByteBuffer> content)
        public HttpRequest setBody(BinaryData content)
        public BinaryData getBodyAsBinaryData()
        public HttpRequest copy()
        @Deprecated public HttpRequest setHeader(String name, String value)
        public HttpRequest setHeader(HttpHeaderName headerName, String value)
        public HttpHeaders getHeaders()
        public HttpRequest setHeaders(HttpHeaders headers)
        public HttpMethod getHttpMethod()
        public HttpRequest setHttpMethod(HttpMethod httpMethod)
        public URL getUrl()
        public HttpRequest setUrl(URL url)
        public HttpRequest setUrl(String url)
    }
    public abstract class HttpResponse implements Closeable {
        protected HttpResponse(HttpRequest request)
        public abstract Flux<ByteBuffer> getBody()
        public BinaryData getBodyAsBinaryData()
        public abstract Mono<byte[]> getBodyAsByteArray()
        public Mono<InputStream> getBodyAsInputStream()
        public InputStream getBodyAsInputStreamSync()
        public abstract Mono<String> getBodyAsString()
        public abstract Mono<String> getBodyAsString(Charset charset)
        public HttpResponse buffer()
        @Override public void close()
        public abstract HttpHeaders getHeaders()
        @Deprecated public abstract String getHeaderValue(String name)
        public String getHeaderValue(HttpHeaderName headerName)
        public final HttpRequest getRequest()
        public abstract int getStatusCode()
        public void writeBodyTo(WritableByteChannel channel) throws IOException
        public Mono<Void> writeBodyToAsync(AsynchronousByteChannel channel)
    }
    @Fluent
    public class MatchConditions {
        public MatchConditions()
        public String getIfMatch()
        public MatchConditions setIfMatch(String ifMatch)
        public String getIfNoneMatch()
        public MatchConditions setIfNoneMatch(String ifNoneMatch)
    }
    public class ProxyOptions {
        public ProxyOptions(Type type, InetSocketAddress address)
        public InetSocketAddress getAddress()
        public ProxyOptions setCredentials(String username, String password)
        public static ProxyOptions fromConfiguration(Configuration configuration)
        public static ProxyOptions fromConfiguration(Configuration configuration, boolean createUnresolved)
        public String getNonProxyHosts()
        public ProxyOptions setNonProxyHosts(String nonProxyHosts)
        public String getPassword()
        public Type getType()
        public String getUsername()
        public enum Type {
            HTTP(Proxy.Type.HTTP),
            SOCKS4(Proxy.Type.SOCKS),
            SOCKS5(Proxy.Type.SOCKS);
            public Proxy.Type toProxyType()
        }
    }
    @Fluent
    public class RequestConditions extends MatchConditions {
        public RequestConditions()
        @Override public RequestConditions setIfMatch(String ifMatch)
        public OffsetDateTime getIfModifiedSince()
        public RequestConditions setIfModifiedSince(OffsetDateTime ifModifiedSince)
        @Override public RequestConditions setIfNoneMatch(String ifNoneMatch)
        public OffsetDateTime getIfUnmodifiedSince()
        public RequestConditions setIfUnmodifiedSince(OffsetDateTime ifUnmodifiedSince)
    }
}
package com.azure.core.http.policy {
    public class AddDatePolicy implements HttpPipelinePolicy {
        public AddDatePolicy()
        @Override public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next)
        @Override public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next)
    }
    public class AddHeadersFromContextPolicy implements HttpPipelinePolicy {
        public static final String AZURE_REQUEST_HTTP_HEADERS_KEY = "azure-http-headers-key";
        public AddHeadersFromContextPolicy()
        @Override public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next)
        @Override public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next)
    }
    public class AddHeadersPolicy implements HttpPipelinePolicy {
        public AddHeadersPolicy(HttpHeaders headers)
        @Override public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next)
        @Override public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next)
    }
    public interface AfterRetryPolicyProvider extends HttpPolicyProvider {
    }
    public final class AzureKeyCredentialPolicy extends KeyCredentialPolicy {
        public AzureKeyCredentialPolicy(String name, AzureKeyCredential credential)
        public AzureKeyCredentialPolicy(String name, AzureKeyCredential credential, String prefix)
    }
    public final class AzureSasCredentialPolicy implements HttpPipelinePolicy {
        public AzureSasCredentialPolicy(AzureSasCredential credential)
        public AzureSasCredentialPolicy(AzureSasCredential credential, boolean requireHttps)
        @Override public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next)
        @Override public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next)
    }
    public class BearerTokenAuthenticationPolicy implements HttpPipelinePolicy {
        public BearerTokenAuthenticationPolicy(TokenCredential credential, String... scopes)
        public Mono<Void> setAuthorizationHeader(HttpPipelineCallContext context, TokenRequestContext tokenRequestContext)
        public void setAuthorizationHeaderSync(HttpPipelineCallContext context, TokenRequestContext tokenRequestContext)
        public Mono<Void> authorizeRequest(HttpPipelineCallContext context)
        public Mono<Boolean> authorizeRequestOnChallenge(HttpPipelineCallContext context, HttpResponse response)
        public boolean authorizeRequestOnChallengeSync(HttpPipelineCallContext context, HttpResponse response)
        public void authorizeRequestSync(HttpPipelineCallContext context)
        @Override public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next)
        @Override public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next)
    }
    public interface BeforeRetryPolicyProvider extends HttpPolicyProvider {
    }
    public class CookiePolicy implements HttpPipelinePolicy {
        public CookiePolicy()
        @Override public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next)
        @Override public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next)
    }
    public final class DefaultRedirectStrategy implements RedirectStrategy {
        public DefaultRedirectStrategy()
        public DefaultRedirectStrategy(int maxAttempts)
        public DefaultRedirectStrategy(int maxAttempts, String locationHeader, Set<HttpMethod> allowedMethods)
        @Override public HttpRequest createRedirectRequest(HttpResponse httpResponse)
        @Override public int getMaxAttempts()
        @Override public boolean shouldAttemptRedirect(HttpPipelineCallContext context, HttpResponse httpResponse, int tryCount, Set<String> attemptedRedirectUrls)
    }
    public class ExponentialBackoff implements RetryStrategy {
        public ExponentialBackoff()
        public ExponentialBackoff(ExponentialBackoffOptions options)
        public ExponentialBackoff(int maxRetries, Duration baseDelay, Duration maxDelay)
        @Override public Duration calculateRetryDelay(int retryAttempts)
        @Override public int getMaxRetries()
        @Override public boolean shouldRetryCondition(RequestRetryCondition requestRetryCondition)
    }
    public class ExponentialBackoffOptions {
        public ExponentialBackoffOptions()
        public Duration getBaseDelay()
        public ExponentialBackoffOptions setBaseDelay(Duration baseDelay)
        public Duration getMaxDelay()
        public ExponentialBackoffOptions setMaxDelay(Duration maxDelay)
        public Integer getMaxRetries()
        public ExponentialBackoffOptions setMaxRetries(Integer maxRetries)
    }
    public class FixedDelay implements RetryStrategy {
        public FixedDelay(FixedDelayOptions fixedDelayOptions)
        public FixedDelay(int maxRetries, Duration delay)
        @Override public Duration calculateRetryDelay(int retryAttempts)
        @Override public int getMaxRetries()
        @Override public boolean shouldRetryCondition(RequestRetryCondition requestRetryCondition)
    }
    public class FixedDelayOptions {
        public FixedDelayOptions(int maxRetries, Duration delay)
        public Duration getDelay()
        public int getMaxRetries()
    }
    public class HostPolicy implements HttpPipelinePolicy {
        public HostPolicy(String host)
        @Override public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next)
        @Override public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next)
    }
    public enum HttpLogDetailLevel {
        NONE,
        BASIC,
        HEADERS,
        BODY,
        BODY_AND_HEADERS;
        public boolean shouldLogBody()
        public boolean shouldLogHeaders()
        public boolean shouldLogUrl()
    }
    public class HttpLogOptions {
        public HttpLogOptions()
        @Deprecated public HttpLogOptions addAllowedHeaderName(String allowedHeaderName)
        public HttpLogOptions addAllowedHttpHeaderName(HttpHeaderName allowedHeaderName)
        public HttpLogOptions addAllowedQueryParamName(String allowedQueryParamName)
        @Deprecated public Set<String> getAllowedHeaderNames()
        @Deprecated public HttpLogOptions setAllowedHeaderNames(Set<String> allowedHeaderNames)
        public Set<HttpHeaderName> getAllowedHttpHeaderNames()
        public HttpLogOptions setAllowedHttpHeaderNames(Set<HttpHeaderName> allowedHttpHeaderNames)
        public Set<String> getAllowedQueryParamNames()
        public HttpLogOptions setAllowedQueryParamNames(Set<String> allowedQueryParamNames)
        @Deprecated public String getApplicationId()
        @Deprecated public HttpLogOptions setApplicationId(String applicationId)
        public HttpLogOptions disableRedactedHeaderLogging(boolean disableRedactedHeaderLogging)
        public HttpLogDetailLevel getLogLevel()
        public HttpLogOptions setLogLevel(HttpLogDetailLevel logLevel)
        @Deprecated public boolean isPrettyPrintBody()
        @Deprecated public HttpLogOptions setPrettyPrintBody(boolean prettyPrintBody)
        public boolean isRedactedHeaderLoggingDisabled()
        public HttpRequestLogger getRequestLogger()
        public HttpLogOptions setRequestLogger(HttpRequestLogger requestLogger)
        public HttpResponseLogger getResponseLogger()
        public HttpLogOptions setResponseLogger(HttpResponseLogger responseLogger)
    }
    public class HttpLoggingPolicy implements HttpPipelinePolicy {
        public static final String RETRY_COUNT_CONTEXT = "requestRetryCount";
        public HttpLoggingPolicy(HttpLogOptions httpLogOptions)
        @Override public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next)
        @Override public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next)
    }
    @FunctionalInterface
    public interface HttpPipelinePolicy {
        default HttpPipelinePosition getPipelinePosition()
        Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next)
        default HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next)
    }
    public class HttpPipelineSyncPolicy implements HttpPipelinePolicy {
        public HttpPipelineSyncPolicy()
        protected HttpResponse afterReceivedResponse(HttpPipelineCallContext context, HttpResponse response)
        protected void beforeSendingRequest(HttpPipelineCallContext context)
        @Override public final Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next)
        @Override public final HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next)
    }
    public interface HttpPolicyProvider {
        HttpPipelinePolicy create()
    }
    public final class HttpPolicyProviders {
        public static void addAfterRetryPolicies(List<HttpPipelinePolicy> policies)
        public static void addBeforeRetryPolicies(List<HttpPipelinePolicy> policies)
    }
    @FunctionalInterface
    public interface HttpRequestLogger {
        default LogLevel getLogLevel(HttpRequestLoggingContext loggingOptions)
        Mono<Void> logRequest(ClientLogger logger, HttpRequestLoggingContext loggingOptions)
        default void logRequestSync(ClientLogger logger, HttpRequestLoggingContext loggingOptions)
    }
    public final class HttpRequestLoggingContext {
        public Context getContext()
        public HttpRequest getHttpRequest()
        public Integer getTryCount()
    }
    @FunctionalInterface
    public interface HttpResponseLogger {
        default LogLevel getLogLevel(HttpResponseLoggingContext loggingOptions)
        Mono<HttpResponse> logResponse(ClientLogger logger, HttpResponseLoggingContext loggingOptions)
        default HttpResponse logResponseSync(ClientLogger logger, HttpResponseLoggingContext loggingOptions)
    }
    public final class HttpResponseLoggingContext {
        public Context getContext()
        public HttpResponse getHttpResponse()
        public Duration getResponseDuration()
        public Integer getTryCount()
    }
    public class KeyCredentialPolicy implements HttpPipelinePolicy {
        public KeyCredentialPolicy(String name, KeyCredential credential)
        public KeyCredentialPolicy(String name, KeyCredential credential, String prefix)
        @Override public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next)
        @Override public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next)
    }
    public class PortPolicy implements HttpPipelinePolicy {
        public PortPolicy(int port, boolean overwrite)
        @Override public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next)
        @Override public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next)
    }
    public class ProtocolPolicy implements HttpPipelinePolicy {
        public ProtocolPolicy(String protocol, boolean overwrite)
        @Override public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next)
        @Override public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next)
    }
    public final class RedirectPolicy implements HttpPipelinePolicy {
        public RedirectPolicy()
        public RedirectPolicy(RedirectStrategy redirectStrategy)
        @Override public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next)
        @Override public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next)
    }
    public interface RedirectStrategy {
        HttpRequest createRedirectRequest(HttpResponse httpResponse)
        int getMaxAttempts()
        boolean shouldAttemptRedirect(HttpPipelineCallContext context, HttpResponse httpResponse, int tryCount, Set<String> attemptedRedirectUrls)
    }
    public class RequestIdPolicy implements HttpPipelinePolicy {
        public RequestIdPolicy()
        public RequestIdPolicy(String requestIdHeaderName)
        @Override public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next)
        @Override public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next)
    }
    public final class RequestRetryCondition {
        public HttpResponse getResponse()
        public List<Throwable> getRetriedThrowables()
        public Throwable getThrowable()
        public int getTryCount()
    }
    public class RetryOptions {
        public RetryOptions(ExponentialBackoffOptions exponentialBackoffOptions)
        public RetryOptions(FixedDelayOptions fixedDelayOptions)
        public ExponentialBackoffOptions getExponentialBackoffOptions()
        public FixedDelayOptions getFixedDelayOptions()
        public Predicate<RequestRetryCondition> getShouldRetryCondition()
        public RetryOptions setShouldRetryCondition(Predicate<RequestRetryCondition> shouldRetryCondition)
    }
    public class RetryPolicy implements HttpPipelinePolicy {
        public RetryPolicy()
        public RetryPolicy(RetryStrategy retryStrategy)
        public RetryPolicy(RetryOptions retryOptions)
        public RetryPolicy(String retryAfterHeader, ChronoUnit retryAfterTimeUnit)
        public RetryPolicy(RetryStrategy retryStrategy, String retryAfterHeader, ChronoUnit retryAfterTimeUnit)
        @Override public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next)
        @Override public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next)
    }
    public interface RetryStrategy {
        int HTTP_STATUS_TOO_MANY_REQUESTS = 429;
        Duration calculateRetryDelay(int retryAttempts)
        default Duration calculateRetryDelay(RequestRetryCondition requestRetryCondition)
        int getMaxRetries()
        default boolean shouldRetry(HttpResponse httpResponse)
        default boolean shouldRetryCondition(RequestRetryCondition requestRetryCondition)
        default boolean shouldRetryException(Throwable throwable)
    }
    @Deprecated
    public class TimeoutPolicy implements HttpPipelinePolicy {
        public TimeoutPolicy(Duration timeoutDuration)
        @Override public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next)
    }
    public class UserAgentPolicy implements HttpPipelinePolicy {
        public static final String OVERRIDE_USER_AGENT_CONTEXT_KEY = "Override-User-Agent";
        public static final String APPEND_USER_AGENT_CONTEXT_KEY = "Append-User-Agent";
        public UserAgentPolicy()
        public UserAgentPolicy(String userAgent)
        public UserAgentPolicy(String applicationId, String sdkName, String sdkVersion, Configuration configuration)
        @Deprecated public UserAgentPolicy(String sdkName, String sdkVersion, Configuration configuration, ServiceVersion version)
        @Override public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next)
        @Override public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next)
    }
}
package com.azure.core.http.rest {
    public interface Page<T> extends ContinuablePage<String, T> {
        @Deprecated default List<T> getItems()
    }
    public class PagedFlux<T> extends PagedFluxBase<T, PagedResponse<T>> {
        public PagedFlux(Supplier<Mono<PagedResponse<T>>> firstPageRetriever)
        public PagedFlux(Function<Integer, Mono<PagedResponse<T>>> firstPageRetriever)
        public PagedFlux(Supplier<Mono<PagedResponse<T>>> firstPageRetriever, Function<String, Mono<PagedResponse<T>>> nextPageRetriever)
        public PagedFlux(Function<Integer, Mono<PagedResponse<T>>> firstPageRetriever, BiFunction<String, Integer, Mono<PagedResponse<T>>> nextPageRetriever)
        public static <T> PagedFlux<T> create(Supplier<PageRetriever<String, PagedResponse<T>>> provider)
        @Deprecated public <S> PagedFlux<S> mapPage(Function<T, S> mapper)
    }
    @Deprecated
    public class PagedFluxBase<T, P extends PagedResponse<T>> extends ContinuablePagedFluxCore<String, T, P> {
        public PagedFluxBase(Supplier<Mono<P>> firstPageRetriever)
        public PagedFluxBase(Supplier<Mono<P>> firstPageRetriever, Function<String, Mono<P>> nextPageRetriever)
        public Flux<P> byPage()
        public Flux<P> byPage(String continuationToken)
        @Override public void subscribe(CoreSubscriber<? super T> coreSubscriber)
    }
    public class PagedIterable<T> extends PagedIterableBase<T, PagedResponse<T>> {
        public PagedIterable(PagedFlux<T> pagedFlux)
        public PagedIterable(Supplier<PagedResponse<T>> firstPageRetriever)
        public PagedIterable(Function<Integer, PagedResponse<T>> firstPageRetriever)
        public PagedIterable(Supplier<PagedResponse<T>> firstPageRetriever, Function<String, PagedResponse<T>> nextPageRetriever)
        public PagedIterable(Function<Integer, PagedResponse<T>> firstPageRetriever, BiFunction<String, Integer, PagedResponse<T>> nextPageRetriever)
        public <S> PagedIterable<S> mapPage(Function<T, S> mapper)
    }
    public class PagedIterableBase<T, P extends PagedResponse<T>> extends ContinuablePagedIterable<String, T, P> {
        public PagedIterableBase(PagedFluxBase<T, P> pagedFluxBase)
        public PagedIterableBase(Supplier<PageRetrieverSync<String, P>> provider)
    }
    public interface PagedResponse<T> extends Page<T> , Response<List<T>> , Closeable {
        default List<T> getValue()
    }
    public class PagedResponseBase<H, T> implements PagedResponse<T> {
        public PagedResponseBase(HttpRequest request, int statusCode, HttpHeaders headers, Page<T> page, H deserializedHeaders)
        public PagedResponseBase(HttpRequest request, int statusCode, HttpHeaders headers, List<T> items, String continuationToken, H deserializedHeaders)
        @Override public void close()
        @Override public String getContinuationToken()
        public H getDeserializedHeaders()
        @Override public IterableStream<T> getElements()
        @Override public HttpHeaders getHeaders()
        @Override public HttpRequest getRequest()
        @Override public int getStatusCode()
    }
    public final class RequestOptions {
        public RequestOptions()
        @Deprecated public RequestOptions addHeader(String header, String value)
        public RequestOptions addHeader(HttpHeaderName header, String value)
        public RequestOptions addQueryParam(String parameterName, String value)
        public RequestOptions addQueryParam(String parameterName, String value, boolean encoded)
        public RequestOptions addRequestCallback(Consumer<HttpRequest> requestCallback)
        public RequestOptions setBody(BinaryData requestBody)
        public Context getContext()
        public RequestOptions setContext(Context context)
        @Deprecated public RequestOptions setHeader(String header, String value)
        public RequestOptions setHeader(HttpHeaderName header, String value)
    }
    public interface Response<T> {
        HttpHeaders getHeaders()
        HttpRequest getRequest()
        int getStatusCode()
        T getValue()
    }
    public class ResponseBase<H, T> implements Response<T> {
        public ResponseBase(HttpRequest request, int statusCode, HttpHeaders headers, T value, H deserializedHeaders)
        public H getDeserializedHeaders()
        @Override public HttpHeaders getHeaders()
        @Override public HttpRequest getRequest()
        @Override public int getStatusCode()
        @Override public T getValue()
    }
    public final class RestProxy implements InvocationHandler {
        public static <A> A create(Class<A> swaggerInterface)
        public static <A> A create(Class<A> swaggerInterface, HttpPipeline httpPipeline)
        public static <A> A create(Class<A> swaggerInterface, HttpPipeline httpPipeline, SerializerAdapter serializer)
        @Override public Object invoke(Object proxy, Method method, Object[] args)
        public Mono<HttpResponse> send(HttpRequest request, Context contextData)
    }
    public class SimpleResponse<T> implements Response<T> {
        public SimpleResponse(Response<?> response, T value)
        public SimpleResponse(HttpRequest request, int statusCode, HttpHeaders headers, T value)
        @Override public HttpHeaders getHeaders()
        @Override public HttpRequest getRequest()
        @Override public int getStatusCode()
        @Override public T getValue()
    }
    public final class StreamResponse extends SimpleResponse<Flux<ByteBuffer>> implements Closeable {
        public StreamResponse(HttpResponse response)
        @Deprecated public StreamResponse(HttpRequest request, int statusCode, HttpHeaders headers, Flux<ByteBuffer> value)
        @Override public void close()
        @Override public Flux<ByteBuffer> getValue()
        public void writeValueTo(WritableByteChannel channel)
        public Mono<Void> writeValueToAsync(AsynchronousByteChannel channel)
    }
}
package com.azure.core.models {
    public final class AzureCloud extends ExpandableStringEnum<AzureCloud> {
        public static final AzureCloud AZURE_PUBLIC_CLOUD = fromString("AZURE_PUBLIC_CLOUD");
        public static final AzureCloud AZURE_CHINA_CLOUD = fromString("AZURE_CHINA_CLOUD");
        public static final AzureCloud AZURE_US_GOVERNMENT_CLOUD = fromString("AZURE_US_GOVERNMENT");
        @Deprecated public AzureCloud()
        public static AzureCloud fromString(String cloudName)
    }
    @Fluent
    public final class CloudEvent implements JsonSerializable<CloudEvent> {
        public CloudEvent(String source, String type, BinaryData data, CloudEventDataFormat format, String dataContentType)
        public CloudEvent addExtensionAttribute(String name, Object value)
        public BinaryData getData()
        public String getDataContentType()
        public String getDataSchema()
        public CloudEvent setDataSchema(String dataSchema)
        public Map<String, Object> getExtensionAttributes()
        public static List<CloudEvent> fromString(String cloudEventsJson)
        public static List<CloudEvent> fromString(String cloudEventsJson, boolean skipValidation)
        public String getId()
        public CloudEvent setId(String id)
        public String getSource()
        public String getSubject()
        public CloudEvent setSubject(String subject)
        public OffsetDateTime getTime()
        public CloudEvent setTime(OffsetDateTime time)
        public String getType()
    }
    public final class CloudEventDataFormat extends ExpandableStringEnum<CloudEventDataFormat> {
        public static final CloudEventDataFormat BYTES = fromString("BYTES", CloudEventDataFormat.class);
        public static final CloudEventDataFormat JSON = fromString("JSON", CloudEventDataFormat.class);
        @Deprecated public CloudEventDataFormat()
        public static CloudEventDataFormat fromString(String name)
    }
    @Immutable
    public final class GeoBoundingBox implements JsonSerializable<GeoBoundingBox> {
        public GeoBoundingBox(double west, double south, double east, double north)
        public GeoBoundingBox(double west, double south, double east, double north, double minAltitude, double maxAltitude)
        public double getEast()
        @Override public boolean equals(Object obj)
        @Override public int hashCode()
        public Double getMaxAltitude()
        public Double getMinAltitude()
        public double getNorth()
        public double getSouth()
        @Override public String toString()
        public double getWest()
    }
    @Immutable
    public final class GeoCollection extends GeoObject {
        public GeoCollection(List<GeoObject> geometries)
        public GeoCollection(List<GeoObject> geometries, GeoBoundingBox boundingBox, Map<String, Object> customProperties)
        @Override public boolean equals(Object obj)
        public static GeoCollection fromJson(JsonReader jsonReader) throws IOException
        public List<GeoObject> getGeometries()
        @Override public int hashCode()
        @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
        @Override public GeoObjectType getType()
    }
    @Immutable
    public final class GeoLineString extends GeoObject {
        public GeoLineString(List<GeoPosition> positions)
        public GeoLineString(List<GeoPosition> positions, GeoBoundingBox boundingBox, Map<String, Object> customProperties)
        public List<GeoPosition> getCoordinates()
        @Override public boolean equals(Object obj)
        public static GeoLineString fromJson(JsonReader jsonReader) throws IOException
        @Override public int hashCode()
        @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
        @Override public GeoObjectType getType()
    }
    @Immutable
    public final class GeoLineStringCollection extends GeoObject {
        public GeoLineStringCollection(List<GeoLineString> lines)
        public GeoLineStringCollection(List<GeoLineString> lines, GeoBoundingBox boundingBox, Map<String, Object> customProperties)
        @Override public boolean equals(Object obj)
        public static GeoLineStringCollection fromJson(JsonReader jsonReader) throws IOException
        @Override public int hashCode()
        public List<GeoLineString> getLines()
        @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
        @Override public GeoObjectType getType()
    }
    @Immutable
    public final class GeoLinearRing implements JsonSerializable<GeoLinearRing> {
        public GeoLinearRing(List<GeoPosition> coordinates)
        public List<GeoPosition> getCoordinates()
        @Override public boolean equals(Object obj)
        @Override public int hashCode()
    }
    @Immutable
    public abstract class GeoObject implements JsonSerializable<GeoObject> {
        protected GeoObject(GeoBoundingBox boundingBox, Map<String, Object> customProperties)
        public final GeoBoundingBox getBoundingBox()
        public final Map<String, Object> getCustomProperties()
        @Override public boolean equals(Object obj)
        @Override public int hashCode()
        public abstract GeoObjectType getType()
    }
    public final class GeoObjectType extends ExpandableStringEnum<GeoObjectType> {
        public static final GeoObjectType POINT = fromString("Point");
        public static final GeoObjectType MULTI_POINT = fromString("MultiPoint");
        public static final GeoObjectType POLYGON = fromString("Polygon");
        public static final GeoObjectType MULTI_POLYGON = fromString("MultiPolygon");
        public static final GeoObjectType LINE_STRING = fromString("LineString");
        public static final GeoObjectType MULTI_LINE_STRING = fromString("MultiLineString");
        public static final GeoObjectType GEOMETRY_COLLECTION = fromString("GeometryCollection");
        @Deprecated public GeoObjectType()
        public static GeoObjectType fromString(String name)
        public static Collection<GeoObjectType> values()
    }
    @Immutable
    public final class GeoPoint extends GeoObject {
        public GeoPoint(GeoPosition position)
        public GeoPoint(double longitude, double latitude)
        public GeoPoint(double longitude, double latitude, Double altitude)
        public GeoPoint(GeoPosition position, GeoBoundingBox boundingBox, Map<String, Object> customProperties)
        public GeoPosition getCoordinates()
        @Override public boolean equals(Object obj)
        public static GeoPoint fromJson(JsonReader jsonReader) throws IOException
        @Override public int hashCode()
        @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
        @Override public GeoObjectType getType()
    }
    @Immutable
    public final class GeoPointCollection extends GeoObject {
        public GeoPointCollection(List<GeoPoint> points)
        public GeoPointCollection(List<GeoPoint> points, GeoBoundingBox boundingBox, Map<String, Object> customProperties)
        @Override public boolean equals(Object obj)
        public static GeoPointCollection fromJson(JsonReader jsonReader) throws IOException
        @Override public int hashCode()
        public List<GeoPoint> getPoints()
        @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
        @Override public GeoObjectType getType()
    }
    @Immutable
    public final class GeoPolygon extends GeoObject {
        public GeoPolygon(GeoLinearRing ring)
        public GeoPolygon(List<GeoLinearRing> rings)
        public GeoPolygon(GeoLinearRing ring, GeoBoundingBox boundingBox, Map<String, Object> customProperties)
        public GeoPolygon(List<GeoLinearRing> rings, GeoBoundingBox boundingBox, Map<String, Object> customProperties)
        @Override public boolean equals(Object obj)
        public static GeoPolygon fromJson(JsonReader jsonReader) throws IOException
        @Override public int hashCode()
        public GeoLinearRing getOuterRing()
        public List<GeoLinearRing> getRings()
        @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
        @Override public GeoObjectType getType()
    }
    @Immutable
    public final class GeoPolygonCollection extends GeoObject {
        public GeoPolygonCollection(List<GeoPolygon> polygons)
        public GeoPolygonCollection(List<GeoPolygon> polygons, GeoBoundingBox boundingBox, Map<String, Object> customProperties)
        @Override public boolean equals(Object obj)
        public static GeoPolygonCollection fromJson(JsonReader jsonReader) throws IOException
        @Override public int hashCode()
        public List<GeoPolygon> getPolygons()
        @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
        @Override public GeoObjectType getType()
    }
    @Immutable
    public final class GeoPosition implements JsonSerializable<GeoPosition> {
        public GeoPosition(double longitude, double latitude)
        public GeoPosition(double longitude, double latitude, Double altitude)
        public Double getAltitude()
        public int count()
        @Override public boolean equals(Object obj)
        @Override public int hashCode()
        public double getLatitude()
        public double getLongitude()
        @Override public String toString()
    }
    public final class JsonPatchDocument implements JsonSerializable<JsonPatchDocument> {
        public JsonPatchDocument()
        public JsonPatchDocument(JsonSerializer serializer)
        public JsonPatchDocument appendAdd(String path, Object value)
        public JsonPatchDocument appendAddRaw(String path, String rawJson)
        public JsonPatchDocument appendCopy(String from, String path)
        public JsonPatchDocument appendMove(String from, String path)
        public JsonPatchDocument appendRemove(String path)
        public JsonPatchDocument appendReplace(String path, Object value)
        public JsonPatchDocument appendReplaceRaw(String path, String rawJson)
        public JsonPatchDocument appendTest(String path, Object value)
        public JsonPatchDocument appendTestRaw(String path, String rawJson)
        @Override public String toString()
    }
    @Fluent
    public class MessageContent {
        public MessageContent()
        public BinaryData getBodyAsBinaryData()
        public MessageContent setBodyAsBinaryData(BinaryData binaryData)
        public String getContentType()
        public MessageContent setContentType(String contentType)
    }
    public final class ResponseError implements JsonSerializable<ResponseError> {
        public ResponseError(String code, String message)
        public String getCode()
        public String getMessage()
    }
}
package com.azure.core.util {
    public interface AsyncCloseable {
        Mono<Void> closeAsync()
    }
    public final class AuthenticateChallenge {
        public AuthenticateChallenge(String scheme)
        public AuthenticateChallenge(String scheme, String token68)
        public AuthenticateChallenge(String scheme, Map<String, String> parameters)
        public Map<String, String> getParameters()
        public String getScheme()
        public String getToken68()
    }
    public class AuthorizationChallengeHandler {
        public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
        public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";
        public static final String AUTHORIZATION = "Authorization";
        public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";
        public static final String AUTHENTICATION_INFO = "Authentication-Info";
        public static final String PROXY_AUTHENTICATION_INFO = "Proxy-Authentication-Info";
        public AuthorizationChallengeHandler(String username, String password)
        public final String attemptToPipelineAuthorization(String method, String uri, Supplier<byte[]> entityBodySupplier)
        public final void consumeAuthenticationInfoHeader(Map<String, String> authenticationInfoMap)
        public final String handleBasic()
        public final String handleDigest(String method, String uri, List<Map<String, String>> challenges, Supplier<byte[]> entityBodySupplier)
        public static Map<String, String> parseAuthenticationOrAuthorizationHeader(String header)
    }
    public final class Base64Url {
        public Base64Url(String string)
        public Base64Url(byte[] bytes)
        public byte[] decodedBytes()
        public static Base64Url encode(byte[] bytes)
        public byte[] encodedBytes()
        @Override public boolean equals(Object obj)
        @Override public int hashCode()
        @Override public String toString()
    }
    public final class Base64Util {
        public static byte[] decode(byte[] encoded)
        public static byte[] decodeString(String encoded)
        public static byte[] decodeURL(byte[] src)
        public static byte[] encode(byte[] src)
        public static String encodeToString(byte[] src)
        public static byte[] encodeURLWithoutPadding(byte[] src)
    }
    public final class BinaryData {
        public static BinaryData fromByteBuffer(ByteBuffer data)
        public static BinaryData fromBytes(byte[] data)
        public static BinaryData fromFile(Path file)
        public static BinaryData fromFile(Path file, int chunkSize)
        public static BinaryData fromFile(Path file, Long position, Long length)
        public static BinaryData fromFile(Path file, Long position, Long length, int chunkSize)
        public static Mono<BinaryData> fromFlux(Flux<ByteBuffer> data)
        public static Mono<BinaryData> fromFlux(Flux<ByteBuffer> data, Long length)
        public static Mono<BinaryData> fromFlux(Flux<ByteBuffer> data, Long length, boolean bufferContent)
        public static BinaryData fromListByteBuffer(List<ByteBuffer> data)
        public static BinaryData fromObject(Object data)
        public static BinaryData fromObject(Object data, ObjectSerializer serializer)
        public static Mono<BinaryData> fromObjectAsync(Object data)
        public static Mono<BinaryData> fromObjectAsync(Object data, ObjectSerializer serializer)
        public static BinaryData fromStream(InputStream inputStream)
        public static BinaryData fromStream(InputStream inputStream, Long length)
        public static Mono<BinaryData> fromStreamAsync(InputStream inputStream)
        public static Mono<BinaryData> fromStreamAsync(InputStream inputStream, Long length)
        public static BinaryData fromString(String data)
        public Long getLength()
        public boolean isReplayable()
        public ByteBuffer toByteBuffer()
        public byte[] toBytes()
        public Flux<ByteBuffer> toFluxByteBuffer()
        public <T> T toObject(Class<T> clazz)
        public <T> T toObject(TypeReference<T> typeReference)
        public <T> T toObject(Class<T> clazz, ObjectSerializer serializer)
        public <T> T toObject(TypeReference<T> typeReference, ObjectSerializer serializer)
        public <T> Mono<T> toObjectAsync(Class<T> clazz)
        public <T> Mono<T> toObjectAsync(TypeReference<T> typeReference)
        public <T> Mono<T> toObjectAsync(Class<T> clazz, ObjectSerializer serializer)
        public <T> Mono<T> toObjectAsync(TypeReference<T> typeReference, ObjectSerializer serializer)
        public BinaryData toReplayableBinaryData()
        public Mono<BinaryData> toReplayableBinaryDataAsync()
        public InputStream toStream()
        public String toString()
        public void writeTo(OutputStream outputStream) throws IOException
        public void writeTo(WritableByteChannel channel) throws IOException
        public Mono<Void> writeTo(AsynchronousByteChannel channel)
        public void writeTo(JsonWriter jsonWriter) throws IOException
    }
    @Fluent
    public class ClientOptions {
        public ClientOptions()
        public String getApplicationId()
        public ClientOptions setApplicationId(String applicationId)
        public Iterable<Header> getHeaders()
        public ClientOptions setHeaders(Iterable<Header> headers)
        public MetricsOptions getMetricsOptions()
        public ClientOptions setMetricsOptions(MetricsOptions metricsOptions)
        public TracingOptions getTracingOptions()
        public ClientOptions setTracingOptions(TracingOptions tracingOptions)
    }
    public class Configuration implements Cloneable {
        public static final String PROPERTY_HTTP_PROXY = "HTTP_PROXY";
        public static final String PROPERTY_HTTPS_PROXY = "HTTPS_PROXY";
        public static final String PROPERTY_IDENTITY_ENDPOINT = "IDENTITY_ENDPOINT";
        public static final String PROPERTY_IDENTITY_HEADER = "IDENTITY_HEADER";
        public static final String PROPERTY_NO_PROXY = "NO_PROXY";
        public static final String PROPERTY_MSI_ENDPOINT = "MSI_ENDPOINT";
        public static final String PROPERTY_MSI_SECRET = "MSI_SECRET";
        public static final String PROPERTY_AZURE_SUBSCRIPTION_ID = "AZURE_SUBSCRIPTION_ID";
        public static final String PROPERTY_AZURE_USERNAME = "AZURE_USERNAME";
        public static final String PROPERTY_AZURE_PASSWORD = "AZURE_PASSWORD";
        public static final String PROPERTY_AZURE_CLIENT_ID = "AZURE_CLIENT_ID";
        public static final String PROPERTY_AZURE_CLIENT_SECRET = "AZURE_CLIENT_SECRET";
        public static final String PROPERTY_AZURE_TENANT_ID = "AZURE_TENANT_ID";
        public static final String PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH = "AZURE_CLIENT_CERTIFICATE_PATH";
        public static final String PROPERTY_AZURE_CLIENT_CERTIFICATE_PASSWORD = "AZURE_CLIENT_CERTIFICATE_PASSWORD";
        public static final String PROPERTY_AZURE_CLIENT_SEND_CERTIFICATE_CHAIN = "AZURE_CLIENT_SEND_CERTIFICATE_CHAIN";
        public static final String PROPERTY_AZURE_IDENTITY_DISABLE_CP1 = "AZURE_IDENTITY_DISABLE_CP1";
        public static final String PROPERTY_AZURE_POD_IDENTITY_TOKEN_URL = "AZURE_POD_IDENTITY_TOKEN_URL";
        public static final String PROPERTY_AZURE_REGIONAL_AUTHORITY_NAME = "AZURE_REGIONAL_AUTHORITY_NAME";
        public static final String PROPERTY_AZURE_RESOURCE_GROUP = "AZURE_RESOURCE_GROUP";
        public static final String PROPERTY_AZURE_CLOUD = "AZURE_CLOUD";
        public static final String PROPERTY_AZURE_AUTHORITY_HOST = "AZURE_AUTHORITY_HOST";
        public static final String PROPERTY_AZURE_TELEMETRY_DISABLED = "AZURE_TELEMETRY_DISABLED";
        public static final String PROPERTY_AZURE_LOG_LEVEL = "AZURE_LOG_LEVEL";
        public static final String PROPERTY_AZURE_HTTP_LOG_DETAIL_LEVEL = "AZURE_HTTP_LOG_DETAIL_LEVEL";
        public static final String PROPERTY_AZURE_TRACING_DISABLED = "AZURE_TRACING_DISABLED";
        public static final String PROPERTY_AZURE_TRACING_IMPLEMENTATION = "AZURE_TRACING_IMPLEMENTATION";
        public static final String PROPERTY_AZURE_METRICS_DISABLED = "AZURE_METRICS_DISABLED";
        public static final String PROPERTY_AZURE_METRICS_IMPLEMENTATION = "AZURE_METRICS_IMPLEMENTATION";
        public static final String PROPERTY_AZURE_REQUEST_RETRY_COUNT = "AZURE_REQUEST_RETRY_COUNT";
        public static final String PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT = "AZURE_REQUEST_CONNECT_TIMEOUT";
        public static final String PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT = "AZURE_REQUEST_WRITE_TIMEOUT";
        public static final String PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT = "AZURE_REQUEST_RESPONSE_TIMEOUT";
        public static final String PROPERTY_AZURE_REQUEST_READ_TIMEOUT = "AZURE_REQUEST_READ_TIMEOUT";
        public static final String PROPERTY_AZURE_HTTP_CLIENT_IMPLEMENTATION = "AZURE_HTTP_CLIENT_IMPLEMENTATION";
        public static final Configuration NONE = new NoopConfiguration ( /* Elided */ ) ;
        @Deprecated public Configuration()
        public String get(String name)
        public <T> T get(ConfigurationProperty<T> property)
        public <T> T get(String name, T defaultValue)
        public <T> T get(String name, Function<String, T> converter)
        @Deprecated public Configuration clone()
        public boolean contains(String name)
        public boolean contains(ConfigurationProperty<?> property)
        public static Configuration getGlobalConfiguration()
        @Deprecated public Configuration put(String name, String value)
        @Deprecated public String remove(String name)
    }
    @Fluent
    public final class ConfigurationBuilder {
        public ConfigurationBuilder()
        public ConfigurationBuilder(ConfigurationSource source)
        public ConfigurationBuilder(ConfigurationSource source, ConfigurationSource systemPropertiesConfigurationSource, ConfigurationSource environmentConfigurationSource)
        public ConfigurationBuilder putProperty(String name, String value)
        public ConfigurationBuilder root(String rootPath)
        public Configuration build()
        public Configuration buildSection(String path)
    }
    public final class ConfigurationProperty<T> {
        public Iterable<String> getAliases()
        public Function<String, T> getConverter()
        public T getDefaultValue()
        public String getEnvironmentVariableName()
        public String getName()
        public boolean isRequired()
        public boolean isShared()
        public String getSystemPropertyName()
        public Function<String, String> getValueSanitizer()
    }
    public final class ConfigurationPropertyBuilder<T> {
        public ConfigurationPropertyBuilder(String name, Function<String, T> converter)
        public ConfigurationPropertyBuilder<T> aliases(String... aliases)
        public ConfigurationPropertyBuilder<T> defaultValue(T defaultValue)
        public ConfigurationPropertyBuilder<T> environmentVariableName(String environmentVariableName)
        public ConfigurationPropertyBuilder<T> logValue(boolean logValue)
        public static ConfigurationPropertyBuilder<Boolean> ofBoolean(String name)
        public static ConfigurationPropertyBuilder<Duration> ofDuration(String name)
        public static ConfigurationPropertyBuilder<Integer> ofInteger(String name)
        public static ConfigurationPropertyBuilder<String> ofString(String name)
        public ConfigurationPropertyBuilder<T> required(boolean required)
        public ConfigurationPropertyBuilder<T> shared(boolean shared)
        public ConfigurationPropertyBuilder<T> systemPropertyName(String systemPropertyName)
        public ConfigurationProperty<T> build()
    }
    @FunctionalInterface
    public interface ConfigurationSource {
        Map<String, String> getProperties(String source)
    }
    @Immutable
    public class Context {
        public static final Context NONE ;
        public Context(Object key, Object value)
        public Context addData(Object key, Object value)
        public Optional<Object> getData(Object key)
        public static Context of(Map<Object, Object> keyValues)
        public Map<Object, Object> getValues()
    }
    public final class Contexts {
        public Context getContext()
        public static Contexts empty()
        public ProgressReporter getHttpRequestProgressReporter()
        public Contexts setHttpRequestProgressReporter(ProgressReporter progressReporter)
        public static Contexts with(Context context)
    }
    public final class CoreUtils {
        public static Thread addShutdownHookSafely(Thread shutdownThread)
        public static ExecutorService addShutdownHookSafely(ExecutorService executorService, Duration shutdownTimeout)
        public static String getApplicationId(ClientOptions clientOptions, HttpLogOptions logOptions)
        public static <T> String arrayToString(T[] array, Function<T, String> mapper)
        public static String bomAwareToString(byte[] bytes, String contentType)
        public static String bytesToHexString(byte[] bytes)
        public static byte[] clone(byte[] source)
        public static int[] clone(int[] source)
        public static <T> T[] clone(T[] source)
        public static HttpHeaders createHttpHeadersFromClientOptions(ClientOptions clientOptions)
        public static Duration getDefaultTimeoutFromEnvironment(Configuration configuration, String timeoutPropertyName, Duration defaultTimeout, ClientLogger logger)
        public static String durationToStringWithDays(Duration duration)
        @Deprecated public static <T> Publisher<T> extractAndFetch(PagedResponse<T> page, Context context, BiFunction<String, Context, Publisher<T>> content)
        public static long extractSizeFromContentRange(String contentRange)
        public static <T> T findFirstOfType(Object[] args, Class<T> clazz)
        public static Context mergeContexts(Context into, Context from)
        public static boolean isNullOrEmpty(Object[] array)
        public static boolean isNullOrEmpty(Collection<?> collection)
        public static boolean isNullOrEmpty(Map<?, ?> map)
        public static boolean isNullOrEmpty(CharSequence charSequence)
        public static List<AuthenticateChallenge> parseAuthenticateHeader(String authenticateHeader)
        public static OffsetDateTime parseBestOffsetDateTime(String dateString)
        public static Iterator<Map.Entry<String, String>> parseQueryParameters(String queryParameters)
        public static Map<String, String> getProperties(String propertiesFileName)
        public static UUID randomUuid()
        public static <T> T getResultWithTimeout(Future<T> future, Duration timeout) throws InterruptedException, ExecutionException, TimeoutException
        public static String stringJoin(String delimiter, List<String> values)
    }
    public final class DateTimeRfc1123 {
        public DateTimeRfc1123(OffsetDateTime dateTime)
        public DateTimeRfc1123(String formattedString)
        public OffsetDateTime getDateTime()
        @Override public boolean equals(Object obj)
        @Override public int hashCode()
        public static String toRfc1123String(OffsetDateTime dateTime)
        @Override public String toString()
    }
    public final class ETag {
        public static final ETag ALL = new ETag ( /* Elided */ ) ;
        public ETag(String eTag)
        @Override public boolean equals(Object o)
        @Override public int hashCode()
        @Override public String toString()
    }
    public interface ExpandableEnum<T> {
        T getValue()
    }
    public abstract class ExpandableStringEnum<T extends ExpandableStringEnum<T>> implements ExpandableEnum<String> {
        @Deprecated public ExpandableStringEnum()
        @Override public boolean equals(Object obj)
        protected static <T extends ExpandableStringEnum<T>> T fromString(String name, Class<T> clazz)
        @Override public int hashCode()
        @Override public String toString()
        @Override public String getValue()
        protected static <T extends ExpandableStringEnum<T>> Collection<T> values(Class<T> clazz)
    }
    public final class FluxUtil {
        public static Flux<ByteBuffer> addProgressReporting(Flux<ByteBuffer> flux, ProgressReporter progressReporter)
        public static byte[] byteBufferToArray(ByteBuffer byteBuffer)
        public static Mono<byte[]> collectBytesFromNetworkResponse(Flux<ByteBuffer> stream, HttpHeaders headers)
        public static Mono<byte[]> collectBytesInByteBufferStream(Flux<ByteBuffer> stream)
        public static Mono<byte[]> collectBytesInByteBufferStream(Flux<ByteBuffer> stream, int sizeHint)
        public static Flux<ByteBuffer> createRetriableDownloadFlux(Supplier<Flux<ByteBuffer>> downloadSupplier, BiFunction<Throwable, Long, Flux<ByteBuffer>> onDownloadErrorResume, int maxRetries)
        public static Flux<ByteBuffer> createRetriableDownloadFlux(Supplier<Flux<ByteBuffer>> downloadSupplier, BiFunction<Throwable, Long, Flux<ByteBuffer>> onDownloadErrorResume, int maxRetries, long position)
        public static Flux<ByteBuffer> createRetriableDownloadFlux(Supplier<Flux<ByteBuffer>> downloadSupplier, BiFunction<Throwable, Long, Flux<ByteBuffer>> onDownloadErrorResume, RetryOptions retryOptions, long position)
        public static boolean isFluxByteBuffer(Type entityType)
        public static <T> Flux<T> fluxContext(Function<Context, Flux<T>> serviceCall)
        public static <T> Flux<T> fluxError(ClientLogger logger, RuntimeException ex)
        public static <T> Mono<T> monoError(ClientLogger logger, RuntimeException ex)
        public static <T> Mono<T> monoError(LoggingEventBuilder logBuilder, RuntimeException ex)
        public static <T> PagedFlux<T> pagedFluxError(ClientLogger logger, RuntimeException ex)
        public static Flux<ByteBuffer> readFile(AsynchronousFileChannel fileChannel)
        public static Flux<ByteBuffer> readFile(AsynchronousFileChannel fileChannel, long offset, long length)
        public static Flux<ByteBuffer> readFile(AsynchronousFileChannel fileChannel, int chunkSize, long offset, long length)
        public static Flux<ByteBuffer> toFluxByteBuffer(InputStream inputStream)
        public static Flux<ByteBuffer> toFluxByteBuffer(InputStream inputStream, int chunkSize)
        public static <T> Mono<T> toMono(Response<T> response)
        public static reactor.util.context.Context toReactorContext(Context context)
        public static <T> Mono<T> withContext(Function<Context, Mono<T>> serviceCall)
        public static <T> Mono<T> withContext(Function<Context, Mono<T>> serviceCall, Map<String, String> contextAttributes)
        public static Mono<Void> writeFile(Flux<ByteBuffer> content, AsynchronousFileChannel outFile)
        public static Mono<Void> writeFile(Flux<ByteBuffer> content, AsynchronousFileChannel outFile, long position)
        public static Mono<Void> writeToAsynchronousByteChannel(Flux<ByteBuffer> content, AsynchronousByteChannel channel)
        public static Mono<Void> writeToOutputStream(Flux<ByteBuffer> content, OutputStream stream)
        public static Mono<Void> writeToWritableByteChannel(Flux<ByteBuffer> content, WritableByteChannel channel)
    }
    public class Header {
        public Header(String name, String value)
        public Header(String name, String... values)
        public Header(String name, List<String> values)
        public void addValue(String value)
        public String getName()
        @Override public String toString()
        public String getValue()
        public String[] getValues()
        public List<String> getValuesList()
    }
    @Fluent
    public final class HttpClientOptions extends ClientOptions {
        public HttpClientOptions()
        @Override public HttpClientOptions setApplicationId(String applicationId)
        public Configuration getConfiguration()
        public HttpClientOptions setConfiguration(Configuration configuration)
        public Duration getConnectionIdleTimeout()
        public HttpClientOptions setConnectionIdleTimeout(Duration connectionIdleTimeout)
        public Duration getConnectTimeout()
        public HttpClientOptions setConnectTimeout(Duration connectTimeout)
        @Override public HttpClientOptions setHeaders(Iterable<Header> headers)
        public Class<? extends HttpClientProvider> getHttpClientProvider()
        public HttpClientOptions setHttpClientProvider(Class<? extends HttpClientProvider> httpClientProvider)
        public Integer getMaximumConnectionPoolSize()
        public HttpClientOptions setMaximumConnectionPoolSize(Integer maximumConnectionPoolSize)
        public ProxyOptions getProxyOptions()
        public HttpClientOptions setProxyOptions(ProxyOptions proxyOptions)
        public Duration getReadTimeout()
        public HttpClientOptions readTimeout(Duration readTimeout)
        public HttpClientOptions setReadTimeout(Duration readTimeout)
        public Duration getResponseTimeout()
        public HttpClientOptions responseTimeout(Duration responseTimeout)
        public HttpClientOptions setResponseTimeout(Duration responseTimeout)
        public Duration getWriteTimeout()
        public HttpClientOptions setWriteTimeout(Duration writeTimeout)
    }
    public class IterableStream<T> implements Iterable<T> {
        public IterableStream(Flux<T> flux)
        public IterableStream(Iterable<T> iterable)
        @Override public Iterator<T> iterator()
        public static <T> IterableStream<T> of(Iterable<T> iterable)
        public Stream<T> stream()
    }
    @Fluent
    public final class LibraryTelemetryOptions {
        public LibraryTelemetryOptions(String libraryName)
        public String getLibraryName()
        public String getLibraryVersion()
        public LibraryTelemetryOptions setLibraryVersion(String libraryVersion)
        public String getResourceProviderNamespace()
        public LibraryTelemetryOptions setResourceProviderNamespace(String rpNamespace)
        public String getSchemaUrl()
        public LibraryTelemetryOptions setSchemaUrl(String schemaUrl)
    }
    public class MetricsOptions {
        public MetricsOptions()
        protected MetricsOptions(Class<? extends MeterProvider> meterProvider)
        public boolean isEnabled()
        public MetricsOptions setEnabled(boolean enabled)
        public static MetricsOptions fromConfiguration(Configuration configuration)
        public Class<? extends MeterProvider> getMeterProvider()
    }
    @FunctionalInterface
    public interface ProgressListener {
        void handleProgress(long progress)
    }
    public final class ProgressReporter {
        public ProgressReporter createChild()
        public void reportProgress(long progress)
        public void reset()
        public static ProgressReporter withProgressListener(ProgressListener progressListener)
    }
    public interface ReferenceManager {
        ReferenceManager INSTANCE = new ReferenceManagerImpl ( /* Elided */ ) ;
        void register(Object object, Runnable cleanupAction)
    }
    public interface ServiceVersion {
        String getVersion()
    }
    public final class SharedExecutorService implements ScheduledExecutorService {
        @Override public boolean awaitTermination(long timeout, TimeUnit unit)
        @Override public void execute(Runnable command)
        public ScheduledExecutorService getExecutorService()
        public void setExecutorService(ScheduledExecutorService executorService)
        public static SharedExecutorService getInstance()
        @Override public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException
        @Override public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException
        @Override public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException
        @Override public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
        public void reset()
        @Override public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit)
        @Override public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit)
        @Override public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
        @Override public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
        @Override public boolean isShutdown()
        @Override public void shutdown()
        @Override public List<Runnable> shutdownNow()
        @Override public <T> Future<T> submit(Callable<T> task)
        @Override public Future<?> submit(Runnable task)
        @Override public <T> Future<T> submit(Runnable task, T result)
        @Override public boolean isTerminated()
    }
    @Immutable
    public interface TelemetryAttributes {
    }
    public class TracingOptions {
        public TracingOptions()
        protected TracingOptions(Class<? extends TracerProvider> tracerProvider)
        public Set<String> getAllowedTracingQueryParamNames()
        public TracingOptions setAllowedTracingQueryParamNames(Set<String> allowedQueryParamNames)
        public boolean isEnabled()
        public TracingOptions setEnabled(boolean enabled)
        public static TracingOptions fromConfiguration(Configuration configuration)
        public Class<? extends TracerProvider> getTracerProvider()
    }
    public final class UrlBuilder {
        public UrlBuilder()
        public UrlBuilder addQueryParameter(String queryParameterName, String queryParameterEncodedValue)
        public UrlBuilder clearQuery()
        public String getHost()
        public UrlBuilder setHost(String host)
        public static UrlBuilder parse(String url)
        public static UrlBuilder parse(URL url)
        public String getPath()
        public UrlBuilder setPath(String path)
        public Integer getPort()
        public UrlBuilder setPort(String port)
        public UrlBuilder setPort(int port)
        public Map<String, String> getQuery()
        public UrlBuilder setQuery(String query)
        public UrlBuilder setQueryParameter(String queryParameterName, String queryParameterEncodedValue)
        public String getQueryString()
        public String getScheme()
        public UrlBuilder setScheme(String scheme)
        @Override public String toString()
        public URL toUrl() throws MalformedURLException
    }
    public class UserAgentProperties {
        public String getName()
        public String getVersion()
    }
    public final class UserAgentUtil {
        public static final String DEFAULT_USER_AGENT_HEADER = "azsdk-java";
        public static String toUserAgentString(String applicationId, String sdkName, String sdkVersion, Configuration configuration)
    }
}
package com.azure.core.util.builder {
    public final class ClientBuilderUtil {
        public static HttpPipelinePolicy validateAndGetRetryPolicy(HttpPipelinePolicy retryPolicy, RetryOptions retryOptions)
        public static HttpPipelinePolicy validateAndGetRetryPolicy(HttpPipelinePolicy retryPolicy, RetryOptions retryOptions, HttpPipelinePolicy defaultPolicy)
    }
}
package com.azure.core.util.io {
    public final class IOUtils {
        public static AsynchronousByteChannel toAsynchronousByteChannel(AsynchronousFileChannel fileChannel, long position)
        public static void transfer(ReadableByteChannel source, WritableByteChannel destination) throws IOException
        public static void transfer(ReadableByteChannel source, WritableByteChannel destination, Long estimatedSourceSize) throws IOException
        public static Mono<Void> transferAsync(ReadableByteChannel source, AsynchronousByteChannel destination)
        public static Mono<Void> transferAsync(ReadableByteChannel source, AsynchronousByteChannel destination, Long estimatedSourceSize)
        public static Mono<Void> transferStreamResponseToAsynchronousByteChannel(AsynchronousByteChannel targetChannel, StreamResponse sourceResponse, BiFunction<Throwable, Long, Mono<StreamResponse>> onErrorResume, ProgressReporter progressReporter, int maxRetries)
    }
}
package com.azure.core.util.logging {
    public class ClientLogger {
        public ClientLogger(Class<?> clazz)
        public ClientLogger(String className)
        public ClientLogger(Class<?> clazz, Map<String, Object> context)
        public ClientLogger(String className, Map<String, Object> context)
        public LoggingEventBuilder atError()
        public LoggingEventBuilder atInfo()
        public LoggingEventBuilder atLevel(LogLevel level)
        public LoggingEventBuilder atVerbose()
        public LoggingEventBuilder atWarning()
        public boolean canLogAtLevel(LogLevel logLevel)
        public void error(String message)
        public void error(String format, Object... args)
        public void info(String message)
        public void info(String format, Object... args)
        public void log(LogLevel logLevel, Supplier<String> message)
        public void log(LogLevel logLevel, Supplier<String> message, Throwable throwable)
        public RuntimeException logExceptionAsError(RuntimeException runtimeException)
        public RuntimeException logExceptionAsWarning(RuntimeException runtimeException)
        @Deprecated public <T extends Throwable> T logThowableAsWarning(T throwable)
        public <T extends Throwable> T logThrowableAsError(T throwable)
        public <T extends Throwable> T logThrowableAsWarning(T throwable)
        public void verbose(String message)
        public void verbose(String format, Object... args)
        public void warning(String message)
        public void warning(String format, Object... args)
    }
    public enum LogLevel {
        VERBOSE(1, "1", "verbose", "debug"),
        INFORMATIONAL(2, "2", "info", "information", "informational"),
        WARNING(3, "3", "warn", "warning"),
        ERROR(4, "4", "err", "error"),
        NOT_SET(5, "5");
        public static LogLevel fromString(String logLevelVal)
        public int getLogLevel()
    }
    @Fluent
    public final class LoggingEventBuilder {
        public LoggingEventBuilder addKeyValue(String key, String value)
        public LoggingEventBuilder addKeyValue(String key, Object value)
        public LoggingEventBuilder addKeyValue(String key, boolean value)
        public LoggingEventBuilder addKeyValue(String key, long value)
        public LoggingEventBuilder addKeyValue(String key, Supplier<String> valueSupplier)
        public void log(String message)
        public void log(Supplier<String> messageSupplier)
        public Throwable log(Throwable throwable)
        public RuntimeException log(RuntimeException runtimeException)
        public void log(Supplier<String> messageSupplier, Throwable throwable)
        public void log(String format, Object... args)
    }
}
package com.azure.core.util.metrics {
    public interface DoubleHistogram {
        boolean isEnabled()
        void record(double value, TelemetryAttributes attributes, Context context)
    }
    public interface LongCounter {
        void add(long value, TelemetryAttributes attributes, Context context)
        boolean isEnabled()
    }
    public interface LongGauge {
        boolean isEnabled()
        AutoCloseable registerCallback(Supplier<Long> valueSupplier, TelemetryAttributes attributes)
    }
    public interface Meter extends AutoCloseable {
        @Override void close()
        TelemetryAttributes createAttributes(Map<String, Object> attributeMap)
        DoubleHistogram createDoubleHistogram(String name, String description, String unit)
        LongCounter createLongCounter(String name, String description, String unit)
        default LongGauge createLongGauge(String name, String description, String unit)
        LongCounter createLongUpDownCounter(String name, String description, String unit)
        boolean isEnabled()
    }
    public interface MeterProvider {
        default Meter createMeter(LibraryTelemetryOptions libraryOptions, MetricsOptions applicationOptions)
        Meter createMeter(String libraryName, String libraryVersion, MetricsOptions applicationOptions)
        static MeterProvider getDefaultProvider()
    }
}
package com.azure.core.util.paging {
    public interface ContinuablePage<C, T> {
        C getContinuationToken()
        IterableStream<T> getElements()
    }
    public abstract class ContinuablePagedFlux<C, T, P extends ContinuablePage<C, T>> extends Flux<T> {
        public ContinuablePagedFlux()
        protected ContinuablePagedFlux(Predicate<C> continuationPredicate)
        public abstract Flux<P> byPage()
        public abstract Flux<P> byPage(C continuationToken)
        public abstract Flux<P> byPage(int preferredPageSize)
        public abstract Flux<P> byPage(C continuationToken, int preferredPageSize)
        protected final Predicate<C> getContinuationPredicate()
    }
    public abstract class ContinuablePagedFluxCore<C, T, P extends ContinuablePage<C, T>> extends ContinuablePagedFlux<C, T, P> {
        protected ContinuablePagedFluxCore(Supplier<PageRetriever<C, P>> pageRetrieverProvider)
        protected ContinuablePagedFluxCore(Supplier<PageRetriever<C, P>> pageRetrieverProvider, int pageSize)
        protected ContinuablePagedFluxCore(Supplier<PageRetriever<C, P>> pageRetrieverProvider, Integer pageSize, Predicate<C> continuationPredicate)
        @Override public Flux<P> byPage()
        @Override public Flux<P> byPage(C continuationToken)
        @Override public Flux<P> byPage(int preferredPageSize)
        @Override public Flux<P> byPage(C continuationToken, int preferredPageSize)
        public Integer getPageSize()
        @Override public void subscribe(CoreSubscriber<? super T> coreSubscriber)
    }
    public class ContinuablePagedIterable<C, T, P extends ContinuablePage<C, T>> extends IterableStream<T> {
        public ContinuablePagedIterable(ContinuablePagedFlux<C, T, P> pagedFlux)
        public ContinuablePagedIterable(ContinuablePagedFlux<C, T, P> pagedFlux, int batchSize)
        public ContinuablePagedIterable(Supplier<PageRetrieverSync<C, P>> pageRetrieverSyncProvider, Integer pageSize, Predicate<C> continuationPredicate)
        public Iterable<P> iterableByPage()
        public Iterable<P> iterableByPage(C continuationToken)
        public Iterable<P> iterableByPage(int preferredPageSize)
        public Iterable<P> iterableByPage(C continuationToken, int preferredPageSize)
        @Override public Iterator<T> iterator()
        @Override public Stream<T> stream()
        public Stream<P> streamByPage()
        public Stream<P> streamByPage(C continuationToken)
        public Stream<P> streamByPage(int preferredPageSize)
        public Stream<P> streamByPage(C continuationToken, int preferredPageSize)
    }
    @FunctionalInterface
    public interface PageRetriever<C, P> {
        Flux<P> get(C continuationToken, Integer pageSize)
    }
    @FunctionalInterface
    public interface PageRetrieverSync<C, P> {
        P getPage(C continuationToken, Integer pageSize)
    }
}
package com.azure.core.util.polling {
    public final class AsyncPollResponse<T, U> {
        public Mono<T> cancelOperation()
        public Mono<U> getFinalResult()
        public LongRunningOperationStatus getStatus()
        public T getValue()
    }
    public final class ChainedPollingStrategy<T, U> implements PollingStrategy<T, U> {
        public ChainedPollingStrategy(List<PollingStrategy<T, U>> strategies)
        @Override public Mono<T> cancel(PollingContext<T> pollingContext, PollResponse<T> initialResponse)
        @Override public Mono<Boolean> canPoll(Response<?> initialResponse)
        @Override public Mono<PollResponse<T>> onInitialResponse(Response<?> response, PollingContext<T> pollingContext, TypeReference<T> pollResponseType)
        @Override public Mono<PollResponse<T>> poll(PollingContext<T> context, TypeReference<T> pollResponseType)
        @Override public Mono<U> getResult(PollingContext<T> context, TypeReference<U> resultType)
    }
    public final class DefaultPollingStrategy<T, U> implements PollingStrategy<T, U> {
        public DefaultPollingStrategy(HttpPipeline httpPipeline)
        public DefaultPollingStrategy(PollingStrategyOptions pollingStrategyOptions)
        public DefaultPollingStrategy(HttpPipeline httpPipeline, JsonSerializer serializer)
        public DefaultPollingStrategy(HttpPipeline httpPipeline, JsonSerializer serializer, Context context)
        public DefaultPollingStrategy(HttpPipeline httpPipeline, String endpoint, JsonSerializer serializer, Context context)
        @Override public Mono<Boolean> canPoll(Response<?> initialResponse)
        @Override public Mono<PollResponse<T>> onInitialResponse(Response<?> response, PollingContext<T> pollingContext, TypeReference<T> pollResponseType)
        @Override public Mono<PollResponse<T>> poll(PollingContext<T> context, TypeReference<T> pollResponseType)
        @Override public Mono<U> getResult(PollingContext<T> context, TypeReference<U> resultType)
    }
    public class LocationPollingStrategy<T, U> implements PollingStrategy<T, U> {
        public LocationPollingStrategy(HttpPipeline httpPipeline)
        public LocationPollingStrategy(PollingStrategyOptions pollingStrategyOptions)
        public LocationPollingStrategy(HttpPipeline httpPipeline, ObjectSerializer serializer)
        public LocationPollingStrategy(HttpPipeline httpPipeline, ObjectSerializer serializer, Context context)
        public LocationPollingStrategy(HttpPipeline httpPipeline, String endpoint, ObjectSerializer serializer, Context context)
        @Override public Mono<Boolean> canPoll(Response<?> initialResponse)
        @Override public Mono<PollResponse<T>> onInitialResponse(Response<?> response, PollingContext<T> pollingContext, TypeReference<T> pollResponseType)
        @Override public Mono<PollResponse<T>> poll(PollingContext<T> pollingContext, TypeReference<T> pollResponseType)
        @Override public Mono<U> getResult(PollingContext<T> pollingContext, TypeReference<U> resultType)
    }
    public final class LongRunningOperationStatus extends ExpandableStringEnum<LongRunningOperationStatus> {
        public static final LongRunningOperationStatus NOT_STARTED = fromString("NOT_STARTED", false);
        public static final LongRunningOperationStatus IN_PROGRESS = fromString("IN_PROGRESS", false);
        public static final LongRunningOperationStatus SUCCESSFULLY_COMPLETED = fromString("SUCCESSFULLY_COMPLETED", true);
        public static final LongRunningOperationStatus FAILED = fromString("FAILED", true);
        public static final LongRunningOperationStatus USER_CANCELLED = fromString("USER_CANCELLED", true);
        @Deprecated public LongRunningOperationStatus()
        public boolean isComplete()
        public static LongRunningOperationStatus fromString(String name, boolean isComplete)
    }
    public class OperationResourcePollingStrategy<T, U> implements PollingStrategy<T, U> {
        public OperationResourcePollingStrategy(HttpPipeline httpPipeline)
        public OperationResourcePollingStrategy(HttpHeaderName operationLocationHeaderName, PollingStrategyOptions pollingStrategyOptions)
        public OperationResourcePollingStrategy(HttpPipeline httpPipeline, ObjectSerializer serializer, String operationLocationHeaderName)
        public OperationResourcePollingStrategy(HttpPipeline httpPipeline, ObjectSerializer serializer, String operationLocationHeaderName, Context context)
        public OperationResourcePollingStrategy(HttpPipeline httpPipeline, String endpoint, ObjectSerializer serializer, String operationLocationHeaderName, Context context)
        @Override public Mono<Boolean> canPoll(Response<?> initialResponse)
        @Override public Mono<PollResponse<T>> onInitialResponse(Response<?> response, PollingContext<T> pollingContext, TypeReference<T> pollResponseType)
        @Override public Mono<PollResponse<T>> poll(PollingContext<T> pollingContext, TypeReference<T> pollResponseType)
        @Override public Mono<U> getResult(PollingContext<T> pollingContext, TypeReference<U> resultType)
    }
    @Immutable
    public final class PollOperationDetails implements JsonSerializable<PollOperationDetails> {
        public ResponseError getError()
        public String getOperationId()
    }
    public final class PollResponse<T> {
        public PollResponse(LongRunningOperationStatus status, T value)
        public PollResponse(LongRunningOperationStatus status, T value, Duration retryAfter)
        public Duration getRetryAfter()
        public LongRunningOperationStatus getStatus()
        public T getValue()
    }
    public final class PollerFlux<T, U> extends Flux<AsyncPollResponse<T, U>> {
        public PollerFlux(Duration pollInterval, Function<PollingContext<T>, Mono<T>> activationOperation, Function<PollingContext<T>, Mono<PollResponse<T>>> pollOperation, BiFunction<PollingContext<T>, PollResponse<T>, Mono<T>> cancelOperation, Function<PollingContext<T>, Mono<U>> fetchResultOperation)
        public static <T, U> PollerFlux<T, U> create(Duration pollInterval, Function<PollingContext<T>, Mono<PollResponse<T>>> activationOperation, Function<PollingContext<T>, Mono<PollResponse<T>>> pollOperation, BiFunction<PollingContext<T>, PollResponse<T>, Mono<T>> cancelOperation, Function<PollingContext<T>, Mono<U>> fetchResultOperation)
        public static <T, U> PollerFlux<T, U> create(Duration pollInterval, Supplier<Mono<? extends Response<?>>> initialOperation, PollingStrategy<T, U> strategy, TypeReference<T> pollResponseType, TypeReference<U> resultType)
        public static <T, U> PollerFlux<T, U> error(Exception ex)
        public Duration getPollInterval()
        public PollerFlux<T, U> setPollInterval(Duration pollInterval)
        @Override public void subscribe(CoreSubscriber<? super AsyncPollResponse<T, U>> actual)
        public SyncPoller<T, U> getSyncPoller()
    }
    public final class PollingContext<T> {
        public PollResponse<T> getActivationResponse()
        public String getData(String name)
        public PollingContext<T> setData(String name, String value)
        public PollResponse<T> getLatestResponse()
    }
    public interface PollingStrategy<T, U> {
        default Mono<T> cancel(PollingContext<T> pollingContext, PollResponse<T> initialResponse)
        Mono<Boolean> canPoll(Response<?> initialResponse)
        Mono<PollResponse<T>> onInitialResponse(Response<?> response, PollingContext<T> pollingContext, TypeReference<T> pollResponseType)
        Mono<PollResponse<T>> poll(PollingContext<T> pollingContext, TypeReference<T> pollResponseType)
        Mono<U> getResult(PollingContext<T> pollingContext, TypeReference<U> resultType)
    }
    @Fluent
    public final class PollingStrategyOptions {
        public PollingStrategyOptions(HttpPipeline httpPipeline)
        public Context getContext()
        public PollingStrategyOptions setContext(Context context)
        public String getEndpoint()
        public PollingStrategyOptions setEndpoint(String endpoint)
        public HttpPipeline getHttpPipeline()
        public ObjectSerializer getSerializer()
        public PollingStrategyOptions setSerializer(ObjectSerializer serializer)
        public String getServiceVersion()
        public PollingStrategyOptions setServiceVersion(String serviceVersion)
    }
    public class StatusCheckPollingStrategy<T, U> implements PollingStrategy<T, U> {
        public StatusCheckPollingStrategy()
        public StatusCheckPollingStrategy(ObjectSerializer serializer)
        @Override public Mono<Boolean> canPoll(Response<?> initialResponse)
        @Override public Mono<PollResponse<T>> onInitialResponse(Response<?> response, PollingContext<T> pollingContext, TypeReference<T> pollResponseType)
        @Override public Mono<PollResponse<T>> poll(PollingContext<T> context, TypeReference<T> pollResponseType)
        @Override public Mono<U> getResult(PollingContext<T> pollingContext, TypeReference<U> resultType)
    }
    public final class SyncChainedPollingStrategy<T, U> implements SyncPollingStrategy<T, U> {
        public SyncChainedPollingStrategy(List<SyncPollingStrategy<T, U>> strategies)
        @Override public T cancel(PollingContext<T> pollingContext, PollResponse<T> initialResponse)
        @Override public boolean canPoll(Response<?> initialResponse)
        @Override public PollResponse<T> onInitialResponse(Response<?> response, PollingContext<T> pollingContext, TypeReference<T> pollResponseType)
        @Override public PollResponse<T> poll(PollingContext<T> context, TypeReference<T> pollResponseType)
        @Override public U getResult(PollingContext<T> context, TypeReference<U> resultType)
    }
    public final class SyncDefaultPollingStrategy<T, U> implements SyncPollingStrategy<T, U> {
        public SyncDefaultPollingStrategy(HttpPipeline httpPipeline)
        public SyncDefaultPollingStrategy(PollingStrategyOptions pollingStrategyOptions)
        public SyncDefaultPollingStrategy(HttpPipeline httpPipeline, JsonSerializer serializer)
        public SyncDefaultPollingStrategy(HttpPipeline httpPipeline, JsonSerializer serializer, Context context)
        public SyncDefaultPollingStrategy(HttpPipeline httpPipeline, String endpoint, JsonSerializer serializer, Context context)
        @Override public T cancel(PollingContext<T> pollingContext, PollResponse<T> initialResponse)
        @Override public boolean canPoll(Response<?> initialResponse)
        @Override public PollResponse<T> onInitialResponse(Response<?> response, PollingContext<T> pollingContext, TypeReference<T> pollResponseType)
        @Override public PollResponse<T> poll(PollingContext<T> pollingContext, TypeReference<T> pollResponseType)
        @Override public U getResult(PollingContext<T> pollingContext, TypeReference<U> resultType)
    }
    public class SyncLocationPollingStrategy<T, U> implements SyncPollingStrategy<T, U> {
        public SyncLocationPollingStrategy(HttpPipeline httpPipeline)
        public SyncLocationPollingStrategy(PollingStrategyOptions pollingStrategyOptions)
        public SyncLocationPollingStrategy(HttpPipeline httpPipeline, ObjectSerializer serializer)
        public SyncLocationPollingStrategy(HttpPipeline httpPipeline, ObjectSerializer serializer, Context context)
        public SyncLocationPollingStrategy(HttpPipeline httpPipeline, String endpoint, ObjectSerializer serializer, Context context)
        @Override public boolean canPoll(Response<?> initialResponse)
        @Override public PollResponse<T> onInitialResponse(Response<?> response, PollingContext<T> pollingContext, TypeReference<T> pollResponseType)
        @Override public PollResponse<T> poll(PollingContext<T> pollingContext, TypeReference<T> pollResponseType)
        @Override public U getResult(PollingContext<T> pollingContext, TypeReference<U> resultType)
    }
    public class SyncOperationResourcePollingStrategy<T, U> implements SyncPollingStrategy<T, U> {
        public SyncOperationResourcePollingStrategy(HttpPipeline httpPipeline)
        public SyncOperationResourcePollingStrategy(HttpHeaderName operationLocationHeaderName, PollingStrategyOptions pollingStrategyOptions)
        public SyncOperationResourcePollingStrategy(HttpPipeline httpPipeline, ObjectSerializer serializer, String operationLocationHeaderName)
        public SyncOperationResourcePollingStrategy(HttpPipeline httpPipeline, ObjectSerializer serializer, String operationLocationHeaderName, Context context)
        public SyncOperationResourcePollingStrategy(HttpPipeline httpPipeline, String endpoint, ObjectSerializer serializer, String operationLocationHeaderName, Context context)
        @Override public boolean canPoll(Response<?> initialResponse)
        @Override public PollResponse<T> onInitialResponse(Response<?> response, PollingContext<T> pollingContext, TypeReference<T> pollResponseType)
        @Override public PollResponse<T> poll(PollingContext<T> pollingContext, TypeReference<T> pollResponseType)
        @Override public U getResult(PollingContext<T> pollingContext, TypeReference<U> resultType)
    }
    public interface SyncPoller<T, U> {
        void cancelOperation()
        static <T, U> SyncPoller<T, U> createPoller(Duration pollInterval, Function<PollingContext<T>, PollResponse<T>> syncActivationOperation, Function<PollingContext<T>, PollResponse<T>> pollOperation, BiFunction<PollingContext<T>, PollResponse<T>, T> cancelOperation, Function<PollingContext<T>, U> fetchResultOperation)
        static <T, U> SyncPoller<T, U> createPoller(Duration pollInterval, Supplier<Response<?>> initialOperation, SyncPollingStrategy<T, U> strategy, TypeReference<T> pollResponseType, TypeReference<U> resultType)
        U getFinalResult()
        default U getFinalResult(Duration timeout)
        PollResponse<T> poll()
        default SyncPoller<T, U> setPollInterval(Duration pollInterval)
        PollResponse<T> waitForCompletion()
        PollResponse<T> waitForCompletion(Duration timeout)
        PollResponse<T> waitUntil(LongRunningOperationStatus statusToWaitFor)
        PollResponse<T> waitUntil(Duration timeout, LongRunningOperationStatus statusToWaitFor)
    }
    public interface SyncPollingStrategy<T, U> {
        default T cancel(PollingContext<T> pollingContext, PollResponse<T> initialResponse)
        boolean canPoll(Response<?> initialResponse)
        PollResponse<T> onInitialResponse(Response<?> response, PollingContext<T> pollingContext, TypeReference<T> pollResponseType)
        PollResponse<T> poll(PollingContext<T> pollingContext, TypeReference<T> pollResponseType)
        U getResult(PollingContext<T> pollingContext, TypeReference<U> resultType)
    }
    public class SyncStatusCheckPollingStrategy<T, U> implements SyncPollingStrategy<T, U> {
        public SyncStatusCheckPollingStrategy()
        public SyncStatusCheckPollingStrategy(ObjectSerializer serializer)
        @Override public boolean canPoll(Response<?> initialResponse)
        @Override public PollResponse<T> onInitialResponse(Response<?> response, PollingContext<T> pollingContext, TypeReference<T> pollResponseType)
        @Override public PollResponse<T> poll(PollingContext<T> context, TypeReference<T> pollResponseType)
        @Override public U getResult(PollingContext<T> pollingContext, TypeReference<U> resultType)
    }
}
package com.azure.core.util.serializer {
    public enum CollectionFormat {
        CSV(","),
        SSV(" "),
        TSV("\t"),
        PIPES("|"),
        MULTI("&");
        public String getDelimiter()
    }
    public class JacksonAdapter implements SerializerAdapter {
        public JacksonAdapter()
        @Deprecated public JacksonAdapter(BiConsumer<ObjectMapper, ObjectMapper> configureSerialization)
        public static SerializerAdapter createDefaultSerializerAdapter()
        @Override public <T> T deserialize(HttpHeaders headers, Type deserializedHeadersType) throws IOException
        @Override public <T> T deserialize(String value, Type type, SerializerEncoding encoding) throws IOException
        @Override public <T> T deserialize(byte[] bytes, Type type, SerializerEncoding encoding) throws IOException
        @Override public <T> T deserialize(InputStream inputStream, Type type, SerializerEncoding encoding) throws IOException
        @Override public <T> T deserializeHeader(Header header, Type type) throws IOException
        @Override public String serialize(Object object, SerializerEncoding encoding) throws IOException
        @Override public void serialize(Object object, SerializerEncoding encoding, OutputStream outputStream) throws IOException
        @Override public String serializeList(List<?> list, CollectionFormat format)
        @Deprecated public ObjectMapper serializer()
        @Override public String serializeRaw(Object object)
        @Override public byte[] serializeToBytes(Object object, SerializerEncoding encoding) throws IOException
        @Deprecated protected ObjectMapper simpleMapper()
    }
    public interface JsonSerializer extends ObjectSerializer {
        @Override <T> T deserialize(InputStream stream, TypeReference<T> typeReference)
        @Override <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference)
        @Override default <T> T deserializeFromBytes(byte[] data, TypeReference<T> typeReference)
        @Override default <T> Mono<T> deserializeFromBytesAsync(byte[] data, TypeReference<T> typeReference)
        @Override void serialize(OutputStream stream, Object value)
        @Override Mono<Void> serializeAsync(OutputStream stream, Object value)
        @Override default byte[] serializeToBytes(Object value)
        @Override default Mono<byte[]> serializeToBytesAsync(Object value)
    }
    public interface JsonSerializerProvider {
        JsonSerializer createInstance()
    }
    public final class JsonSerializerProviders {
        public static JsonSerializer createInstance()
        public static JsonSerializer createInstance(boolean useDefaultIfAbsent)
    }
    public interface MemberNameConverter {
        String convertMemberName(Member member)
    }
    public interface MemberNameConverterProvider {
        MemberNameConverter createInstance()
    }
    public final class MemberNameConverterProviders {
        public static MemberNameConverter createInstance()
    }
    public interface ObjectSerializer {
        <T> T deserialize(InputStream stream, TypeReference<T> typeReference)
        <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference)
        default <T> T deserializeFromBytes(byte[] data, TypeReference<T> typeReference)
        default <T> Mono<T> deserializeFromBytesAsync(byte[] data, TypeReference<T> typeReference)
        void serialize(OutputStream stream, Object value)
        Mono<Void> serializeAsync(OutputStream stream, Object value)
        default byte[] serializeToBytes(Object value)
        default Mono<byte[]> serializeToBytesAsync(Object value)
    }
    public interface SerializerAdapter {
        <T> T deserialize(HttpHeaders headers, Type type) throws IOException
        <T> T deserialize(String value, Type type, SerializerEncoding encoding) throws IOException
        default <T> T deserialize(byte[] bytes, Type type, SerializerEncoding encoding) throws IOException
        default <T> T deserialize(InputStream inputStream, Type type, SerializerEncoding encoding) throws IOException
        default <T> T deserializeHeader(Header header, Type type) throws IOException
        String serialize(Object object, SerializerEncoding encoding) throws IOException
        default void serialize(Object object, SerializerEncoding encoding, OutputStream outputStream) throws IOException
        default String serializeIterable(Iterable<?> iterable, CollectionFormat format)
        String serializeList(List<?> list, CollectionFormat format)
        String serializeRaw(Object object)
        default byte[] serializeToBytes(Object object, SerializerEncoding encoding) throws IOException
    }
    public enum SerializerEncoding {
        JSON,
        XML,
        TEXT;
        public static SerializerEncoding fromHeaders(HttpHeaders headers)
    }
    public abstract class TypeReference<T> {
        public TypeReference()
        public static <T> TypeReference<T> createInstance(Class<T> clazz)
        public Class<T> getJavaClass()
        public Type getJavaType()
    }
}
package com.azure.core.util.tracing {
    @Deprecated
    public enum ProcessKind {
        SEND,
        MESSAGE,
        PROCESS;
    }
    public enum SpanKind {
        INTERNAL,
        CLIENT,
        SERVER,
        PRODUCER,
        CONSUMER;
    }
    @Fluent
    public final class StartSpanOptions {
        public StartSpanOptions(SpanKind kind)
        public StartSpanOptions addLink(TracingLink link)
        public StartSpanOptions setAttribute(String key, Object value)
        public Map<String, Object> getAttributes()
        public List<TracingLink> getLinks()
        public Context getRemoteParent()
        public StartSpanOptions setRemoteParent(Context parent)
        public SpanKind getSpanKind()
        public Instant getStartTimestamp()
        public StartSpanOptions setStartTimestamp(Instant timestamp)
    }
    public interface Tracer {
        @Deprecated String PARENT_SPAN_KEY = "parent-span";
        String PARENT_TRACE_CONTEXT_KEY = "trace-context";
        @Deprecated String USER_SPAN_NAME_KEY = "user-span-name";
        String ENTITY_PATH_KEY = "entity-path";
        String HOST_NAME_KEY = "hostname";
        String SPAN_CONTEXT_KEY = "span-context";
        @Deprecated String DIAGNOSTIC_ID_KEY = "Diagnostic-Id";
        @Deprecated String SCOPE_KEY = "scope";
        @Deprecated String AZ_TRACING_NAMESPACE_KEY = "az.namespace";
        @Deprecated String SPAN_BUILDER_KEY = "builder";
        @Deprecated String MESSAGE_ENQUEUED_TIME = "x-opt-enqueued-time";
        String DISABLE_TRACING_KEY = "disable-tracing";
        @Deprecated default void addEvent(String name, Map<String, Object> attributes, OffsetDateTime timestamp)
        default void addEvent(String name, Map<String, Object> attributes, OffsetDateTime timestamp, Context context)
        @Deprecated default void addLink(Context context)
        void setAttribute(String key, String value, Context context)
        default void setAttribute(String key, long value, Context context)
        default void setAttribute(String key, Object value, Context context)
        default boolean isEnabled()
        @Deprecated default void end(int responseCode, Throwable error, Context context)
        void end(String errorMessage, Throwable throwable, Context context)
        default Context extractContext(Function<String, String> headerGetter)
        @Deprecated default Context extractContext(String diagnosticId, Context context)
        default void injectContext(BiConsumer<String, String> headerSetter, Context context)
        default AutoCloseable makeSpanCurrent(Context context)
        default boolean isRecording(Context span)
        @Deprecated default Context getSharedSpanBuilder(String spanName, Context context)
        @Deprecated default Context setSpanName(String spanName, Context context)
        Context start(String methodName, Context context)
        default Context start(String methodName, StartSpanOptions options, Context context)
        @Deprecated default Context start(String spanName, Context context, ProcessKind processKind)
    }
    public interface TracerProvider {
        default Tracer createTracer(LibraryTelemetryOptions libraryOptions, TracingOptions options)
        Tracer createTracer(String libraryName, String libraryVersion, String azNamespace, TracingOptions options)
        static TracerProvider getDefaultProvider()
    }
    @Deprecated
    public final class TracerProxy {
        public static void setAttribute(String key, String value, Context context)
        public static void end(int responseCode, Throwable error, Context context)
        public static Context setSpanName(String spanName, Context context)
        public static Context start(String methodName, Context context)
        public static Context start(String methodName, StartSpanOptions spanOptions, Context context)
        public static boolean isTracingEnabled()
    }
    @Immutable
    public class TracingLink {
        public TracingLink(Context context)
        public TracingLink(Context context, Map<String, Object> attributes)
        public Map<String, Object> getAttributes()
        public Context getContext()
    }
}
```