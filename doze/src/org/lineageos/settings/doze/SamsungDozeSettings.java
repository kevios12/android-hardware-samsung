package org.lineageos.settings.doze;

import android.app.ActionBar;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;


public class SamsungDozeSettings extends PreferenceFragment
        implements CompoundButton.OnCheckedChangeListener, Preference.OnPreferenceChangeListener {

    private TextView mTextView;
    private View mSwitchBar;

    private SwitchPreference mAlwaysOnDisplayPreference;
    private SwitchPreference mHandwavePreference;
    private SwitchPreference mPocketPreference;
    private SwitchPreference mWakeOnGesturePreference;

    private Handler mHandler = new Handler();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.gesture_panel);
        final ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        boolean dozeEnabled = Utils.isDozeEnabled(getActivity());

        mAlwaysOnDisplayPreference = findPreference(Utils.ALWAYS_ON_DISPLAY);
        mAlwaysOnDisplayPreference.setEnabled(dozeEnabled);
        mAlwaysOnDisplayPreference.setChecked(Utils.isAlwaysOnEnabled(getActivity()));
        mAlwaysOnDisplayPreference.setOnPreferenceChangeListener(this);

        mHandwavePreference = findPreference(Utils.GESTURE_HAND_WAVE_KEY);
        mHandwavePreference.setEnabled(dozeEnabled);
        mHandwavePreference.setOnPreferenceChangeListener(this);

        mPocketPreference = findPreference(Utils.GESTURE_POCKET_KEY);
        mPocketPreference.setEnabled(dozeEnabled);
        mPocketPreference.setOnPreferenceChangeListener(this);

        mWakeOnGesturePreference = (SwitchPreference) findPreference(Utils.WAKE_ON_GESTURE_KEY);
        mWakeOnGesturePreference.setEnabled(dozeEnabled);
        mWakeOnGesturePreference.setOnPreferenceChangeListener(this);

        // Hide AOD if not supported and set all its dependents otherwise
        if (!Utils.alwaysOnDisplayAvailable(getActivity())) {
            getPreferenceScreen().removePreference(mAlwaysOnDisplayPreference);
        } else {
            mHandwavePreference.setDependency(Utils.ALWAYS_ON_DISPLAY);
            mPocketPreference.setDependency(Utils.ALWAYS_ON_DISPLAY);
            mWakeOnGesturePreference.setDependency(Utils.ALWAYS_ON_DISPLAY);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view =
                LayoutInflater.from(getContext()).inflate(R.layout.doze, container, false);
        ((ViewGroup) view).addView(super.onCreateView(inflater, container, savedInstanceState));
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        boolean dozeEnabled = Utils.isDozeEnabled(getActivity());

        mTextView = view.findViewById(R.id.switch_text);
        mTextView.setText(getString(dozeEnabled ?
                R.string.switch_bar_on : R.string.switch_bar_off));

        mSwitchBar = view.findViewById(R.id.switch_bar);
        Switch switchWidget = mSwitchBar.findViewById(android.R.id.switch_widget);
        switchWidget.setChecked(dozeEnabled);
        switchWidget.setOnCheckedChangeListener(this);
        mSwitchBar.setActivated(dozeEnabled);
        mSwitchBar.setOnClickListener(v -> {
            switchWidget.setChecked(!switchWidget.isChecked());
            mSwitchBar.setActivated(switchWidget.isChecked());
        });
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (Utils.ALWAYS_ON_DISPLAY.equals(preference.getKey())) {
            Utils.enableAlwaysOn(getActivity(), (Boolean) newValue);
        }

        mHandler.post(() -> Utils.checkDozeService(getActivity()));

        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        Utils.enableDoze(getActivity(), isChecked);
        Utils.checkDozeService(getActivity());

        mTextView.setText(getString(isChecked ? R.string.switch_bar_on : R.string.switch_bar_off));
        mSwitchBar.setActivated(isChecked);

        if (!isChecked) {
            Utils.enableAlwaysOn(getActivity(), false);
            mAlwaysOnDisplayPreference.setChecked(false);
        }
        mAlwaysOnDisplayPreference.setEnabled(isChecked);

        mHandwavePreference.setEnabled(isChecked);
        mPocketPreference.setEnabled(isChecked);
        mWakeOnGesturePreference.setEnabled(isChecked);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return false;
    }
}
