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
 * in Jobs.
 */
public class JobsInner {
    /** The Retrofit service to perform REST calls. */
    private JobsService service;
    /** The service client containing this operation class. */
    private ContentModeratorImageTextClientImpl client;

    /**
     * Initializes an instance of JobsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public JobsInner(Retrofit retrofit, ContentModeratorImageTextClientImpl client) {
        this.service = retrofit.create(JobsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for Jobs to be
     * used by Retrofit to perform actually REST calls.
     */
    interface JobsService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Jobs getJobDetails" })
        @GET("contentmoderator/review/v1.0/teams/{teamName}/jobs/{JobId}")
        Observable<Response<ResponseBody>> getJobDetails(@Path("teamName") String teamName, @Path("JobId") String jobId, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Jobs createJobMethod" })
        @POST("contentmoderator/review/v1.0/teams/{teamName}/jobs")
        Observable<Response<ResponseBody>> createJobMethod(@Path("teamName") String teamName, @Query("ContentType") String contentType, @Query("ContentId") String contentId, @Query("WorkflowName") String workflowName, @Query("CallBackEndpoint") String callBackEndpoint, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("Content-Type") String contentType, @Body ContentInner content, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

    }

    /**
     * Get the Job Details for a Job Id.
     *
     * @param teamName Your Team Name.
     * @param jobId Id of the job.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
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
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (teamName == null) {
            throw new IllegalArgumentException("Parameter teamName is required and cannot be null.");
        }
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.getJobDetails(teamName, jobId, this.client.ocpApimSubscriptionKey(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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

    private ServiceResponse<JobInner> getJobDetailsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<JobInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<JobInner>() { }.getType())
                .registerError(CloudException.class)
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
     * @param content Content to evaluate.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the CreateJobInner object if successful.
     */
    public CreateJobInner createJobMethod(String teamName, String contentType, String contentId, String workflowName, ContentInner content) {
        return createJobMethodWithServiceResponseAsync(teamName, contentType, contentId, workflowName, content).toBlocking().single().body();
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
     * @param content Content to evaluate.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<CreateJobInner> createJobMethodAsync(String teamName, String contentType, String contentId, String workflowName, ContentInner content, final ServiceCallback<CreateJobInner> serviceCallback) {
        return ServiceFuture.fromResponse(createJobMethodWithServiceResponseAsync(teamName, contentType, contentId, workflowName, content), serviceCallback);
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
     * @param content Content to evaluate.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the CreateJobInner object
     */
    public Observable<CreateJobInner> createJobMethodAsync(String teamName, String contentType, String contentId, String workflowName, ContentInner content) {
        return createJobMethodWithServiceResponseAsync(teamName, contentType, contentId, workflowName, content).map(new Func1<ServiceResponse<CreateJobInner>, CreateJobInner>() {
            @Override
            public CreateJobInner call(ServiceResponse<CreateJobInner> response) {
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
     * @param content Content to evaluate.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the CreateJobInner object
     */
    public Observable<ServiceResponse<CreateJobInner>> createJobMethodWithServiceResponseAsync(String teamName, String contentType, String contentId, String workflowName, ContentInner content) {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
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
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        if (this.client.contentType() == null) {
            throw new IllegalArgumentException("Parameter this.client.contentType() is required and cannot be null.");
        }
        if (content == null) {
            throw new IllegalArgumentException("Parameter content is required and cannot be null.");
        }
        Validator.validate(content);
        final String callBackEndpoint = null;
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.createJobMethod(teamName, contentType, contentId, workflowName, callBackEndpoint, this.client.ocpApimSubscriptionKey(), this.client.contentType(), content, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<CreateJobInner>>>() {
                @Override
                public Observable<ServiceResponse<CreateJobInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<CreateJobInner> clientResponse = createJobMethodDelegate(response);
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
     * @param content Content to evaluate.
     * @param callBackEndpoint Callback endpoint for posting the create job result.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the CreateJobInner object if successful.
     */
    public CreateJobInner createJobMethod(String teamName, String contentType, String contentId, String workflowName, ContentInner content, String callBackEndpoint) {
        return createJobMethodWithServiceResponseAsync(teamName, contentType, contentId, workflowName, content, callBackEndpoint).toBlocking().single().body();
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
     * @param content Content to evaluate.
     * @param callBackEndpoint Callback endpoint for posting the create job result.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<CreateJobInner> createJobMethodAsync(String teamName, String contentType, String contentId, String workflowName, ContentInner content, String callBackEndpoint, final ServiceCallback<CreateJobInner> serviceCallback) {
        return ServiceFuture.fromResponse(createJobMethodWithServiceResponseAsync(teamName, contentType, contentId, workflowName, content, callBackEndpoint), serviceCallback);
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
     * @param content Content to evaluate.
     * @param callBackEndpoint Callback endpoint for posting the create job result.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the CreateJobInner object
     */
    public Observable<CreateJobInner> createJobMethodAsync(String teamName, String contentType, String contentId, String workflowName, ContentInner content, String callBackEndpoint) {
        return createJobMethodWithServiceResponseAsync(teamName, contentType, contentId, workflowName, content, callBackEndpoint).map(new Func1<ServiceResponse<CreateJobInner>, CreateJobInner>() {
            @Override
            public CreateJobInner call(ServiceResponse<CreateJobInner> response) {
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
     * @param content Content to evaluate.
     * @param callBackEndpoint Callback endpoint for posting the create job result.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the CreateJobInner object
     */
    public Observable<ServiceResponse<CreateJobInner>> createJobMethodWithServiceResponseAsync(String teamName, String contentType, String contentId, String workflowName, ContentInner content, String callBackEndpoint) {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
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
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        if (this.client.contentType() == null) {
            throw new IllegalArgumentException("Parameter this.client.contentType() is required and cannot be null.");
        }
        if (content == null) {
            throw new IllegalArgumentException("Parameter content is required and cannot be null.");
        }
        Validator.validate(content);
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.createJobMethod(teamName, contentType, contentId, workflowName, callBackEndpoint, this.client.ocpApimSubscriptionKey(), this.client.contentType(), content, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<CreateJobInner>>>() {
                @Override
                public Observable<ServiceResponse<CreateJobInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<CreateJobInner> clientResponse = createJobMethodDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<CreateJobInner> createJobMethodDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<CreateJobInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<CreateJobInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
