/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package sample.jms.topic;

import java.io.Serializable;

public class User implements Serializable {

    private static final long serialVersionUID = -295422703255886286L;
    private String name;

    User(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
