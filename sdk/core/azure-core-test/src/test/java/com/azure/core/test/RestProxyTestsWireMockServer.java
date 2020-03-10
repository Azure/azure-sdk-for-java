package com.azure.core.test;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Delete;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.FormParam;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.Head;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Headers;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.Patch;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.ContentType;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.StreamResponse;
import com.azure.core.test.implementation.entities.HttpBinFormDataJSON;
import com.azure.core.test.implementation.entities.HttpBinHeaders;
import com.azure.core.test.implementation.entities.HttpBinJSON;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

public final class RestProxyTestsWireMockServer {
    public static WireMockServer getRestProxyTestsServer() {
        WireMockServer server = new WireMockServer(WireMockConfiguration.options()
            .extensions(new RestProxyResponseTransformer())
            .port(21354)
            .httpsPort(21355)
            .disableRequestJournal());

        // Null body response.
        server.stubFor(WireMock.get(urlPathMatching("/bytes/0")));

        // Response that is a random 100 bytes.
        server.stubFor(WireMock.get(urlPathMatching("/bytes/100")));

        // Response that will returned the URL string as-is.
        server.stubFor(get(urlPathMatching("/anything.*")));

        return server;
    }

    private static final class RestProxyResponseTransformer extends ResponseDefinitionTransformer {
        @Override
        public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition,
            FileSource fileSource, Parameters parameters) {
            try {
                return transformImpl(request, responseDefinition, fileSource, parameters);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private ResponseDefinition transformImpl(Request request, ResponseDefinition responseDefinition,
            FileSource fileSource, Parameters parameters) throws IOException {
            URL url = new URL(request.getAbsoluteUrl());

            if (url.getPath().startsWith("/bytes")) {
                return new ResponseDefinitionBuilder()
                    .withStatus(200)
                    .withBody(new SecureRandom().generateSeed(Integer.parseInt(url.getPath().split("/", 3)[2])))
                    .build();
            } else if (url.getPath().startsWith("/anything")) {
                HttpBinJSON responseBody = new HttpBinJSON();
                responseBody.url(URLDecoder.decode(request.getAbsoluteUrl(), "UTF-8"));

                return new ResponseDefinitionBuilder()
                    .withStatus(200)
                    .withBody(new JacksonAdapter().serialize(responseBody, SerializerEncoding.JSON))
                    .build();
            } else {
                return responseDefinition;
            }
        }

        @Override
        public String getName() {
            return "rest-proxy-transformer";
        }
    }

    @Host("http://httpbin.org")
    @ServiceInterface(name = "Service7")
    private interface Service7 {
        @Get("anything")
        @ExpectedResponses({200})
        HttpBinJSON getAnything(@HeaderParam("a") String a, @HeaderParam("b") int b);

        @Get("anything")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> getAnythingAsync(@HeaderParam("a") String a, @HeaderParam("b") int b);
    }

    @Host("http://httpbin.org")
    @ServiceInterface(name = "Service8")
    private interface Service8 {
        @Post("post")
        @ExpectedResponses({200})
        HttpBinJSON post(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String postBody);

        @Post("post")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> postAsync(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String postBody);
    }

    @Host("http://httpbin.org")
    @ServiceInterface(name = "Service9")
    private interface Service9 {
        @Put("put")
        @ExpectedResponses({200})
        HttpBinJSON put(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) int putBody);

        @Put("put")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> putAsync(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) int putBody);

        @Put("put")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(MyRestException.class)
        HttpBinJSON putBodyAndContentLength(@BodyParam("application/octet-stream") ByteBuffer body,
            @HeaderParam("Content-Length") long contentLength);

        @Put("put")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(MyRestException.class)
        Mono<HttpBinJSON> putAsyncBodyAndContentLength(@BodyParam("application/octet-stream") Flux<ByteBuffer> body,
            @HeaderParam("Content-Length") long contentLength);

        @Put("put")
        @ExpectedResponses({201})
        HttpBinJSON putWithUnexpectedResponse(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        Mono<HttpBinJSON> putWithUnexpectedResponseAsync(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(MyRestException.class)
        HttpBinJSON putWithUnexpectedResponseAndExceptionType(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(MyRestException.class)
        Mono<HttpBinJSON> putWithUnexpectedResponseAndExceptionTypeAsync(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {200}, value = MyRestException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        HttpBinJSON putWithUnexpectedResponseAndDeterminedExceptionType(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {200}, value = MyRestException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<HttpBinJSON> putWithUnexpectedResponseAndDeterminedExceptionTypeAsync(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {400}, value = HttpResponseException.class)
        @UnexpectedResponseExceptionType(MyRestException.class)
        HttpBinJSON putWithUnexpectedResponseAndFallthroughExceptionType(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {400}, value = HttpResponseException.class)
        @UnexpectedResponseExceptionType(MyRestException.class)
        Mono<HttpBinJSON> putWithUnexpectedResponseAndFallthroughExceptionTypeAsync(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {400}, value = MyRestException.class)
        HttpBinJSON putWithUnexpectedResponseAndNoFallthroughExceptionType(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(code = {400}, value = MyRestException.class)
        Mono<HttpBinJSON> putWithUnexpectedResponseAndNoFallthroughExceptionTypeAsync(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String putBody);
    }

    @Host("http://httpbin.org")
    @ServiceInterface(name = "Service10")
    private interface Service10 {
        @Head("anything")
        @ExpectedResponses({200})
        Response<Void> head();

        @Head("anything")
        @ExpectedResponses({200})
        boolean headBoolean();

        @Head("anything")
        @ExpectedResponses({200})
        void voidHead();

        @Head("anything")
        @ExpectedResponses({200})
        Mono<Response<Void>> headAsync();

        @Head("anything")
        @ExpectedResponses({200})
        Mono<Boolean> headBooleanAsync();

        @Head("anything")
        @ExpectedResponses({200})
        Mono<Void> completableHeadAsync();
    }

    @Host("http://httpbin.org")
    @ServiceInterface(name = "Service11")
    private interface Service11 {
        @Delete("delete")
        @ExpectedResponses({200})
        HttpBinJSON delete(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) boolean bodyBoolean);

        @Delete("delete")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> deleteAsync(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) boolean bodyBoolean);
    }

    @Host("http://httpbin.org")
    @ServiceInterface(name = "Service12")
    private interface Service12 {
        @Patch("patch")
        @ExpectedResponses({200})
        HttpBinJSON patch(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String bodyString);

        @Patch("patch")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> patchAsync(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String bodyString);
    }

    @Host("http://httpbin.org")
    @ServiceInterface(name = "Service13")
    private interface Service13 {
        @Get("anything")
        @ExpectedResponses({200})
        @Headers({"MyHeader:MyHeaderValue", "MyOtherHeader:My,Header,Value"})
        HttpBinJSON get();

        @Get("anything")
        @ExpectedResponses({200})
        @Headers({"MyHeader:MyHeaderValue", "MyOtherHeader:My,Header,Value"})
        Mono<HttpBinJSON> getAsync();
    }

    @Host("https://httpbin.org")
    @ServiceInterface(name = "Service14")
    private interface Service14 {
        @Get("anything")
        @ExpectedResponses({200})
        @Headers({"MyHeader:MyHeaderValue"})
        HttpBinJSON get();

        @Get("anything")
        @ExpectedResponses({200})
        @Headers({"MyHeader:MyHeaderValue"})
        Mono<HttpBinJSON> getAsync();
    }

    @Host("https://httpbin.org")
    @ServiceInterface(name = "Service16")
    private interface Service16 {
        @Put("put")
        @ExpectedResponses({200})
        HttpBinJSON putByteArray(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] bytes);

        @Put("put")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> putByteArrayAsync(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] bytes);
    }

    @Host("http://{hostPart1}{hostPart2}.org")
    @ServiceInterface(name = "Service17")
    private interface Service17 {
        @Get("get")
        @ExpectedResponses({200})
        HttpBinJSON get(@HostParam("hostPart1") String hostPart1, @HostParam("hostPart2") String hostPart2);

        @Get("get")
        @ExpectedResponses({200})
        Mono<HttpBinJSON> getAsync(@HostParam("hostPart1") String hostPart1, @HostParam("hostPart2") String hostPart2);
    }

    @Host("https://httpbin.org")
    @ServiceInterface(name = "Service18")
    private interface Service18 {
        @Get("status/200")
        void getStatus200();

        @Get("status/200")
        @ExpectedResponses({200})
        void getStatus200WithExpectedResponse200();

        @Get("status/300")
        void getStatus300();

        @Get("status/300")
        @ExpectedResponses({300})
        void getStatus300WithExpectedResponse300();

        @Get("status/400")
        void getStatus400();

        @Get("status/400")
        @ExpectedResponses({400})
        void getStatus400WithExpectedResponse400();

        @Get("status/500")
        void getStatus500();

        @Get("status/500")
        @ExpectedResponses({500})
        void getStatus500WithExpectedResponse500();
    }

    @Host("http://httpbin.org")
    @ServiceInterface(name = "Service19")
    private interface Service19 {
        @Put("put")
        HttpBinJSON putWithNoContentTypeAndStringBody(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        HttpBinJSON putWithNoContentTypeAndByteArrayBody(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] body);

        @Put("put")
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndStringBody(
            @BodyParam(ContentType.APPLICATION_JSON) String body);

        @Put("put")
        @Headers({"Content-Type: application/json"})
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndByteArrayBody(
            @BodyParam(ContentType.APPLICATION_JSON) byte[] body);

        @Put("put")
        @Headers({"Content-Type: application/json; charset=utf-8"})
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        @Headers({"Content-Type: application/octet-stream"})
        HttpBinJSON putWithHeaderApplicationOctetStreamContentTypeAndStringBody(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        @Headers({"Content-Type: application/octet-stream"})
        HttpBinJSON putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] body);

        @Put("put")
        HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndStringBody(
            @BodyParam(ContentType.APPLICATION_JSON) String body);

        @Put("put")
        HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(
            @BodyParam(ContentType.APPLICATION_JSON + "; charset=utf-8") String body);

        @Put("put")
        HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(
            @BodyParam(ContentType.APPLICATION_JSON) byte[] body);

        @Put("put")
        HttpBinJSON putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        HttpBinJSON putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] body);
    }

    @Host("http://httpbin.org")
    @ServiceInterface(name = "Service20")
    private interface Service20 {
        @Get("bytes/100")
        ResponseBase<HttpBinHeaders, Void> getBytes100OnlyHeaders();

        @Get("bytes/100")
        ResponseBase<HttpHeaders, Void> getBytes100OnlyRawHeaders();

        @Get("bytes/100")
        ResponseBase<HttpBinHeaders, byte[]> getBytes100BodyAndHeaders();

        @Put("put")
        ResponseBase<HttpBinHeaders, Void> putOnlyHeaders(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Put("put")
        ResponseBase<HttpBinHeaders, HttpBinJSON> putBodyAndHeaders(
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @Get("bytes/100")
        ResponseBase<Void, Void> getBytesOnlyStatus();

        @Get("bytes/100")
        Response<Void> getVoidResponse();

        @Put("put")
        Response<HttpBinJSON> putBody(@BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);
    }

    @Host("http://httpbin.org")
    @ServiceInterface(name = "UnexpectedOKService")
    interface UnexpectedOKService {
        @Get("/bytes/1024")
        @ExpectedResponses({400})
        StreamResponse getBytes();
    }

    @Host("https://www.example.com")
    @ServiceInterface(name = "Service21")
    private interface Service21 {
        @Get("http://httpbin.org/bytes/100")
        @ExpectedResponses({200})
        byte[] getBytes100();
    }

    @Host("http://httpbin.org")
    @ServiceInterface(name = "DownloadService")
    interface DownloadService {
        @Get("/bytes/30720")
        StreamResponse getBytes();

        @Get("/bytes/30720")
        Flux<ByteBuffer> getBytesFlux();
    }

    @Host("https://httpbin.org")
    @ServiceInterface(name = "FlowableUploadService")
    interface FlowableUploadService {
        @Put("/put")
        Response<HttpBinJSON> put(@BodyParam("text/plain") Flux<ByteBuffer> content,
            @HeaderParam("Content-Length") long contentLength);
    }

    @Host("{url}")
    @ServiceInterface(name = "Service22")
    interface Service22 {
        @Get("/")
        byte[] getBytes(@HostParam("url") String url);
    }

    @Host("http://httpbin.org/")
    @ServiceInterface(name = "Service23")
    interface Service23 {
        @Get("bytes/28")
        byte[] getBytes();
    }

    @Host("http://httpbin.org/")
    @ServiceInterface(name = "Service24")
    interface Service24 {
        @Put("put")
        HttpBinJSON put(@HeaderParam("ABC") Map<String, String> headerCollection);
    }

    @Host("http://httpbin.org/")
    @ServiceInterface(name = "Service26")
    interface Service26 {
        @Post("post")
        HttpBinFormDataJSON postForm(@FormParam("custname") String name, @FormParam("custtel") String telephone,
            @FormParam("custemail") String email, @FormParam("size") HttpBinFormDataJSON.PizzaSize size,
            @FormParam("toppings") List<String> toppings);
    }
}
