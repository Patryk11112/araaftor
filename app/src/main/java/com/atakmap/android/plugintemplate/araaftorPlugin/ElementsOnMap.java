package com.atakmap.android.plugintemplate.araaftorPlugin;

import java.io.Serializable;

public class ElementsOnMap implements Serializable
{
    public double first;
    public double second;
    public String third;

    public ElementsOnMap(double first, double second, String third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public double getFirst() {
        return first;
    }

    public void setFirst(double first) {
        this.first = first;
    }

    public void setSecond(double second) {
        this.second = second;
    }

    public double getSecond() {
        return second;
    }

    public String getThird() {
        return third;
    }

    public void setThird(String third) {
        this.third = third;
    }

    @Override
    public String toString() {
        return "Tuple{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}
