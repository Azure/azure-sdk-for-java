// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import reactor.core.publisher.Mono;

import java.io.File;
import java.io.InputStream;

/**
 * Provides access to OneDeploy.
 */
public interface SupportsOneDeploy {

    /**
     * Deploy a file to Azure site.
     *
     * @param type the deploy type
     * @param file the file to upload
     */
    void deploy(DeployType type, File file);

    /**
     * Deploy a file to Azure site.
     *
     * @param type the deploy type
     * @param file the file to upload
     * @return the completable of the operation
     */
    Mono<Void> deployAsync(DeployType type, File file);

    /**
     * Deploy a file to Azure site.
     *
     * @param type the deploy type
     * @param file the file to upload
     * @param deployOptions the deploy options
     */
    void deploy(DeployType type, File file, DeployOptions deployOptions);

    /**
     * Deploy a file to Azure site.
     *
     * @param type the deploy type
     * @param file the file to upload
     * @param deployOptions the deploy options
     * @return the completable of the operation
     */
    Mono<Void> deployAsync(DeployType type, File file, DeployOptions deployOptions);

    /**
     * Deploy a file to Azure site.
     *
     * @param type the deploy type
     * @param file the file to upload
     * @param length the length of the file
     */
    void deploy(DeployType type, InputStream file, long length);

    /**
     * Deploy a file to Azure site.
     *
     * @param type the deploy type
     * @param file the file to upload
     * @param length the length of the file
     * @return the completable of the operation
     */
    Mono<Void> deployAsync(DeployType type, InputStream file, long length);

    /**
     * Deploy a file to Azure site.
     *
     * @param type the deploy type
     * @param file the file to upload
     * @param length the length of the file
     * @param deployOptions the deploy options
     */
    void deploy(DeployType type, InputStream file, long length, DeployOptions deployOptions);

    /**
     * Deploy a file to Azure site.
     *
     * @param type the deploy type
     * @param file the file to upload
     * @param length the length of the file
     * @param deployOptions the deploy options
     * @return the completable of the operation
     */
    Mono<Void> deployAsync(DeployType type, InputStream file, long length, DeployOptions deployOptions);
}
