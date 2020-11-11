// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.model.Executable;
import java.time.OffsetDateTime;

/** A client-side representation allowing user to deploy to a web app through web deployment (MSDeploy). */
@Fluent
public interface WebDeployment extends Executable<WebDeployment>, HasParent<WebAppBase> {
    /** @return username of the deployer */
    String deployer();

    /** @return the start time of the deploy operation */
    OffsetDateTime startTime();

    /** @return the end time of the deploy operation */
    OffsetDateTime endTime();

    /** @return whether the deployment operation has completed */
    boolean complete();

    /** The entirety of web deployment parameters definition. */
    interface Definition extends DefinitionStages.WithPackageUri, DefinitionStages.WithExecute {
    }

    /** Grouping of web deployment definition stages. */
    interface DefinitionStages {
        /** The first stage of a web deployment definition. */
        interface WithPackageUri {
            /**
             * Specifies the zipped package to deploy.
             *
             * @param packageUri the URL to the package. It can be a publicly available link to the package zip, or an
             *     Azure Storage object with a SAS token
             * @return the next definition stage
             */
            WithExecute withPackageUri(String packageUri);
        }

        /** A web deployment definition stage allowing specifying whether to delete existing deployments. */
        interface WithExistingDeploymentsDeleted {
            /**
             * Specifies whether existing deployed files on the web app should be deleted.
             *
             * @param deleteExisting if set to true, all files on the web app will be deleted. Default is false.
             * @return the next definition stage
             */
            WithExecute withExistingDeploymentsDeleted(boolean deleteExisting);
        }

        /** A web deployment definition stage allowing adding more packages. */
        interface WithAddOnPackage {
            /**
             * Adds an extra package to the deployment.
             *
             * @param packageUri the URL to the package. It can be a publicly available link to the package zip, or an
             *     Azure Storage object with a SAS token
             * @return the next definition stage
             */
            WithExecute withAddOnPackage(String packageUri);
        }

        /** A web deployment definition stage allowing specifying parameters. */
        interface WithSetParameters {
            /**
             * Specifies the XML file containing the parameters.
             *
             * @param fileUri the XML file's URI
             * @return the next definition stage
             */
            WithExecute withSetParametersXmlFile(String fileUri);

            /**
             * Adds a parameter for the deployment.
             *
             * @param name name of the parameter
             * @param value the value of the parameter
             * @return the next definition stage
             */
            WithExecute withSetParameter(String name, String value);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created,
         * but also allows for any other optional settings to be specified.
         */
        interface WithExecute
            extends Executable<WebDeployment>, WithExistingDeploymentsDeleted, WithAddOnPackage, WithSetParameters {
        }
    }
}
