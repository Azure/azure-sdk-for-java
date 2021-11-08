package org.slf4j.impl;

import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;

import java.util.HashMap;
import java.util.Map;

public class StaticMDCBinder {
    public static final StaticMDCBinder SINGLETON = new StaticMDCBinder();

    private StaticMDCBinder() {
    }

    public static final StaticMDCBinder getSingleton() {
        return SINGLETON;
    }

    public MDCAdapter getMDCA() {
        return new MDCAdapter() {
            private Map<String, String> context = null;
            @Override
            public void put(String key, String val) {
                if (this.context != null) {
                    this.context.put(key, val);
                }
            }

            @Override
            public String get(String key) {
                if (this.context != null) {
                    return this.context.get(key);
                }
                return null;
            }

            @Override
            public void remove(String key) {
                if (this.context != null) {
                    this.context.remove(key);
                }
            }

            @Override
            public void clear() {
                if (this.context != null) {
                    this.context.clear();
                }
            }

            @Override
            public Map<String, String> getCopyOfContextMap() {
                if (this.context != null) {
                    return new HashMap<>(this.context);
                }

                return null;
            }

            @Override
            public void setContextMap(Map<String, String> contextMap) {
                this.context = contextMap;
            }
        };
    }

    public String getMDCAdapterClassStr() {
        return NOPMDCAdapter.class.getName();
    }
}

//public class MdcAda
