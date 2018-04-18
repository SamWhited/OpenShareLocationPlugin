package com.samwhited.opensharelocationplugin.preferences;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.preference.Preference;
import android.util.AttributeSet;

import com.samwhited.opensharelocationplugin.R;
import com.samwhited.opensharelocationplugin.activities.AboutActivity;

public class AboutPreference extends Preference {
    @SuppressWarnings("unused")
    public AboutPreference(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        setSummary();
    }

    @SuppressWarnings("unused")
    public AboutPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setSummary();
    }

    @Override
    protected void onClick() {
        super.onClick();
        final Intent intent = new Intent(getContext(), AboutActivity.class);
        getContext().startActivity(intent);
    }

    public String getVersion() {
        final Context context = getContext();
        final String packageName = context == null ? null : context.getPackageName();
        if (packageName != null) {
            try {
                return context.getPackageManager().getPackageInfo(packageName, 0).versionName;
            } catch (final PackageManager.NameNotFoundException e) {
                return "";
            }
        } else {
            return "";
        }
    }

    private void setSummary() {
        setSummary(getContext().getResources().getString(R.string.app_name) + " " + getVersion());
    }
}

