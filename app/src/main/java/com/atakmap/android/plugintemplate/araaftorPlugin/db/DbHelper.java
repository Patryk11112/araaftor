package com.atakmap.android.plugintemplate.araaftorPlugin.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SAME";

    private static final int DATABASE_VERSION = 18;

    static final String WMS_TABLE_NAME = "WMS";
    static final String SETTINGS_TABLE_NAME = "Settings";
    static final String USER_TABLE_NAME = "Users";
    static final String WEB_SERVICES_TABLE_NAME = "WebServices";
    static final String WEB_SERVICES_ATTRIBUTES_TABLE_NAME = "WebServicesAttribs";
    static final String UNITS_TABLE_NAME = "Units";
    static final String GEOPOINT_TABLE_NAME = "GeoPoints";
    static final String FILTERS_TABLE_NAME = "Filters";
    private static final String PREFERENCES_TABLE_NAME = "Preferences";

    private static final String DATABASE_CREATE_1 = "create table " + WMS_TABLE_NAME + " (_id integer primary key, name text not null, url text not null, layers text not null, wmsmap text);";
    private static final String DATABASE_CREATE_2 = "create table " + SETTINGS_TABLE_NAME + " (setting text, value text not null, uid integer not null);";
    private static final String DATABASE_CREATE_3 = "create table " + USER_TABLE_NAME + " (_id integer primary key autoincrement, name text not null, password text not null, firstName text, surname text, rank text, function text, pin integer, country text, rankShort text, admin integer);";
    private static final String DATABASE_CREATE_4 = "create table " + WEB_SERVICES_TABLE_NAME + " (_id integer primary key, name text not null, namespace text not null, url text not null, action text not null, method text not null);";
    private static final String DATABASE_CREATE_5 = "create table " + WEB_SERVICES_ATTRIBUTES_TABLE_NAME + " (_id integer primary key, name text not null, wsid integer not null);";
    private static final String DATABASE_CREATE_6 = "create table " + UNITS_TABLE_NAME + " (_id integer primary key, name text not null, parentUnit integer, gpid integer not null);";
    private static final String DATABASE_CREATE_7 = "create table " + GEOPOINT_TABLE_NAME + " (_id integer primary key, app6aCode text not null, lat real not null, lon real not null);";
    private static final String DATABASE_CREATE_8 = "create table " + FILTERS_TABLE_NAME + " (_id integer primary key, name text not null , filter text not null,visible text not null, transparency text not null, signfilter integer not null);";
    private static final String DATABASE_CREATE_15 = "create table " + PREFERENCES_TABLE_NAME + " (_id integer primary key autoincrement, name text not null, description text)";

    private static final String[] DATABASE_INSERT_SET = {
            "insert into " + WMS_TABLE_NAME + " values (1, 'OSM ALL', 'http://129.206.228.72/cached/osm', 'osm_auto:all', '');"
            , "insert into " + WMS_TABLE_NAME + " values (2, 'Hackaton', 'http://192.168.20.83:6080/arcgis/services/World_Map/MapServer/WmsServer', '1', '');"
            , "insert into " + WMS_TABLE_NAME + " values (3, 'Hackaton - Brienfing', 'http://192.168.20.83:6080/arcgis/services/Brienfing_Map/MapServer/WmsServer', '0', '');"
            , "insert into " + WMS_TABLE_NAME + " values (4, 'PCN - Italy', 'http://wms.pcn.minambiente.it/ogc', 'OI.ORTOIMMAGINI.2012', '/ms_ogc/WMS_v1.3/raster/ortofoto_colore_12.map');"

            , "insert into " + SETTINGS_TABLE_NAME + " values('WMS', '4', 0)"
            , "insert into " + SETTINGS_TABLE_NAME + " values('WMS', '4', 1)"
            , "insert into " + SETTINGS_TABLE_NAME + " values('WMS', '4', 2)"
            , "insert into " + SETTINGS_TABLE_NAME + " values('WMS', '4', 3)"
            , "insert into " + SETTINGS_TABLE_NAME + " values('WMS', '4', 4)"
            , "insert into " + SETTINGS_TABLE_NAME + " values('FirstLogin', '1', 1)"
            , "insert into " + SETTINGS_TABLE_NAME + " values('SelectedUser', '2', 1)"
            , "insert into " + SETTINGS_TABLE_NAME + " values('ButtonSize', '1.0', 1)"
            , "insert into " + USER_TABLE_NAME + " values(1, 'mkukielka', 'asd', 'Marcin', 'Kukielka', 'Lt.', 'Patrol Commander', 12345, 'PL', 'Lt.',1)"
            , "insert into " + USER_TABLE_NAME + " values(2, 'ppieczonka', 'asd', 'Pawel', 'Pieczonka', 'Sgt.', 'Patrol Commander', 12345, 'PL', 'Sgt.',0)"
            , "insert into " + USER_TABLE_NAME + " values(3, 'tgutowski', 'asd', 'Tomasz', 'Gutowski', 'Sgt', 'Patrol Commander', 12345, 'PL', 'Sgt',0)"
            , "insert into " + USER_TABLE_NAME + " values(4, 'mmotysek', 'asd', 'Mikolaj', 'Motysek', 'Crp.', 'Patrol Commander', 12345, 'PL', 'Crp.',0)"
            , "insert into " + SETTINGS_TABLE_NAME + " values('FriendlyOverlay', '1', 1)"
            , "insert into " + SETTINGS_TABLE_NAME + " values('HostileOverlay', '1', 1)"
            , "insert into " + SETTINGS_TABLE_NAME + " values('NeutralOverlay', '1', 1)"
            , "insert into " + SETTINGS_TABLE_NAME + " values('DefaultUser', '1', 1)"
            , "insert into " + WEB_SERVICES_TABLE_NAME + " values(1, 'logIn', 'http://webservices/', 'http://##Server##/UserWebService/UserWebService', 'http://webservices/login', 'login')"
            , "insert into " + WEB_SERVICES_ATTRIBUTES_TABLE_NAME + " values(1, 'username', 1)"
            , "insert into " + WEB_SERVICES_ATTRIBUTES_TABLE_NAME + " values(2, 'password', 1)"
            , "insert into " + WEB_SERVICES_TABLE_NAME + " values(2, 'setEmptyScenario', 'http://db.tcop.wcy.wat.edu.pl/', 'http://##Server##/DatabaseWSService/DatabaseWS', 'http://db.tcop.wcy.wat.edu.pl/setEmptyScenario', 'setEmptyScenario')"
            , "insert into " + WEB_SERVICES_ATTRIBUTES_TABLE_NAME + " values(3, 'reportName', 2)"
            , "insert into " + WEB_SERVICES_ATTRIBUTES_TABLE_NAME + " values(4, 'fullName', 2)"
            , "insert into " + WEB_SERVICES_TABLE_NAME + " values(3, 'getListOfScenario', 'http://db.tcop.wcy.wat.edu.pl/', 'http://##Server##/DatabaseWSService/DatabaseWS', 'http://db.tcop.wcy.wat.edu.pl/getListOfScenario', 'getListOfScenario')"
            , "insert into " + WEB_SERVICES_TABLE_NAME + " values(4, 'getSymbol', 'http://tcop.wcy.wat.edu.pl/', 'http://##Server##/UnitMarkAPP6AService/UnitMarkAPP6A', 'http://tcop.wcy.wat.edu.pl/getUnitMarkAPP6A', 'getUnitMarkAPP6A')"
            , "insert into " + WEB_SERVICES_ATTRIBUTES_TABLE_NAME + " values(5, 'sign', 4)"
            , "insert into " + WEB_SERVICES_ATTRIBUTES_TABLE_NAME + " values(6, 'width', 4)"
            , "insert into " + WEB_SERVICES_ATTRIBUTES_TABLE_NAME + " values(7, 'height', 4)"
            , "insert into " + WEB_SERVICES_ATTRIBUTES_TABLE_NAME + " values(8, 'token', 4)"
            , "insert into " + WEB_SERVICES_TABLE_NAME + " values(5, 'getScenarioById', 'http://db.tcop.wcy.wat.edu.pl/', 'http://##Server##/DatabaseWSService/DatabaseWS', 'http://db.tcop.wcy.wat.edu.pl/getScenarioById', 'getScenarioById')"
            , "insert into " + WEB_SERVICES_ATTRIBUTES_TABLE_NAME + " values(9, 'id', 5)"
            , "insert into " + WEB_SERVICES_TABLE_NAME + " values(6, 'getGeoObjects', 'http://db.tcop.wcy.wat.edu.pl/', 'http://##Server##/DatabaseWSService/DatabaseWS', 'http://db.tcop.wcy.wat.edu.pl/getGeoObjects', 'getGeoObjects')"
            , "insert into " + WEB_SERVICES_ATTRIBUTES_TABLE_NAME + " values(10, 'id', 6)"
            , "insert into " + WEB_SERVICES_TABLE_NAME + " values(7, 'getListOfTemplate', 'http://template.normalBeans.eacop.org/', 'http://##Server##/TemplateService/Template', 'http://template.normalBeans.eacop.org/getListOfTemplate', 'getListOfTemplate')"
            , "insert into " + WEB_SERVICES_TABLE_NAME + " values(8, 'getUnitTemplateEquipment', 'http://template.normalBeans.eacop.org/', 'http://##Server##/TemplateService/Template', 'http://template.normalBeans.eacop.org/getUnitTemplateEquipment', 'getUnitTemplateEquipment')"
            , "insert into " + WEB_SERVICES_ATTRIBUTES_TABLE_NAME + " values(11, 'id', 8)"
            , "insert into " + WEB_SERVICES_TABLE_NAME + " values(9, 'getUnitTemplateSupplies', 'http://template.normalBeans.eacop.org/', 'http://##Server##/TemplateService/Template', 'http://template.normalBeans.eacop.org/getUnitTemplateSupplies', 'getUnitTemplateSupplies')"
            , "insert into " + WEB_SERVICES_ATTRIBUTES_TABLE_NAME + " values(12, 'id', 9)"
            , "insert into " + WEB_SERVICES_TABLE_NAME + " values(10, 'getTasks', 'http://db.tcop.wcy.wat.edu.pl/', 'http://##Server##/DatabaseWSService/DatabaseWS', 'http://db.tcop.wcy.wat.edu.pl/getTasks', 'getTasks')"
            , "insert into " + WEB_SERVICES_ATTRIBUTES_TABLE_NAME + " values(13, 'id', 10)"
            , "insert into " + WEB_SERVICES_TABLE_NAME + " values(11, 'getNFFIServiceDataMCop', 'http://nffi.tcop.wcy.wat.edu.pl/', 'http://##Server##/NFFIServiceClientService/NFFIServiceClient', 'http://nffi.tcop.wcy.wat.edu.pl/getNFFIServiceDataMCop', 'getNFFIServiceDataMCop')"
            , "insert into " + WEB_SERVICES_TABLE_NAME + " values(12, 'getTSOServiceDataMCop', 'http://nffi.tcop.wcy.wat.edu.pl/', 'http://##Server##/NFFIServiceClientService/NFFIServiceClient', 'http://nffi.tcop.wcy.wat.edu.pl/getTSOServiceDataMCop', 'getTSOServiceDataMCop')"
            , "insert into " + WEB_SERVICES_ATTRIBUTES_TABLE_NAME + " values(14, 'arg0', 12)"
            , "insert into " + WEB_SERVICES_TABLE_NAME + " values(13, 'getTsoSymbol', 'http://tcop.wcy.wat.edu.pl/', 'http://##Server##/UnitMarkAPP6AService/UnitMarkAPP6A', 'http://tcop.wcy.wat.edu.pl/getTsoSymbol', 'getTsoSymbol')"
            , "insert into " + WEB_SERVICES_ATTRIBUTES_TABLE_NAME + " values(15, 'arg0', 13)"
            , "insert into " + WEB_SERVICES_TABLE_NAME + " values(14, 'getMergeNffiTsoMcop', 'http://nffi.tcop.wcy.wat.edu.pl/', 'http://##Server##/NFFIServiceClientService/NFFIServiceClient', 'http://nffi.tcop.wcy.wat.edu.pl/getMergeNffiTsoMcop', 'getMergeNffiTsoMcop')"
            , "insert into " + WEB_SERVICES_ATTRIBUTES_TABLE_NAME + " values(16, 'arg0', 14)"
            , "insert into " + WEB_SERVICES_TABLE_NAME + " values(15, 'getRankNato', 'http://tcop.wcy.wat.edu.pl/', 'http://##Server##/UnitMarkAPP6AService/UnitMarkAPP6A', 'http://tcop.wcy.wat.edu.pl/getRankNato', 'getRankNato')"
            , "insert into " + WEB_SERVICES_ATTRIBUTES_TABLE_NAME + " values(17, 'rank', 15)"
            , "insert into " + WEB_SERVICES_ATTRIBUTES_TABLE_NAME + " values(18, 'country', 15)"
            , "insert into " + WEB_SERVICES_TABLE_NAME + " values(16, 'getShortUserData', 'http://webservices/', 'http://##Server##/UserWebService/UserWebService', 'http://webservices/getShortUserData', 'getShortUserData')"
            , "insert into " + WEB_SERVICES_ATTRIBUTES_TABLE_NAME + " values(19, 'username', 16)"
            , "insert into " + WEB_SERVICES_TABLE_NAME + " values(17, 'queryDummyData', 'http://eunec.wat.wcy.edu.pl/', 'http://##Server##/KBQueryBeanService/KBQueryBean', 'http://eunec.wat.wcy.edu.pl/queryDummyData', 'queryDummyData')"

            , "insert into " + FILTERS_TABLE_NAME + " values(1,'Friendly', '-f-------------','true', '255', 1)"
            , "insert into " + FILTERS_TABLE_NAME + " values(2,'Hostile', '-h-------------','true', '255', 1)"
            , "insert into " + FILTERS_TABLE_NAME + " values(3,'Neutral', '-n-------------','true', '255', 1)"
            , "insert into " + FILTERS_TABLE_NAME + " values(4,'Unknown', '-u-------------','true', '255', 1)"
            , "insert into " + FILTERS_TABLE_NAME + " values(5,'Area Signs', '','true', '255', 0)"
            , "insert into " + FILTERS_TABLE_NAME + " values(6,'Weather', '','true', '255', 0)"
            , "insert into " + FILTERS_TABLE_NAME + " values(7,'CCTV', '','true', '255', 0)"
            , "insert into " + FILTERS_TABLE_NAME + " values(8,'Civilians', '','true', '255', 0)"
            , "insert into " + FILTERS_TABLE_NAME + " values(9,'ATAK', '','true', '255', 0)"
            , "insert into " + FILTERS_TABLE_NAME + " values(10,'Traffic', '','true', '255', 0)"

    };

    public static final String UNIT_TEMPLATES_TABLE_NAME = "UnitTemplates";

    private static final List<String> UNIT_TEMPLATES_VALUES = new ArrayList<>();

    static {
        UNIT_TEMPLATES_VALUES.add("1, 'motorized infantry battalion (KTO)', '\"W\" 763', NULL");
        UNIT_TEMPLATES_VALUES.add("2, 'motorized infantry company (KTO)', '\"W\" 118', NULL");
        UNIT_TEMPLATES_VALUES.add("3, 'motorized infantry platoon (KTO)', '\"W\" 34', NULL");
        UNIT_TEMPLATES_VALUES.add("4, 'mechanized battalion (BWP-1)', '\"W\" 761', NULL");
        UNIT_TEMPLATES_VALUES.add("5, 'mechanized company (BWP-1)', '\"W\" 118', NULL");
        UNIT_TEMPLATES_VALUES.add("6, 'mechanized platoon (BWP-1)', '\"W\" 34', NULL");
        UNIT_TEMPLATES_VALUES.add("7, 'tank battalion (PT-91)', '\"W\" 373', NULL");
        UNIT_TEMPLATES_VALUES.add("8, 'tank company (PT-91/T-72)', '\"W\" 52', NULL");
        UNIT_TEMPLATES_VALUES.add("9, 'tank platoon (PT-91/T-72)', '\"W\" 12', NULL");
    }

    private static final List<String> EQUIPMENT_DEFINITIONS_VALUES = new ArrayList<>();

    static {
        EQUIPMENT_DEFINITIONS_VALUES.add("1, 'Amphibious Wheeled Armoured Personnel Carrier Wolverine (KTO Rosomak)', 0.92, 'Armour'");
        EQUIPMENT_DEFINITIONS_VALUES.add("2, 'UKM-2000 ', 0.4, 'Infantry'");
        EQUIPMENT_DEFINITIONS_VALUES.add("3, '7.62×54mmR General Purpose Machine Gun (PK)', 0.3, 'Infantry'");
        EQUIPMENT_DEFINITIONS_VALUES.add("4, '7.62×54 mmR Sniper Rifle (SWD)', 0.5, 'Infantry'");
        EQUIPMENT_DEFINITIONS_VALUES.add("5, '.50 BMG Anti-materiel Rifle (wkw wilk)', 0.55, 'Infantry'");
        EQUIPMENT_DEFINITIONS_VALUES.add("6, '40 mm Automatic Grenade Launcher (mk-19)', 4, 'Infantry'");
        EQUIPMENT_DEFINITIONS_VALUES.add("7, '40 mm Rocket-propelled grenade launcher (RPG-7)', 0.12, 'Infantry'");
        EQUIPMENT_DEFINITIONS_VALUES.add("8, '60mm Mortar ', 0.56, 'Artillery'");
        EQUIPMENT_DEFINITIONS_VALUES.add("9, '98mm Mortar', 0.58, 'Artillery'");
        EQUIPMENT_DEFINITIONS_VALUES.add("10, 'Anti-Tank Guided Missile System (PPK SPIKE)', 0.75, 'Artillery'");
        EQUIPMENT_DEFINITIONS_VALUES.add("11, 'Combat Reconnaissance/Patrol Vehicle (BRDM-2M96ik Szakal)', 0.45, 'Armour'");
        EQUIPMENT_DEFINITIONS_VALUES.add("12, 'Wheeled all-terrain command vehicle (ZWD-3)', 0, ''");
        EQUIPMENT_DEFINITIONS_VALUES.add("13, 'Tank (PT-91/T-72)', 2.35, 'Armour'");
        EQUIPMENT_DEFINITIONS_VALUES.add("14, 'Amphibious Tracked Infantry reconnaissance Vehicle (BWR-1D)', 0.4, 'Armour'");
        EQUIPMENT_DEFINITIONS_VALUES.add("15, 'Amphibious Tracked Infantry Fighting Vehicle (BWP-1)', 0.8, 'Armour'");
        EQUIPMENT_DEFINITIONS_VALUES.add("16, '120 mm Mortar ', 0.97, 'Artillery'");
        EQUIPMENT_DEFINITIONS_VALUES.add("17, 'Tank Destroyer (9P133 Malyutka)', 1, 'Infantry'");
        EQUIPMENT_DEFINITIONS_VALUES.add("18, '9 mm Makarov Semi-Auto Pistol', 0.08, 'Infantry'");
        EQUIPMENT_DEFINITIONS_VALUES.add("19, '9 mm Para Submachine Gun', 0.1, 'Infantry'");
    }


    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE_1);
        database.execSQL(DATABASE_CREATE_2);
        database.execSQL(DATABASE_CREATE_3);
        database.execSQL(DATABASE_CREATE_4);
        database.execSQL(DATABASE_CREATE_5);
        database.execSQL(DATABASE_CREATE_6);
        database.execSQL(DATABASE_CREATE_7);
        database.execSQL(DATABASE_CREATE_8);
        database.execSQL(DATABASE_CREATE_15);
        for (String aDATABASE_INSERT_SET : DATABASE_INSERT_SET) {
            database.execSQL(aDATABASE_INSERT_SET);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

        database.execSQL("DROP TABLE IF EXISTS " + WMS_TABLE_NAME);
        database.execSQL("DROP TABLE IF EXISTS " + SETTINGS_TABLE_NAME);
        database.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_NAME);
        database.execSQL("DROP TABLE IF EXISTS " + UNITS_TABLE_NAME);
        database.execSQL("DROP TABLE IF EXISTS " + GEOPOINT_TABLE_NAME);
        database.execSQL("DROP TABLE IF EXISTS " + WEB_SERVICES_ATTRIBUTES_TABLE_NAME);
        database.execSQL("DROP TABLE IF EXISTS " + WEB_SERVICES_TABLE_NAME);
        database.execSQL("DROP TABLE IF EXISTS " + FILTERS_TABLE_NAME);
        database.execSQL("DROP TABLE IF EXISTS " + PREFERENCES_TABLE_NAME);


        onCreate(database);
    }


}