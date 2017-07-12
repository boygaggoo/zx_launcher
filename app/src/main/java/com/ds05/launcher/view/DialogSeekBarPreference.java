package com.ds05.launcher.view;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.ds05.launcher.R;

public class DialogSeekBarPreference extends DialogPreference implements
        OnSeekBarChangeListener {

    private Context mContext;
    private SeekBar mSeekBar;
    private TextView mTextView;
    private CheckBox mCheckBox;

    private int mMax;
    private int mProgress;
    private boolean mSeekBarEnable = true;
    private boolean mIsChecked;
    private boolean mIsShowCheckBox;
    private int mCheckBoxStringRes;
    private OnSeekBarChangeListener mOnSeekBarChangeListener;
    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;

    public DialogSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        return super.onCreateView(parent);
    }

    @Override
    protected View onCreateDialogView() {
        return super.onCreateDialogView();
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mSeekBar = (SeekBar) view.findViewById(R.id.id_seekBar);
        mTextView = (TextView) view.findViewById(R.id.id_textView);
        mCheckBox = (CheckBox) view.findViewById(R.id.id_checkbox);

        if(mOnSeekBarChangeListener != null) {
            mSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        } else {
            mSeekBar.setOnSeekBarChangeListener(this);
        }
        mSeekBar.setMax(mMax);
        mSeekBar.setProgress(mProgress);
        mSeekBar.setEnabled(mSeekBarEnable);

        if(mIsShowCheckBox) {
            mCheckBox.setVisibility(View.VISIBLE);
            mCheckBox.setChecked(mIsChecked);
            mCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
            if(mCheckBoxStringRes != 0) {
                mCheckBox.setText(mContext.getString(mCheckBoxStringRes));
            }
        } else {
            mCheckBox.setVisibility(View.GONE);
        }
    }

    public void setMax(int max) {
        mMax = max;
        if(mSeekBar != null) {
            mSeekBar.setMax(max);
        }
    }
    public void setProgress(int progress) {
        mProgress = progress;
        if(mSeekBar != null) {
            mSeekBar.setProgress(progress);
        }
    }
    public int getMax() {
        if(mSeekBar != null) {
            mMax = mSeekBar.getMax();
        }
        return mMax;
    }
    public int getProgress() {
        if(mSeekBar != null) {
            mProgress = mSeekBar.getProgress();
        }
        return mProgress;
    }
    public void enabledProgress(boolean enable) {
        mSeekBarEnable = enable;
        if(mSeekBar != null) {
            mSeekBar.setEnabled(enable);
        }
    }
    public void setProgressChangedListener(OnSeekBarChangeListener l) {
        mOnSeekBarChangeListener = l;
        if(mSeekBar != null) {
            mSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        }
    }
    public void showCheckBox(boolean show) {
        mIsShowCheckBox = show;
        if(mCheckBox != null) {
            if (show) {
                mCheckBox.setVisibility(View.VISIBLE);
            } else {
                mCheckBox.setVisibility(View.GONE);
            }
        }
    }
    public void setChecked(boolean checked) {
        mIsChecked = checked;
        if(mCheckBox != null) {
            mCheckBox.setChecked(checked);
        }
    }
    public boolean isChecked() {
        if(mCheckBox != null) {
            mIsChecked = mCheckBox.isChecked();
        }
        return mIsChecked;
    }
    public void setCheckBoxString(int resId) {
        mCheckBoxStringRes = resId;
        if(mCheckBox != null) {
            mCheckBox.setText(resId);
        }
    }
    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener l) {
        mOnCheckedChangeListener = l;
        if(mCheckBox != null) {
            mCheckBox.setOnCheckedChangeListener(l);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            int curr = mSeekBar.getProgress();
            if(mProgress == curr) return;

            mProgress = curr;
            callChangeListener(curr + "");
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

}
