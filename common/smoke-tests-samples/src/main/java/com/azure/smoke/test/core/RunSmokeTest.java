// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.smoke.test.core;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * This file run smoke test.
 */
public class RunSmokeTest {
    //private static String[] packagesToScan = {"com.azure.messaging.servicebus"};


    public static void main(String[] args) {

       // Set<String> files=new HashSet<>();
       // listOfPackage("c:/ht1/master/azure-sdk-for-java/common/smoke-tests-samples/src/main/java",files);

        try {
            Class<?> aClass = Class.forName("com.azure.messaging.servicebus.PeekMessageAsyncSample");

            try {
                Method method = aClass.getMethod("main", String[].class);
                String param =  null;
                // Now call the main method in each sample class.
                if (method != null) {
                    Object result = method.invoke(null, param);

                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }catch (ClassNotFoundException e ) {
            e.printStackTrace();
        }
    }

 public static void listOfPackage(String directoryName, Set<String> pack) {
        File directory = new File(directoryName);

        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                String path=file.getPath();
                String packName=path.substring(path.indexOf("\\java")+6, path.lastIndexOf('\\'));
                String className=path.substring(path.lastIndexOf('\\')+1, path.lastIndexOf('.'));
                String fullClassName = packName.replace('\\', '.') + "." + className;
                pack.add(packName.replace('\\', '.'));
                if (fullClassName.indexOf("PeekMessageAsyncSample") < 0) {
                    continue;
                }
                try {
                    Class<?> aClass = Class.forName(fullClassName);
                    try {
                        Method method = aClass.getMethod("main", String[].class);
                        String param =  null;
                        // Now call the main method in each sample class.
                        if (method != null) {
                            Object result = method.invoke(null, param);

                        }
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }catch (ClassNotFoundException e ) {
                    e.printStackTrace();
                }
            } else if (file.isDirectory()) {

                listOfPackage(file.getAbsolutePath(), pack);
            }
        }
    }

    /*public static void runTest() {

        Reflections reflections = new Reflections(new ConfigurationBuilder()
            .setUrls(ClasspathHelper.forPackage(packagesToScan[0]))
            .setScanners(new SubTypesScanner(),
                new TypeAnnotationsScanner().filterResultsBy(s -> {
                    if (s.equalsIgnoreCase(Tag.class.getName())) {
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
                if (method != null) {
                    method.invoke(null, param);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });

    }
    */
}
