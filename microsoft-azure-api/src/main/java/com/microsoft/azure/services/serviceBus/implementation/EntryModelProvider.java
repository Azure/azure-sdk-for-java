package com.microsoft.azure.services.serviceBus.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import com.microsoft.azure.services.serviceBus.Queue;
import com.microsoft.azure.services.serviceBus.schema.Entry;
import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider;
import com.sun.jersey.spi.MessageBodyWorkers;

public class EntryModelProvider extends
		AbstractMessageReaderWriterProvider<EntryModel<?>> {

	MessageBodyWorkers workers;

	public EntryModelProvider(@Context MessageBodyWorkers workers) {
		this.workers = workers;
	}

	public boolean isReadable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return EntryModel.class.isAssignableFrom(type);
	}

	public EntryModel<?> readFrom(Class<EntryModel<?>> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {

		MessageBodyReader<Entry> reader = workers.getMessageBodyReader(
				Entry.class, Entry.class, annotations, mediaType);

		Entry entry = reader.readFrom(Entry.class, Entry.class, annotations,
				mediaType, httpHeaders, entityStream);

		return new Queue(entry);
	}

	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return EntryModel.class.isAssignableFrom(type);
	}

	public void writeTo(EntryModel<?> t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
		
		Entry entry = t.getEntry();
		
		MessageBodyWriter<Entry> writer = workers.getMessageBodyWriter(
				Entry.class, Entry.class, annotations, mediaType);

		writer.writeTo(entry, Entry.class, genericType, annotations, mediaType, httpHeaders, entityStream);
	}

}
