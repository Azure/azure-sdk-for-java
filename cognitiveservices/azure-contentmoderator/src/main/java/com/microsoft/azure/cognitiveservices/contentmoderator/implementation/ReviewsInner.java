/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;

import retrofit2.Retrofit;
import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.cognitiveservices.contentmoderator.APIErrorException;
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
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.Response;
import rx.functions.Func1;
import rx.Observable;

/**
 * An instance of this class provides access to all the operations defined
 * in Reviews.
 */
public class ReviewsInner {
    /** The Retrofit service to perform REST calls. */
    private ReviewsService service;
    /** The service client containing this operation class. */
    private ContentModeratorClientImpl client;

    /**
     * Initializes an instance of ReviewsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public ReviewsInner(Retrofit retrofit, ContentModeratorClientImpl client) {
        this.service = retrofit.create(ReviewsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for Reviews to be
     * used by Retrofit to perform actually REST calls.
     */
    interface ReviewsService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.Reviews getReview" })
        @GET("contentmoderator/review/v1.0/teams/{teamName}/reviews/{reviewId}")
        Observable<Response<ResponseBody>> getReview(@Path("teamName") String teamName, @Path("reviewId") String reviewId, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.Reviews getJobDetails" })
        @GET("contentmoderator/review/v1.0/teams/{teamName}/jobs/{JobId}")
        Observable<Response<ResponseBody>> getJobDetails(@Path("teamName") String teamName, @Path("JobId") String jobId, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.Reviews createReviews" })
        @POST("contentmoderator/review/v1.0/teams/{teamName}/reviews")
        Observable<Response<ResponseBody>> createReviews(@Path("teamName") String teamName, @Header("UrlContentType") String urlContentType, @Query("subTeam") String subTeam, @Body List<CreateReviewBodyItemInner> createReviewBody, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.Reviews createJob" })
        @POST("contentmoderator/review/v1.0/teams/{teamName}/jobs")
        Observable<Response<ResponseBody>> createJob(@Path("teamName") String teamName, @Query("ContentType") String contentType, @Query("ContentId") String contentId, @Query("WorkflowName") String workflowName, @Query("CallBackEndpoint") String callBackEndpoint, @Header("Content-Type") String jobContentType, @Body ContentInner content, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.Reviews addVideoFrame" })
        @POST("contentmoderator/review/v1.0/teams/{teamName}/reviews/{reviewId}/frames")
        Observable<Response<ResponseBody>> addVideoFrame(@Path("teamName") String teamName, @Path("reviewId") String reviewId, @Query("timescale") Integer timescale, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.Reviews getVideoFrames" })
        @GET("contentmoderator/review/v1.0/teams/{teamName}/reviews/{reviewId}/frames")
        Observable<Response<ResponseBody>> getVideoFrames(@Path("teamName") String teamName, @Path("reviewId") String reviewId, @Query("startSeed") Integer startSeed, @Query("noOfRecords") Integer noOfRecords, @Query("filter") String filter, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.Reviews publishVideoReview" })
        @POST("contentmoderator/review/v1.0/teams/{teamName}/reviews/{reviewId}/publish")
        Observable<Response<ResponseBody>> publishVideoReview(@Path("teamName") String teamName, @Path("reviewId") String reviewId, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.Reviews addVideoTranscriptModerationResult" })
        @PUT("contentmoderator/review/v1.0/teams/{teamName}/reviews/{reviewId}/transcriptmoderationresult")
        Observable<Response<ResponseBody>> addVideoTranscriptModerationResult(@Path("teamName") String teamName, @Path("reviewId") String reviewId, @Header("Content-Type") String contentType, @Body List<TranscriptModerationBodyItemInner> transcriptModerationBody, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: text/plain", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.Reviews addVideoTranscript" })
        @PUT("contentmoderator/review/v1.0/teams/{teamName}/reviews/{reviewId}/transcript")
        Observable<Response<ResponseBody>> addVideoTranscript(@Path("teamName") String teamName, @Path("reviewId") String reviewId, @Header("Content-Type") String contentType, @Body RequestBody vTTfile, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.Reviews createVideoReviews" })
        @POST("contentmoderator/review/v1.0/teams/{teamName}/reviews")
        Observable<Response<ResponseBody>> createVideoReviews(@Path("teamName") String teamName, @Header("Content-Type") String contentType, @Query("subTeam") String subTeam, @Body List<CreateVideoReviewsBodyItemInner> createVideoReviewsBody, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.Reviews addVideoFrameUrl" })
        @POST("contentmoderator/review/v1.0/teams/{teamName}/reviews/{reviewId}/frames")
        Observable<Response<ResponseBody>> addVideoFrameUrl(@Path("teamName") String teamName, @Path("reviewId") String reviewId, @Header("Content-Type") String contentType, @Query("timescale") Integer timescale, @Body List<VideoFrameBodyItemInner> videoFrameBody, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Multipart
        @POST("contentmoderator/review/v1.0/teams/{teamName}/reviews/{reviewId}/frames")
        Observable<Response<ResponseBody>> addVideoFrameStream(@Path("teamName") String teamName, @Path("reviewId") String reviewId, @Header("Content-Type") String contentType, @Query("timescale") Integer timescale, @Part("frameImageZip") RequestBody frameImageZip, @Part("frameMetadata") String frameMetadata, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

    }

    /**
     * Returns review details for the review Id passed.
     *
     * @param teamName Your Team Name.
     * @param reviewId Id of the review.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ReviewInner object if successful.
     */
    public ReviewInner getReview(String teamName, String reviewId) {
        return getReviewWithServiceResponseAsync(teamName, reviewId).toBlocking().single().body();
    }

    /**
     * Returns review details for the review Id passed.
     *
     * @param teamName Your Team Name.
     * @param reviewId Id of the review.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ReviewInner> getReviewAsync(String teamName, String reviewId, final ServiceCallback<ReviewInner> serviceCallback) {
        return ServiceFuture.fromResponse(getReviewWithServiceResponseAsync(teamName, reviewId), serviceCallback);
    }

    /**
     * Returns review details for the review Id passed.
     *
     * @param teamName Your Team Name.
     * @param reviewId Id of the review.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ReviewInner object
     */
    public Observable<ReviewInner> getReviewAsync(String teamName, String reviewId) {
        return getReviewWithServiceResponseAsync(teamName, reviewId).map(new Func1<ServiceResponse<ReviewInner>, ReviewInner>() {
            @Override
            public ReviewInner call(ServiceResponse<ReviewInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns review details for the review Id passed.
     *
     * @param teamName Your Team Name.
     * @param reviewId Id of the review.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ReviewInner object
     */
    public Observable<ServiceResponse<ReviewInner>> getReviewWithServiceResponseAsync(String teamName, String reviewId) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (reviewId == null) {
            throw new IllegalArgumentException("Parameter reviewId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.getReview(teamName, reviewId, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ReviewInner>>>() {
                @Override
                public Observable<ServiceResponse<ReviewInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ReviewInner> clientResponse = getReviewDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<ReviewInner> getReviewDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<ReviewInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ReviewInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Get the Job Details for a Job Id.
     *
     * @param teamName Your Team Name.
     * @param jobId Id of the job.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the JobInner object if successful.
     */
    public JobInner getJobDetails(String teamName, String jobId) {
        return getJobDetailsWithServiceResponseAsync(teamName, jobId).toBlocking().single().body();
    }

    /**
     * Get the Job Details for a Job Id.
     *
     * @param teamName Your Team Name.
     * @param jobId Id of the job.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<JobInner> getJobDetailsAsync(String teamName, String jobId, final ServiceCallback<JobInner> serviceCallback) {
        return ServiceFuture.fromResponse(getJobDetailsWithServiceResponseAsync(teamName, jobId), serviceCallback);
    }

    /**
     * Get the Job Details for a Job Id.
     *
     * @param teamName Your Team Name.
     * @param jobId Id of the job.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the JobInner object
     */
    public Observable<JobInner> getJobDetailsAsync(String teamName, String jobId) {
        return getJobDetailsWithServiceResponseAsync(teamName, jobId).map(new Func1<ServiceResponse<JobInner>, JobInner>() {
            @Override
            public JobInner call(ServiceResponse<JobInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Get the Job Details for a Job Id.
     *
     * @param teamName Your Team Name.
     * @param jobId Id of the job.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the JobInner object
     */
    public Observable<ServiceResponse<JobInner>> getJobDetailsWithServiceResponseAsync(String teamName, String jobId) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.getJobDetails(teamName, jobId, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<JobInner>>>() {
                @Override
                public Observable<ServiceResponse<JobInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<JobInner> clientResponse = getJobDetailsDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<JobInner> getJobDetailsDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<JobInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<JobInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param urlContentType The content type.
     * @param createReviewBody Body for create reviews API
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;String&gt; object if successful.
     */
    public List<String> createReviews(String teamName, String urlContentType, List<CreateReviewBodyItemInner> createReviewBody) {
        return createReviewsWithServiceResponseAsync(teamName, urlContentType, createReviewBody).toBlocking().single().body();
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param urlContentType The content type.
     * @param createReviewBody Body for create reviews API
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<String>> createReviewsAsync(String teamName, String urlContentType, List<CreateReviewBodyItemInner> createReviewBody, final ServiceCallback<List<String>> serviceCallback) {
        return ServiceFuture.fromResponse(createReviewsWithServiceResponseAsync(teamName, urlContentType, createReviewBody), serviceCallback);
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param urlContentType The content type.
     * @param createReviewBody Body for create reviews API
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;String&gt; object
     */
    public Observable<List<String>> createReviewsAsync(String teamName, String urlContentType, List<CreateReviewBodyItemInner> createReviewBody) {
        return createReviewsWithServiceResponseAsync(teamName, urlContentType, createReviewBody).map(new Func1<ServiceResponse<List<String>>, List<String>>() {
            @Override
            public List<String> call(ServiceResponse<List<String>> response) {
                return response.body();
            }
        });
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param urlContentType The content type.
     * @param createReviewBody Body for create reviews API
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;String&gt; object
     */
    public Observable<ServiceResponse<List<String>>> createReviewsWithServiceResponseAsync(String teamName, String urlContentType, List<CreateReviewBodyItemInner> createReviewBody) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (urlContentType == null) {
            throw new IllegalArgumentException("Parameter urlContentType is required and cannot be null.");
        }
        if (createReviewBody == null) {
            throw new IllegalArgumentException("Parameter createReviewBody is required and cannot be null.");
        }
        Validator.validate(createReviewBody);
        final String subTeam = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.createReviews(teamName, urlContentType, subTeam, createReviewBody, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<String>>>>() {
                @Override
                public Observable<ServiceResponse<List<String>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<String>> clientResponse = createReviewsDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param urlContentType The content type.
     * @param createReviewBody Body for create reviews API
     * @param subTeam SubTeam of your team, you want to assign the created review to.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;String&gt; object if successful.
     */
    public List<String> createReviews(String teamName, String urlContentType, List<CreateReviewBodyItemInner> createReviewBody, String subTeam) {
        return createReviewsWithServiceResponseAsync(teamName, urlContentType, createReviewBody, subTeam).toBlocking().single().body();
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param urlContentType The content type.
     * @param createReviewBody Body for create reviews API
     * @param subTeam SubTeam of your team, you want to assign the created review to.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<String>> createReviewsAsync(String teamName, String urlContentType, List<CreateReviewBodyItemInner> createReviewBody, String subTeam, final ServiceCallback<List<String>> serviceCallback) {
        return ServiceFuture.fromResponse(createReviewsWithServiceResponseAsync(teamName, urlContentType, createReviewBody, subTeam), serviceCallback);
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param urlContentType The content type.
     * @param createReviewBody Body for create reviews API
     * @param subTeam SubTeam of your team, you want to assign the created review to.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;String&gt; object
     */
    public Observable<List<String>> createReviewsAsync(String teamName, String urlContentType, List<CreateReviewBodyItemInner> createReviewBody, String subTeam) {
        return createReviewsWithServiceResponseAsync(teamName, urlContentType, createReviewBody, subTeam).map(new Func1<ServiceResponse<List<String>>, List<String>>() {
            @Override
            public List<String> call(ServiceResponse<List<String>> response) {
                return response.body();
            }
        });
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param urlContentType The content type.
     * @param createReviewBody Body for create reviews API
     * @param subTeam SubTeam of your team, you want to assign the created review to.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;String&gt; object
     */
    public Observable<ServiceResponse<List<String>>> createReviewsWithServiceResponseAsync(String teamName, String urlContentType, List<CreateReviewBodyItemInner> createReviewBody, String subTeam) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (urlContentType == null) {
            throw new IllegalArgumentException("Parameter urlContentType is required and cannot be null.");
        }
        if (createReviewBody == null) {
            throw new IllegalArgumentException("Parameter createReviewBody is required and cannot be null.");
        }
        Validator.validate(createReviewBody);
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.createReviews(teamName, urlContentType, subTeam, createReviewBody, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<String>>>>() {
                @Override
                public Observable<ServiceResponse<List<String>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<String>> clientResponse = createReviewsDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<List<String>> createReviewsDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<List<String>, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<List<String>>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * A job Id will be returned for the content posted on this endpoint.
     Once the content is evaluated against the Workflow provided the review will be created or ignored based on the workflow expression.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;p&gt;
     &lt;h4&gt;Job Completion CallBack Sample&lt;/h4&gt;&lt;br/&gt;
     {&lt;br/&gt;
       "JobId": "&lt;Job Id&gt;,&lt;br/&gt;
       "ReviewId": "&lt;Review Id, if the Job resulted in a Review to be created&gt;",&lt;br/&gt;
       "WorkFlowId": "default",&lt;br/&gt;
       "Status": "&lt;This will be one of Complete, InProgress, Error&gt;",&lt;br/&gt;
       "ContentType": "Image",&lt;br/&gt;
       "ContentId": "&lt;This is the ContentId that was specified on input&gt;",&lt;br/&gt;
       "CallBackType": "Job",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;
     &lt;p&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;&lt;br/&gt;
     {
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param contentType Image, Text or Video. Possible values include: 'Image', 'Text', 'Video'
     * @param contentId Id/Name to identify the content submitted.
     * @param workflowName Workflow Name that you want to invoke.
     * @param jobContentType The content type. Possible values include: 'application/json', 'image/jpeg'
     * @param content Content to evaluate.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the JobIdInner object if successful.
     */
    public JobIdInner createJob(String teamName, String contentType, String contentId, String workflowName, String jobContentType, ContentInner content) {
        return createJobWithServiceResponseAsync(teamName, contentType, contentId, workflowName, jobContentType, content).toBlocking().single().body();
    }

    /**
     * A job Id will be returned for the content posted on this endpoint.
     Once the content is evaluated against the Workflow provided the review will be created or ignored based on the workflow expression.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;p&gt;
     &lt;h4&gt;Job Completion CallBack Sample&lt;/h4&gt;&lt;br/&gt;
     {&lt;br/&gt;
       "JobId": "&lt;Job Id&gt;,&lt;br/&gt;
       "ReviewId": "&lt;Review Id, if the Job resulted in a Review to be created&gt;",&lt;br/&gt;
       "WorkFlowId": "default",&lt;br/&gt;
       "Status": "&lt;This will be one of Complete, InProgress, Error&gt;",&lt;br/&gt;
       "ContentType": "Image",&lt;br/&gt;
       "ContentId": "&lt;This is the ContentId that was specified on input&gt;",&lt;br/&gt;
       "CallBackType": "Job",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;
     &lt;p&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;&lt;br/&gt;
     {
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param contentType Image, Text or Video. Possible values include: 'Image', 'Text', 'Video'
     * @param contentId Id/Name to identify the content submitted.
     * @param workflowName Workflow Name that you want to invoke.
     * @param jobContentType The content type. Possible values include: 'application/json', 'image/jpeg'
     * @param content Content to evaluate.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<JobIdInner> createJobAsync(String teamName, String contentType, String contentId, String workflowName, String jobContentType, ContentInner content, final ServiceCallback<JobIdInner> serviceCallback) {
        return ServiceFuture.fromResponse(createJobWithServiceResponseAsync(teamName, contentType, contentId, workflowName, jobContentType, content), serviceCallback);
    }

    /**
     * A job Id will be returned for the content posted on this endpoint.
     Once the content is evaluated against the Workflow provided the review will be created or ignored based on the workflow expression.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;p&gt;
     &lt;h4&gt;Job Completion CallBack Sample&lt;/h4&gt;&lt;br/&gt;
     {&lt;br/&gt;
       "JobId": "&lt;Job Id&gt;,&lt;br/&gt;
       "ReviewId": "&lt;Review Id, if the Job resulted in a Review to be created&gt;",&lt;br/&gt;
       "WorkFlowId": "default",&lt;br/&gt;
       "Status": "&lt;This will be one of Complete, InProgress, Error&gt;",&lt;br/&gt;
       "ContentType": "Image",&lt;br/&gt;
       "ContentId": "&lt;This is the ContentId that was specified on input&gt;",&lt;br/&gt;
       "CallBackType": "Job",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;
     &lt;p&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;&lt;br/&gt;
     {
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param contentType Image, Text or Video. Possible values include: 'Image', 'Text', 'Video'
     * @param contentId Id/Name to identify the content submitted.
     * @param workflowName Workflow Name that you want to invoke.
     * @param jobContentType The content type. Possible values include: 'application/json', 'image/jpeg'
     * @param content Content to evaluate.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the JobIdInner object
     */
    public Observable<JobIdInner> createJobAsync(String teamName, String contentType, String contentId, String workflowName, String jobContentType, ContentInner content) {
        return createJobWithServiceResponseAsync(teamName, contentType, contentId, workflowName, jobContentType, content).map(new Func1<ServiceResponse<JobIdInner>, JobIdInner>() {
            @Override
            public JobIdInner call(ServiceResponse<JobIdInner> response) {
                return response.body();
            }
        });
    }

    /**
     * A job Id will be returned for the content posted on this endpoint.
     Once the content is evaluated against the Workflow provided the review will be created or ignored based on the workflow expression.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;p&gt;
     &lt;h4&gt;Job Completion CallBack Sample&lt;/h4&gt;&lt;br/&gt;
     {&lt;br/&gt;
       "JobId": "&lt;Job Id&gt;,&lt;br/&gt;
       "ReviewId": "&lt;Review Id, if the Job resulted in a Review to be created&gt;",&lt;br/&gt;
       "WorkFlowId": "default",&lt;br/&gt;
       "Status": "&lt;This will be one of Complete, InProgress, Error&gt;",&lt;br/&gt;
       "ContentType": "Image",&lt;br/&gt;
       "ContentId": "&lt;This is the ContentId that was specified on input&gt;",&lt;br/&gt;
       "CallBackType": "Job",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;
     &lt;p&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;&lt;br/&gt;
     {
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param contentType Image, Text or Video. Possible values include: 'Image', 'Text', 'Video'
     * @param contentId Id/Name to identify the content submitted.
     * @param workflowName Workflow Name that you want to invoke.
     * @param jobContentType The content type. Possible values include: 'application/json', 'image/jpeg'
     * @param content Content to evaluate.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the JobIdInner object
     */
    public Observable<ServiceResponse<JobIdInner>> createJobWithServiceResponseAsync(String teamName, String contentType, String contentId, String workflowName, String jobContentType, ContentInner content) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Parameter contentType is required and cannot be null.");
        }
        if (contentId == null) {
            throw new IllegalArgumentException("Parameter contentId is required and cannot be null.");
        }
        if (workflowName == null) {
            throw new IllegalArgumentException("Parameter workflowName is required and cannot be null.");
        }
        if (jobContentType == null) {
            throw new IllegalArgumentException("Parameter jobContentType is required and cannot be null.");
        }
        if (content == null) {
            throw new IllegalArgumentException("Parameter content is required and cannot be null.");
        }
        Validator.validate(content);
        final String callBackEndpoint = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.createJob(teamName, contentType, contentId, workflowName, callBackEndpoint, jobContentType, content, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<JobIdInner>>>() {
                @Override
                public Observable<ServiceResponse<JobIdInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<JobIdInner> clientResponse = createJobDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * A job Id will be returned for the content posted on this endpoint.
     Once the content is evaluated against the Workflow provided the review will be created or ignored based on the workflow expression.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;p&gt;
     &lt;h4&gt;Job Completion CallBack Sample&lt;/h4&gt;&lt;br/&gt;
     {&lt;br/&gt;
       "JobId": "&lt;Job Id&gt;,&lt;br/&gt;
       "ReviewId": "&lt;Review Id, if the Job resulted in a Review to be created&gt;",&lt;br/&gt;
       "WorkFlowId": "default",&lt;br/&gt;
       "Status": "&lt;This will be one of Complete, InProgress, Error&gt;",&lt;br/&gt;
       "ContentType": "Image",&lt;br/&gt;
       "ContentId": "&lt;This is the ContentId that was specified on input&gt;",&lt;br/&gt;
       "CallBackType": "Job",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;
     &lt;p&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;&lt;br/&gt;
     {
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param contentType Image, Text or Video. Possible values include: 'Image', 'Text', 'Video'
     * @param contentId Id/Name to identify the content submitted.
     * @param workflowName Workflow Name that you want to invoke.
     * @param jobContentType The content type. Possible values include: 'application/json', 'image/jpeg'
     * @param content Content to evaluate.
     * @param callBackEndpoint Callback endpoint for posting the create job result.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the JobIdInner object if successful.
     */
    public JobIdInner createJob(String teamName, String contentType, String contentId, String workflowName, String jobContentType, ContentInner content, String callBackEndpoint) {
        return createJobWithServiceResponseAsync(teamName, contentType, contentId, workflowName, jobContentType, content, callBackEndpoint).toBlocking().single().body();
    }

    /**
     * A job Id will be returned for the content posted on this endpoint.
     Once the content is evaluated against the Workflow provided the review will be created or ignored based on the workflow expression.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;p&gt;
     &lt;h4&gt;Job Completion CallBack Sample&lt;/h4&gt;&lt;br/&gt;
     {&lt;br/&gt;
       "JobId": "&lt;Job Id&gt;,&lt;br/&gt;
       "ReviewId": "&lt;Review Id, if the Job resulted in a Review to be created&gt;",&lt;br/&gt;
       "WorkFlowId": "default",&lt;br/&gt;
       "Status": "&lt;This will be one of Complete, InProgress, Error&gt;",&lt;br/&gt;
       "ContentType": "Image",&lt;br/&gt;
       "ContentId": "&lt;This is the ContentId that was specified on input&gt;",&lt;br/&gt;
       "CallBackType": "Job",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;
     &lt;p&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;&lt;br/&gt;
     {
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param contentType Image, Text or Video. Possible values include: 'Image', 'Text', 'Video'
     * @param contentId Id/Name to identify the content submitted.
     * @param workflowName Workflow Name that you want to invoke.
     * @param jobContentType The content type. Possible values include: 'application/json', 'image/jpeg'
     * @param content Content to evaluate.
     * @param callBackEndpoint Callback endpoint for posting the create job result.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<JobIdInner> createJobAsync(String teamName, String contentType, String contentId, String workflowName, String jobContentType, ContentInner content, String callBackEndpoint, final ServiceCallback<JobIdInner> serviceCallback) {
        return ServiceFuture.fromResponse(createJobWithServiceResponseAsync(teamName, contentType, contentId, workflowName, jobContentType, content, callBackEndpoint), serviceCallback);
    }

    /**
     * A job Id will be returned for the content posted on this endpoint.
     Once the content is evaluated against the Workflow provided the review will be created or ignored based on the workflow expression.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;p&gt;
     &lt;h4&gt;Job Completion CallBack Sample&lt;/h4&gt;&lt;br/&gt;
     {&lt;br/&gt;
       "JobId": "&lt;Job Id&gt;,&lt;br/&gt;
       "ReviewId": "&lt;Review Id, if the Job resulted in a Review to be created&gt;",&lt;br/&gt;
       "WorkFlowId": "default",&lt;br/&gt;
       "Status": "&lt;This will be one of Complete, InProgress, Error&gt;",&lt;br/&gt;
       "ContentType": "Image",&lt;br/&gt;
       "ContentId": "&lt;This is the ContentId that was specified on input&gt;",&lt;br/&gt;
       "CallBackType": "Job",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;
     &lt;p&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;&lt;br/&gt;
     {
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param contentType Image, Text or Video. Possible values include: 'Image', 'Text', 'Video'
     * @param contentId Id/Name to identify the content submitted.
     * @param workflowName Workflow Name that you want to invoke.
     * @param jobContentType The content type. Possible values include: 'application/json', 'image/jpeg'
     * @param content Content to evaluate.
     * @param callBackEndpoint Callback endpoint for posting the create job result.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the JobIdInner object
     */
    public Observable<JobIdInner> createJobAsync(String teamName, String contentType, String contentId, String workflowName, String jobContentType, ContentInner content, String callBackEndpoint) {
        return createJobWithServiceResponseAsync(teamName, contentType, contentId, workflowName, jobContentType, content, callBackEndpoint).map(new Func1<ServiceResponse<JobIdInner>, JobIdInner>() {
            @Override
            public JobIdInner call(ServiceResponse<JobIdInner> response) {
                return response.body();
            }
        });
    }

    /**
     * A job Id will be returned for the content posted on this endpoint.
     Once the content is evaluated against the Workflow provided the review will be created or ignored based on the workflow expression.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;p&gt;
     &lt;h4&gt;Job Completion CallBack Sample&lt;/h4&gt;&lt;br/&gt;
     {&lt;br/&gt;
       "JobId": "&lt;Job Id&gt;,&lt;br/&gt;
       "ReviewId": "&lt;Review Id, if the Job resulted in a Review to be created&gt;",&lt;br/&gt;
       "WorkFlowId": "default",&lt;br/&gt;
       "Status": "&lt;This will be one of Complete, InProgress, Error&gt;",&lt;br/&gt;
       "ContentType": "Image",&lt;br/&gt;
       "ContentId": "&lt;This is the ContentId that was specified on input&gt;",&lt;br/&gt;
       "CallBackType": "Job",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;
     &lt;p&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;&lt;br/&gt;
     {
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param contentType Image, Text or Video. Possible values include: 'Image', 'Text', 'Video'
     * @param contentId Id/Name to identify the content submitted.
     * @param workflowName Workflow Name that you want to invoke.
     * @param jobContentType The content type. Possible values include: 'application/json', 'image/jpeg'
     * @param content Content to evaluate.
     * @param callBackEndpoint Callback endpoint for posting the create job result.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the JobIdInner object
     */
    public Observable<ServiceResponse<JobIdInner>> createJobWithServiceResponseAsync(String teamName, String contentType, String contentId, String workflowName, String jobContentType, ContentInner content, String callBackEndpoint) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Parameter contentType is required and cannot be null.");
        }
        if (contentId == null) {
            throw new IllegalArgumentException("Parameter contentId is required and cannot be null.");
        }
        if (workflowName == null) {
            throw new IllegalArgumentException("Parameter workflowName is required and cannot be null.");
        }
        if (jobContentType == null) {
            throw new IllegalArgumentException("Parameter jobContentType is required and cannot be null.");
        }
        if (content == null) {
            throw new IllegalArgumentException("Parameter content is required and cannot be null.");
        }
        Validator.validate(content);
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.createJob(teamName, contentType, contentId, workflowName, callBackEndpoint, jobContentType, content, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<JobIdInner>>>() {
                @Override
                public Observable<ServiceResponse<JobIdInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<JobIdInner> clientResponse = createJobDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<JobIdInner> createJobDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<JobIdInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<JobIdInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void addVideoFrame(String teamName, String reviewId) {
        addVideoFrameWithServiceResponseAsync(teamName, reviewId).toBlocking().single().body();
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> addVideoFrameAsync(String teamName, String reviewId, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(addVideoFrameWithServiceResponseAsync(teamName, reviewId), serviceCallback);
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> addVideoFrameAsync(String teamName, String reviewId) {
        return addVideoFrameWithServiceResponseAsync(teamName, reviewId).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> addVideoFrameWithServiceResponseAsync(String teamName, String reviewId) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (reviewId == null) {
            throw new IllegalArgumentException("Parameter reviewId is required and cannot be null.");
        }
        final Integer timescale = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.addVideoFrame(teamName, reviewId, timescale, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = addVideoFrameDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param timescale Timescale of the video you are adding frames to.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void addVideoFrame(String teamName, String reviewId, Integer timescale) {
        addVideoFrameWithServiceResponseAsync(teamName, reviewId, timescale).toBlocking().single().body();
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param timescale Timescale of the video you are adding frames to.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> addVideoFrameAsync(String teamName, String reviewId, Integer timescale, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(addVideoFrameWithServiceResponseAsync(teamName, reviewId, timescale), serviceCallback);
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param timescale Timescale of the video you are adding frames to.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> addVideoFrameAsync(String teamName, String reviewId, Integer timescale) {
        return addVideoFrameWithServiceResponseAsync(teamName, reviewId, timescale).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param timescale Timescale of the video you are adding frames to.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> addVideoFrameWithServiceResponseAsync(String teamName, String reviewId, Integer timescale) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (reviewId == null) {
            throw new IllegalArgumentException("Parameter reviewId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.addVideoFrame(teamName, reviewId, timescale, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = addVideoFrameDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Void> addVideoFrameDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Void, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the FramesInner object if successful.
     */
    public FramesInner getVideoFrames(String teamName, String reviewId) {
        return getVideoFramesWithServiceResponseAsync(teamName, reviewId).toBlocking().single().body();
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<FramesInner> getVideoFramesAsync(String teamName, String reviewId, final ServiceCallback<FramesInner> serviceCallback) {
        return ServiceFuture.fromResponse(getVideoFramesWithServiceResponseAsync(teamName, reviewId), serviceCallback);
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the FramesInner object
     */
    public Observable<FramesInner> getVideoFramesAsync(String teamName, String reviewId) {
        return getVideoFramesWithServiceResponseAsync(teamName, reviewId).map(new Func1<ServiceResponse<FramesInner>, FramesInner>() {
            @Override
            public FramesInner call(ServiceResponse<FramesInner> response) {
                return response.body();
            }
        });
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the FramesInner object
     */
    public Observable<ServiceResponse<FramesInner>> getVideoFramesWithServiceResponseAsync(String teamName, String reviewId) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (reviewId == null) {
            throw new IllegalArgumentException("Parameter reviewId is required and cannot be null.");
        }
        final Integer startSeed = null;
        final Integer noOfRecords = null;
        final String filter = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.getVideoFrames(teamName, reviewId, startSeed, noOfRecords, filter, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<FramesInner>>>() {
                @Override
                public Observable<ServiceResponse<FramesInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<FramesInner> clientResponse = getVideoFramesDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param startSeed Time stamp of the frame from where you want to start fetching the frames.
     * @param noOfRecords Number of frames to fetch.
     * @param filter Get frames filtered by tags.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the FramesInner object if successful.
     */
    public FramesInner getVideoFrames(String teamName, String reviewId, Integer startSeed, Integer noOfRecords, String filter) {
        return getVideoFramesWithServiceResponseAsync(teamName, reviewId, startSeed, noOfRecords, filter).toBlocking().single().body();
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param startSeed Time stamp of the frame from where you want to start fetching the frames.
     * @param noOfRecords Number of frames to fetch.
     * @param filter Get frames filtered by tags.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<FramesInner> getVideoFramesAsync(String teamName, String reviewId, Integer startSeed, Integer noOfRecords, String filter, final ServiceCallback<FramesInner> serviceCallback) {
        return ServiceFuture.fromResponse(getVideoFramesWithServiceResponseAsync(teamName, reviewId, startSeed, noOfRecords, filter), serviceCallback);
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param startSeed Time stamp of the frame from where you want to start fetching the frames.
     * @param noOfRecords Number of frames to fetch.
     * @param filter Get frames filtered by tags.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the FramesInner object
     */
    public Observable<FramesInner> getVideoFramesAsync(String teamName, String reviewId, Integer startSeed, Integer noOfRecords, String filter) {
        return getVideoFramesWithServiceResponseAsync(teamName, reviewId, startSeed, noOfRecords, filter).map(new Func1<ServiceResponse<FramesInner>, FramesInner>() {
            @Override
            public FramesInner call(ServiceResponse<FramesInner> response) {
                return response.body();
            }
        });
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param startSeed Time stamp of the frame from where you want to start fetching the frames.
     * @param noOfRecords Number of frames to fetch.
     * @param filter Get frames filtered by tags.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the FramesInner object
     */
    public Observable<ServiceResponse<FramesInner>> getVideoFramesWithServiceResponseAsync(String teamName, String reviewId, Integer startSeed, Integer noOfRecords, String filter) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (reviewId == null) {
            throw new IllegalArgumentException("Parameter reviewId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.getVideoFrames(teamName, reviewId, startSeed, noOfRecords, filter, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<FramesInner>>>() {
                @Override
                public Observable<ServiceResponse<FramesInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<FramesInner> clientResponse = getVideoFramesDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<FramesInner> getVideoFramesDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<FramesInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<FramesInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Publish video review to make it available for review.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void publishVideoReview(String teamName, String reviewId) {
        publishVideoReviewWithServiceResponseAsync(teamName, reviewId).toBlocking().single().body();
    }

    /**
     * Publish video review to make it available for review.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> publishVideoReviewAsync(String teamName, String reviewId, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(publishVideoReviewWithServiceResponseAsync(teamName, reviewId), serviceCallback);
    }

    /**
     * Publish video review to make it available for review.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> publishVideoReviewAsync(String teamName, String reviewId) {
        return publishVideoReviewWithServiceResponseAsync(teamName, reviewId).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Publish video review to make it available for review.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> publishVideoReviewWithServiceResponseAsync(String teamName, String reviewId) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (reviewId == null) {
            throw new IllegalArgumentException("Parameter reviewId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.publishVideoReview(teamName, reviewId, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = publishVideoReviewDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Void> publishVideoReviewDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Void, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(204, new TypeToken<Void>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * This API adds a transcript screen text result file for a video review. Transcript screen text result file is a result of Screen Text API . In order to generate transcript screen text result file , a transcript file has to be screened for profanity using Screen Text API.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param contentType The content type.
     * @param transcriptModerationBody Body for add video transcript moderation result API
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void addVideoTranscriptModerationResult(String teamName, String reviewId, String contentType, List<TranscriptModerationBodyItemInner> transcriptModerationBody) {
        addVideoTranscriptModerationResultWithServiceResponseAsync(teamName, reviewId, contentType, transcriptModerationBody).toBlocking().single().body();
    }

    /**
     * This API adds a transcript screen text result file for a video review. Transcript screen text result file is a result of Screen Text API . In order to generate transcript screen text result file , a transcript file has to be screened for profanity using Screen Text API.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param contentType The content type.
     * @param transcriptModerationBody Body for add video transcript moderation result API
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> addVideoTranscriptModerationResultAsync(String teamName, String reviewId, String contentType, List<TranscriptModerationBodyItemInner> transcriptModerationBody, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(addVideoTranscriptModerationResultWithServiceResponseAsync(teamName, reviewId, contentType, transcriptModerationBody), serviceCallback);
    }

    /**
     * This API adds a transcript screen text result file for a video review. Transcript screen text result file is a result of Screen Text API . In order to generate transcript screen text result file , a transcript file has to be screened for profanity using Screen Text API.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param contentType The content type.
     * @param transcriptModerationBody Body for add video transcript moderation result API
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> addVideoTranscriptModerationResultAsync(String teamName, String reviewId, String contentType, List<TranscriptModerationBodyItemInner> transcriptModerationBody) {
        return addVideoTranscriptModerationResultWithServiceResponseAsync(teamName, reviewId, contentType, transcriptModerationBody).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * This API adds a transcript screen text result file for a video review. Transcript screen text result file is a result of Screen Text API . In order to generate transcript screen text result file , a transcript file has to be screened for profanity using Screen Text API.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param contentType The content type.
     * @param transcriptModerationBody Body for add video transcript moderation result API
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> addVideoTranscriptModerationResultWithServiceResponseAsync(String teamName, String reviewId, String contentType, List<TranscriptModerationBodyItemInner> transcriptModerationBody) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (reviewId == null) {
            throw new IllegalArgumentException("Parameter reviewId is required and cannot be null.");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Parameter contentType is required and cannot be null.");
        }
        if (transcriptModerationBody == null) {
            throw new IllegalArgumentException("Parameter transcriptModerationBody is required and cannot be null.");
        }
        Validator.validate(transcriptModerationBody);
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.addVideoTranscriptModerationResult(teamName, reviewId, contentType, transcriptModerationBody, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = addVideoTranscriptModerationResultDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Void> addVideoTranscriptModerationResultDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Void, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(204, new TypeToken<Void>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * This API adds a transcript file (text version of all the words spoken in a video) to a video review. The file should be a valid WebVTT format.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param vTTfile Transcript file of the video.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void addVideoTranscript(String teamName, String reviewId, byte[] vTTfile) {
        addVideoTranscriptWithServiceResponseAsync(teamName, reviewId, vTTfile).toBlocking().single().body();
    }

    /**
     * This API adds a transcript file (text version of all the words spoken in a video) to a video review. The file should be a valid WebVTT format.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param vTTfile Transcript file of the video.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> addVideoTranscriptAsync(String teamName, String reviewId, byte[] vTTfile, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(addVideoTranscriptWithServiceResponseAsync(teamName, reviewId, vTTfile), serviceCallback);
    }

    /**
     * This API adds a transcript file (text version of all the words spoken in a video) to a video review. The file should be a valid WebVTT format.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param vTTfile Transcript file of the video.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> addVideoTranscriptAsync(String teamName, String reviewId, byte[] vTTfile) {
        return addVideoTranscriptWithServiceResponseAsync(teamName, reviewId, vTTfile).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * This API adds a transcript file (text version of all the words spoken in a video) to a video review. The file should be a valid WebVTT format.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param vTTfile Transcript file of the video.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> addVideoTranscriptWithServiceResponseAsync(String teamName, String reviewId, byte[] vTTfile) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (reviewId == null) {
            throw new IllegalArgumentException("Parameter reviewId is required and cannot be null.");
        }
        if (vTTfile == null) {
            throw new IllegalArgumentException("Parameter vTTfile is required and cannot be null.");
        }
        final String contentType = "text/plain";
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        RequestBody vTTfileConverted = RequestBody.create(MediaType.parse("text/plain"), vTTfile);
        return service.addVideoTranscript(teamName, reviewId, contentType, vTTfileConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = addVideoTranscriptDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Void> addVideoTranscriptDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Void, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(204, new TypeToken<Void>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param contentType The content type.
     * @param createVideoReviewsBody Body for create reviews API
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;String&gt; object if successful.
     */
    public List<String> createVideoReviews(String teamName, String contentType, List<CreateVideoReviewsBodyItemInner> createVideoReviewsBody) {
        return createVideoReviewsWithServiceResponseAsync(teamName, contentType, createVideoReviewsBody).toBlocking().single().body();
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param contentType The content type.
     * @param createVideoReviewsBody Body for create reviews API
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<String>> createVideoReviewsAsync(String teamName, String contentType, List<CreateVideoReviewsBodyItemInner> createVideoReviewsBody, final ServiceCallback<List<String>> serviceCallback) {
        return ServiceFuture.fromResponse(createVideoReviewsWithServiceResponseAsync(teamName, contentType, createVideoReviewsBody), serviceCallback);
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param contentType The content type.
     * @param createVideoReviewsBody Body for create reviews API
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;String&gt; object
     */
    public Observable<List<String>> createVideoReviewsAsync(String teamName, String contentType, List<CreateVideoReviewsBodyItemInner> createVideoReviewsBody) {
        return createVideoReviewsWithServiceResponseAsync(teamName, contentType, createVideoReviewsBody).map(new Func1<ServiceResponse<List<String>>, List<String>>() {
            @Override
            public List<String> call(ServiceResponse<List<String>> response) {
                return response.body();
            }
        });
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param contentType The content type.
     * @param createVideoReviewsBody Body for create reviews API
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;String&gt; object
     */
    public Observable<ServiceResponse<List<String>>> createVideoReviewsWithServiceResponseAsync(String teamName, String contentType, List<CreateVideoReviewsBodyItemInner> createVideoReviewsBody) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Parameter contentType is required and cannot be null.");
        }
        if (createVideoReviewsBody == null) {
            throw new IllegalArgumentException("Parameter createVideoReviewsBody is required and cannot be null.");
        }
        Validator.validate(createVideoReviewsBody);
        final String subTeam = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.createVideoReviews(teamName, contentType, subTeam, createVideoReviewsBody, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<String>>>>() {
                @Override
                public Observable<ServiceResponse<List<String>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<String>> clientResponse = createVideoReviewsDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param contentType The content type.
     * @param createVideoReviewsBody Body for create reviews API
     * @param subTeam SubTeam of your team, you want to assign the created review to.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;String&gt; object if successful.
     */
    public List<String> createVideoReviews(String teamName, String contentType, List<CreateVideoReviewsBodyItemInner> createVideoReviewsBody, String subTeam) {
        return createVideoReviewsWithServiceResponseAsync(teamName, contentType, createVideoReviewsBody, subTeam).toBlocking().single().body();
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param contentType The content type.
     * @param createVideoReviewsBody Body for create reviews API
     * @param subTeam SubTeam of your team, you want to assign the created review to.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<String>> createVideoReviewsAsync(String teamName, String contentType, List<CreateVideoReviewsBodyItemInner> createVideoReviewsBody, String subTeam, final ServiceCallback<List<String>> serviceCallback) {
        return ServiceFuture.fromResponse(createVideoReviewsWithServiceResponseAsync(teamName, contentType, createVideoReviewsBody, subTeam), serviceCallback);
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param contentType The content type.
     * @param createVideoReviewsBody Body for create reviews API
     * @param subTeam SubTeam of your team, you want to assign the created review to.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;String&gt; object
     */
    public Observable<List<String>> createVideoReviewsAsync(String teamName, String contentType, List<CreateVideoReviewsBodyItemInner> createVideoReviewsBody, String subTeam) {
        return createVideoReviewsWithServiceResponseAsync(teamName, contentType, createVideoReviewsBody, subTeam).map(new Func1<ServiceResponse<List<String>>, List<String>>() {
            @Override
            public List<String> call(ServiceResponse<List<String>> response) {
                return response.body();
            }
        });
    }

    /**
     * The reviews created would show up for Reviewers on your team. As Reviewers complete reviewing, results of the Review would be POSTED (i.e. HTTP POST) on the specified CallBackEndpoint.
     &lt;h3&gt;CallBack Schemas &lt;/h3&gt;
     &lt;h4&gt;Review Completion CallBack Sample&lt;/h4&gt;
     &lt;p&gt;
     {&lt;br/&gt;
       "ReviewId": "&lt;Review Id&gt;",&lt;br/&gt;
       "ModifiedOn": "2016-10-11T22:36:32.9934851Z",&lt;br/&gt;
       "ModifiedBy": "&lt;Name of the Reviewer&gt;",&lt;br/&gt;
       "CallBackType": "Review",&lt;br/&gt;
       "ContentId": "&lt;The ContentId that was specified input&gt;",&lt;br/&gt;
       "Metadata": {&lt;br/&gt;
         "adultscore": "0.xxx",&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "racyscore": "0.xxx",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       },&lt;br/&gt;
       "ReviewerResultTags": {&lt;br/&gt;
         "a": "False",&lt;br/&gt;
         "r": "True"&lt;br/&gt;
       }&lt;br/&gt;
     }&lt;br/&gt;
     &lt;/p&gt;.
     *
     * @param teamName Your team name.
     * @param contentType The content type.
     * @param createVideoReviewsBody Body for create reviews API
     * @param subTeam SubTeam of your team, you want to assign the created review to.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;String&gt; object
     */
    public Observable<ServiceResponse<List<String>>> createVideoReviewsWithServiceResponseAsync(String teamName, String contentType, List<CreateVideoReviewsBodyItemInner> createVideoReviewsBody, String subTeam) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Parameter contentType is required and cannot be null.");
        }
        if (createVideoReviewsBody == null) {
            throw new IllegalArgumentException("Parameter createVideoReviewsBody is required and cannot be null.");
        }
        Validator.validate(createVideoReviewsBody);
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.createVideoReviews(teamName, contentType, subTeam, createVideoReviewsBody, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<String>>>>() {
                @Override
                public Observable<ServiceResponse<List<String>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<String>> clientResponse = createVideoReviewsDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<List<String>> createVideoReviewsDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<List<String>, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<List<String>>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Use this method to add frames for a video review.Timescale: This parameter is a factor which is used to convert the timestamp on a frame into milliseconds. Timescale is provided in the output of the Content Moderator video media processor on the Azure Media Services platform.Timescale in the Video Moderation output is Ticks/Second.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param contentType The content type.
     * @param videoFrameBody Body for add video frames API
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void addVideoFrameUrl(String teamName, String reviewId, String contentType, List<VideoFrameBodyItemInner> videoFrameBody) {
        addVideoFrameUrlWithServiceResponseAsync(teamName, reviewId, contentType, videoFrameBody).toBlocking().single().body();
    }

    /**
     * Use this method to add frames for a video review.Timescale: This parameter is a factor which is used to convert the timestamp on a frame into milliseconds. Timescale is provided in the output of the Content Moderator video media processor on the Azure Media Services platform.Timescale in the Video Moderation output is Ticks/Second.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param contentType The content type.
     * @param videoFrameBody Body for add video frames API
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> addVideoFrameUrlAsync(String teamName, String reviewId, String contentType, List<VideoFrameBodyItemInner> videoFrameBody, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(addVideoFrameUrlWithServiceResponseAsync(teamName, reviewId, contentType, videoFrameBody), serviceCallback);
    }

    /**
     * Use this method to add frames for a video review.Timescale: This parameter is a factor which is used to convert the timestamp on a frame into milliseconds. Timescale is provided in the output of the Content Moderator video media processor on the Azure Media Services platform.Timescale in the Video Moderation output is Ticks/Second.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param contentType The content type.
     * @param videoFrameBody Body for add video frames API
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> addVideoFrameUrlAsync(String teamName, String reviewId, String contentType, List<VideoFrameBodyItemInner> videoFrameBody) {
        return addVideoFrameUrlWithServiceResponseAsync(teamName, reviewId, contentType, videoFrameBody).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Use this method to add frames for a video review.Timescale: This parameter is a factor which is used to convert the timestamp on a frame into milliseconds. Timescale is provided in the output of the Content Moderator video media processor on the Azure Media Services platform.Timescale in the Video Moderation output is Ticks/Second.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param contentType The content type.
     * @param videoFrameBody Body for add video frames API
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> addVideoFrameUrlWithServiceResponseAsync(String teamName, String reviewId, String contentType, List<VideoFrameBodyItemInner> videoFrameBody) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (reviewId == null) {
            throw new IllegalArgumentException("Parameter reviewId is required and cannot be null.");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Parameter contentType is required and cannot be null.");
        }
        if (videoFrameBody == null) {
            throw new IllegalArgumentException("Parameter videoFrameBody is required and cannot be null.");
        }
        Validator.validate(videoFrameBody);
        final Integer timescale = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.addVideoFrameUrl(teamName, reviewId, contentType, timescale, videoFrameBody, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = addVideoFrameUrlDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Use this method to add frames for a video review.Timescale: This parameter is a factor which is used to convert the timestamp on a frame into milliseconds. Timescale is provided in the output of the Content Moderator video media processor on the Azure Media Services platform.Timescale in the Video Moderation output is Ticks/Second.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param contentType The content type.
     * @param videoFrameBody Body for add video frames API
     * @param timescale Timescale of the video.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void addVideoFrameUrl(String teamName, String reviewId, String contentType, List<VideoFrameBodyItemInner> videoFrameBody, Integer timescale) {
        addVideoFrameUrlWithServiceResponseAsync(teamName, reviewId, contentType, videoFrameBody, timescale).toBlocking().single().body();
    }

    /**
     * Use this method to add frames for a video review.Timescale: This parameter is a factor which is used to convert the timestamp on a frame into milliseconds. Timescale is provided in the output of the Content Moderator video media processor on the Azure Media Services platform.Timescale in the Video Moderation output is Ticks/Second.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param contentType The content type.
     * @param videoFrameBody Body for add video frames API
     * @param timescale Timescale of the video.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> addVideoFrameUrlAsync(String teamName, String reviewId, String contentType, List<VideoFrameBodyItemInner> videoFrameBody, Integer timescale, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(addVideoFrameUrlWithServiceResponseAsync(teamName, reviewId, contentType, videoFrameBody, timescale), serviceCallback);
    }

    /**
     * Use this method to add frames for a video review.Timescale: This parameter is a factor which is used to convert the timestamp on a frame into milliseconds. Timescale is provided in the output of the Content Moderator video media processor on the Azure Media Services platform.Timescale in the Video Moderation output is Ticks/Second.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param contentType The content type.
     * @param videoFrameBody Body for add video frames API
     * @param timescale Timescale of the video.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> addVideoFrameUrlAsync(String teamName, String reviewId, String contentType, List<VideoFrameBodyItemInner> videoFrameBody, Integer timescale) {
        return addVideoFrameUrlWithServiceResponseAsync(teamName, reviewId, contentType, videoFrameBody, timescale).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Use this method to add frames for a video review.Timescale: This parameter is a factor which is used to convert the timestamp on a frame into milliseconds. Timescale is provided in the output of the Content Moderator video media processor on the Azure Media Services platform.Timescale in the Video Moderation output is Ticks/Second.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param contentType The content type.
     * @param videoFrameBody Body for add video frames API
     * @param timescale Timescale of the video.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> addVideoFrameUrlWithServiceResponseAsync(String teamName, String reviewId, String contentType, List<VideoFrameBodyItemInner> videoFrameBody, Integer timescale) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (reviewId == null) {
            throw new IllegalArgumentException("Parameter reviewId is required and cannot be null.");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Parameter contentType is required and cannot be null.");
        }
        if (videoFrameBody == null) {
            throw new IllegalArgumentException("Parameter videoFrameBody is required and cannot be null.");
        }
        Validator.validate(videoFrameBody);
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.addVideoFrameUrl(teamName, reviewId, contentType, timescale, videoFrameBody, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = addVideoFrameUrlDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Void> addVideoFrameUrlDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Void, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(204, new TypeToken<Void>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Use this method to add frames for a video review.Timescale: This parameter is a factor which is used to convert the timestamp on a frame into milliseconds. Timescale is provided in the output of the Content Moderator video media processor on the Azure Media Services platform.Timescale in the Video Moderation output is Ticks/Second.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param contentType The content type.
     * @param frameImageZip Zip file containing frame images.
     * @param frameMetadata Metadata of the frame.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void addVideoFrameStream(String teamName, String reviewId, String contentType, byte[] frameImageZip, String frameMetadata) {
        addVideoFrameStreamWithServiceResponseAsync(teamName, reviewId, contentType, frameImageZip, frameMetadata).toBlocking().single().body();
    }

    /**
     * Use this method to add frames for a video review.Timescale: This parameter is a factor which is used to convert the timestamp on a frame into milliseconds. Timescale is provided in the output of the Content Moderator video media processor on the Azure Media Services platform.Timescale in the Video Moderation output is Ticks/Second.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param contentType The content type.
     * @param frameImageZip Zip file containing frame images.
     * @param frameMetadata Metadata of the frame.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> addVideoFrameStreamAsync(String teamName, String reviewId, String contentType, byte[] frameImageZip, String frameMetadata, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(addVideoFrameStreamWithServiceResponseAsync(teamName, reviewId, contentType, frameImageZip, frameMetadata), serviceCallback);
    }

    /**
     * Use this method to add frames for a video review.Timescale: This parameter is a factor which is used to convert the timestamp on a frame into milliseconds. Timescale is provided in the output of the Content Moderator video media processor on the Azure Media Services platform.Timescale in the Video Moderation output is Ticks/Second.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param contentType The content type.
     * @param frameImageZip Zip file containing frame images.
     * @param frameMetadata Metadata of the frame.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> addVideoFrameStreamAsync(String teamName, String reviewId, String contentType, byte[] frameImageZip, String frameMetadata) {
        return addVideoFrameStreamWithServiceResponseAsync(teamName, reviewId, contentType, frameImageZip, frameMetadata).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Use this method to add frames for a video review.Timescale: This parameter is a factor which is used to convert the timestamp on a frame into milliseconds. Timescale is provided in the output of the Content Moderator video media processor on the Azure Media Services platform.Timescale in the Video Moderation output is Ticks/Second.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param contentType The content type.
     * @param frameImageZip Zip file containing frame images.
     * @param frameMetadata Metadata of the frame.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> addVideoFrameStreamWithServiceResponseAsync(String teamName, String reviewId, String contentType, byte[] frameImageZip, String frameMetadata) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (reviewId == null) {
            throw new IllegalArgumentException("Parameter reviewId is required and cannot be null.");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Parameter contentType is required and cannot be null.");
        }
        if (frameImageZip == null) {
            throw new IllegalArgumentException("Parameter frameImageZip is required and cannot be null.");
        }
        if (frameMetadata == null) {
            throw new IllegalArgumentException("Parameter frameMetadata is required and cannot be null.");
        }
        final Integer timescale = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        RequestBody frameImageZipConverted = RequestBody.create(MediaType.parse("multipart/form-data"), frameImageZip);
        return service.addVideoFrameStream(teamName, reviewId, contentType, timescale, frameImageZipConverted, frameMetadata, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = addVideoFrameStreamDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Use this method to add frames for a video review.Timescale: This parameter is a factor which is used to convert the timestamp on a frame into milliseconds. Timescale is provided in the output of the Content Moderator video media processor on the Azure Media Services platform.Timescale in the Video Moderation output is Ticks/Second.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param contentType The content type.
     * @param frameImageZip Zip file containing frame images.
     * @param frameMetadata Metadata of the frame.
     * @param timescale Timescale of the video .
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void addVideoFrameStream(String teamName, String reviewId, String contentType, byte[] frameImageZip, String frameMetadata, Integer timescale) {
        addVideoFrameStreamWithServiceResponseAsync(teamName, reviewId, contentType, frameImageZip, frameMetadata, timescale).toBlocking().single().body();
    }

    /**
     * Use this method to add frames for a video review.Timescale: This parameter is a factor which is used to convert the timestamp on a frame into milliseconds. Timescale is provided in the output of the Content Moderator video media processor on the Azure Media Services platform.Timescale in the Video Moderation output is Ticks/Second.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param contentType The content type.
     * @param frameImageZip Zip file containing frame images.
     * @param frameMetadata Metadata of the frame.
     * @param timescale Timescale of the video .
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> addVideoFrameStreamAsync(String teamName, String reviewId, String contentType, byte[] frameImageZip, String frameMetadata, Integer timescale, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(addVideoFrameStreamWithServiceResponseAsync(teamName, reviewId, contentType, frameImageZip, frameMetadata, timescale), serviceCallback);
    }

    /**
     * Use this method to add frames for a video review.Timescale: This parameter is a factor which is used to convert the timestamp on a frame into milliseconds. Timescale is provided in the output of the Content Moderator video media processor on the Azure Media Services platform.Timescale in the Video Moderation output is Ticks/Second.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param contentType The content type.
     * @param frameImageZip Zip file containing frame images.
     * @param frameMetadata Metadata of the frame.
     * @param timescale Timescale of the video .
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> addVideoFrameStreamAsync(String teamName, String reviewId, String contentType, byte[] frameImageZip, String frameMetadata, Integer timescale) {
        return addVideoFrameStreamWithServiceResponseAsync(teamName, reviewId, contentType, frameImageZip, frameMetadata, timescale).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Use this method to add frames for a video review.Timescale: This parameter is a factor which is used to convert the timestamp on a frame into milliseconds. Timescale is provided in the output of the Content Moderator video media processor on the Azure Media Services platform.Timescale in the Video Moderation output is Ticks/Second.
     *
     * @param teamName Your team name.
     * @param reviewId Id of the review.
     * @param contentType The content type.
     * @param frameImageZip Zip file containing frame images.
     * @param frameMetadata Metadata of the frame.
     * @param timescale Timescale of the video .
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> addVideoFrameStreamWithServiceResponseAsync(String teamName, String reviewId, String contentType, byte[] frameImageZip, String frameMetadata, Integer timescale) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (reviewId == null) {
            throw new IllegalArgumentException("Parameter reviewId is required and cannot be null.");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Parameter contentType is required and cannot be null.");
        }
        if (frameImageZip == null) {
            throw new IllegalArgumentException("Parameter frameImageZip is required and cannot be null.");
        }
        if (frameMetadata == null) {
            throw new IllegalArgumentException("Parameter frameMetadata is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        RequestBody frameImageZipConverted = RequestBody.create(MediaType.parse("multipart/form-data"), frameImageZip);
        return service.addVideoFrameStream(teamName, reviewId, contentType, timescale, frameImageZipConverted, frameMetadata, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = addVideoFrameStreamDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Void> addVideoFrameStreamDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Void, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(204, new TypeToken<Void>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

}
