package io.clientcore.annotation.processor.test.implementation;

import io.clientcore.annotation.processor.test.implementation.models.SimpleXmlSerializable;
import io.clientcore.core.annotations.ServiceInterface;
import io.clientcore.core.http.annotations.BodyParam;
import io.clientcore.core.http.annotations.HeaderParam;
import io.clientcore.core.http.annotations.HttpRequestInformation;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.pipeline.HttpPipeline;

import java.lang.reflect.InvocationTargetException;

/**
 * Service interface for handling XML serialization and deserialization.
 */
@ServiceInterface(name = "XmlSerializable", host = "http://localhost")
public interface SimpleXmlSerializableService {

    /**
     * Creates a new instance of the SimpleXmlSerializableService.
     *
     * @param pipeline The HTTP pipeline to be used by the service.
     * @return A new instance of SimpleXmlSerializableService.
     * @throws IllegalArgumentException if the pipeline is null.
     * @throws RuntimeException if there is an error creating the service instance.
     */
    static SimpleXmlSerializableService getNewInstance(HttpPipeline pipeline) {
        if (pipeline == null) {
            throw new IllegalArgumentException("pipeline cannot be null");
        }
        try {
            Class<?> clazz = Class.forName("io.clientcore.annotation.processor.test.SimpleXmlSerializableServiceImpl");
            return (SimpleXmlSerializableService) clazz
                    .getMethod("getNewInstance", HttpPipeline.class)
                    .invoke(null, pipeline);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                 | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends an XML payload with the content type "application/xml".
     *
     * @param simpleXmlSerializable The XML payload to be sent.
     */
    @HttpRequestInformation(method = HttpMethod.PUT, path = "sendApplicationXml", expectedStatusCodes = { 200 })
    void sendApplicationXml(@BodyParam("application/xml") SimpleXmlSerializable simpleXmlSerializable);

    /**
     * Sends an XML payload with the content type "text/xml".
     *
     * @param simpleXmlSerializable The XML payload to be sent.
     */
    @HttpRequestInformation(method = HttpMethod.PUT, path = "sendTextXml", expectedStatusCodes = { 200 })
    void sendTextXml(@BodyParam("text/xml") SimpleXmlSerializable simpleXmlSerializable);

    /**
     * Retrieves an XML payload.
     *
     * @param contentType The content type of the XML payload.
     * @return The retrieved XML payload.
     */
    @HttpRequestInformation(method = HttpMethod.GET, path = "getXml", expectedStatusCodes = { 200 })
    SimpleXmlSerializable getXml(@HeaderParam("Content-Type") String contentType);

    /**
     * Retrieves an invalid XML payload.
     * @param contentType The content type of the invalid XML payload.
     * @return The retrieved invalid XML payload.
     */
    @HttpRequestInformation(method = HttpMethod.GET, path = "getInvalidXml", expectedStatusCodes = { 200 })
    SimpleXmlSerializable getInvalidXml(@HeaderParam("Content-Type") String contentType);
}
