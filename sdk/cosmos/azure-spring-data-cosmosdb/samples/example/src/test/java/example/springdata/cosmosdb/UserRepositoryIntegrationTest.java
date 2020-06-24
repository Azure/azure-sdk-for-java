// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package example.springdata.cosmosdb;

import com.microsoft.azure.spring.data.cosmosdb.exception.CosmosDBAccessException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {UserRepositoryConfiguration.class})
public class UserRepositoryIntegrationTest {

    private static final String ID = "0123456789";
    private static final String EMAIL = "xxx-xx@xxx.com";
    private static final String NAME = "myName";
    private static final String POSTAL_CODE = "0123456789";
    private static final String STREET = "zixing road";
    private static final String CITY = "shanghai";
    private static final String ROLE_CREATOR = "creator";
    private static final String ROLE_CONTRIBUTOR = "contributor";
    private static final int COST_CREATOR = 234;
    private static final int COST_CONTRIBUTOR = 666;
    private static final Long COUNT = 123L;

    @Autowired
    private UserRepository repository;

    @Autowired
    private ApplicationContext applicationContext;

    @After
    public void cleanup() {
        //  Switch back to primary key to reset the invalid key
        //  Switch to invalid key
        final UserRepositoryConfiguration bean =
            applicationContext.getBean(UserRepositoryConfiguration.class);
        bean.switchToPrimaryKey();
        this.repository.deleteAll().block();
    }

    @Test
    public void testUserRepository() {
        final Address address = new Address(POSTAL_CODE, STREET, CITY);
        final Role creator = new Role(ROLE_CREATOR, COST_CREATOR);
        final Role contributor = new Role(ROLE_CONTRIBUTOR, COST_CONTRIBUTOR);
        final User user = new User(ID, EMAIL, NAME, COUNT, address, Arrays.asList(creator, contributor));

        this.repository.save(user).block();

        // Test for findById
        User result = this.repository.findById(ID).block();
        Assert.notNull(result, "should be exist in database");
        Assert.isTrue(result.getId().equals(ID), "should be the same id");

        // Test for findByName
		List<User> resultList = this.repository.findByName(user.getName()).collectList().block();
		Assert.isTrue(resultList.size() == 1, "should be only one user here");
		Assert.isTrue(resultList.get(0).getName().equals(user.getName()), "should be same Name");
		Assert.notNull(result.getRoleList(), "roleList should not be null");
        Assert.isTrue(result.getRoleList().size() == user.getRoleList().size(), "must be the same list size");

        for (int i = 0; i < user.getRoleList().size(); i++) {
            final Role role = result.getRoleList().get(i);
            final Role roleReference = user.getRoleList().get(i);

            Assert.isTrue(role.getName().equals(roleReference.getName()), "should be the same role name");
            Assert.isTrue(role.getCost() == roleReference.getCost(), "should be the same role cost");
        }

        // Test for findByEmailAndAddress
        resultList = this.repository.findByEmailAndAddress(user.getEmail(), user.getAddress()).collectList().block();
        Assert.isTrue(resultList.size() == 1, "should be only one user here");

        result = resultList.get(0);
        Assert.isTrue(result.getEmail().equals(user.getEmail()), "should be same Email");
        Assert.isTrue(result.getAddress().getPostalCode().equals(user.getAddress().getPostalCode()),
                "should be same postalCode");
        Assert.isTrue(result.getAddress().getCity().equals(user.getAddress().getCity()), "should be same City");
        Assert.isTrue(result.getAddress().getStreet().equals(user.getAddress().getStreet()), "should be same street");

        resultList = this.repository.findByEmailOrName(user.getEmail(), user.getName()).collectList().block();
        result = resultList.get(0);
        Assert.isTrue(result.getId().equals(user.getId()), "should be the same Id");

        resultList = this.repository.findByCount(COUNT,
            Sort.by(new Sort.Order(Sort.Direction.ASC, "count"))).collectList().block();
        result = resultList.get(0);
        Assert.isTrue(result.getId().equals(user.getId()), "should be the same Id");

        resultList = this.repository.findByNameIn(Arrays.asList(user.getName(), "fake-name")).collectList().block();
        result = resultList.get(0);
        Assert.isTrue(result.getId().equals(user.getId()), "should be the same Id");

        // Test for findByAddress
        final Flux<User> findByAddressFlux = this.repository.findByAddress(address);
        resultList = findByAddressFlux.collectList().block();
        result = resultList.get(0);
        Assert.isTrue(result.getId().equals(user.getId()), "should be the same Id");
    }

    @Test
    public void testSecondaryKeyRotation() {
        //  Switch to secondary key
        final UserRepositoryConfiguration bean =
            applicationContext.getBean(UserRepositoryConfiguration.class);
        bean.switchToSecondaryKey();


        final Address address = new Address(POSTAL_CODE, STREET, CITY);
        final Role creator = new Role(ROLE_CREATOR, COST_CREATOR);
        final Role contributor = new Role(ROLE_CONTRIBUTOR, COST_CONTRIBUTOR);
        final User user = new User(ID, EMAIL, NAME, COUNT, address, Arrays.asList(creator, contributor));

        this.repository.save(user).block();

        // Test for findById
        final User result = this.repository.findById(ID).block();
        Assert.notNull(result, "should be exist in database");
        Assert.isTrue(result.getId().equals(ID), "should be the same id");

        // Test for findByName
        final List<User> resultList = this.repository.findByName(user.getName()).collectList().block();
        Assert.isTrue(resultList.size() == 1, "should be only one user here");
        Assert.isTrue(resultList.get(0).getName().equals(user.getName()), "should be same Name");
        Assert.notNull(result.getRoleList(), "roleList should not be null");
        Assert.isTrue(result.getRoleList().size() == user.getRoleList().size(), "must be the same list size");
    }

    @Test(expected = CosmosDBAccessException.class)
    public void testInvalidSecondaryKey() {
        final Address address = new Address(POSTAL_CODE, STREET, CITY);
        final Role creator = new Role(ROLE_CREATOR, COST_CREATOR);
        final Role contributor = new Role(ROLE_CONTRIBUTOR, COST_CONTRIBUTOR);
        final User user = new User(ID, EMAIL, NAME, COUNT, address, Arrays.asList(creator, contributor));

        this.repository.save(user).block();

        // Test for findById
        final User result = this.repository.findById(ID).block();
        Assert.notNull(result, "should be exist in database");
        Assert.isTrue(result.getId().equals(ID), "should be the same id");

        //  Switch to invalid key
        final UserRepositoryConfiguration bean =
            applicationContext.getBean(UserRepositoryConfiguration.class);
        bean.switchKey("Invalid key");

        // Test for findByName
        this.repository.findById(user.getId()).block();
    }

}

