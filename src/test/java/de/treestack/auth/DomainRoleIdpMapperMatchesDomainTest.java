package de.treestack.auth;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static de.treestack.auth.DomainRoleIdpMapper.matchesDomain;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainRoleIdpMapperMatchesDomainTest {

        @Test
        void when_matchModeExact_expect_onlyExactMatches() {
            Set<String> domains = Set.of("treestack.de", "partner.org");

            assertTrue(matchesDomain("treestack.de", domains, DomainMatchMode.EXACT));
            assertTrue(matchesDomain("partner.org", domains, DomainMatchMode.EXACT));

            assertFalse(matchesDomain("www.treestack.de", domains, DomainMatchMode.EXACT));
            assertFalse(matchesDomain("dev.treestack.de", domains, DomainMatchMode.EXACT));
            assertFalse(matchesDomain("treestack.com", domains, DomainMatchMode.EXACT));
        }

        @Test
        void when_matchModeWildcard_expect_matchSubdomainsOnly() {
            Set<String> domains = Set.of("*.treestack.de");

            assertTrue(matchesDomain("dev.treestack.de", domains, DomainMatchMode.WILDCARD));

            assertFalse(matchesDomain("treestack.de", domains, DomainMatchMode.WILDCARD));
            assertFalse(matchesDomain("eviltreestack.de", domains, DomainMatchMode.WILDCARD));
            assertFalse(matchesDomain("api.dev.treestack.de", domains, DomainMatchMode.WILDCARD));
        }

        @Test
        void when_matchModeWildcard_and_multipleDomains_expect_matchSubdomainsOnly() {
            Set<String> domains = Set.of("*.treestack.de", "*.partner.org", "ex*mple.org");

            assertTrue(matchesDomain("dev.treestack.de", domains, DomainMatchMode.WILDCARD));
            assertTrue(matchesDomain("eu.partner.org", domains, DomainMatchMode.WILDCARD));

            assertFalse(matchesDomain("treestack.de", domains, DomainMatchMode.WILDCARD));
            assertFalse(matchesDomain("partner.org", domains, DomainMatchMode.WILDCARD));
            assertFalse(matchesDomain("explodingkittentemple.org", domains, DomainMatchMode.WILDCARD));
        }

        @Test
        void when_matchModeRegex_expect_matchWithRegex() {
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
        void when_onePatternInvalid_expect_toIgnoreIt() {
            Set<String> domains = Set.of(
                    "partner-.*\\.org",
                    "[invalid-regex"
            );

            // valid regex still works
            assertTrue(matchesDomain("partner-1.org", domains, DomainMatchMode.REGEX));

            // invalid regex must NOT crash and must NOT match
            assertFalse(matchesDomain("anything.com", domains, DomainMatchMode.REGEX));
        }

        @Test
        void when_domainListEmpty_expect_neverMatch() {
            Set<String> domains = Set.of();

            assertFalse(matchesDomain("treestack.de", domains, DomainMatchMode.EXACT));
            assertFalse(matchesDomain("dev.treestack.de", domains, DomainMatchMode.WILDCARD));
            assertFalse(matchesDomain("partner-1.org", domains, DomainMatchMode.REGEX));
        }

        @Test
        void when_domainListNull() {
            Set<String> domains = Set.of("treestack.de");

            assertFalse(matchesDomain(null, domains, DomainMatchMode.EXACT));
            assertFalse(matchesDomain(null, domains, DomainMatchMode.WILDCARD));
            assertFalse(matchesDomain(null, domains, DomainMatchMode.REGEX));
        }

    }