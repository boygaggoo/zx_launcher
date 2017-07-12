package com.ds05.launcher.ui.settings;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.BidiFormatter;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.ds05.launcher.ModuleBaseFragment;
import com.ds05.launcher.R;
import com.ds05.launcher.common.ZonePicker;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Chongyang.Hu on 2017/1/19 0019.
 */

public class TimeDateSettings extends ModuleBaseFragment
        implements Preference.OnPreferenceChangeListener, Serializable,
        SharedPreferences.OnSharedPreferenceChangeListener,
        TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    public static final String KEY_AUTO_DATE_TIME = "key_auto_date_time";
    public static final String KEY_AUTO_TIME_ZONE = "key_auto_time_zone";
    public static final String KEY_SET_DATE = "key_set_date";
    public static final String KEY_SET_TIME = "key_set_time";
    public static final String KEY_SELECT_TIME_ZONE = "key_select_timezone";
    public static final String KEY_USE_24_HOUR = "key_use_24_hour";

    private static final String HOURS_12 = "12";
    private static final String HOURS_24 = "24";

    // Used for showing the current date format, which looks like "12/31/2010", "2010/12/13", etc.
    // The date value is dummy (independent of actual date).
    private Calendar mDummyDate;

    private static final int DIALOG_DATEPICKER = 0;
    private static final int DIALOG_TIMEPICKER = 1;

    // have we been launched from the setup wizard?
    protected static final String EXTRA_IS_FIRST_RUN = "firstRun";

    private CheckBoxPreference mAutoTimePref;
    private Preference mTimePref;
    private Preference mTime24Pref;
    private CheckBoxPreference mAutoTimeZonePref;
    private Preference mTimeZone;
    private Preference mDatePref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.date_time_settings);

        initUI();

        findPreference(KEY_AUTO_DATE_TIME).setOnPreferenceChangeListener(this);
        findPreference(KEY_AUTO_TIME_ZONE).setOnPreferenceChangeListener(this);
        findPreference(KEY_SET_DATE).setOnPreferenceChangeListener(this);
        findPreference(KEY_SET_TIME).setOnPreferenceChangeListener(this);
        findPreference(KEY_SELECT_TIME_ZONE).setOnPreferenceChangeListener(this);
        findPreference(KEY_USE_24_HOUR).setOnPreferenceChangeListener(this);
    }

    private void initUI() {
        boolean autoTimeEnabled = getAutoState(Settings.Global.AUTO_TIME);
        boolean autoTimeZoneEnabled = getAutoState(Settings.Global.AUTO_TIME_ZONE);

        mAutoTimePref = (CheckBoxPreference) findPreference(KEY_AUTO_DATE_TIME);

        mDummyDate = Calendar.getInstance();

        mAutoTimePref.setChecked(autoTimeEnabled);
        mAutoTimeZonePref = (CheckBoxPreference) findPreference(KEY_AUTO_TIME_ZONE);
        mAutoTimeZonePref.setChecked(autoTimeZoneEnabled);

        mTimePref = findPreference(KEY_SET_TIME);
        mTime24Pref = findPreference(KEY_USE_24_HOUR);
        mTimeZone = findPreference(KEY_SELECT_TIME_ZONE);
        mDatePref = findPreference(KEY_SET_DATE);

        // Prevents duplicated values on date format selector.
        mDummyDate.set(mDummyDate.get(Calendar.YEAR), mDummyDate.DECEMBER, 31, 13, 0, 0);

//        mTimePref.setEnabled(!autoTimeEnabled);
//        mDatePref.setEnabled(!autoTimeEnabled);
//        mTimeZone.setEnabled(!autoTimeZoneEnabled);
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.settings_str_date_time);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        ((CheckBoxPreference)mTime24Pref).setChecked(is24Hour());

        // Register for time ticks and other reasons for time change
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        getActivity().registerReceiver(mIntentReceiver, filter, null, null);

        updateTimeAndDateDisplay(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mIntentReceiver);
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void updateTimeAndDateDisplay(Context context) {
        java.text.DateFormat shortDateFormat = DateFormat.getDateFormat(context);
        final Calendar now = Calendar.getInstance();
        mDummyDate.setTimeZone(now.getTimeZone());
        // We use December 31st because it's unambiguous when demonstrating the date format.
        // We use 13:00 so we can demonstrate the 12/24 hour options.
        mDummyDate.set(now.get(Calendar.YEAR), 11, 31, 13, 0, 0);
        Date dummyDate = mDummyDate.getTime();
        mTimePref.setSummary(DateFormat.getTimeFormat(getActivity()).format(now.getTime()));
        mTimeZone.setSummary(getTimeZoneText(now.getTimeZone(), true));
        mDatePref.setSummary(shortDateFormat.format(now.getTime()));
        mTime24Pref.setSummary(DateFormat.getTimeFormat(getActivity()).format(dummyDate));
    }

    public static String getTimeZoneText(TimeZone tz, boolean includeName) {
        Date now = new Date();

        // Use SimpleDateFormat to format the GMT+00:00 string.
        SimpleDateFormat gmtFormatter = new SimpleDateFormat("ZZZZ");
        gmtFormatter.setTimeZone(tz);
        String gmtString = gmtFormatter.format(now);

        // Ensure that the "GMT+" stays with the "00:00" even if the digits are RTL.
        BidiFormatter bidiFormatter = BidiFormatter.getInstance();
        Locale l = Locale.getDefault();
        boolean isRtl = TextUtils.getLayoutDirectionFromLocale(l) == View.LAYOUT_DIRECTION_RTL;
        gmtString = bidiFormatter.unicodeWrap(gmtString,
                isRtl ? TextDirectionHeuristics.RTL : TextDirectionHeuristics.LTR);

        if (!includeName) {
            return gmtString;
        }

        // Optionally append the time zone name.
        SimpleDateFormat zoneNameFormatter = new SimpleDateFormat("zzzz");
        zoneNameFormatter.setTimeZone(tz);
        String zoneNameString = zoneNameFormatter.format(now);

        // We don't use punctuation here to avoid having to worry about localizing that too!
        return gmtString + " " + zoneNameString;
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Activity activity = getActivity();
            if (activity != null) {
                updateTimeAndDateDisplay(activity);
            }
        }
    };

    private boolean is24Hour() {
        return DateFormat.is24HourFormat(getActivity());
    }

    private boolean getAutoState(String name) {
        try {
            return Settings.Global.getInt(getActivity().getContentResolver(), name) > 0;
        } catch (Settings.SettingNotFoundException snfe) {
            return false;
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if(key.equals(KEY_SELECT_TIME_ZONE)) {
            Fragment zoneFrag = new ZonePicker();
            Bundle bundle = new Bundle();
            bundle.putSerializable("ModuleBaseFragment", this);
            zoneFrag.setArguments(bundle);
            jumpToFragment(zoneFrag);
            return true;
        }

        if (preference == mDatePref) {
//            showDialog(DIALOG_DATEPICKER);
            final Calendar calendar = Calendar.getInstance();
            DatePickerDialog d = new DatePickerDialog(
                    getActivity(),
                    this,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            configureDatePicker(d.getDatePicker());
            d.show();
        } else if (preference == mTimePref) {
            // The 24-hour mode may have changed, so recreate the dialog
//            removeDialog(DIALOG_TIMEPICKER);
//            showDialog(DIALOG_TIMEPICKER);
            final Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(
                    getActivity(),
                    this,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(getActivity())).show();
        } else if (preference == mTime24Pref) {
            final boolean is24Hour = ((CheckBoxPreference)mTime24Pref).isChecked();
            set24Hour(is24Hour);
            updateTimeAndDateDisplay(getActivity());
            timeUpdated(is24Hour);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        updateTimeAndDateDisplay(getActivity());
    }

    private void timeUpdated(boolean is24Hour) {
//        Intent timeChanged = new Intent(Intent.ACTION_TIME_CHANGED);
//        timeChanged.putExtra(Intent.EXTRA_TIME_PREF_24_HOUR_FORMAT, is24Hour);
//        getActivity().sendBroadcast(timeChanged);
        //TODO
    }

    private void set24Hour(boolean is24Hour) {
//        Settings.System.putString(getContentResolver(),
//                Settings.System.TIME_12_24,
//                is24Hour? HOURS_24 : HOURS_12);
        //TODO
    }
    private String getDateFormat() {
        return Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.DATE_FORMAT);
    }
    /* package */ static void setDate(Context context, int year, int month, int day) {
        //TODO
    }

    /* package */ static void setTime(Context context, int hourOfDay, int minute) {
        //TODO
    }

    static void configureDatePicker(DatePicker datePicker) {
        // The system clock can't represent dates outside this range.
        Calendar t = Calendar.getInstance();
        t.clear();
        t.set(1970, Calendar.JANUARY, 1);
        datePicker.setMinDate(t.getTimeInMillis());
        t.clear();
        t.set(2037, Calendar.DECEMBER, 31);
        datePicker.setMaxDate(t.getTimeInMillis());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        if (key.equals(KEY_AUTO_DATE_TIME)) {
//            boolean autoEnabled = sharedPreferences.getBoolean(key, true);
//            Settings.Global.putInt(getActivity().getContentResolver(), Settings.Global.AUTO_TIME,
//                    autoEnabled ? 1 : 0);
//            mTimePref.setEnabled(!autoEnabled);
//            mDatePref.setEnabled(!autoEnabled);
//        } else if (key.equals(KEY_AUTO_TIME_ZONE)) {
//            boolean autoZoneEnabled = sharedPreferences.getBoolean(key, true);
//            Settings.Global.putInt(
//                    getActivity().getContentResolver(),
//                    Settings.Global.AUTO_TIME_ZONE, autoZoneEnabled ? 1 : 0);
//            mTimeZone.setEnabled(!autoZoneEnabled);
//        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        final Activity activity = getActivity();
        if (activity != null) {
            setDate(activity, year, monthOfYear, dayOfMonth);
            updateTimeAndDateDisplay(activity);
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        final Activity activity = getActivity();
        if (activity != null) {
            setTime(activity, hourOfDay, minute);
            updateTimeAndDateDisplay(activity);
        }
    }
}
