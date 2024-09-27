// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.aot.graalvm.samples.cosmos;

/**
 * Helper class to get family details.
 */
public final class Families {

    /**
     * Returns the family details of Andersen family.
     * @return the family details of Andersen family.
     */
    public static Family getAndersenFamilyItem() {
        Family andersenFamily = new Family();
        andersenFamily.setId("Andersen-" + System.currentTimeMillis());
        andersenFamily.setLastName("Andersen");

        Parent parent1 = new Parent();
        parent1.setFirstName("Thomas");

        Parent parent2 = new Parent();
        parent2.setFirstName("Mary Kay");

        andersenFamily.setParents(new Parent[] { parent1, parent2 });

        Child child1 = new Child();
        child1.setFirstName("Henriette Thaulow");
        child1.setGender("female");
        child1.setGrade(5);

        Pet pet1 = new Pet();
        pet1.setGivenName("Fluffy");

        child1.setPets(new Pet[] { pet1 });

        andersenFamily.setDistrict("WA5");
        Address address = new Address();
        address.setCity("Seattle");
        address.setCounty("King");
        address.setState("WA");

        andersenFamily.setAddress(address);
        andersenFamily.setRegistered(true);
        andersenFamily.setChildren(new Child[] { child1 });

        return andersenFamily;
    }

    /**
     * Returns the family details of Wakefield family.
     * @return the family details of Wakefield family.
     */
    public static Family getWakefieldFamilyItem() {
        Family wakefieldFamily = new Family();
        wakefieldFamily.setId("Wakefield-" + System.currentTimeMillis());
        wakefieldFamily.setLastName("Wakefield");

        Parent parent1 = new Parent();
        parent1.setFamilyName("Wakefield");
        parent1.setFirstName("Robin");

        Parent parent2 = new Parent();
        parent2.setFamilyName("Miller");
        parent2.setFirstName("Ben");

        wakefieldFamily.setParents(new Parent[] { parent1, parent2 });

        Child child1 = new Child();
        child1.setFirstName("Jesse");
        child1.setFamilyName("Merriam");
        child1.setGrade(8);

        Pet pet1 = new Pet();
        pet1.setGivenName("Goofy");

        Pet pet2 = new Pet();
        pet2.setGivenName("Shadow");

        child1.setPets(new Pet[] { pet1, pet2 });

        Child child2 = new Child();
        child2.setFirstName("Lisa");
        child2.setFamilyName("Miller");
        child2.setGrade(1);
        child2.setGender("female");

        wakefieldFamily.setChildren(new Child[] { child1, child2 });

        Address address = new Address();
        address.setCity("NY");
        address.setCounty("Manhattan");
        address.setState("NY");

        wakefieldFamily.setAddress(address);
        wakefieldFamily.setDistrict("NY23");
        wakefieldFamily.setRegistered(true);
        wakefieldFamily.setChildren(new Child[] { child1, child2 });
        return wakefieldFamily;
    }

    /**
     * Returns the family details of Johnson family.
     * @return the family details of Johnson family.
     */
    public static Family getJohnsonFamilyItem() {
        Family johnsonFamily = new Family();
        johnsonFamily.setId("Johnson-" + System.currentTimeMillis());
        johnsonFamily.setLastName("Johnson");

        Parent parent1 = new Parent();
        parent1.setFirstName("John");

        Parent parent2 = new Parent();
        parent2.setFirstName("Lili");

        johnsonFamily.setParents(new Parent[] { parent1, parent2 });

        return johnsonFamily;
    }

    /**
     * Returns  the family details of Smith family.
     * @return the family details of Smith family.
     */
    public static Family getSmithFamilyItem() {
        Family smithFamily = new Family();
        smithFamily.setId("Smith-" + System.currentTimeMillis());
        smithFamily.setLastName("Smith");

        Parent parent1 = new Parent();
        parent1.setFirstName("John");

        Parent parent2 = new Parent();
        parent2.setFirstName("Cynthia");
        smithFamily.setParents(new Parent[] { parent1, parent2 });

        return smithFamily;
    }

    private Families() {
    }
}
