// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
//

package com.azure.iot.deviceupdate;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Delete;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.iot.deviceupdate.models.Device;
import com.azure.iot.deviceupdate.models.DeviceClass;
import com.azure.iot.deviceupdate.models.DeviceTag;
import com.azure.iot.deviceupdate.models.Group;
import com.azure.iot.deviceupdate.models.PageableListOfDeviceClasses;
import com.azure.iot.deviceupdate.models.PageableListOfDeviceTags;
import com.azure.iot.deviceupdate.models.PageableListOfDevices;
import com.azure.iot.deviceupdate.models.PageableListOfGroups;
import com.azure.iot.deviceupdate.models.PageableListOfStrings;
import com.azure.iot.deviceupdate.models.PageableListOfUpdatableDevices;
import com.azure.iot.deviceupdate.models.PageableListOfUpdateIds;
import com.azure.iot.deviceupdate.models.UpdatableDevices;
import com.azure.iot.deviceupdate.models.UpdateCompliance;
import com.azure.iot.deviceupdate.models.UpdateId;
import reactor.core.publisher.Mono;

/** An instance of this class provides access to all the operations defined in Devices. */
public final class Devices {
    /** The proxy service used to perform REST calls. */
    private final DevicesService service;

    /** The service client containing this operation class. */
    private final DeviceUpdateClient client;

    /**
     * Initializes an instance of Devices.
     *
     * @param client the instance of the service client containing this operation class.
     */
    Devices(DeviceUpdateClient client) {
        this.service = RestProxy.create(DevicesService.class, client.getHttpPipeline(), client.getSerializerAdapter());
        this.client = client;
    }

    /**
     * The interface defining all the services for DeviceUpdateClientDevices to be used by the proxy service to perform
     * REST calls.
     */
    @Host("https://{accountEndpoint}")
    @ServiceInterface(name = "DeviceUpdateClientDe")
    private interface DevicesService {
        @Get("/deviceupdate/{instanceId}/v2/management/deviceclasses")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<PageableListOfDeviceClasses>> getAllDeviceClasses(
                @HostParam("accountEndpoint") String accountEndpoint,
                @PathParam(value = "instanceId", encoded = true) String instanceId,
                @HeaderParam("Accept") String accept);

        @Get("/deviceupdate/{instanceId}/v2/management/deviceclasses/{deviceClassId}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<DeviceClass>> getDeviceClass(
                @HostParam("accountEndpoint") String accountEndpoint,
                @PathParam(value = "instanceId", encoded = true) String instanceId,
                @PathParam("deviceClassId") String deviceClassId,
                @HeaderParam("Accept") String accept);

        @Get("/deviceupdate/{instanceId}/v2/management/deviceclasses/{deviceClassId}/deviceids")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<PageableListOfStrings>> getDeviceClassDeviceIds(
                @HostParam("accountEndpoint") String accountEndpoint,
                @PathParam(value = "instanceId", encoded = true) String instanceId,
                @PathParam("deviceClassId") String deviceClassId,
                @HeaderParam("Accept") String accept);

        @Get("/deviceupdate/{instanceId}/v2/management/deviceclasses/{deviceClassId}/installableupdates")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<PageableListOfUpdateIds>> getDeviceClassInstallableUpdates(
                @HostParam("accountEndpoint") String accountEndpoint,
                @PathParam(value = "instanceId", encoded = true) String instanceId,
                @PathParam("deviceClassId") String deviceClassId,
                @HeaderParam("Accept") String accept);

        @Get("/deviceupdate/{instanceId}/v2/management/devices")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<PageableListOfDevices>> getAllDevices(
                @HostParam("accountEndpoint") String accountEndpoint,
                @PathParam(value = "instanceId", encoded = true) String instanceId,
                @QueryParam("$filter") String filter,
                @HeaderParam("Accept") String accept);

        @Get("/deviceupdate/{instanceId}/v2/management/devices/{deviceId}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Device>> getDevice(
                @HostParam("accountEndpoint") String accountEndpoint,
                @PathParam(value = "instanceId", encoded = true) String instanceId,
                @PathParam("deviceId") String deviceId,
                @HeaderParam("Accept") String accept);

        @Get("/deviceupdate/{instanceId}/v2/management/updatecompliance")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<UpdateCompliance>> getUpdateCompliance(
                @HostParam("accountEndpoint") String accountEndpoint,
                @PathParam(value = "instanceId", encoded = true) String instanceId,
                @HeaderParam("Accept") String accept);

        @Get("/deviceupdate/{instanceId}/v2/management/devicetags")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<PageableListOfDeviceTags>> getAllDeviceTags(
                @HostParam("accountEndpoint") String accountEndpoint,
                @PathParam(value = "instanceId", encoded = true) String instanceId,
                @HeaderParam("Accept") String accept);

        @Get("/deviceupdate/{instanceId}/v2/management/devicetags/{tagName}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<DeviceTag>> getDeviceTag(
                @HostParam("accountEndpoint") String accountEndpoint,
                @PathParam(value = "instanceId", encoded = true) String instanceId,
                @PathParam("tagName") String tagName,
                @HeaderParam("Accept") String accept);

        @Get("/deviceupdate/{instanceId}/v2/management/groups")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<PageableListOfGroups>> getAllGroups(
                @HostParam("accountEndpoint") String accountEndpoint,
                @PathParam(value = "instanceId", encoded = true) String instanceId,
                @HeaderParam("Accept") String accept);

        @Get("/deviceupdate/{instanceId}/v2/management/groups/{groupId}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Group>> getGroup(
                @HostParam("accountEndpoint") String accountEndpoint,
                @PathParam(value = "instanceId", encoded = true) String instanceId,
                @PathParam("groupId") String groupId,
                @HeaderParam("Accept") String accept);

        @Put("/deviceupdate/{instanceId}/v2/management/groups/{groupId}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Group>> createOrUpdateGroup(
                @HostParam("accountEndpoint") String accountEndpoint,
                @PathParam(value = "instanceId", encoded = true) String instanceId,
                @PathParam("groupId") String groupId,
                @BodyParam("application/json") Group group,
                @HeaderParam("Accept") String accept);

        @Delete("/deviceupdate/{instanceId}/v2/management/groups/{groupId}")
        @ExpectedResponses({200, 204})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> deleteGroup(
                @HostParam("accountEndpoint") String accountEndpoint,
                @PathParam(value = "instanceId", encoded = true) String instanceId,
                @PathParam("groupId") String groupId);

        @Get("/deviceupdate/{instanceId}/v2/management/groups/{groupId}/updateCompliance")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<UpdateCompliance>> getGroupUpdateCompliance(
                @HostParam("accountEndpoint") String accountEndpoint,
                @PathParam(value = "instanceId", encoded = true) String instanceId,
                @PathParam("groupId") String groupId,
                @HeaderParam("Accept") String accept);

        @Get("/deviceupdate/{instanceId}/v2/management/groups/{groupId}/bestUpdates")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<PageableListOfUpdatableDevices>> getGroupBestUpdates(
                @HostParam("accountEndpoint") String accountEndpoint,
                @PathParam(value = "instanceId", encoded = true) String instanceId,
                @PathParam("groupId") String groupId,
                @QueryParam("$filter") String filter,
                @HeaderParam("Accept") String accept);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<PageableListOfDeviceClasses>> getAllDeviceClassesNext(
                @PathParam(value = "nextLink", encoded = true) String nextLink,
                @HostParam("accountEndpoint") String accountEndpoint,
                @HeaderParam("Accept") String accept);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<PageableListOfStrings>> getDeviceClassDeviceIdsNext(
                @PathParam(value = "nextLink", encoded = true) String nextLink,
                @HostParam("accountEndpoint") String accountEndpoint,
                @HeaderParam("Accept") String accept);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<PageableListOfUpdateIds>> getDeviceClassInstallableUpdatesNext(
                @PathParam(value = "nextLink", encoded = true) String nextLink,
                @HostParam("accountEndpoint") String accountEndpoint,
                @HeaderParam("Accept") String accept);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<PageableListOfDevices>> getAllDevicesNext(
                @PathParam(value = "nextLink", encoded = true) String nextLink,
                @HostParam("accountEndpoint") String accountEndpoint,
                @HeaderParam("Accept") String accept);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<PageableListOfDeviceTags>> getAllDeviceTagsNext(
                @PathParam(value = "nextLink", encoded = true) String nextLink,
                @HostParam("accountEndpoint") String accountEndpoint,
                @HeaderParam("Accept") String accept);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<PageableListOfGroups>> getAllGroupsNext(
                @PathParam(value = "nextLink", encoded = true) String nextLink,
                @HostParam("accountEndpoint") String accountEndpoint,
                @HeaderParam("Accept") String accept);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<PageableListOfUpdatableDevices>> getGroupBestUpdatesNext(
                @PathParam(value = "nextLink", encoded = true) String nextLink,
                @HostParam("accountEndpoint") String accountEndpoint,
                @HeaderParam("Accept") String accept);
    }

    /**
     * Gets a list of all device classes (unique combinations of device manufacturer and model) for all devices
     * connected to Device Update for IoT Hub.
     *
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of all device classes (unique combinations of device manufacturer and model) for all devices
     *     connected to Device Update for IoT Hub.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<DeviceClass>> getAllDeviceClassesSinglePageAsync() {
        final String accept = "application/json";
        return service.getAllDeviceClasses(this.client.getAccountEndpoint(), this.client.getInstanceId(), accept)
                .map(
                        res ->
                                new PagedResponseBase<>(
                                        res.getRequest(),
                                        res.getStatusCode(),
                                        res.getHeaders(),
                                        res.getValue().getValue(),
                                        res.getValue().getNextLink(),
                                        null));
    }

    /**
     * Gets a list of all device classes (unique combinations of device manufacturer and model) for all devices
     * connected to Device Update for IoT Hub.
     *
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of all device classes (unique combinations of device manufacturer and model) for all devices
     *     connected to Device Update for IoT Hub.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DeviceClass> getAllDeviceClassesAsync() {
        return new PagedFlux<>(
                () -> getAllDeviceClassesSinglePageAsync(),
                nextLink -> getAllDeviceClassesNextSinglePageAsync(nextLink));
    }

    /**
     * Gets a list of all device classes (unique combinations of device manufacturer and model) for all devices
     * connected to Device Update for IoT Hub.
     *
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of all device classes (unique combinations of device manufacturer and model) for all devices
     *     connected to Device Update for IoT Hub.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeviceClass> getAllDeviceClasses() {
        return new PagedIterable<>(getAllDeviceClassesAsync());
    }

    /**
     * Gets the properties of a device class.
     *
     * @param deviceClassId Device class identifier.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the properties of a device class.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DeviceClass>> getDeviceClassWithResponseAsync(String deviceClassId) {
        final String accept = "application/json";
        return service.getDeviceClass(
                this.client.getAccountEndpoint(), this.client.getInstanceId(), deviceClassId, accept);
    }

    /**
     * Gets the properties of a device class.
     *
     * @param deviceClassId Device class identifier.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the properties of a device class.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DeviceClass> getDeviceClassAsync(String deviceClassId) {
        return getDeviceClassWithResponseAsync(deviceClassId)
                .flatMap(
                        (Response<DeviceClass> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Gets the properties of a device class.
     *
     * @param deviceClassId Device class identifier.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the properties of a device class.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DeviceClass getDeviceClass(String deviceClassId) {
        return getDeviceClassAsync(deviceClassId).block();
    }

    /**
     * Gets a list of device identifiers in a device class.
     *
     * @param deviceClassId Device class identifier.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of device identifiers in a device class.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<String>> getDeviceClassDeviceIdsSinglePageAsync(String deviceClassId) {
        final String accept = "application/json";
        return service.getDeviceClassDeviceIds(
                        this.client.getAccountEndpoint(), this.client.getInstanceId(), deviceClassId, accept)
                .map(
                        res ->
                                new PagedResponseBase<>(
                                        res.getRequest(),
                                        res.getStatusCode(),
                                        res.getHeaders(),
                                        res.getValue().getValue(),
                                        res.getValue().getNextLink(),
                                        null));
    }

    /**
     * Gets a list of device identifiers in a device class.
     *
     * @param deviceClassId Device class identifier.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of device identifiers in a device class.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> getDeviceClassDeviceIdsAsync(String deviceClassId) {
        return new PagedFlux<>(
                () -> getDeviceClassDeviceIdsSinglePageAsync(deviceClassId),
                nextLink -> getDeviceClassDeviceIdsNextSinglePageAsync(nextLink));
    }

    /**
     * Gets a list of device identifiers in a device class.
     *
     * @param deviceClassId Device class identifier.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of device identifiers in a device class.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> getDeviceClassDeviceIds(String deviceClassId) {
        return new PagedIterable<>(getDeviceClassDeviceIdsAsync(deviceClassId));
    }

    /**
     * Gets a list of installable updates for a device class.
     *
     * @param deviceClassId Device class identifier.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of installable updates for a device class.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<UpdateId>> getDeviceClassInstallableUpdatesSinglePageAsync(String deviceClassId) {
        final String accept = "application/json";
        return service.getDeviceClassInstallableUpdates(
                        this.client.getAccountEndpoint(), this.client.getInstanceId(), deviceClassId, accept)
                .map(
                        res ->
                                new PagedResponseBase<>(
                                        res.getRequest(),
                                        res.getStatusCode(),
                                        res.getHeaders(),
                                        res.getValue().getValue(),
                                        res.getValue().getNextLink(),
                                        null));
    }

    /**
     * Gets a list of installable updates for a device class.
     *
     * @param deviceClassId Device class identifier.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of installable updates for a device class.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<UpdateId> getDeviceClassInstallableUpdatesAsync(String deviceClassId) {
        return new PagedFlux<>(
                () -> getDeviceClassInstallableUpdatesSinglePageAsync(deviceClassId),
                nextLink -> getDeviceClassInstallableUpdatesNextSinglePageAsync(nextLink));
    }

    /**
     * Gets a list of installable updates for a device class.
     *
     * @param deviceClassId Device class identifier.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of installable updates for a device class.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<UpdateId> getDeviceClassInstallableUpdates(String deviceClassId) {
        return new PagedIterable<>(getDeviceClassInstallableUpdatesAsync(deviceClassId));
    }

    /**
     * Gets a list of devices connected to Device Update for IoT Hub.
     *
     * @param filter Restricts the set of devices returned. You can only filter on device GroupId.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of devices connected to Device Update for IoT Hub.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<Device>> getAllDevicesSinglePageAsync(String filter) {
        final String accept = "application/json";
        return service.getAllDevices(this.client.getAccountEndpoint(), this.client.getInstanceId(), filter, accept)
                .map(
                        res ->
                                new PagedResponseBase<>(
                                        res.getRequest(),
                                        res.getStatusCode(),
                                        res.getHeaders(),
                                        res.getValue().getValue(),
                                        res.getValue().getNextLink(),
                                        null));
    }

    /**
     * Gets a list of devices connected to Device Update for IoT Hub.
     *
     * @param filter Restricts the set of devices returned. You can only filter on device GroupId.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of devices connected to Device Update for IoT Hub.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<Device> getAllDevicesAsync(String filter) {
        return new PagedFlux<>(
                () -> getAllDevicesSinglePageAsync(filter), nextLink -> getAllDevicesNextSinglePageAsync(nextLink));
    }

    /**
     * Gets a list of devices connected to Device Update for IoT Hub.
     *
     * @param filter Restricts the set of devices returned. You can only filter on device GroupId.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of devices connected to Device Update for IoT Hub.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Device> getAllDevices(String filter) {
        return new PagedIterable<>(getAllDevicesAsync(filter));
    }

    /**
     * Gets the device properties and latest deployment status for a device connected to Device Update for IoT Hub.
     *
     * @param deviceId Device identifier in Azure IOT Hub.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the device properties and latest deployment status for a device connected to Device Update for IoT Hub.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Device>> getDeviceWithResponseAsync(String deviceId) {
        final String accept = "application/json";
        return service.getDevice(this.client.getAccountEndpoint(), this.client.getInstanceId(), deviceId, accept);
    }

    /**
     * Gets the device properties and latest deployment status for a device connected to Device Update for IoT Hub.
     *
     * @param deviceId Device identifier in Azure IOT Hub.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the device properties and latest deployment status for a device connected to Device Update for IoT Hub.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Device> getDeviceAsync(String deviceId) {
        return getDeviceWithResponseAsync(deviceId)
                .flatMap(
                        (Response<Device> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Gets the device properties and latest deployment status for a device connected to Device Update for IoT Hub.
     *
     * @param deviceId Device identifier in Azure IOT Hub.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the device properties and latest deployment status for a device connected to Device Update for IoT Hub.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Device getDevice(String deviceId) {
        return getDeviceAsync(deviceId).block();
    }

    /**
     * Gets the breakdown of how many devices are on their latest update, have new updates available, or are in progress
     * receiving new updates.
     *
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the breakdown of how many devices are on their latest update, have new updates available, or are in
     *     progress receiving new updates.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<UpdateCompliance>> getUpdateComplianceWithResponseAsync() {
        final String accept = "application/json";
        return service.getUpdateCompliance(this.client.getAccountEndpoint(), this.client.getInstanceId(), accept);
    }

    /**
     * Gets the breakdown of how many devices are on their latest update, have new updates available, or are in progress
     * receiving new updates.
     *
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the breakdown of how many devices are on their latest update, have new updates available, or are in
     *     progress receiving new updates.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<UpdateCompliance> getUpdateComplianceAsync() {
        return getUpdateComplianceWithResponseAsync()
                .flatMap(
                        (Response<UpdateCompliance> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Gets the breakdown of how many devices are on their latest update, have new updates available, or are in progress
     * receiving new updates.
     *
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the breakdown of how many devices are on their latest update, have new updates available, or are in
     *     progress receiving new updates.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UpdateCompliance getUpdateCompliance() {
        return getUpdateComplianceAsync().block();
    }

    /**
     * Gets a list of available group device tags for all devices connected to Device Update for IoT Hub.
     *
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of available group device tags for all devices connected to Device Update for IoT Hub.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<DeviceTag>> getAllDeviceTagsSinglePageAsync() {
        final String accept = "application/json";
        return service.getAllDeviceTags(this.client.getAccountEndpoint(), this.client.getInstanceId(), accept)
                .map(
                        res ->
                                new PagedResponseBase<>(
                                        res.getRequest(),
                                        res.getStatusCode(),
                                        res.getHeaders(),
                                        res.getValue().getValue(),
                                        res.getValue().getNextLink(),
                                        null));
    }

    /**
     * Gets a list of available group device tags for all devices connected to Device Update for IoT Hub.
     *
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of available group device tags for all devices connected to Device Update for IoT Hub.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DeviceTag> getAllDeviceTagsAsync() {
        return new PagedFlux<>(
                () -> getAllDeviceTagsSinglePageAsync(), nextLink -> getAllDeviceTagsNextSinglePageAsync(nextLink));
    }

    /**
     * Gets a list of available group device tags for all devices connected to Device Update for IoT Hub.
     *
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of available group device tags for all devices connected to Device Update for IoT Hub.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeviceTag> getAllDeviceTags() {
        return new PagedIterable<>(getAllDeviceTagsAsync());
    }

    /**
     * Gets a count of how many devices have a device tag.
     *
     * @param tagName Tag name.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a count of how many devices have a device tag.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DeviceTag>> getDeviceTagWithResponseAsync(String tagName) {
        final String accept = "application/json";
        return service.getDeviceTag(this.client.getAccountEndpoint(), this.client.getInstanceId(), tagName, accept);
    }

    /**
     * Gets a count of how many devices have a device tag.
     *
     * @param tagName Tag name.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a count of how many devices have a device tag.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DeviceTag> getDeviceTagAsync(String tagName) {
        return getDeviceTagWithResponseAsync(tagName)
                .flatMap(
                        (Response<DeviceTag> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Gets a count of how many devices have a device tag.
     *
     * @param tagName Tag name.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a count of how many devices have a device tag.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DeviceTag getDeviceTag(String tagName) {
        return getDeviceTagAsync(tagName).block();
    }

    /**
     * Gets a list of all device groups.
     *
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of all device groups.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<Group>> getAllGroupsSinglePageAsync() {
        final String accept = "application/json";
        return service.getAllGroups(this.client.getAccountEndpoint(), this.client.getInstanceId(), accept)
                .map(
                        res ->
                                new PagedResponseBase<>(
                                        res.getRequest(),
                                        res.getStatusCode(),
                                        res.getHeaders(),
                                        res.getValue().getValue(),
                                        res.getValue().getNextLink(),
                                        null));
    }

    /**
     * Gets a list of all device groups.
     *
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of all device groups.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<Group> getAllGroupsAsync() {
        return new PagedFlux<>(
                () -> getAllGroupsSinglePageAsync(), nextLink -> getAllGroupsNextSinglePageAsync(nextLink));
    }

    /**
     * Gets a list of all device groups.
     *
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of all device groups.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Group> getAllGroups() {
        return new PagedIterable<>(getAllGroupsAsync());
    }

    /**
     * Gets the properties of a group.
     *
     * @param groupId Group identifier.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the properties of a group.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Group>> getGroupWithResponseAsync(String groupId) {
        final String accept = "application/json";
        return service.getGroup(this.client.getAccountEndpoint(), this.client.getInstanceId(), groupId, accept);
    }

    /**
     * Gets the properties of a group.
     *
     * @param groupId Group identifier.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the properties of a group.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Group> getGroupAsync(String groupId) {
        return getGroupWithResponseAsync(groupId)
                .flatMap(
                        (Response<Group> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Gets the properties of a group.
     *
     * @param groupId Group identifier.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the properties of a group.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Group getGroup(String groupId) {
        return getGroupAsync(groupId).block();
    }

    /**
     * Create or update a device group.
     *
     * @param groupId Group identifier.
     * @param group The group properties.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return group details.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Group>> createOrUpdateGroupWithResponseAsync(String groupId, Group group) {
        final String accept = "application/json";
        return service.createOrUpdateGroup(
                this.client.getAccountEndpoint(), this.client.getInstanceId(), groupId, group, accept);
    }

    /**
     * Create or update a device group.
     *
     * @param groupId Group identifier.
     * @param group The group properties.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return group details.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Group> createOrUpdateGroupAsync(String groupId, Group group) {
        return createOrUpdateGroupWithResponseAsync(groupId, group)
                .flatMap(
                        (Response<Group> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Create or update a device group.
     *
     * @param groupId Group identifier.
     * @param group The group properties.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return group details.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Group createOrUpdateGroup(String groupId, Group group) {
        return createOrUpdateGroupAsync(groupId, group).block();
    }

    /**
     * Deletes a device group.
     *
     * @param groupId Group identifier.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteGroupWithResponseAsync(String groupId) {
        return service.deleteGroup(this.client.getAccountEndpoint(), this.client.getInstanceId(), groupId);
    }

    /**
     * Deletes a device group.
     *
     * @param groupId Group identifier.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteGroupAsync(String groupId) {
        return deleteGroupWithResponseAsync(groupId).flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Deletes a device group.
     *
     * @param groupId Group identifier.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteGroup(String groupId) {
        deleteGroupAsync(groupId).block();
    }

    /**
     * Get group update compliance information such as how many devices are on their latest update, how many need new
     * updates, and how many are in progress on receiving a new update.
     *
     * @param groupId Group identifier.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return group update compliance information such as how many devices are on their latest update, how many need
     *     new updates, and how many are in progress on receiving a new update.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<UpdateCompliance>> getGroupUpdateComplianceWithResponseAsync(String groupId) {
        final String accept = "application/json";
        return service.getGroupUpdateCompliance(
                this.client.getAccountEndpoint(), this.client.getInstanceId(), groupId, accept);
    }

    /**
     * Get group update compliance information such as how many devices are on their latest update, how many need new
     * updates, and how many are in progress on receiving a new update.
     *
     * @param groupId Group identifier.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return group update compliance information such as how many devices are on their latest update, how many need
     *     new updates, and how many are in progress on receiving a new update.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<UpdateCompliance> getGroupUpdateComplianceAsync(String groupId) {
        return getGroupUpdateComplianceWithResponseAsync(groupId)
                .flatMap(
                        (Response<UpdateCompliance> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get group update compliance information such as how many devices are on their latest update, how many need new
     * updates, and how many are in progress on receiving a new update.
     *
     * @param groupId Group identifier.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return group update compliance information such as how many devices are on their latest update, how many need
     *     new updates, and how many are in progress on receiving a new update.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UpdateCompliance getGroupUpdateCompliance(String groupId) {
        return getGroupUpdateComplianceAsync(groupId).block();
    }

    /**
     * Get the best available updates for a group and a count of how many devices need each update.
     *
     * @param groupId Group identifier.
     * @param filter Restricts the set of bestUpdates returned. You can filter on update Provider, Name and Version
     *     property.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the best available updates for a group and a count of how many devices need each update.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<UpdatableDevices>> getGroupBestUpdatesSinglePageAsync(String groupId, String filter) {
        final String accept = "application/json";
        return service.getGroupBestUpdates(
                        this.client.getAccountEndpoint(), this.client.getInstanceId(), groupId, filter, accept)
                .map(
                        res ->
                                new PagedResponseBase<>(
                                        res.getRequest(),
                                        res.getStatusCode(),
                                        res.getHeaders(),
                                        res.getValue().getValue(),
                                        res.getValue().getNextLink(),
                                        null));
    }

    /**
     * Get the best available updates for a group and a count of how many devices need each update.
     *
     * @param groupId Group identifier.
     * @param filter Restricts the set of bestUpdates returned. You can filter on update Provider, Name and Version
     *     property.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the best available updates for a group and a count of how many devices need each update.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<UpdatableDevices> getGroupBestUpdatesAsync(String groupId, String filter) {
        return new PagedFlux<>(
                () -> getGroupBestUpdatesSinglePageAsync(groupId, filter),
                nextLink -> getGroupBestUpdatesNextSinglePageAsync(nextLink));
    }

    /**
     * Get the best available updates for a group and a count of how many devices need each update.
     *
     * @param groupId Group identifier.
     * @param filter Restricts the set of bestUpdates returned. You can filter on update Provider, Name and Version
     *     property.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the best available updates for a group and a count of how many devices need each update.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<UpdatableDevices> getGroupBestUpdates(String groupId, String filter) {
        return new PagedIterable<>(getGroupBestUpdatesAsync(groupId, filter));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink The nextLink parameter.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the list of device classes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<DeviceClass>> getAllDeviceClassesNextSinglePageAsync(String nextLink) {
        final String accept = "application/json";
        return service.getAllDeviceClassesNext(nextLink, this.client.getAccountEndpoint(), accept)
                .map(
                        res ->
                                new PagedResponseBase<>(
                                        res.getRequest(),
                                        res.getStatusCode(),
                                        res.getHeaders(),
                                        res.getValue().getValue(),
                                        res.getValue().getNextLink(),
                                        null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink The nextLink parameter.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the list of strings with server paging support.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<String>> getDeviceClassDeviceIdsNextSinglePageAsync(String nextLink) {
        final String accept = "application/json";
        return service.getDeviceClassDeviceIdsNext(nextLink, this.client.getAccountEndpoint(), accept)
                .map(
                        res ->
                                new PagedResponseBase<>(
                                        res.getRequest(),
                                        res.getStatusCode(),
                                        res.getHeaders(),
                                        res.getValue().getValue(),
                                        res.getValue().getNextLink(),
                                        null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink The nextLink parameter.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the list of update identities.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<UpdateId>> getDeviceClassInstallableUpdatesNextSinglePageAsync(String nextLink) {
        final String accept = "application/json";
        return service.getDeviceClassInstallableUpdatesNext(nextLink, this.client.getAccountEndpoint(), accept)
                .map(
                        res ->
                                new PagedResponseBase<>(
                                        res.getRequest(),
                                        res.getStatusCode(),
                                        res.getHeaders(),
                                        res.getValue().getValue(),
                                        res.getValue().getNextLink(),
                                        null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink The nextLink parameter.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the list of devices.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<Device>> getAllDevicesNextSinglePageAsync(String nextLink) {
        final String accept = "application/json";
        return service.getAllDevicesNext(nextLink, this.client.getAccountEndpoint(), accept)
                .map(
                        res ->
                                new PagedResponseBase<>(
                                        res.getRequest(),
                                        res.getStatusCode(),
                                        res.getHeaders(),
                                        res.getValue().getValue(),
                                        res.getValue().getNextLink(),
                                        null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink The nextLink parameter.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the list of device tags.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<DeviceTag>> getAllDeviceTagsNextSinglePageAsync(String nextLink) {
        final String accept = "application/json";
        return service.getAllDeviceTagsNext(nextLink, this.client.getAccountEndpoint(), accept)
                .map(
                        res ->
                                new PagedResponseBase<>(
                                        res.getRequest(),
                                        res.getStatusCode(),
                                        res.getHeaders(),
                                        res.getValue().getValue(),
                                        res.getValue().getNextLink(),
                                        null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink The nextLink parameter.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the list of groups.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<Group>> getAllGroupsNextSinglePageAsync(String nextLink) {
        final String accept = "application/json";
        return service.getAllGroupsNext(nextLink, this.client.getAccountEndpoint(), accept)
                .map(
                        res ->
                                new PagedResponseBase<>(
                                        res.getRequest(),
                                        res.getStatusCode(),
                                        res.getHeaders(),
                                        res.getValue().getValue(),
                                        res.getValue().getNextLink(),
                                        null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink The nextLink parameter.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the list of updatable devices.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<UpdatableDevices>> getGroupBestUpdatesNextSinglePageAsync(String nextLink) {
        final String accept = "application/json";
        return service.getGroupBestUpdatesNext(nextLink, this.client.getAccountEndpoint(), accept)
                .map(
                        res ->
                                new PagedResponseBase<>(
                                        res.getRequest(),
                                        res.getStatusCode(),
                                        res.getHeaders(),
                                        res.getValue().getValue(),
                                        res.getValue().getNextLink(),
                                        null));
    }
}
