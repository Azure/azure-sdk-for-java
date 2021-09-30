// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.trace.sleuth;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class AzureApplicationContext implements BeanFactoryAware {

    private static BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        AzureApplicationContext.beanFactory = beanFactory;
    }

    public static <T> T getBean(String beanName, Class<T> beanClass) throws BeansException{
        return beanFactory.getBean(beanName, beanClass);
    }

    public static <T> T getBean(Class<T> beanClass) throws BeansException{
        return beanFactory.getBean(beanClass);
    }
}
