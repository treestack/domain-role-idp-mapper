package de.treestack.auth;

import java.util.Locale;

enum DomainMatchMode {
    EXACT,
    WILDCARD,
    REGEX;

    static DomainMatchMode from(String raw) {
        try {
            return raw != null
                    ? DomainMatchMode.valueOf(raw.toUpperCase(Locale.ROOT))
                    : EXACT;
        } catch (IllegalArgumentException e) {
            return EXACT;
        }
    }
}