package icechen1.com.blackbox.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;

import java.io.File;

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

        SwitchPreference themeListPreference = (SwitchPreference)findPreference("always_on_notification");
        themeListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Intent i = new Intent(PreferenceActivity.this, AudioRecordService.class);
                i.putExtra("mode", AudioRecordService.MODE_SET_PASSIVE_NOTIF);
                startService(i);
                return true;
            }
        });

        //-- RESET SERVICE
        SwitchPreference shakePreference = (SwitchPreference)findPreference("should_detect_shake");
        shakePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Intent i = new Intent(PreferenceActivity.this, AudioRecordService.class);
                startService(i);
                return true;
            }
        });

        EditTextPreference pathPreference = (EditTextPreference)findPreference("path");
        final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Rewind/");
        pathPreference.setDialogMessage(getString(R.string.setting_path_help, dir));

    }
}
