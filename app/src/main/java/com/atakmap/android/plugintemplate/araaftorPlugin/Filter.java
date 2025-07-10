package com.atakmap.android.plugintemplate.araaftorPlugin;

public class Filter {

    private String filter;
    private String name;
    private int alpha = 255;
    private boolean visible = true;

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Filter(String name, String filter, String visible, String alpha) {
        this.filter = filter;
        this.name = name;
        this.alpha = Integer.parseInt(alpha);
        this.visible = Boolean.parseBoolean(visible);
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

}