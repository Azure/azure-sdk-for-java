package com.azure.json.reflect;

import java.lang.reflect.Field;

public class FieldHelper {
	//Get all fields from a class
	
	public static Field[] extractFields(Class<?> workingClass) {
		//Take in class as parameter and return a list of fields
		Field fieldList[] = workingClass.getDeclaredFields();
		try {
			for (Field field : fieldList) {
				//make them accessible
		        field.setAccessible(true);	
		};
		fieldList = workingClass.getDeclaredFields();
		}
		catch(Exception e ) {
			System.out.print(e);
		}
		
		return fieldList;
		
	}

}
