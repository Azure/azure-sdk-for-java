/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi.implementation;

import retrofit2.Retrofit;
import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.cognitiveservices.faceapi.APIErrorException;
import com.microsoft.azure.cognitiveservices.faceapi.CreatePersonRequest;
import com.microsoft.azure.cognitiveservices.faceapi.ImageUrl;
import com.microsoft.azure.cognitiveservices.faceapi.UpdatePersonFaceDataRequest;
import com.microsoft.rest.CollectionFormat;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.Validator;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.HTTP;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.Response;
import rx.functions.Func1;
import rx.Observable;

/**
 * An instance of this class provides access to all the operations defined
 * in Persons.
 */
public class PersonsInner {
    /** The Retrofit service to perform REST calls. */
    private PersonsService service;
    /** The service client containing this operation class. */
    private FaceAPIImpl client;

    /**
     * Initializes an instance of PersonsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public PersonsInner(Retrofit retrofit, FaceAPIImpl client) {
        this.service = retrofit.create(PersonsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for Persons to be
     * used by Retrofit to perform actually REST calls.
     */
    interface PersonsService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.Persons create" })
        @POST("persongroups/{personGroupId}/persons")
        Observable<Response<ResponseBody>> create(@Path("personGroupId") String personGroupId, @Header("accept-language") String acceptLanguage, @Body CreatePersonRequest body, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.Persons list" })
        @GET("persongroups/{personGroupId}/persons")
        Observable<Response<ResponseBody>> list(@Path("personGroupId") String personGroupId, @Query("start") String start, @Query("top") Integer top, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.Persons delete" })
        @HTTP(path = "persongroups/{personGroupId}/persons/{personId}", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> delete(@Path("personGroupId") String personGroupId, @Path("personId") UUID personId, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.Persons get" })
        @GET("persongroups/{personGroupId}/persons/{personId}")
        Observable<Response<ResponseBody>> get(@Path("personGroupId") String personGroupId, @Path("personId") UUID personId, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.Persons update" })
        @PATCH("persongroups/{personGroupId}/persons/{personId}")
        Observable<Response<ResponseBody>> update(@Path("personGroupId") String personGroupId, @Path("personId") UUID personId, @Header("accept-language") String acceptLanguage, @Body CreatePersonRequest body, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.Persons deleteFace" })
        @HTTP(path = "persongroups/{personGroupId}/persons/{personId}/persistedFaces/{persistedFaceId}", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> deleteFace(@Path("personGroupId") String personGroupId, @Path("personId") UUID personId, @Path("persistedFaceId") UUID persistedFaceId, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.Persons getFace" })
        @GET("persongroups/{personGroupId}/persons/{personId}/persistedFaces/{persistedFaceId}")
        Observable<Response<ResponseBody>> getFace(@Path("personGroupId") String personGroupId, @Path("personId") UUID personId, @Path("persistedFaceId") UUID persistedFaceId, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.Persons updateFace" })
        @PATCH("persongroups/{personGroupId}/persons/{personId}/persistedFaces/{persistedFaceId}")
        Observable<Response<ResponseBody>> updateFace(@Path("personGroupId") String personGroupId, @Path("personId") UUID personId, @Path("persistedFaceId") UUID persistedFaceId, @Header("accept-language") String acceptLanguage, @Body UpdatePersonFaceDataRequest body, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.Persons addPersonFace" })
        @POST("persongroups/{personGroupId}/persons/{personId}/persistedFaces")
        Observable<Response<ResponseBody>> addPersonFace(@Path("personGroupId") String personGroupId, @Path("personId") UUID personId, @Query("userData") String userData, @Query("targetFace") String targetFace, @Header("accept-language") String acceptLanguage, @Body ImageUrl imageUrl, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/octet-stream", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.Persons addPersonFaceFromStream" })
        @POST("persongroups/{personGroupId}/persons/{personId}/persistedFaces")
        Observable<Response<ResponseBody>> addPersonFaceFromStream(@Path("personGroupId") String personGroupId, @Path("personId") UUID personId, @Query("userData") String userData, @Query("targetFace") String targetFace, @Body RequestBody image, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

    }

    /**
     * Create a new person in a specified person group.
     *
     * @param personGroupId Specifying the target person group to create the person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the CreatePersonResultInner object if successful.
     */
    public CreatePersonResultInner create(String personGroupId) {
        return createWithServiceResponseAsync(personGroupId).toBlocking().single().body();
    }

    /**
     * Create a new person in a specified person group.
     *
     * @param personGroupId Specifying the target person group to create the person.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<CreatePersonResultInner> createAsync(String personGroupId, final ServiceCallback<CreatePersonResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(createWithServiceResponseAsync(personGroupId), serviceCallback);
    }

    /**
     * Create a new person in a specified person group.
     *
     * @param personGroupId Specifying the target person group to create the person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the CreatePersonResultInner object
     */
    public Observable<CreatePersonResultInner> createAsync(String personGroupId) {
        return createWithServiceResponseAsync(personGroupId).map(new Func1<ServiceResponse<CreatePersonResultInner>, CreatePersonResultInner>() {
            @Override
            public CreatePersonResultInner call(ServiceResponse<CreatePersonResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Create a new person in a specified person group.
     *
     * @param personGroupId Specifying the target person group to create the person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the CreatePersonResultInner object
     */
    public Observable<ServiceResponse<CreatePersonResultInner>> createWithServiceResponseAsync(String personGroupId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        final String name = null;
        final String userData = null;
        CreatePersonRequest body = new CreatePersonRequest();
        body.withName(null);
        body.withUserData(null);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.create(personGroupId, this.client.acceptLanguage(), body, parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<CreatePersonResultInner>>>() {
                @Override
                public Observable<ServiceResponse<CreatePersonResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<CreatePersonResultInner> clientResponse = createDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Create a new person in a specified person group.
     *
     * @param personGroupId Specifying the target person group to create the person.
     * @param name Display name of the target person. The maximum length is 128.
     * @param userData Optional fields for user-provided data attached to a person. Size limit is 16KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the CreatePersonResultInner object if successful.
     */
    public CreatePersonResultInner create(String personGroupId, String name, String userData) {
        return createWithServiceResponseAsync(personGroupId, name, userData).toBlocking().single().body();
    }

    /**
     * Create a new person in a specified person group.
     *
     * @param personGroupId Specifying the target person group to create the person.
     * @param name Display name of the target person. The maximum length is 128.
     * @param userData Optional fields for user-provided data attached to a person. Size limit is 16KB.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<CreatePersonResultInner> createAsync(String personGroupId, String name, String userData, final ServiceCallback<CreatePersonResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(createWithServiceResponseAsync(personGroupId, name, userData), serviceCallback);
    }

    /**
     * Create a new person in a specified person group.
     *
     * @param personGroupId Specifying the target person group to create the person.
     * @param name Display name of the target person. The maximum length is 128.
     * @param userData Optional fields for user-provided data attached to a person. Size limit is 16KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the CreatePersonResultInner object
     */
    public Observable<CreatePersonResultInner> createAsync(String personGroupId, String name, String userData) {
        return createWithServiceResponseAsync(personGroupId, name, userData).map(new Func1<ServiceResponse<CreatePersonResultInner>, CreatePersonResultInner>() {
            @Override
            public CreatePersonResultInner call(ServiceResponse<CreatePersonResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Create a new person in a specified person group.
     *
     * @param personGroupId Specifying the target person group to create the person.
     * @param name Display name of the target person. The maximum length is 128.
     * @param userData Optional fields for user-provided data attached to a person. Size limit is 16KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the CreatePersonResultInner object
     */
    public Observable<ServiceResponse<CreatePersonResultInner>> createWithServiceResponseAsync(String personGroupId, String name, String userData) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        CreatePersonRequest body = new CreatePersonRequest();
        body.withName(name);
        body.withUserData(userData);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.create(personGroupId, this.client.acceptLanguage(), body, parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<CreatePersonResultInner>>>() {
                @Override
                public Observable<ServiceResponse<CreatePersonResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<CreatePersonResultInner> clientResponse = createDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<CreatePersonResultInner> createDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<CreatePersonResultInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<CreatePersonResultInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * List all persons in a person group, and retrieve person information (including personId, name, userData and persistedFaceIds of registered faces of the person).
     *
     * @param personGroupId personGroupId of the target person group.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;PersonResultInner&gt; object if successful.
     */
    public List<PersonResultInner> list(String personGroupId) {
        return listWithServiceResponseAsync(personGroupId).toBlocking().single().body();
    }

    /**
     * List all persons in a person group, and retrieve person information (including personId, name, userData and persistedFaceIds of registered faces of the person).
     *
     * @param personGroupId personGroupId of the target person group.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<PersonResultInner>> listAsync(String personGroupId, final ServiceCallback<List<PersonResultInner>> serviceCallback) {
        return ServiceFuture.fromResponse(listWithServiceResponseAsync(personGroupId), serviceCallback);
    }

    /**
     * List all persons in a person group, and retrieve person information (including personId, name, userData and persistedFaceIds of registered faces of the person).
     *
     * @param personGroupId personGroupId of the target person group.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;PersonResultInner&gt; object
     */
    public Observable<List<PersonResultInner>> listAsync(String personGroupId) {
        return listWithServiceResponseAsync(personGroupId).map(new Func1<ServiceResponse<List<PersonResultInner>>, List<PersonResultInner>>() {
            @Override
            public List<PersonResultInner> call(ServiceResponse<List<PersonResultInner>> response) {
                return response.body();
            }
        });
    }

    /**
     * List all persons in a person group, and retrieve person information (including personId, name, userData and persistedFaceIds of registered faces of the person).
     *
     * @param personGroupId personGroupId of the target person group.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;PersonResultInner&gt; object
     */
    public Observable<ServiceResponse<List<PersonResultInner>>> listWithServiceResponseAsync(String personGroupId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        final String start = null;
        final Integer top = null;
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.list(personGroupId, start, top, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<PersonResultInner>>>>() {
                @Override
                public Observable<ServiceResponse<List<PersonResultInner>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<PersonResultInner>> clientResponse = listDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * List all persons in a person group, and retrieve person information (including personId, name, userData and persistedFaceIds of registered faces of the person).
     *
     * @param personGroupId personGroupId of the target person group.
     * @param start Starting person id to return (used to list a range of persons).
     * @param top Number of persons to return starting with the person id indicated by the 'start' parameter.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;PersonResultInner&gt; object if successful.
     */
    public List<PersonResultInner> list(String personGroupId, String start, Integer top) {
        return listWithServiceResponseAsync(personGroupId, start, top).toBlocking().single().body();
    }

    /**
     * List all persons in a person group, and retrieve person information (including personId, name, userData and persistedFaceIds of registered faces of the person).
     *
     * @param personGroupId personGroupId of the target person group.
     * @param start Starting person id to return (used to list a range of persons).
     * @param top Number of persons to return starting with the person id indicated by the 'start' parameter.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<PersonResultInner>> listAsync(String personGroupId, String start, Integer top, final ServiceCallback<List<PersonResultInner>> serviceCallback) {
        return ServiceFuture.fromResponse(listWithServiceResponseAsync(personGroupId, start, top), serviceCallback);
    }

    /**
     * List all persons in a person group, and retrieve person information (including personId, name, userData and persistedFaceIds of registered faces of the person).
     *
     * @param personGroupId personGroupId of the target person group.
     * @param start Starting person id to return (used to list a range of persons).
     * @param top Number of persons to return starting with the person id indicated by the 'start' parameter.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;PersonResultInner&gt; object
     */
    public Observable<List<PersonResultInner>> listAsync(String personGroupId, String start, Integer top) {
        return listWithServiceResponseAsync(personGroupId, start, top).map(new Func1<ServiceResponse<List<PersonResultInner>>, List<PersonResultInner>>() {
            @Override
            public List<PersonResultInner> call(ServiceResponse<List<PersonResultInner>> response) {
                return response.body();
            }
        });
    }

    /**
     * List all persons in a person group, and retrieve person information (including personId, name, userData and persistedFaceIds of registered faces of the person).
     *
     * @param personGroupId personGroupId of the target person group.
     * @param start Starting person id to return (used to list a range of persons).
     * @param top Number of persons to return starting with the person id indicated by the 'start' parameter.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;PersonResultInner&gt; object
     */
    public Observable<ServiceResponse<List<PersonResultInner>>> listWithServiceResponseAsync(String personGroupId, String start, Integer top) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.list(personGroupId, start, top, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<PersonResultInner>>>>() {
                @Override
                public Observable<ServiceResponse<List<PersonResultInner>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<PersonResultInner>> clientResponse = listDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<List<PersonResultInner>> listDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<List<PersonResultInner>, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<List<PersonResultInner>>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Delete an existing person from a person group. Persisted face images of the person will also be deleted.
     *
     * @param personGroupId Specifying the person group containing the person.
     * @param personId The target personId to delete.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void delete(String personGroupId, UUID personId) {
        deleteWithServiceResponseAsync(personGroupId, personId).toBlocking().single().body();
    }

    /**
     * Delete an existing person from a person group. Persisted face images of the person will also be deleted.
     *
     * @param personGroupId Specifying the person group containing the person.
     * @param personId The target personId to delete.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> deleteAsync(String personGroupId, UUID personId, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(deleteWithServiceResponseAsync(personGroupId, personId), serviceCallback);
    }

    /**
     * Delete an existing person from a person group. Persisted face images of the person will also be deleted.
     *
     * @param personGroupId Specifying the person group containing the person.
     * @param personId The target personId to delete.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> deleteAsync(String personGroupId, UUID personId) {
        return deleteWithServiceResponseAsync(personGroupId, personId).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Delete an existing person from a person group. Persisted face images of the person will also be deleted.
     *
     * @param personGroupId Specifying the person group containing the person.
     * @param personId The target personId to delete.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> deleteWithServiceResponseAsync(String personGroupId, UUID personId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        if (personId == null) {
            throw new IllegalArgumentException("Parameter personId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.delete(personGroupId, personId, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = deleteDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Void> deleteDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Void, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Retrieve a person's information, including registered persisted faces, name and userData.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Specifying the target person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the PersonResultInner object if successful.
     */
    public PersonResultInner get(String personGroupId, UUID personId) {
        return getWithServiceResponseAsync(personGroupId, personId).toBlocking().single().body();
    }

    /**
     * Retrieve a person's information, including registered persisted faces, name and userData.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Specifying the target person.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<PersonResultInner> getAsync(String personGroupId, UUID personId, final ServiceCallback<PersonResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(getWithServiceResponseAsync(personGroupId, personId), serviceCallback);
    }

    /**
     * Retrieve a person's information, including registered persisted faces, name and userData.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Specifying the target person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PersonResultInner object
     */
    public Observable<PersonResultInner> getAsync(String personGroupId, UUID personId) {
        return getWithServiceResponseAsync(personGroupId, personId).map(new Func1<ServiceResponse<PersonResultInner>, PersonResultInner>() {
            @Override
            public PersonResultInner call(ServiceResponse<PersonResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Retrieve a person's information, including registered persisted faces, name and userData.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Specifying the target person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PersonResultInner object
     */
    public Observable<ServiceResponse<PersonResultInner>> getWithServiceResponseAsync(String personGroupId, UUID personId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        if (personId == null) {
            throw new IllegalArgumentException("Parameter personId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.get(personGroupId, personId, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<PersonResultInner>>>() {
                @Override
                public Observable<ServiceResponse<PersonResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<PersonResultInner> clientResponse = getDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<PersonResultInner> getDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<PersonResultInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<PersonResultInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Update name or userData of a person.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void update(String personGroupId, UUID personId) {
        updateWithServiceResponseAsync(personGroupId, personId).toBlocking().single().body();
    }

    /**
     * Update name or userData of a person.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> updateAsync(String personGroupId, UUID personId, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(updateWithServiceResponseAsync(personGroupId, personId), serviceCallback);
    }

    /**
     * Update name or userData of a person.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> updateAsync(String personGroupId, UUID personId) {
        return updateWithServiceResponseAsync(personGroupId, personId).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Update name or userData of a person.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> updateWithServiceResponseAsync(String personGroupId, UUID personId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        if (personId == null) {
            throw new IllegalArgumentException("Parameter personId is required and cannot be null.");
        }
        final String name = null;
        final String userData = null;
        CreatePersonRequest body = new CreatePersonRequest();
        body.withName(null);
        body.withUserData(null);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.update(personGroupId, personId, this.client.acceptLanguage(), body, parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = updateDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Update name or userData of a person.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @param name Display name of the target person. The maximum length is 128.
     * @param userData Optional fields for user-provided data attached to a person. Size limit is 16KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void update(String personGroupId, UUID personId, String name, String userData) {
        updateWithServiceResponseAsync(personGroupId, personId, name, userData).toBlocking().single().body();
    }

    /**
     * Update name or userData of a person.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @param name Display name of the target person. The maximum length is 128.
     * @param userData Optional fields for user-provided data attached to a person. Size limit is 16KB.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> updateAsync(String personGroupId, UUID personId, String name, String userData, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(updateWithServiceResponseAsync(personGroupId, personId, name, userData), serviceCallback);
    }

    /**
     * Update name or userData of a person.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @param name Display name of the target person. The maximum length is 128.
     * @param userData Optional fields for user-provided data attached to a person. Size limit is 16KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> updateAsync(String personGroupId, UUID personId, String name, String userData) {
        return updateWithServiceResponseAsync(personGroupId, personId, name, userData).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Update name or userData of a person.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @param name Display name of the target person. The maximum length is 128.
     * @param userData Optional fields for user-provided data attached to a person. Size limit is 16KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> updateWithServiceResponseAsync(String personGroupId, UUID personId, String name, String userData) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        if (personId == null) {
            throw new IllegalArgumentException("Parameter personId is required and cannot be null.");
        }
        CreatePersonRequest body = new CreatePersonRequest();
        body.withName(name);
        body.withUserData(userData);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.update(personGroupId, personId, this.client.acceptLanguage(), body, parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = updateDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Void> updateDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Void, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Delete a face from a person. Relative image for the persisted face will also be deleted.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Specifying the person that the target persisted face belong to.
     * @param persistedFaceId The persisted face to remove.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void deleteFace(String personGroupId, UUID personId, UUID persistedFaceId) {
        deleteFaceWithServiceResponseAsync(personGroupId, personId, persistedFaceId).toBlocking().single().body();
    }

    /**
     * Delete a face from a person. Relative image for the persisted face will also be deleted.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Specifying the person that the target persisted face belong to.
     * @param persistedFaceId The persisted face to remove.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> deleteFaceAsync(String personGroupId, UUID personId, UUID persistedFaceId, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(deleteFaceWithServiceResponseAsync(personGroupId, personId, persistedFaceId), serviceCallback);
    }

    /**
     * Delete a face from a person. Relative image for the persisted face will also be deleted.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Specifying the person that the target persisted face belong to.
     * @param persistedFaceId The persisted face to remove.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> deleteFaceAsync(String personGroupId, UUID personId, UUID persistedFaceId) {
        return deleteFaceWithServiceResponseAsync(personGroupId, personId, persistedFaceId).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Delete a face from a person. Relative image for the persisted face will also be deleted.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Specifying the person that the target persisted face belong to.
     * @param persistedFaceId The persisted face to remove.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> deleteFaceWithServiceResponseAsync(String personGroupId, UUID personId, UUID persistedFaceId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        if (personId == null) {
            throw new IllegalArgumentException("Parameter personId is required and cannot be null.");
        }
        if (persistedFaceId == null) {
            throw new IllegalArgumentException("Parameter persistedFaceId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.deleteFace(personGroupId, personId, persistedFaceId, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = deleteFaceDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Void> deleteFaceDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Void, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Retrieve information about a persisted face (specified by persistedFaceId, personId and its belonging personGroupId).
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Specifying the target person that the face belongs to.
     * @param persistedFaceId The persistedFaceId of the target persisted face of the person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the PersonFaceResultInner object if successful.
     */
    public PersonFaceResultInner getFace(String personGroupId, UUID personId, UUID persistedFaceId) {
        return getFaceWithServiceResponseAsync(personGroupId, personId, persistedFaceId).toBlocking().single().body();
    }

    /**
     * Retrieve information about a persisted face (specified by persistedFaceId, personId and its belonging personGroupId).
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Specifying the target person that the face belongs to.
     * @param persistedFaceId The persistedFaceId of the target persisted face of the person.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<PersonFaceResultInner> getFaceAsync(String personGroupId, UUID personId, UUID persistedFaceId, final ServiceCallback<PersonFaceResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(getFaceWithServiceResponseAsync(personGroupId, personId, persistedFaceId), serviceCallback);
    }

    /**
     * Retrieve information about a persisted face (specified by persistedFaceId, personId and its belonging personGroupId).
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Specifying the target person that the face belongs to.
     * @param persistedFaceId The persistedFaceId of the target persisted face of the person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PersonFaceResultInner object
     */
    public Observable<PersonFaceResultInner> getFaceAsync(String personGroupId, UUID personId, UUID persistedFaceId) {
        return getFaceWithServiceResponseAsync(personGroupId, personId, persistedFaceId).map(new Func1<ServiceResponse<PersonFaceResultInner>, PersonFaceResultInner>() {
            @Override
            public PersonFaceResultInner call(ServiceResponse<PersonFaceResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Retrieve information about a persisted face (specified by persistedFaceId, personId and its belonging personGroupId).
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Specifying the target person that the face belongs to.
     * @param persistedFaceId The persistedFaceId of the target persisted face of the person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PersonFaceResultInner object
     */
    public Observable<ServiceResponse<PersonFaceResultInner>> getFaceWithServiceResponseAsync(String personGroupId, UUID personId, UUID persistedFaceId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        if (personId == null) {
            throw new IllegalArgumentException("Parameter personId is required and cannot be null.");
        }
        if (persistedFaceId == null) {
            throw new IllegalArgumentException("Parameter persistedFaceId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.getFace(personGroupId, personId, persistedFaceId, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<PersonFaceResultInner>>>() {
                @Override
                public Observable<ServiceResponse<PersonFaceResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<PersonFaceResultInner> clientResponse = getFaceDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<PersonFaceResultInner> getFaceDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<PersonFaceResultInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<PersonFaceResultInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Update a person persisted face's userData field.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @param persistedFaceId persistedFaceId of target face, which is persisted and will not expire.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void updateFace(String personGroupId, UUID personId, UUID persistedFaceId) {
        updateFaceWithServiceResponseAsync(personGroupId, personId, persistedFaceId).toBlocking().single().body();
    }

    /**
     * Update a person persisted face's userData field.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @param persistedFaceId persistedFaceId of target face, which is persisted and will not expire.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> updateFaceAsync(String personGroupId, UUID personId, UUID persistedFaceId, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(updateFaceWithServiceResponseAsync(personGroupId, personId, persistedFaceId), serviceCallback);
    }

    /**
     * Update a person persisted face's userData field.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @param persistedFaceId persistedFaceId of target face, which is persisted and will not expire.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> updateFaceAsync(String personGroupId, UUID personId, UUID persistedFaceId) {
        return updateFaceWithServiceResponseAsync(personGroupId, personId, persistedFaceId).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Update a person persisted face's userData field.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @param persistedFaceId persistedFaceId of target face, which is persisted and will not expire.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> updateFaceWithServiceResponseAsync(String personGroupId, UUID personId, UUID persistedFaceId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        if (personId == null) {
            throw new IllegalArgumentException("Parameter personId is required and cannot be null.");
        }
        if (persistedFaceId == null) {
            throw new IllegalArgumentException("Parameter persistedFaceId is required and cannot be null.");
        }
        final String userData = null;
        UpdatePersonFaceDataRequest body = new UpdatePersonFaceDataRequest();
        body.withUserData(null);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.updateFace(personGroupId, personId, persistedFaceId, this.client.acceptLanguage(), body, parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = updateFaceDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Update a person persisted face's userData field.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @param persistedFaceId persistedFaceId of target face, which is persisted and will not expire.
     * @param userData User-provided data attached to the face. The size limit is 1KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void updateFace(String personGroupId, UUID personId, UUID persistedFaceId, String userData) {
        updateFaceWithServiceResponseAsync(personGroupId, personId, persistedFaceId, userData).toBlocking().single().body();
    }

    /**
     * Update a person persisted face's userData field.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @param persistedFaceId persistedFaceId of target face, which is persisted and will not expire.
     * @param userData User-provided data attached to the face. The size limit is 1KB.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> updateFaceAsync(String personGroupId, UUID personId, UUID persistedFaceId, String userData, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(updateFaceWithServiceResponseAsync(personGroupId, personId, persistedFaceId, userData), serviceCallback);
    }

    /**
     * Update a person persisted face's userData field.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @param persistedFaceId persistedFaceId of target face, which is persisted and will not expire.
     * @param userData User-provided data attached to the face. The size limit is 1KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> updateFaceAsync(String personGroupId, UUID personId, UUID persistedFaceId, String userData) {
        return updateFaceWithServiceResponseAsync(personGroupId, personId, persistedFaceId, userData).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Update a person persisted face's userData field.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @param persistedFaceId persistedFaceId of target face, which is persisted and will not expire.
     * @param userData User-provided data attached to the face. The size limit is 1KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> updateFaceWithServiceResponseAsync(String personGroupId, UUID personId, UUID persistedFaceId, String userData) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        if (personId == null) {
            throw new IllegalArgumentException("Parameter personId is required and cannot be null.");
        }
        if (persistedFaceId == null) {
            throw new IllegalArgumentException("Parameter persistedFaceId is required and cannot be null.");
        }
        UpdatePersonFaceDataRequest body = new UpdatePersonFaceDataRequest();
        body.withUserData(userData);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.updateFace(personGroupId, personId, persistedFaceId, this.client.acceptLanguage(), body, parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = updateFaceDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Void> updateFaceDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Void, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the PersistedFaceResultInner object if successful.
     */
    public PersistedFaceResultInner addPersonFace(String personGroupId, UUID personId, String url) {
        return addPersonFaceWithServiceResponseAsync(personGroupId, personId, url).toBlocking().single().body();
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param url the String value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<PersistedFaceResultInner> addPersonFaceAsync(String personGroupId, UUID personId, String url, final ServiceCallback<PersistedFaceResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(addPersonFaceWithServiceResponseAsync(personGroupId, personId, url), serviceCallback);
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PersistedFaceResultInner object
     */
    public Observable<PersistedFaceResultInner> addPersonFaceAsync(String personGroupId, UUID personId, String url) {
        return addPersonFaceWithServiceResponseAsync(personGroupId, personId, url).map(new Func1<ServiceResponse<PersistedFaceResultInner>, PersistedFaceResultInner>() {
            @Override
            public PersistedFaceResultInner call(ServiceResponse<PersistedFaceResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PersistedFaceResultInner object
     */
    public Observable<ServiceResponse<PersistedFaceResultInner>> addPersonFaceWithServiceResponseAsync(String personGroupId, UUID personId, String url) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        if (personId == null) {
            throw new IllegalArgumentException("Parameter personId is required and cannot be null.");
        }
        if (url == null) {
            throw new IllegalArgumentException("Parameter url is required and cannot be null.");
        }
        final String userData = null;
        final List<Integer> targetFace = null;
        ImageUrl imageUrl = new ImageUrl();
        imageUrl.withUrl(url);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        String targetFaceConverted = this.client.serializerAdapter().serializeList(targetFace, CollectionFormat.CSV);
        return service.addPersonFace(personGroupId, personId, userData, targetFaceConverted, this.client.acceptLanguage(), imageUrl, parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<PersistedFaceResultInner>>>() {
                @Override
                public Observable<ServiceResponse<PersistedFaceResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<PersistedFaceResultInner> clientResponse = addPersonFaceDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param url the String value
     * @param userData User-specified data about the target face to add for any purpose. The maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added to a person in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the PersistedFaceResultInner object if successful.
     */
    public PersistedFaceResultInner addPersonFace(String personGroupId, UUID personId, String url, String userData, List<Integer> targetFace) {
        return addPersonFaceWithServiceResponseAsync(personGroupId, personId, url, userData, targetFace).toBlocking().single().body();
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param url the String value
     * @param userData User-specified data about the target face to add for any purpose. The maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added to a person in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<PersistedFaceResultInner> addPersonFaceAsync(String personGroupId, UUID personId, String url, String userData, List<Integer> targetFace, final ServiceCallback<PersistedFaceResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(addPersonFaceWithServiceResponseAsync(personGroupId, personId, url, userData, targetFace), serviceCallback);
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param url the String value
     * @param userData User-specified data about the target face to add for any purpose. The maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added to a person in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PersistedFaceResultInner object
     */
    public Observable<PersistedFaceResultInner> addPersonFaceAsync(String personGroupId, UUID personId, String url, String userData, List<Integer> targetFace) {
        return addPersonFaceWithServiceResponseAsync(personGroupId, personId, url, userData, targetFace).map(new Func1<ServiceResponse<PersistedFaceResultInner>, PersistedFaceResultInner>() {
            @Override
            public PersistedFaceResultInner call(ServiceResponse<PersistedFaceResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param url the String value
     * @param userData User-specified data about the target face to add for any purpose. The maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added to a person in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PersistedFaceResultInner object
     */
    public Observable<ServiceResponse<PersistedFaceResultInner>> addPersonFaceWithServiceResponseAsync(String personGroupId, UUID personId, String url, String userData, List<Integer> targetFace) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        if (personId == null) {
            throw new IllegalArgumentException("Parameter personId is required and cannot be null.");
        }
        if (url == null) {
            throw new IllegalArgumentException("Parameter url is required and cannot be null.");
        }
        Validator.validate(targetFace);
        ImageUrl imageUrl = new ImageUrl();
        imageUrl.withUrl(url);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        String targetFaceConverted = this.client.serializerAdapter().serializeList(targetFace, CollectionFormat.CSV);
        return service.addPersonFace(personGroupId, personId, userData, targetFaceConverted, this.client.acceptLanguage(), imageUrl, parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<PersistedFaceResultInner>>>() {
                @Override
                public Observable<ServiceResponse<PersistedFaceResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<PersistedFaceResultInner> clientResponse = addPersonFaceDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<PersistedFaceResultInner> addPersonFaceDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<PersistedFaceResultInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<PersistedFaceResultInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the PersistedFaceResultInner object if successful.
     */
    public PersistedFaceResultInner addPersonFaceFromStream(String personGroupId, UUID personId, byte[] image) {
        return addPersonFaceFromStreamWithServiceResponseAsync(personGroupId, personId, image).toBlocking().single().body();
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param image An image stream.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<PersistedFaceResultInner> addPersonFaceFromStreamAsync(String personGroupId, UUID personId, byte[] image, final ServiceCallback<PersistedFaceResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(addPersonFaceFromStreamWithServiceResponseAsync(personGroupId, personId, image), serviceCallback);
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PersistedFaceResultInner object
     */
    public Observable<PersistedFaceResultInner> addPersonFaceFromStreamAsync(String personGroupId, UUID personId, byte[] image) {
        return addPersonFaceFromStreamWithServiceResponseAsync(personGroupId, personId, image).map(new Func1<ServiceResponse<PersistedFaceResultInner>, PersistedFaceResultInner>() {
            @Override
            public PersistedFaceResultInner call(ServiceResponse<PersistedFaceResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PersistedFaceResultInner object
     */
    public Observable<ServiceResponse<PersistedFaceResultInner>> addPersonFaceFromStreamWithServiceResponseAsync(String personGroupId, UUID personId, byte[] image) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        if (personId == null) {
            throw new IllegalArgumentException("Parameter personId is required and cannot be null.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Parameter image is required and cannot be null.");
        }
        final String userData = null;
        final List<Integer> targetFace = null;
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        String targetFaceConverted = this.client.serializerAdapter().serializeList(targetFace, CollectionFormat.CSV);
        RequestBody imageConverted = RequestBody.create(MediaType.parse("application/octet-stream"), image);
        return service.addPersonFaceFromStream(personGroupId, personId, userData, targetFaceConverted, imageConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<PersistedFaceResultInner>>>() {
                @Override
                public Observable<ServiceResponse<PersistedFaceResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<PersistedFaceResultInner> clientResponse = addPersonFaceFromStreamDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param image An image stream.
     * @param userData User-specified data about the target face to add for any purpose. The maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added to a person in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the PersistedFaceResultInner object if successful.
     */
    public PersistedFaceResultInner addPersonFaceFromStream(String personGroupId, UUID personId, byte[] image, String userData, List<Integer> targetFace) {
        return addPersonFaceFromStreamWithServiceResponseAsync(personGroupId, personId, image, userData, targetFace).toBlocking().single().body();
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param image An image stream.
     * @param userData User-specified data about the target face to add for any purpose. The maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added to a person in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<PersistedFaceResultInner> addPersonFaceFromStreamAsync(String personGroupId, UUID personId, byte[] image, String userData, List<Integer> targetFace, final ServiceCallback<PersistedFaceResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(addPersonFaceFromStreamWithServiceResponseAsync(personGroupId, personId, image, userData, targetFace), serviceCallback);
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param image An image stream.
     * @param userData User-specified data about the target face to add for any purpose. The maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added to a person in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PersistedFaceResultInner object
     */
    public Observable<PersistedFaceResultInner> addPersonFaceFromStreamAsync(String personGroupId, UUID personId, byte[] image, String userData, List<Integer> targetFace) {
        return addPersonFaceFromStreamWithServiceResponseAsync(personGroupId, personId, image, userData, targetFace).map(new Func1<ServiceResponse<PersistedFaceResultInner>, PersistedFaceResultInner>() {
            @Override
            public PersistedFaceResultInner call(ServiceResponse<PersistedFaceResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param image An image stream.
     * @param userData User-specified data about the target face to add for any purpose. The maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added to a person in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PersistedFaceResultInner object
     */
    public Observable<ServiceResponse<PersistedFaceResultInner>> addPersonFaceFromStreamWithServiceResponseAsync(String personGroupId, UUID personId, byte[] image, String userData, List<Integer> targetFace) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        if (personId == null) {
            throw new IllegalArgumentException("Parameter personId is required and cannot be null.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Parameter image is required and cannot be null.");
        }
        Validator.validate(targetFace);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        String targetFaceConverted = this.client.serializerAdapter().serializeList(targetFace, CollectionFormat.CSV);
        RequestBody imageConverted = RequestBody.create(MediaType.parse("application/octet-stream"), image);
        return service.addPersonFaceFromStream(personGroupId, personId, userData, targetFaceConverted, imageConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<PersistedFaceResultInner>>>() {
                @Override
                public Observable<ServiceResponse<PersistedFaceResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<PersistedFaceResultInner> clientResponse = addPersonFaceFromStreamDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<PersistedFaceResultInner> addPersonFaceFromStreamDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<PersistedFaceResultInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<PersistedFaceResultInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

}
