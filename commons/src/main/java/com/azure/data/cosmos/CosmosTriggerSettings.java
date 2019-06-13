/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Constants;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class CosmosTriggerSettings extends Resource {

    /**
     * Constructor
     */
    public CosmosTriggerSettings(){
        super();
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the trigger settings.
     */
    public CosmosTriggerSettings(String jsonString){
        super(jsonString);
    }

    CosmosTriggerSettings(ResourceResponse<Trigger> response) {
        super(response.getResource().toJson());
    }

    /**
     * Get the body of the trigger.
     *
     * @return the body of the trigger.
     */
    public String body() {
        return super.getString(Constants.Properties.BODY);
    }

    /**
     * Set the body of the trigger.
     *
     * @param body the body of the trigger.
     * @return the CosmosTriggerSettings.
     */
    public CosmosTriggerSettings body(String body) {
        super.set(Constants.Properties.BODY, body);
        return this;
    }

    /**
     * Get the type of the trigger.
     *
     * @return the trigger type.
     */
    public TriggerType triggerType() {
        TriggerType result = TriggerType.PRE;
        try {
            result = TriggerType.valueOf(
                    StringUtils.upperCase(super.getString(Constants.Properties.TRIGGER_TYPE)));
        } catch (IllegalArgumentException e) {
            // ignore the exception and return the default
            this.getLogger().warn("INVALID triggerType value {}.", super.getString(Constants.Properties.TRIGGER_TYPE));
        }
        return result;
    }

    /**
     * Set the type of the resource.
     *
     * @param triggerType the trigger type.
     * @return the CosmosTriggerSettings.
     */
    public CosmosTriggerSettings triggerType(TriggerType triggerType) {
        super.set(Constants.Properties.TRIGGER_TYPE, triggerType.toString());
        return this;
    }

    /**
     * Get the operation type of the trigger.
     *
     * @return the trigger operation.
     */
    public TriggerOperation triggerOperation() {
        TriggerOperation result = TriggerOperation.CREATE;
        try {
            result = TriggerOperation.valueOf(
                    StringUtils.upperCase(super.getString(Constants.Properties.TRIGGER_OPERATION)));
        } catch (IllegalArgumentException e) {
            // ignore the exception and return the default
            this.getLogger().warn("INVALID triggerOperation value {}.", super.getString(Constants.Properties.TRIGGER_OPERATION));
        }
        return result;
    }

    /**
     * Set the operation type of the trigger.
     *
     * @param triggerOperation the trigger operation.
     * @return the CosmosTriggerSettings.
     */
    public CosmosTriggerSettings triggerOperation(TriggerOperation triggerOperation) {
        super.set(Constants.Properties.TRIGGER_OPERATION, triggerOperation.toString());
        return this;
    }

    static List<CosmosTriggerSettings> getFromV2Results(List<Trigger> results) {
        return results.stream().map(trigger -> new CosmosTriggerSettings(trigger.toJson())).collect(Collectors.toList());
    }
}
