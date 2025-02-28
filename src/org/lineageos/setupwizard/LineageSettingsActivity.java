/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2017-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard;

import static org.lineageos.setupwizard.SetupWizardApp.DISABLE_NAV_KEYS;
import static org.lineageos.setupwizard.SetupWizardApp.KEY_SEND_METRICS;

import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import lineageos.hardware.LineageHardwareManager;
import lineageos.providers.LineageSettings;

public class LineageSettingsActivity extends BaseSetupWizardActivity {

    private SetupWizardApp mSetupWizardApp;

    private CheckBox mMetrics;
    private CheckBox mNavKeys;

    private boolean mSupportsKeyDisabler = false;

    private final View.OnClickListener mMetricsClickListener = view -> {
        boolean checked = !mMetrics.isChecked();
        mMetrics.setChecked(checked);
        mSetupWizardApp.getSettingsBundle().putBoolean(KEY_SEND_METRICS, checked);
    };

    private final View.OnClickListener mNavKeysClickListener = view -> {
        boolean checked = !mNavKeys.isChecked();
        mNavKeys.setChecked(checked);
        mSetupWizardApp.getSettingsBundle().putBoolean(DISABLE_NAV_KEYS, checked);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSetupWizardApp = (SetupWizardApp) getApplication();
        setNextText(R.string.next);

        String os_name = getString(R.string.aospb_os_name);
        String privacyPolicy = getString(R.string.aospb_services_pp_explanation, os_name);
        String privacyPolicyUri = getString(R.string.services_privacy_policy_uri);
        String policySummary = getString(R.string.services_find_privacy_policy, privacyPolicyUri);
        String servicesFullDescription = getString(R.string.services_full_description,
                privacyPolicy, policySummary);
        getGlifLayout().setDescriptionText(servicesFullDescription);

        View metricsRow = findViewById(R.id.metrics);
        metricsRow.setOnClickListener(mMetricsClickListener);
        metricsRow.requestFocus();
        String metricsHelpImproveLineage =
                getString(R.string.aospb_services_help_improve_cm, os_name);
        String metricsSummary = getString(R.string.aospb_services_metrics_label,
                metricsHelpImproveLineage, os_name, os_name);
        final SpannableStringBuilder metricsSpan = new SpannableStringBuilder(metricsSummary);
        metricsSpan.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                0, metricsHelpImproveLineage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView metrics = findViewById(R.id.enable_metrics_summary);
        metrics.setText(metricsSpan);
        mMetrics = findViewById(R.id.enable_metrics_checkbox);

        View navKeysRow = findViewById(R.id.nav_keys);
        navKeysRow.setOnClickListener(mNavKeysClickListener);
        mNavKeys = findViewById(R.id.nav_keys_checkbox);
        mSupportsKeyDisabler = isKeyDisablerSupported(this);
        if (mSupportsKeyDisabler) {
            mNavKeys.setChecked(LineageSettings.System.getIntForUser(getContentResolver(),
                    LineageSettings.System.FORCE_SHOW_NAVBAR, 0, UserHandle.USER_CURRENT) != 0);
        } else {
            navKeysRow.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDisableNavkeysOption();
        updateMetricsOption();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setup_lineage_settings;
    }

    @Override
    protected int getTitleResId() {
        return R.string.aospb_setup_services;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.ic_features;
    }

    private void updateMetricsOption() {
        final Bundle myPageBundle = mSetupWizardApp.getSettingsBundle();
        boolean metricsChecked =
                !myPageBundle.containsKey(KEY_SEND_METRICS) || myPageBundle
                        .getBoolean(KEY_SEND_METRICS);
        mMetrics.setChecked(metricsChecked);
        myPageBundle.putBoolean(KEY_SEND_METRICS, metricsChecked);
    }

    private void updateDisableNavkeysOption() {
        if (mSupportsKeyDisabler) {
            final Bundle myPageBundle = mSetupWizardApp.getSettingsBundle();
            boolean enabled = LineageSettings.System.getIntForUser(getContentResolver(),
                    LineageSettings.System.FORCE_SHOW_NAVBAR, 0, UserHandle.USER_CURRENT) != 0;
            boolean checked = myPageBundle.containsKey(DISABLE_NAV_KEYS) ?
                    myPageBundle.getBoolean(DISABLE_NAV_KEYS) :
                    enabled;
            mNavKeys.setChecked(checked);
            myPageBundle.putBoolean(DISABLE_NAV_KEYS, checked);
        }
    }

    private static boolean isKeyDisablerSupported(Context context) {
        final LineageHardwareManager hardware = LineageHardwareManager.getInstance(context);
        return hardware.isSupported(LineageHardwareManager.FEATURE_KEY_DISABLE);
    }
}
