// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Headers;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.exception.AzureException;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.http.rest.StreamResponse;
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.appservice.models.DeployType;
import com.azure.resourcemanager.appservice.models.KuduDeploymentResult;
import com.azure.resourcemanager.appservice.models.WebAppBase;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/** A client which interacts with Kudu service. */
class KuduClient {

    private final ClientLogger logger = new ClientLogger(getClass());

    private final String host;
    private final KuduService service;

    private static final String DEPLOYER_JAVA_SDK = "JavaSDK";

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

        service = RestProxy.create(KuduService.class, webAppBase.manager().httpPipeline(),
            SerializerFactory.createDefaultManagementSerializerAdapter());
    }

    public static class DeploymentStatus implements JsonSerializable<DeploymentStatus> {
        private String id;
        private Integer status;
        private Boolean isTemp;

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            // no serialization
            jsonWriter.writeStartObject();
            return jsonWriter.writeEndObject();
        }

        public static DeploymentStatus fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                DeploymentStatus deserializedDeploymentStatus
                    = new DeploymentStatus();
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("id".equals(fieldName)) {
                        deserializedDeploymentStatus.id = reader.getString();
                    } else if ("status".equals(fieldName)) {
                        deserializedDeploymentStatus.status = reader.getNullable(JsonReader::getInt);
                    } else if ("is_temp".equals(fieldName)) {
                        deserializedDeploymentStatus.isTemp = reader.getNullable(JsonReader::getBoolean);
                    } else {
                        reader.skipChildren();
                    }
                }

                return deserializedDeploymentStatus;
            });
        }

        public String getId() {
            return id;
        }

        public Integer getStatus() {
            return status;
        }

        public Boolean getTemp() {
            return isTemp;
        }
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

        @Headers({"Content-Type: application/octet-stream"})
        @Post("api/wardeploy")
        Mono<Void> warDeploy(
            @HostParam("$host") String host,
            @BodyParam("application/octet-stream") Flux<ByteBuffer> warFile,
            @HeaderParam("content-length") long size,
            @QueryParam("name") String appName);

        @Headers({"Content-Type: application/octet-stream"})
        @Post("api/zipdeploy")
        Mono<Response<Void>> zipDeploy(
            @HostParam("$host") String host,
            @BodyParam("application/octet-stream") Flux<ByteBuffer> zipFile,
            @HeaderParam("content-length") long size,
            @QueryParam("isAsync") Boolean isAsync,
            @QueryParam("deployer") String deployer
        );

        // OneDeploy
        @Headers({"Content-Type: application/octet-stream"})
        @Post("api/publish")
        Mono<Response<Void>> deploy(
            @HostParam("$host") String host,
            @BodyParam("application/octet-stream") Flux<ByteBuffer> file,
            @HeaderParam("content-length") long size,
            @QueryParam("type") DeployType type,
            @QueryParam("path") String path,
            @QueryParam("restart") Boolean restart,
            @QueryParam("clean") Boolean clean,
            @QueryParam("isAsync") Boolean isAsync,
            @QueryParam("trackDeploymentProgress") Boolean trackDeploymentProgress
        );

        // OneDeploy for FunctionApp of Flex Consumption plan
        @Headers({"Content-Type: application/zip"})
        @Post("api/publish")
        Mono<Response<Void>> deployFlexConsumption(
            @HostParam("$host") String host,
            @BodyParam("application/zip") Flux<ByteBuffer> file,
            @HeaderParam("content-length") long size,
            @QueryParam("remoteBuild") Boolean remoteBuild,
            @QueryParam("deployer") String deployer
        );

        @Get("api/settings")
        Mono<Map<String, String>> settings(@HostParam("$host") String host);

        @Headers({"Accept: application/json"})
        @Get("api/deployments/{deploymentId}")
        Mono<Response<DeploymentStatus>> deploymentStatus(
            @HostParam("$host") String host,
            @PathParam("deploymentId") String deploymentId
        );
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

    Mono<Void> zipDeployAsync(InputStream zipFile, long length) {
        Flux<ByteBuffer> flux = FluxUtil.toFluxByteBuffer(zipFile);
        return retryOnError(service.zipDeploy(host, flux, length, false, null)).then();
    }

    Mono<Void> zipDeployAsync(File zipFile) throws IOException {
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(zipFile.toPath(), StandardOpenOption.READ);
        return retryOnError(service.zipDeploy(host, FluxUtil.readFile(fileChannel), fileChannel.size(), false, null))
            .doFinally(ignored -> {
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    logger.logThrowableAsError(e);
                }
            }).then();
    }

    Mono<Void> deployAsync(DeployType type,
                           InputStream file, long length,
                           String path, Boolean restart, Boolean clean) {
        Flux<ByteBuffer> flux = FluxUtil.toFluxByteBuffer(file);
        return retryOnError(service.deploy(host, flux, length, type, path, restart, clean, false, false))
            .then();
    }

    Mono<Void> deployAsync(DeployType type,
                           File file,
                           String path, Boolean restart, Boolean clean) throws IOException {
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ);
        return retryOnError(service.deploy(host, FluxUtil.readFile(fileChannel), fileChannel.size(),
            type, path, restart, clean, false, false))
            .then()
            .doFinally(ignored -> {
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    logger.logThrowableAsError(e);
                }
            });
    }

    Mono<KuduDeploymentResult> pushDeployAsync(DeployType type, File file, String path, Boolean restart,
                                               Boolean clean, Boolean trackDeployment) throws IOException {
        final boolean trackDeploymentProgress = trackDeployment == null || trackDeployment;

        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ);
        return getDeploymentResult(retryOnError(service.deploy(host, FluxUtil.readFile(fileChannel), fileChannel.size(),
            type, path, restart, clean, true, trackDeployment)), trackDeploymentProgress)
            .doFinally(ignored -> {
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    logger.logThrowableAsError(e);
                }
            });
    }

    private Mono<KuduDeploymentResult> getDeploymentResult(Mono<Response<Void>> responseMono, boolean trackDeploymentProgress) {
        return responseMono.map(response -> {
            HttpHeader deploymentIdHeader = response.getHeaders().get("SCM-DEPLOYMENT-ID");
            if (trackDeploymentProgress && (deploymentIdHeader == null || deploymentIdHeader.getValue() == null
                || deploymentIdHeader.getValue().isEmpty())) {

                // error, deployment ID not available
                throw logger.logExceptionAsError(
                    new AzureException("Deployment ID not found in response header 'SCM-DEPLOYMENT-ID'"));
            }
            return new KuduDeploymentResult(deploymentIdHeader == null ? null : deploymentIdHeader.getValue());
        });
    }

    Mono<KuduDeploymentResult> pushZipDeployAsync(File file) throws IOException {
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ);
        return getDeploymentResult(retryOnError(service.zipDeploy(host, FluxUtil.readFile(fileChannel), fileChannel.size(),
            true, DEPLOYER_JAVA_SDK)), true)
            .doFinally(ignored -> {
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    logger.logThrowableAsError(e);
                }
            });
    }

    Mono<KuduDeploymentResult> pushZipDeployAsync(InputStream file, long length) throws IOException {
        Flux<ByteBuffer> flux = FluxUtil.toFluxByteBuffer(file);
        return getDeploymentResult(retryOnError(service.zipDeploy(host, flux, length,
            true, DEPLOYER_JAVA_SDK)), true);
    }

    Mono<KuduDeploymentResult> pushDeployFlexConsumptionAsync(File file) throws IOException {
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ);
        return retryOnError(service.deployFlexConsumption(host, FluxUtil.readFile(fileChannel), fileChannel.size(),
            false, DEPLOYER_JAVA_SDK))
            // there is no "SCM-DEPLOYMENT-ID" header in response
            .then(Mono.just(new KuduDeploymentResult("latest")))
            .doFinally(ignored -> {
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    logger.logThrowableAsError(e);
                }
            });
    }

    Mono<KuduDeploymentResult> pushDeployFlexConsumptionAsync(InputStream file, long length) throws IOException {
        Flux<ByteBuffer> flux = FluxUtil.toFluxByteBuffer(file);
        return retryOnError(service.deployFlexConsumption(host, flux, length,
            false, DEPLOYER_JAVA_SDK))
            .then(Mono.just(new KuduDeploymentResult("latest")));
    }

    Mono<Map<String, String>> settings() {
        return retryOnError(service.settings(host));
    }

    private static final Duration MAX_DEPLOY_TIMEOUT = Duration.ofMinutes(10);

    Mono<Void> pollDeploymentStatus(KuduDeploymentResult result, Duration pollInterval) {
        AtomicLong pollCount = new AtomicLong();
        AtomicReference<String> deploymentId = new AtomicReference<>(result.deploymentId());
        return Mono.defer(() -> service.deploymentStatus(host, deploymentId.get()))
            .flatMap(response -> {
                DeploymentStatus deploymentStatus = response.getValue();
                Integer status = deploymentStatus.getStatus();

                // https://github.com/Azure/azure-cli/blob/dev/src/azure-cli/azure/cli/command_modules/appservice/custom.py
                boolean succeeded = status == 4;
                boolean completed = succeeded
                    || status == -1
                    || (status >= 3 && status <= 6);

                // use deploymentId from response, as the initial deploymentId could be "latest"
                // but do not use temp id
                String id = deploymentStatus.getId();
                if (!CoreUtils.isNullOrEmpty(id) && deploymentStatus.getTemp() == Boolean.FALSE) {
                    deploymentId.set(id);
                }

                if (succeeded) {
                    return Mono.just(deploymentStatus);
                } else if (completed) {
                    return Mono.error(new RuntimeException("Deployment failed, status " + status));
                } else {
                    if (pollInterval.multipliedBy(pollCount.get()).compareTo(MAX_DEPLOY_TIMEOUT) >= 0) {
                        // timeout
                        return Mono.error(new RuntimeException("Deployment timed out, status " + status));
                    } else {
                        // continue polling
                        return Mono.empty();
                    }
                }
            })
            .repeatWhenEmpty(longFlux -> longFlux.flatMap(index -> {
                pollCount.set(index);
                return Mono.delay(ResourceManagerUtils.InternalRuntimeContext.getDelayDuration(pollInterval));
            }))
            .then();
    }

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
