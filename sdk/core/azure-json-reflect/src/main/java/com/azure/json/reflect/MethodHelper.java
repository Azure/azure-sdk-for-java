package com.azure.json.reflect;

import java.lang.reflect.Method;

public class MethodHelper {
	//get all methods from a class
	public static Method[] extractMethods(Class<?> workingClass) {
		Method[] methodList = workingClass.getDeclaredMethods();
		try {
			for(Method method: methodList) {
				//make them accessible
				method.setAccessible(true);
			}
			
		}
		catch(Exception e){
			System.out.print(e);
		}
		return methodList;
	}

}
