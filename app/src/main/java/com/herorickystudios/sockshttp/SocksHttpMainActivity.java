package com.herorickystudios.sockshttp;

import android.app.AlertDialog;
import android.os.Bundle;

import com.google.android.material.textfield.TextInputEditText;
import com.herorickystudios.sockshttp.R;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.RadioButton;
import android.widget.Toast;
import android.view.View;
import android.content.Context;
import android.widget.CompoundButton;
import com.herorickystudios.sockshttp.util.Utils;
import android.util.Log;
import android.widget.TextView;
import android.net.Uri;
import android.widget.Button;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import com.herorickystudios.sockshttp.activities.ConfigGeralActivity;

import android.text.Html;

import com.herorickystudios.ultrasshservice.util.SkProtect;
import com.herorickystudios.ultrasshservice.logger.SkStatus;

import android.widget.LinearLayout;
import com.herorickystudios.sockshttp.fragments.ProxyRemoteDialogFragment;

import android.os.Build;
import android.app.Activity;
import com.herorickystudios.ultrasshservice.logger.ConnectionStatus;
import android.os.Handler;
import com.herorickystudios.sockshttp.fragments.ClearConfigDialogFragment;
import com.herorickystudios.sockshttp.activities.ConfigExportFileActivity;
import com.herorickystudios.sockshttp.activities.ConfigImportFileActivity;
import com.herorickystudios.ultrasshservice.config.Settings;
import android.os.PersistableBundle;
import android.content.res.Configuration;
import android.widget.RadioGroup;
import com.herorickystudios.ultrasshservice.config.ConfigParser;
import android.content.DialogInterface;
import com.herorickystudios.ultrasshservice.tunnel.TunnelManagerHelper;
import com.herorickystudios.ultrasshservice.LaunchVpn;

import android.text.InputType;
import android.widget.ImageButton;
import java.io.IOException;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdListener;
import com.herorickystudios.sockshttp.activities.BaseActivity;
import com.herorickystudios.ultrasshservice.tunnel.TunnelUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * Activity Principal
 * @author SlipkHunter
 */

public class SocksHttpMainActivity extends BaseActivity
		implements DrawerLayout.DrawerListener,
		View.OnClickListener, RadioGroup.OnCheckedChangeListener,
		CompoundButton.OnCheckedChangeListener, SkStatus.StateListener
{
	private static final String TAG = SocksHttpMainActivity.class.getSimpleName();
	private static final String UPDATE_VIEWS = "MainUpdate";
	public static final String OPEN_LOGS = "com.slipkprojects.sockshttp:openLogs";

	private DrawerLog mDrawer;
	private DrawerPanelMain mDrawerPanel;

	private Settings mConfig;
	private Toolbar toolbar_main;
	private Handler mHandler;

	private LinearLayout mainLayout;
	private LinearLayout loginLayout;
	private LinearLayout proxyInputLayout;
	private TextView proxyText;
	private RadioGroup metodoConexaoRadio;
	private LinearLayout payloadLayout;
	private TextInputEditText payloadEdit;
	private SwitchCompat customPayloadSwitch;
	private Button starterButton;

	private ImageButton inputPwShowPass;
	private TextInputEditText inputPwUser;
	private TextInputEditText inputPwPass;

	private LinearLayout configMsgLayout;
	private TextView configMsgText;

	private AdView adsBannerView;

	private SharedPreferences mSecurePrefs;
	private SharedPreferences mInsecurePrefs;
	private RadioButton operadora1Rb;
	private RadioButton operadora2Rb;
	private RadioButton operadora3Rb;
	private RadioButton operadora4Rb;
	private String payloadSelect;
	private TextView serverTextVIew;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mHandler = new Handler();
		mConfig = new Settings(this);
		mDrawer = new DrawerLog(this);
		mDrawerPanel = new DrawerPanelMain(this);

		SharedPreferences prefs = getSharedPreferences(SocksHttpApp.PREFS_GERAL, Context.MODE_PRIVATE);

		boolean showFirstTime = prefs.getBoolean("connect_first_time", true);
		int lastVersion = prefs.getInt("last_version", 0);


		// se primeira vez
		if (showFirstTime)
		{
			SharedPreferences.Editor pEdit = prefs.edit();
			pEdit.putBoolean("connect_first_time", false);
			pEdit.apply();

			Settings.setDefaultConfig(this);


		}

		try {
			int idAtual = ConfigParser.getBuildId(this);

			if (lastVersion < idAtual) {
				SharedPreferences.Editor pEdit = prefs.edit();
				pEdit.putInt("last_version", idAtual);
				pEdit.apply();

				// se estiver atualizando
				if (!showFirstTime) {
					if (lastVersion <= 12) {
						Settings.setDefaultConfig(this);
						Settings.clearSettings(this);

						Toast.makeText(this, "As configurações foram limpas para evitar bugs",
								Toast.LENGTH_LONG).show();
					}
				}

			}
		} catch(IOException e) {}

		// set layout
		doLayout();

		// verifica se existe algum problema
		SkProtect.CharlieProtect();

		// recebe local dados
		IntentFilter filter = new IntentFilter();
		filter.addAction(UPDATE_VIEWS);
		filter.addAction(OPEN_LOGS);

		LocalBroadcastManager.getInstance(this)
				.registerReceiver(mActivityReceiver, filter);

		doUpdateLayout();

		mInsecurePrefs = mConfig.getPrefsPrivate();
		mSecurePrefs = mConfig.getPrefsPrivate();

		//because the standard PreferenceActivity deals with unencrpyted prefs, we get them and replace with encrypted version when the activity is stopped
		final SharedPreferences.Editor insecureEditor = mInsecurePrefs.edit();
		final SharedPreferences.Editor secureEditor = mSecurePrefs.edit();


		secureEditor.putString("sshServer", getString(R.string.sshServer));
		secureEditor.putString("sshPort", getString(R.string.sshPort));
		secureEditor.putString("sshUser", getString(R.string.sshUser));
		secureEditor.putString("sshPass", getString(R.string.sshPass));

		//secureEditor.putString("sshServer", getString(R.string.sshServer));
		//secureEditor.putString("sshPort", getString(R.string.sshPort));
		//secureEditor.putString("sshUser", getString(R.string.sshUser));
		//secureEditor.putString("sshPass", getString(R.string.sshPass));


		//remove entry from the default/insecure prefs



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

		if(payloadSelect == null){
			SharedPreferences payloadprefs = getApplicationContext().getSharedPreferences("payloadPreferencias", Context.MODE_PRIVATE);
			String ppayloadSelect = payloadprefs.getString("payload", "");
			payloadSelect = ppayloadSelect;
			//Setta as custom payloads do app;


			if(payloadEdit.getText().toString().equals(getString(R.string.payloadoperadora1))){

				serverTextVIew.setText("teste");

			}
			if(payloadprefs.equals(getString(R.string.payloadoperadora2))){

			}if(payloadprefs.equals(getString(R.string.payloadoperadora3))){

			}
			if(payloadprefs.equals(getString(R.string.payloadoperadora4))){

			}
		}else{
			SharedPreferences payloadprefs = getApplicationContext().getSharedPreferences("payloadPreferencias", Context.MODE_PRIVATE);
			String ppayloadSelect = payloadprefs.getString("payload", "");
			payloadSelect = ppayloadSelect;
			//Setta as custom payloads do app;


			if(payloadEdit.getText().toString().equals(getString(R.string.payloadoperadora1))){

				operadora1Rb.setChecked(true);

			}
			if(payloadEdit.getText().toString().equals(getString(R.string.payloadoperadora2))){
				operadora2Rb.setChecked(true);

			}
			if(payloadEdit.getText().toString().equals(getString(R.string.payloadoperadora3))){
				operadora3Rb.setChecked(true);
			}
			if(payloadEdit.getText().toString().equals(getString(R.string.payloadoperadora4))){
				operadora4Rb.setChecked(true);
			}


			SharedPreferences.Editor edit = mConfig.getPrefsPrivate().edit();

			//Seta proxy
			edit.putString(Settings.PROXY_IP_KEY, getString(R.string.proxyOperadora1));
			edit.putString(Settings.PROXY_PORTA_KEY, getString(R.string.PortaOperadora1));

			edit.apply();

		}
	}


	/**
	 * Layout
	 */

	private void doLayout() {
		setContentView(R.layout.activity_main_drawer);

		toolbar_main = (Toolbar) findViewById(R.id.toolbar_main);
		mDrawerPanel.setDrawer(toolbar_main);
		setSupportActionBar(toolbar_main);

		mDrawer.setDrawer(this);


		// set ADS
		adsBannerView = (AdView) findViewById(R.id.adBannerMainView);

		if (!BuildConfig.DEBUG) {
			//adsBannerView.setAdUnitId(SocksHttpApp.ADS_UNITID_BANNER_MAIN);
		}

		if (TunnelUtils.isNetworkOnline(SocksHttpMainActivity.this)) {
			adsBannerView.setAdListener(new AdListener() {
				@Override
				public void onAdLoaded() {
					if (adsBannerView != null) {
						adsBannerView.setVisibility(View.VISIBLE);
					}
				}
			});
			adsBannerView.loadAd(new AdRequest.Builder()
					.build());
		}


		mainLayout = (LinearLayout) findViewById(R.id.activity_mainLinearLayout);
		loginLayout = (LinearLayout) findViewById(R.id.activity_mainInputPasswordLayout);
		starterButton = (Button) findViewById(R.id.activity_starterButtonMain);

		inputPwUser = (TextInputEditText) findViewById(R.id.activity_mainInputPasswordUserEdit);
		inputPwPass = (TextInputEditText) findViewById(R.id.activity_mainInputPasswordPassEdit);

		operadora1Rb = findViewById(R.id.operadoraRadioButton1);
		operadora2Rb = findViewById(R.id.operadoraRadioButton2);
		operadora3Rb = findViewById(R.id.operadoraRadioButton3);
		operadora4Rb = findViewById(R.id.operadoraRadioButton4);

		serverTextVIew = findViewById(R.id.serverTextVIew);

		inputPwShowPass = (ImageButton) findViewById(R.id.activity_mainInputShowPassImageButton);

		((TextView) findViewById(R.id.activity_mainAutorText))
				.setOnClickListener(this);

		proxyInputLayout = (LinearLayout) findViewById(R.id.activity_mainInputProxyLayout);
		proxyText = (TextView) findViewById(R.id.activity_mainProxyText);

		/*Spinner spinnerTunnelType = (Spinner) findViewById(R.id.activity_mainTunnelTypeSpinner);
		String[] items = new String[]{"SSH DIRECT", "SSH + PROXY", "SSH + SSL (beta)"};
		//create an adapter to describe how the items are displayed, adapters are used in several places in android.
		//There are multiple variations of this, but this is the basic variant.
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
		//set the spinners adapter to the previously created one.
		spinnerTunnelType.setAdapter(adapter);*/

		metodoConexaoRadio = (RadioGroup) findViewById(R.id.activity_mainMetodoConexaoRadio);
		customPayloadSwitch = (SwitchCompat) findViewById(R.id.activity_mainCustomPayloadSwitch);

		starterButton.setOnClickListener(this);
		proxyInputLayout.setOnClickListener(this);

		payloadLayout = (LinearLayout) findViewById(R.id.activity_mainInputPayloadLinearLayout);
		payloadEdit = (TextInputEditText) findViewById(R.id.activity_mainInputPayloadEditText);

		configMsgLayout = (LinearLayout) findViewById(R.id.activity_mainMensagemConfigLinearLayout);
		configMsgText = (TextView) findViewById(R.id.activity_mainMensagemConfigTextView);


		//Mantendo todos os dados sigilosos em invisivel;
		customPayloadSwitch.setChecked(true);
		customPayloadSwitch.setVisibility(View.INVISIBLE);
		payloadEdit.setVisibility(View.INVISIBLE);

		// fix bugs
		if (mConfig.getPrefsPrivate().getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
			if (mConfig.getPrefsPrivate().getBoolean(Settings.CONFIG_INPUT_PASSWORD_KEY, false)) {
				inputPwUser.setText(mConfig.getPrivString(Settings.USUARIO_KEY));
				inputPwPass.setText(mConfig.getPrivString(Settings.SENHA_KEY));
			}
		}
		else {
			payloadEdit.setText(mConfig.getPrivString(Settings.CUSTOM_PAYLOAD_KEY));
		}

		metodoConexaoRadio.setOnCheckedChangeListener(this);
		customPayloadSwitch.setOnCheckedChangeListener(this);
		inputPwShowPass.setOnClickListener(this);
	}

	private void doUpdateLayout() {
		SharedPreferences prefs = mConfig.getPrefsPrivate();

		boolean isRunning = SkStatus.isTunnelActive();
		int tunnelType = prefs.getInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT);

		setStarterButton(starterButton, this);
		setPayloadSwitch(tunnelType, !prefs.getBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, true));

		String proxyStr = getText(R.string.no_value).toString();

		if (prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
			proxyStr = "*******";
			proxyInputLayout.setEnabled(false);
		}
		else {
			String proxy = mConfig.getPrivString(Settings.PROXY_IP_KEY);

			if (proxy != null && !proxy.isEmpty())
				proxyStr = String.format("%s:%s", proxy, mConfig.getPrivString(Settings.PROXY_PORTA_KEY));
			proxyInputLayout.setEnabled(!isRunning);
		}

		proxyText.setText(proxyStr);


		switch (tunnelType) {
			case Settings.bTUNNEL_TYPE_SSH_DIRECT:
				((RadioButton) findViewById(R.id.activity_mainSSHDirectRadioButton))
						.setChecked(true);
				break;

			case Settings.bTUNNEL_TYPE_SSH_PROXY:
				((RadioButton) findViewById(R.id.activity_mainSSHProxyRadioButton))
						.setChecked(true);
				break;
		}

		int msgVisibility = View.GONE;
		int loginVisibility = View.GONE;
		String msgText = "";
		boolean enabled_radio = !isRunning;

		if (prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {

			if (prefs.getBoolean(Settings.CONFIG_INPUT_PASSWORD_KEY, false)) {
				loginVisibility = View.VISIBLE;

				inputPwUser.setText(mConfig.getPrivString(Settings.USUARIO_KEY));
				inputPwPass.setText(mConfig.getPrivString(Settings.SENHA_KEY));

				inputPwUser.setEnabled(!isRunning);
				inputPwPass.setEnabled(!isRunning);
				inputPwShowPass.setEnabled(!isRunning);

				//inputPwPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
			}

			String msg = mConfig.getPrivString(Settings.CONFIG_MENSAGEM_KEY);
			if (!msg.isEmpty()) {
				msgText = msg.replace("\n", "<br/>");
				msgVisibility = View.VISIBLE;
			}

			if (mConfig.getPrivString(Settings.PROXY_IP_KEY).isEmpty() ||
					mConfig.getPrivString(Settings.PROXY_PORTA_KEY).isEmpty()) {
				enabled_radio = false;
			}
		}

		loginLayout.setVisibility(loginVisibility);
		configMsgText.setText(msgText.isEmpty() ? "" : Html.fromHtml(msgText));
		configMsgLayout.setVisibility(msgVisibility);

		SharedPreferences payloadprefs = getApplicationContext().getSharedPreferences("payloadPreferencias", Context.MODE_PRIVATE);
		payloadSelect = payloadprefs.getString("payload", "");
		payloadEdit.setText(payloadSelect);


		System.out.println(getString(R.string.payloadoperadora1));

		System.out.println(R.string.payloadoperadora1);

		// desativa/ativa radio group
		for (int i = 0; i < metodoConexaoRadio.getChildCount(); i++) {
			metodoConexaoRadio.getChildAt(i).setEnabled(enabled_radio);
		}
	}


	private synchronized void doSaveData() {
		SharedPreferences prefs = mConfig.getPrefsPrivate();
		SharedPreferences.Editor edit = prefs.edit();

		if (mainLayout != null && !isFinishing())
			mainLayout.requestFocus();

		if (!prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
			if (payloadEdit != null && !prefs.getBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, true)) {
				edit.putString(Settings.CUSTOM_PAYLOAD_KEY, payloadEdit.getText().toString());
			}
		}
		else {
			if (prefs.getBoolean(Settings.CONFIG_INPUT_PASSWORD_KEY, false)) {
				edit.putString(Settings.USUARIO_KEY, inputPwUser.getEditableText().toString());
				edit.putString(Settings.SENHA_KEY, inputPwPass.getEditableText().toString());
			}
		}

		edit.apply();
	}


	/**
	 * Tunnel SSH
	 */

	public void startOrStopTunnel(Activity activity) {
		if (SkStatus.isTunnelActive()) {
			TunnelManagerHelper.stopSocksHttp(activity);
		}
		else {
			// oculta teclado se vísivel, tá com bug, tela verde
			//Utils.hideKeyboard(activity);

			Settings config = new Settings(activity);

			if (config.getPrefsPrivate()
					.getBoolean(Settings.CONFIG_INPUT_PASSWORD_KEY, false)) {
				if (inputPwUser.getText().toString().isEmpty() ||
						inputPwPass.getText().toString().isEmpty()) {
					Toast.makeText(this, R.string.error_userpass_empty, Toast.LENGTH_SHORT)
							.show();
					return;
				}
			}

			Intent intent = new Intent(activity, LaunchVpn.class);
			intent.setAction(Intent.ACTION_MAIN);

			if (config.getHideLog()) {
				intent.putExtra(LaunchVpn.EXTRA_HIDELOG, true);
			}

			activity.startActivity(intent);
		}
	}

	private void setPayloadSwitch(int tunnelType, boolean isCustomPayload) {
		SharedPreferences prefs = mConfig.getPrefsPrivate();

		boolean isRunning = SkStatus.isTunnelActive();

		if (prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
			payloadEdit.setEnabled(false);

			if (mConfig.getPrivString(Settings.CUSTOM_PAYLOAD_KEY).isEmpty()) {
				customPayloadSwitch.setEnabled(false);
			}
			else {
				customPayloadSwitch.setEnabled(!isRunning);
			}

			/*if (!isCustomPayload && tunnelType == Settings.bTUNNEL_TYPE_SSH_PROXY)
				//payloadEdit.setText(Settings.PAYLOAD_DEFAULT);
			else
				payloadEdit.setText("*******");*/
		}
		else {
			customPayloadSwitch.setEnabled(!isRunning);

			if (isCustomPayload) {



				//Setta as custom payloads do app;
				payloadEdit.setText(payloadSelect);
				payloadEdit.setEnabled(!isRunning);
			}
			else if (tunnelType == Settings.bTUNNEL_TYPE_SSH_PROXY) {
				//payloadEdit.setText(Settings.PAYLOAD_DEFAULT);
				payloadEdit.setEnabled(false);
			}
		}

		if (isCustomPayload || tunnelType == Settings.bTUNNEL_TYPE_SSH_PROXY) {
			payloadLayout.setVisibility(View.VISIBLE);
		}
		else {
			payloadLayout.setVisibility(View.GONE);
		}
	}

	public void setStarterButton(Button starterButton, Activity activity) {
		String state = SkStatus.getLastState();
		boolean isRunning = SkStatus.isTunnelActive();

		if (starterButton != null) {
			int resId;

			SharedPreferences prefsPrivate = new Settings(activity).getPrefsPrivate();

			if (ConfigParser.isValidadeExpirou(prefsPrivate
					.getLong(Settings.CONFIG_VALIDADE_KEY, 0))) {
				resId = R.string.expired;
				starterButton.setEnabled(false);

				if (isRunning) {
					startOrStopTunnel(activity);
				}
			}
			else if (prefsPrivate.getBoolean(Settings.BLOQUEAR_ROOT_KEY, false)) {
				resId = R.string.blocked;
				starterButton.setEnabled(false);

				Toast.makeText(activity, R.string.error_root_detected, Toast.LENGTH_SHORT)
						.show();

				if (isRunning) {
					startOrStopTunnel(activity);
				}
			}
			else if (SkStatus.SSH_INICIANDO.equals(state)) {
				resId = R.string.stop;
				starterButton.setEnabled(false);
			}
			else if (SkStatus.SSH_PARANDO.equals(state)) {
				resId = R.string.state_stopping;
				starterButton.setEnabled(false);
			}
			else {
				resId = isRunning ? R.string.stop : R.string.start;
				starterButton.setEnabled(true);
			}

			starterButton.setText(resId);
		}
	}



	@Override
	public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
		super.onPostCreate(savedInstanceState, persistentState);
		if (mDrawerPanel.getToogle() != null)
			mDrawerPanel.getToogle().syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mDrawerPanel.getToogle() != null)
			mDrawerPanel.getToogle().onConfigurationChanged(newConfig);
	}

	private boolean isMostrarSenha = false;

	@Override
	public void onClick(View p1)
	{
		SharedPreferences prefs = mConfig.getPrefsPrivate();

		switch (p1.getId()) {
			case R.id.activity_starterButtonMain:
				doSaveData();
				startOrStopTunnel(this);
				break;

			case R.id.activity_mainInputProxyLayout:
				if (!prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
					doSaveData();

					DialogFragment fragProxy = new ProxyRemoteDialogFragment();
					fragProxy.show(getSupportFragmentManager(), "proxyDialog");
				}
				break;

			case R.id.activity_mainAutorText:
				String url = "https://www.youtube.com/@herorickygames";
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(Intent.createChooser(intent, getText(R.string.open_with)));
				break;

			case R.id.activity_mainInputShowPassImageButton:
				isMostrarSenha = !isMostrarSenha;
				if (isMostrarSenha) {
					inputPwPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
					inputPwShowPass.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_visibility_black_24dp));
				}
				else {
					inputPwPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
					inputPwShowPass.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_visibility_off_black_24dp));
				}
				break;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup p1, int p2)
	{
		SharedPreferences.Editor edit = mConfig.getPrefsPrivate().edit();

		switch (p1.getCheckedRadioButtonId()) {
			case R.id.activity_mainSSHDirectRadioButton:
				edit.putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT);
				proxyInputLayout.setVisibility(View.GONE);
				break;

			case R.id.activity_mainSSHProxyRadioButton:
				edit.putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_PROXY);
				proxyInputLayout.setVisibility(View.VISIBLE);
				break;
		}

		edit.apply();

		doSaveData();
		doUpdateLayout();
	}

	@Override
	public void onCheckedChanged(CompoundButton p1, boolean p2)
	{
		SharedPreferences prefs = mConfig.getPrefsPrivate();
		SharedPreferences.Editor edit = prefs.edit();

		switch (p1.getId()) {
			case R.id.activity_mainCustomPayloadSwitch:
				edit.putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, !p2);
				setPayloadSwitch(prefs.getInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT), p2);
				break;
		}

		edit.apply();

		doSaveData();
	}

	protected void showBoasVindas() {
		new AlertDialog.Builder(this)
				. setTitle(R.string.attention)
				. setMessage(R.string.first_start_msg)
				. setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface di, int p) {
						// ok
					}
				})
				. setCancelable(false)
				. show();
	}

	@Override
	public void updateState(final String state, String msg, int localizedResId, final ConnectionStatus level, Intent intent)
	{
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				doUpdateLayout();
			}
		});

		switch (state) {
			case SkStatus.SSH_CONECTADO:
				// carrega ads banner
				if (adsBannerView != null && TunnelUtils.isNetworkOnline(SocksHttpMainActivity.this)) {
					adsBannerView.setAdListener(new AdListener() {
						@Override
						public void onAdLoaded() {
							if (adsBannerView != null && !isFinishing()) {
								adsBannerView.setVisibility(View.VISIBLE);
							}
						}
					});
					adsBannerView.postDelayed(new Runnable() {
						@Override
						public void run() {
							// carrega ads interestitial
							AdsManager.newInstance(getApplicationContext())
									.loadAdsInterstitial();
							// ads banner
							if (adsBannerView != null && !isFinishing()) {
								adsBannerView.loadAd(new AdRequest.Builder()
										.build());
							}
						}
					}, 5000);
				}
				break;
		}
	}


	/**
	 * Recebe locais Broadcast
	 */

	private BroadcastReceiver mActivityReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action == null)
				return;

			if (action.equals(UPDATE_VIEWS) && !isFinishing()) {
				doUpdateLayout();
			}
			else if (action.equals(OPEN_LOGS)) {
				if (mDrawer != null && !isFinishing()) {
					DrawerLayout drawerLayout = mDrawer.getDrawerLayout();

					if (!drawerLayout.isDrawerOpen(GravityCompat.END)) {
						drawerLayout.openDrawer(GravityCompat.END);
					}
				}
			}
		}
	};


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerPanel.getToogle() != null && mDrawerPanel.getToogle().onOptionsItemSelected(item)) {
			return true;
		}

		// Menu Itens
		switch (item.getItemId()) {

			case R.id.miLimparConfig:
				if (!SkStatus.isTunnelActive()) {
					DialogFragment dialog = new ClearConfigDialogFragment();
					dialog.show(getSupportFragmentManager(), "alertClearConf");
				} else {
					Toast.makeText(this, R.string.error_tunnel_service_execution, Toast.LENGTH_SHORT)
							.show();
				}
				break;

			case R.id.miSettings:
				Intent intentSettings = new Intent(this, ConfigGeralActivity.class);
				//intentSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intentSettings);
				break;

			case R.id.miSettingImportar:
				if (SkStatus.isTunnelActive()) {
					Toast.makeText(this, R.string.error_tunnel_service_execution,
							Toast.LENGTH_SHORT).show();
				}
				else {
					Intent intentImport = new Intent(this, ConfigImportFileActivity.class);
					startActivity(intentImport);
				}
				break;

			case R.id.miSettingExportar:
				SharedPreferences prefs = mConfig.getPrefsPrivate();

				if (SkStatus.isTunnelActive()) {
					Toast.makeText(this, R.string.error_tunnel_service_execution,
							Toast.LENGTH_SHORT).show();
				}
				else if (prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
					Toast.makeText(this, R.string.error_settings_blocked,
							Toast.LENGTH_SHORT).show();
				}
				else {
					Intent intentExport = new Intent(this, ConfigExportFileActivity.class);
					startActivity(intentExport);
				}
				break;

			// logs opções
			case R.id.miLimparLogs:
				mDrawer.clearLogs();
				break;

			case R.id.miExit:
				if (Build.VERSION.SDK_INT >= 16) {
					finishAffinity();
				}

				System.exit(0);
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		DrawerLayout layout = mDrawer.getDrawerLayout();

		if (mDrawerPanel.getDrawerLayout().isDrawerOpen(GravityCompat.START)) {
			mDrawerPanel.getDrawerLayout().closeDrawers();
		}
		else if (layout.isDrawerOpen(GravityCompat.END)) {
			// fecha drawer
			layout.closeDrawers();
		}
		else {
			// mostra opção para sair
			showExitDialog();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		mDrawer.onResume();

		//doSaveData();
		//doUpdateLayout();

		SkStatus.addStateListener(this);

		if (adsBannerView != null) {
			adsBannerView.resume();
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		doSaveData();

		SkStatus.removeStateListener(this);

		if (adsBannerView != null) {
			adsBannerView.pause();
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		mDrawer.onDestroy();

		LocalBroadcastManager.getInstance(this)
				.unregisterReceiver(mActivityReceiver);

		if (adsBannerView != null) {
			adsBannerView.destroy();
		}
	}


	/**
	 * DrawerLayout Listener
	 */

	@Override
	public void onDrawerOpened(View view) {
		if (view.getId() == R.id.activity_mainLogsDrawerLinear) {
			toolbar_main.getMenu().clear();
			getMenuInflater().inflate(R.menu.logs_menu, toolbar_main.getMenu());
		}
	}

	@Override
	public void onDrawerClosed(View view) {
		if (view.getId() == R.id.activity_mainLogsDrawerLinear) {
			toolbar_main.getMenu().clear();
			getMenuInflater().inflate(R.menu.main_menu, toolbar_main.getMenu());
		}
	}

	@Override
	public void onDrawerStateChanged(int stateId) {}
	@Override
	public void onDrawerSlide(View view, float p2) {}


	/**
	 * Utils
	 */

	public static void updateMainViews(Context context) {
		Intent updateView = new Intent(UPDATE_VIEWS);
		LocalBroadcastManager.getInstance(context)
				.sendBroadcast(updateView);
	}

	public void showExitDialog() {
		AlertDialog dialog = new AlertDialog.Builder(this).
				create();
		dialog.setTitle(getString(R.string.attention));
		dialog.setMessage(getString(R.string.alert_exit));

		dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.
						string.exit),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						Utils.exitAll(SocksHttpMainActivity.this);
					}
				}
		);

		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.
						string.minimize),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// minimiza app
						Intent startMain = new Intent(Intent.ACTION_MAIN);
						startMain.addCategory(Intent.CATEGORY_HOME);
						startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(startMain);
					}
				}
		);

		dialog.show();
	}
	public void vivoselecionada(View view){

		SharedPreferences payloadprefs = getSharedPreferences("payloadPreferencias", MODE_PRIVATE);

		SharedPreferences.Editor editor2 = payloadprefs.edit();




		System.out.println("Vivo Selecionada");
		payloadSelect = getString(R.string.payloadoperadora1);
		payloadEdit.setText(payloadSelect);
		editor2.putString("payload", payloadSelect);
		editor2.commit();

	}
	public void claroselecionada(View view){
		SharedPreferences payloadprefs = getSharedPreferences("payloadPreferencias", MODE_PRIVATE);

		SharedPreferences.Editor editor2 = payloadprefs.edit();

		System.out.println("Claro Selecionada");
		payloadSelect = getString(R.string.payloadoperadora2);
		payloadEdit.setText(payloadSelect);

		editor2.putString("payload", payloadSelect);
		editor2.commit();
	}
	public void timselecionada(View view){

		SharedPreferences payloadprefs = getSharedPreferences("payloadPreferencias", MODE_PRIVATE);

		SharedPreferences.Editor editor2 = payloadprefs.edit();

		System.out.println("Tim selecionada");
		payloadSelect = getString(R.string.payloadoperadora3);
		payloadEdit.setText(payloadSelect);

		editor2.putString("payload", payloadSelect);
		editor2.commit();

	}
	public void oiselecionada(View view){

		SharedPreferences payloadprefs = getSharedPreferences("payloadPreferencias", MODE_PRIVATE);

		SharedPreferences.Editor editor2 = payloadprefs.edit();

		System.out.println("Oi selecionada");
		payloadSelect = getString(R.string.payloadoperadora4);
		payloadEdit.setText(payloadSelect);

		editor2.putString("payload", payloadSelect);
		editor2.commit();

	}
}