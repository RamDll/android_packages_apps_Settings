/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.deviceinfo;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.DeviceInfoUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class HardwareInfoPreferenceController extends BasePreferenceController {

    private static final String TAG = "DeviceModelPrefCtrl";

    public HardwareInfoPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
    }

    @Override
    public int getAvailabilityStatus() {
        return mContext.getResources().getBoolean(R.bool.config_show_device_model)
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public CharSequence getSummary() {
        return getDeviceModel();
    }

    public static String getDeviceModel() {
        final String model = getModelName();
        FutureTask<String> msvSuffixTask = new FutureTask<>(() -> DeviceInfoUtils.getMsvSuffix());

        msvSuffixTask.run();
        try {
            // Wait for msv suffix value.
            final String msvSuffix = msvSuffixTask.get();
            return model + msvSuffix;
        } catch (ExecutionException e) {
            Log.e(TAG, "Execution error, so we only show model name");
        } catch (InterruptedException e) {
            Log.e(TAG, "Interruption error, so we only show model name");
        }
        // If we can't get an msv suffix value successfully,
        // it's better to return model name.
        return model;
    }

    // Marketing name with the model number, e.g. "POCO M6 Pro (2312FPCA6G)".
    private static String getModelName() {
        final String marketName = SystemProperties.get("ro.product.marketname");
        if (TextUtils.isEmpty(marketName) || marketName.equals(Build.MODEL)) {
            return Build.MODEL;
        }
        return marketName + " (" + Build.MODEL + ")";
    }
}
