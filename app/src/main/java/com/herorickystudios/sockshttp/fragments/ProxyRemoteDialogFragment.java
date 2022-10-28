package com.herorickystudios.sockshttp.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.google.android.material.textfield.TextInputEditText;
import com.herorickystudios.sockshttp.R;
import android.view.View;

import com.herorickystudios.sockshttp.SocksHttpMainActivity;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.ViewGroup;
import android.widget.Button;
import com.herorickystudios.ultrasshservice.config.Settings;
import android.content.SharedPreferences;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.DialogFragment;

public class ProxyRemoteDialogFragment extends DialogFragment
	implements View.OnClickListener {

	private Settings mConfig;
	
	private boolean usarProxyAutenticacao;
	private boolean usarPayloadPadrao;
	
	private TextInputEditText proxyRemotoIpEdit;
	private TextInputEditText proxyRemotoPortaEdit;

	private AppCompatCheckBox proxyAutenticacaoCheck;
	private LinearLayout autenticacaoLayout;
	private TextInputEditText autenticacaoUsuario;
	private TextInputEditText autenticacaoSenha;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		
		mConfig = new Settings(getContext());
		SharedPreferences prefs = mConfig.getPrefsPrivate();
		
		usarPayloadPadrao = prefs.getBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, true);
		usarProxyAutenticacao = prefs.getBoolean(Settings.PROXY_USAR_AUTENTICACAO_KEY, false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		getDialog().setCanceledOnTouchOutside(false);
		
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		LayoutInflater li = LayoutInflater.from(getContext());
		View view = li.inflate(R.layout.fragment_proxy_remote, null); 

		proxyRemotoIpEdit = view.findViewById(R.id.fragment_proxy_remoteProxyIpEdit);
		proxyRemotoPortaEdit = view.findViewById(R.id.fragment_proxy_remoteProxyPortaEdit);
		
		proxyAutenticacaoCheck = view.findViewById(R.id.fragment_proxy_remoteUsarAutenticacaoCheck);
		autenticacaoLayout = view.findViewById(R.id.fragment_proxy_remoteAutenticacaoLinearLayout);
		autenticacaoUsuario = view.findViewById(R.id.fragment_proxy_remoteAutenticacaoUsuarioEdit);
		autenticacaoSenha = view.findViewById(R.id.fragment_proxy_remoteAutenticacaoSenhaEdit);
		
		Button cancelButton = view.findViewById(R.id.fragment_proxy_remoteCancelButton);
		Button saveButton = view.findViewById(R.id.fragment_proxy_remoteSaveButton);
		
		cancelButton.setOnClickListener(this);
		saveButton.setOnClickListener(this);
		
		if (!usarPayloadPadrao) {
			proxyAutenticacaoCheck.setEnabled(false);
			autenticacaoLayout.setVisibility(View.GONE);
		}
		else {
			proxyAutenticacaoCheck.setEnabled(true);
		}


		//Setta o proxyIP
		proxyRemotoIpEdit.setText(mConfig.getPrivString(Settings.PROXY_IP_KEY));

		//Setta Porta
		proxyRemotoPortaEdit.setText(mConfig.getPrivString(Settings.PROXY_PORTA_KEY));

		if (!mConfig.getPrivString(Settings.PROXY_PORTA_KEY).isEmpty())


		
		proxyAutenticacaoCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton p1, boolean x) {
				usarProxyAutenticacao = x;
				setProxyAutenticacaoView(x);
			}
		});
		
		if (!usarProxyAutenticacao) {
			setProxyAutenticacaoView(usarProxyAutenticacao);
		}
		else {
			proxyAutenticacaoCheck.setChecked(true);
		}
		
		//setCancelable(false);
		
		return new AlertDialog.Builder(getActivity())
			. setTitle("Configurações de Proxy")
            .setView(view)
			. show();
	}
	
	private void setProxyAutenticacaoView(boolean usarAutenticacao) {
		if (!usarAutenticacao) {
			autenticacaoLayout.setVisibility(View.GONE);
		}
		else {
			autenticacaoLayout.setVisibility(View.VISIBLE);
		}
	}

	
	/**
	 * onClick implementação
	*/

	@Override
	public void onClick(View view)
	{
		switch (view.getId()) {
			case R.id.fragment_proxy_remoteSaveButton:
				String proxy_ip = proxyRemotoIpEdit.getEditableText().toString();
				String proxy_porta = proxyRemotoPortaEdit.getEditableText().toString();
				
				if (proxy_porta == null || proxy_porta.isEmpty() || proxy_porta.equals("0") ||
					proxy_ip == null || proxy_ip.isEmpty()) {
					Toast.makeText(getContext(), "Proxy inválido", Toast.LENGTH_SHORT)
						.show();
				}
				
				else {
					SharedPreferences.Editor edit = mConfig.getPrefsPrivate().edit();
					
					edit.putBoolean(Settings.PROXY_USAR_AUTENTICACAO_KEY, usarProxyAutenticacao);

					edit.putString(Settings.PROXY_IP_KEY, proxy_ip);
					edit.putString(Settings.PROXY_PORTA_KEY, proxy_porta);

					edit.apply();
					
					SocksHttpMainActivity.updateMainViews(getContext());

					dismiss();
				}
			break;

			case R.id.fragment_proxy_remoteCancelButton:
				dismiss();
			break;
		}
	}

}
