package com.atakmap.android.plugintemplate.araaftorPlugin.scenario;

import android.util.Log;

import com.atakmap.android.plugintemplate.araaftorPlugin.GeoObject;
import com.atakmap.android.plugintemplate.araaftorPlugin.Unit;
import com.atakmap.android.plugintemplate.araaftorPlugin.db.DB;
import com.atakmap.android.plugintemplate.araaftorPlugin.interfaces.IScenarioServiceConnector;

import java.util.LinkedList;

import com.atakmap.android.plugintemplate.araaftorPlugin.Filter;

public class Scenario {
    private final DB db;
    private LinkedList<GeoObject> scenarioObjects = new LinkedList<>();
    private IScenarioServiceConnector connector;

    public LinkedList<GeoObject> getScenarioObjects() {
        return scenarioObjects;
    }

    public Scenario(DB db) {
        this.db = db;
    }

    public void clearScenarioObjects() {
        GeoObject temp = scenarioObjects.getFirst();
        scenarioObjects.clear();
        scenarioObjects.add(temp);
    }

    public void clearAllScenarioObjects() {
        scenarioObjects.clear();
    }

    public LinkedList<GeoObject> getScenarioObjectsByFilter(Filter filter) {
        LinkedList<GeoObject> filteredObjects = new LinkedList<>();
        for (int i = 0; i < scenarioObjects.size(); i++) {
            String code = scenarioObjects.get(i).getApp6aCode();
            if (matches(code, filter) && !filteredObjects.contains(scenarioObjects.get(i)))
                filteredObjects.add(scenarioObjects.get(i));
        }
        return filteredObjects;
    }

    private boolean matches(String code, Filter filter) {
        char[] codeArray = code.toCharArray();
        char[] filterArray = filter.getFilter().toCharArray();
        for (int i = 0; i < codeArray.length; i++)
            if (filterArray[i] != codeArray[i] && filterArray[i] != '-')
                return false;
        return true;
    }

    public void addUnitToScenario(Unit unit, String superiorName) {
        scenarioObjects.add(unit);
        Integer supid = null;
        if (superiorName != null)
            supid = db.getUnitIdByName(superiorName);
        db.addUnit(unit, supid);
        if (supid != null) {
            connector.clearTasks(supid);
        }
    }

    public void addUnitToScenario(Unit unit, int supid) {
        db.addUnit(unit, supid);
        if (supid != 0) {
            connector.clearTasks(supid);
        }
    }

    public void processUnits(LinkedList<Unit> units) {
        Log.d("SAME-SCENARIO", "Units size: " + units.size());
        for (Unit unit : units) {
            getScenarioObjects().add(unit);
            if (unit.getSuperior() != null) {
                Log.d("SAME-SCENARIO", "Adding unit: " + unit.toString() + " subordinate of: " + unit.getSuperior().getId());
            } else
                Log.d("SAME-SCENARIO", "Adding unit: " + unit.toString());
            if (unit.getSubordinates() != null)
                processUnits(unit.getSubordinates());
        }

    }

    public Unit getUnitIfExists(String guid) {
        for (GeoObject geoObject : scenarioObjects) {
            if (geoObject instanceof Unit) {
                if (((Unit) geoObject).getGuid().equals(guid)) {
                    return (Unit) geoObject;
                }
            }
        }

        return null;
    }

    public DB getDb() {
        return db;
    }
}
