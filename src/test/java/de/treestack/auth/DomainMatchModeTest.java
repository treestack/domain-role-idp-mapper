package de.treestack.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DomainMatchModeTest {

    @Test
    void when_null_expect_defaultToExact() {
        var actual = DomainMatchMode.from(null);
        assertEquals(DomainMatchMode.EXACT, actual);
    }

    @Test
    void when_invalid_expect_defaultToExact() {
        var actual = DomainMatchMode.from("invalid_value");
        assertEquals(DomainMatchMode.EXACT, actual);
    }

    @Test
    void when_givenExactValue_expect_returnCorrectValue() {
        DomainMatchMode actual = DomainMatchMode.from("exact");
        assertEquals(DomainMatchMode.EXACT, actual);

        actual = DomainMatchMode.from("wildcard");
        assertEquals(DomainMatchMode.WILDCARD, actual);

        actual = DomainMatchMode.from("regex");
        assertEquals(DomainMatchMode.REGEX, actual);
    }

    @Test
    void when_givenWrongCaseInput_expect_returnCorrectValue() {
        DomainMatchMode actual = DomainMatchMode.from("EXACT");
        assertEquals(DomainMatchMode.EXACT, actual);

        actual = DomainMatchMode.from("WiLdCaRd");
        assertEquals(DomainMatchMode.WILDCARD, actual);

        actual = DomainMatchMode.from("RegEx");
        assertEquals(DomainMatchMode.REGEX, actual);
    }

}