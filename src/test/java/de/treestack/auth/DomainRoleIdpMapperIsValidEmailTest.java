package de.treestack.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainRoleIdpMapperIsValidEmailTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "test@example.com",
            "first.last@example.com",
            "user+mailbox@sub.domain.tld",
            "a@b.cd"
    })
    void when_validEmail_expect_true(String email) {
        assertTrue(DomainRoleIdpMapper.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "testexample.com",
            "test@",
            "@example.com",
            "test@.com",
            "test@example.",
            "test@example.c",
            "test@example.toolongtld",
            "test..test@example.com",
            ".test@example.com",
            "test.@example.com",
            "123@456.78"
    })
    void when_invalidEmail_expect_false(String email) {
        assertFalse(DomainRoleIdpMapper.isValidEmail(email));
    }

    @Test
    void when_nullEmail_expect_false() {
        assertFalse(DomainRoleIdpMapper.isValidEmail(null));
    }
}
