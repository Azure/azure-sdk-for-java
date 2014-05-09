/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.servicebus.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider;
import com.sun.jersey.spi.MessageBodyWorkers;

public class EntryModelProvider extends
        AbstractMessageReaderWriterProvider<EntryModel<?>> {

    private MessageBodyWorkers workers;

    public EntryModelProvider(@Context MessageBodyWorkers workers) {
        this.workers = workers;
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return EntryModel.class.isAssignableFrom(type);
    }

    @Override
    public EntryModel<?> readFrom(Class<EntryModel<?>> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {

        MessageBodyReader<Entry> reader = workers.getMessageBodyReader(
                Entry.class, Entry.class, annotations, mediaType);

        Entry entry = reader.readFrom(Entry.class, Entry.class, annotations,
                mediaType, httpHeaders, entityStream);

        // these exceptions are masked as a RuntimeException because they cannot
        // be thrown by this override
        try {
            return type.getConstructor(Entry.class).newInstance(entry);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return EntryModel.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(EntryModel<?> t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {

        Entry entry = t.getEntry();

        MessageBodyWriter<Entry> writer = workers.getMessageBodyWriter(
                Entry.class, Entry.class, annotations, mediaType);

        writer.writeTo(entry, Entry.class, genericType, annotations, mediaType,
                httpHeaders, entityStream);
    }

}
