package de.treestack.auth;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static de.treestack.auth.DomainRoleIdpMapper.matchesDomain;
import static org.junit.jupiter.api.Assertions.*;

class DomainMatchModeTest {

    // ---------- EXACT ----------

    @Test
    void exact_shouldMatchOnlyExactDomain() {
        Set<String> domains = Set.of("treestack.de", "partner.org");

        assertTrue(matchesDomain("treestack.de", domains, DomainMatchMode.EXACT));
        assertTrue(matchesDomain("partner.org", domains, DomainMatchMode.EXACT));

        assertFalse(matchesDomain("www.treestack.de", domains, DomainMatchMode.EXACT));
        assertFalse(matchesDomain("dev.treestack.de", domains, DomainMatchMode.EXACT));
        assertFalse(matchesDomain("treestack.com", domains, DomainMatchMode.EXACT));
    }

    // ---------- WILDCARD ----------

    @Test
    void wildcard_shouldMatchSubdomainsOnly() {
        Set<String> domains = Set.of("*.treestack.de");

        assertTrue(matchesDomain("dev.treestack.de", domains, DomainMatchMode.WILDCARD));
        assertTrue(matchesDomain("api.dev.treestack.de", domains, DomainMatchMode.WILDCARD));

        // intentionally NOT matched
        assertFalse(matchesDomain("treestack.de", domains, DomainMatchMode.WILDCARD));
        assertFalse(matchesDomain("eviltreestack.de", domains, DomainMatchMode.WILDCARD));
    }

    @Test
    void wildcard_shouldHandleMixedEntries() {
        Set<String> domains = Set.of("*.treestack.de", "*.partner.org");

        assertTrue(matchesDomain("dev.treestack.de", domains, DomainMatchMode.WILDCARD));
        assertTrue(matchesDomain("eu.partner.org", domains, DomainMatchMode.WILDCARD));

        assertFalse(matchesDomain("treestack.de", domains, DomainMatchMode.WILDCARD));
        assertFalse(matchesDomain("partner.org", domains, DomainMatchMode.WILDCARD));
    }

    // ---------- REGEX ----------

    @Test
    void regex_shouldMatchUsingJavaRegex() {
        Set<String> domains = Set.of(
                "partner-.*\\.org",
                "corp[0-9]+\\.internal"
        );

        assertTrue(matchesDomain("partner-1.org", domains, DomainMatchMode.REGEX));
        assertTrue(matchesDomain("partner-eu.org", domains, DomainMatchMode.REGEX));
        assertTrue(matchesDomain("corp12.internal", domains, DomainMatchMode.REGEX));

        assertFalse(matchesDomain("partner.org", domains, DomainMatchMode.REGEX));
        assertFalse(matchesDomain("corp.internal", domains, DomainMatchMode.REGEX));
    }

    @Test
    void regex_shouldIgnoreInvalidPatterns() {
        Set<String> domains = Set.of(
                "partner-.*\\.org",
                "[invalid-regex"
        );

        // valid regex still works
        assertTrue(matchesDomain("partner-1.org", domains, DomainMatchMode.REGEX));

        // invalid regex must NOT crash and must NOT match
        assertFalse(matchesDomain("anything.com", domains, DomainMatchMode.REGEX));
    }

    // ---------- EDGE CASES ----------

    @Test
    void emptyConfiguredDomains_shouldNeverMatch() {
        Set<String> domains = Set.of();

        assertFalse(matchesDomain("treestack.de", domains, DomainMatchMode.EXACT));
        assertFalse(matchesDomain("dev.treestack.de", domains, DomainMatchMode.WILDCARD));
        assertFalse(matchesDomain("partner-1.org", domains, DomainMatchMode.REGEX));
    }

    @Test
    void nullDomain_shouldNeverMatch() {
        Set<String> domains = Set.of("treestack.de");

        assertFalse(matchesDomain(null, domains, DomainMatchMode.EXACT));
        assertFalse(matchesDomain(null, domains, DomainMatchMode.WILDCARD));
        assertFalse(matchesDomain(null, domains, DomainMatchMode.REGEX));
    }

}