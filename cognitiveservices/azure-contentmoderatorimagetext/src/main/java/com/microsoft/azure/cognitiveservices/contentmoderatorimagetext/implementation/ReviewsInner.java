/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.implementation;

import retrofit2.Retrofit;
import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.AzureRegion;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.Validator;
import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.POST;
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
    private ContentModeratorImageTextClientImpl client;

    /**
     * Initializes an instance of ReviewsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public ReviewsInner(Retrofit retrofit, ContentModeratorImageTextClientImpl client) {
        this.service = retrofit.create(ReviewsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for Reviews to be
     * used by Retrofit to perform actually REST calls.
     */
    interface ReviewsService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Reviews getReview" })
        @GET("contentmoderator/review/v1.0/teams/{teamName}/reviews/{reviewId}")
        Observable<Response<ResponseBody>> getReview(@Path("teamName") String teamName, @Path("reviewId") String reviewId, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Reviews createReviews" })
        @POST("contentmoderator/review/v1.0/teams/{teamName}/reviews")
        Observable<Response<ResponseBody>> createReviews(@Path("teamName") String teamName, @Header("Content-Type") String contentType, @Query("subTeam") String subTeam, @Body List<BodyItemInner> body, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

    }

    /**
     * Returns review details for the review Id passed.
     *
     * @param teamName Your Team Name.
     * @param reviewId Id of the review.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
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
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (reviewId == null) {
            throw new IllegalArgumentException("Parameter reviewId is required and cannot be null.");
        }
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.getReview(teamName, reviewId, this.client.ocpApimSubscriptionKey(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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

    private ServiceResponse<ReviewInner> getReviewDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<ReviewInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ReviewInner>() { }.getType())
                .registerError(CloudException.class)
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
     * @param body Body for create reviews API
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ReviewListResultInner object if successful.
     */
    public ReviewListResultInner createReviews(String teamName, List<BodyItemInner> body) {
        return createReviewsWithServiceResponseAsync(teamName, body).toBlocking().single().body();
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
     * @param body Body for create reviews API
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ReviewListResultInner> createReviewsAsync(String teamName, List<BodyItemInner> body, final ServiceCallback<ReviewListResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(createReviewsWithServiceResponseAsync(teamName, body), serviceCallback);
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
     * @param body Body for create reviews API
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ReviewListResultInner object
     */
    public Observable<ReviewListResultInner> createReviewsAsync(String teamName, List<BodyItemInner> body) {
        return createReviewsWithServiceResponseAsync(teamName, body).map(new Func1<ServiceResponse<ReviewListResultInner>, ReviewListResultInner>() {
            @Override
            public ReviewListResultInner call(ServiceResponse<ReviewListResultInner> response) {
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
     * @param body Body for create reviews API
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ReviewListResultInner object
     */
    public Observable<ServiceResponse<ReviewListResultInner>> createReviewsWithServiceResponseAsync(String teamName, List<BodyItemInner> body) {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (this.client.contentType() == null) {
            throw new IllegalArgumentException("Parameter this.client.contentType() is required and cannot be null.");
        }
        if (body == null) {
            throw new IllegalArgumentException("Parameter body is required and cannot be null.");
        }
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        Validator.validate(body);
        final String subTeam = null;
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.createReviews(teamName, this.client.contentType(), subTeam, body, this.client.ocpApimSubscriptionKey(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ReviewListResultInner>>>() {
                @Override
                public Observable<ServiceResponse<ReviewListResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ReviewListResultInner> clientResponse = createReviewsDelegate(response);
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
     * @param body Body for create reviews API
     * @param subTeam SubTeam of your team, you want to assign the created review to.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ReviewListResultInner object if successful.
     */
    public ReviewListResultInner createReviews(String teamName, List<BodyItemInner> body, String subTeam) {
        return createReviewsWithServiceResponseAsync(teamName, body, subTeam).toBlocking().single().body();
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
     * @param body Body for create reviews API
     * @param subTeam SubTeam of your team, you want to assign the created review to.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ReviewListResultInner> createReviewsAsync(String teamName, List<BodyItemInner> body, String subTeam, final ServiceCallback<ReviewListResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(createReviewsWithServiceResponseAsync(teamName, body, subTeam), serviceCallback);
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
     * @param body Body for create reviews API
     * @param subTeam SubTeam of your team, you want to assign the created review to.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ReviewListResultInner object
     */
    public Observable<ReviewListResultInner> createReviewsAsync(String teamName, List<BodyItemInner> body, String subTeam) {
        return createReviewsWithServiceResponseAsync(teamName, body, subTeam).map(new Func1<ServiceResponse<ReviewListResultInner>, ReviewListResultInner>() {
            @Override
            public ReviewListResultInner call(ServiceResponse<ReviewListResultInner> response) {
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
     * @param body Body for create reviews API
     * @param subTeam SubTeam of your team, you want to assign the created review to.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ReviewListResultInner object
     */
    public Observable<ServiceResponse<ReviewListResultInner>> createReviewsWithServiceResponseAsync(String teamName, List<BodyItemInner> body, String subTeam) {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (this.client.contentType() == null) {
            throw new IllegalArgumentException("Parameter this.client.contentType() is required and cannot be null.");
        }
        if (body == null) {
            throw new IllegalArgumentException("Parameter body is required and cannot be null.");
        }
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        Validator.validate(body);
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.createReviews(teamName, this.client.contentType(), subTeam, body, this.client.ocpApimSubscriptionKey(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ReviewListResultInner>>>() {
                @Override
                public Observable<ServiceResponse<ReviewListResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ReviewListResultInner> clientResponse = createReviewsDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<ReviewListResultInner> createReviewsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<ReviewListResultInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ReviewListResultInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
