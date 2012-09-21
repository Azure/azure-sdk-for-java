/**
 * 
 */
package com.microsoft.windowsazure.services.media.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBException;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider;

/**
 * Jersey provider to unmarshal lists of entities from Media Services.
 * 
 */
public class ODataEntityCollectionProvider extends AbstractMessageReaderWriterProvider<List<ODataEntity<?>>> {
    private final ODataAtomUnmarshaller unmarshaller;

    public ODataEntityCollectionProvider() throws JAXBException {
        unmarshaller = new ODataAtomUnmarshaller();
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return ODataEntity.isODataEntityCollectionType(type, genericType);
    }

    @Override
    public List<ODataEntity<?>> readFrom(Class<List<ODataEntity<?>>> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {

        String responseType = mediaType.getParameters().get("type");
        try {
            if (responseType == null || responseType.equals("feed")) {
                return unmarshaller.unmarshalFeed(entityStream,
                        (Class<ODataEntity<?>>) ODataEntity.getCollectedType(genericType));
            }
            else {
                throw new RuntimeException();
            }
        }
        catch (JAXBException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        catch (ServiceException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Can this type be written by this provider?
     * 
     * @return false - we don't support writing
     */
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return false;
    }

    /**
     * Write the given object to the stream.
     * This method implementation throws, we don't support writing.
     * 
     * @throws UnsupportedOperationException
     */
    @Override
    public void writeTo(List<ODataEntity<?>> t, Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {

        throw new UnsupportedOperationException();
    }

}
