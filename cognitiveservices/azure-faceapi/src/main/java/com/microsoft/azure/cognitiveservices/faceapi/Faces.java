/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import com.microsoft.azure.cognitiveservices.faceapi.models.APIErrorException;
import com.microsoft.azure.cognitiveservices.faceapi.models.DetectedFace;
import com.microsoft.azure.cognitiveservices.faceapi.models.GroupResponse;
import com.microsoft.azure.cognitiveservices.faceapi.models.IdentifyResultItem;
import com.microsoft.azure.cognitiveservices.faceapi.models.SimilarFaceResult;
import com.microsoft.azure.cognitiveservices.faceapi.models.VerifyResult;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import java.util.List;
import rx.Observable;

/**
 * An instance of this class provides access to all the operations defined
 * in Faces.
 */
public interface Faces {
    /**
     * Given query face's faceId, find the similar-looking faces from a faceId array or a faceListId.
     *
     * @param faceId FaceId of the query face. User needs to call Face - Detect first to get a valid faceId. Note that this faceId is not persisted and will expire 24 hours after the detection call
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;SimilarFaceResult&gt; object if successful.
     */
    List<SimilarFaceResult> findSimilar(String faceId);

    /**
     * Given query face's faceId, find the similar-looking faces from a faceId array or a faceListId.
     *
     * @param faceId FaceId of the query face. User needs to call Face - Detect first to get a valid faceId. Note that this faceId is not persisted and will expire 24 hours after the detection call
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<List<SimilarFaceResult>> findSimilarAsync(String faceId, final ServiceCallback<List<SimilarFaceResult>> serviceCallback);

    /**
     * Given query face's faceId, find the similar-looking faces from a faceId array or a faceListId.
     *
     * @param faceId FaceId of the query face. User needs to call Face - Detect first to get a valid faceId. Note that this faceId is not persisted and will expire 24 hours after the detection call
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;SimilarFaceResult&gt; object
     */
    Observable<List<SimilarFaceResult>> findSimilarAsync(String faceId);

    /**
     * Given query face's faceId, find the similar-looking faces from a faceId array or a faceListId.
     *
     * @param faceId FaceId of the query face. User needs to call Face - Detect first to get a valid faceId. Note that this faceId is not persisted and will expire 24 hours after the detection call
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;SimilarFaceResult&gt; object
     */
    Observable<ServiceResponse<List<SimilarFaceResult>>> findSimilarWithServiceResponseAsync(String faceId);
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
    List<SimilarFaceResult> findSimilar(String faceId, String faceListId, List<String> faceIds, Integer maxNumOfCandidatesReturned, String mode);

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
    ServiceFuture<List<SimilarFaceResult>> findSimilarAsync(String faceId, String faceListId, List<String> faceIds, Integer maxNumOfCandidatesReturned, String mode, final ServiceCallback<List<SimilarFaceResult>> serviceCallback);

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
    Observable<List<SimilarFaceResult>> findSimilarAsync(String faceId, String faceListId, List<String> faceIds, Integer maxNumOfCandidatesReturned, String mode);

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
    Observable<ServiceResponse<List<SimilarFaceResult>>> findSimilarWithServiceResponseAsync(String faceId, String faceListId, List<String> faceIds, Integer maxNumOfCandidatesReturned, String mode);

    /**
     * Divide candidate faces into groups based on face similarity.
     *
     * @param faceIds Array of candidate faceId created by Face - Detect. The maximum is 1000 faces
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the GroupResponse object if successful.
     */
    GroupResponse group(List<String> faceIds);

    /**
     * Divide candidate faces into groups based on face similarity.
     *
     * @param faceIds Array of candidate faceId created by Face - Detect. The maximum is 1000 faces
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<GroupResponse> groupAsync(List<String> faceIds, final ServiceCallback<GroupResponse> serviceCallback);

    /**
     * Divide candidate faces into groups based on face similarity.
     *
     * @param faceIds Array of candidate faceId created by Face - Detect. The maximum is 1000 faces
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the GroupResponse object
     */
    Observable<GroupResponse> groupAsync(List<String> faceIds);

    /**
     * Divide candidate faces into groups based on face similarity.
     *
     * @param faceIds Array of candidate faceId created by Face - Detect. The maximum is 1000 faces
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the GroupResponse object
     */
    Observable<ServiceResponse<GroupResponse>> groupWithServiceResponseAsync(List<String> faceIds);

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
    List<IdentifyResultItem> identify(String personGroupId, List<String> faceIds);

    /**
     * Identify unknown faces from a person group.
     *
     * @param personGroupId personGroupId of the target person group, created by PersonGroups.Create
     * @param faceIds Array of candidate faceId created by Face - Detect.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<List<IdentifyResultItem>> identifyAsync(String personGroupId, List<String> faceIds, final ServiceCallback<List<IdentifyResultItem>> serviceCallback);

    /**
     * Identify unknown faces from a person group.
     *
     * @param personGroupId personGroupId of the target person group, created by PersonGroups.Create
     * @param faceIds Array of candidate faceId created by Face - Detect.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;IdentifyResultItem&gt; object
     */
    Observable<List<IdentifyResultItem>> identifyAsync(String personGroupId, List<String> faceIds);

    /**
     * Identify unknown faces from a person group.
     *
     * @param personGroupId personGroupId of the target person group, created by PersonGroups.Create
     * @param faceIds Array of candidate faceId created by Face - Detect.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;IdentifyResultItem&gt; object
     */
    Observable<ServiceResponse<List<IdentifyResultItem>>> identifyWithServiceResponseAsync(String personGroupId, List<String> faceIds);
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
    List<IdentifyResultItem> identify(String personGroupId, List<String> faceIds, Integer maxNumOfCandidatesReturned, Double confidenceThreshold);

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
    ServiceFuture<List<IdentifyResultItem>> identifyAsync(String personGroupId, List<String> faceIds, Integer maxNumOfCandidatesReturned, Double confidenceThreshold, final ServiceCallback<List<IdentifyResultItem>> serviceCallback);

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
    Observable<List<IdentifyResultItem>> identifyAsync(String personGroupId, List<String> faceIds, Integer maxNumOfCandidatesReturned, Double confidenceThreshold);

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
    Observable<ServiceResponse<List<IdentifyResultItem>>> identifyWithServiceResponseAsync(String personGroupId, List<String> faceIds, Integer maxNumOfCandidatesReturned, Double confidenceThreshold);

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
    VerifyResult verify(String faceId, String personId, String personGroupId);

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
    ServiceFuture<VerifyResult> verifyAsync(String faceId, String personId, String personGroupId, final ServiceCallback<VerifyResult> serviceCallback);

    /**
     * Verify whether two faces belong to a same person or whether one face belongs to a person.
     *
     * @param faceId faceId the face, comes from Face - Detect
     * @param personId Specify a certain person in a person group. personId is created in Persons.Create.
     * @param personGroupId Using existing personGroupId and personId for fast loading a specified person. personGroupId is created in Person Groups.Create.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the VerifyResult object
     */
    Observable<VerifyResult> verifyAsync(String faceId, String personId, String personGroupId);

    /**
     * Verify whether two faces belong to a same person or whether one face belongs to a person.
     *
     * @param faceId faceId the face, comes from Face - Detect
     * @param personId Specify a certain person in a person group. personId is created in Persons.Create.
     * @param personGroupId Using existing personGroupId and personId for fast loading a specified person. personGroupId is created in Person Groups.Create.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the VerifyResult object
     */
    Observable<ServiceResponse<VerifyResult>> verifyWithServiceResponseAsync(String faceId, String personId, String personGroupId);

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;DetectedFace&gt; object if successful.
     */
    List<DetectedFace> detect(String url);

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param url the String value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<List<DetectedFace>> detectAsync(String url, final ServiceCallback<List<DetectedFace>> serviceCallback);

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;DetectedFace&gt; object
     */
    Observable<List<DetectedFace>> detectAsync(String url);

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;DetectedFace&gt; object
     */
    Observable<ServiceResponse<List<DetectedFace>>> detectWithServiceResponseAsync(String url);
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
    List<DetectedFace> detect(String url, Boolean returnFaceId, Boolean returnFaceLandmarks, String returnFaceAttributes);

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
    ServiceFuture<List<DetectedFace>> detectAsync(String url, Boolean returnFaceId, Boolean returnFaceLandmarks, String returnFaceAttributes, final ServiceCallback<List<DetectedFace>> serviceCallback);

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
    Observable<List<DetectedFace>> detectAsync(String url, Boolean returnFaceId, Boolean returnFaceLandmarks, String returnFaceAttributes);

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
    Observable<ServiceResponse<List<DetectedFace>>> detectWithServiceResponseAsync(String url, Boolean returnFaceId, Boolean returnFaceLandmarks, String returnFaceAttributes);

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;DetectedFace&gt; object if successful.
     */
    List<DetectedFace> detectInStream(byte[] image);

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param image An image stream.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<List<DetectedFace>> detectInStreamAsync(byte[] image, final ServiceCallback<List<DetectedFace>> serviceCallback);

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;DetectedFace&gt; object
     */
    Observable<List<DetectedFace>> detectInStreamAsync(byte[] image);

    /**
     * Detect human faces in an image and returns face locations, and optionally with faceIds, landmarks, and attributes.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;DetectedFace&gt; object
     */
    Observable<ServiceResponse<List<DetectedFace>>> detectInStreamWithServiceResponseAsync(byte[] image);
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
    List<DetectedFace> detectInStream(byte[] image, Boolean returnFaceId, Boolean returnFaceLandmarks, String returnFaceAttributes);

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
    ServiceFuture<List<DetectedFace>> detectInStreamAsync(byte[] image, Boolean returnFaceId, Boolean returnFaceLandmarks, String returnFaceAttributes, final ServiceCallback<List<DetectedFace>> serviceCallback);

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
    Observable<List<DetectedFace>> detectInStreamAsync(byte[] image, Boolean returnFaceId, Boolean returnFaceLandmarks, String returnFaceAttributes);

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
    Observable<ServiceResponse<List<DetectedFace>>> detectInStreamWithServiceResponseAsync(byte[] image, Boolean returnFaceId, Boolean returnFaceLandmarks, String returnFaceAttributes);

}
