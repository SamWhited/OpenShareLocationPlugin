package com.samwhited.opensharelocationplugin.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;

import com.samwhited.opensharelocationplugin.fragments.SettingsFragment;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final FragmentManager fm = getFragmentManager();
		SettingsFragment mSettingsFragment = (SettingsFragment) fm.findFragmentById(android.R.id.content);
		if (mSettingsFragment == null || !mSettingsFragment.getClass().equals(SettingsFragment.class)) {
			mSettingsFragment = new SettingsFragment();
			fm.beginTransaction().replace(android.R.id.content, mSettingsFragment).commit();
		}
	}
}
