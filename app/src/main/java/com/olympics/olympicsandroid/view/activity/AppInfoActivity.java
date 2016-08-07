package com.olympics.olympicsandroid.view.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.olympics.olympicsandroid.R;
import com.olympics.olympicsandroid.networkLayer.cache.database.OlympicsPrefs;

public class AppInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String SHARE_APP_MESSAGE = "Olympics is around the corner! Check out this App for live updates.";
    private static final String PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=";
    private static final String CONTACT_US_MAIL = "mailto:jksolympics@gmail.com";
    private static final String SHARE_CONTACT_INTENT_TYPE = "text/plain";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);

        //Setup onclick listener
        findViewById(R.id.contact_us_view).setOnClickListener(this);
        findViewById(R.id.rate_app_view).setOnClickListener(this);
        findViewById(R.id.share_app_view).setOnClickListener(this);

        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_unit_id));

        //Setup Action bar
        setActionBar();

        setupAds();

        setUpVersion();

        setUpSettings();
    }

    private void setUpSettings() {
        ToggleButton toggle = (ToggleButton) findViewById(R.id.id_time_settings);
        toggle.setChecked(OlympicsPrefs.getInstance(null).getIsUnit24Hour());
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    OlympicsPrefs.getInstance(null).setIsUnit24Hour(true);
                } else {
                    OlympicsPrefs.getInstance(null).setIsUnit24Hour(false);
                }
            }
        });
    }

    private void setUpVersion() {
        TextView versionText = (TextView) findViewById(R.id.id_app_version);

        try {
            StringBuilder version = new StringBuilder(getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
            version.append(".");
            version.append(getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode);
            if (version != null && version.length() > 0) {
                versionText.setText(version);
                versionText.setVisibility(View.VISIBLE);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionText.setVisibility(View.GONE);
        }

    }

    private void setupAds() {
        if (true)//OlympicsPrefs.getInstance(null).getIsAdEnabled())
        {
            AdView mAdView = (AdView) findViewById(R.id.ad_view);
            AdRequest adRequest = new AdRequest.Builder().addTestDevice("D800AADBD8B5AC6C27736D495B83EB21").build();
            mAdView.loadAd(adRequest);
        }
    }

    private void setActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View view) {
        if (view != null) {
            switch (view.getId()) {
                case R.id.contact_us_view:
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setType(SHARE_CONTACT_INTENT_TYPE);
                    intent.setData(Uri.parse(CONTACT_US_MAIL));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);


                    break;
                case R.id.share_app_view:
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT,
                            SHARE_APP_MESSAGE + Uri.parse(PLAY_STORE_LINK +
                                    getPackageName()));
                    sendIntent.setType(SHARE_CONTACT_INTENT_TYPE);
                    startActivity(sendIntent);
                    break;
                case R.id.rate_app_view:
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(PLAY_STORE_LINK +
                            getPackageName()));
                    startActivity(intent);
                    break;
            }
        }

    }
}
