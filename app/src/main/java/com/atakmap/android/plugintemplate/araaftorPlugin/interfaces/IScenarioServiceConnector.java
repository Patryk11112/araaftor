package com.atakmap.android.plugintemplate.araaftorPlugin.interfaces;

import java.util.LinkedList;

import com.atakmap.android.plugintemplate.araaftorPlugin.GeoObject;

public interface IScenarioServiceConnector {
    LinkedList<GeoObject> getScenarioObjects();

    boolean clearTasks(int unitId);
}
