package com.herorickystudios.sockshttp.fragments;

import android.app.Dialog;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.DialogInterface;
import com.herorickystudios.sockshttp.R;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.herorickystudios.ultrasshservice.logger.SkStatus;
import com.herorickystudios.sockshttp.SocksHttpMainActivity;
import com.herorickystudios.ultrasshservice.config.Settings;

public class ClearConfigDialogFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog dialog = new AlertDialog.Builder(getActivity()).
			create();
		dialog.setTitle(getActivity().getString(R.string.attention));
		dialog.setMessage(getActivity().getString(R.string.alert_clear_settings));

		dialog.setButton(DialogInterface.BUTTON_POSITIVE, getActivity().getString(R.
			string.yes),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Settings.clearSettings(getContext());
					
					// limpa logs
					SkStatus.clearLog();
					
					SocksHttpMainActivity.updateMainViews(getContext());
					
					Toast.makeText(getActivity(), R.string.success_clear_settings, Toast.LENGTH_SHORT)
						.show();
				}
			}
		);

		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getActivity().getString(R.
			string.no),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dismiss();
				}
			}
		);
		
		return dialog;
	}

}
