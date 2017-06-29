/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.search.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupPagedList;
import com.microsoft.azure.management.search.AdminKeys;
import com.microsoft.azure.management.search.AdminKeyKind;
import com.microsoft.azure.management.search.CheckNameAvailabilityResult;
import com.microsoft.azure.management.search.QueryKey;
import com.microsoft.azure.management.search.SearchService;
import com.microsoft.azure.management.search.SearchServices;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation for SearchServices.
 */
@LangDefinition
class SearchServicesImpl
    extends GroupableResourcesImpl<
            SearchService,
            SearchServiceImpl,
            SearchServiceInner,
            ServicesInner,
            SearchServiceManager>
    implements SearchServices {

  SearchServicesImpl(final SearchServiceManager searchServiceManager) {
    super(searchServiceManager.inner().services(), searchServiceManager);
  }

  @Override
  public PagedList<SearchService> list() {
    final SearchServicesImpl self = this;
    return new GroupPagedList<SearchService>(this.manager().resourceManager().resourceGroups().list()) {
      @Override
      public List<SearchService> listNextGroup(String resourceGroupName) {
        return wrapList(self.inner().listByResourceGroup(resourceGroupName));
      }
    };
  }

  @Override
  public Observable<SearchService> listAsync() {
    return this.manager().resourceManager().resourceGroups().listAsync()
        .flatMap(new Func1<ResourceGroup, Observable<SearchService>>() {
          @Override
          public Observable<SearchService> call(ResourceGroup resourceGroup) {
            return wrapListAsync(inner().listByResourceGroupAsync(resourceGroup.name()));
          }
        });
  }

  @Override
  public PagedList<SearchService> listByResourceGroup(String groupName) {
    return wrapList(this.inner().listByResourceGroup(groupName));
  }

  @Override
  public Observable<SearchService> listByResourceGroupAsync(String resourceGroupName) {
    return wrapListAsync(this.inner().listByResourceGroupAsync(resourceGroupName));
  }

  @Override
  protected Observable<SearchServiceInner> getInnerAsync(String resourceGroupName, String name) {
    return this.inner().getByResourceGroupAsync(resourceGroupName, name);
  }
  @Override
  public SearchServiceImpl define(String name) {
    return wrapModel(name);
  }

  @Override
  protected Completable deleteInnerAsync(String groupName, String name) {
    return this.inner().deleteAsync(groupName, name).toCompletable();
  }

  // Fluent model create helpers

  @Override
  protected SearchServiceImpl wrapModel(String name) {
    SearchServiceInner inner = new SearchServiceInner();

    return new SearchServiceImpl(name, inner, super.manager());
  }

  @Override
  protected SearchServiceImpl wrapModel(SearchServiceInner inner) {
    if (inner == null) {
      return null;
    }
    return new SearchServiceImpl(inner.name(), inner, this.manager());
  }

  @Override
  public CheckNameAvailabilityResult checkNameAvailability(String name) {
    return this.checkNameAvailabilityAsync(name).toBlocking().last();
  }

  @Override
  public Observable<CheckNameAvailabilityResult> checkNameAvailabilityAsync(String name) {
    return this.inner().checkNameAvailabilityAsync(name).map(new Func1<CheckNameAvailabilityOutputInner, CheckNameAvailabilityResult>() {
      @Override
      public CheckNameAvailabilityResult call(CheckNameAvailabilityOutputInner checkNameAvailabilityOutputInner) {
        return new CheckNameAvailabilityResultImpl(checkNameAvailabilityOutputInner);
      }
    });
  }

  @Override
  public ServiceFuture<CheckNameAvailabilityResult> checkNameAvailabilityAsync(String name, ServiceCallback<CheckNameAvailabilityResult> callback) {
    return ServiceFuture.fromBody(this.checkNameAvailabilityAsync(name), callback);
  }

  @Override
  public AdminKeys getAdminKeys(String resourceGroupName, String searchServiceName) {
    return new AdminKeysImpl(this.manager().inner().adminKeys().get(resourceGroupName, searchServiceName));
  }

  @Override
  public Observable<AdminKeys> getAdminKeysAsync(String resourceGroupName, String searchServiceName) {
    return this.manager().inner().adminKeys().getAsync(resourceGroupName, searchServiceName)
        .map(new Func1<AdminKeyResultInner, AdminKeys>() {
          @Override
          public AdminKeys call(AdminKeyResultInner adminKeyResultInner) {
            return new AdminKeysImpl(adminKeyResultInner);
          }
        });
  }

  @Override
  public List<QueryKey> listQueryKeys(String resourceGroupName, String searchServiceName) {
    List<QueryKey> queryKeys = new ArrayList<>();

    List<QueryKeyInner> queryKeyInners = this.manager().inner().queryKeys().listBySearchService(resourceGroupName, searchServiceName);
    if (queryKeyInners != null) {
      for (QueryKeyInner queryKeyInner : queryKeyInners) {
        queryKeys.add(new QueryKeyImpl(queryKeyInner));
      }
    }

    return Collections.unmodifiableList(queryKeys);
  }

  @Override
  public Observable<QueryKey> listQueryKeysAsync(String resourceGroupName, String searchServiceName) {
    Observable<List<QueryKeyInner>> queryKeysList = this.manager().inner().queryKeys()
        .listBySearchServiceAsync(resourceGroupName, searchServiceName);

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

  @Override
  public AdminKeys regenerateAdminKeys(String resourceGroupName, String searchServiceName, AdminKeyKind keyKind) {
    return new AdminKeysImpl(this.manager().inner().adminKeys().regenerate(resourceGroupName, searchServiceName, keyKind));
  }

  @Override
  public Observable<AdminKeys> regenerateAdminKeysAsync(String resourceGroupName, String searchServiceName, AdminKeyKind keyKind) {
    return this.manager().inner().adminKeys().regenerateAsync(resourceGroupName, searchServiceName, keyKind)
        .map(new Func1<AdminKeyResultInner, AdminKeys>() {
          @Override
          public AdminKeys call(AdminKeyResultInner adminKeyResultInner) {
            return new AdminKeysImpl(adminKeyResultInner);
          }
        });
  }

  @Override
  public QueryKey createQueryKey(String resourceGroupName, String searchServiceName, String name) {
    return new QueryKeyImpl(this.manager().inner().queryKeys().create(resourceGroupName, searchServiceName, name));
  }

  @Override
  public Observable<QueryKey> createQueryKeyAsync(String resourceGroupName, String searchServiceName, String name) {
    return this.manager().inner().queryKeys().createAsync(resourceGroupName, searchServiceName, name)
        .map(new Func1<QueryKeyInner, QueryKey>() {
          @Override
          public QueryKey call(QueryKeyInner queryKeyInner) {
            return new QueryKeyImpl(queryKeyInner);
          }
        });
  }

  @Override
  public void deleteQueryKey(String resourceGroupName, String searchServiceName, String key) {
    this.manager().inner().queryKeys().delete(resourceGroupName, searchServiceName, key);
  }

  @Override
  public Completable deleteQueryKeyAsync(String resourceGroupName, String searchServiceName, String key) {
    return this.manager().inner().queryKeys().deleteAsync(resourceGroupName, searchServiceName, key).toCompletable();
  }
}