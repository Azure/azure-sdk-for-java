/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi.implementation;

import retrofit2.Retrofit;
import com.microsoft.azure.cognitiveservices.faceapi.Faces;
import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.cognitiveservices.faceapi.models.APIErrorException;
import com.microsoft.azure.cognitiveservices.faceapi.models.DetectedFace;
import com.microsoft.azure.cognitiveservices.faceapi.models.FindSimilarRequest;
import com.microsoft.azure.cognitiveservices.faceapi.models.GroupRequest;
import com.microsoft.azure.cognitiveservices.faceapi.models.GroupResponse;
import com.microsoft.azure.cognitiveservices.faceapi.models.IdentifyRequest;
import com.microsoft.azure.cognitiveservices.faceapi.models.IdentifyResultItem;
import com.microsoft.azure.cognitiveservices.faceapi.models.ImageUrl;
import com.microsoft.azure.cognitiveservices.faceapi.models.SimilarFaceResult;
import com.microsoft.azure.cognitiveservices.faceapi.models.VerifyRequest;
import com.microsoft.azure.cognitiveservices.faceapi.models.VerifyResult;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.Validator;
import java.io.IOException;
import java.util.List;
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
public class FacesImpl implements Faces {
    /** The Retrofit service to perform REST calls. */
    private FacesService service;
    /** The service client containing this operation class. */
    private FaceAPIImpl client;

    /**
     * Initializes an instance of Faces.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public FacesImpl(Retrofit retrofit, FaceAPIImpl client) {
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
        Observable<Response<ResponseBody>> findSimilar(@Body FindSimilarRequest body, @Header("x-ms-parameterized-host") String parameterizedHost);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.Faces group" })
        @POST("group")
        Observable<Response<ResponseBody>> group(@Body GroupRequest body, @Header("x-ms-parameterized-host") String parameterizedHost);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.Faces identify" })
        @POST("identify")
        Observable<Response<ResponseBody>> identify(@Body IdentifyRequest body, @Header("x-ms-parameterized-host") String parameterizedHost);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.Faces verify" })
        @POST("verify")
        Observable<Response<ResponseBody>> verify(@Body VerifyRequest body, @Header("x-ms-parameterized-host") String parameterizedHost);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.Faces detect" })
        @POST("detect")
        Observable<Response<ResponseBody>> detect(@Query("returnFaceId") Boolean returnFaceId, @Query("returnFaceLandmarks") Boolean returnFaceLandmarks, @Query("returnFaceAttributes") String returnFaceAttributes, @Body ImageUrl imageUrl, @Header("x-ms-parameterized-host") String parameterizedHost);

        @Headers({ "Content-Type: application/octet-stream", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.Faces detectInStream" })
        @POST("detect")
        Observable<Response<ResponseBody>> detectInStream(@Query("returnFaceId") Boolean returnFaceId, @Query("returnFaceLandmarks") Boolean returnFaceLandmarks, @Query("returnFaceAttributes") String returnFaceAttributes, @Body RequestBody image, @Header("x-ms-parameterized-host") String parameterizedHost);

    }

    /**
     * Given query face's faceId, find the similar-looking faces from a faceId array or a faceListId.
     *
     * @param faceId FaceId of the query face. User needs to call Face - Detect first to get a valid faceId. Note that this faceId is not persisted and will expire 24 hours after the detection call
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;SimilarFaceResult&gt; object if successful.
     */
    public List<SimilarFaceResult> findSimilar(String faceId) {
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
    public ServiceFuture<List<SimilarFaceResult>> findSimilarAsync(String faceId, final ServiceCallback<List<SimilarFaceResult>> serviceCallback) {
        return ServiceFuture.fromResponse(findSimilarWithServiceResponseAsync(faceId), serviceCallback);
    }

    /**
     * Given query face's faceId, find the similar-looking faces from a faceId array or a faceListId.
     *
     * @param faceId FaceId of the query face. User needs to call Face - Detect first to get a valid faceId. Note that this faceId is not persisted and will expire 24 hours after the detection call
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;SimilarFaceResult&gt; object
     */
    public Observable<List<SimilarFaceResult>> findSimilarAsync(String faceId) {
        return findSimilarWithServiceResponseAsync(faceId).map(new Func1<ServiceResponse<List<SimilarFaceResult>>, List<SimilarFaceResult>>() {
            @Override
            public List<SimilarFaceResult> call(ServiceResponse<List<SimilarFaceResult>> response) {
                return response.body();
            }
        });
    }

    /**
     * Given query face's faceId, find the similar-looking faces from a faceId array or a faceListId.
     *
     * @param faceId FaceId of the query face. User needs to call Face - Detect first to get a valid faceId. Note that this faceId is not persisted and will expire 24 hours after the detection call
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;SimilarFaceResult&gt; object
     */
    public Observable<ServiceResponse<List<SimilarFaceResult>>> findSimilarWithServiceResponseAsync(String faceId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (faceId == null) {
            throw new IllegalArgumentException("Parameter faceId is required and cannot be null.");
        }
        final String faceListId = null;
        final List<String> faceIds = null;
        final Integer maxNumOfCandidatesReturned = null;
        final String mode = null;
        FindSimilarRequest body = new FindSimilarRequest();
        body.withFaceId(faceId);
        body.withFaceListId(null);
        body.withFaceIds(null);
        body.withMaxNumOfCandidatesReturned(null);
        body.withMode(null);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.findSimilar(body, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<SimilarFaceResult>>>>() {
                @Override
                public Observable<ServiceResponse<List<SimilarFaceResult>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<SimilarFaceResult>> clientResponse = findSimilarDelegate(response);
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
     * @return the List&lt;SimilarFaceResult&gt; object if successful.
     */
    public List<SimilarFaceResult> findSimilar(String faceId, String faceListId, List<String> faceIds, Integer maxNumOfCandidatesReturned, String mode) {
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
    public ServiceFuture<List<SimilarFaceResult>> findSimilarAsync(String faceId, String faceListId, List<String> faceIds, Integer maxNumOfCandidatesReturned, String mode, final ServiceCallback<List<SimilarFaceResult>> serviceCallback) {
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
     * @return the observable to the List&lt;SimilarFaceResult&gt; object
     */
    public Observable<List<SimilarFaceResult>> findSimilarAsync(String faceId, String faceListId, List<String> faceIds, Integer maxNumOfCandidatesReturned, String mode) {
        return findSimilarWithServiceResponseAsync(faceId, faceListId, faceIds, maxNumOfCandidatesReturned, mode).map(new Func1<ServiceResponse<List<SimilarFaceResult>>, List<SimilarFaceResult>>() {
            @Override
            public List<SimilarFaceResult> call(ServiceResponse<List<SimilarFaceResult>> response) {
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
     * @return the observable to the List&lt;SimilarFaceResult&gt; object
     */
    public Observable<ServiceResponse<List<SimilarFaceResult>>> findSimilarWithServiceResponseAsync(String faceId, String faceListId, List<String> faceIds, Integer maxNumOfCandidatesReturned, String mode) {
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
        return service.findSimilar(body, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<SimilarFaceResult>>>>() {
                @Override
                public Observable<ServiceResponse<List<SimilarFaceResult>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<SimilarFaceResult>> clientResponse = findSimilarDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<List<SimilarFaceResult>> findSimilarDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<List<SimilarFaceResult>, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<List<SimilarFaceResult>>() { }.getType())
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
     * @return the GroupResponse object if successful.
     */
    public GroupResponse group(List<String> faceIds) {
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
    public ServiceFuture<GroupResponse> groupAsync(List<String> faceIds, final ServiceCallback<GroupResponse> serviceCallback) {
        return ServiceFuture.fromResponse(groupWithServiceResponseAsync(faceIds), serviceCallback);
    }

    /**
     * Divide candidate faces into groups based on face similarity.
     *
     * @param faceIds Array of candidate faceId created by Face - Detect. The maximum is 1000 faces
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the GroupResponse object
     */
    public Observable<GroupResponse> groupAsync(List<String> faceIds) {
        return groupWithServiceResponseAsync(faceIds).map(new Func1<ServiceResponse<GroupResponse>, GroupResponse>() {
            @Override
            public GroupResponse call(ServiceResponse<GroupResponse> response) {
                return response.body();
            }
        });
    }

    /**
     * Divide candidate faces into groups based on face similarity.
     *
     * @param faceIds Array of candidate faceId created by Face - Detect. The maximum is 1000 faces
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the GroupResponse object
     */
    public Observable<ServiceResponse<GroupResponse>> groupWithServiceResponseAsync(List<String> faceIds) {
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
        return service.group(body, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<GroupResponse>>>() {
                @Override
                public Observable<ServiceResponse<GroupResponse>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<GroupResponse> clientResponse = groupDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<GroupResponse> groupDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<GroupResponse, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<GroupResponse>() { }.getType())
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
     * @return the List&lt;IdentifyResultItem&gt; object if successful.
     */
    public List<IdentifyResultItem> identify(String personGroupId, List<String> faceIds) {
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
    public ServiceFuture<List<IdentifyResultItem>> identifyAsync(String personGroupId, List<String> faceIds, final ServiceCallback<List<IdentifyResultItem>> serviceCallback) {
        return ServiceFuture.fromResponse(identifyWithServiceResponseAsync(personGroupId, faceIds), serviceCallback);
    }

    /**
     * Identify unknown faces from a person group.
     *
     * @param personGroupId personGroupId of the target person group, created by PersonGroups.Create
     * @param faceIds Array of candidate faceId created by Face - Detect.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;IdentifyResultItem&gt; object
     */
    public Observable<List<IdentifyResultItem>> identifyAsync(String personGroupId, List<String> faceIds) {
        return identifyWithServiceResponseAsync(personGroupId, faceIds).map(new Func1<ServiceResponse<List<IdentifyResultItem>>, List<IdentifyResultItem>>() {
            @Override
            public List<IdentifyResultItem> call(ServiceResponse<List<IdentifyResultItem>> response) {
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
     * @return the observable to the List&lt;IdentifyResultItem&gt; object
     */
    public Observable<ServiceResponse<List<IdentifyResultItem>>> identifyWithServiceResponseAsync(String personGroupId, List<String> faceIds) {
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
        return service.identify(body, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<IdentifyResultItem>>>>() {
                @Override
                public Observable<ServiceResponse<List<IdentifyResultItem>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<IdentifyResultItem>> clientResponse = identifyDelegate(response);
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
     * @param confidenceThreshold Confidence threshold of identification, used to judge whether one face belong to one person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;IdentifyResultItem&gt; object if successful.
     */
    public List<IdentifyResultItem> identify(String personGroupId, List<String> faceIds, Integer maxNumOfCandidatesReturned, Double confidenceThreshold) {
        return identifyWithServiceResponseAsync(personGroupId, faceIds, maxNumOfCandidatesReturned, confidenceThreshold).toBlocking().single().body();
    }

    /**
     * Identify unknown faces from a person group.
     *
     * @param personGroupId personGroupId of the target person group, created by PersonGroups.Create
     * @param faceIds Array of candidate faceId created by Face - Detect.
     * @param maxNumOfCandidatesReturned The number of top similar faces returned.
     * @param confidenceThreshold Confidence threshold of identification, used to judge whether one face belong to one person.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<IdentifyResultItem>> identifyAsync(String personGroupId, List<String> faceIds, Integer maxNumOfCandidatesReturned, Double confidenceThreshold, final ServiceCallback<List<IdentifyResultItem>> serviceCallback) {
        return ServiceFuture.fromResponse(identifyWithServiceResponseAsync(personGroupId, faceIds, maxNumOfCandidatesReturned, confidenceThreshold), serviceCallback);
    }

    /**
     * Identify unknown faces from a person group.
     *
     * @param personGroupId personGroupId of the target person group, created by PersonGroups.Create
     * @param faceIds Array of candidate faceId created by Face - Detect.
     * @param maxNumOfCandidatesReturned The number of top similar faces returned.
     * @param confidenceThreshold Confidence threshold of identification, used to judge whether one face belong to one person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;IdentifyResultItem&gt; object
     */
    public Observable<List<IdentifyResultItem>> identifyAsync(String personGroupId, List<String> faceIds, Integer maxNumOfCandidatesReturned, Double confidenceThreshold) {
        return identifyWithServiceResponseAsync(personGroupId, faceIds, maxNumOfCandidatesReturned, confidenceThreshold).map(new Func1<ServiceResponse<List<IdentifyResultItem>>, List<IdentifyResultItem>>() {
            @Override
            public List<IdentifyResultItem> call(ServiceResponse<List<IdentifyResultItem>> response) {
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
     * @param confidenceThreshold Confidence threshold of identification, used to judge whether one face belong to one person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;IdentifyResultItem&gt; object
     */
    public Observable<ServiceResponse<List<IdentifyResultItem>>> identifyWithServiceResponseAsync(String personGroupId, List<String> faceIds, Integer maxNumOfCandidatesReturned, Double confidenceThreshold) {
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
        return service.identify(body, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<IdentifyResultItem>>>>() {
                @Override
                public Observable<ServiceResponse<List<IdentifyResultItem>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<IdentifyResultItem>> clientResponse = identifyDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<List<IdentifyResultItem>> identifyDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<List<IdentifyResultItem>, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<List<IdentifyResultItem>>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Verify whether two faces belong to a same person or whether one face belongs to a person.
     *
     * @param faceId faceId the face, comes from Face - Detect
     * @param personId Specify a certain person in a person group. personId is created in Persons.Create.
     * @param personGroupId Using existing personGroupId and personId for fast loading a specified person. personGroupId is created in Person Groups.Create.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the VerifyResult object if successful.
     */
    public VerifyResult verify(String faceId, String personId, String personGroupId) {
        return verifyWithServiceResponseAsync(faceId, personId, personGroupId).toBlocking().single().body();
    }

    /**
     * Verify whether two faces belong to a same person or whether one face belongs to a person.
     *
     * @param faceId faceId the face, comes from Face - Detect
     * @param personId Specify a certain person in a person group. personId is created in Persons.Create.
     * @param personGroupId Using existing personGroupId and personId for fast loading a specified person. personGroupId is created in Person Groups.Create.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<VerifyResult> verifyAsync(String faceId, String personId, String personGroupId, final ServiceCallback<VerifyResult> serviceCallback) {
        return ServiceFuture.fromResponse(verifyWithServiceResponseAsync(faceId, personId, personGroupId), serviceCallback);
    }

    /**
     * Verify whether two faces belong to a same person or whether one face belongs to a person.
     *
     * @param faceId faceId the face, comes from Face - Detect
     * @param personId Specify a certain person in a person group. personId is created in Persons.Create.
     * @param personGroupId Using existing personGroupId and personId for fast loading a specified person. personGroupId is created in Person Groups.Create.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the VerifyResult object
     */
    public Observable<VerifyResult> verifyAsync(String faceId, String personId, String personGroupId) {
        return verifyWithServiceResponseAsync(faceId, personId, personGroupId).map(new Func1<ServiceResponse<VerifyResult>, VerifyResult>() {
            @Override
            public VerifyResult call(ServiceResponse<VerifyResult> response) {
                return response.body();
            }
        });
    }

    /**
     * Verify whether two faces belong to a same person or whether one face belongs to a person.
     *
     * @param faceId faceId the face, comes from Face - Detect
     * @param personId Specify a certain person in a person group. personId is created in Persons.Create.
     * @param personGroupId Using existing personGroupId and personId for fast loading a specified person. personGroupId is created in Person Groups.Create.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the VerifyResult object
     */
    public Observable<ServiceResponse<VerifyResult>> verifyWithServiceResponseAsync(String faceId, String personId, String personGroupId) {
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
        VerifyRequest body = new VerifyRequest();
        body.withFaceId(faceId);
        body.withPersonId(personId);
        body.withPersonGroupId(personGroupId);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.verify(body, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<VerifyResult>>>() {
                @Override
                public Observable<ServiceResponse<VerifyResult>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<VerifyResult> clientResponse = verifyDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<VerifyResult> verifyDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<VerifyResult, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<VerifyResult>() { }.getType())
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
     * @return the List&lt;DetectedFace&gt; object if successful.
     */
    public List<DetectedFace> detect(String url) {
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
    public ServiceFuture<List<DetectedFace>> detectAsync(String url, final ServiceCallback<List<DetectedFace>> serviceCallback) {
        return ServiceFuture.fromResponse(detectWithServiceResponseAsync(url), serviceCallback);
    }

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;DetectedFace&gt; object
     */
    public Observable<List<DetectedFace>> detectAsync(String url) {
        return detectWithServiceResponseAsync(url).map(new Func1<ServiceResponse<List<DetectedFace>>, List<DetectedFace>>() {
            @Override
            public List<DetectedFace> call(ServiceResponse<List<DetectedFace>> response) {
                return response.body();
            }
        });
    }

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;DetectedFace&gt; object
     */
    public Observable<ServiceResponse<List<DetectedFace>>> detectWithServiceResponseAsync(String url) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (url == null) {
            throw new IllegalArgumentException("Parameter url is required and cannot be null.");
        }
        final Boolean returnFaceId = null;
        final Boolean returnFaceLandmarks = null;
        final String returnFaceAttributes = null;
        ImageUrl imageUrl = new ImageUrl();
        imageUrl.withUrl(url);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.detect(returnFaceId, returnFaceLandmarks, returnFaceAttributes, imageUrl, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<DetectedFace>>>>() {
                @Override
                public Observable<ServiceResponse<List<DetectedFace>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<DetectedFace>> clientResponse = detectDelegate(response);
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
     * @return the List&lt;DetectedFace&gt; object if successful.
     */
    public List<DetectedFace> detect(String url, Boolean returnFaceId, Boolean returnFaceLandmarks, String returnFaceAttributes) {
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
    public ServiceFuture<List<DetectedFace>> detectAsync(String url, Boolean returnFaceId, Boolean returnFaceLandmarks, String returnFaceAttributes, final ServiceCallback<List<DetectedFace>> serviceCallback) {
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
     * @return the observable to the List&lt;DetectedFace&gt; object
     */
    public Observable<List<DetectedFace>> detectAsync(String url, Boolean returnFaceId, Boolean returnFaceLandmarks, String returnFaceAttributes) {
        return detectWithServiceResponseAsync(url, returnFaceId, returnFaceLandmarks, returnFaceAttributes).map(new Func1<ServiceResponse<List<DetectedFace>>, List<DetectedFace>>() {
            @Override
            public List<DetectedFace> call(ServiceResponse<List<DetectedFace>> response) {
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
     * @return the observable to the List&lt;DetectedFace&gt; object
     */
    public Observable<ServiceResponse<List<DetectedFace>>> detectWithServiceResponseAsync(String url, Boolean returnFaceId, Boolean returnFaceLandmarks, String returnFaceAttributes) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (url == null) {
            throw new IllegalArgumentException("Parameter url is required and cannot be null.");
        }
        ImageUrl imageUrl = new ImageUrl();
        imageUrl.withUrl(url);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.detect(returnFaceId, returnFaceLandmarks, returnFaceAttributes, imageUrl, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<DetectedFace>>>>() {
                @Override
                public Observable<ServiceResponse<List<DetectedFace>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<DetectedFace>> clientResponse = detectDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<List<DetectedFace>> detectDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<List<DetectedFace>, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<List<DetectedFace>>() { }.getType())
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
     * @return the List&lt;DetectedFace&gt; object if successful.
     */
    public List<DetectedFace> detectInStream(byte[] image) {
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
    public ServiceFuture<List<DetectedFace>> detectInStreamAsync(byte[] image, final ServiceCallback<List<DetectedFace>> serviceCallback) {
        return ServiceFuture.fromResponse(detectInStreamWithServiceResponseAsync(image), serviceCallback);
    }

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;DetectedFace&gt; object
     */
    public Observable<List<DetectedFace>> detectInStreamAsync(byte[] image) {
        return detectInStreamWithServiceResponseAsync(image).map(new Func1<ServiceResponse<List<DetectedFace>>, List<DetectedFace>>() {
            @Override
            public List<DetectedFace> call(ServiceResponse<List<DetectedFace>> response) {
                return response.body();
            }
        });
    }

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;DetectedFace&gt; object
     */
    public Observable<ServiceResponse<List<DetectedFace>>> detectInStreamWithServiceResponseAsync(byte[] image) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Parameter image is required and cannot be null.");
        }
        final Boolean returnFaceId = null;
        final Boolean returnFaceLandmarks = null;
        final String returnFaceAttributes = null;
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        RequestBody imageConverted = RequestBody.create(MediaType.parse("application/octet-stream"), image);
        return service.detectInStream(returnFaceId, returnFaceLandmarks, returnFaceAttributes, imageConverted, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<DetectedFace>>>>() {
                @Override
                public Observable<ServiceResponse<List<DetectedFace>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<DetectedFace>> clientResponse = detectInStreamDelegate(response);
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
     * @return the List&lt;DetectedFace&gt; object if successful.
     */
    public List<DetectedFace> detectInStream(byte[] image, Boolean returnFaceId, Boolean returnFaceLandmarks, String returnFaceAttributes) {
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
    public ServiceFuture<List<DetectedFace>> detectInStreamAsync(byte[] image, Boolean returnFaceId, Boolean returnFaceLandmarks, String returnFaceAttributes, final ServiceCallback<List<DetectedFace>> serviceCallback) {
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
     * @return the observable to the List&lt;DetectedFace&gt; object
     */
    public Observable<List<DetectedFace>> detectInStreamAsync(byte[] image, Boolean returnFaceId, Boolean returnFaceLandmarks, String returnFaceAttributes) {
        return detectInStreamWithServiceResponseAsync(image, returnFaceId, returnFaceLandmarks, returnFaceAttributes).map(new Func1<ServiceResponse<List<DetectedFace>>, List<DetectedFace>>() {
            @Override
            public List<DetectedFace> call(ServiceResponse<List<DetectedFace>> response) {
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
     * @return the observable to the List&lt;DetectedFace&gt; object
     */
    public Observable<ServiceResponse<List<DetectedFace>>> detectInStreamWithServiceResponseAsync(byte[] image, Boolean returnFaceId, Boolean returnFaceLandmarks, String returnFaceAttributes) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Parameter image is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        RequestBody imageConverted = RequestBody.create(MediaType.parse("application/octet-stream"), image);
        return service.detectInStream(returnFaceId, returnFaceLandmarks, returnFaceAttributes, imageConverted, parameterizedHost)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<DetectedFace>>>>() {
                @Override
                public Observable<ServiceResponse<List<DetectedFace>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<DetectedFace>> clientResponse = detectInStreamDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<List<DetectedFace>> detectInStreamDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<List<DetectedFace>, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<List<DetectedFace>>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

}
