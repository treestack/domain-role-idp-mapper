package de.treestack.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DomainRoleIdpMapperMetadataTest {

    static DomainRoleIdpMapper classUnderTest = new DomainRoleIdpMapper();

    @Test
    void returnsId() {
        assertEquals(DomainRoleIdpMapper.PROVIDER_ID, classUnderTest.getId());
    }

    @Test
    void returnsDisplayType() {
        assertFalse(classUnderTest.getDisplayType().isEmpty());
    }

    @Test
    void returnsDisplayCategory() {
        assertFalse(classUnderTest.getDisplayCategory().isEmpty());
    }

    @Test
    void hasHelpText() {
        assertFalse(classUnderTest.getHelpText().isEmpty());
    }

    @Test
    void hasConfigProperties() {
        assertFalse(classUnderTest.getConfigProperties().isEmpty());
    }

    @Test
    void returnsCompatibleProviders() {
        assertEquals("*", classUnderTest.getCompatibleProviders()[0]);
    }

}
