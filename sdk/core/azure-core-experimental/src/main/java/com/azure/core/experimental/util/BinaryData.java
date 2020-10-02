package com.azure.core.experimental.util;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * This is binary representation of data. This provides convenience API to
 *
 * <p><strong>Create an instance of sender</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.servicebussenderclient.instantiation}
 */
final public class BinaryData {
    private byte[] data;
    private static final ClientLogger LOGGER = new ClientLogger(BinaryData.class);

    BinaryData() {
        // This exists, so no one is able to create instance, user need to use static function to create instances.
    }

    BinaryData(byte[] data) {
        this.data = data;
    }

    /**
     *
     * @return
     */
    public InputStream toStream() {
        InputStream outStream = new ByteArrayInputStream(this.data);
        try {
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outStream;
    }

    /**
     *
     * @return
     */
    public Mono<InputStream> toStreamAsync() {
        return Mono.fromCallable(() -> toStream()) ;
    }

    /**
     *
     * @param inputStream
     * @return
     */
    public static BinaryData fromStream(InputStream inputStream) {
        final int bufferSize = 1024;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[bufferSize];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            return fromBytes(buffer.toByteArray());
        } catch (IOException ex) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    /**
     * Create {@link BinaryData} instance with given value.
     *
     * @param inputStream to use.
     */
    public static Mono<BinaryData> fromStreamAsync(InputStream inputStream) {
        return Mono.fromCallable(() -> fromStream(inputStream)) ;
       // return fromStreamAsync(inputStream, 4096);
    }

    /**
     * Create {@link BinaryData} instance with given value.
     *
     * @param data to use.
     */
    public static Mono<BinaryData> fromFlux(Flux<ByteBuffer> data) {
        return FluxUtil.collectBytesInByteBufferStream(data)
            .flatMap(bytes -> Mono.just(fromBytes(bytes)));
    }

    /**
     * Create {@link BinaryData} instance with given value.
     *
     * @param data to use.
     */
    public static BinaryData fromString(String data, Charset charSet) {
        return new BinaryData(data.getBytes(charSet));
    }
    /**
     * Create {@link BinaryData} instance with given value.
     *
     * @param data to use.
     */
    public static BinaryData fromString(String data) {
        return new BinaryData(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Create {@link BinaryData} instance with given byte array data.
     *
     * @param data to use.
     */
    public static BinaryData fromBytes(byte[] data) {
        return new BinaryData(data);
    }

    /**
     * Create {@link BinaryData} instance with given object and {@link JsonSerializer}.
     *
     * @param data to use.
     */
    public static BinaryData fromObject(Object data, ObjectSerializer serializer) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializer.serialize(outputStream, data);
        return new BinaryData(outputStream.toByteArray());

    }

    /**
     * Asynchronously, create {@link BinaryData} instance with given object and {@link JsonSerializer}.
     *
     * @param data to use.
     */
    public static Mono<BinaryData> fromObjectAsync(Object data, ObjectSerializer serializer) {
        return Mono.fromCallable(() -> fromObject(data, serializer)) ;

    }

    /**
     * Gets the binary data.
     *
     * @return byte array representing {@link BinaryData}.
     */
    public byte[] toBytes() {
        return Arrays.copyOf(this.data, this.data.length);
    }

    /**
     * Gets the binary data.
     *
     * @return byte array representing {@link BinaryData}.
     */
    public String toString() {
        return new String(this.data, StandardCharsets.UTF_8);
    }

    /**
     * Gets the binary data.
     *
     * @return byte array representing {@link BinaryData}.
     */
    public String toString(Charset charSet) {
        return new String(this.data, charSet);
    }

    /**
     * Apply the {@link ObjectSerializer} on the bytes representation of the data.
     *
     * @param clazz representing the type of the Object.
     * @param serializer to use deserialize data into type.
     * @return The type
     */
    public <T> T toObject(Class<T> clazz, ObjectSerializer serializer) {
        TypeReference<T>  ref = TypeReference.createInstance(clazz);
        InputStream jsonStream = new ByteArrayInputStream(this.data);
        return serializer.deserialize(jsonStream, ref);
    }

    /**
     * Asynchronously, apply the {@link ObjectSerializer} on the bytes representation of the data.
     *
     * @param clazz representing the type of the Object.
     * @param serializer to use deserialize data into type.
     * @return The type
     */
    public  <T> Mono<T> toObjectAsync(Class<T> clazz, ObjectSerializer serializer) {
        return Mono.fromCallable(() -> toObject(clazz, serializer)) ;
    }
}
