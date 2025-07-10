package com.atakmap.android.plugintemplate.araaftorPlugin.services;

import android.util.Log;

import com.atakmap.android.plugintemplate.araaftorPlugin.signs.Symbol;

public class SymbolContainer {
    String fileCode;
    Symbol symbol;
    private int count = 1;
    SymbolCacheService parent;

    public SymbolContainer(SymbolCacheService parent, Symbol symbol, String fileCode) {
        this.parent = parent;
        this.symbol = symbol;
        this.fileCode = fileCode;
    }

    public void decreaseCount() {
        count--;
        if (count == 0) {
            Log.d("SAMECache", "Freed: " + symbol.getCode());
            parent.removeFromList(this);
        }
    }

    public void increaseCount() {
        count++;
    }

    public void increaseCount(int i) {
        count += i;
    }

    public void decreaseCount(int i) {
        count -= i;

        if (count <= 0) {
            parent.removeFromList(this);
        }
    }

    public String getFileCode() {
        return fileCode;
    }
}
