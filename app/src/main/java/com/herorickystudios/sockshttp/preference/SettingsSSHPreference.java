package com.herorickystudios.sockshttp.preference;

import android.os.Bundle;
import android.content.SharedPreferences;
import android.content.Intent;

import com.herorickystudios.sockshttp.R;
import com.herorickystudios.ultrasshservice.logger.SkStatus;
import com.herorickystudios.ultrasshservice.config.SettingsConstants;
import com.herorickystudios.ultrasshservice.config.Settings;
import com.herorickystudios.ultrasshservice.logger.ConnectionStatus;
import android.os.Handler;
import android.util.Log;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsSSHPreference extends PreferenceFragmentCompat
	implements SettingsConstants, SkStatus.StateListener
{
	private static final String TAG = SettingsSSHPreference.class.getSimpleName();
	
	private Handler mHandler;
	private Settings mConfig;
	private SharedPreferences mSecurePrefs;
	private SharedPreferences mInsecurePrefs;
	
	protected String[] listEdit_keysProteger = {
		SERVIDOR_KEY,
		SERVIDOR_PORTA_KEY,
		USUARIO_KEY,
		SENHA_KEY
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		mHandler = new Handler();
		mConfig = new Settings(getContext());
	
		mInsecurePrefs = getPreferenceManager()
			.getDefaultSharedPreferences(getContext());
		mSecurePrefs = mConfig.getPrefsPrivate();
	}

	@Override
    public void onCreatePreferences(Bundle bundle, String s)
	{
        // Load the Preferences from the XML file
        setPreferencesFromResource(R.xml.sshtunnel_preferences, s);
		
		// update views
		getPreferenceScreen().setEnabled(!SkStatus.isTunnelActive());
	}
	
	@Override
	public void onResume()
	{
		super.onResume();

		SkStatus.addStateListener(this);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		SkStatus.removeStateListener(this);
	}

	@Override
	public void updateState(String state, String logMessage, int localizedResId, ConnectionStatus level, Intent intent)
	{
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				getPreferenceScreen().setEnabled(!SkStatus.isTunnelActive());
			}
		});
	}

	@Override
    public void onStart() {
        super.onStart();
		
		for (String key : listEdit_keysProteger) {
			if (mSecurePrefs.contains(key)) {
				((EditTextPreference)findPreference(key))
					.setText(mSecurePrefs.getString(key, null));
			}
			
			if (mSecurePrefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
				if ((key.equals(USUARIO_KEY) || key.equals(SENHA_KEY)) &&
					mSecurePrefs.getBoolean(Settings.CONFIG_INPUT_PASSWORD_KEY, false)) {
					continue;
				}
				
				Preference pref = findPreference(key);
				
				pref.setEnabled(false);
				pref.setSummary(R.string.blocked);
			}
		}
		
		String key = Settings.PORTA_LOCAL_KEY;
		if (mSecurePrefs.contains(key)) {
			((EditTextPreference)findPreference(key))
				.setText(mSecurePrefs.getString(key, null));
		}
    }
	
	@Override
    public void onStop() {
        super.onStop();

        //because the standard PreferenceActivity deals with unencrpyted prefs, we get them and replace with encrypted version when the activity is stopped
        final SharedPreferences.Editor insecureEditor = mInsecurePrefs.edit();
        final SharedPreferences.Editor secureEditor = mSecurePrefs.edit();
        
		for (String key : listEdit_keysProteger) {
			if (mInsecurePrefs.contains(key)) {

				System.out.println(key);
				secureEditor.putString("sshServer", mInsecurePrefs.getString("sshServer", getString(R.string.sshServer)));
				secureEditor.putString("sshPort", mInsecurePrefs.getString("sshPort", getString(R.string.sshPort)));
				secureEditor.putString("sshUser", mInsecurePrefs.getString("sshUser", getString(R.string.sshUser)));
				secureEditor.putString("sshPass", mInsecurePrefs.getString("sshPass", getString(R.string.sshPass)));


				//remove entry from the default/insecure prefs
				insecureEditor.remove(key);
			}
		}
		
        String key = Settings.PORTA_LOCAL_KEY;
        if (mInsecurePrefs.contains(key)) {
            Log.d(TAG, "match found for " + key + " adding encrypted copy to secure prefs");
            //secureEditor.putString(key, mInsecurePrefs.getString(key, null));

			secureEditor.putString("sshServer", mInsecurePrefs.getString("sshServer", "testeS"));
			secureEditor.putString("sshPort", mInsecurePrefs.getString("sshPort", "testeP"));
			secureEditor.putString("sshUser", mInsecurePrefs.getString("sshUser", "testeU"));
			secureEditor.putString("sshPass", mInsecurePrefs.getString("sshPass", "testePss"));

            insecureEditor.remove(key);
        }
        
		insecureEditor.commit();
        secureEditor.commit();
    }
}
