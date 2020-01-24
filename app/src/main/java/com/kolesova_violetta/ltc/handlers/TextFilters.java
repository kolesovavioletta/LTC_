package com.kolesova_violetta.ltc.handlers;

import android.text.InputFilter;
import android.widget.EditText;

public class TextFilters {
    /**
     * Вывод в текстовом поле только латинских символов.
     */
    public static void setEditTextFilterOnlyLatin(EditText editText) {
        editText.setFilters(new InputFilter[] {
                (charSequence, i, i1, spanned, i2, i3) -> {
                    if(charSequence == null || charSequence.equals("")) return null;
                    String ONLY_LETTER_REGEX = "^[a-zA-Z]*$";
                    String source = charSequence.subSequence(i, i1).toString();
                    boolean onlyLetter = source.matches(ONLY_LETTER_REGEX);
                    if(onlyLetter) {
                        return null; // keep original (don't change)
                    } else {
                        String NOT_LETTER_REGEX = "[^a-zA-Z]+";
                        return source.replaceAll(NOT_LETTER_REGEX, "");
                    }
                }
        });
    }

    /**
     * Ограничение количества символов в текстовом поле.
     */
    public static void setMaxLengthInputToEditText(EditText editText, int length) {
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(length);
        editText.setFilters(filterArray);
    }
}
