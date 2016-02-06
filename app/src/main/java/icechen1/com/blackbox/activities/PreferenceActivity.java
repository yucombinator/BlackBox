package icechen1.com.blackbox.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;

import icechen1.com.blackbox.R;
import icechen1.com.blackbox.services.AudioRecordService;

public class PreferenceActivity extends com.lb.material_preferences_library.PreferenceActivity {

    @Override
    protected int getPreferencesXmlId() {
        return R.xml.preferences;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        CheckBoxPreference themeListPreference = (CheckBoxPreference)findPreference("always_on_notification");
        themeListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Intent i = new Intent(PreferenceActivity.this, AudioRecordService.class);
                i.putExtra("mode", AudioRecordService.MODE_SET_PASSIVE_NOTIF);
                startService(i);
                return true;
            }
        });
    }
}
