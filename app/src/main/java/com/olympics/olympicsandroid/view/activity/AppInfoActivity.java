package com.olympics.olympicsandroid.view.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.olympics.olympicsandroid.R;

public class AppInfoActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String SHARE_APP_MESSAGE = "Hey check out olympics app at: ";
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

        //Setup Action bar
        setActionBar();
    }

    private void setActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
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
