package com.microsoft.windowsazure.services.media.implementation;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.mail.MessagingException;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

@Produces("multipart/mixed")
public class BatchMimeMultipartBodyWritter implements MessageBodyWriter<BatchMimeMultipart> {

	@Override
	public long getSize(BatchMimeMultipart bmm, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4) {
		return -1;
	}

	@Override
	public boolean isWriteable(Class<?> type, Type arg1, Annotation[] arg2, MediaType arg3) {
		return type == BatchMimeMultipart.class;
	}

	@Override
	public void writeTo(BatchMimeMultipart t, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4,
			MultivaluedMap<String, Object> arg5, OutputStream entityStream) throws IOException, WebApplicationException {
		try {
            t.writeTo(entityStream);
        } catch (MessagingException ex) {
            throw new WebApplicationException(ex);
        }	
	}

}
