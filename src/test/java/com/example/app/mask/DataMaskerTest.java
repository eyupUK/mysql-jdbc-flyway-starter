package com.example.app.mask;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DataMaskerTest {
    @Test
    void recognizesExactConfirmationAmongOtherArguments() {
        Assertions.assertTrue(DataMasker.isConfirmed(new String[]{"--confirm"}));
        Assertions.assertTrue(DataMasker.isConfirmed(new String[]{"--verbose", "--confirm"}));
    }

    @Test
    void rejectsMissingAndLookalikeConfirmations() {
        Assertions.assertFalse(DataMasker.isConfirmed(null));
        Assertions.assertFalse(DataMasker.isConfirmed(new String[0]));
        Assertions.assertFalse(DataMasker.isConfirmed(new String[]{"--confirm=true"}));
        Assertions.assertFalse(DataMasker.isConfirmed(new String[]{"--CONFIRM"}));
        Assertions.assertFalse(DataMasker.isConfirmed(new String[]{"confirm"}));
    }

    @Test
    void mainRefusesUnconfirmedRunsBeforeAccessingTheDatabase() {
        IllegalArgumentException noArguments = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> DataMasker.main(new String[0])
        );
        Assertions.assertEquals("Refusing to mask data without --confirm", noArguments.getMessage());

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> DataMasker.main(null)
        );
    }
}
