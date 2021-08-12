// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.mapping.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.core.GenericTypeResolver;

/*
 * Ported to cosmos from spring-data-mongo
 * https://github.com/spring-projects/spring-data-mongodb/blob/main/spring-data-mongodb/src/main/java/org/springframework/data/mongodb/core/mapping/event/AbstractMongoEventListener.java
 */
public abstract class AbstractCosmosEventListener<E> implements ApplicationListener<CosmosMappingEvent<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCosmosEventListener.class);
    private final Class<?> domainClass;

    /**
     * Creates a new {@link AbstractCosmosEventListener}.
     */
    public AbstractCosmosEventListener() {
        Class<?> typeArgument = GenericTypeResolver.resolveTypeArgument(this.getClass(), AbstractCosmosEventListener.class);
        this.domainClass = typeArgument == null ? Object.class : typeArgument;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void onApplicationEvent(CosmosMappingEvent<?> event) {
        if (event instanceof AfterLoadEvent) {
            AfterLoadEvent<?> afterLoadEvent = (AfterLoadEvent<?>) event;

            if (domainClass.isAssignableFrom(afterLoadEvent.getType())) {
                onAfterLoad((AfterLoadEvent<E>) event);
            }
        }
    }

    /**
     * Captures {@link AfterLoadEvent}.
     *
     * @param event will never be {@literal null}.
     */
    public void onAfterLoad(AfterLoadEvent<E> event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("onAfterLoad({})", event.getDocument());
        }
    }

}
