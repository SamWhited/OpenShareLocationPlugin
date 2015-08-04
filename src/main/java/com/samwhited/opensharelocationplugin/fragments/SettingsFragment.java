package com.samwhited.opensharelocationplugin.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.samwhited.opensharelocationplugin.R;

public class SettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);
	}
}
