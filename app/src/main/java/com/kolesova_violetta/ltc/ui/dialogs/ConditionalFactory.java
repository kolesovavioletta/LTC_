package com.kolesova_violetta.ltc.ui.dialogs;

import android.util.Log;

import static com.kolesova_violetta.ltc.mock.Const.*;
import static com.kolesova_violetta.ltc.ui.fragments.view.MonitoringPreferenceFragment.PREF_PHONE_1;

/**
 * Выбор условия корректности данных по preferenceKey
 */
public class ConditionalFactory {
    static ConditionalOfCloseDialogPref getConditionalByKey(String key) {
        switch (key) {
            case PREF_TRAILER_VIN:
            case PREF_TRACTOR_VIN:
                return new VariableLengthConditional(17, 20);
            case PREF_TRAILER_ID:
                return new LengthConditional(7);
            case PREF_TRAILER_NUMBER:
            case PREF_TRACTOR_NUMBER:
                return new StateNumberConditional();
            case PREF_PHONE_1:
                return new PhoneConditional();
            case PREF_DRIVER_NAME:
                return new FullNameConditional();
            case PREF_TRACTOR_YEAR:
            case PREF_TRAILER_YEAR:
                return new LengthConditional(4);
            default:
                return new NotNullConditional();
        }
    }

    public interface ConditionalOfCloseDialogPref {
        boolean isSuccess(String value);
    }

    /**
     * Проверка ввода пользователя на соответствие шаблону: ИмяФ / ИмяФО
     */
    public static class FullNameConditional implements ConditionalOfCloseDialogPref {
        @Override
        public boolean isSuccess(String value) {
            // строка не может быть пустой
            if(value == null || value.isEmpty()) {
                return false;
            }
            // строка имеет минимум 2 символа ИФ
            int length = value.length();
            if(length < 2) {
                return false;
            }
            // в строке могут быть только латинские символы
            for (int i = 0; i < value.length(); i++) {
                char currentChar = value.charAt(i);
                if(!isAvailableSymbol(currentChar)) {
                    return false;
                }
            }
            // первый и последний символ обязательно заглавные
            value = value.substring(0, 1) + value.substring(length-1, length);
            return value.equals(value.toUpperCase());
        }

        private boolean isAvailableSymbol(char ch) {
            return 'A' <= ch && ch <= 'Z' || 'a' <= ch && ch <= 'z';
        }
    }

    /**
     * Сравнение длины строки с заданным значением
     * mLength требуемая длина строки
     */
    public static class LengthConditional implements ConditionalOfCloseDialogPref {
        private int mLength;

        LengthConditional(int length) {
            mLength = length;

            if(mLength < 0) {
                Log.e("!@#$", "LengthConditional:LengthConditional:81: " +
                        "Длина строки не может быть отрицательным значением " + length);
                mLength = 0;
            }
        }

        @Override
        public boolean isSuccess(String value) {
            if(value == null) {
                value = "";
            }
            return value.length() == mLength;
        }
    }

    /**
     * Сравнение длины строки с заданными граничными значениями
     * mMin минимальная требуемая длина строки
     * mMax максимальная требуемая длина строки
     */
    public static class VariableLengthConditional implements ConditionalOfCloseDialogPref {
        private int mMin;
        private int mMax;

        VariableLengthConditional(int min, int max) {
            mMin = min;
            mMax = max;

            if(mMin < 0) {
                Log.e("!@#$", "VariableLengthConditional:VariableLengthConditional:105: " +
                        "Длина строки не может быть отрицательным значением " + mMin);
                mMin = 0;
            }

            if(mMax < 0) {
                Log.e("!@#$", "VariableLengthConditional:VariableLengthConditional:111: " +
                        "Длина строки не может быть отрицательным значением " + mMax);
                mMax = 0;
            }

            if(mMax <= mMin) {
                Log.d("!@#$", "VariableLengthConditional:VariableLengthConditional:117: " +
                        "Максимальная длина строки не может быть меньше или равна минимальной " +
                        "Max = " + mMax + " mMin = " + mMin);
            }
        }

        @Override
        public boolean isSuccess(String value) {
            if(value == null) {
                value = "";
            }
            int length = value.length();
            return mMin <= length && length <= mMax;
        }
    }

    /**
     * Сравнение гос номера с шаблоном: длина строки 8 - 10 латинских заглавных символов
     */
    public static class StateNumberConditional implements ConditionalOfCloseDialogPref {
        @Override
        public boolean isSuccess(String value) {
            // строка не может быть пустой
            if(value == null || value.isEmpty()) {
                return false;
            }
            // символы форматирования не учитываются
            value = deleteUnderscoreAndSpace(value);
            // в строке могут быть только заглавные латинские символы
            for (int i = 0; i < value.length(); i++) {
                char currentChar = value.charAt(i);
                if(!isAvailableSymbol(currentChar)) {
                    return false;
                }
            }
            int length = value.length();
            return length > 7 && length < 11;
        }

        private static boolean isAvailableSymbol(char ch) {
            return 'A' <= ch && ch <= 'Z' || Character.isDigit(ch);
        }

        /**
         * Удаление из строки сомволов: нижнее подчеркивание и пробел
         */
        private static String deleteUnderscoreAndSpace(String value) {
            return value
                    .replace("_", "")
                    .replace(" ", "");
        }
    }

    /**
     * Проверка на НЕ пустое значение
     */
    private static class NotNullConditional implements ConditionalOfCloseDialogPref {
        @Override
        public boolean isSuccess(String value) {
            return !(value == null || value.isEmpty());
        }
    }

    /**
     * Сравнение телефона с шаблоном: 11 цифр
     */
    private static class PhoneConditional implements ConditionalOfCloseDialogPref {
        @Override
        public boolean isSuccess(String value) {
            if(value == null || value.isEmpty()) {
                return false;
            }
            return value
                    .replaceAll("\\D", "")
                    .length() == 11;
        }
    }
}
