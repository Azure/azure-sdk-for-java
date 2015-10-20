/**
 * 
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.microsoft.windowsazure.exception;

import com.microsoft.windowsazure.core.utils.BOMInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Service Exception indicates an error while executing a service operation.
 */
public class ServiceException extends Exception {

    private static final long serialVersionUID = -4942076377009150131L;

    private CloudError error;
    private int httpStatusCode;
    private String httpReasonPhrase;
    private String serviceName;

    private Map<String, String> errorValues;
    private String rawResponseBody;

    public ServiceException() {
        super();

        init();
    }

    public ServiceException(final String message) {
        super(message);
        init();
    }

    public ServiceException(final String message, final Throwable cause) {
        super(message, cause);
        init();
    }

    public ServiceException(final Throwable cause) {
        super(cause);
        init();
    }

    private void init() {
        errorValues = new HashMap<String, String>();
        error = new CloudError();
    }

    @Override
    public String getMessage() {
        final StringBuffer buffer = new StringBuffer(50);
        buffer.append(super.getMessage());

        if (this.rawResponseBody != null) {
            buffer.append("\nResponse Body: ");
            buffer.append(this.rawResponseBody);
        }

        return buffer.toString();
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(final int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getHttpReasonPhrase() {
        return httpReasonPhrase;
    }

    public void setHttpReasonPhrase(final String httpReasonPhrase) {
        this.httpReasonPhrase = httpReasonPhrase;
    }

    public CloudError getError() {
        return error;
    }

    public void setError(final CloudError error) {
        this.error = error;
    }

    public Map<String, String> getErrorValues() {
        return errorValues;
    }

    public void setErrorValues(final Map<String, String> errorValues) {
        this.errorValues = errorValues;
    }

    public String getErrorValue(final String name) {
        return errorValues.get(name);
    }

    public void setErrorValue(final String name, final String value) {
        this.errorValues.put(name, value);
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(final String serviceName) {
        this.serviceName = serviceName;
    }

    public void setRawResponseBody(final String body) {
        this.rawResponseBody = body;
    }

    public String getRawResponseBody() {
        return rawResponseBody;
    }

    public static ServiceException create(final HttpRequest httpRequest,
            final String requestContent, final HttpResponse httpResponse,
            final HttpEntity entity, final String defaultTo) {
        ServiceException serviceException;

        if (httpResponse.getEntity().getContentType().getValue()
                .equals("application/json")
                || httpResponse.getEntity().getContentType().getValue()
                        .equals("text/json")) {
            serviceException = createFromJson(httpRequest, requestContent,
                    httpResponse, entity);
        } else if (httpResponse.getEntity().getContentType().getValue()
                .equals("application/xml")
                || httpResponse.getEntity().getContentType().getValue()
                        .equals("text/xml")) {
            serviceException = createFromXml(httpRequest, requestContent,
                    httpResponse, entity);
        } else if ("Json".equals(defaultTo)) {
            serviceException = createFromJson(httpRequest, requestContent,
                    httpResponse, entity);
        } else {
            serviceException = createFromXml(httpRequest, requestContent,
                    httpResponse, entity);
        }

        return serviceException;
    }

    public static ServiceException createFromXml(final HttpRequest httpRequest,
            final String requestContent, final HttpResponse httpResponse,
            final HttpEntity entity) {
        String content;
        try {
            content = EntityUtils.toString(entity);
        } catch (IOException e) {
            return new ServiceException(e);
        }

        ServiceException serviceException;

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory
                    .newDocumentBuilder();
            Document responseDoc = documentBuilder.parse(new BOMInputStream(new ByteArrayInputStream(content.getBytes())));
            
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            String code = xpath.compile("/Error/Code/text()").evaluate(
                    responseDoc);
            String message = xpath.compile("/Error/Message/text()").evaluate(
                    responseDoc);

            serviceException = new ServiceException(buildExceptionMessage(code,
                    message, content, httpResponse));

            serviceException.getError().setCode(code);
            serviceException.getError().setMessage(message);
        } catch (XPathExpressionException e) {
            return new ServiceException(content);
        } catch (ParserConfigurationException e) {
            return new ServiceException(content);
        } catch (SAXException e) {
            return new ServiceException(content);
        } catch (IOException e) {
            return new ServiceException(content);
        }

        serviceException.setHttpStatusCode(httpResponse.getStatusLine()
                .getStatusCode());
        serviceException.setHttpReasonPhrase(httpResponse.getStatusLine()
                .getReasonPhrase());

        return serviceException;
    }

    private static String buildExceptionMessage(String code, String message,
            String responseContent, HttpResponse httpResponse) {
        return (code != null && message != null) ? code + ": " + message
                : (message != null) ? message
                        : (code != null) ? code
                                : (responseContent != null) ? responseContent
                                        : (httpResponse != null
                                                && httpResponse.getStatusLine() != null && httpResponse
                                                .getStatusLine()
                                                .getReasonPhrase() != null) ? httpResponse
                                                .getStatusLine()
                                                .getReasonPhrase()
                                                : (httpResponse != null && httpResponse
                                                        .getStatusLine() != null) ? Integer
                                                        .toString(httpResponse
                                                                .getStatusLine()
                                                                .getStatusCode())
                                                        : "Invalid operation";
    }

    public static ServiceException createFromJson(HttpRequest httpRequest,
            String requestContent, HttpResponse httpResponse, HttpEntity entity) {
        String content;
        try {
            content = EntityUtils.toString(entity);
        } catch (IOException e) {
            return new ServiceException(e);
        }

        ServiceException serviceException;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode responseDoc = objectMapper.readTree(content);

            JsonNode errorNode = responseDoc.get("Error");
            if (errorNode == null) {
                errorNode = responseDoc.get("error");
            }
            if (errorNode != null) {
                responseDoc = errorNode;
            }
           
            String code;
            if (responseDoc.get("Code") != null) {
                code = responseDoc.get("Code").getTextValue();
            } else if (responseDoc.get("code") != null) {
                code = responseDoc.get("code").getTextValue();
            } else {
                code = Integer.toString(httpResponse.getStatusLine().getStatusCode());
            }
            
            String message;
            if (responseDoc.get("Message") != null) {
                message = responseDoc.get("Message").getTextValue();
            } else if (responseDoc.get("message") != null) {
                message = responseDoc.get("message").getTextValue();
            } else {
                message = httpResponse.getStatusLine().getReasonPhrase();
            }

            serviceException = new ServiceException(buildExceptionMessage(code,
                    message, content, httpResponse));
            serviceException.getError().setCode(code);
            serviceException.getError().setMessage(message);
        } catch (IOException e) {
            return new ServiceException();
        }

        serviceException.setHttpStatusCode(httpResponse.getStatusLine()
                .getStatusCode());
        serviceException.setHttpReasonPhrase(httpResponse.getStatusLine()
                .getReasonPhrase());

        return serviceException;
    }
}
