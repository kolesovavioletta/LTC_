package com.kolesova_violetta.ltc.ui.dialogs;

import com.kolesova_violetta.ltc.ui.dialogs.ConditionalFactory.ConditionalOfCloseDialogPref;
import com.kolesova_violetta.ltc.ui.dialogs.ConditionalFactory.FullNameConditional;
import com.kolesova_violetta.ltc.ui.dialogs.ConditionalFactory.LengthConditional;
import com.kolesova_violetta.ltc.ui.dialogs.ConditionalFactory.StateNumberConditional;
import com.kolesova_violetta.ltc.ui.dialogs.ConditionalFactory.VariableLengthConditional;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.kolesova_violetta.ltc.ui.fragments.MonitoringPreferenceFragment.PREF_PHONE_1;

public class ConditionalFactoryTest {

    private static FullNameConditional fullNameConditional;
    private static StateNumberConditional stateNumberConditional;
    private static ConditionalOfCloseDialogPref phoneConditional;
    private static ConditionalOfCloseDialogPref notNullConditional;

    @BeforeClass
    public static void setUp() {
        fullNameConditional = new FullNameConditional();
        stateNumberConditional = new StateNumberConditional();
        phoneConditional = ConditionalFactory.getConditionalByKey(PREF_PHONE_1);
        notNullConditional = ConditionalFactory.getConditionalByKey("random");
    }

    @Test
    public void fullNameConditional_CorrectShort_ReturnsTrue() {
        Assert.assertTrue(fullNameConditional.isSuccess("ABC"));
    }

    @Test
    public void fullNameConditional_CorrectShortReduction_ReturnsTrue() {
        Assert.assertTrue(fullNameConditional.isSuccess("AB"));
    }

    @Test
    public void fullNameConditional_Correct_ReturnsTrue() {
        Assert.assertTrue(fullNameConditional.isSuccess("AntonioBC"));
    }

    @Test
    public void fullNameConditional_InvalidAbbreviateWithSpaceMiddle_ReturnsFalse() {
        Assert.assertFalse(fullNameConditional.isSuccess("AntonioB C"));
    }

    @Test
    public void fullNameConditional_InvalidAbbreviateWithSpaceEnd_ReturnsFalse() {
        Assert.assertFalse(fullNameConditional.isSuccess("AntonioBC "));
    }

    @Test
    public void fullNameConditional_InvalidAbbreviateWithDots_ReturnsFalse() {
        Assert.assertFalse(fullNameConditional.isSuccess("AntonioB.C."));
    }

    @Test
    public void fullNameConditional_InvalidLong_ReturnsFalse() {
        Assert.assertFalse(fullNameConditional.isSuccess("Antonio Bob Constantin"));
    }

    @Test
    public void fullNameConditional_InvalidLastName_ReturnsFalse() {
        Assert.assertFalse(fullNameConditional.isSuccess("AntonioBConstantin"));
    }

    @Test
    public void fullNameConditional_InvalidNotAbbreviate_ReturnsFalse() {
        Assert.assertFalse(fullNameConditional.isSuccess("AntonioBobConstantin"));
    }

    @Test
    public void fullNameConditional_InvalidNotLatin_ReturnsFalse() {
        Assert.assertFalse(fullNameConditional.isSuccess("ФедорПМ"));
    }

    @Test
    public void fullNameConditional_InvalidShort_ReturnsFalse() {
        Assert.assertFalse(fullNameConditional.isSuccess("B"));
    }

    @Test
    public void fullNameConditional_EmptyString_ReturnsFalse() {
        Assert.assertFalse(fullNameConditional.isSuccess(""));
    }

    @Test
    public void fullNameConditional_Null_ReturnsFalse() {
        Assert.assertFalse(fullNameConditional.isSuccess(null));
    }

    //----------------------------------------------------------------------------------------------

    @Test
    public void lengthConditional_Correct_ReturnsTrue() {
       LengthConditional lengthConditional = new LengthConditional(8);
       Assert.assertTrue(lengthConditional.isSuccess("12345678"));
    }

    @Test
    public void lengthConditional_EmptyString_ReturnsTrue() {
        LengthConditional lengthConditional = new LengthConditional(0);
        Assert.assertTrue(lengthConditional.isSuccess(""));
    }

    @Test
    public void lengthConditional_Null_ReturnsTrue() {
        LengthConditional lengthConditional = new LengthConditional(0);
        Assert.assertTrue(lengthConditional.isSuccess(null));
    }

    @Test
    public void lengthConditional_InvalidStringLength_ReturnsFalse() {
        LengthConditional lengthConditional = new LengthConditional(8);
        Assert.assertFalse(lengthConditional.isSuccess("123"));
        Assert.assertFalse(lengthConditional.isSuccess("1234567"));
        Assert.assertFalse(lengthConditional.isSuccess("123456789"));
    }

    @Test
    public void lengthConditional_InvalidLengthValue_ReturnsFalse() {
        LengthConditional lengthConditional = new LengthConditional(-3);
        Assert.assertFalse(lengthConditional.isSuccess("123"));
    }

    //----------------------------------------------------------------------------------------------

    @Test
    public void variableLengthConditional_Correct_ReturnsTrue() {
        VariableLengthConditional lengthConditional = new VariableLengthConditional(6, 8);
        Assert.assertTrue(lengthConditional.isSuccess("123456"));
        Assert.assertTrue(lengthConditional.isSuccess("abcdefg"));
        Assert.assertTrue(lengthConditional.isSuccess("12345678"));
    }

    @Test
    public void variableLengthConditional_InvalidShortLength_ReturnsFalse() {
        VariableLengthConditional lengthConditional = new VariableLengthConditional(6, 8);
        Assert.assertFalse(lengthConditional.isSuccess("12345"));
    }

    @Test
    public void variableLengthConditional_InvalidLongLength_ReturnsFalse() {
        VariableLengthConditional lengthConditional = new VariableLengthConditional(6, 8);
        Assert.assertFalse(lengthConditional.isSuccess("123456789"));
    }

    @Test
    public void variableLengthConditional_EmptyString_ReturnsTrue() {
        VariableLengthConditional lengthConditional = new VariableLengthConditional(0, 8);
        Assert.assertTrue(lengthConditional.isSuccess(""));
    }

    @Test
    public void variableLengthConditional_Null_ReturnsTrue() {
        VariableLengthConditional lengthConditional = new VariableLengthConditional(0, 8);
        Assert.assertTrue(lengthConditional.isSuccess(null));
    }

    @Test
    public void lengthConditional_CorrectEqualsBounce_ReturnsTrue() {
        VariableLengthConditional lengthConditional = new VariableLengthConditional(8, 8);

        Assert.assertTrue(lengthConditional.isSuccess("12345678"));

        Assert.assertFalse(lengthConditional.isSuccess("1234567"));
        Assert.assertFalse(lengthConditional.isSuccess("123456789"));
    }

    @Test
    public void lengthConditional_InvalidRevertBounce_ReturnsFalse() {
        VariableLengthConditional lengthConditional = new VariableLengthConditional(8, 6);

        Assert.assertFalse(lengthConditional.isSuccess("123456"));
        Assert.assertFalse(lengthConditional.isSuccess("1234567"));
        Assert.assertFalse(lengthConditional.isSuccess("12345678"));
        Assert.assertFalse(lengthConditional.isSuccess("12345"));
        Assert.assertFalse(lengthConditional.isSuccess("123456789"));
    }

    @Test
    public void lengthConditional_InvalidNegativeBounce_ReturnsTrue() {
        VariableLengthConditional lengthConditional = new VariableLengthConditional(-2, 3);

        Assert.assertTrue(lengthConditional.isSuccess(""));
        Assert.assertTrue(lengthConditional.isSuccess("1"));
        Assert.assertTrue(lengthConditional.isSuccess("123"));

        Assert.assertFalse(lengthConditional.isSuccess("1234"));
    }

    //----------------------------------------------------------------------------------------------

    @Test
    public void stateNumberConditional_Correct_ReturnsTrue() {
        Assert.assertTrue(stateNumberConditional.isSuccess("ABCD 4567 EF"));
        Assert.assertTrue(stateNumberConditional.isSuccess("4567 ABCD"));
        Assert.assertTrue(stateNumberConditional.isSuccess("4567 ABCD "));
    }

    @Test
    public void stateNumberConditional_InvalidLongLength_ReturnsFalse() {
        Assert.assertFalse(stateNumberConditional.isSuccess("ABCD 4567 EFG"));
    }

    @Test
    public void stateNumberConditional_InvalidShortLength_ReturnsFalse() {
        Assert.assertFalse(stateNumberConditional.isSuccess("ABCD 456"));
    }

    @Test
    public void stateNumberConditional_InvalidNotLatin_ReturnsFalse() {
        Assert.assertFalse(stateNumberConditional.isSuccess("АБВГ 4567 ЕЖ"));
        Assert.assertFalse(stateNumberConditional.isSuccess("4567 АБВГ"));
    }

    @Test
    public void stateNumberConditional_EmptyString_ReturnsFalse() {
        Assert.assertFalse(stateNumberConditional.isSuccess(""));
    }

    @Test
    public void stateNumberConditional_Null_ReturnsFalse() {
        Assert.assertFalse(stateNumberConditional.isSuccess(null));
    }

    //----------------------------------------------------------------------------------------------

    @Test
    public void notNullConditional_Correct_ReturnsTrue() {
        Assert.assertTrue(notNullConditional.isSuccess("abc132"));
        Assert.assertTrue(notNullConditional.isSuccess(" "));
    }

    @Test
    public void notNullConditional_EmptyString_ReturnsFalse() {
        Assert.assertFalse(notNullConditional.isSuccess(""));
    }

    @Test
    public void notNullConditional_Null_ReturnsFalse() {
        Assert.assertFalse(notNullConditional.isSuccess(null));
    }

    //----------------------------------------------------------------------------------------------

    @Test
    public void phoneConditional_Correct_ReturnsTrue() {
        Assert.assertTrue(phoneConditional.isSuccess("+7 234 567 89 01"));
        Assert.assertTrue(phoneConditional.isSuccess("8 (234)567-89-01"));
    }

    @Test
    public void phoneConditional_InvalidShortLength_ReturnsFalse() {
        Assert.assertFalse(phoneConditional.isSuccess("8 (234)567-89"));
    }

    @Test
    public void phoneConditional_InvalidLongLength_ReturnsFalse() {
        Assert.assertFalse(phoneConditional.isSuccess("+7 234 567 89 01 45"));
    }

    @Test
    public void phoneNumberConditional_EmptyString_ReturnsFalse() {
        Assert.assertFalse(phoneConditional.isSuccess(""));
    }

    @Test
    public void phoneNumberConditional_Null_ReturnsFalse() {
        Assert.assertFalse(phoneConditional.isSuccess(null));
    }
}