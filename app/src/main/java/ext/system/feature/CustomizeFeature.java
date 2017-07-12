package ext.system.feature;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Locale;

/**
 * Created by Chongyang.Hu on 2017/3/23 0023.
 */

public final class CustomizeFeature {
    public static final String ACTION_SET_LOCALE = "com.yancy.feature.CustomizeFeature.ACTION_SET_LOCALE";
    public static final String ACTION_MASTER_CLEAR = "com.yancy.feature.CustomizeFeature.ACTION_MASTER_CLEAR";
    public static final String ACTION_SET_AUTO_TIME = "com.yancy.feature.CustomizeFeature.ACTION_SET_AUTO_TIME";
    public static final String ACTION_SET_AUTO_TIME_ZONE = "com.yancy.feature.CustomizeFeature.ACTION_SET_AUTO_TIME_ZONE";
    public static final String ACTION_SET_SYSTEM_DATE = "com.yancy.feature.CustomizeFeature.ACTION_SET_SYSTEM_DATE";
    public static final String ACTION_SET_SYSTEM_TIME = "com.yancy.feature.CustomizeFeature.ACTION_SET_SYSTEM_TIME";
    public static final String ACTION_SET_SYSTEM_TIME_ZONE = "com.yancy.feature.CustomizeFeature.ACTION_SET_SYSTEM_TIME_ZONE";
    public static final String ACTION_SET_SYSTEM_TIME_FORMAT = "com.yancy.feature.CustomizeFeature.ACTION_SET_SYSTEM_TIME_FORMAT";

    public static final String EXTRA_LOCALE_VALUE = "LOCALE_VALUE";
    public static final String EXTRA_AUTO_TIME = "EXTRA_AUTO_TIME";
    public static final String EXTRA_AUTO_TIME_ZONE = "EXTRA_AUTO_TIME_ZONE";
    public static final String EXTRA_DATE_YEAR = "EXTRA_DATE_YEAR";
    public static final String EXTRA_DATE_MONTH = "EXTRA_DATE_MONTH";
    public static final String EXTRA_DATE_DAY = "EXTRA_DATE_DAY";
    public static final String EXTRA_TIME_HOUR = "EXTRA_TIME_HOUR";
    public static final String EXTRA_TIME_MINUTE = "EXTRA_TIME_MINUTE";
    public static final String EXTRA_TIME_ZONE_ID = "EXTRA_TIME_ZONE_ID";
    public static final String EXTRA_TIME_FORMAT = "EXTRA_TIME_FORMAT";

    public static void factoryReset(Context ctx) {
        Log.d("TEST", "factoryReset");
        Intent intent = new Intent(CustomizeFeature.ACTION_MASTER_CLEAR);
        ctx.sendBroadcast(intent);
    }

    public static void updateLocale(Context ctx, Locale locale) {
        Log.d("TEST", "updateLocale: " + locale);
        Intent intent = new Intent(CustomizeFeature.ACTION_SET_LOCALE);
        intent.putExtra(CustomizeFeature.EXTRA_LOCALE_VALUE, locale);
        ctx.sendBroadcast(intent);
    }
}
