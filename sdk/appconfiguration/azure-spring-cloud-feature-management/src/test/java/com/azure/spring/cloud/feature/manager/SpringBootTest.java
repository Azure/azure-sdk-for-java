package com.azure.spring.cloud.feature.manager;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.test.context.BootstrapWith;

@Target(value=ElementType.TYPE)
@Retention(value=RetentionPolicy.RUNTIME)
@Documented
@Inherited
@BootstrapWith(value=SpringBootTestContextBootstrapper.class)
@ExtendWith(value=org.springframework.test.context.junit.jupiter.SpringExtension.class)
public @interface SpringBootTest 
{
    
}