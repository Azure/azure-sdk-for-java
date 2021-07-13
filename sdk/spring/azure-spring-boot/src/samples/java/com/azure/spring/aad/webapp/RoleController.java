// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RoleController {
    @GetMapping("group1")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_group1')")
    public String group1() {
        return "group1 message";
    }

    @GetMapping("group2")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_group2')")
    public String group2() {
        return "group2 message";
    }

    @GetMapping("group1Id")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_<group1-id>')")
    public String group1Id() {
        return "group1Id message";
    }

    @GetMapping("group2Id")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_<group2-id>')")
    public String group2Id() {
        return "group2Id message";
    }
}
