// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration;

import com.azure.ai.formrecognizer.FormRecognizerClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisServiceVersion;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.BlobContentSource;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.BlobFileListContentSource;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.BuildDocumentClassifierOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.BuildDocumentModelOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ClassifierDocumentTypeDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ComposeDocumentModelOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ContentSource;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.CopyAuthorizationOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentClassifierDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelBuildMode;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelCopyAuthorization;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelSummary;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationStatus;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationSummary;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ResourceDetails;
import com.azure.ai.formrecognizer.documentanalysis.implementation.DocumentClassifiersImpl;
import com.azure.ai.formrecognizer.documentanalysis.implementation.DocumentModelsImpl;
import com.azure.ai.formrecognizer.documentanalysis.implementation.FormRecognizerClientImpl;
import com.azure.ai.formrecognizer.documentanalysis.implementation.MiscellaneousImpl;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.AuthorizeCopyRequest;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.BuildDocumentClassifierRequest;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.BuildDocumentModelRequest;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.ComposeDocumentModelRequest;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.CopyAuthorization;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentClassifiersBuildClassifierHeaders;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelsBuildModelHeaders;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelsComposeModelHeaders;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelsCopyModelToHeaders;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.ErrorResponseException;
import com.azure.ai.formrecognizer.documentanalysis.implementation.util.Transforms;
import com.azure.ai.formrecognizer.documentanalysis.implementation.util.Utility;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentAnalysisAudience;
import com.azure.ai.formrecognizer.documentanalysis.models.OperationResult;
import com.azure.ai.formrecognizer.training.FormTrainingClient;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.polling.SyncPoller;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Constants.DEFAULT_POLL_INTERVAL;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Transforms.fromInnerCDocumentClassifierDetails;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Transforms.getAuthorizeCopyRequest;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Transforms.getBuildDocumentClassifierRequest;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Transforms.getBuildDocumentModelRequest;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Transforms.getComposeDocumentModelRequest;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Transforms.getHttpResponseException;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Transforms.getInnerCopyAuthorization;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Transforms.toInnerDocTypes;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Utility.getBuildDocumentModelOptions;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Utility.getComposeModelOptions;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Utility.getCopyAuthorizationOptions;

/**
 * <p>This class provides an asynchronous client to connect to the Form Recognizer Azure Cognitive Service.</p>
 * <p>This client provides asynchronous methods to perform:</p>
 *
 * <ol>
 *     <li>Build a custom model: Extract data from your specific documents by building custom models using the
 *     {@link #beginBuildDocumentModel(String, DocumentModelBuildMode) beginBuidlDocumentModel} method to provide a
 *     container SAS URL to your Azure Storage Blob container.</li>
 *     <li>Composed custom models: Creates a new model from document types of collection of existing models using the
 *     {@link #beginComposeDocumentModel(List) beginComposeDocumentModel} method.</li>
 *     <li>Copy custom model: Copy a custom Form Recognizer model to a target Form Recognizer resource using the
 *     {@link #beginCopyDocumentModelTo(String, DocumentModelCopyAuthorization) beginCopyDocumentModelTo} method.</li>
 *     <li>Custom model management: Get detailed information, delete and list custom models using methods
 *     {@link #getDocumentModel(String) getDocumentModel}, {@link #listDocumentModels() listDocumentModels} and
 *     {@link #deleteDocumentModel(String) deleteDocumentModel} respectively.</li>
 *     <li>Operations management: Get detailed information and list operations on the Form Recognizer account using
 *     methods {@link #getOperation(String) getOperation} and {@link #listOperations()} respectively.</li>
 *     <li>Polling and Callbacks: It includes mechanisms for polling the service to check the status of an analysis
 *     operation or registering callbacks to receive notifications when the analysis is complete.</li>
 * </ol>
 *
 *
 * <p><strong>Note: </strong>This client only supports {@link DocumentAnalysisServiceVersion#V2022_08_31} and newer.
 * To use an older service version, {@link FormRecognizerClient} and {@link FormTrainingClient}.</p>
 *
 * <p>Service clients are the point of interaction for developers to use Azure Form Recognizer.
 * {@link DocumentModelAdministrationClient} is the synchronous service client and
 * {@link DocumentModelAdministrationAsyncClient} is the asynchronous service client.
 * The examples shown in this document use a credential object named DefaultAzureCredential for authentication, which is
 * appropriate for most scenarios, including local development and production environments. Additionally, we
 * recommend using
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments.
 * You can find more information on different ways of authenticating and their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation"</a>.
 * </p>
 *
 * <p><strong>Sample: Construct a
 * {@link com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient} with
 * DefaultAzureCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a
 * {@link com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient}, using
 * the `DefaultAzureCredentialBuilder` to configure it.</p>
 *
 * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.initialization -->
 * <pre>
 * DocumentModelAdministrationClient client = new DocumentModelAdministrationClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.initialization  -->
 *
 * <p>Further, see the code sample below to use
 * {@link com.azure.core.credential.AzureKeyCredential AzureKeyCredential} for client creation.</p>
 *
 * <!-- src_embed readme-sample-createDocumentModelAdministrationAsyncClient -->
 * <pre>
 * DocumentModelAdministrationAsyncClient documentModelAdministrationAsyncClient =
 *     new DocumentModelAdministrationClientBuilder&#40;&#41;
 *         .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *         .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *         .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createDocumentModelAdministrationAsyncClient  -->
 *
 * @see com.azure.ai.formrecognizer.documentanalysis.administration
 * @see DocumentModelAdministrationClientBuilder
 * @see DocumentModelAdministrationAsyncClient
 */
@ServiceClient(builder = DocumentModelAdministrationClientBuilder.class)
public final class DocumentModelAdministrationClient {
    private static final ClientLogger LOGGER = new ClientLogger(DocumentModelAdministrationClient.class);
    private final FormRecognizerClientImpl formRecognizerClientImpl;
    private final DocumentModelsImpl documentModelsImpl;
    private final MiscellaneousImpl miscellaneousImpl;
    private final DocumentClassifiersImpl documentClassifiersImpl;
    private final DocumentAnalysisAudience audience;

    /**
     * Create a {@link DocumentModelAdministrationClient} that sends requests to the Form Recognizer service's endpoint.
     * Each service call goes through the {@link DocumentModelAdministrationClientBuilder#pipeline http pipeline}.
     *
     * @param formRecognizerClientImpl The proxy service used to perform REST calls.
     * @param audience ARM management audience associated with the given form recognizer resource.
     *
     */
    DocumentModelAdministrationClient(FormRecognizerClientImpl formRecognizerClientImpl,
        DocumentAnalysisAudience audience) {
        this.formRecognizerClientImpl = formRecognizerClientImpl;
        this.documentModelsImpl = formRecognizerClientImpl.getDocumentModels();
        this.miscellaneousImpl = formRecognizerClientImpl.getMiscellaneous();
        this.documentClassifiersImpl = formRecognizerClientImpl.getDocumentClassifiers();
        this.audience = audience;
    }

    /**
     * Creates a new {@link DocumentAnalysisClient} object. The new {@link DocumentAnalysisClient}
     * uses the same request policy pipeline as the {@link DocumentAnalysisClient}.
     *
     * @return A new {@link DocumentAnalysisClient} object.
     */
    public DocumentAnalysisClient getDocumentAnalysisClient() {
        return new DocumentAnalysisClientBuilder().endpoint(formRecognizerClientImpl.getEndpoint())
            .pipeline(formRecognizerClientImpl.getHttpPipeline())
            .audience(this.audience)
            .buildClient();
    }

    /**
     * Builds a custom document analysis model.
     * <p>Models are built using documents that are of the following content
     * type - 'application/pdf', 'image/jpeg', 'image/png', 'image/tiff', image/bmp.
     * Other type of content is ignored.
     * </p>
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginBuildDocumentModel#String-BuildMode -->
     * <pre>
     * String blobContainerUrl = &quot;&#123;SAS-URL-of-your-container-in-blob-storage&#125;&quot;;
     * DocumentModelDetails documentModelDetails
     *     = documentModelAdministrationClient.beginBuildDocumentModel&#40;blobContainerUrl,
     *         DocumentModelBuildMode.TEMPLATE&#41;
     *     .getFinalResult&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModelDetails.getModelId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModelDetails.getCreatedOn&#40;&#41;&#41;;
     * documentModelDetails.getDocumentTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *     documentTypeDetails.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *         System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *         System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Field confidence: %.2f&quot;, documentTypeDetails.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginBuildDocumentModel#String-BuildMode -->
     *
     * @param blobContainerUrl an Azure Storage blob container's SAS URI. A container URI (without SAS)
     * can be used if the container is public or has a managed identity configured. For more information on
     * setting up a training data set, see: <a href="https://aka.ms/azsdk/formrecognizer/buildcustommodel">here</a>.
     * @param buildMode the preferred technique for creating models. For faster training of models use
     * {@link DocumentModelBuildMode#TEMPLATE}. See <a href="https://aka.ms/azsdk/formrecognizer/buildmode">here</a>
     * for more information on building mode for custom documents.
     * @return A {@link SyncPoller} that polls the building model operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns the trained {@link DocumentModelDetails custom document analysis model}.
     * @throws HttpResponseException If building model fails with {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If {@code blobContainerUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<OperationResult, DocumentModelDetails> beginBuildDocumentModel(
        String blobContainerUrl, DocumentModelBuildMode buildMode) {
        return beginBuildDocumentModel(blobContainerUrl, buildMode, null, null, Context.NONE);
    }

    /**
     * Builds a custom document analysis model.
     * <p>Models are built using documents that are of the following content
     * type - 'application/pdf', 'image/jpeg', 'image/png', 'image/tiff', image/bmp.
     * Other type of content is ignored.
     * </p>
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginBuildDocumentModel#String-BuildMode-String-Options-Context -->
     * <pre>
     * String blobContainerUrl = &quot;&#123;SAS-URL-of-your-container-in-blob-storage&#125;&quot;;
     * String modelId = &quot;custom-model-id&quot;;
     * String prefix = &quot;Invoice&quot;;
     * Map&lt;String, String&gt; attrs = new HashMap&lt;String, String&gt;&#40;&#41;;
     * attrs.put&#40;&quot;createdBy&quot;, &quot;sample&quot;&#41;;
     *
     * DocumentModelDetails documentModelDetails
     *     = documentModelAdministrationClient.beginBuildDocumentModel&#40;blobContainerUrl,
     *         DocumentModelBuildMode.TEMPLATE,
     *         prefix,
     *         new BuildDocumentModelOptions&#40;&#41;
     *             .setModelId&#40;modelId&#41;
     *             .setDescription&#40;&quot;model desc&quot;&#41;
     *             .setTags&#40;attrs&#41;,
     *         Context.NONE&#41;
     *     .getFinalResult&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModelDetails.getModelId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Description: %s%n&quot;, documentModelDetails.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModelDetails.getCreatedOn&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model assigned tags: %s%n&quot;, documentModelDetails.getTags&#40;&#41;&#41;;
     * documentModelDetails.getDocumentTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *     documentTypeDetails.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *         System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *         System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Field confidence: %.2f&quot;, documentTypeDetails.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginBuildDocumentModel#String-BuildMode-String-Options-Context -->
     *
     * @param blobContainerUrl an Azure Storage blob container's SAS URI. A container URI (without SAS)
     * can be used if the container is public or has a managed identity configured. For more information on
     * setting up a training data set, see: <a href="https://aka.ms/azsdk/formrecognizer/buildcustommodel">here</a>.
     * @param buildMode the preferred technique for creating models. For faster training of models use
     * {@link DocumentModelBuildMode#TEMPLATE}. See <a href="https://aka.ms/azsdk/formrecognizer/buildmode">here</a>
     * for more information on building mode for custom documents.
     * @param prefix case-sensitive prefix blob name prefix to filter documents for training.
     * @param buildDocumentModelOptions The configurable {@link BuildDocumentModelOptions options} to pass when
     * building a custom document analysis model.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link SyncPoller} that polls the building model operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns the built {@link DocumentModelDetails custom document analysis model}.
     * @throws HttpResponseException If building the model fails with {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If {@code blobContainerUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<OperationResult, DocumentModelDetails> beginBuildDocumentModel(String blobContainerUrl,
        DocumentModelBuildMode buildMode, String prefix, BuildDocumentModelOptions buildDocumentModelOptions,
        Context context) {
        Objects.requireNonNull(blobContainerUrl, "'blobContainerUrl' cannot be null.");
        return beginBuildDocumentModelSync(new BlobContentSource(blobContainerUrl).setPrefix(prefix), buildMode,
            buildDocumentModelOptions, context);
    }

    /**
     * Builds a custom document analysis model.
     * <p>Models are built using documents that are of the following content
     * type - 'application/pdf', 'image/jpeg', 'image/png', 'image/tiff', image/bmp.
     * Other type of content is ignored.
     * </p>
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginBuildDocumentModel#ContentSource-BuildMode -->
     * <pre>
     * String blobContainerUrl = &quot;&#123;SAS-URL-of-your-container-in-blob-storage&#125;&quot;;
     * String fileList = &quot;&quot;;
     *
     * DocumentModelDetails documentModelDetails
     *     = documentModelAdministrationClient.beginBuildDocumentModel&#40;
     *         new BlobFileListContentSource&#40;blobContainerUrl, fileList&#41;,
     *         DocumentModelBuildMode.TEMPLATE&#41;
     *     .getFinalResult&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModelDetails.getModelId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModelDetails.getCreatedOn&#40;&#41;&#41;;
     * documentModelDetails.getDocumentTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *     documentTypeDetails.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *         System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *         System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Field confidence: %.2f&quot;, documentTypeDetails.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginBuildDocumentModel#ContentSource-BuildMode -->
     *
     * @param contentSource training data source to be used for building the model. It can be an Azure
     * Storage blob container's provided along with its respective prefix or Path to a JSONL file within the
     * container specifying the set of documents for training. For more information on
     * setting up a training data set, see: <a href="https://aka.ms/azsdk/formrecognizer/buildcustommodel">here</a>.
     * @param buildMode the preferred technique for creating models. For faster training of models use
     * {@link DocumentModelBuildMode#TEMPLATE}. See <a href="https://aka.ms/azsdk/formrecognizer/buildmode">here</a>
     * for more information on building mode for custom documents.
     * been cancelled. The completed operation returns the built {@link DocumentModelDetails custom document analysis model}.
     * @return A {@link SyncPoller} that polls the building model operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns the built {@link DocumentModelDetails custom document analysis model}.
     * @throws HttpResponseException If building the model fails with {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If {@code blobContainerUrl} and {@code fileList} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<OperationResult, DocumentModelDetails> beginBuildDocumentModel(
        ContentSource contentSource, DocumentModelBuildMode buildMode) {
        return beginBuildDocumentModel(contentSource, buildMode, null, Context.NONE);
    }

    /**
     * Builds a custom document analysis model.
     * <p>Models are built using documents that are of the following content
     * type - 'application/pdf', 'image/jpeg', 'image/png', 'image/tiff', image/bmp.
     * Other type of content is ignored.
     * </p>
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginBuildDocumentModel#ContentSource-BuildMode-Options-Context -->
     * <pre>
     * String blobContainerUrl = &quot;&#123;SAS-URL-of-your-container-in-blob-storage&#125;&quot;;
     * String fileList = &quot;&quot;;
     * String modelId = &quot;custom-model-id&quot;;
     * Map&lt;String, String&gt; attrs = new HashMap&lt;String, String&gt;&#40;&#41;;
     * attrs.put&#40;&quot;createdBy&quot;, &quot;sample&quot;&#41;;
     *
     * DocumentModelDetails documentModelDetails
     *     = documentModelAdministrationClient.beginBuildDocumentModel&#40;
     *         new BlobFileListContentSource&#40;blobContainerUrl, fileList&#41;,
     *         DocumentModelBuildMode.TEMPLATE,
     *         new BuildDocumentModelOptions&#40;&#41;
     *             .setModelId&#40;modelId&#41;
     *             .setDescription&#40;&quot;model desc&quot;&#41;
     *             .setTags&#40;attrs&#41;,
     *         Context.NONE&#41;
     *     .getFinalResult&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModelDetails.getModelId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Description: %s%n&quot;, documentModelDetails.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModelDetails.getCreatedOn&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model assigned tags: %s%n&quot;, documentModelDetails.getTags&#40;&#41;&#41;;
     * documentModelDetails.getDocumentTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *     documentTypeDetails.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *         System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *         System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Field confidence: %.2f&quot;, documentTypeDetails.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginBuildDocumentModel#ContentSource-BuildMode-Options-Context -->
     *
     * @param contentSource training data source to be used for building the model. It can be an Azure
     * Storage blob container's provided along with its respective prefix or Path to a JSONL file within the
     * container specifying the set of documents for training. For more information on
     * setting up a training data set, see: <a href="https://aka.ms/azsdk/formrecognizer/buildcustommodel">here</a>.
     * @param buildMode the preferred technique for creating models. For faster training of models use
     * {@link DocumentModelBuildMode#TEMPLATE}. See <a href="https://aka.ms/azsdk/formrecognizer/buildmode">here</a>
     * for more information on building mode for custom documents.
     * @param buildDocumentModelOptions The configurable {@link BuildDocumentModelOptions options} to pass when
     * building a custom document analysis model.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link SyncPoller} that polls the building model operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns the built {@link DocumentModelDetails custom document analysis model}.
     * @throws HttpResponseException If building the model fails with {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If {@code blobContainerUrl} and {@code fileList} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<OperationResult, DocumentModelDetails> beginBuildDocumentModel(ContentSource contentSource,
        DocumentModelBuildMode buildMode, BuildDocumentModelOptions buildDocumentModelOptions, Context context) {
        return beginBuildDocumentModelSync(contentSource, buildMode, buildDocumentModelOptions, context);
    }

    private SyncPoller<OperationResult, DocumentModelDetails> beginBuildDocumentModelSync(ContentSource contentSource,
        DocumentModelBuildMode buildMode, BuildDocumentModelOptions buildDocumentModelOptions, Context context) {

        BuildDocumentModelOptions finalBuildDocumentModelOptions
            = getBuildDocumentModelOptions(buildDocumentModelOptions);
        String modelId = finalBuildDocumentModelOptions.getModelId();
        if (modelId == null) {
            modelId = Utility.generateRandomModelID();
        }
        String finalModelId = modelId;
        if (contentSource instanceof BlobFileListContentSource) {
            BlobFileListContentSource blobFileListContentSource = (BlobFileListContentSource) contentSource;
            Objects.requireNonNull(blobFileListContentSource.getContainerUrl(), "'blobContainerUrl' is required.");
            Objects.requireNonNull(blobFileListContentSource.getFileList(), "'fileList' is required.");
        }
        if (contentSource instanceof BlobContentSource) {
            BlobContentSource blobContentSource = (BlobContentSource) contentSource;
            Objects.requireNonNull(blobContentSource.getContainerUrl(), "'blobContainerUrl' is required.");
        }
        return SyncPoller.createPoller(
            DEFAULT_POLL_INTERVAL,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, buildModelActivationOperation(
                contentSource, buildMode, finalModelId, finalBuildDocumentModelOptions, context).apply(cxt)),
            buildModelPollingOperation(context), getCancellationIsNotSupported(),
            buildModelFetchingOperation(context));
    }

    /**
     * Get information about the current Form Recognizer resource.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getResourceDetails -->
     * <pre>
     * ResourceDetails resourceDetails = documentModelAdministrationClient.getResourceDetails&#40;&#41;;
     * System.out.printf&#40;&quot;Max number of models that can be build for this account: %d%n&quot;,
     *     resourceDetails.getCustomDocumentModelLimit&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Current count of built document analysis models: %d%n&quot;,
     *     resourceDetails.getCustomDocumentModelCount&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getResourceDetails -->
     *
     * @return The requested resource information details.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ResourceDetails getResourceDetails() {
        return getResourceDetailsWithResponse(Context.NONE).getValue();
    }

    /**
     * Get information about the current Form recognizer resource with a Http response and a
     * specified {@link Context}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getResourceDetailsWithResponse#Context -->
     * <pre>
     * Response&lt;ResourceDetails&gt; response =
     *     documentModelAdministrationClient.getResourceDetailsWithResponse&#40;Context.NONE&#41;;
     * System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     * ResourceDetails resourceDetails = response.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Max number of models that can be build for this account: %d%n&quot;,
     *     resourceDetails.getCustomDocumentModelLimit&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Current count of built document analysis models: %d%n&quot;,
     *     resourceDetails.getCustomDocumentModelCount&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getResourceDetailsWithResponse#Context -->
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The requested resource information details.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ResourceDetails> getResourceDetailsWithResponse(Context context) {
        try {
            Response<com.azure.ai.formrecognizer.documentanalysis.implementation.models.ResourceDetails> response =
                miscellaneousImpl.getResourceInfoWithResponse(context);

            return new SimpleResponse<>(response, Transforms.toAccountProperties(response.getValue()));
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    /**
     * Deletes the specified custom document analysis model.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.deleteDocumentModel#string -->
     * <pre>
     * String modelId = &quot;&#123;custom-model-id&#125;&quot;;
     * documentModelAdministrationClient.deleteDocumentModel&#40;modelId&#41;;
     * System.out.printf&#40;&quot;Model ID: %s is deleted.%n&quot;, modelId&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.deleteDocumentModel#string -->
     *
     * @param modelId The unique model identifier.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteDocumentModel(String modelId) {
        deleteDocumentModelWithResponse(modelId, Context.NONE);
    }

    /**
     * Deletes the specified custom document analysis model.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.deleteDocumentModelWithResponse#string-Context -->
     * <pre>
     * String modelId = &quot;&#123;custom-model-id&#125;&quot;;
     * Response&lt;Void&gt; response
     *     = documentModelAdministrationClient.deleteDocumentModelWithResponse&#40;modelId, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model ID: %s is deleted.%n&quot;, modelId&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.deleteDocumentModelWithResponse#string-Context -->
     *
     * @param modelId The unique model identifier.
     * @param context Additional context that is passed through the Http pipeline during the service call.

     * @return A {@link Response} containing status code and HTTP headers.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteDocumentModelWithResponse(String modelId, Context context) {
        if (CoreUtils.isNullOrEmpty(modelId)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "'modelId' is required and cannot be null or empty"));
        }

        try {
            return
                documentModelsImpl.deleteModelWithResponse(modelId, context);
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    /**
     * Generate authorization for copying a custom model into the target Form Recognizer resource.
     * <p> This should be called by the target resource (where the model will be copied to) and the output can be passed as
     * the target parameter into {@link DocumentModelAdministrationClient#beginCopyDocumentModelTo(String, DocumentModelCopyAuthorization)}.
     * </p>
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getCopyAuthorization -->
     * <pre>
     * DocumentModelCopyAuthorization documentModelCopyAuthorization
     *     = documentModelAdministrationClient.getCopyAuthorization&#40;&#41;;
     * System.out.printf&#40;&quot;Copy Authorization for model id: %s, access token: %s, expiration time: %s, &quot;
     *         + &quot;target resource ID; %s, target resource region: %s%n&quot;,
     *     documentModelCopyAuthorization.getTargetModelId&#40;&#41;,
     *     documentModelCopyAuthorization.getAccessToken&#40;&#41;,
     *     documentModelCopyAuthorization.getExpiresOn&#40;&#41;,
     *     documentModelCopyAuthorization.getTargetResourceId&#40;&#41;,
     *     documentModelCopyAuthorization.getTargetResourceRegion&#40;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getCopyAuthorization -->
     * @return The {@link DocumentModelCopyAuthorization}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentModelCopyAuthorization getCopyAuthorization() {
        return getCopyAuthorizationWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Generate authorization for copying a custom model into the target Form Recognizer resource.
     * <p> This should be called by the target resource (where the model will be copied to) and the output can be passed as
     * the target parameter into {@link DocumentModelAdministrationClient#beginCopyDocumentModelTo(String, DocumentModelCopyAuthorization)}.
     * </p>
     *
     * @param copyAuthorizationOptions The configurable {@link CopyAuthorizationOptions options} to pass when
     * copying a model.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getCopyAuthorizationWithResponse#Options-Context -->
     * <pre>
     * String modelId = &quot;my-copied-model&quot;;
     * Map&lt;String, String&gt; attrs = new HashMap&lt;String, String&gt;&#40;&#41;;
     * attrs.put&#40;&quot;createdBy&quot;, &quot;sample&quot;&#41;;
     *
     * Response&lt;DocumentModelCopyAuthorization&gt; copyAuthorizationResponse =
     *     documentModelAdministrationClient.getCopyAuthorizationWithResponse&#40;
     *         new CopyAuthorizationOptions&#40;&#41;
     *             .setModelId&#40;modelId&#41;
     *             .setDescription&#40;&quot;model-desc&quot;&#41;
     *             .setTags&#40;attrs&#41;,
     *         Context.NONE&#41;;
     *
     * System.out.printf&#40;&quot;Copy Authorization operation returned with status: %s&quot;,
     *     copyAuthorizationResponse.getStatusCode&#40;&#41;&#41;;
     * DocumentModelCopyAuthorization documentModelCopyAuthorization = copyAuthorizationResponse.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Copy Authorization for model id: %s, access token: %s, &quot;
     *         + &quot;expiration time: %s, target resource ID; %s, target resource region: %s%n&quot;,
     *     documentModelCopyAuthorization.getTargetModelId&#40;&#41;,
     *     documentModelCopyAuthorization.getAccessToken&#40;&#41;,
     *     documentModelCopyAuthorization.getExpiresOn&#40;&#41;,
     *     documentModelCopyAuthorization.getTargetResourceId&#40;&#41;,
     *     documentModelCopyAuthorization.getTargetResourceRegion&#40;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getCopyAuthorizationWithResponse#Options-Context -->
     *
     * @return A {@link Response} containing the {@link DocumentModelCopyAuthorization}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentModelCopyAuthorization> getCopyAuthorizationWithResponse(
        CopyAuthorizationOptions copyAuthorizationOptions, Context context) {
        copyAuthorizationOptions = getCopyAuthorizationOptions(copyAuthorizationOptions);
        String modelId = copyAuthorizationOptions.getModelId();
        modelId = modelId == null ? Utility.generateRandomModelID() : modelId;

        AuthorizeCopyRequest authorizeCopyRequest = getAuthorizeCopyRequest(copyAuthorizationOptions, modelId);

        try {
            Response<CopyAuthorization> response = documentModelsImpl.authorizeModelCopyWithResponse(
                authorizeCopyRequest, context);

            return new SimpleResponse<>(response, Transforms.toCopyAuthorization(response.getValue()));
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    /**
     * Create a composed model from the provided list of existing models in the account.
     *
     * <p>This operations fails if the list consists of an invalid, non-existing model Id or duplicate IDs.
     * </p>
     *
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginComposeDocumentModel#list -->
     * <pre>
     * String modelId1 = &quot;&#123;custom-model-id_1&#125;&quot;;
     * String modelId2 = &quot;&#123;custom-model-id_2&#125;&quot;;
     * final DocumentModelDetails documentModelDetails
     *     = documentModelAdministrationClient.beginComposeDocumentModel&#40;Arrays.asList&#40;modelId1, modelId2&#41;&#41;
     *     .getFinalResult&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModelDetails.getModelId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Description: %s%n&quot;, documentModelDetails.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModelDetails.getCreatedOn&#40;&#41;&#41;;
     * documentModelDetails.getDocumentTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *     documentTypeDetails.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *         System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *         System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Field confidence: %.2f&quot;, documentTypeDetails.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginComposeDocumentModel#list -->
     *
     * @param componentModelIds The list of models IDs to form the composed model.
     * @return A {@link SyncPoller} that polls the create composed model operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns the {@link DocumentModelDetails composed model}.
     * @throws HttpResponseException If create composed model operation fails and model with
     * {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If the list of {@code componentModelIds} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<OperationResult, DocumentModelDetails> beginComposeDocumentModel(List<String> componentModelIds) {
        return beginComposeDocumentModel(componentModelIds, null, Context.NONE);
    }

    /**
     * Create a composed model from the provided list of existing models in the account.
     *
     * <p>This operations fails if the list consists of an invalid, non-existing model Id or duplicate IDs.
     * </p>
     *
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginComposeDocumentModel#list-Options-Context -->
     * <pre>
     * String modelId1 = &quot;&#123;custom-model-id_1&#125;&quot;;
     * String modelId2 = &quot;&#123;custom-model-id_2&#125;&quot;;
     * String modelId = &quot;my-composed-model&quot;;
     * Map&lt;String, String&gt; attrs = new HashMap&lt;String, String&gt;&#40;&#41;;
     * attrs.put&#40;&quot;createdBy&quot;, &quot;sample&quot;&#41;;
     *
     * final DocumentModelDetails documentModelDetails =
     *     documentModelAdministrationClient.beginComposeDocumentModel&#40;Arrays.asList&#40;modelId1, modelId2&#41;,
     *             new ComposeDocumentModelOptions&#40;&#41;
     *                 .setModelId&#40;modelId&#41;
     *                 .setDescription&#40;&quot;my composed model desc&quot;&#41;
     *                 .setTags&#40;attrs&#41;,
     *             Context.NONE&#41;
     *         .setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;
     *         .getFinalResult&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModelDetails.getModelId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Description: %s%n&quot;, documentModelDetails.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModelDetails.getCreatedOn&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model assigned tags: %s%n&quot;, documentModelDetails.getTags&#40;&#41;&#41;;
     * documentModelDetails.getDocumentTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *     documentTypeDetails.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *         System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *         System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Field confidence: %.2f&quot;, documentTypeDetails.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginComposeDocumentModel#list-Options-Context -->
     *
     * @param componentModelIds The list of models IDs to form the composed model.
     * @param composeDocumentModelOptions The configurable {@link ComposeDocumentModelOptions options} to pass when
     * creating a composed model.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the create composed model operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns the {@link DocumentModelDetails composed model}.
     * @throws HttpResponseException If create composed model operation fails and model with
     * {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If the list of {@code componentModelIds} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<OperationResult, DocumentModelDetails> beginComposeDocumentModel(List<String> componentModelIds,
        ComposeDocumentModelOptions composeDocumentModelOptions, Context context) {
        return beginComposeDocumentModelSync(componentModelIds, composeDocumentModelOptions, context);
    }

    private SyncPoller<OperationResult, DocumentModelDetails> beginComposeDocumentModelSync(
        List<String> componentModelIds, ComposeDocumentModelOptions composeDocumentModelOptions, Context context) {
        if (CoreUtils.isNullOrEmpty(componentModelIds)) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'componentModelIds' cannot be null or empty"));
        }

        composeDocumentModelOptions = getComposeModelOptions(composeDocumentModelOptions);
        String modelId = composeDocumentModelOptions.getModelId();
        modelId = modelId == null ? Utility.generateRandomModelID() : modelId;

        final ComposeDocumentModelRequest composeRequest =
            getComposeDocumentModelRequest(componentModelIds, composeDocumentModelOptions, modelId);

        return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, composeModelActivationOperation(
                composeRequest, context).apply(cxt)), buildModelPollingOperation(context),
            getCancellationIsNotSupported(), buildModelFetchingOperation(context));
    }

    /**
     * Copy a custom model stored in this resource (the source) to the user specified target Form Recognizer resource.
     *
     * <p>This should be called with the source Form Recognizer resource (with the model that is intended to be copied).
     * The target parameter should be supplied from the target resource's output from
     * {@link DocumentModelAdministrationClient#getCopyAuthorization()} method.
     * </p>
     *
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginCopyDocumentModelTo#string-copyAuthorization -->
     * <pre>
     * String copyModelId = &quot;copy-model&quot;;
     * &#47;&#47; Get authorization to copy the model to target resource
     * DocumentModelCopyAuthorization documentModelCopyAuthorization
     *     = documentModelAdministrationClient.getCopyAuthorization&#40;&#41;;
     * &#47;&#47; Start copy operation from the source client
     * DocumentModelDetails documentModelDetails
     *     = documentModelAdministrationClient.beginCopyDocumentModelTo&#40;copyModelId, documentModelCopyAuthorization&#41;
     *         .getFinalResult&#40;&#41;;
     * System.out.printf&#40;&quot;Copied model has model ID: %s, was created on: %s.%n,&quot;,
     *     documentModelDetails.getModelId&#40;&#41;,
     *     documentModelDetails.getCreatedOn&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginCopyDocumentModelTo#string-copyAuthorization -->
     *
     * @param sourceModelId Model identifier of the source model to copy to target resource.
     * @param target the copy authorization to the target Form Recognizer resource. The copy authorization can be
     * generated from the target resource's call to {@link DocumentModelAdministrationClient#getCopyAuthorization()}
     *
     * @return A {@link SyncPoller} that polls the copy model operation until it has completed, has failed,
     * or has been cancelled.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<OperationResult, DocumentModelDetails> beginCopyDocumentModelTo(String sourceModelId,
        DocumentModelCopyAuthorization target) {
        return beginCopyDocumentModelTo(sourceModelId, target, Context.NONE);
    }

    /**
     * Copy a custom model stored in this resource (the source) to the user specified target Form Recognizer resource.
     *
     * <p>This should be called with the source Form Recognizer resource (with the model that is intended to be copied).
     * The target parameter should be supplied from the target resource's output from
     * {@link DocumentModelAdministrationClient#getCopyAuthorization()} method.
     * </p>
     *
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginCopyDocumentModelTo#string-copyAuthorization-Context -->
     * <pre>
     * String copyModelId = &quot;copy-model&quot;;
     * &#47;&#47; Get authorization to copy the model to target resource
     * DocumentModelCopyAuthorization documentModelCopyAuthorization
     *     = documentModelAdministrationClient.getCopyAuthorization&#40;&#41;;
     * &#47;&#47; Start copy operation from the source client
     * DocumentModelDetails documentModelDetails =
     *     documentModelAdministrationClient.beginCopyDocumentModelTo&#40;copyModelId,
     *             documentModelCopyAuthorization,
     *             Context.NONE&#41;
     *         .getFinalResult&#40;&#41;;
     * System.out.printf&#40;&quot;Copied model has model ID: %s, was created on: %s.%n,&quot;,
     *     documentModelDetails.getModelId&#40;&#41;,
     *     documentModelDetails.getCreatedOn&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginCopyDocumentModelTo#string-copyAuthorization-Context -->
     *
     * @param sourceModelId Model identifier of the model to copy to target resource.
     * @param target the copy authorization to the target Form Recognizer resource. The copy authorization can be
     * generated from the target resource's call to {@link DocumentModelAdministrationClient#getCopyAuthorization()}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the copy model operation until it has completed, has failed,
     * or has been cancelled.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<OperationResult, DocumentModelDetails> beginCopyDocumentModelTo(String sourceModelId,
        DocumentModelCopyAuthorization target, Context context) {
        return beginCopyDocumentModelToSync(sourceModelId, target, context);
    }

    private SyncPoller<OperationResult, DocumentModelDetails> beginCopyDocumentModelToSync(String sourceModelId,
        DocumentModelCopyAuthorization target, Context context) {

        return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, getCopyActivationOperation(sourceModelId,
                target, context).apply(cxt)),
            buildModelPollingOperation(context), getCancellationIsNotSupported(),
            buildModelFetchingOperation(context));
    }

    /**
     * List information for each model on the Form Recognizer account that were built successfully.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listDocumentModels -->
     * <pre>
     * documentModelAdministrationClient.listDocumentModels&#40;&#41;
     *     .forEach&#40;documentModel -&gt;
     *         System.out.printf&#40;&quot;Model ID: %s, Model description: %s, Created on: %s.%n&quot;,
     *             documentModel.getModelId&#40;&#41;,
     *             documentModel.getDescription&#40;&#41;,
     *             documentModel.getCreatedOn&#40;&#41;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listDocumentModels -->
     *
     * @return {@link PagedIterable} of {@link DocumentModelSummary} custom form model information.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DocumentModelSummary> listDocumentModels() {
        return listDocumentModels(Context.NONE);
    }

    /**
     * List information for each model on the Form Recognizer account that were built successfully with a Http
     * response and a specified {@link Context}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listDocumentModels#Context -->
     * <pre>
     * documentModelAdministrationClient.listDocumentModels&#40;Context.NONE&#41;
     *     .forEach&#40;documentModel -&gt;
     *         System.out.printf&#40;&quot;Model ID: %s, Model description: %s, Created on: %s.%n&quot;,
     *             documentModel.getModelId&#40;&#41;,
     *             documentModel.getDescription&#40;&#41;,
     *             documentModel.getCreatedOn&#40;&#41;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listDocumentModels#Context -->
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return {@link PagedIterable} of {@link DocumentModelSummary} custom form model information.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DocumentModelSummary> listDocumentModels(Context context) {
        return listDocumentModelsSync(context);
    }

    private PagedIterable<DocumentModelSummary> listDocumentModelsSync(Context context) {
        return new PagedIterable<>(() -> listFirstPageModelInfo(context),
            continuationToken -> listNextPageModelInfo(continuationToken, context));
    }

    private PagedResponse<DocumentModelSummary> listFirstPageModelInfo(Context context) {
        try {
            PagedResponse<com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelSummary> res =
                documentModelsImpl.listModelsSinglePage(context);
            return new PagedResponseBase<>(res.getRequest(), res.getStatusCode(), res.getHeaders(),
                Transforms.toDocumentModelInfo(res.getValue()), res.getContinuationToken(), null);
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    private PagedResponse<DocumentModelSummary> listNextPageModelInfo(String nextPageLink, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return null;
        }
        try {
            PagedResponse<com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelSummary> res =
                documentModelsImpl.listModelsNextSinglePage(nextPageLink, context);
            return new PagedResponseBase<>(res.getRequest(), res.getStatusCode(), res.getHeaders(),
                Transforms.toDocumentModelInfo(res.getValue()), res.getContinuationToken(), null);
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    /**
     * Get detailed information for a specified model ID.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getDocumentModel#string -->
     * <pre>
     * String modelId = &quot;&#123;custom-model-id&#125;&quot;;
     * DocumentModelDetails documentModelDetails = documentModelAdministrationClient.getDocumentModel&#40;modelId&#41;;
     * System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModelDetails.getModelId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Description: %s%n&quot;, documentModelDetails.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModelDetails.getCreatedOn&#40;&#41;&#41;;
     * documentModelDetails.getDocumentTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *     documentTypeDetails.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *         System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *         System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Field confidence: %.2f&quot;, documentTypeDetails.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getDocumentModel#string -->
     *
     * @param modelId The unique model identifier.
     *
     * @return The detailed information for the specified model.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentModelDetails getDocumentModel(String modelId) {
        return getDocumentModelWithResponse(modelId, Context.NONE).getValue();
    }

    /**
     * Get detailed information for a specified model ID.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getDocumentModelWithResponse#string-Context -->
     * <pre>
     * String modelId = &quot;&#123;custom-model-id&#125;&quot;;
     * Response&lt;DocumentModelDetails&gt; response
     *     = documentModelAdministrationClient.getDocumentModelWithResponse&#40;modelId, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     * DocumentModelDetails documentModelDetails = response.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModelDetails.getModelId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Description: %s%n&quot;, documentModelDetails.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModelDetails.getCreatedOn&#40;&#41;&#41;;
     * documentModelDetails.getDocumentTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *     documentTypeDetails.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *         System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *         System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Field confidence: %.2f&quot;, documentTypeDetails.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getDocumentModelWithResponse#string-Context -->
     *
     * @param modelId The unique model identifier.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The detailed information for the specified model.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentModelDetails> getDocumentModelWithResponse(String modelId, Context context) {
        if (CoreUtils.isNullOrEmpty(modelId)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "'modelId' is required and cannot be null or empty"));
        }
        try {
            Response<com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelDetails> response =
                documentModelsImpl.getModelWithResponse(modelId, context);

            return new SimpleResponse<>(response, Transforms.toDocumentModelDetails(response.getValue()));
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    /**
     * Get detailed operation information for the specified ID with Http response.
     * <p> This operations fails if the operation ID used is past 24 hours.</p>
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getOperation#string -->
     * <pre>
     * String operationId = &quot;&#123;operation-id&#125;&quot;;
     * OperationDetails operationDetails
     *     = documentModelAdministrationClient.getOperation&#40;operationId&#41;;
     * System.out.printf&#40;&quot;Operation ID: %s%n&quot;, operationDetails.getOperationId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Operation Kind: %s%n&quot;, operationDetails.getKind&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Operation Status: %s%n&quot;, operationDetails.getStatus&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model ID created with this operation: %s%n&quot;,
     *     &#40;&#40;DocumentModelBuildOperationDetails&#41; operationDetails&#41;.getResult&#40;&#41;.getModelId&#40;&#41;&#41;;
     * if &#40;OperationStatus.FAILED.equals&#40;operationDetails.getStatus&#40;&#41;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Operation fail error: %s%n&quot;, operationDetails.getError&#40;&#41;.getMessage&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getOperation#string -->
     *
     * @param operationId Unique operation ID.
     *
     * @return The detailed information for the specified operation.
     * @throws IllegalArgumentException If {@code operationId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public OperationDetails getOperation(String operationId) {
        return getOperationWithResponse(operationId, Context.NONE).getValue();
    }

    /**
     * Get detailed operation information for the specified ID with Http response.
     * <p> This operations fails if the operation ID used is past 24 hours.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getOperationWithResponse#string-Context -->
     * <pre>
     * String operationId = &quot;&#123;operation-id&#125;&quot;;
     * Response&lt;OperationDetails&gt; response =
     *     documentModelAdministrationClient.getOperationWithResponse&#40;operationId, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     * OperationDetails operationDetails = response.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Operation ID: %s%n&quot;, operationDetails.getOperationId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Operation Kind: %s%n&quot;, operationDetails.getKind&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Operation Status: %s%n&quot;, operationDetails.getStatus&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model ID created with this operation: %s%n&quot;,
     *     &#40;&#40;DocumentModelBuildOperationDetails&#41; operationDetails&#41;.getResult&#40;&#41;.getModelId&#40;&#41;&#41;;
     * if &#40;OperationStatus.FAILED.equals&#40;operationDetails.getStatus&#40;&#41;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Operation fail error: %s%n&quot;, operationDetails.getError&#40;&#41;.getMessage&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getOperationWithResponse#string-Context -->
     *
     * @param operationId Unique operation ID.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The detailed information for the specified operation.
     * @throws IllegalArgumentException If {@code operationId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<OperationDetails> getOperationWithResponse(String operationId, Context context) {
        if (CoreUtils.isNullOrEmpty(operationId)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "'operationId' is required and cannot be null or empty"));
        }
        try {
            Response<com.azure.ai.formrecognizer.documentanalysis.implementation.models.OperationDetails> response =
                miscellaneousImpl.getOperationWithResponse(operationId, context);

            return new SimpleResponse<>(response, Transforms.toOperationDetails(response.getValue()));
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    /**
     * List information for each model operation on the Form Recognizer account in the past 24 hours.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listOperations -->
     * <pre>
     * PagedIterable&lt;OperationSummary&gt;
     *     modelOperationInfo = documentModelAdministrationClient.listOperations&#40;&#41;;
     * modelOperationInfo.forEach&#40;modelOperationSummary -&gt; &#123;
     *     System.out.printf&#40;&quot;Operation ID: %s%n&quot;, modelOperationSummary.getOperationId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Status: %s%n&quot;, modelOperationSummary.getStatus&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Created on: %s%n&quot;, modelOperationSummary.getCreatedOn&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Percent completed: %d%n&quot;, modelOperationSummary.getPercentCompleted&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Kind: %s%n&quot;, modelOperationSummary.getKind&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Last updated on: %s%n&quot;, modelOperationSummary.getLastUpdatedOn&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation resource location: %s%n&quot;, modelOperationSummary.getResourceLocation&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listOperations -->
     *
     * @return {@link PagedIterable} of {@link OperationSummary} custom form model information.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<OperationSummary> listOperations() {
        return listOperations(Context.NONE);
    }

    /**
     * List information for each operation on the Form Recognizer account in the past 24 hours with an HTTP response and
     * a specified {@link Context}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listOperations#Context -->
     * <pre>
     * PagedIterable&lt;OperationSummary&gt;
     *     modelOperationInfo = documentModelAdministrationClient.listOperations&#40;Context.NONE&#41;;
     * modelOperationInfo.forEach&#40;modelOperationSummary -&gt; &#123;
     *     System.out.printf&#40;&quot;Operation ID: %s%n&quot;, modelOperationSummary.getOperationId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Status: %s%n&quot;, modelOperationSummary.getStatus&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Created on: %s%n&quot;, modelOperationSummary.getCreatedOn&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Percent completed: %d%n&quot;, modelOperationSummary.getPercentCompleted&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Kind: %s%n&quot;, modelOperationSummary.getKind&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Last updated on: %s%n&quot;, modelOperationSummary.getLastUpdatedOn&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation resource location: %s%n&quot;, modelOperationSummary.getResourceLocation&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listOperations#Context -->
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return {@link PagedIterable} of {@link OperationSummary} custom form model information.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<OperationSummary> listOperations(Context context) {
        return listOperationsSync(context);
    }

    private PagedIterable<OperationSummary> listOperationsSync(Context context) {
        return new PagedIterable<>(() -> listFirstPageOperationInfo(context),
            continuationToken -> listNextPageOperationInfo(continuationToken, context));
    }

    private PagedResponse<OperationSummary> listFirstPageOperationInfo(Context context) {
        try {
            PagedResponse<com.azure.ai.formrecognizer.documentanalysis.implementation.models.OperationSummary> res =
                miscellaneousImpl.listOperationsSinglePage(context);

            return new PagedResponseBase<>(res.getRequest(), res.getStatusCode(), res.getHeaders(),
                Transforms.toOperationSummary(res.getValue()), res.getContinuationToken(), null);
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    private PagedResponse<OperationSummary> listNextPageOperationInfo(String nextPageLink, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return null;
        }
        try {
            PagedResponse<com.azure.ai.formrecognizer.documentanalysis.implementation.models.OperationSummary> res =
                miscellaneousImpl.listOperationsNextSinglePage(nextPageLink, context);

            return new PagedResponseBase<>(res.getRequest(), res.getStatusCode(), res.getHeaders(),
                Transforms.toOperationSummary(res.getValue()), res.getContinuationToken(), null);
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    /**
     * Builds a custom classifier document model.
     * <p>Classifier models can identify multiple documents or multiple instances of a single document. For that,
     * you need at least five documents for each class and two classes of documents.
     * </p>
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginBuildDocumentClassifier#Map -->
     * <pre>
     * String blobContainerUrl1040D = &quot;&#123;SAS_URL_of_your_container_in_blob_storage&#125;&quot;;
     * String blobContainerUrl1040A = &quot;&#123;SAS_URL_of_your_container_in_blob_storage&#125;&quot;;
     * HashMap&lt;String, ClassifierDocumentTypeDetails&gt; documentTypes = new HashMap&lt;&gt;&#40;&#41;;
     * documentTypes.put&#40;&quot;1040-D&quot;, new ClassifierDocumentTypeDetails&#40;new BlobContentSource&#40;blobContainerUrl1040D&#41;
     * &#41;&#41;;
     * documentTypes.put&#40;&quot;1040-A&quot;, new ClassifierDocumentTypeDetails&#40;new BlobContentSource&#40;blobContainerUrl1040A&#41;
     * &#41;&#41;;
     *
     * DocumentClassifierDetails classifierDetails
     *     = documentModelAdministrationClient.beginBuildDocumentClassifier&#40;documentTypes&#41;
     *     .getFinalResult&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Classifier ID: %s%n&quot;, classifierDetails.getClassifierId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Classifier description: %s%n&quot;, classifierDetails.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Classifier created on: %s%n&quot;, classifierDetails.getCreatedOn&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Classifier expires on: %s%n&quot;, classifierDetails.getExpiresOn&#40;&#41;&#41;;
     * classifierDetails.getDocumentTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *     if &#40;documentTypeDetails.getContentSource&#40;&#41; instanceof BlobContentSource&#41; &#123;
     *         System.out.printf&#40;&quot;Blob Source container Url: %s&quot;, &#40;&#40;BlobContentSource&#41; documentTypeDetails
     *             .getContentSource&#40;&#41;&#41;.getContainerUrl&#40;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginBuildDocumentClassifier#Map -->
     *
     * @param documentTypes List of document types to classify against.
     * @return A {@link SyncPoller} that polls the building model operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns the built {@link DocumentClassifierDetails custom document analysis model}.
     * @throws HttpResponseException If building the model fails with {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If {@code documentTypes} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<OperationResult, DocumentClassifierDetails> beginBuildDocumentClassifier(
        Map<String, ClassifierDocumentTypeDetails> documentTypes) {
        return beginBuildDocumentClassifier(documentTypes, null, Context.NONE);
    }

    /**
     * Builds a custom classifier document model.
     * <p>Classifier models can identify multiple documents or multiple instances of a single document. For that,
     * you need at least five documents for each class and two classes of documents.
     * </p>
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginBuildDocumentClassifier#Map-Options-Context -->
     * <pre>
     * String blobContainerUrl1040D = &quot;&#123;SAS_URL_of_your_container_in_blob_storage&#125;&quot;;
     * String blobContainerUrl1040A = &quot;&#123;SAS_URL_of_your_container_in_blob_storage&#125;&quot;;
     * HashMap&lt;String, ClassifierDocumentTypeDetails&gt; documentTypesDetailsMap = new HashMap&lt;&gt;&#40;&#41;;
     * documentTypesDetailsMap.put&#40;&quot;1040-D&quot;, new ClassifierDocumentTypeDetails&#40;new BlobContentSource&#40;blobContainerUrl1040D&#41;
     * &#41;&#41;;
     * documentTypesDetailsMap.put&#40;&quot;1040-A&quot;, new ClassifierDocumentTypeDetails&#40;new BlobContentSource&#40;blobContainerUrl1040A&#41;
     * &#41;&#41;;
     *
     * DocumentClassifierDetails classifierDetails
     *     = documentModelAdministrationClient.beginBuildDocumentClassifier&#40;documentTypesDetailsMap,
     *         new BuildDocumentClassifierOptions&#40;&#41;
     *             .setClassifierId&#40;&quot;classifierId&quot;&#41;
     *             .setDescription&#40;&quot;classifier desc&quot;&#41;,
     *         Context.NONE&#41;
     *     .getFinalResult&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Classifier ID: %s%n&quot;, classifierDetails.getClassifierId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Classifier description: %s%n&quot;, classifierDetails.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Classifier created on: %s%n&quot;, classifierDetails.getCreatedOn&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Classifier expires on: %s%n&quot;, classifierDetails.getExpiresOn&#40;&#41;&#41;;
     * classifierDetails.getDocumentTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *     if &#40;documentTypeDetails.getContentSource&#40;&#41; instanceof BlobContentSource&#41; &#123;
     *         System.out.printf&#40;&quot;Blob Source container Url: %s&quot;, &#40;&#40;BlobContentSource&#41; documentTypeDetails
     *             .getContentSource&#40;&#41;&#41;.getContainerUrl&#40;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginBuildDocumentClassifier#Map-Options-Context -->
     *
     * @param documentTypes List of document types to classify against.
     * @param buildDocumentClassifierOptions The configurable {@link BuildDocumentClassifierOptions options} to pass when
     * building a custom classifier document model.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link SyncPoller} that polls the building model operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns the built {@link DocumentClassifierDetails custom document classifier model}.
     * @throws HttpResponseException If building the model fails with {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If {@code documentTypes} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<OperationResult, DocumentClassifierDetails> beginBuildDocumentClassifier(
        Map<String, ClassifierDocumentTypeDetails> documentTypes,
        BuildDocumentClassifierOptions buildDocumentClassifierOptions, Context context) {
        return beginBuildDocumentClassifierSync(documentTypes, buildDocumentClassifierOptions, context);
    }

    private SyncPoller<OperationResult, DocumentClassifierDetails> beginBuildDocumentClassifierSync(
        Map<String, ClassifierDocumentTypeDetails> documentTypes, BuildDocumentClassifierOptions options,
        Context context) {

        BuildDocumentClassifierOptions
            finalBuildDocumentClassifierOptions = options == null ? new BuildDocumentClassifierOptions() : options;
        String classifierId = finalBuildDocumentClassifierOptions.getClassifierId();
        if (classifierId == null) {
            classifierId = Utility.generateRandomModelID();
        }
        String finalId = classifierId;

        return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, buildClassifierActivationOperation(
                finalId, documentTypes, finalBuildDocumentClassifierOptions, context).apply(cxt)),
            buildModelPollingOperation(context), getCancellationIsNotSupported(),
            classifierFetchingOperation(context));
    }

    /**
     * List information for each document classifier on the Form Recognizer account that were built successfully.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listDocumentClassifiers -->
     * <pre>
     * documentModelAdministrationClient.listDocumentClassifiers&#40;&#41;
     *     .forEach&#40;documentModel -&gt;
     *         System.out.printf&#40;&quot;Classifier ID: %s, Classifier description: %s, Created on: %s.%n&quot;,
     *             documentModel.getClassifierId&#40;&#41;,
     *             documentModel.getDescription&#40;&#41;,
     *             documentModel.getCreatedOn&#40;&#41;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listDocumentClassifiers -->
     *
     * @return {@link PagedIterable} of {@link DocumentClassifierDetails document classifiers} on the Form Recognizer account.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DocumentClassifierDetails> listDocumentClassifiers() {
        return listDocumentClassifiers(Context.NONE);
    }

    /**
     * List information for each document classifier on the Form Recognizer account that were built successfully with a Http
     * response and a specified {@link Context}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listDocumentClassifiers#Context -->
     * <pre>
     * documentModelAdministrationClient.listDocumentClassifiers&#40;Context.NONE&#41;
     *     .forEach&#40;documentModel -&gt;
     *         System.out.printf&#40;&quot;Classifier ID: %s, Classifier description: %s, Created on: %s.%n&quot;,
     *             documentModel.getClassifierId&#40;&#41;,
     *             documentModel.getDescription&#40;&#41;,
     *             documentModel.getCreatedOn&#40;&#41;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listDocumentClassifiers#Context -->
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return {@link PagedIterable} of {@link DocumentClassifierDetails document classifiers} on the Form Recognizer account.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DocumentClassifierDetails> listDocumentClassifiers(Context context) {
        return listDocumentClassifiersSync(context);
    }

    private PagedIterable<DocumentClassifierDetails> listDocumentClassifiersSync(Context context) {
        return new PagedIterable<>(() -> listFirstPageClassifiers(context),
            continuationToken -> listNextPageClassifiers(continuationToken, context));
    }

    private PagedResponse<DocumentClassifierDetails> listFirstPageClassifiers(Context context) {
        try {
            PagedResponse<com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentClassifierDetails>
                res = documentClassifiersImpl.listClassifiersSinglePage(context);
            return new PagedResponseBase<>(res.getRequest(), res.getStatusCode(), res.getHeaders(),
                fromInnerCDocumentClassifierDetails(res.getValue()), res.getContinuationToken(), null);
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    private PagedResponse<DocumentClassifierDetails> listNextPageClassifiers(String nextPageLink, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return null;
        }
        try {
            PagedResponse<com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentClassifierDetails>
                res = documentClassifiersImpl.listClassifiersNextSinglePage(nextPageLink, context);
            return new PagedResponseBase<>(res.getRequest(), res.getStatusCode(), res.getHeaders(),
                fromInnerCDocumentClassifierDetails(res.getValue()), res.getContinuationToken(), null);
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    /**
     * Get detailed information for a document classifier by its ID.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getDocumentClassifier#string -->
     * <pre>
     * String classifierId = &quot;&#123;classifierId&#125;&quot;;
     * DocumentClassifierDetails documentClassifierDetails
     *     = documentModelAdministrationClient.getDocumentClassifier&#40;classifierId&#41;;
     * System.out.printf&#40;&quot;Classifier ID: %s%n&quot;, documentClassifierDetails.getClassifierId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Classifier Description: %s%n&quot;, documentClassifierDetails.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Classifier Created on: %s%n&quot;, documentClassifierDetails.getCreatedOn&#40;&#41;&#41;;
     * documentClassifierDetails.getDocumentTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *     if &#40;documentTypeDetails.getContentSource&#40;&#41; instanceof BlobContentSource&#41; &#123;
     *         System.out.printf&#40;&quot;Blob Source container Url: %s&quot;, &#40;&#40;BlobContentSource&#41; documentTypeDetails
     *             .getContentSource&#40;&#41;&#41;.getContainerUrl&#40;&#41;&#41;;
     *     &#125;
     *     if &#40;documentTypeDetails.getContentSource&#40;&#41; instanceof BlobFileListContentSource&#41; &#123;
     *         System.out.printf&#40;&quot;Blob File List Source container Url: %s&quot;,
     *             &#40;&#40;BlobFileListContentSource&#41; documentTypeDetails
     *                 .getContentSource&#40;&#41;&#41;.getContainerUrl&#40;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getDocumentClassifier#string -->
     *
     * @param classifierId The unique document classifier identifier.
     *
     * @return The detailed information for the specified document classifier ID.
     * @throws IllegalArgumentException If {@code classifierId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentClassifierDetails getDocumentClassifier(String classifierId) {
        return getDocumentClassifierWithResponse(classifierId, Context.NONE).getValue();
    }

    /**
     * Get detailed information for a document classifier by its ID.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getDocumentClassifierWithResponse#string-Context -->
     * <pre>
     * String modelId = &quot;&#123;custom-model-id&#125;&quot;;
     * Response&lt;DocumentClassifierDetails&gt; response
     *     = documentModelAdministrationClient.getDocumentClassifierWithResponse&#40;modelId, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     * DocumentClassifierDetails documentClassifierDetails = response.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Classifier ID: %s%n&quot;, documentClassifierDetails.getClassifierId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Classifier Description: %s%n&quot;, documentClassifierDetails.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Classifier Created on: %s%n&quot;, documentClassifierDetails.getCreatedOn&#40;&#41;&#41;;
     * documentClassifierDetails.getDocumentTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *     if &#40;documentTypeDetails.getContentSource&#40;&#41; instanceof BlobContentSource&#41; &#123;
     *         System.out.printf&#40;&quot;Blob Source container Url: %s&quot;, &#40;&#40;BlobContentSource&#41; documentTypeDetails
     *             .getContentSource&#40;&#41;&#41;.getContainerUrl&#40;&#41;&#41;;
     *     &#125;
     *     if &#40;documentTypeDetails.getContentSource&#40;&#41; instanceof BlobFileListContentSource&#41; &#123;
     *         System.out.printf&#40;&quot;Blob File List Source container Url: %s&quot;,
     *             &#40;&#40;BlobFileListContentSource&#41; documentTypeDetails
     *                 .getContentSource&#40;&#41;&#41;.getContainerUrl&#40;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getDocumentClassifierWithResponse#string-Context -->
     *
     * @param classifierId The unique document classifier identifier.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The detailed information for the specified document classifier ID.
     * @throws IllegalArgumentException If {@code classifierId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentClassifierDetails> getDocumentClassifierWithResponse(String classifierId, Context context) {
        if (CoreUtils.isNullOrEmpty(classifierId)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "'classifierId' is required and cannot be null or empty"));
        }
        try {
            Response<com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentClassifierDetails>
                response = documentClassifiersImpl.getClassifierWithResponse(classifierId, context);

            return new SimpleResponse<>(response, Transforms.fromInnerDocumentClassifierDetails(response.getValue()));
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    /**
     * Deletes the specified document classifier.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.deleteDocumentClassifier#string -->
     * <pre>
     * String classifierId = &quot;&#123;classifierId&#125;&quot;;
     * documentModelAdministrationClient.deleteDocumentClassifier&#40;classifierId&#41;;
     * System.out.printf&#40;&quot;Classifier ID: %s is deleted.%n&quot;, classifierId&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.deleteDocumentClassifier#string -->
     *
     * @param classifierId The unique document classifier identifier.
     * @throws IllegalArgumentException If {@code classifierId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteDocumentClassifier(String classifierId) {
        deleteDocumentClassifierWithResponse(classifierId, Context.NONE);
    }

    /**
     * Deletes the specified document classifier.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.deleteDocumentClassifierWithResponse#string-Context -->
     * <pre>
     * String classifierId = &quot;&#123;classifierId&#125;&quot;;
     * Response&lt;Void&gt; response
     *     = documentModelAdministrationClient.deleteDocumentClassifierWithResponse&#40;classifierId, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Classifier ID: %s is deleted.%n&quot;, classifierId&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.deleteDocumentClassifierWithResponse#string-Context -->
     *
     * @param classifierId The unique document classifier identifier.
     * @param context Additional context that is passed through the Http pipeline during the service call.

     * @return A {@link Response} containing status code and HTTP headers.
     * @throws IllegalArgumentException If {@code classifierId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteDocumentClassifierWithResponse(String classifierId, Context context) {
        if (CoreUtils.isNullOrEmpty(classifierId)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "'classifierId' is required and cannot be null or empty"));
        }
        try {
            return
                documentClassifiersImpl.deleteClassifierWithResponse(classifierId, context);
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    private Function<PollingContext<OperationResult>, OperationResult> buildModelActivationOperation(
        ContentSource contentSource, DocumentModelBuildMode buildMode, String modelId,
        BuildDocumentModelOptions buildDocumentModelOptions, Context context) {
        return (pollingContext) -> {
            try {
                BuildDocumentModelRequest buildDocumentModelRequest = getBuildDocumentModelRequest(contentSource,
                    buildMode, modelId, buildDocumentModelOptions);

                ResponseBase<DocumentModelsBuildModelHeaders, Void> response
                    = documentModelsImpl.buildModelWithResponse(buildDocumentModelRequest, context);
                return Transforms.toDocumentOperationResult(response.getDeserializedHeaders().getOperationLocation());
            } catch (ErrorResponseException ex) {
                throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
            }
        };
    }

    private Function<PollingContext<OperationResult>, PollResponse<OperationResult>>
        buildModelPollingOperation(Context context) {
        return (pollingContext) -> {
            try {
                PollResponse<OperationResult> operationResultPollResponse = pollingContext.getLatestResponse();
                String modelId = operationResultPollResponse.getValue().getOperationId();

                return processBuildingModelResponse(
                    miscellaneousImpl.getOperationWithResponse(modelId, context).getValue(),
                    operationResultPollResponse);
            } catch (ErrorResponseException ex) {
                throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
            }
        };
    }

    private PollResponse<OperationResult> processBuildingModelResponse(
        com.azure.ai.formrecognizer.documentanalysis.implementation.models.OperationDetails getOperationResponse,
        PollResponse<OperationResult> trainingModelOperationResponse) {
        LongRunningOperationStatus status;
        switch (getOperationResponse.getStatus()) {
            case NOT_STARTED:
            case RUNNING:
                status = LongRunningOperationStatus.IN_PROGRESS;
                break;
            case SUCCEEDED:
                status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                break;
            case FAILED:
                throw LOGGER.logExceptionAsError(
                    Transforms.mapResponseErrorToHttpResponseException(getOperationResponse.getError()));
            case CANCELED:
            default:
                status = LongRunningOperationStatus.fromString(
                    getOperationResponse.getStatus().toString(), true);
                break;
        }
        return new PollResponse<>(status,
            trainingModelOperationResponse.getValue());
    }

    private BiFunction<PollingContext<OperationResult>, PollResponse<OperationResult>, OperationResult>
        getCancellationIsNotSupported() {
        return (pollingContext, activationResponse) -> {
            throw LOGGER.logExceptionAsError(new RuntimeException("Cancellation is not supported"));
        };
    }

    private Function<PollingContext<OperationResult>, DocumentModelDetails>
        buildModelFetchingOperation(Context context) {
        return (pollingContext) -> {
            try {
                final String modelId = pollingContext.getLatestResponse().getValue().getOperationId();
                return
                    Transforms.toDocumentModelFromOperationId(miscellaneousImpl.getOperationWithResponse(modelId,
                        context).getValue());
            } catch (ErrorResponseException ex) {
                throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
            }
        };
    }

    private Function<PollingContext<OperationResult>, OperationResult>
        composeModelActivationOperation(ComposeDocumentModelRequest composeRequest, Context context) {
        return (pollingContext) -> {
            try {
                ResponseBase<DocumentModelsComposeModelHeaders, Void> response
                    = documentModelsImpl.composeModelWithResponse(composeRequest, context);
                return Transforms.toDocumentOperationResult(response.getDeserializedHeaders().getOperationLocation());
            } catch (ErrorResponseException ex) {
                throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
            }
        };
    }

    private Function<PollingContext<OperationResult>, OperationResult> getCopyActivationOperation(String modelId,
        DocumentModelCopyAuthorization target, Context context) {
        return (pollingContext) -> {
            try {
                Objects.requireNonNull(modelId, "'modelId' cannot be null.");
                Objects.requireNonNull(target, "'target' cannot be null.");
                com.azure.ai.formrecognizer.documentanalysis.implementation.models.CopyAuthorization copyRequest
                    = getInnerCopyAuthorization(target);
                ResponseBase<DocumentModelsCopyModelToHeaders, Void> response
                    = documentModelsImpl.copyModelToWithResponse(modelId, copyRequest, context);
                return Transforms.toDocumentOperationResult(response.getDeserializedHeaders().getOperationLocation());
            }  catch (ErrorResponseException ex) {
                throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
            }
        };
    }

    private Function<PollingContext<OperationResult>, OperationResult> buildClassifierActivationOperation(
        String classifierId, Map<String, ClassifierDocumentTypeDetails> documentTypes,
        BuildDocumentClassifierOptions buildDocumentClassifierOptions, Context context) {
        return (pollingContext) -> {
            try {
                Objects.requireNonNull(documentTypes, "'documentTypes' cannot be null.");
                BuildDocumentClassifierRequest buildDocumentModelRequest = getBuildDocumentClassifierRequest(
                    classifierId, buildDocumentClassifierOptions.getDescription(), toInnerDocTypes(documentTypes));

                ResponseBase<DocumentClassifiersBuildClassifierHeaders, Void> response
                    = documentClassifiersImpl.buildClassifierWithResponse(buildDocumentModelRequest, context);
                return Transforms.toDocumentOperationResult(response.getDeserializedHeaders().getOperationLocation());
            } catch (ErrorResponseException ex) {
                throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
            }
        };
    }

    private Function<PollingContext<OperationResult>, DocumentClassifierDetails>
        classifierFetchingOperation(Context context) {
        return (pollingContext) -> {
            try {
                final String classifierId = pollingContext.getLatestResponse().getValue().getOperationId();
                return Transforms.toDocumentClassifierFromOperationId(miscellaneousImpl.getOperationWithResponse(
                        classifierId, context).getValue());
            } catch (ErrorResponseException ex) {
                throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
            }
        };
    }
}
