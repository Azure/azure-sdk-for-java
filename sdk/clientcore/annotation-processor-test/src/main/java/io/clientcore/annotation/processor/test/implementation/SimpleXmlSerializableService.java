package io.clientcore.annotation.processor.test.implementation;

import io.clientcore.annotation.processor.test.implementation.models.SimpleXmlSerializable;
import io.clientcore.core.annotations.ServiceInterface;
import io.clientcore.core.http.annotations.BodyParam;
import io.clientcore.core.http.annotations.HeaderParam;
import io.clientcore.core.http.annotations.HttpRequestInformation;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.pipeline.HttpPipeline;

import java.lang.reflect.InvocationTargetException;

@ServiceInterface(name = "XmlSerializable", host = "http://localhost")
public interface SimpleXmlSerializableService {

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

    @HttpRequestInformation(method = HttpMethod.PUT, path = "sendApplicationXml", expectedStatusCodes = { 200 })
    void sendApplicationXml(@BodyParam("application/xml") SimpleXmlSerializable simpleXmlSerializable);

    @HttpRequestInformation(method = HttpMethod.PUT, path = "sendTextXml", expectedStatusCodes = { 200 })
    void sendTextXml(@BodyParam("text/xml") SimpleXmlSerializable simpleXmlSerializable);

    @HttpRequestInformation(method = HttpMethod.GET, path = "getXml", expectedStatusCodes = { 200 })
    SimpleXmlSerializable getXml(@HeaderParam("Content-Type") String contentType);

    @HttpRequestInformation(method = HttpMethod.GET, path = "getInvalidXml", expectedStatusCodes = { 200 })
    SimpleXmlSerializable getInvalidXml();
}
