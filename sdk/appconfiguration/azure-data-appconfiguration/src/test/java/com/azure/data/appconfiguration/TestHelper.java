package com.azure.data.appconfiguration;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

public class TestHelper {
    public static Stream<Arguments> getCombinations(Object[]... objectsArray) {
//        List<Arguments> arguments = new ArrayList<>();
//        for (Object object: objectsArray) {
//            arguments.add(Arguments.of(new NettyAsyncHttpClientBuilder().wiretap(true).build(), object));
//            arguments.add(Arguments.of(new OkHttpAsyncHttpClientBuilder().build(), object));
//        }
//        return Stream.of(arguments.toArray(Arguments[]::new));

        Set<List<>> combinations
        return Stream.of(a)
    }

    Set<List<T>> combinations = new HashSet<List<T>>();
    Set<List<T>> newCombinations;

    int index = 0;

    // extract each of the integers in the first list
    // and add each to ints as a new list
    for(T i: lists.get(0)) {
        List<T> newList = new ArrayList<T>();
        newList.add(i);
        combinations.add(newList);
    }
    index++;
    while(index < lists.size()) {
        List<T> nextList = lists.get(index);
        newCombinations = new HashSet<List<T>>();
        for(List<T> first: combinations) {
            for(T second: nextList) {
                List<T> newList = new ArrayList<T>();
                newList.addAll(first);
                newList.add(second);
                newCombinations.add(newList);
            }
        }
        combinations = newCombinations;

        index++;
    }

    return combinations;
}
