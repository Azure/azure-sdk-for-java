/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.exception;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ServiceException extends Exception {

    private static final long serialVersionUID = -4942076377009150131L;

    int httpStatusCode;
    String httpReasonPhrase;
    String serviceName;

    String errorCode;
    String errorMessage;
    Map<String, String> errorValues;
    String rawResponseBody;

    public ServiceException() {
        init();
    }

    public ServiceException(String message) {
        super(message);
        init();
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        init();
    }

    public ServiceException(Throwable cause) {
        super(cause);
        init();
    }

    private void init() {
        errorValues = new HashMap<String, String>();
    }

    @Override
    public String getMessage() {
        if (this.rawResponseBody == null)
            return super.getMessage();
        else
            return super.getMessage() + "\nResponse Body: " + this.rawResponseBody;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getHttpReasonPhrase() {
        return httpReasonPhrase;
    }

    public void setHttpReasonPhrase(String httpReasonPhrase) {
        this.httpReasonPhrase = httpReasonPhrase;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Map<String, String> getErrorValues() {
        return errorValues;
    }

    public void setErrorValues(Map<String, String> errorValues) {
        this.errorValues = errorValues;
    }

    public String getErrorValue(String name) {
        return errorValues.get(name);
    }

    public void setErrorValue(String name, String value) {
        this.errorValues.put(name, value);
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setRawResponseBody(String body) {
        this.rawResponseBody = body;
    }

    public String getRawResponseBody() {
        return rawResponseBody;
    }
    
    public static ServiceException create(HttpRequest httpRequest, String requestContent, HttpResponse httpResponse, HttpEntity entity, String defaultTo)
    {
        if (httpResponse.getEntity().getContentType().getValue().equals("application/json") ||
            httpResponse.getEntity().getContentType().getValue().equals("text/json"))
        {
            return createFromJson(httpRequest, requestContent, httpResponse, entity);
        } else if (httpResponse.getEntity().getContentType().getValue().equals("application/xml") ||
                   httpResponse.getEntity().getContentType().getValue().equals("text/xml"))
        {
            return createFromXml(httpRequest, requestContent, httpResponse, entity);
        } else if (defaultTo.equals("Json"))
        {
            return createFromJson(httpRequest, requestContent, httpResponse, entity);
        }

        return createFromXml(httpRequest, requestContent, httpResponse, entity);
    }
    
    public static ServiceException createFromXml(HttpRequest httpRequest, String requestContent, HttpResponse httpResponse, HttpEntity entity)
    {
        String content;
        try
        {
            content = EntityUtils.toString(entity);
        }
        catch (IOException e)
        {
            return new ServiceException(e);
        }
        
        ServiceException serviceException;

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document responseDoc = documentBuilder.parse(new InputSource(new ByteArrayInputStream(content.getBytes("utf-8"))));
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            
            String code = xpath.compile("/Error/Code/text()").evaluate(responseDoc);
            String message = xpath.compile("/Error/Message/text()").evaluate(responseDoc);

            serviceException = new ServiceException(buildExceptionMessage(code, message, content, httpResponse));
            serviceException.setErrorCode(code);
            serviceException.setErrorMessage(message);
        }
        catch (XPathExpressionException e)
        {
            return new ServiceException(content);
        }
        catch (ParserConfigurationException e)
        {
            return new ServiceException(content);
        }
        catch (SAXException e)
        {
            return new ServiceException(content);
        }
        catch (IOException e)
        {
            return new ServiceException(content);
        }
        
        serviceException.setHttpStatusCode(httpResponse.getStatusLine().getStatusCode());
        serviceException.setHttpReasonPhrase(httpResponse.getStatusLine().getReasonPhrase());
        
        return serviceException;
    }
    
    private static String buildExceptionMessage(String code, String message, String responseContent, HttpResponse httpResponse)
    {
        return (code != null && message != null) ? code + ": " + message :
               (message != null) ? message :
               (code != null) ? code :
               (responseContent != null) ? responseContent :
               (httpResponse != null && httpResponse.getStatusLine() != null && httpResponse.getStatusLine().getReasonPhrase() != null) ?
                httpResponse.getStatusLine().getReasonPhrase() :
                (httpResponse != null && httpResponse.getStatusLine() != null) ?
                Integer.toString(httpResponse.getStatusLine().getStatusCode()) :
               "Invalid operation";
    }
    
    public static ServiceException createFromJson(HttpRequest httpRequest, String requestContent, HttpResponse httpResponse, HttpEntity entity)
    {
        String content;
        try
        {
            content = EntityUtils.toString(entity);
        }
        catch (IOException e)
        {
            return new ServiceException(e);
        }
        
        ServiceException serviceException;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode responseDoc = objectMapper.readTree(content);
            
            String code = responseDoc.get("Code").getTextValue();
            String message = responseDoc.get("Message").getTextValue();

            serviceException = new ServiceException(buildExceptionMessage(code, message, content, httpResponse));
            serviceException.setErrorCode(code);
            serviceException.setErrorMessage(message);
        }
        catch (IOException e) {
            return new ServiceException();
        }
        
        serviceException.setHttpStatusCode(httpResponse.getStatusLine().getStatusCode());
        serviceException.setHttpReasonPhrase(httpResponse.getStatusLine().getReasonPhrase());
        
        return serviceException;
    }
}
