package com.atakmap.android.plugintemplate.araaftorPlugin.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.atakmap.android.plugintemplate.araaftorPlugin.Unit;

import org.osmdroid.util.GeoPoint;

import java.util.LinkedList;


public class DB {

    public class DBException extends Exception {

        public DBException(String detailMessage) {
            super(detailMessage);
        }
    }

    private DbHelper dbHelper;
    private SQLiteDatabase database;

    public DB(Context context) {
        dbHelper = new DbHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    public String[] getMapNames() {
        String[] cols = new String[]{"name"};
        String[] names;
        Cursor mCursor = database.query(true, DbHelper.WMS_TABLE_NAME, cols, null, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
            int cnt = mCursor.getCount();
            names = new String[cnt];
            for (int i = 0; i < cnt; i++) {
                names[i] = mCursor.getString(0);
                mCursor.moveToNext();
            }
            return names;
        }
        return new String[]{};
    }

    public long saveMap(String name, String url, String layers, String wmsmap) {
        Cursor mCursor = database.query(DbHelper.WMS_TABLE_NAME, new String[]{"_id"}, null, null, null, null, "_id DESC");
        int id = 1;
        if (mCursor != null) {
            mCursor.moveToFirst();
            if (mCursor.getCount() != 0) {
                id = mCursor.getInt(0);
                id++;
            }
        }

        ContentValues values = new ContentValues();
        values.put("_id", id);
        values.put("name", name);
        values.put("url", url);
        values.put("layers", layers);
        values.put("wmsmap", wmsmap);
        return database.insert(DbHelper.WMS_TABLE_NAME, null, values);
    }

    public long updateMap(String oldName, String newName, String url, String layers, String wmsmap) {
        ContentValues values = new ContentValues();
        values.put("name", newName);
        values.put("url", url);
        values.put("layers", layers);
        values.put("wmsmap", wmsmap);
        return database.update(DbHelper.WMS_TABLE_NAME, values, "name='" + oldName + "'", null);
    }

    public String[] loadMap(String name) {
        String[] map = new String[3];
        Cursor mCursor = database.query(DbHelper.WMS_TABLE_NAME, new String[]{"url, layers", "wmsmap"}, "name='" + name + "'", null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
            map[0] = mCursor.getString(0);
            map[1] = mCursor.getString(1);
            map[2] = mCursor.getString(2);
            return map;
        } else return null;
    }

    public String[] loadMapById(int id) {
        String[] map = new String[3];
        Cursor mCursor = database.query(DbHelper.WMS_TABLE_NAME, new String[]{"url, layers", "wmsmap"}, "_id='" + id + "'", null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
            if (mCursor.getCount() == 0) {
                return null;
            }
            map[0] = mCursor.getString(0);
            map[1] = mCursor.getString(1);
            map[2] = mCursor.getString(2);
            return map;
        } else return null;
    }


    public String loadSetting(String setting, int userId) {
        Cursor mCursor;
        if (userId == 0)
            mCursor = database.query(DbHelper.SETTINGS_TABLE_NAME, new String[]{"value"}, "setting='" + setting + "'", null, null, null, null);
        else
            mCursor = database.query(DbHelper.SETTINGS_TABLE_NAME, new String[]{"value"}, "setting='" + setting + "' and uid=" + userId, null, null, null, null);
        String val = "";
        if (mCursor != null) {
            mCursor.moveToFirst();
            if (mCursor.getCount() > 0)
                val = mCursor.getString(0);
            else if (userId != 0) {

                ContentValues cv = new ContentValues();
                cv.put("setting", setting);
                cv.put("value", "1");
                cv.put("uid", userId);
                database.insert(DbHelper.SETTINGS_TABLE_NAME, null, cv);
                val = "1";
            }
        }
        return val;
    }

    public void saveSetting(int userId, String setting, String value) {
        ContentValues values = new ContentValues();
        values.put("value", value);
        database.update(DbHelper.SETTINGS_TABLE_NAME, values, "setting='" + setting + "' and uid=" + userId, null);
    }

    public void updateSelectedUser(int newUser) {
        ContentValues values = new ContentValues();
        values.put("value", newUser + "");
        database.update(DbHelper.SETTINGS_TABLE_NAME, values, "setting='SelectedUser'", null);
    }

    public int getSelectedUser() {
        Cursor mCursor;
        mCursor = database.query(DbHelper.SETTINGS_TABLE_NAME, new String[]{"value"}, "setting='SelectedUser'", null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
            if (mCursor.getCount() > 0)
                return Integer.parseInt(mCursor.getString(0));
        }

        return 0;
    }

    public boolean checkAdminAcess() {
        Cursor mCursor;
        mCursor = database.query(DbHelper.USER_TABLE_NAME, new String[]{"admin"}, "_id=" + getSelectedUser(), null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
            if (mCursor.getCount() > 0) {
                if (Integer.parseInt(mCursor.getString(0)) == 1) return true;
                else return false;
            }
        }
        return false;
    }

    public void updateButtonSize(float buttonSize) {
        ContentValues values = new ContentValues();
        values.put("value", buttonSize + "");
        database.update(DbHelper.SETTINGS_TABLE_NAME, values, "setting='ButtonSize'", null);
    }

    public float getButtonSize() {
        Cursor mCursor;
        mCursor = database.query(DbHelper.SETTINGS_TABLE_NAME, new String[]{"value"}, "setting='ButtonSize'", null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
            if (mCursor.getCount() > 0)
                return Float.parseFloat(mCursor.getString(0));
        }

        return 0;
    }

    public String[][] loadFiltersSettings() {
        Cursor mCursor = database.query(DbHelper.FILTERS_TABLE_NAME, new String[]{"name", "filter", "visible", "transparency"}, null, null, null, null, null);
        String[][] val = null;
        if (mCursor != null) {
            mCursor.moveToFirst();
            if (mCursor.getCount() > 0) {
                val = new String[mCursor.getCount()][4];
                for (int i = 0; i < val.length; i++) {
                    val[i] = new String[]{mCursor.getString(0), mCursor.getString(1), mCursor.getString(2), mCursor.getString(3)};
                    mCursor.moveToNext();
                }
            }
        }
        return val;
    }

    public String[][] loadSignesFiltersSettings() {
        Cursor mCursor = database.query(DbHelper.FILTERS_TABLE_NAME, new String[]{"name", "filter", "visible", "transparency"}, "signfilter=1", null, null, null, null);
        String[][] val = null;
        if (mCursor != null) {
            mCursor.moveToFirst();
            if (mCursor.getCount() > 0) {
                val = new String[mCursor.getCount()][4];
                for (int i = 0; i < val.length; i++) {
                    val[i] = new String[]{mCursor.getString(0), mCursor.getString(1), mCursor.getString(2), mCursor.getString(3)};
                    mCursor.moveToNext();
                }
            }
        }
        return val;
    }

    public String[] loadFilterSettings(String filterName) {
        Cursor mCursor = database.query(DbHelper.FILTERS_TABLE_NAME, new String[]{"name", "filter", "visible", "transparency"}, "name='" + filterName + "'", null, null, null, null);
        String[] val = null;
        if (mCursor != null) {
            mCursor.moveToFirst();
            if (mCursor.getCount() > 0) {
                val = new String[]{mCursor.getString(0), mCursor.getString(1), mCursor.getString(2), mCursor.getString(3)};
            }
        }
        return val;
    }

    public void saveFilterSetting(String name, Boolean visible) {
        ContentValues values = new ContentValues();
        values.put("visible", Boolean.toString(visible));
        database.update(DbHelper.FILTERS_TABLE_NAME, values, "name='" + name + "'", null);
    }

    public long saveFilterSetting(String name, int transparency) {
        ContentValues values = new ContentValues();
        values.put("transparency", Integer.toString(transparency));
        return database.update(DbHelper.FILTERS_TABLE_NAME, values, "name='" + name + "'", null);
    }

    public boolean checkPIN(int userId, int pin) {

        Cursor mCursor = database.query(DbHelper.USER_TABLE_NAME, new String[]{"pin"}, "_id='" + userId + "'", null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
            if (mCursor.getCount() == 0)
                return false;
            return pin == mCursor.getInt(0);
        } else return false;
    }

    public void updatePin(int userId, int newPin) {
        ContentValues values = new ContentValues();
        values.put("pin", newPin);
        database.update(DbHelper.USER_TABLE_NAME, values, "_id=" + userId, null);
    }

    public String[] getUser(int id) {
        String[] user = new String[10];
        user[0] = String.valueOf(id);
        Cursor mCursor = database.query(DbHelper.USER_TABLE_NAME, new String[]{"name", "password", "firstName", "surname", "rank", "function", "pin", "country", "rankShort"}, "_id='" + id + "'", null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
            if (mCursor.getCount() == 0)
                return null;
            user[1] = String.valueOf(mCursor.getString(0));
            user[2] = String.valueOf(mCursor.getString(1));
            user[3] = String.valueOf(mCursor.getString(2));
            user[4] = String.valueOf(mCursor.getString(3));
            user[5] = String.valueOf(mCursor.getString(4));
            user[6] = String.valueOf(mCursor.getString(5));
            user[7] = String.valueOf(mCursor.getString(6));
            user[8] = String.valueOf(mCursor.getString(7));
            user[9] = String.valueOf(mCursor.getString(8));
            return user;
        } else return null;
    }

    public LinkedList<Unit> getUnitsFromScenario() {
        LinkedList<Unit> topLevel = new LinkedList<>();
        Cursor mCursor = database.query(DbHelper.UNITS_TABLE_NAME, new String[]{"_id, name, gpid"}, "parentUnit IS NULL OR parentUnit=0", null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
            for (int j = 0; j < mCursor.getCount(); j++) {
                Cursor cCursor = database.query(DbHelper.GEOPOINT_TABLE_NAME, new String[]{"lat, lon, app6aCode"}, "_id=" + mCursor.getInt(2), null, null, null, null);
                if (cCursor != null) {
                    cCursor.moveToFirst();
                    topLevel.add(new Unit(mCursor.getString(1), new GeoPoint(cCursor.getDouble(0), cCursor.getDouble(1)), cCursor.getString(2), getSubordinatesFromUnit(mCursor.getInt(0)), mCursor.getInt(0)));
                }
                mCursor.moveToNext();
            }
        }

        for (int i = 0; i < topLevel.size(); i++) {
            proceedSuperior(topLevel.get(i).getSubordinates(), topLevel.get(i));
        }
        return topLevel;
    }

    private void proceedSuperior(LinkedList<Unit> subordinates, Unit superior) {
        if (subordinates == null)
            return;
        for (int i = 0; i < subordinates.size(); i++) {
            subordinates.get(i).setSuperior(superior);
            proceedSuperior(subordinates.get(i).getSubordinates(), subordinates.get(i));
        }
    }

    public LinkedList<Unit> getSubordinatesFromUnit(int uid) {
        LinkedList<Unit> subordinates = new LinkedList<>();
        Cursor mCursor = database.query(DbHelper.UNITS_TABLE_NAME, new String[]{"_id, name, gpid"}, "parentUnit=" + uid, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
            for (int j = 0; j < mCursor.getCount(); j++) {
                Cursor cCursor = database.query(DbHelper.GEOPOINT_TABLE_NAME, new String[]{"lat, lon, app6aCode"}, "_id=" + mCursor.getInt(2), null, null, null, null);
                if (cCursor != null) {
                    cCursor.moveToFirst();
                    subordinates.add(new Unit(mCursor.getString(1), new GeoPoint(cCursor.getDouble(0), cCursor.getDouble(1)), cCursor.getString(2), getSubordinatesFromUnit(mCursor.getInt(0)), mCursor.getInt(0)));
                }
                mCursor.moveToNext();
            }
        }
        return subordinates;
    }

    public LinkedList<String> getUnitNamesFromScenario(String filter) {
        LinkedList<String> units = new LinkedList<>();
        Cursor mCursor;
        if (filter == null)
            mCursor = database.query(DbHelper.UNITS_TABLE_NAME, new String[]{"name"}, null, null, null, null, null);
        else
            mCursor = database.query(DbHelper.UNITS_TABLE_NAME, new String[]{"name"}, "name LIKE '%" + filter + "%'", null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
            for (int j = 0; j < mCursor.getCount(); j++) {
                units.add(mCursor.getString(0));
                mCursor.moveToNext();
            }
        }
        return units;
    }

    public long changeUnitAffiliation(String fromUnit, String toUnit) throws DBException {
        Cursor mCursor = database.query(DbHelper.UNITS_TABLE_NAME, new String[]{"_id"}, "name='" + toUnit + "'", null, null, null, null);
        int toUnitId = -1;
        if (mCursor != null) {
            mCursor.moveToFirst();
            toUnitId = mCursor.getInt(0);
        } else
            throw new DBException("No such unit");
        if (toUnitId == -1)
            throw new DBException("No such unit");
        ContentValues values = new ContentValues();
        values.put("parentUnit", toUnitId);
        return database.update(DbHelper.UNITS_TABLE_NAME, values, "name='" + fromUnit + "'", null);
    }

    public int getNewUnitId() {
        int i = 1;
        Cursor mCursor = database.query(DbHelper.UNITS_TABLE_NAME, new String[]{"_id"}, null, null, null, null, "_id DESC");
        if (mCursor != null) {
            if (mCursor.getCount() == 0)
                return 1;
            mCursor.moveToFirst();
            if (mCursor.getCount() != 0) i = mCursor.getInt(0) + 1;

        }
        return i;
    }

    public Integer getUnitIdByName(String superiorName) {
        Cursor mCursor = database.query(DbHelper.UNITS_TABLE_NAME, new String[]{"_id"}, "name='" + superiorName + "'", null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
            if (mCursor.getCount() > 0)
                return mCursor.getInt(0);
            return null;
        }
        return null;
    }

    public String getUnitCodeByName(String superiorName) {
        Cursor mCursor = database.query(DbHelper.UNITS_TABLE_NAME, new String[]{"gpid"}, "name='" + superiorName + "'", null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
            Cursor xCursor = database.query(DbHelper.GEOPOINT_TABLE_NAME, new String[]{"app6aCode"}, "_id='" + mCursor.getInt(0) + "'", null, null, null, null);
            if (xCursor != null) {
                xCursor.moveToFirst();
                return xCursor.getString(0);
            }
            mCursor.close();
        }
        return null;
    }

    public void addUnit(Unit unit, Integer supid) {
        Cursor mCursor = database.query(DbHelper.GEOPOINT_TABLE_NAME, new String[]{"_id"}, null, null, null, null, "_id DESC");
        int id = 1;
        if (mCursor != null) {
            mCursor.moveToFirst();
            if (mCursor.getCount() > 0)
                id = mCursor.getInt(0) + 1;
        }
        ContentValues gp = new ContentValues();
        gp.put("_id", id);
        gp.put("app6aCode", unit.getApp6aCode());
        gp.put("lat", unit.getLocation().getLatitudeE6() / 1e6);
        gp.put("lon", unit.getLocation().getLongitudeE6() / 1e6);
        database.insert(DbHelper.GEOPOINT_TABLE_NAME, null, gp);

        ContentValues values = new ContentValues();
        if (unit.getId() > 0)
            values.put("_id", unit.getId());
        else
            values.put("_id", this.getNewUnitId());
        values.put("name", unit.getName());
        values.put("parentUnit", supid);
        values.put("gpid", id);
        long i = database.insert(DbHelper.UNITS_TABLE_NAME, null, values);
    }

    public void deleteUnits() {
        database.delete(DbHelper.UNITS_TABLE_NAME, "_id>1", null);
        database.delete(DbHelper.GEOPOINT_TABLE_NAME, "_id>1", null);
    }

    /**
     * erase all units from database
     */
    public void deleteUnitsAll() {
        database.delete(DbHelper.UNITS_TABLE_NAME, null, null);
        database.delete(DbHelper.GEOPOINT_TABLE_NAME, null, null);
    }


    public int updateOrInsertUser(String user, String pass, int pinPass) {
        Cursor mCursor;
        ContentValues values = new ContentValues();
        values.put("name", user);
        values.put("password", pass);
        values.put("pin", pinPass + "");
        int id = -1;
        mCursor = database.query(DbHelper.USER_TABLE_NAME, new String[]{"_id"}, "name='" + user + "'", null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
            if (mCursor.getCount() > 0)
                id = Integer.parseInt(mCursor.getString(0));
        }
        if (id == -1)
            return (int) database.insert(DbHelper.USER_TABLE_NAME, "_id", values);
        else
            database.update(DbHelper.USER_TABLE_NAME, values, "_id=" + id, null);
        return id;
    }

}
