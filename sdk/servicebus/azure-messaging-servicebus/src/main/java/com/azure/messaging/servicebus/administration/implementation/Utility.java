package com.azure.messaging.servicebus.administration.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.administration.implementation.models.CreateQueueBody;
import com.azure.messaging.servicebus.administration.implementation.models.CreateQueueBodyContent;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescription;
import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import com.azure.messaging.servicebus.administration.models.QueueProperties;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static com.azure.core.http.policy.AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.AZ_TRACING_NAMESPACE_VALUE;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SERVICE_BUS_DLQ_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME;

public class Utility {
    public static final String CONTENT_TYPE = "application/xml";
    public static final ClientLogger LOGGER = new ClientLogger(Utility.class);

    /**
     * Check that the additional headers field is present and add the additional auth header
     *
     * @param headerName name of the header to be added
     * @param context current request context
     */
    public static void addSupplementaryAuthHeader(String headerName, String entity, Context context) {
        context.getData(AZURE_REQUEST_HTTP_HEADERS_KEY)
            .ifPresent(headers -> {
                if (headers instanceof HttpHeaders) {
                    HttpHeaders customHttpHeaders = (HttpHeaders) headers;
                    customHttpHeaders.add(headerName, entity);
                }
            });
    }

    /**
     * Given an XML title element, returns the XML text inside. Jackson deserializes Objects as LinkedHashMaps. XML text
     * is represented as an entry with an empty string as the key.
     * <p>
     * For example, the text returned from this {@code <title text="text/xml">QueueName</title>} is "QueueName".
     *
     * @param responseTitle XML title element.
     * @return The XML text inside the title. {@code null} is returned if there is no value.
     */
    @SuppressWarnings("unchecked")
    public static String getTitleValue(Object responseTitle) {
        if (!(responseTitle instanceof Map)) {
            return null;
        }

        final Map<String, String> map;
        try {
            map = (Map<String, String>) responseTitle;
            return map.get("");
        } catch (ClassCastException error) {
            LOGGER.warning("Unable to cast to Map<String,String>. Title: {}", responseTitle, error);
            return null;
        }
    }

}
