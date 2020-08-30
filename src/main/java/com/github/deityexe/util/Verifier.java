package com.github.deityexe.util;

/**
 * Verification helper for input parameter values.
 */
public class Verifier {

    /**
     * Checks, if the given field might be a valid account name.
     *
     * @param field The field to be verified.
     * @return True, if the value from the field might be a valid account name.
     */
    public boolean mayBeAccount(final String field) {
        if (field.contains(" ") || field.startsWith("@")) {
            return false;
        }

        return true;
    }
}
