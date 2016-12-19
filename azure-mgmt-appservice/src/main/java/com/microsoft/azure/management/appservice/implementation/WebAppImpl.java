/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.AppServicePricingTier;
import com.microsoft.azure.management.appservice.DeploymentSlots;
import com.microsoft.azure.management.appservice.HostNameBinding;
import com.microsoft.azure.management.appservice.PublishingProfile;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.appservice.WebAppSourceControl;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.ResourceGroupInner;
import rx.Observable;
import rx.functions.Func1;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The implementation for {@link WebApp}.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
class WebAppImpl
        extends WebAppBaseImpl<WebApp, WebAppImpl>
        implements
            WebApp,
            WebApp.Definition,
            WebApp.Update,
            WebApp.UpdateStages.WithNewAppServicePlan {

    private DeploymentSlots deploymentSlots;
    private AppServicePlanImpl appServicePlan;

    WebAppImpl(String name, SiteInner innerObject, SiteConfigInner configObject, final WebAppsInner client, AppServiceManager manager, WebSiteManagementClientImpl serviceClient) {
        super(name, innerObject, configObject, client, manager, serviceClient);
    }

    @Override
    Observable<SiteInner> createOrUpdateInner(SiteInner site) {
        return client.createOrUpdateAsync(resourceGroupName(), name(), site);
    }

    @Override
    Observable<SiteInner> getInner() {
        return client.getAsync(resourceGroupName(), name());
    }

    @Override
    Observable<SiteConfigInner> getConfigInner() {
        return client.getConfigurationAsync(resourceGroupName(), name());
    }

    @Override
    Observable<SiteConfigInner> createOrUpdateSiteConfig(SiteConfigInner siteConfig) {
        return client.createOrUpdateConfigurationAsync(resourceGroupName(), name(), siteConfig);
    }

    @Override
    Observable<Void> deleteHostNameBinding(String hostname) {
        return client.deleteHostNameBindingAsync(resourceGroupName(), name(), hostname);
    }

    @Override
    Observable<StringDictionaryInner> listAppSettings() {
        return client.listApplicationSettingsAsync(resourceGroupName(), name());
    }

    @Override
    Observable<StringDictionaryInner> updateAppSettings(StringDictionaryInner inner) {
        return client.updateApplicationSettingsAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Observable<ConnectionStringDictionaryInner> listConnectionStrings() {
        return client.listConnectionStringsAsync(resourceGroupName(), name());
    }

    @Override
    Observable<ConnectionStringDictionaryInner> updateConnectionStrings(ConnectionStringDictionaryInner inner) {
        return client.updateConnectionStringsAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Observable<SlotConfigNamesResourceInner> listSlotConfigurations() {
        return client.listSlotConfigurationNamesAsync(resourceGroupName(), name());
    }

    @Override
    Observable<SlotConfigNamesResourceInner> updateSlotConfigurations(SlotConfigNamesResourceInner inner) {
        return client.updateSlotConfigurationNamesAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Observable<SiteSourceControlInner> createOrUpdateSourceControl(SiteSourceControlInner inner) {
        return client.createOrUpdateSourceControlAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Observable<Void> deleteSourceControl() {
        return client.deleteSourceControlAsync(resourceGroupName(), name()).map(new Func1<Object, Void>() {
            @Override
            public Void call(Object o) {
                return null;
            }
        });
    }

    @Override
    public DeploymentSlots deploymentSlots() {
        if (deploymentSlots == null) {
            deploymentSlots = new DeploymentSlotsImpl(this, client, myManager, serviceClient);
        }
        return deploymentSlots;
    }

    @Override
    public Map<String, HostNameBinding> getHostNameBindings() {
        List<HostNameBindingInner> collectionInner = client.listHostNameBindings(resourceGroupName(), name());
        List<HostNameBinding> hostNameBindings = new ArrayList<>();
        for (HostNameBindingInner inner : collectionInner) {
            hostNameBindings.add(new HostNameBindingImpl<>(inner, this, client));
        }
        return Collections.unmodifiableMap(Maps.uniqueIndex(hostNameBindings, new Function<HostNameBinding, String>() {
            @Override
            public String apply(HostNameBinding input) {
                return input.name().replace(name() + "/", "");
            }
        }));
    }

    @Override
    public PublishingProfile getPublishingProfile() {
        InputStream stream = client.listPublishingProfileXmlWithSecrets(resourceGroupName(), name());
        try {
            String xml = CharStreams.toString(new InputStreamReader(stream));
            return new PublishingProfileImpl(xml, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public WebAppSourceControl getSourceControl() {
        SiteSourceControlInner siteSourceControlInner = client.getSourceControl(resourceGroupName(), name());
        return new WebAppSourceControlImpl<>(siteSourceControlInner, this, serviceClient);
    }

    @Override
    public void verifyDomainOwnership(String certificateOrderName, String domainVerificationToken) {
        verifyDomainOwnershipAsync(certificateOrderName, domainVerificationToken).toBlocking().subscribe();
    }

    @Override
    public Observable<Void> verifyDomainOwnershipAsync(String certificateOrderName, String domainVerificationToken) {
        IdentifierInner identifierInner = new IdentifierInner().withIdentifierId(domainVerificationToken);
        identifierInner.withLocation("global");
        return client.createOrUpdateDomainOwnershipIdentifierAsync(resourceGroupName(), name(), certificateOrderName, identifierInner)
                .map(new Func1<IdentifierInner, Void>() {
                    @Override
                    public Void call(IdentifierInner identifierInner) {
                        return null;
                    }
                });
    }

    @Override
    public void start() {
        client.start(resourceGroupName(), name());
        refresh();
    }

    @Override
    public void stop() {
        client.stop(resourceGroupName(), name());
        refresh();
    }

    @Override
    public void restart() {
        client.restart(resourceGroupName(), name());
        refresh();
    }

    @Override
    public void swap(String slotName) {
        client.swapSlotWithProduction(resourceGroupName(), name(), new CsmSlotEntityInner().withTargetSlot(slotName));
        refresh();
    }

    @Override
    public void applySlotConfigurations(String slotName) {
        client.applySlotConfigToProduction(resourceGroupName(), name(), new CsmSlotEntityInner().withTargetSlot(slotName));
        refresh();
    }

    @Override
    public void resetSlotConfigurations() {
        client.resetProductionSlotConfig(resourceGroupName(), name());
    }

    @Override
    public WebAppImpl withNewAppServicePlan(String name) {
        appServicePlan = (AppServicePlanImpl) myManager.appServicePlans().define(name);
        String id = ResourceUtils.constructResourceId(myManager.subscriptionId(),
                resourceGroupName(), "Microsoft.Web", "serverFarms", name, "");
        inner().withServerFarmId(id);
        return this;
    }

    @Override
    public WebAppImpl withFreePricingTier() {
        return withPricingTier(AppServicePricingTier.FREE_F1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public WebAppImpl withPricingTier(AppServicePricingTier pricingTier) {
        appServicePlan = appServicePlan
                .withRegion(region())
                .withPricingTier(pricingTier);
        if (super.creatableGroup != null && isInCreateMode()) {
            appServicePlan = appServicePlan.withNewResourceGroup(resourceGroupName());
            ((Wrapper<ResourceGroupInner>) super.creatableGroup).inner().withLocation(regionName());
        } else {
            appServicePlan = appServicePlan.withExistingResourceGroup(resourceGroupName());
        }
        if (isInCreateMode()) {
            addCreatableDependency(appServicePlan);
        } else {
            addAppliableDependency(appServicePlan);
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public WebAppImpl withExistingAppServicePlan(AppServicePlan appServicePlan) {
        inner().withServerFarmId(appServicePlan.id());
        if (super.creatableGroup != null && isInCreateMode()) {
            ((Wrapper<ResourceGroupInner>) super.creatableGroup).inner().withLocation(appServicePlan.regionName());
        }
        this.withRegion(appServicePlan.regionName());
        return this;
    }
}