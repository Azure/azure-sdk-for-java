// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.models.AcrErrorsException;
import com.azure.containers.containerregistry.implementation.models.ManifestAttributesBase;
import com.azure.containers.containerregistry.implementation.models.ManifestAttributesManifestReferences;
import com.azure.containers.containerregistry.models.TagProperties;
import com.azure.containers.containerregistry.models.ContentProperties;
import com.azure.containers.containerregistry.models.RegistryArtifactProperties;
import com.azure.containers.containerregistry.models.RepositoryProperties;
import com.azure.containers.containerregistry.models.DeleteRepositoryResult;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceExistsException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.CoreUtils;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class Utils {
    private static final String CONTINUATIONLINK_HEADER_NAME = "Link";
    private static final String LINK_MATCHER = "(<(.+)>;.*)";
    private static final Pattern CONTINUATIONLINK_PATTERN = Pattern.compile(LINK_MATCHER);

    static <T> PagedResponse<T> getPagedResponseWithContinuationToken(PagedResponse<T> listResponse) {
        return Utils.getPagedResponseWithContinuationToken(listResponse, values -> values);
    }

    static <T, R> PagedResponse<T> getPagedResponseWithContinuationToken(PagedResponse<R> listResponse, Function<List<R>, List<T>> mapperFunction) {
        String continuationLink = null;
        HttpHeaders headers = listResponse.getHeaders();

        if (headers != null) {
            String continuationLinkHeader = headers.getValue(CONTINUATIONLINK_HEADER_NAME);
            if (!CoreUtils.isNullOrEmpty(continuationLinkHeader)) {
                Matcher matcher = CONTINUATIONLINK_PATTERN.matcher(continuationLinkHeader);
                if (matcher.matches()) {
                    if (matcher.groupCount() == 2) {
                        String apiPath = matcher.group(2);
                        continuationLink = apiPath;
                    }
                }
            }
        }

        List<T> values = null;

        if (mapperFunction != null) {
            values = mapperFunction.apply(listResponse.getValue());
        }

        return new PagedResponseBase<String, T>(
            listResponse.getRequest(),
            listResponse.getStatusCode(),
            listResponse.getHeaders(),
            values,
            continuationLink,
            null
        );
    }

    static Throwable mapException(Throwable exception) {
        if (!(exception instanceof AcrErrorsException)) {
            return exception;
        }

        final AcrErrorsException errorsException = ((AcrErrorsException) exception);
        final HttpResponse errorHttpResponse = errorsException.getResponse();

        final int statusCode = errorHttpResponse.getStatusCode();
        final String errorDetail = errorsException.getMessage();

        switch (statusCode) {
            case 401:
                return new ClientAuthenticationException(errorDetail, errorsException.getResponse(), exception);
            case 404:
                return new ResourceNotFoundException(errorDetail, errorsException.getResponse(), exception);
            case 409:
                return new ResourceExistsException(errorDetail, errorsException.getResponse(), exception);
            case 412:
                return new ResourceModifiedException(errorDetail, errorsException.getResponse(), exception);
            default:
                return new HttpResponseException(errorDetail, errorsException.getResponse(), exception);
        }
    }

    static <T, R> Response<R> mapResponse(Response<T> response, Function<T, R> mapFunction)
    {
        if (response == null || mapFunction == null) {
            return null;
        }

        return new ResponseBase<String, R>(
            response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            mapFunction.apply(response.getValue()),
            null);
    }

    static DeleteRepositoryResult mapDeleteRepositoryResult(com.azure.containers.containerregistry.implementation.models.DeleteRepositoryResult resultImpl) {
        if (resultImpl == null) {
            return null;
        }

        return new DeleteRepositoryResult(resultImpl.getDeletedRegistryArtifactDigests(), resultImpl.getDeletedTags());
    }

    static RepositoryProperties mapRepositoryProperties(com.azure.containers.containerregistry.implementation.models.RepositoryProperties propsImpl) {
        if (propsImpl == null) {
            return null;
        }

        ContentProperties writeableProps = mapContentProperties(propsImpl.getWriteableProperties());

        return new RepositoryProperties(
            propsImpl.getName(),
            propsImpl.getRegistryArtifactCount(),
            propsImpl.getTagCount(),
            writeableProps,
            propsImpl.getCreatedOn(),
            propsImpl.getLastUpdatedOn()
        );
    }

    static RegistryArtifactProperties mapArtifactProperties(com.azure.containers.containerregistry.implementation.models.RegistryArtifactProperties propsImpl) {
        if (propsImpl == null) {
            return null;
        }

        List<RegistryArtifactProperties> registryArtifacts = getRegistryArtifacts(propsImpl.getRegistryArtifacts());

        return new RegistryArtifactProperties(
            propsImpl.getDigest(),
            propsImpl.getManifestProperties(),
            registryArtifacts,
            propsImpl.getCpuArchitecture(),
            propsImpl.getOperatingSystem(),
            propsImpl.getCreatedOn(),
            propsImpl.getLastUpdatedOn(),
            propsImpl.getTags(),
            propsImpl.getSize());
    }

    static ContentProperties mapContentProperties(com.azure.containers.containerregistry.implementation.models.ContentProperties props) {

        if (props == null) {
            return null;
        }

        return new ContentProperties()
            .setCanDelete(props.isCanDelete())
            .setCanList(props.isCanList())
            .setCanRead(props.isCanRead())
            .setCanWrite(props.isCanWrite());
    }

    static List<RegistryArtifactProperties> getRegistryArtifacts(List<ManifestAttributesManifestReferences> artifacts) {
        if (artifacts == null) {
            return null;
        }

        return artifacts.stream()
            .map(artifact -> new RegistryArtifactProperties(
                artifact.getDigest(),
                null,
                null,
                artifact.getCpuArchitecture(),
                artifact.getOperatingSystem(),
                null,
                null,
                null,
                null
            )).collect(Collectors.toList());
    }

    static List<RegistryArtifactProperties> getRegistryArtifactsProperties(List<ManifestAttributesBase> baseArtifacts)
    {
        if (baseArtifacts == null) {
            return null;
        }

        return baseArtifacts.stream().map(value -> new RegistryArtifactProperties(
                value.getDigest(),
                value.getWriteableProperties(),
                getRegistryArtifacts(value.getReferences()),
                value.getCpuArchitecture(),
                value.getOperatingSystem(),
                value.getCreatedOn(),
                value.getLastUpdatedOn(),
                value.getTags(),
                value.getSize()
            )

        ).collect(Collectors.toList());
    }

    static TagProperties mapTagProperties(com.azure.containers.containerregistry.implementation.models.TagProperties props) {
        if (props == null) {
            return null;
        }

        ContentProperties contentProperties = mapContentProperties(props.getWriteableProperties());

        return new TagProperties(
            props.getName(),
            props.getRepository(),
            props.getDigest(),
            contentProperties,
            props.getCreatedOn(),
            props.getLastUpdatedOn()
        );
    }

}
