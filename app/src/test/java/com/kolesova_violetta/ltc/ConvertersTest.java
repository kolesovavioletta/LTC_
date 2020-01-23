package com.kolesova_violetta.ltc;

import com.kolesova_violetta.ltc.handlers.Converters;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConvertersTest {

    @Test
    public void sumValuesOfStringList1() {
        List<String> testList = Arrays.asList("2", "3", "4", "5");
        int expected = 2 + 3 + 4 + 5;
        int actual = Converters.sumValuesOfStringList(testList);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void sumValuesOfStringList2() {
        List<String> testList = Arrays.asList("0", "0", "0", "0");
        int expected = 0;
        int actual = Converters.sumValuesOfStringList(testList);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void sumValuesOfStringList3() {
        List<String> testList = Arrays.asList("-1", "0", "9", "0");
        int expected = 8;
        int actual = Converters.sumValuesOfStringList(testList);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void sumValuesOfStringList4() {
        List<String> testList = Collections.emptyList();
        int expected = 0;
        int actual = Converters.sumValuesOfStringList(testList);
        Assert.assertEquals(expected, actual);
    }
    //-------------------------------------------------------------------------------------------

    @Test
    public void getRand1() {
        int min = 0;
        int max = 10;
        int actual = Converters.getRand(min, max);
        Assert.assertTrue(actual >= min && actual <= max);
    }

    @Test
    public void getRand2() {
        int min = -7;
        int max = 2;
        int actual = Converters.getRand(min, max);
        Assert.assertTrue(actual >= min && actual <= max);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRand3() {
        int min = 4;
        int max = 1;
        Converters.getRand(min, max);
    }

    @Test
    public void getRand4() {
        int max, min;
        min = max = 1;
        int actual = Converters.getRand(min, max);
        Assert.assertTrue(actual >= min && actual <= max);
    }
    //-------------------------------------------------------------------------------------------

    @Test
    public void concat1() {
        float[] arr1 = new float[] {1, 2 ,3};
        float[] arr2 = new float[] {4, 5 ,6};
        float[] expected = new float[] {1, 2 ,3, 4, 5,6};
        float[] actual = Converters.concat(arr1, arr2);
        Assert.assertArrayEquals(expected, actual, 0);
    }

    @Test
    public void concat2() {
        float[] arr1 = new float[] {};
        float[] arr2 = new float[] {4, 5 ,6};
        float[] actual = Converters.concat(arr1, arr2);
        Assert.assertArrayEquals(arr2, actual, 0);
    }
    //-------------------------------------------------------------------------------------------
    @Test
    public void convertArrayToString1() {
        int[] arr = new int[] {1, 2 ,3};
        String expected = "123";
        String actual = Converters.convertArrayToString(arr);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void convertArrayToString2() {
        int[] arr = new int[] {};
        String expected = "";
        String actual = Converters.convertArrayToString(arr);
        Assert.assertEquals(expected, actual);
    }
//-------------------------------------------------------------------------------------------

    @Test
    public void stringToIntArray1() {
        int[] expected = new int[] {1, 2 ,3};
        String task = "123";
        int[] actual = Converters.stringToIntArray(task);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void stringToIntArray2() {
        int[] expected = new int[] {};
        String task = "";
        int[] actual = Converters.stringToIntArray(task);
        Assert.assertArrayEquals(expected, actual);
    }
}