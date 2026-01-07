package de.treestack.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainRoleIdpMapperExtractDomainTest {

    @ParameterizedTest
    @CsvSource({
            "test@example.com,example.com",
            "first.last@EXAMPLE.COM,example.com",
            "user+mailbox@sub.domain.tld,sub.domain.tld",
            "  spaced@example.com  ,example.com"
    })
    void when_validEmail_expect_domainExtracted(String email, String expectedDomain) {
        assertEquals(expectedDomain, DomainRoleIdpMapper.extractDomain(email));
    }

    @Test
    void when_emptyDomain_expect_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> DomainRoleIdpMapper.extractDomain("test@ "));
    }

    @Test
    void when_noAtSymbol_expect_returnsNormalizedInput() {
        assertEquals("no-at-symbol", DomainRoleIdpMapper.extractDomain("no-at-symbol"));
    }

    @Test
    void when_noAtSymbolAndBlank_expect_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> DomainRoleIdpMapper.extractDomain("  "));
    }
}
