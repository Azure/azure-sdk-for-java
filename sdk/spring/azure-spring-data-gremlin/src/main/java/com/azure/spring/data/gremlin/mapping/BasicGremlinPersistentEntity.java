// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.mapping;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.BeanFactoryAccessor;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class BasicGremlinPersistentEntity<T> extends BasicPersistentEntity<T, GremlinPersistentProperty>
        implements GremlinPersistentEntity<T>, ApplicationContextAware {

    private final StandardEvaluationContext context;

    public BasicGremlinPersistentEntity(TypeInformation<T> information) {
        super(information);

        this.context = new StandardEvaluationContext();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context.addPropertyAccessor(new BeanFactoryAccessor());
        this.context.setBeanResolver(new BeanFactoryResolver(applicationContext));
        this.context.setRootObject(applicationContext);
    }
}
