package com.herorickystudios.sockshttp;

import android.app.Application;

import com.herorickystudios.ultrasshservice.util.SkProtect;

import android.content.Context;

import com.herorickystudios.ultrasshservice.SocksHttpCore;
import com.herorickystudios.ultrasshservice.config.Settings;
import com.google.android.gms.ads.MobileAds;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

/**
* App
*/
public class SocksHttpApp extends Application
{
	private static final String TAG = SocksHttpApp.class.getSimpleName();
	public static final String PREFS_GERAL = "SocksHttpGERAL";
	
	public static final String ADS_UNITID_INTERSTITIAL_MAIN = "ca-app-pub-6560862030501171/1687567442";
	public static final String ADS_UNITID_BANNER_MAIN = "ca-app-pub-6560862030501171/3101206569";
	public static final String ADS_UNITID_BANNER_SOBRE = "ca-app-pub-6560862030501171/6860519994";
	public static final String ADS_UNITID_BANNER_TEST = "ca-app-pub-3940256099942544/6300978111";
	public static final String APP_FLURRY_KEY = "RQQ8J9Q2N4RH827G32X9";
	
	private static SocksHttpApp mApp;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		mApp = this;
		
		// captura dados para análise
		/*new FlurryAgent.Builder()
			.withCaptureUncaughtExceptions(true)
            .withIncludeBackgroundSessionsInMetrics(true)
            .withLogLevel(Log.VERBOSE)
            .withPerformanceMetrics(FlurryPerformance.ALL)
			.build(this, APP_FLURRY_KEY);*/
			
		// inicia
		SocksHttpCore.init(this);
		
		// protege o app
		SkProtect.init(this);
		
		// Initialize the Mobile Ads SDK.
        MobileAds.initialize(this);
		
		// modo noturno
		setModoNoturno(this);
	}
	
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		//LocaleHelper.setLocale(this);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		//LocaleHelper.setLocale(this);
	}
	
	private void setModoNoturno(Context context) {
		boolean is = new Settings(context)
			.getModoNoturno().equals("on");

		int night_mode = is ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
		AppCompatDelegate.setDefaultNightMode(night_mode);
	}
	
	public static SocksHttpApp getApp() {
		return mApp;
	}
}
