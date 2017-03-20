/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.utils;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.implementation.PageImpl;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Url;
import rx.Observable;
import rx.exceptions.Exceptions;
import rx.functions.Func1;

import java.io.IOException;
import java.util.List;

/**
 * Defines a few utilities.
 */
public final class Utils {
    /**
     * Converts an object Boolean to a primitive boolean.
     *
     * @param value the <tt>Boolean</tt> value
     * @return <tt>false</tt> if the given Boolean value is null or false else <tt>true</tt>
     */
    public static boolean toPrimitiveBoolean(Boolean value) {
        if (value == null) {
            return false;
        }
        return value;
    }

    /**
     * Converts an object Integer to a primitive int.
     *
     * @param value the <tt>Integer</tt> value
     * @return <tt>0</tt> if the given Integer value is null else <tt>integer value</tt>
     */
    public static int toPrimitiveInt(Integer value) {
        if (value == null) {
            return 0;
        }
        return value;
    }

    /**
     * Converts an object Long to a primitive long.
     *
     * @param value the <tt>Long</tt> value
     * @return <tt>0</tt> if the given Long value is null else <tt>long value</tt>
     */
    public static long toPrimitiveLong(Long value) {
        if (value == null) {
            return 0;
        }
        return value;
    }
    /**
     * Creates an Odata filter string that can be used for filtering list results by tags.
     *
     * @param tagName the name of the tag. If not provided, all resources will be returned.
     * @param tagValue the value of the tag. If not provided, only tag name will be filtered.
     * @return the Odata filter to pass into list methods
     */
    public static String createOdataFilterForTags(String tagName, String tagValue) {
        if (tagName == null) {
            return null;
        } else if (tagValue == null) {
            return String.format("tagname eq '%s'", tagName);
        } else {
            return String.format("tagname eq '%s' and tagvalue eq '%s'", tagName, tagValue);
        }
    }

    /**
     * Gets an observable of {@link U} that emits only the root resource from a given
     * observable of {@link Indexable}.
     *
     * @param stream the input observable of {@link Indexable}
     * @param <U> the specialized type of last item in the input stream
     * @return an observable that emits last item
     */
    @SuppressWarnings("unchecked")
    public static <U extends Indexable> Observable<U> rootResource(Observable<Indexable> stream) {
        return stream.last().map(new Func1<Indexable, U>() {
            @Override
            public U call(Indexable indexable) {
                return (U) indexable;
            }
        });
    }

    /**
     * Download a file asynchronously.
     * @param url the URL pointing to the file
     * @param retrofit the retrofit client
     * @return an Observable pointing to the content of the file
     */
    public static Observable<byte[]> downloadFileAsync(String url, Retrofit retrofit) {
        FileService service = retrofit.create(FileService.class);
        Observable<ResponseBody> response = service.download(url);
        return response.map(new Func1<ResponseBody, byte[]>() {
            @Override
            public byte[] call(ResponseBody responseBody) {
                try {
                    return responseBody.bytes();
                } catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
            }
        });
    }

    /**
     * Converts the given list of a type to paged list of a different type.
     *
     * @param list the list to convert to paged list
     * @param mapper the mapper to map type in input list to output list
     * @param <OutT> the type of items in output paged list
     * @param <InT> the type of items in input paged list
     * @return the paged list
     */
    public static <OutT, InT> PagedList<OutT> toPagedList(List<InT> list, final Func1<InT, OutT> mapper) {
        PageImpl<InT> page = new PageImpl<>();
        page.setItems(list);
        page.setNextPageLink(null);
        PagedList<InT> pagedList = new PagedList<InT>(page) {
            @Override
            public Page<InT> nextPage(String nextPageLink) {
                return null;
            }
        };
        PagedListConverter<InT, OutT> converter = new PagedListConverter<InT, OutT>() {
            @Override
            public OutT typeConvert(InT inner) {
                return mapper.call(inner);
            }
        };
        return converter.convert(pagedList);
    }

    /**
     * A Retrofit service used to download a file.
     */
    private interface FileService {
        @GET
        Observable<ResponseBody> download(@Url String url);
    }

    private Utils() {
    }
}
