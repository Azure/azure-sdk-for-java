/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.blob.implementation;

import java.io.InputStream;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestFilter;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseFilter;
import com.microsoft.windowsazure.core.pipeline.jersey.ServiceFilter;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.exception.ServiceExceptionFactory;
import com.microsoft.windowsazure.services.blob.BlobContract;
import com.microsoft.windowsazure.services.blob.models.BlockList;
import com.microsoft.windowsazure.services.blob.models.CommitBlobBlocksOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobBlockOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobResult;
import com.microsoft.windowsazure.services.blob.models.CreateContainerOptions;
import com.microsoft.windowsazure.services.blob.models.DeleteBlobOptions;
import com.microsoft.windowsazure.services.blob.models.DeleteContainerOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobPropertiesOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobPropertiesResult;
import com.microsoft.windowsazure.services.blob.models.GetBlobResult;
import com.microsoft.windowsazure.services.blob.models.ListBlobBlocksOptions;
import com.microsoft.windowsazure.services.blob.models.ListBlobBlocksResult;
import com.microsoft.windowsazure.services.blob.models.ListContainersOptions;
import com.microsoft.windowsazure.services.blob.models.ListContainersResult;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

public class BlobExceptionProcessor implements BlobContract {
    private static Log log = LogFactory.getLog(BlobExceptionProcessor.class);
    private final BlobContract service;

    @Inject
    public BlobExceptionProcessor(BlobRestProxy service) {
        this.service = service;
    }

    public BlobExceptionProcessor(BlobContract service) {
        this.service = service;
    }

    @Override
    public BlobContract withFilter(ServiceFilter filter) {
        return new BlobExceptionProcessor(service.withFilter(filter));
    }

    @Override
    public BlobContract withRequestFilterFirst(
            ServiceRequestFilter serviceRequestFilter) {
        return new BlobExceptionProcessor(
                service.withRequestFilterFirst(serviceRequestFilter));
    }

    @Override
    public BlobContract withRequestFilterLast(
            ServiceRequestFilter serviceRequestFilter) {
        return new BlobExceptionProcessor(
                service.withRequestFilterLast(serviceRequestFilter));
    }

    @Override
    public BlobContract withResponseFilterFirst(
            ServiceResponseFilter serviceResponseFilter) {
        return new BlobExceptionProcessor(
                service.withResponseFilterFirst(serviceResponseFilter));
    }

    @Override
    public BlobContract withResponseFilterLast(
            ServiceResponseFilter serviceResponseFilter) {
        return new BlobExceptionProcessor(
                service.withResponseFilterLast(serviceResponseFilter));
    }

    private ServiceException processCatch(ServiceException e) {
        log.warn(e.getMessage(), e.getCause());
        return ServiceExceptionFactory.process("blob", e);
    }

    @Override
    public CreateBlobResult createBlockBlob(String container, String blob,
            InputStream contentStream) throws ServiceException {
        try {
            return service.createBlockBlob(container, blob, contentStream);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public CreateBlobResult createBlockBlob(String container, String blob,
            InputStream contentStream, CreateBlobOptions options)
            throws ServiceException {
        try {
            return service.createBlockBlob(container, blob, contentStream,
                    options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void createBlobBlock(String container, String blob, String blockId,
            InputStream contentStream) throws ServiceException {
        try {
            service.createBlobBlock(container, blob, blockId, contentStream);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void createBlobBlock(String container, String blob, String blockId,
            InputStream contentStream, CreateBlobBlockOptions options)
            throws ServiceException {
        try {
            service.createBlobBlock(container, blob, blockId, contentStream,
                    options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void commitBlobBlocks(String container, String blob,
            BlockList blockList) throws ServiceException {
        try {
            service.commitBlobBlocks(container, blob, blockList);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void commitBlobBlocks(String container, String blob,
            BlockList blockList, CommitBlobBlocksOptions options)
            throws ServiceException {
        try {
            service.commitBlobBlocks(container, blob, blockList, options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void deleteBlob(String container, String blob)
            throws ServiceException {
        try {
            service.deleteBlob(container, blob);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void deleteBlob(String container, String blob,
            DeleteBlobOptions options) throws ServiceException {
        try {
            service.deleteBlob(container, blob, options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ListContainersResult listContainers() throws ServiceException {
        try {
            return service.listContainers();
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ListContainersResult listContainers(ListContainersOptions options)
            throws ServiceException {
        try {
            return service.listContainers(options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void createContainer(String container) throws ServiceException {
        try {
            service.createContainer(container);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void createContainer(String container, CreateContainerOptions options)
            throws ServiceException {
        try {
            service.createContainer(container, options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void deleteContainer(String container) throws ServiceException {
        try {
            service.deleteContainer(container);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void deleteContainer(String container, DeleteContainerOptions options)
            throws ServiceException {
        try {
            service.deleteContainer(container, options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ListBlobBlocksResult listBlobBlocks(String container, String blob)
            throws ServiceException {
        try {
            return service.listBlobBlocks(container, blob);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ListBlobBlocksResult listBlobBlocks(String container, String blob,
            ListBlobBlocksOptions options) throws ServiceException {
        try {
            return service.listBlobBlocks(container, blob, options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public GetBlobPropertiesResult getBlobProperties(String container,
            String blob) throws ServiceException {
        try {
            return service.getBlobProperties(container, blob);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public GetBlobPropertiesResult getBlobProperties(String container,
            String blob, GetBlobPropertiesOptions options)
            throws ServiceException {
        try {
            return service.getBlobProperties(container, blob, options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public GetBlobResult getBlob(String container, String blob)
            throws ServiceException {
        try {
            return service.getBlob(container, blob);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public GetBlobResult getBlob(String container, String blob,
            GetBlobOptions options) throws ServiceException {
        try {
            return service.getBlob(container, blob, options);
        } catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        } catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

}
