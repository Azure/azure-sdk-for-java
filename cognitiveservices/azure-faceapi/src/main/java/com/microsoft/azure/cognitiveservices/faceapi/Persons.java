/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import com.microsoft.azure.cognitiveservices.faceapi.models.APIErrorException;
import com.microsoft.azure.cognitiveservices.faceapi.models.CreatePersonResult;
import com.microsoft.azure.cognitiveservices.faceapi.models.PersonFaceResult;
import com.microsoft.azure.cognitiveservices.faceapi.models.PersonResult;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import java.util.List;
import rx.Observable;

/**
 * An instance of this class provides access to all the operations defined
 * in Persons.
 */
public interface Persons {
    /**
     * Create a new person in a specified person group.
     *
     * @param personGroupId Specifying the target person group to create the person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the CreatePersonResult object if successful.
     */
    CreatePersonResult create(String personGroupId);

    /**
     * Create a new person in a specified person group.
     *
     * @param personGroupId Specifying the target person group to create the person.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<CreatePersonResult> createAsync(String personGroupId, final ServiceCallback<CreatePersonResult> serviceCallback);

    /**
     * Create a new person in a specified person group.
     *
     * @param personGroupId Specifying the target person group to create the person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the CreatePersonResult object
     */
    Observable<CreatePersonResult> createAsync(String personGroupId);

    /**
     * Create a new person in a specified person group.
     *
     * @param personGroupId Specifying the target person group to create the person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the CreatePersonResult object
     */
    Observable<ServiceResponse<CreatePersonResult>> createWithServiceResponseAsync(String personGroupId);
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
    CreatePersonResult create(String personGroupId, String name, String userData);

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
    ServiceFuture<CreatePersonResult> createAsync(String personGroupId, String name, String userData, final ServiceCallback<CreatePersonResult> serviceCallback);

    /**
     * Create a new person in a specified person group.
     *
     * @param personGroupId Specifying the target person group to create the person.
     * @param name Display name of the target person. The maximum length is 128.
     * @param userData Optional fields for user-provided data attached to a person. Size limit is 16KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the CreatePersonResult object
     */
    Observable<CreatePersonResult> createAsync(String personGroupId, String name, String userData);

    /**
     * Create a new person in a specified person group.
     *
     * @param personGroupId Specifying the target person group to create the person.
     * @param name Display name of the target person. The maximum length is 128.
     * @param userData Optional fields for user-provided data attached to a person. Size limit is 16KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the CreatePersonResult object
     */
    Observable<ServiceResponse<CreatePersonResult>> createWithServiceResponseAsync(String personGroupId, String name, String userData);

    /**
     * List all persons in a person group, and retrieve person information (including personId, name, userData and persistedFaceIds of registered faces of the person).
     *
     * @param personGroupId personGroupId of the target person group.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;PersonResult&gt; object if successful.
     */
    List<PersonResult> list(String personGroupId);

    /**
     * List all persons in a person group, and retrieve person information (including personId, name, userData and persistedFaceIds of registered faces of the person).
     *
     * @param personGroupId personGroupId of the target person group.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<List<PersonResult>> listAsync(String personGroupId, final ServiceCallback<List<PersonResult>> serviceCallback);

    /**
     * List all persons in a person group, and retrieve person information (including personId, name, userData and persistedFaceIds of registered faces of the person).
     *
     * @param personGroupId personGroupId of the target person group.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;PersonResult&gt; object
     */
    Observable<List<PersonResult>> listAsync(String personGroupId);

    /**
     * List all persons in a person group, and retrieve person information (including personId, name, userData and persistedFaceIds of registered faces of the person).
     *
     * @param personGroupId personGroupId of the target person group.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;PersonResult&gt; object
     */
    Observable<ServiceResponse<List<PersonResult>>> listWithServiceResponseAsync(String personGroupId);

    /**
     * Delete an existing person from a person group. Persisted face images of the person will also be deleted.
     *
     * @param personGroupId Specifying the person group containing the person.
     * @param personId The target personId to delete.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    void delete(String personGroupId, String personId);

    /**
     * Delete an existing person from a person group. Persisted face images of the person will also be deleted.
     *
     * @param personGroupId Specifying the person group containing the person.
     * @param personId The target personId to delete.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<Void> deleteAsync(String personGroupId, String personId, final ServiceCallback<Void> serviceCallback);

    /**
     * Delete an existing person from a person group. Persisted face images of the person will also be deleted.
     *
     * @param personGroupId Specifying the person group containing the person.
     * @param personId The target personId to delete.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    Observable<Void> deleteAsync(String personGroupId, String personId);

    /**
     * Delete an existing person from a person group. Persisted face images of the person will also be deleted.
     *
     * @param personGroupId Specifying the person group containing the person.
     * @param personId The target personId to delete.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    Observable<ServiceResponse<Void>> deleteWithServiceResponseAsync(String personGroupId, String personId);

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
    PersonResult get(String personGroupId, String personId);

    /**
     * Retrieve a person's information, including registered persisted faces, name and userData.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Specifying the target person.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<PersonResult> getAsync(String personGroupId, String personId, final ServiceCallback<PersonResult> serviceCallback);

    /**
     * Retrieve a person's information, including registered persisted faces, name and userData.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Specifying the target person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PersonResult object
     */
    Observable<PersonResult> getAsync(String personGroupId, String personId);

    /**
     * Retrieve a person's information, including registered persisted faces, name and userData.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Specifying the target person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PersonResult object
     */
    Observable<ServiceResponse<PersonResult>> getWithServiceResponseAsync(String personGroupId, String personId);

    /**
     * Update name or userData of a person.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    void update(String personGroupId, String personId);

    /**
     * Update name or userData of a person.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<Void> updateAsync(String personGroupId, String personId, final ServiceCallback<Void> serviceCallback);

    /**
     * Update name or userData of a person.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    Observable<Void> updateAsync(String personGroupId, String personId);

    /**
     * Update name or userData of a person.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    Observable<ServiceResponse<Void>> updateWithServiceResponseAsync(String personGroupId, String personId);
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
    void update(String personGroupId, String personId, String name, String userData);

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
    ServiceFuture<Void> updateAsync(String personGroupId, String personId, String name, String userData, final ServiceCallback<Void> serviceCallback);

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
    Observable<Void> updateAsync(String personGroupId, String personId, String name, String userData);

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
    Observable<ServiceResponse<Void>> updateWithServiceResponseAsync(String personGroupId, String personId, String name, String userData);

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
    void deleteFace(String personGroupId, String personId, String persistedFaceId);

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
    ServiceFuture<Void> deleteFaceAsync(String personGroupId, String personId, String persistedFaceId, final ServiceCallback<Void> serviceCallback);

    /**
     * Delete a face from a person. Relative image for the persisted face will also be deleted.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Specifying the person that the target persisted face belong to.
     * @param persistedFaceId The persisted face to remove.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    Observable<Void> deleteFaceAsync(String personGroupId, String personId, String persistedFaceId);

    /**
     * Delete a face from a person. Relative image for the persisted face will also be deleted.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Specifying the person that the target persisted face belong to.
     * @param persistedFaceId The persisted face to remove.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    Observable<ServiceResponse<Void>> deleteFaceWithServiceResponseAsync(String personGroupId, String personId, String persistedFaceId);

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
    PersonFaceResult getFace(String personGroupId, String personId, String persistedFaceId);

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
    ServiceFuture<PersonFaceResult> getFaceAsync(String personGroupId, String personId, String persistedFaceId, final ServiceCallback<PersonFaceResult> serviceCallback);

    /**
     * Retrieve information about a persisted face (specified by persistedFaceId, personId and its belonging personGroupId).
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Specifying the target person that the face belongs to.
     * @param persistedFaceId The persistedFaceId of the target persisted face of the person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PersonFaceResult object
     */
    Observable<PersonFaceResult> getFaceAsync(String personGroupId, String personId, String persistedFaceId);

    /**
     * Retrieve information about a persisted face (specified by persistedFaceId, personId and its belonging personGroupId).
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Specifying the target person that the face belongs to.
     * @param persistedFaceId The persistedFaceId of the target persisted face of the person.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PersonFaceResult object
     */
    Observable<ServiceResponse<PersonFaceResult>> getFaceWithServiceResponseAsync(String personGroupId, String personId, String persistedFaceId);

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
    void updateFace(String personGroupId, String personId, String persistedFaceId);

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
    ServiceFuture<Void> updateFaceAsync(String personGroupId, String personId, String persistedFaceId, final ServiceCallback<Void> serviceCallback);

    /**
     * Update a person persisted face's userData field.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @param persistedFaceId persistedFaceId of target face, which is persisted and will not expire.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    Observable<Void> updateFaceAsync(String personGroupId, String personId, String persistedFaceId);

    /**
     * Update a person persisted face's userData field.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId personId of the target person.
     * @param persistedFaceId persistedFaceId of target face, which is persisted and will not expire.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    Observable<ServiceResponse<Void>> updateFaceWithServiceResponseAsync(String personGroupId, String personId, String persistedFaceId);
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
    void updateFace(String personGroupId, String personId, String persistedFaceId, String userData);

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
    ServiceFuture<Void> updateFaceAsync(String personGroupId, String personId, String persistedFaceId, String userData, final ServiceCallback<Void> serviceCallback);

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
    Observable<Void> updateFaceAsync(String personGroupId, String personId, String persistedFaceId, String userData);

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
    Observable<ServiceResponse<Void>> updateFaceWithServiceResponseAsync(String personGroupId, String personId, String persistedFaceId, String userData);

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    void addFace(String personGroupId, String personId);

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<Void> addFaceAsync(String personGroupId, String personId, final ServiceCallback<Void> serviceCallback);

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    Observable<Void> addFaceAsync(String personGroupId, String personId);

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    Observable<ServiceResponse<Void>> addFaceWithServiceResponseAsync(String personGroupId, String personId);
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
    void addFace(String personGroupId, String personId, String userData, String targetFace);

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
    ServiceFuture<Void> addFaceAsync(String personGroupId, String personId, String userData, String targetFace, final ServiceCallback<Void> serviceCallback);

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
    Observable<Void> addFaceAsync(String personGroupId, String personId, String userData, String targetFace);

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
    Observable<ServiceResponse<Void>> addFaceWithServiceResponseAsync(String personGroupId, String personId, String userData, String targetFace);

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    void addFaceFromStream(String personGroupId, String personId);

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<Void> addFaceFromStreamAsync(String personGroupId, String personId, final ServiceCallback<Void> serviceCallback);

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    Observable<Void> addFaceFromStreamAsync(String personGroupId, String personId);

    /**
     * Add a representative face to a person for identification. The input face is specified as an image with a targetFace rectangle.
     *
     * @param personGroupId Specifying the person group containing the target person.
     * @param personId Target person that the face is added to.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    Observable<ServiceResponse<Void>> addFaceFromStreamWithServiceResponseAsync(String personGroupId, String personId);
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
    void addFaceFromStream(String personGroupId, String personId, String userData, String targetFace);

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
    ServiceFuture<Void> addFaceFromStreamAsync(String personGroupId, String personId, String userData, String targetFace, final ServiceCallback<Void> serviceCallback);

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
    Observable<Void> addFaceFromStreamAsync(String personGroupId, String personId, String userData, String targetFace);

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
    Observable<ServiceResponse<Void>> addFaceFromStreamWithServiceResponseAsync(String personGroupId, String personId, String userData, String targetFace);

}
