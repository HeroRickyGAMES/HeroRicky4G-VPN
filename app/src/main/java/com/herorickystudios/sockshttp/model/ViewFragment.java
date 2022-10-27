package com.herorickystudios.sockshttp.model;

import androidx.fragment.app.Fragment;

public abstract class ViewFragment extends Fragment
	implements OnUpdateLayout
{
	public void updateLayout()
	{
		updateLayout(null);
	}
}
