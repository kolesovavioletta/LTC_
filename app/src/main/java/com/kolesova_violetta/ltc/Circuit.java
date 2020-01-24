package com.kolesova_violetta.ltc;

import androidx.annotation.NonNull;

interface CalcCoefficient {
    //расчет по формуле коэффициента без груза
    default float calcFormula() {
        return 0f;
    }
    float calc(final int abcd, final int acd) throws ArithmeticException;
}

/**
 * Контур
 */
public class Circuit implements CalcCoefficient {
    private int axesCount; // количество осей
    private String type; // одностор / двустор
    private String var1; // переменная левого колеса / переменная контура
    private String var2; // переменная правого колеса
    private int weight; // вес контура
    private int[] weightAxes; // вес каждой оси контура

    public Circuit() {
    }

    public Circuit(int axesCount, String type, String var, int weight) {
        this.axesCount = axesCount;
        this.type = type;
        this.var1 = var;
        this.weight = weight;
    }

    public Circuit(int axesCount, String type, String var1, String var2, int weight) {
        this.axesCount = axesCount;
        this.type = type;
        this.var1 = var1;
        this.var2 = var2;
        this.weight = weight;
    }

    @Override
    public float calc(final int U_0, final int acd) throws ArithmeticException {
        if (acd < U_0) {
            throw new ArithmeticException("Ошибка датчика или данных (acd < U_0): " + acd + " < " + U_0);
        }
        // получение массы осей контура из пары односторонних контуров
        //...
        return calcFormula();
    }

    public int getAxesCount() {
        return axesCount;
    }

    public void setAxesCount(int axesCount) {
        this.axesCount = axesCount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if(!(type.equals("2") || type.equals("1"))) {
            throw new RuntimeException("Несуществующий тип контура " + type);
        }
        this.type = type;
    }

    public String getVar1() {
        return var1;
    }

    public void setVar1(String var1) {
        this.var1 = var1;
    }

    public String getVar2() {
        if(type.equals("2")) throw new RuntimeException("При двустороннем контуре не нужна вторая переменная");
        return var2;
    }

    public void setVar2(String var2) {
        if(type.equals("2")) throw new RuntimeException("При двустороннем контуре не нужна вторая переменная");
        this.var2 = var2;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int...weights) {
        this.weight = 0;
        for (int value : weights) {
            this.weight += value;
        }
    }

    public int[] getWeightAxes() {
        return weightAxes;
    }

    public void setWeightAxes(int[] weightAxes) {
        this.weightAxes = weightAxes;
    }

    @NonNull
    @Override
    public String toString() {
        return "{ axesCount: " + axesCount + ", " +
                "type: " + type + ", " +
                "var1: " + var1 + ", " +
                "var2: " + var2 + ", " +
                "weight: " + weight + "}";
    }
}

