/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi.implementation;

import retrofit2.Retrofit;
import com.microsoft.azure.cognitiveservices.faceapi.PersonGroups;
import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.cognitiveservices.faceapi.models.APIErrorException;
import com.microsoft.azure.cognitiveservices.faceapi.models.CreatePersonGroupRequest;
import com.microsoft.azure.cognitiveservices.faceapi.models.PersonGroupResult;
import com.microsoft.azure.cognitiveservices.faceapi.models.TrainingStatus;
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
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.Response;
import rx.functions.Func1;
import rx.Observable;

/**
 * An instance of this class provides access to all the operations defined
 * in PersonGroups.
 */
public class PersonGroupsImpl implements PersonGroups {
    /** The Retrofit service to perform REST calls. */
    private PersonGroupsService service;
    /** The service client containing this operation class. */
    private FaceAPIImpl client;

    /**
     * Initializes an instance of PersonGroups.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public PersonGroupsImpl(Retrofit retrofit, FaceAPIImpl client) {
        this.service = retrofit.create(PersonGroupsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for PersonGroups to be
     * used by Retrofit to perform actually REST calls.
     */
    interface PersonGroupsService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.PersonGroups create" })
        @PUT("persongroups/{personGroupId}")
        Observable<Response<ResponseBody>> create(@Path("personGroupId") String personGroupId, @Body CreatePersonGroupRequest body, @Header("x-ms-parameterized-host") String parameterizedHost);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.PersonGroups delete" })
        @HTTP(path = "persongroups/{personGroupId}", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> delete(@Path("personGroupId") String personGroupId, @Header("x-ms-parameterized-host") String parameterizedHost);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.PersonGroups get" })
        @GET("persongroups/{personGroupId}")
        Observable<Response<ResponseBody>> get(@Path("personGroupId") String personGroupId, @Header("x-ms-parameterized-host") String parameterizedHost);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.PersonGroups update" })
        @PATCH("persongroups/{personGroupId}")
        Observable<Response<ResponseBody>> update(@Path("personGroupId") String personGroupId, @Body CreatePersonGroupRequest body, @Header("x-ms-parameterized-host") String parameterizedHost);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.PersonGroups getTrainingStatus" })
        @GET("persongroups/{personGroupId}/training")
        Observable<Response<ResponseBody>> getTrainingStatus(@Path("personGroupId") String personGroupId, @Header("x-ms-parameterized-host") String parameterizedHost);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.PersonGroups list" })
        @GET("persongroups")
        Observable<Response<ResponseBody>> list(@Query("start") String start, @Query("top") Integer top, @Header("x-ms-parameterized-host") String parameterizedHost);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.PersonGroups train" })
        @POST("persongroups/{personGroupId}/train")
        Observable<Response<ResponseBody>> train(@Path("personGroupId") String personGroupId, @Header("x-ms-parameterized-host") String parameterizedHost);

    }

    /**
     * Create a new person group with specified personGroupId, name and user-provided userData.
     *
     * @param personGroupId User-provided personGroupId as a string.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void create(String personGroupId) {
        createWithServiceResponseAsync(personGroupId).toBlocking().single().body();
    }

    /**
     * Create a new person group with specified personGroupId, name and user-provided userData.
     *
     * @param personGroupId User-provided personGroupId as a string.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> createAsync(String personGroupId, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(createWithServiceResponseAsync(personGroupId), serviceCallback);
    }

    /**
     * Create a new person group with specified personGroupId, name and user-provided userData.
     *
     * @param personGroupId User-provided personGroupId as a string.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> createAsync(String personGroupId) {
        return createWithServiceResponseAsync(personGroupId).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Create a new person group with specified personGroupId, name and user-provided userData.
     *
     * @param personGroupId User-provided personGroupId as a string.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> createWithServiceResponseAsync(String personGroupId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        final String name = null;
        final String userData = null;
        CreatePersonGroupRequest body = new CreatePersonGroupRequest();
        body.withName(null);
        body.withUserData(null);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.create(personGroupId, body, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = createDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Create a new person group with specified personGroupId, name and user-provided userData.
     *
     * @param personGroupId User-provided personGroupId as a string.
     * @param name Name of the face list, maximum length is 128.
     * @param userData Optional user defined data for the face list. Length should not exceed 16KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void create(String personGroupId, String name, String userData) {
        createWithServiceResponseAsync(personGroupId, name, userData).toBlocking().single().body();
    }

    /**
     * Create a new person group with specified personGroupId, name and user-provided userData.
     *
     * @param personGroupId User-provided personGroupId as a string.
     * @param name Name of the face list, maximum length is 128.
     * @param userData Optional user defined data for the face list. Length should not exceed 16KB.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> createAsync(String personGroupId, String name, String userData, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(createWithServiceResponseAsync(personGroupId, name, userData), serviceCallback);
    }

    /**
     * Create a new person group with specified personGroupId, name and user-provided userData.
     *
     * @param personGroupId User-provided personGroupId as a string.
     * @param name Name of the face list, maximum length is 128.
     * @param userData Optional user defined data for the face list. Length should not exceed 16KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> createAsync(String personGroupId, String name, String userData) {
        return createWithServiceResponseAsync(personGroupId, name, userData).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Create a new person group with specified personGroupId, name and user-provided userData.
     *
     * @param personGroupId User-provided personGroupId as a string.
     * @param name Name of the face list, maximum length is 128.
     * @param userData Optional user defined data for the face list. Length should not exceed 16KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> createWithServiceResponseAsync(String personGroupId, String name, String userData) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        CreatePersonGroupRequest body = new CreatePersonGroupRequest();
        body.withName(name);
        body.withUserData(userData);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.create(personGroupId, body, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = createDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Void> createDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Void, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Delete an existing person group. Persisted face images of all people in the person group will also be deleted.
     *
     * @param personGroupId The personGroupId of the person group to be deleted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void delete(String personGroupId) {
        deleteWithServiceResponseAsync(personGroupId).toBlocking().single().body();
    }

    /**
     * Delete an existing person group. Persisted face images of all people in the person group will also be deleted.
     *
     * @param personGroupId The personGroupId of the person group to be deleted.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> deleteAsync(String personGroupId, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(deleteWithServiceResponseAsync(personGroupId), serviceCallback);
    }

    /**
     * Delete an existing person group. Persisted face images of all people in the person group will also be deleted.
     *
     * @param personGroupId The personGroupId of the person group to be deleted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> deleteAsync(String personGroupId) {
        return deleteWithServiceResponseAsync(personGroupId).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Delete an existing person group. Persisted face images of all people in the person group will also be deleted.
     *
     * @param personGroupId The personGroupId of the person group to be deleted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> deleteWithServiceResponseAsync(String personGroupId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.delete(personGroupId, parameterizedHost)
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
     * Retrieve the information of a person group, including its name and userData.
     *
     * @param personGroupId personGroupId of the target person group.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the PersonGroupResult object if successful.
     */
    public PersonGroupResult get(String personGroupId) {
        return getWithServiceResponseAsync(personGroupId).toBlocking().single().body();
    }

    /**
     * Retrieve the information of a person group, including its name and userData.
     *
     * @param personGroupId personGroupId of the target person group.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<PersonGroupResult> getAsync(String personGroupId, final ServiceCallback<PersonGroupResult> serviceCallback) {
        return ServiceFuture.fromResponse(getWithServiceResponseAsync(personGroupId), serviceCallback);
    }

    /**
     * Retrieve the information of a person group, including its name and userData.
     *
     * @param personGroupId personGroupId of the target person group.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PersonGroupResult object
     */
    public Observable<PersonGroupResult> getAsync(String personGroupId) {
        return getWithServiceResponseAsync(personGroupId).map(new Func1<ServiceResponse<PersonGroupResult>, PersonGroupResult>() {
            @Override
            public PersonGroupResult call(ServiceResponse<PersonGroupResult> response) {
                return response.body();
            }
        });
    }

    /**
     * Retrieve the information of a person group, including its name and userData.
     *
     * @param personGroupId personGroupId of the target person group.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PersonGroupResult object
     */
    public Observable<ServiceResponse<PersonGroupResult>> getWithServiceResponseAsync(String personGroupId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.get(personGroupId, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<PersonGroupResult>>>() {
                @Override
                public Observable<ServiceResponse<PersonGroupResult>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<PersonGroupResult> clientResponse = getDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<PersonGroupResult> getDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<PersonGroupResult, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<PersonGroupResult>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Update an existing person group's display name and userData. The properties which does not appear in request body will not be updated.
     *
     * @param personGroupId personGroupId of the person group to be updated.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void update(String personGroupId) {
        updateWithServiceResponseAsync(personGroupId).toBlocking().single().body();
    }

    /**
     * Update an existing person group's display name and userData. The properties which does not appear in request body will not be updated.
     *
     * @param personGroupId personGroupId of the person group to be updated.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> updateAsync(String personGroupId, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(updateWithServiceResponseAsync(personGroupId), serviceCallback);
    }

    /**
     * Update an existing person group's display name and userData. The properties which does not appear in request body will not be updated.
     *
     * @param personGroupId personGroupId of the person group to be updated.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> updateAsync(String personGroupId) {
        return updateWithServiceResponseAsync(personGroupId).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Update an existing person group's display name and userData. The properties which does not appear in request body will not be updated.
     *
     * @param personGroupId personGroupId of the person group to be updated.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> updateWithServiceResponseAsync(String personGroupId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        final String name = null;
        final String userData = null;
        CreatePersonGroupRequest body = new CreatePersonGroupRequest();
        body.withName(null);
        body.withUserData(null);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.update(personGroupId, body, parameterizedHost)
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
     * Update an existing person group's display name and userData. The properties which does not appear in request body will not be updated.
     *
     * @param personGroupId personGroupId of the person group to be updated.
     * @param name Name of the face list, maximum length is 128.
     * @param userData Optional user defined data for the face list. Length should not exceed 16KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void update(String personGroupId, String name, String userData) {
        updateWithServiceResponseAsync(personGroupId, name, userData).toBlocking().single().body();
    }

    /**
     * Update an existing person group's display name and userData. The properties which does not appear in request body will not be updated.
     *
     * @param personGroupId personGroupId of the person group to be updated.
     * @param name Name of the face list, maximum length is 128.
     * @param userData Optional user defined data for the face list. Length should not exceed 16KB.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> updateAsync(String personGroupId, String name, String userData, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(updateWithServiceResponseAsync(personGroupId, name, userData), serviceCallback);
    }

    /**
     * Update an existing person group's display name and userData. The properties which does not appear in request body will not be updated.
     *
     * @param personGroupId personGroupId of the person group to be updated.
     * @param name Name of the face list, maximum length is 128.
     * @param userData Optional user defined data for the face list. Length should not exceed 16KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> updateAsync(String personGroupId, String name, String userData) {
        return updateWithServiceResponseAsync(personGroupId, name, userData).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Update an existing person group's display name and userData. The properties which does not appear in request body will not be updated.
     *
     * @param personGroupId personGroupId of the person group to be updated.
     * @param name Name of the face list, maximum length is 128.
     * @param userData Optional user defined data for the face list. Length should not exceed 16KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> updateWithServiceResponseAsync(String personGroupId, String name, String userData) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        CreatePersonGroupRequest body = new CreatePersonGroupRequest();
        body.withName(name);
        body.withUserData(userData);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.update(personGroupId, body, parameterizedHost)
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
     * Retrieve the training status of a person group (completed or ongoing).
     *
     * @param personGroupId personGroupId of target person group.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the TrainingStatus object if successful.
     */
    public TrainingStatus getTrainingStatus(String personGroupId) {
        return getTrainingStatusWithServiceResponseAsync(personGroupId).toBlocking().single().body();
    }

    /**
     * Retrieve the training status of a person group (completed or ongoing).
     *
     * @param personGroupId personGroupId of target person group.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<TrainingStatus> getTrainingStatusAsync(String personGroupId, final ServiceCallback<TrainingStatus> serviceCallback) {
        return ServiceFuture.fromResponse(getTrainingStatusWithServiceResponseAsync(personGroupId), serviceCallback);
    }

    /**
     * Retrieve the training status of a person group (completed or ongoing).
     *
     * @param personGroupId personGroupId of target person group.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TrainingStatus object
     */
    public Observable<TrainingStatus> getTrainingStatusAsync(String personGroupId) {
        return getTrainingStatusWithServiceResponseAsync(personGroupId).map(new Func1<ServiceResponse<TrainingStatus>, TrainingStatus>() {
            @Override
            public TrainingStatus call(ServiceResponse<TrainingStatus> response) {
                return response.body();
            }
        });
    }

    /**
     * Retrieve the training status of a person group (completed or ongoing).
     *
     * @param personGroupId personGroupId of target person group.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TrainingStatus object
     */
    public Observable<ServiceResponse<TrainingStatus>> getTrainingStatusWithServiceResponseAsync(String personGroupId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.getTrainingStatus(personGroupId, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<TrainingStatus>>>() {
                @Override
                public Observable<ServiceResponse<TrainingStatus>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<TrainingStatus> clientResponse = getTrainingStatusDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<TrainingStatus> getTrainingStatusDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<TrainingStatus, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<TrainingStatus>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * List person groups and their information.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;PersonGroupResult&gt; object if successful.
     */
    public List<PersonGroupResult> list() {
        return listWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * List person groups and their information.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<PersonGroupResult>> listAsync(final ServiceCallback<List<PersonGroupResult>> serviceCallback) {
        return ServiceFuture.fromResponse(listWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * List person groups and their information.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;PersonGroupResult&gt; object
     */
    public Observable<List<PersonGroupResult>> listAsync() {
        return listWithServiceResponseAsync().map(new Func1<ServiceResponse<List<PersonGroupResult>>, List<PersonGroupResult>>() {
            @Override
            public List<PersonGroupResult> call(ServiceResponse<List<PersonGroupResult>> response) {
                return response.body();
            }
        });
    }

    /**
     * List person groups and their information.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;PersonGroupResult&gt; object
     */
    public Observable<ServiceResponse<List<PersonGroupResult>>> listWithServiceResponseAsync() {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        final String start = null;
        final Integer top = null;
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.list(start, top, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<PersonGroupResult>>>>() {
                @Override
                public Observable<ServiceResponse<List<PersonGroupResult>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<PersonGroupResult>> clientResponse = listDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * List person groups and their information.
     *
     * @param start List person groups from the least personGroupId greater than the "start".
     * @param top The number of person groups to list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;PersonGroupResult&gt; object if successful.
     */
    public List<PersonGroupResult> list(String start, Integer top) {
        return listWithServiceResponseAsync(start, top).toBlocking().single().body();
    }

    /**
     * List person groups and their information.
     *
     * @param start List person groups from the least personGroupId greater than the "start".
     * @param top The number of person groups to list.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<PersonGroupResult>> listAsync(String start, Integer top, final ServiceCallback<List<PersonGroupResult>> serviceCallback) {
        return ServiceFuture.fromResponse(listWithServiceResponseAsync(start, top), serviceCallback);
    }

    /**
     * List person groups and their information.
     *
     * @param start List person groups from the least personGroupId greater than the "start".
     * @param top The number of person groups to list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;PersonGroupResult&gt; object
     */
    public Observable<List<PersonGroupResult>> listAsync(String start, Integer top) {
        return listWithServiceResponseAsync(start, top).map(new Func1<ServiceResponse<List<PersonGroupResult>>, List<PersonGroupResult>>() {
            @Override
            public List<PersonGroupResult> call(ServiceResponse<List<PersonGroupResult>> response) {
                return response.body();
            }
        });
    }

    /**
     * List person groups and their information.
     *
     * @param start List person groups from the least personGroupId greater than the "start".
     * @param top The number of person groups to list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;PersonGroupResult&gt; object
     */
    public Observable<ServiceResponse<List<PersonGroupResult>>> listWithServiceResponseAsync(String start, Integer top) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.list(start, top, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<PersonGroupResult>>>>() {
                @Override
                public Observable<ServiceResponse<List<PersonGroupResult>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<PersonGroupResult>> clientResponse = listDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<List<PersonGroupResult>> listDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<List<PersonGroupResult>, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<List<PersonGroupResult>>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Queue a person group training task, the training task may not be started immediately.
     *
     * @param personGroupId Target person group to be trained.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void train(String personGroupId) {
        trainWithServiceResponseAsync(personGroupId).toBlocking().single().body();
    }

    /**
     * Queue a person group training task, the training task may not be started immediately.
     *
     * @param personGroupId Target person group to be trained.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> trainAsync(String personGroupId, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(trainWithServiceResponseAsync(personGroupId), serviceCallback);
    }

    /**
     * Queue a person group training task, the training task may not be started immediately.
     *
     * @param personGroupId Target person group to be trained.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> trainAsync(String personGroupId) {
        return trainWithServiceResponseAsync(personGroupId).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Queue a person group training task, the training task may not be started immediately.
     *
     * @param personGroupId Target person group to be trained.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> trainWithServiceResponseAsync(String personGroupId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.train(personGroupId, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = trainDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Void> trainDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Void, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

}
