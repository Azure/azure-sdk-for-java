/**
* Copyright (c) Microsoft Corporation
* 
* All rights reserved. 
* 
* MIT License
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
* (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
* publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
* subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
* ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
* THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.microsoft.azure.management.resources.fluentcore.collection;


/**
 * Provides access to updating a specific Azure resource, based on its resource ID
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 */
public interface SupportsUpdating<T> {
	
	/**
	 * Begins an update definition for an existing resource.
	 * <p>
	 * This is the beginning of the builder pattern used to modify top level resources 
	 * in Azure. The final method completing the update definition and starting the actual resource update 
	 * process in Azure is {@link Appliable#apply()}.
	 * <p>
	 * Note that the {@link Appliable#apply()} method is available at any stage of the update definition 
	 * because all the updatable settings are generally optional. 
	 * <p>Settings typically begin 
	 * with the word "with", for example: <code>.withRegion()</code> and return the update definition itself, to enable chaining 
	 * in the fluent interface style.
	 * @param id the resource id of the resource to update. Remember to call {@see Appliable#apply()} for the changes
	 * to go into effect on Azure.
	 * @return the update definition itself
	 */
	T update(String id);
}
