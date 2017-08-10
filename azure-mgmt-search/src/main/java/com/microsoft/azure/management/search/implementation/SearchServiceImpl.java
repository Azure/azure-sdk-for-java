/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.search.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableParentResourceImpl;
import com.microsoft.azure.management.search.AdminKeys;
import com.microsoft.azure.management.search.AdminKeyKind;
import com.microsoft.azure.management.search.HostingMode;
import com.microsoft.azure.management.search.ProvisioningState;
import com.microsoft.azure.management.search.QueryKey;
import com.microsoft.azure.management.search.SearchService;
import com.microsoft.azure.management.search.SearchServiceStatus;
import com.microsoft.azure.management.search.Sku;
import com.microsoft.azure.management.search.SkuName;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation for Search service and its create and update interfaces.
 */
@LangDefinition
class SearchServiceImpl
    extends GroupableParentResourceImpl<
        SearchService,
        SearchServiceInner,
        SearchServiceImpl,
        SearchServiceManager>
    implements
        SearchService,
        SearchService.Definition,
        SearchService.Update {

  SearchServiceImpl(String name,
                       final SearchServiceInner innerModel,
                       final SearchServiceManager networkManager) {
    super(name, innerModel, networkManager);
  }

  @Override
  protected void initializeChildrenFromInner() {
  }

  // Verbs

  @Override
  public Observable<SearchService> refreshAsync() {
    return super.refreshAsync().map(new Func1<SearchService, SearchService>() {
      @Override
      public SearchService call(SearchService foo) {
        SearchServiceImpl impl = (SearchServiceImpl) foo;
        impl.initializeChildrenFromInner();
        return impl;
      }
    });
  }

  @Override
  protected Observable<SearchServiceInner> getInnerAsync() {
    return this.manager().inner().services().getByResourceGroupAsync(this.resourceGroupName(), this.name());
  }

  // Helpers

  @Override
  protected void beforeCreating() {
    if (isInCreateMode()) {
      // TODO something
    }

//    // Reset and update subnets
//    this.inner().withSubnets(innersFromWrappers(this.childModels.values()));
  }

  @Override
  protected void afterCreating() {
    initializeChildrenFromInner();
  }

  @Override
  protected Observable<SearchServiceInner> createInner() {
    return this.manager().inner().services().createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner());
  }

  // Getters

  @Override
  public SearchServiceStatus status() {
    return this.inner().status();
  }

  @Override
  public String statusDetails() {
    return this.inner().statusDetails();
  }

  @Override
  public ProvisioningState provisioningState() {
    return this.inner().provisioningState();
  }

  @Override
  public HostingMode hostingMode() {
    return this.inner().hostingMode();
  }

  @Override
  public Sku sku() {
    return this.inner().sku();
  }

  @Override
  public int replicaCount() {
    return this.inner().replicaCount();
  }

  @Override
  public int partitionCount() {
    return this.inner().partitionCount();
  }

  @Override
  public AdminKeysImpl getAdminKeys() {
    return new AdminKeysImpl(this.manager().inner().adminKeys().get(this.resourceGroupName(), this.name()));
  }

  @Override
  public Observable<AdminKeys> getAdminKeysAsync() {
    return this.manager().inner().adminKeys().getAsync(this.resourceGroupName(), this.name())
        .map(new Func1<AdminKeyResultInner, AdminKeys>() {
          @Override
          public AdminKeys call(AdminKeyResultInner adminKeyResultInner) {
            return new AdminKeysImpl(adminKeyResultInner);
          }
        });
  }

  @Override
  public List<QueryKey> listQueryKeys() {
    List<QueryKey> queryKeys = new ArrayList<>();

    List<QueryKeyInner> queryKeyInners = this.manager().inner().queryKeys().listBySearchService(this.resourceGroupName(), this.name());
    if (queryKeyInners != null) {
      for (QueryKeyInner queryKeyInner : queryKeyInners) {
        queryKeys.add(new QueryKeyImpl(queryKeyInner));
      }
    }

    return Collections.unmodifiableList(queryKeys);
  }

  @Override
  public Observable<QueryKey> listQueryKeysAsync() {
    Observable<List<QueryKeyInner>> queryKeysList = this.manager().inner().queryKeys()
        .listBySearchServiceAsync(this.resourceGroupName(), this.name());

    return queryKeysList.flatMap(new Func1<List< QueryKeyInner>, Observable<QueryKeyInner>>() {
      @Override
      public Observable<QueryKeyInner> call(List<QueryKeyInner> queryKeyInners) {
        return Observable.from(queryKeyInners);
      }
    }).map(new Func1<QueryKeyInner, QueryKey>() {
      @Override
      public QueryKey call(QueryKeyInner queryKeyInner) {
        return new QueryKeyImpl(queryKeyInner);
      }
    });
  }

  // Actions

  @Override
  public AdminKeys regenerateAdminKeys(AdminKeyKind keyKind) {
    return new AdminKeysImpl(this.manager().inner().adminKeys().regenerate(this.resourceGroupName(), this.name(), keyKind));
  }

  @Override
  public Observable<AdminKeys> regenerateAdminKeysAsync(AdminKeyKind keyKind) {
    return this.manager().inner().adminKeys().regenerateAsync(this.resourceGroupName(), this.name(), keyKind)
        .map(new Func1<AdminKeyResultInner, AdminKeys>() {
          @Override
          public AdminKeys call(AdminKeyResultInner adminKeyResultInner) {
            return new AdminKeysImpl(adminKeyResultInner);
          }
        });
  }

  @Override
  public QueryKey createQueryKey(String name) {
    return new QueryKeyImpl(this.manager().inner().queryKeys().create(this.resourceGroupName(), this.name(), name));
  }

  @Override
  public Observable<QueryKey> createQueryKeyAsync(String name) {
    return this.manager().inner().queryKeys().createAsync(this.resourceGroupName(), this.name(), name)
        .map(new Func1<QueryKeyInner, QueryKey>() {
          @Override
          public QueryKey call(QueryKeyInner queryKeyInner) {
            return new QueryKeyImpl(queryKeyInner);
          }
        });
  }

  @Override
  public void deleteQueryKey(String key) {
    this.manager().inner().queryKeys().delete(this.resourceGroupName(), this.name(), key);
  }

  @Override
  public Completable deleteQueryKeyAsync(String key) {
    return this.manager().inner().queryKeys().deleteAsync(this.resourceGroupName(), this.name(), key).toCompletable();
  }

  // Setters (fluent)

  @Override
  public SearchServiceImpl withSku(SkuName skuName) {
    this.inner().withSku(new Sku().withName(skuName));
    return this;
  }

  @Override
  public SearchServiceImpl withFreeSku() {
    this.inner().withSku(new Sku().withName(SkuName.FREE));
    return this;
  }

  @Override
  public SearchServiceImpl withBasicSku() {
    this.inner().withSku(new Sku().withName(SkuName.BASIC));
    return this;
  }

  @Override
  public SearchServiceImpl withStandardSku() {
    this.inner().withSku(new Sku().withName(SkuName.STANDARD));
    return this;
  }

  @Override
  public SearchServiceImpl withReplicaCount(int replicaCount) {
    this.inner().withReplicaCount(replicaCount);
    return this;
  }

  @Override
  public SearchServiceImpl withPartitionCount(int partitionCount) {
    this.inner().withPartitionCount(partitionCount);
    return this;
  }
}