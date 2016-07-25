package com.olympics.olympicsandroid;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.olympics.olympicsandroid.model.AppVersionData;
import com.olympics.olympicsandroid.model.ErrorModel;
import com.olympics.olympicsandroid.networkLayer.cache.database.OlympicsPrefs;
import com.olympics.olympicsandroid.networkLayer.cache.file.DataCacheHelper;
import com.olympics.olympicsandroid.networkLayer.controller.AppVersionController;
import com.olympics.olympicsandroid.networkLayer.controller.IConfigListener;

/**
 * Created by sarnab.poddar on 7/24/16.
 */
public class OlympicsLifeCyclecallbacks  implements Application.ActivityLifecycleCallbacks {


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        ++resumed;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        ++paused;
        android.util.Log.w("test", "application is in foreground: " + (resumed > paused));
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if(started ==  stopped)
        {
            FirebaseAnalytics.getInstance(OlympicsApplication.getAppContext()).logEvent(FirebaseAnalytics.Event.APP_OPEN,null);
        }

        AppVersionController appVersionController = new AppVersionController();
        appVersionController.getAppConfiguration(createNewIconfligListener());
        ++started;
    }

    private IConfigListener createNewIconfligListener() {
        return new IConfigListener() {
            @Override
            public void onConfigSuccess(AppVersionData appVersionData) {
                if(appVersionData != null && !TextUtils.isEmpty(appVersionData.getCacheCountryChecksum()))
                {
                    if(TextUtils.isEmpty(OlympicsPrefs.getInstance(null).getCacheChecksum()))
                    {
                        OlympicsPrefs.getInstance(null).setCacheChecksum(appVersionData.getCacheCountryChecksum());
                    }
                    else if (!OlympicsPrefs.getInstance(null).getCacheChecksum().equalsIgnoreCase(appVersionData.getCacheCountryChecksum()))
                    {
                        OlympicsPrefs.getInstance(null).setCacheChecksum(appVersionData.getCacheCountryChecksum());
                        DataCacheHelper.getInstance().getDataModel(DataCacheHelper.CACHE_COUNTRY_MODEL, null, null, true);
                    }
                }
            }

            @Override
            public void onConfigFailure(ErrorModel errorModel) {

            }
        };
    }

    @Override
    public void onActivityStopped(Activity activity) {
        ++stopped;
        android.util.Log.w("test", "application is visible: " + (started > stopped));
    }

    // If you want a static function you can use to check if your application is
    // foreground/background, you can use the following:
    // Replace the four variables above with these four
    private static int resumed;
    private static int paused;
    private static int started;
    private static int stopped;

    // And these two public static functions
    public static boolean isApplicationVisible() {
        return started > stopped;
    }

    public static boolean isApplicationInForeground() {
        return resumed > paused;
    }
}
