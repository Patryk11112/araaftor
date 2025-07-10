package com.atakmap.android.plugintemplate.detection;

public class BoundingBox {
    public float x1;
    public float y1;
    public float x2;
    public float y2;
    float cx;
    float cy;
    float w;
    public float h;
    public int cnf;
    int cls;
    public String clsName;
    private int index;

    public BoundingBox(float x1, float y1, float x2, float y2, float centerX, float centerY, float width, float height, int cnf, int index, String clsName) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.cx = centerX;
        this.cy = centerY;
        this.w = width;
        this.h = height;
        this.cnf = cnf;
        this.index = index;  // Inicjalizujemy index
        this.clsName = clsName;
    }

    // Gettery i settery dla wszystkich pól, w tym indeksu
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
