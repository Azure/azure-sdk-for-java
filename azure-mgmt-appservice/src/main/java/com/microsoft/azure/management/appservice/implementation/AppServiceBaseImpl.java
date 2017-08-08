/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.microsoft.azure.Page;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.HostNameBinding;
import com.microsoft.azure.management.appservice.OperatingSystem;
import com.microsoft.azure.management.appservice.PricingTier;
import com.microsoft.azure.management.appservice.PublishingProfile;
import com.microsoft.azure.management.appservice.WebAppBase;
import com.microsoft.azure.management.appservice.WebAppSourceControl;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    AppServiceBaseImpl(String name, SiteInner innerObject, SiteConfigResourceInner configObject, AppServiceManager manager) {
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
    Observable<SiteConfigResourceInner> getConfigInner() {
        return this.manager().inner().webApps().getConfigurationAsync(resourceGroupName(), name());
    }

    @Override
    Observable<SiteConfigResourceInner> createOrUpdateSiteConfig(SiteConfigResourceInner siteConfig) {
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
    public Map<String, HostNameBinding> getHostNameBindings() {
        return getHostNameBindingsAsync().toBlocking().single();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Observable<Map<String, HostNameBinding>> getHostNameBindingsAsync() {
        return this.manager().inner().webApps().listHostNameBindingsAsync(resourceGroupName(), name())
                .flatMap(new Func1<Page<HostNameBindingInner>, Observable<HostNameBindingInner>>() {
                    @Override
                    public Observable<HostNameBindingInner> call(Page<HostNameBindingInner> hostNameBindingInnerPage) {
                        return Observable.from(hostNameBindingInnerPage.items());
                    }
                })
                .map(new Func1<HostNameBindingInner, HostNameBinding>() {
                    @Override
                    public HostNameBinding call(HostNameBindingInner hostNameBindingInner) {
                        return new HostNameBindingImpl<>(hostNameBindingInner, (FluentImplT) AppServiceBaseImpl.this);
                    }
                }).toList()
                .map(new Func1<List<HostNameBinding>, Map<String, HostNameBinding>>() {
                    @Override
                    public Map<String, HostNameBinding> call(List<HostNameBinding> hostNameBindings) {
                        return Collections.unmodifiableMap(Maps.uniqueIndex(hostNameBindings, new Function<HostNameBinding, String>() {
                            @Override
                            public String apply(HostNameBinding input) {
                                return input.name().replace(name() + "/", "");
                            }
                        }));
                    }
                });
    }

    @Override
    public PublishingProfile getPublishingProfile() {
        return getPublishingProfileAsync().toBlocking().single();
    }

    public Observable<PublishingProfile> getPublishingProfileAsync() {
        return manager().inner().webApps().listPublishingProfileXmlWithSecretsAsync(resourceGroupName(), name(), new CsmPublishingProfileOptionsInner())
                .map(new Func1<InputStream, PublishingProfile>() {
                    @Override
                    public PublishingProfile call(InputStream stream) {
                        try {
                            String xml = CharStreams.toString(new InputStreamReader(stream));
                            return new PublishingProfileImpl(xml, AppServiceBaseImpl.this);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    @Override
    public WebAppSourceControl getSourceControl() {
        return getSourceControlAsync().toBlocking().single();
    }

    @Override
    public Observable<WebAppSourceControl> getSourceControlAsync() {
        return manager().inner().webApps().getSourceControlAsync(resourceGroupName(), name())
                .map(new Func1<SiteSourceControlInner, WebAppSourceControl>() {
                    @Override
                    public WebAppSourceControl call(SiteSourceControlInner siteSourceControlInner) {
                        return new WebAppSourceControlImpl<>(siteSourceControlInner, AppServiceBaseImpl.this);
                    }
                });
    }

    @Override
    Observable<MSDeployStatusInner> createMSDeploy(MSDeployInner msDeployInner) {
        return manager().inner().webApps()
                .createMSDeployOperationAsync(resourceGroupName(), name(), msDeployInner);
    }

    @Override
    public void verifyDomainOwnership(String certificateOrderName, String domainVerificationToken) {
        verifyDomainOwnershipAsync(certificateOrderName, domainVerificationToken).toObservable().toBlocking().subscribe();
    }

    @Override
    public Completable verifyDomainOwnershipAsync(String certificateOrderName, String domainVerificationToken) {
        IdentifierInner identifierInner = new IdentifierInner().withIdentifierId(domainVerificationToken);
        identifierInner.withLocation("global");
        return this.manager().inner().webApps().createOrUpdateDomainOwnershipIdentifierAsync(resourceGroupName(), name(), certificateOrderName, identifierInner)
                .map(new Func1<IdentifierInner, Void>() {
                    @Override
                    public Void call(IdentifierInner identifierInner) {
                        return null;
                    }
                }).toCompletable();
    }

    @Override
    public void start() {
        startAsync().toObservable().toBlocking().subscribe();
    }

    @Override
    public Completable startAsync() {
        return manager().inner().webApps().startAsync(resourceGroupName(), name())
                .flatMap(new Func1<Void, Observable<?>>() {
                    @Override
                    public Observable<?> call(Void aVoid) {
                        return refreshAsync();
                    }
                }).toCompletable();
    }

    @Override
    public void stop() {
        stopAsync().toObservable().toBlocking().subscribe();
    }

    @Override
    public Completable stopAsync() {
        return manager().inner().webApps().stopAsync(resourceGroupName(), name())
                .flatMap(new Func1<Void, Observable<?>>() {
                    @Override
                    public Observable<?> call(Void aVoid) {
                        return refreshAsync();
                    }
                }).toCompletable();
    }

    @Override
    public void restart() {
        restartAsync().toObservable().toBlocking().subscribe();
    }

    @Override
    public Completable restartAsync() {
        return manager().inner().webApps().restartAsync(resourceGroupName(), name())
                .flatMap(new Func1<Void, Observable<?>>() {
                    @Override
                    public Observable<?> call(Void aVoid) {
                        return refreshAsync();
                    }
                }).toCompletable();
    }

    @Override
    public void swap(String slotName) {
        swapAsync(slotName).toObservable().toBlocking().subscribe();
    }

    @Override
    public Completable swapAsync(String slotName) {
        return manager().inner().webApps().swapSlotWithProductionAsync(resourceGroupName(), name(), new CsmSlotEntityInner().withTargetSlot(slotName))
                .flatMap(new Func1<Void, Observable<?>>() {
                    @Override
                    public Observable<?> call(Void aVoid) {
                        return refreshAsync();
                    }
                }).toCompletable();
    }

    @Override
    public void applySlotConfigurations(String slotName) {
        applySlotConfigurationsAsync(slotName).toObservable().toBlocking().subscribe();
    }

    @Override
    public Completable applySlotConfigurationsAsync(String slotName) {
        return manager().inner().webApps().applySlotConfigToProductionAsync(resourceGroupName(), name(), new CsmSlotEntityInner().withTargetSlot(slotName))
                .flatMap(new Func1<Void, Observable<?>>() {
                    @Override
                    public Observable<?> call(Void aVoid) {
                        return refreshAsync();
                    }
                }).toCompletable();
    }

    @Override
    public void resetSlotConfigurations() {
        resetSlotConfigurationsAsync().toObservable().toBlocking().subscribe();
    }

    @Override
    public Completable resetSlotConfigurationsAsync() {
        return manager().inner().webApps().resetProductionSlotConfigAsync(resourceGroupName(), name())
                .flatMap(new Func1<Void, Observable<?>>() {
                    @Override
                    public Observable<?> call(Void aVoid) {
                        return refreshAsync();
                    }
                }).toCompletable();
    }

    @SuppressWarnings("unchecked")
    private AppServicePlanImpl newDefaultAppServicePlan() {
        String planName = SdkContext.randomResourceName(name() + "plan", 32);
        AppServicePlanImpl appServicePlan = (AppServicePlanImpl) (this.manager().appServicePlans()
                .define(planName))
                .withRegion(regionName());
        appServicePlan.withOperatingSystem(operatingSystem());
        if (super.creatableGroup != null && isInCreateMode()) {
            appServicePlan = appServicePlan.withNewResourceGroup(super.creatableGroup);
        } else {
            appServicePlan = appServicePlan.withExistingResourceGroup(resourceGroupName());
        }
        return appServicePlan;
    }

    public FluentImplT withNewFreeAppServicePlan() {
        return withNewAppServicePlan(OperatingSystem.WINDOWS, PricingTier.FREE_F1);
    }

    public FluentImplT withNewSharedAppServicePlan() {
        return withNewAppServicePlan(OperatingSystem.WINDOWS, PricingTier.SHARED_D1);
    }

    @SuppressWarnings("unchecked")
    FluentImplT withNewAppServicePlan(OperatingSystem operatingSystem, PricingTier pricingTier) {
        return withNewAppServicePlan(newDefaultAppServicePlan().withOperatingSystem(operatingSystem).withPricingTier(pricingTier));
    }

    public FluentImplT withNewAppServicePlan(PricingTier pricingTier) {
        return withNewAppServicePlan(operatingSystem(), pricingTier);
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withNewAppServicePlan(Creatable<AppServicePlan> appServicePlanCreatable) {
        addCreatableDependency(appServicePlanCreatable);
        String id = ResourceUtils.constructResourceId(this.manager().subscriptionId(),
                resourceGroupName(), "Microsoft.Web", "serverFarms", appServicePlanCreatable.name(), "");
        inner().withServerFarmId(id);
        inner().withReserved(((AppServicePlanImpl) appServicePlanCreatable).inner().reserved());
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withExistingAppServicePlan(AppServicePlan appServicePlan) {
        inner().withServerFarmId(appServicePlan.id());
        inner().withReserved(appServicePlan.inner().reserved());
        this.withRegion(appServicePlan.regionName());
        return (FluentImplT) this;
    }
}