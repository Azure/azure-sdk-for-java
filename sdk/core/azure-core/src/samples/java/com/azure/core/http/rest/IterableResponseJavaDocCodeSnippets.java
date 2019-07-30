// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import reactor.core.publisher.Flux;

import java.util.Iterator;


/**
 * Code snippets for {@link IterableResponse}
 */
public class IterableResponseJavaDocCodeSnippets {

    public void streamSnippet(){
        IterableResponse<Integer> myIterableResponse = new IterableResponse<>(Flux.range(1, 10));
        // BEGIN: com.azure.core.http.rest.stream
        // Iterate over stream
        myIterableResponse.stream().forEach(number -> {
            System.out.println(" The number :" +number);
        });
        // END: com.azure.core.http.rest.stream
    }


    public void iteratorSnippet(){
        IterableResponse<Integer> myIterableResponse = new IterableResponse<>(Flux.range(1, 10));
        // BEGIN: com.azure.core.http.rest.iterator
        // Iterate over stream
        Iterator<Integer> ite = myIterableResponse.iterator();
        while(ite.hasNext()) {
            System.out.printf("Value is %d", ite.next());
        }
        // END: com.azure.core.http.rest.iterator
    }

    public void iteratorCountSnippet(){
        IterableResponse<Integer> myIterableResponse = new IterableResponse<>(Flux.range(1, 10));
        // BEGIN: com.azure.core.http.rest.stream.count
        // Iterate over stream and count
        System.out.println(" Total number of values:" + myIterableResponse.stream().count());
        // END: com.azure.core.http.rest.stream.count
    }
}
