package com.microsoft.windowsazure.services.core;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

public interface ServiceFilter {
    Response handle(Request request, Next next) throws Exception;

    public interface Next {
        Response handle(Request request) throws Exception;
    }

    public interface Request {
        public String getMethod();

        public void setMethod(String method);

        public URI getURI();

        public void setURI(URI uri);

        public MultivaluedMap<String, Object> getHeaders();

        public Object getEntity();

        public void setEntity(Object entity);

        public Map<String, Object> getProperties();

        public void setProperties(Map<String, Object> properties);
    }

    public interface Response {
        int getStatus();

        void setStatus(int status);

        MultivaluedMap<String, String> getHeaders();

        boolean hasEntity();

        InputStream getEntityInputStream();

        void setEntityInputStream(InputStream entity);

        Map<String, Object> getProperties();
    }
}
