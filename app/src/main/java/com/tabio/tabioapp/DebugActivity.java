package com.tabio.tabioapp;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.tabio.tabioapp.coordinate.CoordinatesBaseActivity;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.preference.LanguageSettingsActivity;
import com.tabio.tabioapp.util.DateUtils;
import com.tabio.tabioapp.util.GpsUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class DebugActivity extends PreferenceActivity {
    public static final String TAG = makeLogTag(DebugActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, DebugFragment.newInstance())
                .commit();
    }

    public static class DebugFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        public DebugFragment() {
        }

        public static DebugFragment newInstance() {
            DebugFragment fragment = new DebugFragment();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            updateView();
            TokenBus.get().register(this);
        }

        private void updateView() {
            final Me self = AppController.getInstance().getSelf(false);

            final SharedPreferences prefs = getPreferenceManager().getSharedPreferences();

            {
                final String key = getString(R.string.prefAppUrl);
                Preference preference = (Preference) findPreference(key);
                preference.setSummary(BuildConfig.BASE_URL);
            }

            {
                final String key = getString(R.string.prefDecideUrl);
                Preference preference = (Preference) findPreference(key);
                preference.setSummary(BuildConfig.DECIDE_URL + BuildConfig.DECIDE_VERSION);
            }

            {
                final String key = getString(R.string.prefTabioId);
                Preference preference = (Preference) findPreference(key);
                preference.setSummary(self.getTabioId());
            }

            {
                final String key = getString(R.string.prefToken);
                Preference preference = (Preference) findPreference(key);
                preference.setSummary(self.getToken());
            }

            {
                final String key = getString(R.string.prefRefreshToken);
                Preference preference = (Preference) findPreference(key);
                preference.setSummary(self.getRefreshToken());
            }

            {
                final String key = getString(R.string.prefTokenExpires);
                Preference preference = (Preference) findPreference(key);
                String expires = DateUtils.getDateFromFormat3(getActivity().getApplicationContext(), "yyyy/MM/dd HH:mm:ss", self.getTokenExpires(), self.getLanguage());
                preference.setSummary(expires);
            }

            {
                final String key = getString(R.string.prefUpdateToken);
                Preference preference = (Preference) findPreference(key);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        new AlertDialog.Builder(getActivity(), R.style.ActionDialogStyle)
                                .setTitle("本当に更新しますか？")
                                .setPositiveButton("はい", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        AppController.getInstance().updateToken();
                                    }
                                })
                                .setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                        return false;
                    }
                });
            }

            {
                final String key = getResources().getString(R.string.choiceUserStatus);
                ListPreference pref = (ListPreference) findPreference(key);
                pref.setValueIndex(self.getStatus());
                pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        LOGD(TAG, "changed value=" + newValue);
                        int newStatus = Integer.valueOf((String) newValue);
                        int status = Me.ACTIVE;
                        switch (newStatus) {
                            case 0:
                                status = Me.ACTIVE;
                                break;
                            case 1:
                                status = Me.SUSPENDED;
                                break;
                            case 2:
                                status = Me.LEAVED;
                                break;
                        }
                        self.setStatus(status);
                        try {
                            self.getManager().save();
                        } catch (Exception e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                        return false;
                    }
                });
            }

            {
                final String key = getString(R.string.registrationId);
                Preference preference = (Preference) findPreference(key);
                preference.setSummary(self.getDeviceId());
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        ClipData.Item item = new ClipData.Item(self.getDeviceId());
                        String[] mimeType = new String[1];
                        mimeType[0] = ClipDescription.MIMETYPE_TEXT_URILIST;
                        ClipData cd = new ClipData(new ClipDescription("text_data", mimeType), item);
                        ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                        cm.setPrimaryClip(cd);
                        Toast.makeText(getActivity(), "デバイスIDをクリップボードにコピーしました", Toast.LENGTH_SHORT).show();

                        return false;
                    }
                });
            }

            {
                final String key = getResources().getString(R.string.uninstall);
                PreferenceScreen pref = (PreferenceScreen) findPreference(key);
                pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(Intent.ACTION_DELETE);
                        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                        startActivity(intent);
                        return true;
                    }
                });
            }

            {
                final String key = getResources().getString(R.string.coordinateSkipMin);
                EditTextPreference pref = (EditTextPreference) findPreference(key);
                int skipMin = prefs.getInt(CoordinatesBaseActivity.COORDINATE_SKIP_MIN, 0);
                pref.setText(String.valueOf(skipMin));
                pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        int val = Integer.parseInt((String) newValue);
                        prefs.edit().putInt(CoordinatesBaseActivity.COORDINATE_SKIP_MIN, val).commit();
                        return false;
                    }
                });
            }

            {
                final String key = getResources().getString(R.string.coordinateSkipMax);
                int skipMax = prefs.getInt(CoordinatesBaseActivity.COORDINATE_SKIP_MAX, 0);
                EditTextPreference pref = (EditTextPreference) findPreference(key);
                pref.setText(String.valueOf(skipMax));
                pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        int val = Integer.parseInt((String) newValue);
                        prefs.edit().putInt(CoordinatesBaseActivity.COORDINATE_SKIP_MAX, val).commit();
                        return false;
                    }
                });
            }

            {
                final String key = getString(R.string.decideIsLogin);
                Preference preference = (Preference) findPreference(key);
                preference.setSummary(AppController.getInstance().isDecideLogin() ? "True" : "False");
            }

            {
                final String key = getString(R.string.decideUuid);
                Preference preference = (Preference) findPreference(key);
                preference.setSummary(AppController.getInstance().getDecideUuid());
            }

            {
                final String key = getString(R.string.decideCsrfTokenPref);
                Preference preference = (Preference) findPreference(key);
                preference.setSummary(AppController.getInstance().getDecideCsrfToken());
            }

            {
                final String key = getString(R.string.decideCookie);
                Preference preference = (Preference) findPreference(key);
                preference.setSummary(AppController.getInstance().getDecideCookie());
            }

            {
                final String key = getString(R.string.decideCompTime);
                Preference preference = (Preference) findPreference(key);
                preference.setSummary(String.valueOf(AppController.getInstance().getDecideCompTime())+" 取得済み?"+AppController.getInstance().gotCompTime);
            }

            {
                final String key = getString(R.string.prefAppVersionName);
                Preference preference = (Preference) findPreference(key);
                preference.setSummary(BuildConfig.VERSION_NAME);
            }

            {
                final String key = getString(R.string.prefAppVersionCode);
                Preference preference = (Preference) findPreference(key);
                preference.setSummary(String.valueOf(BuildConfig.VERSION_CODE));
            }
        }

        @Subscribe
        public void onUpdatedToken(final Me me) {
            if (me != null) {
                updateView();
            }
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            TokenBus.get().unregister(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        }
    }
}
