package com.microsoft.azure.services.serviceBus.contract;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;

import com.sun.jersey.core.impl.provider.entity.StringProvider;
import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;

@Produces("application/atom+xml")
@Consumes("application/atom+xml")
public class EntryModelProvider extends AbstractMessageReaderWriterProvider<EntryModel<?>>  {
	private Providers ps;

	public EntryModelProvider(@Context Providers ps){
		this.ps = ps;
	}

	public boolean isReadable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		// TODO Auto-generated method stub
		
		return type == EntryModel.class;
	}

	public EntryModel<?> readFrom(Class<EntryModel<?>> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {
		
		MessageBodyReader<Entry> reader = ps.getMessageBodyReader(Entry.class, Entry.class, annotations, mediaType);
		Entry entry = reader.readFrom(Entry.class, Entry.class, annotations, mediaType, httpHeaders, entityStream);
		
		if (entry.getContents().size() == 1) {
			Content content = (Content)entry.getContents().get(0);
			MediaType entryType = MediaType.valueOf(content.getType());
			MessageBodyReader<QueueDescription> reader2 = ps.getMessageBodyReader(QueueDescription.class, QueueDescription.class, annotations, entryType);
			String data = content.getValue();
			QueueDescription model = reader2.readFrom(QueueDescription.class, QueueDescription.class, annotations, entryType, httpHeaders, new ByteArrayInputStream(data.getBytes()));
			return new EntryModel<QueueDescription>(entry, model);
		}
		return new EntryModel<QueueDescription>(entry, null);
	}

	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		// TODO Auto-generated method stub
		return false;
	}

	public void writeTo(EntryModel<?> t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
		// TODO Auto-generated method stub
		
	}
}
