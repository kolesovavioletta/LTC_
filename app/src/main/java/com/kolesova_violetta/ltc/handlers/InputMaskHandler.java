package com.kolesova_violetta.ltc.handlers;

import android.widget.EditText;

import ru.tinkoff.decoro.MaskImpl;
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser;
import ru.tinkoff.decoro.slots.PredefinedSlots;
import ru.tinkoff.decoro.slots.Slot;
import ru.tinkoff.decoro.slots.SlotValidatorSet;
import ru.tinkoff.decoro.slots.SlotValidators;
import ru.tinkoff.decoro.watchers.FormatWatcher;
import ru.tinkoff.decoro.watchers.MaskFormatWatcher;

public class InputMaskHandler {

    public final static Slot[] TRAILER_STATE_NUMBER = {
            letter(),
            letter(),
            PredefinedSlots.hardcodedSlot(' ').withTags(Slot.TAG_DECORATION),
            PredefinedSlots.digit(),
            PredefinedSlots.digit(),
            PredefinedSlots.digit(),
            PredefinedSlots.digit(),
            PredefinedSlots.hardcodedSlot(' ').withTags(Slot.TAG_DECORATION),
            PredefinedSlots.digit(),
            PredefinedSlots.digit(),
            PredefinedSlots.digit(),
            PredefinedSlots.digit(),
    };

    public final static Slot[] TRACTOR_STATE_NUMBER = {
            letter(),
            PredefinedSlots.hardcodedSlot(' ').withTags(Slot.TAG_DECORATION),
            PredefinedSlots.digit(),
            PredefinedSlots.digit(),
            PredefinedSlots.digit(),
            PredefinedSlots.hardcodedSlot(' ').withTags(Slot.TAG_DECORATION),
            letter(),
            letter(),
            PredefinedSlots.hardcodedSlot(' ').withTags(Slot.TAG_DECORATION),
            PredefinedSlots.digit(),
            PredefinedSlots.digit(),
            PredefinedSlots.digit(),
            PredefinedSlots.digit()
    };

    public final static Slot[] VIN_NUMBER = {
            letterOrDigit(),
            letterOrDigit(),
            letterOrDigit(),
            letterOrDigit(),
            letterOrDigit(),
            letterOrDigit(),
            letterOrDigit(),
            letterOrDigit(),
            letterOrDigit(),
            letterOrDigit(),
            letterOrDigit(),
            letterOrDigit(),
            letterOrDigit(),
            letterOrDigit(),
            letterOrDigit(),
            letterOrDigit(),
            letterOrDigit(),
            letterOrDigit(),
            letterOrDigit(),
            letterOrDigit()
    };

    private static Slot letter() {
        return new Slot(null,
                new SlotValidators.LetterValidator(true, false));
    }

    public static void setMaskOnEditText(Slot[] slots, EditText editText) {
        String savedText = editText.getText().toString();
        MaskImpl mask = MaskImpl.createTerminated(slots);
        if (!savedText.isEmpty() && !savedText.equals("0")) {
            mask.insertFront(savedText);
        }
        FormatWatcher formatWatcher = new MaskFormatWatcher(mask);
        formatWatcher.installOn(editText);
    }

    public static void setMaskOnEditText(String mask, EditText editText) {
        Slot[] slots = new UnderscoreDigitSlotsParser().parseSlots(mask);
        setMaskOnEditText(slots, editText);
    }

    private static Slot letterOrDigit() {
        //TODO: Добавить ограничения на символы 'I' 'O' 'Q'
        //return new Slot(null, new CombinationValidatorForVin());
        return new Slot(null,
                SlotValidatorSet.setOf(
                        new SlotValidators.DigitValidator(),
                        new SlotValidators.LetterValidator(true, false)));
    }

    public static class CombinationValidatorForVin implements Slot.SlotValidator {

        @Override
        public boolean validate(char value) {
            return validateEnglishLetterOrDigit(value);
        }

        private boolean validateEnglishLetterOrDigit(final char value) {
            return (isEnglishCharacter(value) && !isEnglishCharacterException(value)) ||
                    Character.isDigit(value);
        }

        private boolean isEnglishCharacter(final int charCode) {
            return 'A' <= charCode && charCode <= 'Z';
        }

        private boolean isEnglishCharacterException(final int charCode) {
            return 'I' == charCode || 'O' == charCode || charCode == 'Q';
        }

        /*
         * equals(Object) and hashCode() here are override in order to allow have only single
         * instance of this validator in a SlotValidatorSet (which is actually just a HashSet).
         */

        @Override
        public int hashCode() {
            return -56330;
        }

        @Override
        public boolean equals(Object o) {
            // for letter
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            // for digit
            return o instanceof CombinationValidatorForVin;
        }
    }

    public static class CombinationValidatorForVin2 extends SlotValidatorSet {

        @Override
        public boolean validate(char value) {
            return validateEnglishLetterOrDigit(value);
        }

        private boolean validateEnglishLetterOrDigit(final char value) {
            return (isEnglishCharacter(value) && !isEnglishCharacterException(value)) ||
                    Character.isDigit(value);
        }

        private boolean isEnglishCharacter(final int charCode) {
            return 'A' <= charCode && charCode <= 'Z';
        }

        private boolean isEnglishCharacterException(final int charCode) {
            return 'I' == charCode || 'O' == charCode || charCode == 'Q';
        }

        /*
         * equals(Object) and hashCode() here are override in order to allow have only single
         * instance of this validator in a SlotValidatorSet (which is actually just a HashSet).
         */

        @Override
        public int hashCode() {
            return -56330;
        }

        @Override
        public boolean equals(Object o) {
            // for letter
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            // for digit
            return o instanceof CombinationValidatorForVin;
        }
    }
}
