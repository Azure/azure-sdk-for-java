// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Headers;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.http.rest.StreamResponse;
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.appservice.models.DeployType;
import com.azure.resourcemanager.appservice.models.KuduAuthenticationPolicy;
import com.azure.resourcemanager.appservice.models.WebAppBase;
import com.azure.resourcemanager.resources.fluentcore.policy.AuthenticationPolicy;
import com.azure.resourcemanager.resources.fluentcore.policy.AuxiliaryAuthenticationPolicy;
import com.azure.resourcemanager.resources.fluentcore.policy.ProviderRegistrationPolicy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/** A client which interacts with Kudu service. */
class KuduClient {

    private final ClientLogger logger = new ClientLogger(getClass());

    private final String host;
    private final KuduService service;

    KuduClient(WebAppBase webAppBase) {
        if (webAppBase.defaultHostname() == null) {
            throw logger.logExceptionAsError(
                new UnsupportedOperationException("Cannot initialize kudu client before web app is created"));
        }
        String host = webAppBase.defaultHostname().toLowerCase(Locale.ROOT)
            .replace("http://", "")
            .replace("https://", "");
        String[] parts = host.split("\\.", 2);
        host = parts[0] + ".scm." + parts[1];
        this.host = "https://" + host;
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        for (int i = 0, count = webAppBase.manager().httpPipeline().getPolicyCount(); i < count; ++i) {
            HttpPipelinePolicy policy = webAppBase.manager().httpPipeline().getPolicy(i);
            if (!(policy instanceof AuthenticationPolicy)
                && !(policy instanceof ProviderRegistrationPolicy)
                && !(policy instanceof AuxiliaryAuthenticationPolicy)) {
                policies.add(policy);
            }
        }
        policies.add(new KuduAuthenticationPolicy(webAppBase));
        HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(webAppBase.manager().httpPipeline().getHttpClient())
            .build();

        service = RestProxy.create(KuduService.class, httpPipeline,
            SerializerFactory.createDefaultManagementSerializerAdapter());
    }

    @Host("{$host}")
    @ServiceInterface(name = "KuduService")
    private interface KuduService {
        @Get("api/logstream/application")
        Mono<StreamResponse> streamApplicationLogs(@HostParam("$host") String host);

        @Get("api/logstream/http")
        Mono<StreamResponse> streamHttpLogs(@HostParam("$host") String host);

        @Get("api/logstream/kudu/trace")
        Mono<StreamResponse> streamTraceLogs(@HostParam("$host") String host);

        @Get("api/logstream/kudu/deployment")
        Mono<StreamResponse> streamDeploymentLogs(@HostParam("$host") String host);

        @Get("api/logstream")
        Mono<StreamResponse> streamAllLogs(@HostParam("$host") String host);

//        @Headers({
//            "Content-Type: application/octet-stream",
//            "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps warDeploy",
//            "x-ms-body-logging: false"
//        })
//        @Post("api/wardeploy")
//        Mono<Void> warDeploy(
//            @HostParam("$host") String host,
//            @BodyParam("application/octet-stream") byte[] warFile,
//            @QueryParam("name") String appName);

        @Headers({"Content-Type: application/octet-stream"})
        @Post("api/wardeploy")
        Mono<Void> warDeploy(
            @HostParam("$host") String host,
            @BodyParam("application/octet-stream") Flux<ByteBuffer> warFile,
            @HeaderParam("content-length") long size,
            @QueryParam("name") String appName);

//        @Headers({
//            "Content-Type: application/octet-stream",
//            "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps zipDeploy",
//            "x-ms-body-logging: false"
//        })
//        @Post("api/zipdeploy")
//        Mono<Void> zipDeploy(
//            @HostParam("$host") String host,
//            @BodyParam("application/octet-stream") byte[] zipFile);

        @Headers({"Content-Type: application/octet-stream"})
        @Post("api/zipdeploy")
        Mono<Void> zipDeploy(
            @HostParam("$host") String host,
            @BodyParam("application/octet-stream") Flux<ByteBuffer> zipFile,
            @HeaderParam("content-length") long size);

        @Headers({"Content-Type: application/octet-stream"})
        @Post("api/publish")
        Mono<Void> deploy(@HostParam("$host") String host,
            @BodyParam("application/octet-stream") Flux<ByteBuffer> file,
            @HeaderParam("content-length") long size,
            @QueryParam("type") DeployType type,
            @QueryParam("path") String path,
            @QueryParam("restart") Boolean restart,
            @QueryParam("clean") Boolean clean);

        @Get("api/settings")
        Mono<Map<String, String>> settings(@HostParam("$host") String host);
    }

    Flux<String> streamApplicationLogsAsync() {
        return streamFromFluxBytes(service.streamApplicationLogs(host).flatMapMany(StreamResponse::getValue));
    }

    Flux<String> streamHttpLogsAsync() {
        return streamFromFluxBytes(service.streamHttpLogs(host).flatMapMany(StreamResponse::getValue));
    }

    Flux<String> streamTraceLogsAsync() {
        return streamFromFluxBytes(service.streamTraceLogs(host).flatMapMany(StreamResponse::getValue));
    }

    Flux<String> streamDeploymentLogsAsync() {
        return streamFromFluxBytes(service.streamDeploymentLogs(host).flatMapMany(StreamResponse::getValue));
    }

    Flux<String> streamAllLogsAsync() {
        return streamFromFluxBytes(service.streamAllLogs(host).flatMapMany(StreamResponse::getValue));
    }

    static Flux<String> streamFromFluxBytes(final Flux<ByteBuffer> source) {
        final byte newLine = '\n';
        final byte newLineR = '\r';

        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        return source
            .concatMap(
                byteBuffer -> {
                    int index = findByte(byteBuffer, newLine);
                    if (index == -1) {
                        // no newLine byte found, not a line, put it into stream
                        try {
                            stream.write(FluxUtil.byteBufferToArray(byteBuffer));
                            return Flux.empty();
                        } catch (IOException e) {
                            return Flux.error(e);
                        }
                    } else {
                        // newLine byte found, at least 1 line
                        List<String> lines = new ArrayList<>();
                        while ((index = findByte(byteBuffer, newLine)) != -1) {
                            byte[] byteArray = new byte[index + 1];
                            byteBuffer.get(byteArray);
                            try {
                                stream.write(byteArray);
                                String line = new String(stream.toByteArray(), StandardCharsets.UTF_8);
                                if (!line.isEmpty() && line.charAt(line.length() - 1) == newLine) {
                                    // OK this is a line, end with newLine char
                                    line = line.substring(0, line.length() - 1);
                                    if (!line.isEmpty() && line.charAt(line.length() - 1) == newLineR) {
                                        line = line.substring(0, line.length() - 1);
                                    }
                                    lines.add(line);
                                    stream.reset();
                                }
                            } catch (IOException e) {
                                return Flux.error(e);
                            }
                        }
                        if (byteBuffer.hasRemaining()) {
                            // put rest into stream
                            try {
                                stream.write(FluxUtil.byteBufferToArray(byteBuffer));
                            } catch (IOException e) {
                                return Flux.error(e);
                            }
                        }
                        if (lines.isEmpty()) {
                            return Flux.empty();
                        } else {
                            return Flux.fromIterable(lines);
                        }
                    }
                });
    }

    private static int findByte(ByteBuffer byteBuffer, byte b) {
        final int position = byteBuffer.position();
        int index = -1;
        for (int i = 0; i < byteBuffer.remaining(); ++i) {
            if (byteBuffer.get(position + i) == b) {
                index = i;
                break;
            }
        }
        return index;
    }

//    Mono<Void> warDeployAsync(InputStream warFile, String appName) {
//        InputStreamFlux flux = fluxFromInputStream(warFile);
//        if (flux.flux != null) {
//            return withRetry(service.warDeploy(host, flux.flux, flux.size, appName));
//        } else {
//            return withRetry(service.warDeploy(host, flux.bytes, appName));
//        }
//    }

    Mono<Void> warDeployAsync(InputStream warFile, long length, String appName) {
        Flux<ByteBuffer> flux = FluxUtil.toFluxByteBuffer(warFile);
        return retryOnError(service.warDeploy(host, flux, length, appName));
    }

    Mono<Void> warDeployAsync(File warFile, String appName) throws IOException {
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(warFile.toPath(), StandardOpenOption.READ);
        return retryOnError(service.warDeploy(host, FluxUtil.readFile(fileChannel), fileChannel.size(), appName))
                .doFinally(ignored -> {
                    try {
                        fileChannel.close();
                    } catch (IOException e) {
                        logger.logThrowableAsError(e);
                    }
                });
    }

//    Mono<Void> zipDeployAsync(InputStream zipFile) {
//        InputStreamFlux flux = fluxFromInputStream(zipFile);
//        if (flux.flux != null) {
//            return withRetry(service.zipDeploy(host, flux.flux, flux.size));
//        } else {
//            return withRetry(service.zipDeploy(host, flux.bytes));
//        }
//    }

    Mono<Void> zipDeployAsync(InputStream zipFile, long length) {
        Flux<ByteBuffer> flux = FluxUtil.toFluxByteBuffer(zipFile);
        return retryOnError(service.zipDeploy(host, flux, length));
    }

    Mono<Void> zipDeployAsync(File zipFile) throws IOException {
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(zipFile.toPath(), StandardOpenOption.READ);
        return retryOnError(service.zipDeploy(host, FluxUtil.readFile(fileChannel), fileChannel.size()))
            .doFinally(ignored -> {
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    logger.logThrowableAsError(e);
                }
            });
    }

    Mono<Void> deployAsync(DeployType type,
                           InputStream file, long length,
                           String path, Boolean restart, Boolean clean) {
        Flux<ByteBuffer> flux = FluxUtil.toFluxByteBuffer(file);
        return retryOnError(service.deploy(host, flux, length, type, path, restart, clean));
    }

    Mono<Void> deployAsync(DeployType type,
                           File file,
                           String path, Boolean restart, Boolean clean) throws IOException {
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ);
        return retryOnError(service.deploy(host, FluxUtil.readFile(fileChannel), fileChannel.size(),
            type, path, restart, clean))
            .doFinally(ignored -> {
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    logger.logThrowableAsError(e);
                }
            });
    }

    Mono<Map<String, String>> settings() {
        return retryOnError(service.settings(host));
    }

//    private InputStreamFlux fluxFromInputStream(InputStream inputStream) {
//        try {
//            InputStreamFlux inputStreamFlux = new InputStreamFlux();
//            if (inputStream instanceof FileInputStream) {
//                inputStreamFlux.size = ((FileInputStream) inputStream).getChannel().size();
//                inputStreamFlux.flux = FluxUtil.toFluxByteBuffer(inputStream);
//            } else if (inputStream instanceof ByteArrayInputStream) {
//                inputStreamFlux.size = inputStream.available();
//                inputStreamFlux.flux = FluxUtil.toFluxByteBuffer(inputStream);
//            } else {
//                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//                int nRead;
//                byte[] data = new byte[16384];
//                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
//                    buffer.write(data, 0, nRead);
//                }
//                inputStreamFlux.bytes = buffer.toByteArray();
//                inputStreamFlux.size = inputStreamFlux.bytes.length;
//            }
//            return inputStreamFlux;
//        } catch (IOException e) {
//            throw logger.logExceptionAsError(new IllegalStateException(e));
//        }
//    }
//
//    private static class InputStreamFlux {
//        private Flux<ByteBuffer> flux;
//        private byte[] bytes;
//        private long size;
//    }

    private <T> Mono<T> retryOnError(Mono<T> observable) {
        final int retryCount = 5 + 1;   // retryCount is 5, last 1 is guard
        return observable
            .retryWhen(Retry.withThrowable(
                flux ->
                    flux
                        .zipWith(
                            Flux.range(1, retryCount),
                            (Throwable throwable, Integer count) -> {
                                if (count < retryCount
                                    && (throwable instanceof TimeoutException
                                    || throwable instanceof SocketTimeoutException)) {
                                    return count;
                                } else {
                                    throw logger.logExceptionAsError(Exceptions.propagate(throwable));
                                }
                            })
                        .flatMap(i -> Mono.delay(Duration.ofSeconds(((long) i) * 10)))));
    }
}
