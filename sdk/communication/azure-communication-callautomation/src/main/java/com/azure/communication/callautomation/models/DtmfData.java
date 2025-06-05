// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.DtmfDataContructorProxy;
import com.azure.communication.callautomation.implementation.converters.DtmfDataConverter;

/** The dtmf data model. */
public final class DtmfData extends StreamingData {
    /*
     * The dtmf data.
     */
    private final String data;

    static {
        DtmfDataContructorProxy.setAccessor(new DtmfDataContructorProxy.DtmfDataContructorProxyAccessor() {
            @Override
            public DtmfData create(DtmfDataConverter internalData) {
                return new DtmfData(internalData);
            }

            @Override
            public DtmfData create(String data) {
                return new DtmfData(data);
            }
        });
    }

    /**
     * Package-private constructor of the class, used internally.
     *
     * @param internalData The DtmfDataconvertor
     */
    DtmfData(DtmfDataConverter internalData) {
        super(StreamingDataKind.DTMF_DATA);
        this.data = internalData.getData();
    }

    /**
     * The constructor
     *
     * @param data The dtmf data.
     */
    DtmfData(String data) {
        super(StreamingDataKind.DTMF_DATA);
        this.data = data;
    }

    /**
     * Get the data property.
     *
     * @return the data value.
     */
    public String getData() {
        return data;
    }
}
