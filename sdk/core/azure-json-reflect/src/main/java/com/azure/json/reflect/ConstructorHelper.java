package com.azure.json.reflect;

import java.lang.reflect.Constructor;

public class ConstructorHelper {
	//get all constructors from a class
	public static Constructor[] extractConstructors(Class<?> workingClass){
		Constructor[] constructorList = workingClass.getDeclaredConstructors();
		try {
			for(Constructor<?> constructor: constructorList) {
				//make them accessible
				constructor.setAccessible(true);
			}
		}
		catch(Exception e) {
			System.out.print(e);
		}
		return constructorList;
	}

}
