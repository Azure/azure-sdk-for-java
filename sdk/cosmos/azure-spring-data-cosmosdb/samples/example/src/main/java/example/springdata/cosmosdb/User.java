// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package example.springdata.cosmosdb;

import com.microsoft.azure.spring.data.cosmosdb.core.mapping.Document;
import com.microsoft.azure.spring.data.cosmosdb.core.mapping.PartitionKey;
import org.springframework.data.annotation.Id;

import java.util.List;

@Document(collection = "mycollection")
public class User {

    @Id
    private String id;

    private String email;

    @PartitionKey
    private String name;

    private Long count;

    private Address address;

    private List<Role> roleList;

    @Override
    public String toString() {
        return String.format("%s: %s %s %s", this.id, this.email, this.name, this.address.toString());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<Role> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<Role> roleList) {
        this.roleList = roleList;
    }

    public User(String id, String email, String name, Long count, Address address, List<Role> roleList) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.count = count;
        this.address = address;
        this.roleList = roleList;
    }

    public User() {
    }
}

