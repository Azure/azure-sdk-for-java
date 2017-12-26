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
import com.microsoft.azure.cognitiveservices.faceapi.FaceAttributeTypes;
import com.microsoft.azure.cognitiveservices.faceapi.FaceMatchingMode;
import com.microsoft.azure.cognitiveservices.faceapi.FindSimilarRequest;
import com.microsoft.azure.cognitiveservices.faceapi.GroupRequest;
import com.microsoft.azure.cognitiveservices.faceapi.IdentifyRequest;
import com.microsoft.azure.cognitiveservices.faceapi.ImageUrl;
import com.microsoft.azure.cognitiveservices.faceapi.VerifyPersonGroupRequest;
import com.microsoft.azure.cognitiveservices.faceapi.VerifyRequest;
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
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.Response;
import rx.functions.Func1;
import rx.Observable;

/**
 * An instance of this class provides access to all the operations defined
 * in Faces.
 */
public class FacesInner {
    /** The Retrofit service to perform REST calls. */
    private FacesService service;
    /** The service client containing this operation class. */
    private FaceAPIImpl client;

    /**
     * Initializes an instance of FacesInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public FacesInner(Retrofit retrofit, FaceAPIImpl client) {
        this.service = retrofit.create(FacesService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for Faces to be
     * used by Retrofit to perform actually REST calls.
     */
    interface FacesService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.Faces findSimilar" })
        @POST("findsimilars")
        Observable<Response<ResponseBody>> findSimilar(@Header("accept-language") String acceptLanguage, @Body FindSimilarRequest body, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.Faces group" })
        @POST("group")
        Observable<Response<ResponseBody>> group(@Header("accept-language") String acceptLanguage, @Body GroupRequest body, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.Faces identify" })
        @POST("identify")
        Observable<Response<ResponseBody>> identify(@Header("accept-language") String acceptLanguage, @Body IdentifyRequest body, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.Faces verify" })
        @POST("verify")
        Observable<Response<ResponseBody>> verify(@Header("accept-language") String acceptLanguage, @Body VerifyRequest body, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.Faces detect" })
        @POST("detect")
        Observable<Response<ResponseBody>> detect(@Query("returnFaceId") Boolean returnFaceId, @Query("returnFaceLandmarks") Boolean returnFaceLandmarks, @Query("returnFaceAttributes") String returnFaceAttributes, @Header("accept-language") String acceptLanguage, @Body ImageUrl imageUrl, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.Faces verifyWithPersonGroup" })
        @POST("verify")
        Observable<Response<ResponseBody>> verifyWithPersonGroup(@Header("accept-language") String acceptLanguage, @Body VerifyPersonGroupRequest body, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/octet-stream", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.Faces detectInStream" })
        @POST("detect")
        Observable<Response<ResponseBody>> detectInStream(@Query("returnFaceId") Boolean returnFaceId, @Query("returnFaceLandmarks") Boolean returnFaceLandmarks, @Query("returnFaceAttributes") String returnFaceAttributes, @Body RequestBody image, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

    }

    /**
     * Given query face's faceId, find the similar-looking faces from a faceId array or a faceListId.
     *
     * @param faceId FaceId of the query face. User needs to call Face - Detect first to get a valid faceId. Note that this faceId is not persisted and will expire 24 hours after the detection call
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;SimilarFaceResultInner&gt; object if successful.
     */
    public List<SimilarFaceResultInner> findSimilar(UUID faceId) {
        return findSimilarWithServiceResponseAsync(faceId).toBlocking().single().body();
    }

    /**
     * Given query face's faceId, find the similar-looking faces from a faceId array or a faceListId.
     *
     * @param faceId FaceId of the query face. User needs to call Face - Detect first to get a valid faceId. Note that this faceId is not persisted and will expire 24 hours after the detection call
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<SimilarFaceResultInner>> findSimilarAsync(UUID faceId, final ServiceCallback<List<SimilarFaceResultInner>> serviceCallback) {
        return ServiceFuture.fromResponse(findSimilarWithServiceResponseAsync(faceId), serviceCallback);
    }

    /**
     * Given query face's faceId, find the similar-looking faces from a faceId array or a faceListId.
     *
     * @param faceId FaceId of the query face. User needs to call Face - Detect first to get a valid faceId. Note that this faceId is not persisted and will expire 24 hours after the detection call
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;SimilarFaceResultInner&gt; object
     */
    public Observable<List<SimilarFaceResultInner>> findSimilarAsync(UUID faceId) {
        return findSimilarWithServiceResponseAsync(faceId).map(new Func1<ServiceResponse<List<SimilarFaceResultInner>>, List<SimilarFaceResultInner>>() {
            @Override
            public List<SimilarFaceResultInner> call(ServiceResponse<List<SimilarFaceResultInner>> response) {
                return response.body();
            }
        });
    }

    /**
     * Given query face's faceId, find the similar-looking faces from a faceId array or a faceListId.
     *
     * @param faceId FaceId of the query face. User needs to call Face - Detect first to get a valid faceId. Note that this faceId is not persisted and will expire 24 hours after the detection call
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;SimilarFaceResultInner&gt; object
     */
    public Observable<ServiceResponse<List<SimilarFaceResultInner>>> findSimilarWithServiceResponseAsync(UUID faceId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (faceId == null) {
            throw new IllegalArgumentException("Parameter faceId is required and cannot be null.");
        }
        final String faceListId = null;
        final List<UUID> faceIds = null;
        final Integer maxNumOfCandidatesReturned = null;
        final FaceMatchingMode mode = null;
        FindSimilarRequest body = new FindSimilarRequest();
        body.withFaceId(faceId);
        body.withFaceListId(null);
        body.withFaceIds(null);
        body.withMaxNumOfCandidatesReturned(null);
        body.withMode(null);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.findSimilar(this.client.acceptLanguage(), body, parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<SimilarFaceResultInner>>>>() {
                @Override
                public Observable<ServiceResponse<List<SimilarFaceResultInner>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<SimilarFaceResultInner>> clientResponse = findSimilarDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Given query face's faceId, find the similar-looking faces from a faceId array or a faceListId.
     *
     * @param faceId FaceId of the query face. User needs to call Face - Detect first to get a valid faceId. Note that this faceId is not persisted and will expire 24 hours after the detection call
     * @param faceListId An existing user-specified unique candidate face list, created in Face List - Create a Face List. Face list contains a set of persistedFaceIds which are persisted and will never expire. Parameter faceListId and faceIds should not be provided at the same time
     * @param faceIds An array of candidate faceIds. All of them are created by Face - Detect and the faceIds will expire 24 hours after the detection call.
     * @param maxNumOfCandidatesReturned The number of top similar faces returned. The valid range is [1, 1000].
     * @param mode Similar face searching mode. It can be "matchPerson" or "matchFace". Possible values include: 'matchPerson', 'matchFace'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;SimilarFaceResultInner&gt; object if successful.
     */
    public List<SimilarFaceResultInner> findSimilar(UUID faceId, String faceListId, List<UUID> faceIds, Integer maxNumOfCandidatesReturned, FaceMatchingMode mode) {
        return findSimilarWithServiceResponseAsync(faceId, faceListId, faceIds, maxNumOfCandidatesReturned, mode).toBlocking().single().body();
    }

    /**
     * Given query face's faceId, find the similar-looking faces from a faceId array or a faceListId.
     *
     * @param faceId FaceId of the query face. User needs to call Face - Detect first to get a valid faceId. Note that this faceId is not persisted and will expire 24 hours after the detection call
     * @param faceListId An existing user-specified unique candidate face list, created in Face List - Create a Face List. Face list contains a set of persistedFaceIds which are persisted and will never expire. Parameter faceListId and faceIds should not be provided at the same time
     * @param faceIds An array of candidate faceIds. All of them are created by Face - Detect and the faceIds will expire 24 hours after the detection call.
     * @param maxNumOfCandidatesReturned The number of top similar faces returned. The valid range is [1, 1000].
     * @param mode Similar face searching mode. It can be "matchPerson" or "matchFace". Possible values include: 'matchPerson', 'matchFace'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<SimilarFaceResultInner>> findSimilarAsync(UUID faceId, String faceListId, List<UUID> faceIds, Integer maxNumOfCandidatesReturned, FaceMatchingMode mode, final ServiceCallback<List<SimilarFaceResultInner>> serviceCallback) {
        return ServiceFuture.fromResponse(findSimilarWithServiceResponseAsync(faceId, faceListId, faceIds, maxNumOfCandidatesReturned, mode), serviceCallback);
    }

    /**
     * Given query face's faceId, find the similar-looking faces from a faceId array or a faceListId.
     *
     * @param faceId FaceId of the query face. User needs to call Face - Detect first to get a valid faceId. Note that this faceId is not persisted and will expire 24 hours after the detection call
     * @param faceListId An existing user-specified unique candidate face list, created in Face List - Create a Face List. Face list contains a set of persistedFaceIds which are persisted and will never expire. Parameter faceListId and faceIds should not be provided at the same time
     * @param faceIds An array of candidate faceIds. All of them are created by Face - Detect and the faceIds will expire 24 hours after the detection call.
     * @param maxNumOfCandidatesReturned The number of top similar faces returned. The valid range is [1, 1000].
     * @param mode Similar face searching mode. It can be "matchPerson" or "matchFace". Possible values include: 'matchPerson', 'matchFace'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;SimilarFaceResultInner&gt; object
     */
    public Observable<List<SimilarFaceResultInner>> findSimilarAsync(UUID faceId, String faceListId, List<UUID> faceIds, Integer maxNumOfCandidatesReturned, FaceMatchingMode mode) {
        return findSimilarWithServiceResponseAsync(faceId, faceListId, faceIds, maxNumOfCandidatesReturned, mode).map(new Func1<ServiceResponse<List<SimilarFaceResultInner>>, List<SimilarFaceResultInner>>() {
            @Override
            public List<SimilarFaceResultInner> call(ServiceResponse<List<SimilarFaceResultInner>> response) {
                return response.body();
            }
        });
    }

    /**
     * Given query face's faceId, find the similar-looking faces from a faceId array or a faceListId.
     *
     * @param faceId FaceId of the query face. User needs to call Face - Detect first to get a valid faceId. Note that this faceId is not persisted and will expire 24 hours after the detection call
     * @param faceListId An existing user-specified unique candidate face list, created in Face List - Create a Face List. Face list contains a set of persistedFaceIds which are persisted and will never expire. Parameter faceListId and faceIds should not be provided at the same time
     * @param faceIds An array of candidate faceIds. All of them are created by Face - Detect and the faceIds will expire 24 hours after the detection call.
     * @param maxNumOfCandidatesReturned The number of top similar faces returned. The valid range is [1, 1000].
     * @param mode Similar face searching mode. It can be "matchPerson" or "matchFace". Possible values include: 'matchPerson', 'matchFace'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;SimilarFaceResultInner&gt; object
     */
    public Observable<ServiceResponse<List<SimilarFaceResultInner>>> findSimilarWithServiceResponseAsync(UUID faceId, String faceListId, List<UUID> faceIds, Integer maxNumOfCandidatesReturned, FaceMatchingMode mode) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (faceId == null) {
            throw new IllegalArgumentException("Parameter faceId is required and cannot be null.");
        }
        Validator.validate(faceIds);
        FindSimilarRequest body = new FindSimilarRequest();
        body.withFaceId(faceId);
        body.withFaceListId(faceListId);
        body.withFaceIds(faceIds);
        body.withMaxNumOfCandidatesReturned(maxNumOfCandidatesReturned);
        body.withMode(mode);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.findSimilar(this.client.acceptLanguage(), body, parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<SimilarFaceResultInner>>>>() {
                @Override
                public Observable<ServiceResponse<List<SimilarFaceResultInner>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<SimilarFaceResultInner>> clientResponse = findSimilarDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<List<SimilarFaceResultInner>> findSimilarDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<List<SimilarFaceResultInner>, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<List<SimilarFaceResultInner>>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Divide candidate faces into groups based on face similarity.
     *
     * @param faceIds Array of candidate faceId created by Face - Detect. The maximum is 1000 faces
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the GroupResponseInner object if successful.
     */
    public GroupResponseInner group(List<UUID> faceIds) {
        return groupWithServiceResponseAsync(faceIds).toBlocking().single().body();
    }

    /**
     * Divide candidate faces into groups based on face similarity.
     *
     * @param faceIds Array of candidate faceId created by Face - Detect. The maximum is 1000 faces
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<GroupResponseInner> groupAsync(List<UUID> faceIds, final ServiceCallback<GroupResponseInner> serviceCallback) {
        return ServiceFuture.fromResponse(groupWithServiceResponseAsync(faceIds), serviceCallback);
    }

    /**
     * Divide candidate faces into groups based on face similarity.
     *
     * @param faceIds Array of candidate faceId created by Face - Detect. The maximum is 1000 faces
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the GroupResponseInner object
     */
    public Observable<GroupResponseInner> groupAsync(List<UUID> faceIds) {
        return groupWithServiceResponseAsync(faceIds).map(new Func1<ServiceResponse<GroupResponseInner>, GroupResponseInner>() {
            @Override
            public GroupResponseInner call(ServiceResponse<GroupResponseInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Divide candidate faces into groups based on face similarity.
     *
     * @param faceIds Array of candidate faceId created by Face - Detect. The maximum is 1000 faces
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the GroupResponseInner object
     */
    public Observable<ServiceResponse<GroupResponseInner>> groupWithServiceResponseAsync(List<UUID> faceIds) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (faceIds == null) {
            throw new IllegalArgumentException("Parameter faceIds is required and cannot be null.");
        }
        Validator.validate(faceIds);
        GroupRequest body = new GroupRequest();
        body.withFaceIds(faceIds);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.group(this.client.acceptLanguage(), body, parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<GroupResponseInner>>>() {
                @Override
                public Observable<ServiceResponse<GroupResponseInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<GroupResponseInner> clientResponse = groupDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<GroupResponseInner> groupDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<GroupResponseInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<GroupResponseInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Identify unknown faces from a person group.
     *
     * @param personGroupId personGroupId of the target person group, created by PersonGroups.Create
     * @param faceIds Array of candidate faceId created by Face - Detect.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;IdentifyResultItemInner&gt; object if successful.
     */
    public List<IdentifyResultItemInner> identify(String personGroupId, List<UUID> faceIds) {
        return identifyWithServiceResponseAsync(personGroupId, faceIds).toBlocking().single().body();
    }

    /**
     * Identify unknown faces from a person group.
     *
     * @param personGroupId personGroupId of the target person group, created by PersonGroups.Create
     * @param faceIds Array of candidate faceId created by Face - Detect.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<IdentifyResultItemInner>> identifyAsync(String personGroupId, List<UUID> faceIds, final ServiceCallback<List<IdentifyResultItemInner>> serviceCallback) {
        return ServiceFuture.fromResponse(identifyWithServiceResponseAsync(personGroupId, faceIds), serviceCallback);
    }

    /**
     * Identify unknown faces from a person group.
     *
     * @param personGroupId personGroupId of the target person group, created by PersonGroups.Create
     * @param faceIds Array of candidate faceId created by Face - Detect.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;IdentifyResultItemInner&gt; object
     */
    public Observable<List<IdentifyResultItemInner>> identifyAsync(String personGroupId, List<UUID> faceIds) {
        return identifyWithServiceResponseAsync(personGroupId, faceIds).map(new Func1<ServiceResponse<List<IdentifyResultItemInner>>, List<IdentifyResultItemInner>>() {
            @Override
            public List<IdentifyResultItemInner> call(ServiceResponse<List<IdentifyResultItemInner>> response) {
                return response.body();
            }
        });
    }

    /**
     * Identify unknown faces from a person group.
     *
     * @param personGroupId personGroupId of the target person group, created by PersonGroups.Create
     * @param faceIds Array of candidate faceId created by Face - Detect.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;IdentifyResultItemInner&gt; object
     */
    public Observable<ServiceResponse<List<IdentifyResultItemInner>>> identifyWithServiceResponseAsync(String personGroupId, List<UUID> faceIds) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        if (faceIds == null) {
            throw new IllegalArgumentException("Parameter faceIds is required and cannot be null.");
        }
        Validator.validate(faceIds);
        final Integer maxNumOfCandidatesReturned = null;
        final Double confidenceThreshold = null;
        IdentifyRequest body = new IdentifyRequest();
        body.withPersonGroupId(personGroupId);
        body.withFaceIds(faceIds);
        body.withMaxNumOfCandidatesReturned(null);
        body.withConfidenceThreshold(null);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.identify(this.client.acceptLanguage(), body, parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<IdentifyResultItemInner>>>>() {
                @Override
                public Observable<ServiceResponse<List<IdentifyResultItemInner>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<IdentifyResultItemInner>> clientResponse = identifyDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Identify unknown faces from a person group.
     *
     * @param personGroupId personGroupId of the target person group, created by PersonGroups.Create
     * @param faceIds Array of candidate faceId created by Face - Detect.
     * @param maxNumOfCandidatesReturned The number of top similar faces returned.
     * @param confidenceThreshold the Double value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;IdentifyResultItemInner&gt; object if successful.
     */
    public List<IdentifyResultItemInner> identify(String personGroupId, List<UUID> faceIds, Integer maxNumOfCandidatesReturned, Double confidenceThreshold) {
        return identifyWithServiceResponseAsync(personGroupId, faceIds, maxNumOfCandidatesReturned, confidenceThreshold).toBlocking().single().body();
    }

    /**
     * Identify unknown faces from a person group.
     *
     * @param personGroupId personGroupId of the target person group, created by PersonGroups.Create
     * @param faceIds Array of candidate faceId created by Face - Detect.
     * @param maxNumOfCandidatesReturned The number of top similar faces returned.
     * @param confidenceThreshold the Double value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<IdentifyResultItemInner>> identifyAsync(String personGroupId, List<UUID> faceIds, Integer maxNumOfCandidatesReturned, Double confidenceThreshold, final ServiceCallback<List<IdentifyResultItemInner>> serviceCallback) {
        return ServiceFuture.fromResponse(identifyWithServiceResponseAsync(personGroupId, faceIds, maxNumOfCandidatesReturned, confidenceThreshold), serviceCallback);
    }

    /**
     * Identify unknown faces from a person group.
     *
     * @param personGroupId personGroupId of the target person group, created by PersonGroups.Create
     * @param faceIds Array of candidate faceId created by Face - Detect.
     * @param maxNumOfCandidatesReturned The number of top similar faces returned.
     * @param confidenceThreshold the Double value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;IdentifyResultItemInner&gt; object
     */
    public Observable<List<IdentifyResultItemInner>> identifyAsync(String personGroupId, List<UUID> faceIds, Integer maxNumOfCandidatesReturned, Double confidenceThreshold) {
        return identifyWithServiceResponseAsync(personGroupId, faceIds, maxNumOfCandidatesReturned, confidenceThreshold).map(new Func1<ServiceResponse<List<IdentifyResultItemInner>>, List<IdentifyResultItemInner>>() {
            @Override
            public List<IdentifyResultItemInner> call(ServiceResponse<List<IdentifyResultItemInner>> response) {
                return response.body();
            }
        });
    }

    /**
     * Identify unknown faces from a person group.
     *
     * @param personGroupId personGroupId of the target person group, created by PersonGroups.Create
     * @param faceIds Array of candidate faceId created by Face - Detect.
     * @param maxNumOfCandidatesReturned The number of top similar faces returned.
     * @param confidenceThreshold the Double value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;IdentifyResultItemInner&gt; object
     */
    public Observable<ServiceResponse<List<IdentifyResultItemInner>>> identifyWithServiceResponseAsync(String personGroupId, List<UUID> faceIds, Integer maxNumOfCandidatesReturned, Double confidenceThreshold) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        if (faceIds == null) {
            throw new IllegalArgumentException("Parameter faceIds is required and cannot be null.");
        }
        Validator.validate(faceIds);
        IdentifyRequest body = new IdentifyRequest();
        body.withPersonGroupId(personGroupId);
        body.withFaceIds(faceIds);
        body.withMaxNumOfCandidatesReturned(maxNumOfCandidatesReturned);
        body.withConfidenceThreshold(confidenceThreshold);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.identify(this.client.acceptLanguage(), body, parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<IdentifyResultItemInner>>>>() {
                @Override
                public Observable<ServiceResponse<List<IdentifyResultItemInner>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<IdentifyResultItemInner>> clientResponse = identifyDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<List<IdentifyResultItemInner>> identifyDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<List<IdentifyResultItemInner>, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<List<IdentifyResultItemInner>>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Verify whether two faces belong to a same person or whether one face belongs to a person.
     *
     * @param faceId1 faceId of the first face, comes from Face - Detect
     * @param faceId2 faceId of the second face, comes from Face - Detect
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the VerifyResultInner object if successful.
     */
    public VerifyResultInner verify(UUID faceId1, UUID faceId2) {
        return verifyWithServiceResponseAsync(faceId1, faceId2).toBlocking().single().body();
    }

    /**
     * Verify whether two faces belong to a same person or whether one face belongs to a person.
     *
     * @param faceId1 faceId of the first face, comes from Face - Detect
     * @param faceId2 faceId of the second face, comes from Face - Detect
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<VerifyResultInner> verifyAsync(UUID faceId1, UUID faceId2, final ServiceCallback<VerifyResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(verifyWithServiceResponseAsync(faceId1, faceId2), serviceCallback);
    }

    /**
     * Verify whether two faces belong to a same person or whether one face belongs to a person.
     *
     * @param faceId1 faceId of the first face, comes from Face - Detect
     * @param faceId2 faceId of the second face, comes from Face - Detect
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the VerifyResultInner object
     */
    public Observable<VerifyResultInner> verifyAsync(UUID faceId1, UUID faceId2) {
        return verifyWithServiceResponseAsync(faceId1, faceId2).map(new Func1<ServiceResponse<VerifyResultInner>, VerifyResultInner>() {
            @Override
            public VerifyResultInner call(ServiceResponse<VerifyResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Verify whether two faces belong to a same person or whether one face belongs to a person.
     *
     * @param faceId1 faceId of the first face, comes from Face - Detect
     * @param faceId2 faceId of the second face, comes from Face - Detect
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the VerifyResultInner object
     */
    public Observable<ServiceResponse<VerifyResultInner>> verifyWithServiceResponseAsync(UUID faceId1, UUID faceId2) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (faceId1 == null) {
            throw new IllegalArgumentException("Parameter faceId1 is required and cannot be null.");
        }
        if (faceId2 == null) {
            throw new IllegalArgumentException("Parameter faceId2 is required and cannot be null.");
        }
        VerifyRequest body = new VerifyRequest();
        body.withFaceId1(faceId1);
        body.withFaceId2(faceId2);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.verify(this.client.acceptLanguage(), body, parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<VerifyResultInner>>>() {
                @Override
                public Observable<ServiceResponse<VerifyResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<VerifyResultInner> clientResponse = verifyDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<VerifyResultInner> verifyDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<VerifyResultInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<VerifyResultInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;DetectedFaceInner&gt; object if successful.
     */
    public List<DetectedFaceInner> detect(String url) {
        return detectWithServiceResponseAsync(url).toBlocking().single().body();
    }

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param url the String value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<DetectedFaceInner>> detectAsync(String url, final ServiceCallback<List<DetectedFaceInner>> serviceCallback) {
        return ServiceFuture.fromResponse(detectWithServiceResponseAsync(url), serviceCallback);
    }

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;DetectedFaceInner&gt; object
     */
    public Observable<List<DetectedFaceInner>> detectAsync(String url) {
        return detectWithServiceResponseAsync(url).map(new Func1<ServiceResponse<List<DetectedFaceInner>>, List<DetectedFaceInner>>() {
            @Override
            public List<DetectedFaceInner> call(ServiceResponse<List<DetectedFaceInner>> response) {
                return response.body();
            }
        });
    }

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;DetectedFaceInner&gt; object
     */
    public Observable<ServiceResponse<List<DetectedFaceInner>>> detectWithServiceResponseAsync(String url) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (url == null) {
            throw new IllegalArgumentException("Parameter url is required and cannot be null.");
        }
        final Boolean returnFaceId = null;
        final Boolean returnFaceLandmarks = null;
        final List<FaceAttributeTypes> returnFaceAttributes = null;
        ImageUrl imageUrl = new ImageUrl();
        imageUrl.withUrl(url);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        String returnFaceAttributesConverted = this.client.serializerAdapter().serializeList(returnFaceAttributes, CollectionFormat.CSV);
        return service.detect(returnFaceId, returnFaceLandmarks, returnFaceAttributesConverted, this.client.acceptLanguage(), imageUrl, parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<DetectedFaceInner>>>>() {
                @Override
                public Observable<ServiceResponse<List<DetectedFaceInner>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<DetectedFaceInner>> clientResponse = detectDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param url the String value
     * @param returnFaceId A value indicating whether the operation should return faceIds of detected faces.
     * @param returnFaceLandmarks A value indicating whether the operation should return landmarks of the detected faces.
     * @param returnFaceAttributes Analyze and return the one or more specified face attributes in the comma-separated string like "returnFaceAttributes=age,gender". Supported face attributes include age, gender, headPose, smile, facialHair, glasses and emotion. Note that each face attribute analysis has additional computational and time cost.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;DetectedFaceInner&gt; object if successful.
     */
    public List<DetectedFaceInner> detect(String url, Boolean returnFaceId, Boolean returnFaceLandmarks, List<FaceAttributeTypes> returnFaceAttributes) {
        return detectWithServiceResponseAsync(url, returnFaceId, returnFaceLandmarks, returnFaceAttributes).toBlocking().single().body();
    }

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param url the String value
     * @param returnFaceId A value indicating whether the operation should return faceIds of detected faces.
     * @param returnFaceLandmarks A value indicating whether the operation should return landmarks of the detected faces.
     * @param returnFaceAttributes Analyze and return the one or more specified face attributes in the comma-separated string like "returnFaceAttributes=age,gender". Supported face attributes include age, gender, headPose, smile, facialHair, glasses and emotion. Note that each face attribute analysis has additional computational and time cost.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<DetectedFaceInner>> detectAsync(String url, Boolean returnFaceId, Boolean returnFaceLandmarks, List<FaceAttributeTypes> returnFaceAttributes, final ServiceCallback<List<DetectedFaceInner>> serviceCallback) {
        return ServiceFuture.fromResponse(detectWithServiceResponseAsync(url, returnFaceId, returnFaceLandmarks, returnFaceAttributes), serviceCallback);
    }

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param url the String value
     * @param returnFaceId A value indicating whether the operation should return faceIds of detected faces.
     * @param returnFaceLandmarks A value indicating whether the operation should return landmarks of the detected faces.
     * @param returnFaceAttributes Analyze and return the one or more specified face attributes in the comma-separated string like "returnFaceAttributes=age,gender". Supported face attributes include age, gender, headPose, smile, facialHair, glasses and emotion. Note that each face attribute analysis has additional computational and time cost.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;DetectedFaceInner&gt; object
     */
    public Observable<List<DetectedFaceInner>> detectAsync(String url, Boolean returnFaceId, Boolean returnFaceLandmarks, List<FaceAttributeTypes> returnFaceAttributes) {
        return detectWithServiceResponseAsync(url, returnFaceId, returnFaceLandmarks, returnFaceAttributes).map(new Func1<ServiceResponse<List<DetectedFaceInner>>, List<DetectedFaceInner>>() {
            @Override
            public List<DetectedFaceInner> call(ServiceResponse<List<DetectedFaceInner>> response) {
                return response.body();
            }
        });
    }

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param url the String value
     * @param returnFaceId A value indicating whether the operation should return faceIds of detected faces.
     * @param returnFaceLandmarks A value indicating whether the operation should return landmarks of the detected faces.
     * @param returnFaceAttributes Analyze and return the one or more specified face attributes in the comma-separated string like "returnFaceAttributes=age,gender". Supported face attributes include age, gender, headPose, smile, facialHair, glasses and emotion. Note that each face attribute analysis has additional computational and time cost.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;DetectedFaceInner&gt; object
     */
    public Observable<ServiceResponse<List<DetectedFaceInner>>> detectWithServiceResponseAsync(String url, Boolean returnFaceId, Boolean returnFaceLandmarks, List<FaceAttributeTypes> returnFaceAttributes) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (url == null) {
            throw new IllegalArgumentException("Parameter url is required and cannot be null.");
        }
        Validator.validate(returnFaceAttributes);
        ImageUrl imageUrl = new ImageUrl();
        imageUrl.withUrl(url);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        String returnFaceAttributesConverted = this.client.serializerAdapter().serializeList(returnFaceAttributes, CollectionFormat.CSV);
        return service.detect(returnFaceId, returnFaceLandmarks, returnFaceAttributesConverted, this.client.acceptLanguage(), imageUrl, parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<DetectedFaceInner>>>>() {
                @Override
                public Observable<ServiceResponse<List<DetectedFaceInner>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<DetectedFaceInner>> clientResponse = detectDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<List<DetectedFaceInner>> detectDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<List<DetectedFaceInner>, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<List<DetectedFaceInner>>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Verify whether two faces belong to a same person. Compares a face Id with a Person Id.
     *
     * @param faceId faceId the face, comes from Face - Detect
     * @param personId Specify a certain person in a person group. personId is created in Persons.Create.
     * @param personGroupId Using existing personGroupId and personId for fast loading a specified person. personGroupId is created in Person Groups.Create.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the VerifyResultInner object if successful.
     */
    public VerifyResultInner verifyWithPersonGroup(UUID faceId, UUID personId, String personGroupId) {
        return verifyWithPersonGroupWithServiceResponseAsync(faceId, personId, personGroupId).toBlocking().single().body();
    }

    /**
     * Verify whether two faces belong to a same person. Compares a face Id with a Person Id.
     *
     * @param faceId faceId the face, comes from Face - Detect
     * @param personId Specify a certain person in a person group. personId is created in Persons.Create.
     * @param personGroupId Using existing personGroupId and personId for fast loading a specified person. personGroupId is created in Person Groups.Create.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<VerifyResultInner> verifyWithPersonGroupAsync(UUID faceId, UUID personId, String personGroupId, final ServiceCallback<VerifyResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(verifyWithPersonGroupWithServiceResponseAsync(faceId, personId, personGroupId), serviceCallback);
    }

    /**
     * Verify whether two faces belong to a same person. Compares a face Id with a Person Id.
     *
     * @param faceId faceId the face, comes from Face - Detect
     * @param personId Specify a certain person in a person group. personId is created in Persons.Create.
     * @param personGroupId Using existing personGroupId and personId for fast loading a specified person. personGroupId is created in Person Groups.Create.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the VerifyResultInner object
     */
    public Observable<VerifyResultInner> verifyWithPersonGroupAsync(UUID faceId, UUID personId, String personGroupId) {
        return verifyWithPersonGroupWithServiceResponseAsync(faceId, personId, personGroupId).map(new Func1<ServiceResponse<VerifyResultInner>, VerifyResultInner>() {
            @Override
            public VerifyResultInner call(ServiceResponse<VerifyResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Verify whether two faces belong to a same person. Compares a face Id with a Person Id.
     *
     * @param faceId faceId the face, comes from Face - Detect
     * @param personId Specify a certain person in a person group. personId is created in Persons.Create.
     * @param personGroupId Using existing personGroupId and personId for fast loading a specified person. personGroupId is created in Person Groups.Create.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the VerifyResultInner object
     */
    public Observable<ServiceResponse<VerifyResultInner>> verifyWithPersonGroupWithServiceResponseAsync(UUID faceId, UUID personId, String personGroupId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (faceId == null) {
            throw new IllegalArgumentException("Parameter faceId is required and cannot be null.");
        }
        if (personId == null) {
            throw new IllegalArgumentException("Parameter personId is required and cannot be null.");
        }
        if (personGroupId == null) {
            throw new IllegalArgumentException("Parameter personGroupId is required and cannot be null.");
        }
        VerifyPersonGroupRequest body = new VerifyPersonGroupRequest();
        body.withFaceId(faceId);
        body.withPersonId(personId);
        body.withPersonGroupId(personGroupId);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.verifyWithPersonGroup(this.client.acceptLanguage(), body, parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<VerifyResultInner>>>() {
                @Override
                public Observable<ServiceResponse<VerifyResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<VerifyResultInner> clientResponse = verifyWithPersonGroupDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<VerifyResultInner> verifyWithPersonGroupDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<VerifyResultInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<VerifyResultInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;DetectedFaceInner&gt; object if successful.
     */
    public List<DetectedFaceInner> detectInStream(byte[] image) {
        return detectInStreamWithServiceResponseAsync(image).toBlocking().single().body();
    }

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param image An image stream.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<DetectedFaceInner>> detectInStreamAsync(byte[] image, final ServiceCallback<List<DetectedFaceInner>> serviceCallback) {
        return ServiceFuture.fromResponse(detectInStreamWithServiceResponseAsync(image), serviceCallback);
    }

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;DetectedFaceInner&gt; object
     */
    public Observable<List<DetectedFaceInner>> detectInStreamAsync(byte[] image) {
        return detectInStreamWithServiceResponseAsync(image).map(new Func1<ServiceResponse<List<DetectedFaceInner>>, List<DetectedFaceInner>>() {
            @Override
            public List<DetectedFaceInner> call(ServiceResponse<List<DetectedFaceInner>> response) {
                return response.body();
            }
        });
    }

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;DetectedFaceInner&gt; object
     */
    public Observable<ServiceResponse<List<DetectedFaceInner>>> detectInStreamWithServiceResponseAsync(byte[] image) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Parameter image is required and cannot be null.");
        }
        final Boolean returnFaceId = null;
        final Boolean returnFaceLandmarks = null;
        final List<FaceAttributeTypes> returnFaceAttributes = null;
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        String returnFaceAttributesConverted = this.client.serializerAdapter().serializeList(returnFaceAttributes, CollectionFormat.CSV);
        RequestBody imageConverted = RequestBody.create(MediaType.parse("application/octet-stream"), image);
        return service.detectInStream(returnFaceId, returnFaceLandmarks, returnFaceAttributesConverted, imageConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<DetectedFaceInner>>>>() {
                @Override
                public Observable<ServiceResponse<List<DetectedFaceInner>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<DetectedFaceInner>> clientResponse = detectInStreamDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param image An image stream.
     * @param returnFaceId A value indicating whether the operation should return faceIds of detected faces.
     * @param returnFaceLandmarks A value indicating whether the operation should return landmarks of the detected faces.
     * @param returnFaceAttributes Analyze and return the one or more specified face attributes in the comma-separated string like "returnFaceAttributes=age,gender". Supported face attributes include age, gender, headPose, smile, facialHair, glasses and emotion. Note that each face attribute analysis has additional computational and time cost.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;DetectedFaceInner&gt; object if successful.
     */
    public List<DetectedFaceInner> detectInStream(byte[] image, Boolean returnFaceId, Boolean returnFaceLandmarks, List<FaceAttributeTypes> returnFaceAttributes) {
        return detectInStreamWithServiceResponseAsync(image, returnFaceId, returnFaceLandmarks, returnFaceAttributes).toBlocking().single().body();
    }

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param image An image stream.
     * @param returnFaceId A value indicating whether the operation should return faceIds of detected faces.
     * @param returnFaceLandmarks A value indicating whether the operation should return landmarks of the detected faces.
     * @param returnFaceAttributes Analyze and return the one or more specified face attributes in the comma-separated string like "returnFaceAttributes=age,gender". Supported face attributes include age, gender, headPose, smile, facialHair, glasses and emotion. Note that each face attribute analysis has additional computational and time cost.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<DetectedFaceInner>> detectInStreamAsync(byte[] image, Boolean returnFaceId, Boolean returnFaceLandmarks, List<FaceAttributeTypes> returnFaceAttributes, final ServiceCallback<List<DetectedFaceInner>> serviceCallback) {
        return ServiceFuture.fromResponse(detectInStreamWithServiceResponseAsync(image, returnFaceId, returnFaceLandmarks, returnFaceAttributes), serviceCallback);
    }

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param image An image stream.
     * @param returnFaceId A value indicating whether the operation should return faceIds of detected faces.
     * @param returnFaceLandmarks A value indicating whether the operation should return landmarks of the detected faces.
     * @param returnFaceAttributes Analyze and return the one or more specified face attributes in the comma-separated string like "returnFaceAttributes=age,gender". Supported face attributes include age, gender, headPose, smile, facialHair, glasses and emotion. Note that each face attribute analysis has additional computational and time cost.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;DetectedFaceInner&gt; object
     */
    public Observable<List<DetectedFaceInner>> detectInStreamAsync(byte[] image, Boolean returnFaceId, Boolean returnFaceLandmarks, List<FaceAttributeTypes> returnFaceAttributes) {
        return detectInStreamWithServiceResponseAsync(image, returnFaceId, returnFaceLandmarks, returnFaceAttributes).map(new Func1<ServiceResponse<List<DetectedFaceInner>>, List<DetectedFaceInner>>() {
            @Override
            public List<DetectedFaceInner> call(ServiceResponse<List<DetectedFaceInner>> response) {
                return response.body();
            }
        });
    }

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param image An image stream.
     * @param returnFaceId A value indicating whether the operation should return faceIds of detected faces.
     * @param returnFaceLandmarks A value indicating whether the operation should return landmarks of the detected faces.
     * @param returnFaceAttributes Analyze and return the one or more specified face attributes in the comma-separated string like "returnFaceAttributes=age,gender". Supported face attributes include age, gender, headPose, smile, facialHair, glasses and emotion. Note that each face attribute analysis has additional computational and time cost.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;DetectedFaceInner&gt; object
     */
    public Observable<ServiceResponse<List<DetectedFaceInner>>> detectInStreamWithServiceResponseAsync(byte[] image, Boolean returnFaceId, Boolean returnFaceLandmarks, List<FaceAttributeTypes> returnFaceAttributes) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Parameter image is required and cannot be null.");
        }
        Validator.validate(returnFaceAttributes);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        String returnFaceAttributesConverted = this.client.serializerAdapter().serializeList(returnFaceAttributes, CollectionFormat.CSV);
        RequestBody imageConverted = RequestBody.create(MediaType.parse("application/octet-stream"), image);
        return service.detectInStream(returnFaceId, returnFaceLandmarks, returnFaceAttributesConverted, imageConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<DetectedFaceInner>>>>() {
                @Override
                public Observable<ServiceResponse<List<DetectedFaceInner>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<DetectedFaceInner>> clientResponse = detectInStreamDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<List<DetectedFaceInner>> detectInStreamDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<List<DetectedFaceInner>, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<List<DetectedFaceInner>>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

}
