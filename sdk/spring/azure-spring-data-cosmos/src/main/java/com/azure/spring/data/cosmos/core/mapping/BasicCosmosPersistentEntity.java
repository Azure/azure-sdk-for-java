// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core.mapping;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.BeanFactoryAccessor;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Simple value object to capture information of {@link CosmosPersistentProperty}s.
 */
public class BasicCosmosPersistentEntity<T> extends BasicPersistentEntity<T, CosmosPersistentProperty>
        implements CosmosPersistentEntity<T>, ApplicationContextAware {

    private final StandardEvaluationContext context;

    /**
     * Creates a new {@link BasicCosmosPersistentEntity} from the given {@link TypeInformation}.
     *
     * @param typeInformation must not be {@literal null}.
     */
    public BasicCosmosPersistentEntity(TypeInformation<T> typeInformation) {
        super(typeInformation);
        this.context = new StandardEvaluationContext();
    }

    /**
     * To set application context
     * @param applicationContext must not be {@literal null}.
     * @throws BeansException the bean exception
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context.addPropertyAccessor(new BeanFactoryAccessor());
        context.setBeanResolver(new BeanFactoryResolver(applicationContext));
        context.setRootObject(applicationContext);
    }

    /**
     * To get collection of entity
     * @return String
     */
    public String getCollection() {
        return "";
    }

    @Override
    public String getContainer() {
        return "";
    }

    @Override
    public String getLanguage() {
        return "";
    }
}
