/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.implementation;

import com.microsoft.azure.AzureClient;
import com.microsoft.azure.AzureServiceClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.RestClient;

/**
 * Initializes a new instance of the ContentModeratorImageTextClientImpl class.
 */
public class ContentModeratorImageTextClientImpl extends AzureServiceClient {
    /** the {@link AzureClient} used for long running operations. */
    private AzureClient azureClient;

    /**
     * Gets the {@link AzureClient} used for long running operations.
     * @return the azure client;
     */
    public AzureClient getAzureClient() {
        return this.azureClient;
    }

    /** <a href="http://www-01.sil.org/iso639-3/codes.asp">ISO 639-3 code</a>. */
    private String language;

    /**
     * Gets <a href="http://www-01.sil.org/iso639-3/codes.asp">ISO 639-3 code</a>.
     *
     * @return the language value.
     */
    public String language() {
        return this.language;
    }

    /**
     * Sets <a href="http://www-01.sil.org/iso639-3/codes.asp">ISO 639-3 code</a>.
     *
     * @param language the language value.
     * @return the service client itself
     */
    public ContentModeratorImageTextClientImpl withLanguage(String language) {
        this.language = language;
        return this;
    }

    /** Whether to retain the submitted image for future use; defaults to false if omitted. */
    private boolean cacheImage;

    /**
     * Gets Whether to retain the submitted image for future use; defaults to false if omitted.
     *
     * @return the cacheImage value.
     */
    public boolean cacheImage() {
        return this.cacheImage;
    }

    /**
     * Sets Whether to retain the submitted image for future use; defaults to false if omitted.
     *
     * @param cacheImage the cacheImage value.
     * @return the service client itself
     */
    public ContentModeratorImageTextClientImpl withCacheImage(boolean cacheImage) {
        this.cacheImage = cacheImage;
        return this;
    }

    /** When set to True, the image goes through additional processing to come with additional candidates.
    image/tiff is not supported when enhanced is set to true
    Note: This impacts the response time. */
    private boolean enhanced;

    /**
     * Gets When set to True, the image goes through additional processing to come with additional candidates.
     image/tiff is not supported when enhanced is set to true
     Note: This impacts the response time.
     *
     * @return the enhanced value.
     */
    public boolean enhanced() {
        return this.enhanced;
    }

    /**
     * Sets When set to True, the image goes through additional processing to come with additional candidates.
     image/tiff is not supported when enhanced is set to true
     Note: This impacts the response time.
     *
     * @param enhanced the enhanced value.
     * @return the service client itself
     */
    public ContentModeratorImageTextClientImpl withEnhanced(boolean enhanced) {
        this.enhanced = enhanced;
        return this;
    }

    /** Id of the image. */
    private String imageId;

    /**
     * Gets Id of the image.
     *
     * @return the imageId value.
     */
    public String imageId() {
        return this.imageId;
    }

    /**
     * Sets Id of the image.
     *
     * @param imageId the imageId value.
     * @return the service client itself
     */
    public ContentModeratorImageTextClientImpl withImageId(String imageId) {
        this.imageId = imageId;
        return this;
    }

    /** The list Id. */
    private String listId;

    /**
     * Gets The list Id.
     *
     * @return the listId value.
     */
    public String listId() {
        return this.listId;
    }

    /**
     * Sets The list Id.
     *
     * @param listId the listId value.
     * @return the service client itself
     */
    public ContentModeratorImageTextClientImpl withListId(String listId) {
        this.listId = listId;
        return this;
    }

    /** Autocorrect text. */
    private boolean autocorrect;

    /**
     * Gets Autocorrect text.
     *
     * @return the autocorrect value.
     */
    public boolean autocorrect() {
        return this.autocorrect;
    }

    /**
     * Sets Autocorrect text.
     *
     * @param autocorrect the autocorrect value.
     * @return the service client itself
     */
    public ContentModeratorImageTextClientImpl withAutocorrect(boolean autocorrect) {
        this.autocorrect = autocorrect;
        return this;
    }

    /** Detect personal identifiable information. */
    private boolean pII;

    /**
     * Gets Detect personal identifiable information.
     *
     * @return the pII value.
     */
    public boolean pII() {
        return this.pII;
    }

    /**
     * Sets Detect personal identifiable information.
     *
     * @param pII the pII value.
     * @return the service client itself
     */
    public ContentModeratorImageTextClientImpl withPII(boolean pII) {
        this.pII = pII;
        return this;
    }

    /** Classify input. */
    private boolean classify;

    /**
     * Gets Classify input.
     *
     * @return the classify value.
     */
    public boolean classify() {
        return this.classify;
    }

    /**
     * Sets Classify input.
     *
     * @param classify the classify value.
     * @return the service client itself
     */
    public ContentModeratorImageTextClientImpl withClassify(boolean classify) {
        this.classify = classify;
        return this;
    }

    /** Term to be deleted. */
    private String term;

    /**
     * Gets Term to be deleted.
     *
     * @return the term value.
     */
    public String term() {
        return this.term;
    }

    /**
     * Sets Term to be deleted.
     *
     * @param term the term value.
     * @return the service client itself
     */
    public ContentModeratorImageTextClientImpl withTerm(String term) {
        this.term = term;
        return this;
    }

    /** The image label. */
    private String label;

    /**
     * Gets The image label.
     *
     * @return the label value.
     */
    public String label() {
        return this.label;
    }

    /**
     * Sets The image label.
     *
     * @param label the label value.
     * @return the service client itself
     */
    public ContentModeratorImageTextClientImpl withLabel(String label) {
        this.label = label;
        return this;
    }

    /** Language of the terms. */
    private String language1;

    /**
     * Gets Language of the terms.
     *
     * @return the language1 value.
     */
    public String language1() {
        return this.language1;
    }

    /**
     * Sets Language of the terms.
     *
     * @param language1 the language1 value.
     * @return the service client itself
     */
    public ContentModeratorImageTextClientImpl withLanguage1(String language1) {
        this.language1 = language1;
        return this;
    }

    /** Tag for the image. */
    private double tag;

    /**
     * Gets Tag for the image.
     *
     * @return the tag value.
     */
    public double tag() {
        return this.tag;
    }

    /**
     * Sets Tag for the image.
     *
     * @param tag the tag value.
     * @return the service client itself
     */
    public ContentModeratorImageTextClientImpl withTag(double tag) {
        this.tag = tag;
        return this;
    }

    /** List Id of the image list. */
    private String listId1;

    /**
     * Gets List Id of the image list.
     *
     * @return the listId1 value.
     */
    public String listId1() {
        return this.listId1;
    }

    /**
     * Sets List Id of the image list.
     *
     * @param listId1 the listId1 value.
     * @return the service client itself
     */
    public ContentModeratorImageTextClientImpl withListId1(String listId1) {
        this.listId1 = listId1;
        return this;
    }

    /** The content type. */
    private String contentType;

    /**
     * Gets The content type.
     *
     * @return the contentType value.
     */
    public String contentType() {
        return this.contentType;
    }

    /**
     * Sets The content type.
     *
     * @param contentType the contentType value.
     * @return the service client itself
     */
    public ContentModeratorImageTextClientImpl withContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /** The subscription key in header. */
    private String ocpApimSubscriptionKey;

    /**
     * Gets The subscription key in header.
     *
     * @return the ocpApimSubscriptionKey value.
     */
    public String ocpApimSubscriptionKey() {
        return this.ocpApimSubscriptionKey;
    }

    /**
     * Sets The subscription key in header.
     *
     * @param ocpApimSubscriptionKey the ocpApimSubscriptionKey value.
     * @return the service client itself
     */
    public ContentModeratorImageTextClientImpl withOcpApimSubscriptionKey(String ocpApimSubscriptionKey) {
        this.ocpApimSubscriptionKey = ocpApimSubscriptionKey;
        return this;
    }

    /** Supported Azure regions for Computer Vision endpoints. Possible values include: 'westus', 'westeurope', 'southeastasia', 'eastus2', 'westcentralus'. */
    private AzureRegion azureRegion1;

    /**
     * Gets Supported Azure regions for Computer Vision endpoints. Possible values include: 'westus', 'westeurope', 'southeastasia', 'eastus2', 'westcentralus'.
     *
     * @return the azureRegion1 value.
     */
    public AzureRegion azureRegion1() {
        return this.azureRegion1;
    }

    /**
     * Sets Supported Azure regions for Computer Vision endpoints. Possible values include: 'westus', 'westeurope', 'southeastasia', 'eastus2', 'westcentralus'.
     *
     * @param azureRegion1 the azureRegion1 value.
     * @return the service client itself
     */
    public ContentModeratorImageTextClientImpl withAzureRegion1(AzureRegion azureRegion1) {
        this.azureRegion1 = azureRegion1;
        return this;
    }

    /** Gets or sets the preferred language for the response. */
    private String acceptLanguage;

    /**
     * Gets Gets or sets the preferred language for the response.
     *
     * @return the acceptLanguage value.
     */
    public String acceptLanguage() {
        return this.acceptLanguage;
    }

    /**
     * Sets Gets or sets the preferred language for the response.
     *
     * @param acceptLanguage the acceptLanguage value.
     * @return the service client itself
     */
    public ContentModeratorImageTextClientImpl withAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
        return this;
    }

    /** Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30. */
    private int longRunningOperationRetryTimeout;

    /**
     * Gets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30.
     *
     * @return the longRunningOperationRetryTimeout value.
     */
    public int longRunningOperationRetryTimeout() {
        return this.longRunningOperationRetryTimeout;
    }

    /**
     * Sets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30.
     *
     * @param longRunningOperationRetryTimeout the longRunningOperationRetryTimeout value.
     * @return the service client itself
     */
    public ContentModeratorImageTextClientImpl withLongRunningOperationRetryTimeout(int longRunningOperationRetryTimeout) {
        this.longRunningOperationRetryTimeout = longRunningOperationRetryTimeout;
        return this;
    }

    /** When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true. */
    private boolean generateClientRequestId;

    /**
     * Gets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true.
     *
     * @return the generateClientRequestId value.
     */
    public boolean generateClientRequestId() {
        return this.generateClientRequestId;
    }

    /**
     * Sets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true.
     *
     * @param generateClientRequestId the generateClientRequestId value.
     * @return the service client itself
     */
    public ContentModeratorImageTextClientImpl withGenerateClientRequestId(boolean generateClientRequestId) {
        this.generateClientRequestId = generateClientRequestId;
        return this;
    }

    /**
     * The ImagesInner object to access its operations.
     */
    private ImagesInner images;

    /**
     * Gets the ImagesInner object to access its operations.
     * @return the ImagesInner object.
     */
    public ImagesInner images() {
        return this.images;
    }

    /**
     * The TextsInner object to access its operations.
     */
    private TextsInner texts;

    /**
     * Gets the TextsInner object to access its operations.
     * @return the TextsInner object.
     */
    public TextsInner texts() {
        return this.texts;
    }

    /**
     * The ImageListsInner object to access its operations.
     */
    private ImageListsInner imageLists;

    /**
     * Gets the ImageListsInner object to access its operations.
     * @return the ImageListsInner object.
     */
    public ImageListsInner imageLists() {
        return this.imageLists;
    }

    /**
     * The TermsListsInner object to access its operations.
     */
    private TermsListsInner termsLists;

    /**
     * Gets the TermsListsInner object to access its operations.
     * @return the TermsListsInner object.
     */
    public TermsListsInner termsLists() {
        return this.termsLists;
    }

    /**
     * The TermlistsInner object to access its operations.
     */
    private TermlistsInner termlists;

    /**
     * Gets the TermlistsInner object to access its operations.
     * @return the TermlistsInner object.
     */
    public TermlistsInner termlists() {
        return this.termlists;
    }

    /**
     * The ReviewsInner object to access its operations.
     */
    private ReviewsInner reviews;

    /**
     * Gets the ReviewsInner object to access its operations.
     * @return the ReviewsInner object.
     */
    public ReviewsInner reviews() {
        return this.reviews;
    }

    /**
     * The JobsInner object to access its operations.
     */
    private JobsInner jobs;

    /**
     * Gets the JobsInner object to access its operations.
     * @return the JobsInner object.
     */
    public JobsInner jobs() {
        return this.jobs;
    }

    /**
     * Initializes an instance of ContentModeratorImageTextClient client.
     *
     * @param credentials the management credentials for Azure
     */
    public ContentModeratorImageTextClientImpl(ServiceClientCredentials credentials) {
        this("https://{azureRegion}.api.cognitive.microsoft.com", credentials);
    }

    /**
     * Initializes an instance of ContentModeratorImageTextClient client.
     *
     * @param baseUrl the base URL of the host
     * @param credentials the management credentials for Azure
     */
    private ContentModeratorImageTextClientImpl(String baseUrl, ServiceClientCredentials credentials) {
        super(baseUrl, credentials);
        initialize();
    }

    /**
     * Initializes an instance of ContentModeratorImageTextClient client.
     *
     * @param restClient the REST client to connect to Azure.
     */
    public ContentModeratorImageTextClientImpl(RestClient restClient) {
        super(restClient);
        initialize();
    }

    protected void initialize() {
        this.language = "eng";
        this.enhanced = false;
        this.autocorrect = false;
        this.pII = false;
        this.classify = false;
        this.acceptLanguage = "en-US";
        this.longRunningOperationRetryTimeout = 30;
        this.generateClientRequestId = true;
        this.images = new ImagesInner(restClient().retrofit(), this);
        this.texts = new TextsInner(restClient().retrofit(), this);
        this.imageLists = new ImageListsInner(restClient().retrofit(), this);
        this.termsLists = new TermsListsInner(restClient().retrofit(), this);
        this.termlists = new TermlistsInner(restClient().retrofit(), this);
        this.reviews = new ReviewsInner(restClient().retrofit(), this);
        this.jobs = new JobsInner(restClient().retrofit(), this);
        this.azureClient = new AzureClient(this);
    }

    /**
     * Gets the User-Agent header for the client.
     *
     * @return the user agent string.
     */
    @Override
    public String userAgent() {
        return String.format("%s (%s, %s)", super.userAgent(), "ContentModeratorImageTextClient", "1.0");
    }
}
