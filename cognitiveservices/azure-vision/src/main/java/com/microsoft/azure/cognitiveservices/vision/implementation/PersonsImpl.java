/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.vision.implementation;

import retrofit2.Retrofit;
import com.microsoft.azure.cognitiveservices.vision.Persons;
import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.cognitiveservices.vision.models.APIErrorException;
import com.microsoft.azure.cognitiveservices.vision.models.CreatePersonRequest;
import com.microsoft.azure.cognitiveservices.vision.models.CreatePersonResult;
import com.microsoft.azure.cognitiveservices.vision.models.PersonFaceResult;
import com.microsoft.azure.cognitiveservices.vision.models.PersonResult;
import com.microsoft.azure.cognitiveservices.vision.models.UpdatePersonFaceDataRequest;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import java.io.IOException;
import java.util.List;
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
public class PersonsImpl implements Persons {
    /** The Retrofit service to perform REST calls. */
    private PersonsService service;
    /** The service client containing this operation class. */
    private FaceAPIImpl client;

    /**
     * Initializes an instance of Persons.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public PersonsImpl(Retrofit retrofit, FaceAPIImpl client) {
        this.service = retrofit.create(PersonsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for Persons to be
     * used by Retrofit to perform actually REST calls.
     */
    interface PersonsService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.vision.Persons create" })
        @POST("persongroups/{personGroupId}/persons")
        Observable<Response<ResponseBody>> create(@Path("personGroupId") String personGroupId, @Body CreatePersonRequest body, @Header("x-ms-parameterized-host") String parameterizedHost);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.vision.Persons list" })
        @GET("persongroups/{personGroupId}/persons")
        Observable<Response<ResponseBody>> list(@Path("personGroupId") String personGroupId, @Header("x-ms-parameterized-host") String parameterizedHost);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.vision.Persons delete" })
        @HTTP(path = "persongroups/{personGroupId}/persons/{personId}", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> delete(@Path("personGroupId") String personGroupId, @Path("personId") String personId, @Header("x-ms-parameterized-host") String parameterizedHost);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.vision.Persons get" })
        @GET("persongroups/{personGroupId}/persons/{personId}")
        Observable<Response<ResponseBody>> get(@Path("personGroupId") String personGroupId, @Path("personId") String personId, @Header("x-ms-parameterized-host") String parameterizedHost);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.vision.Persons update" })
        @PATCH("persongroups/{personGroupId}/persons/{personId}")
        Observable<Response<ResponseBody>> update(@Path("personGroupId") String personGroupId, @Path("personId") String personId, @Body CreatePersonRequest body, @Header("x-ms-parameterized-host") String parameterizedHost);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.vision.Persons deleteFace" })
        @HTTP(path = "persongroups/{personGroupId}/persons/{personId}/persistedFaces/{persistedFaceId}", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> deleteFace(@Path("personGroupId") String personGroupId, @Path("personId") String personId, @Path("persistedFaceId") String persistedFaceId, @Header("x-ms-parameterized-host") String parameterizedHost);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.vision.Persons getFace" })
        @GET("persongroups/{personGroupId}/persons/{personId}/persistedFaces/{persistedFaceId}")
        Observable<Response<ResponseBody>> getFace(@Path("personGroupId") String personGroupId, @Path("personId") String personId, @Path("persistedFaceId") String persistedFaceId, @Header("x-ms-parameterized-host") String parameterizedHost);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.vision.Persons updateFace" })
        @PATCH("persongroups/{personGroupId}/persons/{personId}/persistedFaces/{persistedFaceId}")
        Observable<Response<ResponseBody>> updateFace(@Path("personGroupId") String personGroupId, @Path("personId") String personId, @Path("persistedFaceId") String persistedFaceId, @Body UpdatePersonFaceDataRequest body, @Header("x-ms-parameterized-host") String parameterizedHost);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.vision.Persons addFace" })
        @POST("persongroups/{personGroupId}/persons/{personId}/persistedFaces")
        Observable<Response<ResponseBody>> addFace(@Path("personGroupId") String personGroupId, @Path("personId") String personId, @Query("userData") String userData, @Query("targetFace") String targetFace, @Header("x-ms-parameterized-host") String parameterizedHost);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.vision.Persons addFaceFromStream" })
        @POST("persongroups/{personGroupId}/persons/{personId}/persistedFaces")
        Observable<Response<ResponseBody>> addFaceFromStream(@Path("personGroupId") String personGroupId, @Path("personId") String personId, @Query("userData") String userData, @Query("targetFace") String targetFace, @Header("x-ms-parameterized-host") String parameterizedHost);

    }

    /**
     * Create a new person in a specified person group.
     *
     * @param personGroupId Specifying the target person group to create the person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the CreatePersonResult object if successful.
     */
    public CreatePersonResult create(String personGroupId) {
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
    public ServiceFuture<CreatePersonResult> createAsync(String personGroupId, final ServiceCallback<CreatePersonResult> serviceCallback) {
        return ServiceFuture.fromResponse(createWithServiceResponseAsync(personGroupId), serviceCallback);
    }

    /**
     * Create a new person in a specified person group.
     *
     * @param personGroupId Specifying the target person group to create the person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the CreatePersonResult object
     */
    public Observable<CreatePersonResult> createAsync(String personGroupId) {
        return createWithServiceResponseAsync(personGroupId).map(new Func1<ServiceResponse<CreatePersonResult>, CreatePersonResult>() {
            @Override
            public CreatePersonResult call(ServiceResponse<CreatePersonResult> response) {
                return response.body();
            }
        });
    }

    /**
     * Create a new person in a specified person group.
     *
     * @param personGroupId Specifying the target person group to create the person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the CreatePersonResult object
     */
    public Observable<ServiceResponse<CreatePersonResult>> createWithServiceResponseAsync(String personGroupId) {
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
        return service.create(personGroupId, body, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<CreatePersonResult>>>() {
                @Override
                public Observable<ServiceResponse<CreatePersonResult>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<CreatePersonResult> clientResponse = createDelegate(response);
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
     * @return the CreatePersonResult object if successful.
     */
    public CreatePersonResult create(String personGroupId, String name, String userData) {
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
    public ServiceFuture<CreatePersonResult> createAsync(String personGroupId, String name, String userData, final ServiceCallback<CreatePersonResult> serviceCallback) {
        return ServiceFuture.fromResponse(createWithServiceResponseAsync(personGroupId, name, userData), serviceCallback);
    }

    /**
     * Create a new person in a specified person group.
     *
     * @param personGroupId Specifying the target person group to create the person.
     * @param name Display name of the target person. The maximum length is 128.
     * @param userData Optional fields for user-provided data attached to a person. Size limit is 16KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the CreatePersonResult object
     */
    public Observable<CreatePersonResult> createAsync(String personGroupId, String name, String userData) {
        return createWithServiceResponseAsync(personGroupId, name, userData).map(new Func1<ServiceResponse<CreatePersonResult>, CreatePersonResult>() {
            @Override
            public CreatePersonResult call(ServiceResponse<CreatePersonResult> response) {
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
     * @return the observable to the CreatePersonResult object
     */
    public Observable<ServiceResponse<CreatePersonResult>> createWithServiceResponseAsync(String personGroupId, String name, String userData) {
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
        return service.create(personGroupId, body, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<CreatePersonResult>>>() {
                @Override
                public Observable<ServiceResponse<CreatePersonResult>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<CreatePersonResult> clientResponse = createDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<CreatePersonResult> createDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<CreatePersonResult, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<CreatePersonResult>() { }.getType())
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
     * @return the List&lt;PersonResult&gt; object if successful.
     */
    public List<PersonResult> list(String personGroupId) {
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
    public ServiceFuture<List<PersonResult>> listAsync(String personGroupId, final ServiceCallback<List<PersonResult>> serviceCallback) {
        return ServiceFuture.fromResponse(listWithServiceResponseAsync(personGroupId), serviceCallback);
    }

    /**
     * List all persons in a person group, and retrieve person information (including personId, name, userData and persistedFaceIds of registered faces of the person).
     *
     * @param personGroupId personGroupId of the target person group.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;PersonResult&gt; object
     */
    public Observable<List<PersonResult>> listAsync(String personGroupId) {
        return listWithServiceResponseAsync(personGroupId).map(new Func1<ServiceResponse<List<PersonResult>>, List<PersonResult>>() {
            @Override
            public List<PersonResult> call(ServiceResponse<List<PersonResult>> response) {
                return response.body();
            }
        });
    }

    /**
     * List all persons in a person group, and retrieve person information (including personId, name, userData and persistedFaceIds of registered faces of the person).
     *
     * @param personGroupId personGroupId of the target person group.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;PersonResult&gt; object
     */
    public Observable<ServiceResponse<List<PersonResult>>> listWithServiceResponseAsync(String personGroupId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.list(personGroupId, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<PersonResult>>>>() {
                @Override
                public Observable<ServiceResponse<List<PersonResult>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<PersonResult>> clientResponse = listDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<List<PersonResult>> listDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<List<PersonResult>, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<List<PersonResult>>() { }.getType())
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
    public void delete(String personGroupId, String personId) {
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
    public ServiceFuture<Void> deleteAsync(String personGroupId, String personId, final ServiceCallback<Void> serviceCallback) {
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
    public Observable<Void> deleteAsync(String personGroupId, String personId) {
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
    public Observable<ServiceResponse<Void>> deleteWithServiceResponseAsync(String personGroupId, String personId) {
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
        return service.delete(personGroupId, personId, parameterizedHost)
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
     * @return the PersonResult object if successful.
     */
    public PersonResult get(String personGroupId, String personId) {
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
    public ServiceFuture<PersonResult> getAsync(String personGroupId, String personId, final ServiceCallback<PersonResult> serviceCallback) {
        return ServiceFuture.fromResponse(getWithServiceResponseAsync(personGroupId, personId), serviceCallback);
    }

    /**
     * Retrieve a person's information, including registered persisted faces, name and userData.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Specifying the target person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PersonResult object
     */
    public Observable<PersonResult> getAsync(String personGroupId, String personId) {
        return getWithServiceResponseAsync(personGroupId, personId).map(new Func1<ServiceResponse<PersonResult>, PersonResult>() {
            @Override
            public PersonResult call(ServiceResponse<PersonResult> response) {
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
     * @return the observable to the PersonResult object
     */
    public Observable<ServiceResponse<PersonResult>> getWithServiceResponseAsync(String personGroupId, String personId) {
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
        return service.get(personGroupId, personId, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<PersonResult>>>() {
                @Override
                public Observable<ServiceResponse<PersonResult>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<PersonResult> clientResponse = getDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<PersonResult> getDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<PersonResult, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<PersonResult>() { }.getType())
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
    public void update(String personGroupId, String personId) {
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
    public ServiceFuture<Void> updateAsync(String personGroupId, String personId, final ServiceCallback<Void> serviceCallback) {
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
    public Observable<Void> updateAsync(String personGroupId, String personId) {
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
    public Observable<ServiceResponse<Void>> updateWithServiceResponseAsync(String personGroupId, String personId) {
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
        return service.update(personGroupId, personId, body, parameterizedHost)
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
    public void update(String personGroupId, String personId, String name, String userData) {
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
    public ServiceFuture<Void> updateAsync(String personGroupId, String personId, String name, String userData, final ServiceCallback<Void> serviceCallback) {
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
    public Observable<Void> updateAsync(String personGroupId, String personId, String name, String userData) {
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
    public Observable<ServiceResponse<Void>> updateWithServiceResponseAsync(String personGroupId, String personId, String name, String userData) {
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
        return service.update(personGroupId, personId, body, parameterizedHost)
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
    public void deleteFace(String personGroupId, String personId, String persistedFaceId) {
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
    public ServiceFuture<Void> deleteFaceAsync(String personGroupId, String personId, String persistedFaceId, final ServiceCallback<Void> serviceCallback) {
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
    public Observable<Void> deleteFaceAsync(String personGroupId, String personId, String persistedFaceId) {
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
    public Observable<ServiceResponse<Void>> deleteFaceWithServiceResponseAsync(String personGroupId, String personId, String persistedFaceId) {
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
        return service.deleteFace(personGroupId, personId, persistedFaceId, parameterizedHost)
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
     * @return the PersonFaceResult object if successful.
     */
    public PersonFaceResult getFace(String personGroupId, String personId, String persistedFaceId) {
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
    public ServiceFuture<PersonFaceResult> getFaceAsync(String personGroupId, String personId, String persistedFaceId, final ServiceCallback<PersonFaceResult> serviceCallback) {
        return ServiceFuture.fromResponse(getFaceWithServiceResponseAsync(personGroupId, personId, persistedFaceId), serviceCallback);
    }

    /**
     * Retrieve information about a persisted face (specified by persistedFaceId, personId and its belonging personGroupId).
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Specifying the target person that the face belongs to.
     * @param persistedFaceId The persistedFaceId of the target persisted face of the person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PersonFaceResult object
     */
    public Observable<PersonFaceResult> getFaceAsync(String personGroupId, String personId, String persistedFaceId) {
        return getFaceWithServiceResponseAsync(personGroupId, personId, persistedFaceId).map(new Func1<ServiceResponse<PersonFaceResult>, PersonFaceResult>() {
            @Override
            public PersonFaceResult call(ServiceResponse<PersonFaceResult> response) {
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
     * @return the observable to the PersonFaceResult object
     */
    public Observable<ServiceResponse<PersonFaceResult>> getFaceWithServiceResponseAsync(String personGroupId, String personId, String persistedFaceId) {
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
        return service.getFace(personGroupId, personId, persistedFaceId, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<PersonFaceResult>>>() {
                @Override
                public Observable<ServiceResponse<PersonFaceResult>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<PersonFaceResult> clientResponse = getFaceDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<PersonFaceResult> getFaceDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<PersonFaceResult, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<PersonFaceResult>() { }.getType())
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
    public void updateFace(String personGroupId, String personId, String persistedFaceId) {
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
    public ServiceFuture<Void> updateFaceAsync(String personGroupId, String personId, String persistedFaceId, final ServiceCallback<Void> serviceCallback) {
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
    public Observable<Void> updateFaceAsync(String personGroupId, String personId, String persistedFaceId) {
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
    public Observable<ServiceResponse<Void>> updateFaceWithServiceResponseAsync(String personGroupId, String personId, String persistedFaceId) {
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
        return service.updateFace(personGroupId, personId, persistedFaceId, body, parameterizedHost)
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
     * @param userData User-provided data attached to the face. The size limit is 1KB
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void updateFace(String personGroupId, String personId, String persistedFaceId, String userData) {
        updateFaceWithServiceResponseAsync(personGroupId, personId, persistedFaceId, userData).toBlocking().single().body();
    }

    /**
     * Update a person persisted face's userData field.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @param persistedFaceId persistedFaceId of target face, which is persisted and will not expire.
     * @param userData User-provided data attached to the face. The size limit is 1KB
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> updateFaceAsync(String personGroupId, String personId, String persistedFaceId, String userData, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(updateFaceWithServiceResponseAsync(personGroupId, personId, persistedFaceId, userData), serviceCallback);
    }

    /**
     * Update a person persisted face's userData field.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @param persistedFaceId persistedFaceId of target face, which is persisted and will not expire.
     * @param userData User-provided data attached to the face. The size limit is 1KB
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> updateFaceAsync(String personGroupId, String personId, String persistedFaceId, String userData) {
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
     * @param userData User-provided data attached to the face. The size limit is 1KB
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> updateFaceWithServiceResponseAsync(String personGroupId, String personId, String persistedFaceId, String userData) {
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
        return service.updateFace(personGroupId, personId, persistedFaceId, body, parameterizedHost)
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
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void addFace(String personGroupId, String personId) {
        addFaceWithServiceResponseAsync(personGroupId, personId).toBlocking().single().body();
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> addFaceAsync(String personGroupId, String personId, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(addFaceWithServiceResponseAsync(personGroupId, personId), serviceCallback);
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> addFaceAsync(String personGroupId, String personId) {
        return addFaceWithServiceResponseAsync(personGroupId, personId).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> addFaceWithServiceResponseAsync(String personGroupId, String personId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        if (personId == null) {
            throw new IllegalArgumentException("Parameter personId is required and cannot be null.");
        }
        final String userData = null;
        final String targetFace = null;
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.addFace(personGroupId, personId, userData, targetFace, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = addFaceDelegate(response);
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
     * @param userData User-specified data about the target face to add for any purpose. The maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added to a person in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void addFace(String personGroupId, String personId, String userData, String targetFace) {
        addFaceWithServiceResponseAsync(personGroupId, personId, userData, targetFace).toBlocking().single().body();
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param userData User-specified data about the target face to add for any purpose. The maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added to a person in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> addFaceAsync(String personGroupId, String personId, String userData, String targetFace, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(addFaceWithServiceResponseAsync(personGroupId, personId, userData, targetFace), serviceCallback);
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param userData User-specified data about the target face to add for any purpose. The maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added to a person in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> addFaceAsync(String personGroupId, String personId, String userData, String targetFace) {
        return addFaceWithServiceResponseAsync(personGroupId, personId, userData, targetFace).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param userData User-specified data about the target face to add for any purpose. The maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added to a person in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> addFaceWithServiceResponseAsync(String personGroupId, String personId, String userData, String targetFace) {
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
        return service.addFace(personGroupId, personId, userData, targetFace, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = addFaceDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Void> addFaceDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
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
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void addFaceFromStream(String personGroupId, String personId) {
        addFaceFromStreamWithServiceResponseAsync(personGroupId, personId).toBlocking().single().body();
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> addFaceFromStreamAsync(String personGroupId, String personId, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(addFaceFromStreamWithServiceResponseAsync(personGroupId, personId), serviceCallback);
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> addFaceFromStreamAsync(String personGroupId, String personId) {
        return addFaceFromStreamWithServiceResponseAsync(personGroupId, personId).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> addFaceFromStreamWithServiceResponseAsync(String personGroupId, String personId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        if (personId == null) {
            throw new IllegalArgumentException("Parameter personId is required and cannot be null.");
        }
        final String userData = null;
        final String targetFace = null;
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.addFaceFromStream(personGroupId, personId, userData, targetFace, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = addFaceFromStreamDelegate(response);
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
     * @param userData User-specified data about the target face to add for any purpose. The maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added to a person, in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void addFaceFromStream(String personGroupId, String personId, String userData, String targetFace) {
        addFaceFromStreamWithServiceResponseAsync(personGroupId, personId, userData, targetFace).toBlocking().single().body();
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param userData User-specified data about the target face to add for any purpose. The maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added to a person, in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> addFaceFromStreamAsync(String personGroupId, String personId, String userData, String targetFace, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(addFaceFromStreamWithServiceResponseAsync(personGroupId, personId, userData, targetFace), serviceCallback);
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param userData User-specified data about the target face to add for any purpose. The maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added to a person, in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> addFaceFromStreamAsync(String personGroupId, String personId, String userData, String targetFace) {
        return addFaceFromStreamWithServiceResponseAsync(personGroupId, personId, userData, targetFace).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param userData User-specified data about the target face to add for any purpose. The maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added to a person, in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> addFaceFromStreamWithServiceResponseAsync(String personGroupId, String personId, String userData, String targetFace) {
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
        return service.addFaceFromStream(personGroupId, personId, userData, targetFace, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = addFaceFromStreamDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Void> addFaceFromStreamDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Void, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

}
