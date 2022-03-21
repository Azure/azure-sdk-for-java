package com.test.models;

public class ClassB {

    public void methodB() {
        // calls through to ClassA.methodA(), which is annotated
        new ClassA().methodA();
    }
}
