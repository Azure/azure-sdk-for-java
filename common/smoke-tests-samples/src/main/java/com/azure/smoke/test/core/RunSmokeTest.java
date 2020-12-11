package com.azure.smoke.test.core;

import org.reflections.Reflections;
import org.junit.jupiter.api.Tag;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 */
public class RunSmokeTest {
    private static String[] packagesToScan = {"com.azure.messaging.servicebus"};

    public static void main(String[] args) throws Exception{
        runTest();
    }

    public static void runTest() {
        TypeAnnotationsScanner scanner =  new  TypeAnnotationsScanner();
        scanner.filterResultsBy(s -> {
            if (s.equalsIgnoreCase("org.junit.jupiter.api.Tag")) {
                return true;
            } else  return false;
        });

        Reflections reflections = new Reflections(new ConfigurationBuilder()
            .setUrls(ClasspathHelper.forPackage(packagesToScan[0]))
            .setScanners(new SubTypesScanner(),
                new TypeAnnotationsScanner().filterResultsBy(s -> {
                    if (s.equalsIgnoreCase("org.junit.jupiter.api.Tag")) {
                        return true;
                    } else  {
                        return false;
                    }
                })));

        reflections.getTypesAnnotatedWith(Tag.class).stream().filter(aClass -> {
            if ( aClass.getAnnotation(Tag.class).value().equalsIgnoreCase("sample") ) {
                return true;
            } else {
                return false;
            }
        })
        .forEach(aClass -> {
            try {
                Method method = aClass.getMethod("main", String[].class);
                String param =  null;
                // Now call the main method in each sample class.
                method.invoke(null, param);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });

    }
}
