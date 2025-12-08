package de.treestack.auth;

import org.junit.jupiter.api.Test;

public class DomainRoleIdpMapperMetadataTest {

    static DomainRoleIdpMapper classUnderTest = new DomainRoleIdpMapper();

    @Test
    void returnsId() {
        assert classUnderTest.getId().equals(DomainRoleIdpMapper.PROVIDER_ID);
    }

    @Test
    void returnsDisplayType() {
        assert !classUnderTest.getDisplayType().isEmpty();
    }

    @Test
    void returnsDisplayCategory() {
        assert !classUnderTest.getDisplayCategory().isEmpty();
    }

    @Test
    void hasHelpText() {
        assert !classUnderTest.getHelpText().isEmpty();
    }

    @Test
    void hasConfigProperties() {
        assert !classUnderTest.getConfigProperties().isEmpty();
    }

    @Test
    void returnsCompatibleProviders() {
        assert classUnderTest.getCompatibleProviders().length > 0;
    }

}
