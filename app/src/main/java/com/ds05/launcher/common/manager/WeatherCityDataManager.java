package com.ds05.launcher.common.manager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chongyang.Hu on 2017/1/14 0014.
 */

public final class WeatherCityDataManager {

    private static final String CITY_DATA_DB = "city_db.db3";
    private Context mContext;
    private AssetsDatabaseManager mManager;
    private SQLiteDatabase mDatabase;

    /* City database constance */
    private static final String TBL_PROVINCE = "t_address_province";
    private static final String TBL_CIYT = "t_address_city";
    private static final String TBL_TOWN = "t_address_town";

    private static final String FIELD_NAME = "name";
    private static final String FIELD_CODE = "code";
    private static final String FIELD_PROVINCE_CODE = "provinceCode";
    private static final String FIELD_CITY_CODE = "cityCode";

    public class City {
        public String name;
        public String code;
    }

    public WeatherCityDataManager(Context ctx) {
        mContext = ctx;
        AssetsDatabaseManager.initManager(ctx);
        mManager = AssetsDatabaseManager.getManager();
        mDatabase = mManager.getDatabase(CITY_DATA_DB);

        if(mDatabase == null) {
            throw new IllegalStateException("Get city database error!");
        }
    }

    public void destroyManager() {
        mDatabase = null;
        mManager.closeDatabase(CITY_DATA_DB);
    }

    public List<City> getAllProvince() {
        Cursor cursor = mDatabase.query(TBL_PROVINCE,
                new String[]{FIELD_CODE, FIELD_NAME},
                null, null, null, null, null);

        if(cursor == null || cursor.getCount() <= 0) return null;

        List<City> ret = new ArrayList<City>();
        cursor.moveToFirst();
        do {
            City city = new City();
            city.code = cursor.getString(0);
            city.name = cursor.getString(1);
            ret.add(city);
        } while (cursor.moveToNext());

        return ret;
    }

    public List<City> getCityByCode(String code) {
        Cursor cursor = mDatabase.query(TBL_CIYT,
                new String[]{FIELD_CODE, FIELD_NAME},
                FIELD_PROVINCE_CODE + "=?", new String[]{code}, null, null, null);

        if(cursor == null || cursor.getCount() <= 0) return null;

        List<City> ret = new ArrayList<City>();
        cursor.moveToFirst();
        do {
            City city = new City();
            city.code = cursor.getString(0);
            city.name = cursor.getString(1);
            ret.add(city);
        } while (cursor.moveToNext());

        return ret;
    }

    public List<City> getTownbyCode(String code) {
        Cursor cursor = mDatabase.query(TBL_TOWN,
                new String[]{FIELD_CODE, FIELD_NAME},
                FIELD_CITY_CODE + "=?", new String[]{code}, null, null, null);

        if(cursor == null || cursor.getCount() <= 0) return null;

        List<City> ret = new ArrayList<City>();
        cursor.moveToFirst();
        do {
            City city = new City();
            city.code = cursor.getString(0);
            city.name = cursor.getString(1);
            ret.add(city);
        } while (cursor.moveToNext());

        return ret;
    }
}
