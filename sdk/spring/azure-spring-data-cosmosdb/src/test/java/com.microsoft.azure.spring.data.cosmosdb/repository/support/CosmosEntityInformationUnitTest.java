// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository.support;

import java.util.List;
import java.util.Objects;

import com.microsoft.azure.spring.data.cosmosdb.common.TestConstants;
import com.microsoft.azure.spring.data.cosmosdb.core.mapping.Document;
import com.microsoft.azure.spring.data.cosmosdb.core.mapping.PartitionKey;
import com.microsoft.azure.spring.data.cosmosdb.domain.Address;
import com.microsoft.azure.spring.data.cosmosdb.domain.Person;
import com.microsoft.azure.spring.data.cosmosdb.domain.Student;
import org.junit.Test;

import org.springframework.data.annotation.Version;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosEntityInformationUnitTest {
    private static final String ID = "entity_info_test_id";
    private static final String FIRST_NAME = "first name";
    private static final String LAST_NAME = "last name";
    private static final List<String> HOBBIES = TestConstants.HOBBIES;
    private static final List<Address> ADDRESSES = TestConstants.ADDRESSES;

    @Test
    public void testGetId() {
        final Person testPerson = new Person(ID, FIRST_NAME, LAST_NAME, HOBBIES, ADDRESSES);
        final CosmosEntityInformation<Person, String> entityInformation =
                new CosmosEntityInformation<Person, String>(Person.class);

        final String idField = entityInformation.getId(testPerson);

        assertThat(idField).isEqualTo(testPerson.getId());
    }

    @Test
    public void testGetIdType() {
        final CosmosEntityInformation<Person, String> entityInformation =
                new CosmosEntityInformation<Person, String>(Person.class);

        final Class<?> idType = entityInformation.getIdType();
        assertThat(idType.getSimpleName()).isEqualTo(String.class.getSimpleName());
    }

    @Test
    public void testGetContainerName() {
        final CosmosEntityInformation<Person, String> entityInformation =
                new CosmosEntityInformation<Person, String>(Person.class);

        final String containerName = entityInformation.getContainerName();
        assertThat(containerName).isEqualTo(Person.class.getSimpleName());
    }

    @Test
    public void testCustomContainerName() {
        final CosmosEntityInformation<VersionedVolunteer, String> entityInformation =
                new CosmosEntityInformation<VersionedVolunteer, String>(VersionedVolunteer.class);

        final String containerName = entityInformation.getContainerName();
        assertThat(containerName).isEqualTo("testCollection");
    }

    @Test
    public void testGetPartitionKeyName() {
        final CosmosEntityInformation<VolunteerWithPartitionKey, String> entityInformation =
                new CosmosEntityInformation<>(VolunteerWithPartitionKey.class);

        final String partitionKeyName = entityInformation.getPartitionKeyFieldName();
        assertThat(partitionKeyName).isEqualTo("name");
    }

    @Test
    public void testNullPartitionKeyName() {
        final CosmosEntityInformation<Volunteer, String> entityInformation =
                new CosmosEntityInformation<>(Volunteer.class);

        final String partitionKeyName = entityInformation.getPartitionKeyFieldName();
        assertThat(partitionKeyName).isEqualTo(null);
    }

    @Test
    public void testCustomPartitionKeyName() {
        final CosmosEntityInformation<VolunteerWithCustomPartitionKey, String> entityInformation =
                new CosmosEntityInformation<>(VolunteerWithCustomPartitionKey.class);

        final String partitionKeyName = entityInformation.getPartitionKeyFieldName();
        assertThat(partitionKeyName).isEqualTo("vol_name");
    }

    @Test
    public void testVersionedEntity() {
        final CosmosEntityInformation<VersionedVolunteer, String> entityInformation =
                new CosmosEntityInformation<VersionedVolunteer, String>(VersionedVolunteer.class);

        final boolean isVersioned = entityInformation.isVersioned();
        assertThat(isVersioned).isTrue();
    }

    @Test
    public void testEntityShouldNotBeVersionedWithWrongType() {
        final CosmosEntityInformation<WrongVersionType, String> entityInformation =
                new CosmosEntityInformation<WrongVersionType, String>(WrongVersionType.class);

        final boolean isVersioned = entityInformation.isVersioned();
        assertThat(isVersioned).isFalse();
    }

    @Test
    public void testEntityShouldNotBeVersionedWithoutAnnotationOnEtag() {
        final CosmosEntityInformation<VersionOnWrongField, String> entityInformation =
                new CosmosEntityInformation<VersionOnWrongField, String>(VersionOnWrongField.class);

        final boolean isVersioned = entityInformation.isVersioned();
        assertThat(isVersioned).isFalse();
    }

    @Test
    public void testNonVersionedEntity() {
        final CosmosEntityInformation<Student, String> entityInformation =
                new CosmosEntityInformation<Student, String>(Student.class);

        final boolean isVersioned = entityInformation.isVersioned();
        assertThat(isVersioned).isFalse();
    }

    @Document(collection = "testCollection")
    private static class Volunteer {
        String id;
        String name;
    }

    @Document
    private static class VolunteerWithCustomPartitionKey {
        private String id;
        @PartitionKey("vol_name")
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Document
    private static class VolunteerWithPartitionKey {
        private String id;
        @PartitionKey
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Document(collection = "testCollection")
    private static class VersionedVolunteer {
        private String id;
        private String name;
        @Version
        private String _etag;

        public VersionedVolunteer() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String get_etag() {
            return _etag;
        }

        public void set_etag(String _etag) {
            this._etag = _etag;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VersionedVolunteer that = (VersionedVolunteer) o;
            return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(_etag, that._etag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, _etag);
        }

        @Override
        public String toString() {
            return "VersionedVolunteer{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", _etag='" + _etag + '\'' +
                '}';
        }
    }

    @Document
    private static class WrongVersionType {
        private String id;
        private String name;
        private long _etag;

        public WrongVersionType() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long get_etag() {
            return _etag;
        }

        public void set_etag(long _etag) {
            this._etag = _etag;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WrongVersionType that = (WrongVersionType) o;
            return _etag == that._etag &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, _etag);
        }

        @Override
        public String toString() {
            return "WrongVersionType{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", _etag=" + _etag +
                '}';
        }
    }

    @Document
    private static class VersionOnWrongField {
        private String id;
        @Version
        private String name;
        private String _etag;

        public VersionOnWrongField() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String get_etag() {
            return _etag;
        }

        public void set_etag(String _etag) {
            this._etag = _etag;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VersionOnWrongField that = (VersionOnWrongField) o;
            return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(_etag, that._etag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, _etag);
        }

        @Override
        public String toString() {
            return "VersionOnWrongField{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", _etag='" + _etag + '\'' +
                '}';
        }
    }
}
