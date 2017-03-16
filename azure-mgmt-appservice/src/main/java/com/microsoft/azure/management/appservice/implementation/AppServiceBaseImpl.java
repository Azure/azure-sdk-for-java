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
import com.microsoft.azure.management.appservice.HostNameBinding;
import com.microsoft.azure.management.appservice.PublishingProfile;
import com.microsoft.azure.management.appservice.WebAppBase;
import com.microsoft.azure.management.appservice.WebAppSourceControl;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
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
 * The base implementation for web apps and function apps.
 *
 * @param <FluentT> the fluent interface, WebApp or FunctionApp
 * @param <FluentImplT> the implementation class for FluentT
 * @param <FluentWithCreateT> the definition stage that derives from Creatable
 * @param <FluentUpdateT> The definition stage that derives from Appliable
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
abstract class AppServiceBaseImpl<
    FluentT extends WebAppBase,
    FluentImplT extends AppServiceBaseImpl<FluentT, FluentImplT, FluentWithCreateT, FluentUpdateT>,
    FluentWithCreateT,
    FluentUpdateT>
        extends WebAppBaseImpl<FluentT, FluentImplT> {

    private AppServicePlanImpl appServicePlan;

    AppServiceBaseImpl(String name, SiteInner innerObject, SiteConfigInner configObject, AppServiceManager manager) {
        super(name, innerObject, configObject, manager);
    }

    @Override
    Observable<SiteInner> createOrUpdateInner(SiteInner site) {
        return this.manager().inner().webApps().createOrUpdateAsync(resourceGroupName(), name(), site);
    }

    @Override
    Observable<SiteInner> getInner() {
        return this.manager().inner().webApps().getByResourceGroupAsync(resourceGroupName(), name());
    }

    @Override
    Observable<SiteConfigInner> getConfigInner() {
        return this.manager().inner().webApps().getConfigurationAsync(resourceGroupName(), name());
    }

    @Override
    Observable<SiteConfigInner> createOrUpdateSiteConfig(SiteConfigInner siteConfig) {
        return this.manager().inner().webApps().createOrUpdateConfigurationAsync(resourceGroupName(), name(), siteConfig);
    }

    @Override
    Observable<Void> deleteHostNameBinding(String hostname) {
        return this.manager().inner().webApps().deleteHostNameBindingAsync(resourceGroupName(), name(), hostname);
    }

    @Override
    Observable<StringDictionaryInner> listAppSettings() {
        return this.manager().inner().webApps().listApplicationSettingsAsync(resourceGroupName(), name());
    }

    @Override
    Observable<StringDictionaryInner> updateAppSettings(StringDictionaryInner inner) {
        return this.manager().inner().webApps().updateApplicationSettingsAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Observable<ConnectionStringDictionaryInner> listConnectionStrings() {
        return this.manager().inner().webApps().listConnectionStringsAsync(resourceGroupName(), name());
    }

    @Override
    Observable<ConnectionStringDictionaryInner> updateConnectionStrings(ConnectionStringDictionaryInner inner) {
        return this.manager().inner().webApps().updateConnectionStringsAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Observable<SlotConfigNamesResourceInner> listSlotConfigurations() {
        return this.manager().inner().webApps().listSlotConfigurationNamesAsync(resourceGroupName(), name());
    }

    @Override
    Observable<SlotConfigNamesResourceInner> updateSlotConfigurations(SlotConfigNamesResourceInner inner) {
        return this.manager().inner().webApps().updateSlotConfigurationNamesAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Observable<SiteSourceControlInner> createOrUpdateSourceControl(SiteSourceControlInner inner) {
        return this.manager().inner().webApps().createOrUpdateSourceControlAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Observable<Void> deleteSourceControl() {
        return this.manager().inner().webApps().deleteSourceControlAsync(resourceGroupName(), name()).map(new Func1<Object, Void>() {
            @Override
            public Void call(Object o) {
                return null;
            }
        });
    }

    @Override
    Observable<SiteAuthSettingsInner> updateAuthentication(SiteAuthSettingsInner inner) {
        return manager().inner().webApps().updateAuthSettingsAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Observable<SiteAuthSettingsInner> getAuthentication() {
        return manager().inner().webApps().getAuthSettingsAsync(resourceGroupName(), name());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, HostNameBinding> getHostNameBindings() {
        List<HostNameBindingInner> collectionInner = this.manager().inner().webApps().listHostNameBindings(resourceGroupName(), name());
        List<HostNameBinding> hostNameBindings = new ArrayList<>();
        for (HostNameBindingInner inner : collectionInner) {
            hostNameBindings.add(new HostNameBindingImpl<>(inner, (FluentImplT) this));
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
        InputStream stream = this.manager().inner().webApps().listPublishingProfileXmlWithSecrets(resourceGroupName(), name());
        try {
            String xml = CharStreams.toString(new InputStreamReader(stream));
            return new PublishingProfileImpl(xml, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public WebAppSourceControl getSourceControl() {
        SiteSourceControlInner siteSourceControlInner = this.manager().inner().webApps().getSourceControl(resourceGroupName(), name());
        return new WebAppSourceControlImpl<>(siteSourceControlInner, this);
    }

    @Override
    public void verifyDomainOwnership(String certificateOrderName, String domainVerificationToken) {
        verifyDomainOwnershipAsync(certificateOrderName, domainVerificationToken).toBlocking().subscribe();
    }

    @Override
    public Observable<Void> verifyDomainOwnershipAsync(String certificateOrderName, String domainVerificationToken) {
        IdentifierInner identifierInner = new IdentifierInner().withIdentifierId(domainVerificationToken);
        identifierInner.withLocation("global");
        return this.manager().inner().webApps().createOrUpdateDomainOwnershipIdentifierAsync(resourceGroupName(), name(), certificateOrderName, identifierInner)
                .map(new Func1<IdentifierInner, Void>() {
                    @Override
                    public Void call(IdentifierInner identifierInner) {
                        return null;
                    }
                });
    }

    @Override
    public void start() {
        this.manager().inner().webApps().start(resourceGroupName(), name());
        refresh();
    }

    @Override
    public void stop() {
        this.manager().inner().webApps().stop(resourceGroupName(), name());
        refresh();
    }

    @Override
    public void restart() {
        this.manager().inner().webApps().restart(resourceGroupName(), name());
        refresh();
    }

    @Override
    public void swap(String slotName) {
        this.manager().inner().webApps().swapSlotWithProduction(resourceGroupName(), name(), new CsmSlotEntityInner().withTargetSlot(slotName));
        refresh();
    }

    @Override
    public void applySlotConfigurations(String slotName) {
        this.manager().inner().webApps().applySlotConfigToProduction(resourceGroupName(), name(), new CsmSlotEntityInner().withTargetSlot(slotName));
        refresh();
    }

    @Override
    public void resetSlotConfigurations() {
        this.manager().inner().webApps().resetProductionSlotConfig(resourceGroupName(), name());
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withNewAppServicePlan(String name) {
        appServicePlan = (AppServicePlanImpl) this.manager().appServicePlans().define(name).withRegion(regionName());
        String id = ResourceUtils.constructResourceId(this.manager().subscriptionId(),
                resourceGroupName(), "Microsoft.Web", "serverFarms", name, "");
        inner().withServerFarmId(id);
        return (FluentImplT) this;
    }

    public FluentImplT withFreePricingTier() {
        return withPricingTier(AppServicePricingTier.FREE_F1);
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withPricingTier(AppServicePricingTier pricingTier) {
        appServicePlan = appServicePlan
                .withPricingTier(pricingTier);
        if (super.creatableGroup != null && isInCreateMode()) {
            appServicePlan = appServicePlan.withNewResourceGroup(resourceGroupName());
        } else {
            appServicePlan = appServicePlan.withExistingResourceGroup(resourceGroupName());
        }
        if (isInCreateMode()) {
            addCreatableDependency(appServicePlan);
        } else {
            addAppliableDependency(appServicePlan);
        }
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withNewAppServicePlan(String name, Region region) {
        return withNewAppServicePlan(name, region.name());
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withNewAppServicePlan(String name, String regionName) {
        withNewAppServicePlan(name).withRegion(regionName);
        appServicePlan.withRegion(regionName);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withExistingAppServicePlan(AppServicePlan appServicePlan) {
        inner().withServerFarmId(appServicePlan.id());
        if (super.creatableGroup != null && isInCreateMode()) {
            ((HasInner<ResourceGroupInner>) super.creatableGroup).inner().withLocation(appServicePlan.regionName());
        }
        this.withRegion(appServicePlan.regionName());
        return (FluentImplT) this;
    }
}