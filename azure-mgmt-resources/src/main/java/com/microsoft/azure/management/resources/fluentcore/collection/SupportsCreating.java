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

import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

/**
 * Providing access to creating Azure top level resources
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 * @param T the initial blank definition interface
 */
public interface SupportsCreating<T> {
	/**
	 * Begins a definition for a new resource.
	 * <p>
	 * This is the beginning of the builder pattern used to create top level resources 
	 * in Azure. The final method completing the definition and starting the actual resource creation 
	 * process in Azure is {@link Creatable#create()}.  
	 * <p>
	 * Note that the {@link Creatable#create()} method is 
	 * only available at the stage of the resource definition that has the minimum set of input 
	 * parameters specified. If you do not see {@link Creatable#create()} among the available methods, it 
	 * means you have not yet specified all the required input settings. Input settings generally begin 
	 * with the word "with", for example: <code>.withNewResourceGroup()</code> and return the next stage 
	 * of the resource definition, as an interface in the "fluent interface" style.
	 * @param name the name of the new resource
	 * @return the first stage of the new resource definition
	 */
	T define(String name);
}
