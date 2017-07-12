package com.ds05.launcher.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;

/**
 * Created by Chongyang.Hu on 2017/1/22 0022.
 */

public class ListPreferenceExt extends ListPreference {
    public interface OnListItemClickListener {
        void onItemClick(int index);
    }

    private int mCurrIndex;

    private OnListItemClickListener mItemClickListener;

    public ListPreferenceExt(Context context, AttributeSet attrs) {
        super(context, attrs);

        mCurrIndex = getValueIndex();
    }

    public ListPreferenceExt(Context context) {
        this(context, null);
    }

    public void setOnListItemClickListener(OnListItemClickListener l) {
        mItemClickListener = l;
    }

    private int getValueIndex() {
        return findIndexOfValue(getValue());
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        builder.setSingleChoiceItems(getEntries(), getValueIndex(),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(mItemClickListener != null) {
                            mItemClickListener.onItemClick(which);
                        }
                        mCurrIndex = which;
                    }
                });
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ListPreferenceExt.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ListPreferenceExt.this.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
            }
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if(mCurrIndex == getValueIndex()) return;

        if (positiveResult) {
            String value = getEntryValues()[mCurrIndex].toString();
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
    }
}
