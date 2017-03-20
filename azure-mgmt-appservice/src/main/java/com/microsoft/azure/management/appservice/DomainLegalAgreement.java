/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;


import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.appservice.implementation.TldLegalAgreementInner;

/**
 * An immutable client-side representation of an Azure domain legal agreement.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
@Beta
public interface DomainLegalAgreement
    extends HasInner<TldLegalAgreementInner> {
    /**
     * @return unique identifier for the agreement
     */
    String agreementKey();

    /**
     * @return agreement title
     */
    String title();

    /**
     * @return agreement details
     */
    String content();

    /**
     * @return url where a copy of the agreement details is hosted
     */
    String url();
}
