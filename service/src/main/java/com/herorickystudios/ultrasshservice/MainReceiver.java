package com.herorickystudios.ultrasshservice;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.herorickystudios.ultrasshservice.tunnel.TunnelManagerHelper;

public class MainReceiver extends BroadcastReceiver
{
	public static final String ACTION_SERVICE_RESTART = "sshTunnelServiceRestsrt",
		ACTION_SERVICE_STOP = "sshtunnelservicestop";
		
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String acao = intent.getAction();
		
		if (acao == null) {
			return;
		}
		
		switch (acao) {
			
			case ACTION_SERVICE_STOP:
				TunnelManagerHelper.stopSocksHttp(context);
			break;
			
			case ACTION_SERVICE_RESTART:
				Intent restartTunnel = new Intent(SocksHttpService.TUNNEL_SSH_RESTART_SERVICE);
				LocalBroadcastManager.getInstance(context)
					.sendBroadcast(restartTunnel);
			break;
		}
	}
}
