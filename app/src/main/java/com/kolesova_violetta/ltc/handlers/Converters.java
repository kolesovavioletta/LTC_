package com.kolesova_violetta.ltc.handlers;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.ArrayRes;
import androidx.annotation.DrawableRes;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Обработчики над примитивами, коллекциями
 */
public class Converters {

    public static int sumValuesOfStringList(List<String> list) {
        if(list == null || list.isEmpty()) return 0;

        int sum = 0;
        for(String s : list)
            sum += Integer.valueOf(s);
        return sum;
    }

    public static float[] concat(float[] first, float[] second) {
        float[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static String convertArrayToString(int[] strArray) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int value : strArray) {
            stringBuilder.append(value);
        }
        return stringBuilder.toString();
    }

    public static int[] stringToIntArray(String str) {
        char[] chArr = str.toCharArray();
        int[] intArr = new int[chArr.length];
        for (int i = 0; i < chArr.length; i++) {
            intArr[i] = Character.getNumericValue(chArr[i]);
        }
        return intArr;
    }
    /*public static void saveOrderedCollection(SharedPreferences sp, Collection collection, String key){
        JSONArray jsonArray = new JSONArray(collection);
        sp.edit().putString(key, jsonArray.toString()).apply();
    }

    public static Collection loadOrderedCollection(SharedPreferences sp, String key){
        List<Object> arrayList = new ArrayList<>();
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(sp.getString(key, "[]"));
            for (int i = 0; i < jsonArray.length(); i++) {
                arrayList.add(jsonArray.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return arrayList;
    }

    public static String replaceEachWordOnSeparateLine(String s) {
        return s.replace(" ", "\n").trim();
    }*/

    public static int getRand(int min, int max) {
        int diff = max - min;
        Random random = new Random();
        int i = random.nextInt(diff + 1);
        i += min;
        return i;
    }

    /*public static float[] fFloatToPrimitive(Float[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        final float[] result = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].floatValue();
        }
        return result;
    }

    public static int[] convertStringToIntArray(String str) {
        String[] sArr = str.trim().split(" ");
        int[] iArr = new int[sArr.length];
        for(int i = 0; i < sArr.length; i++)
            iArr[i] = Integer.parseInt(sArr[i]);
        return iArr;
    }*/

    @DrawableRes
    public static int getImage(Context c, String ImageName) {
        return c.getResources().getIdentifier(
                ImageName, "drawable", c.getPackageName());
    }

    public static Drawable getImage(Context c, int image) {
        return c.getResources().getDrawable(image);
    }

    public static String[] getArray(Context c, String arrayName) {
        int id = getArrayId(c, arrayName);
        if (id <= 0) return null;

        return c.getResources().getStringArray(id);
    }

    @ArrayRes
    public static int getArrayId(Context c, String arrayName) {
        return c.getResources().getIdentifier(
                arrayName, "array", c.getPackageName());
    }
}
