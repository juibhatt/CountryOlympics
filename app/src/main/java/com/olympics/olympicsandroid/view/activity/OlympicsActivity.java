package com.olympics.olympicsandroid.view.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.olympics.olympicsandroid.OlympicsApplication;
import com.olympics.olympicsandroid.R;
import com.olympics.olympicsandroid.model.ErrorModel;
import com.olympics.olympicsandroid.model.IResponseModel;
import com.olympics.olympicsandroid.model.MedalTally;
import com.olympics.olympicsandroid.model.MedalTallyOrganization;
import com.olympics.olympicsandroid.model.Organization;
import com.olympics.olympicsandroid.model.presentationModel.CountryEventUnitModel;
import com.olympics.olympicsandroid.networkLayer.cache.database.OlympicsPrefs;
import com.olympics.olympicsandroid.networkLayer.controller.CountryScheduleController;
import com.olympics.olympicsandroid.networkLayer.controller.IUIListener;
import com.olympics.olympicsandroid.networkLayer.controller.MedalTallyController;
import com.olympics.olympicsandroid.utility.MedalTallyComparator;
import com.olympics.olympicsandroid.utility.UtilityMethods;
import com.olympics.olympicsandroid.view.activity.factory.ActivityFactory;
import com.olympics.olympicsandroid.view.fragment.DateEventAdapter;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

public class OlympicsActivity extends AppCompatActivity implements NavigationView
        .OnNavigationItemSelectedListener, IUIListener {


    private DateEventAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private static int REQUEST_CODE_COUNTRY = 1;

    private MedalTally medalTallyObj;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_olympics);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string
                .navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setUpData();
    }

    private void setUpData()
    {
        //Display country information in navigation drawer
        displaySelectedCountryInfo();

        CountryScheduleController scheduleController = new CountryScheduleController(new
                WeakReference<IUIListener>(this), getApplication());
        scheduleController.getCountryDetails();

        //Call MedalTally API through controller
        //Request for MedalTally Data through controller
        MedalTallyController medalTallyController = new MedalTallyController(new
                WeakReference<IUIListener>(this), getApplication());
        medalTallyController.getMedalTallyData();
   }

    /**
     * This method displays country information in navigation drawer
     */
    private void displaySelectedCountryInfo() {

        //get selected country data from preference
        Organization countryInfo = OlympicsPrefs.getInstance(this).getUserSelectedCountry();

        View headerLayout = ((NavigationView)findViewById(R.id.nav_view)).getHeaderView(0);

        if (headerLayout != null) {
            headerLayout.findViewById(R.id.countryTextView).setOnClickListener(new View
                    .OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(OlympicsActivity.this,CountrySelectionActivity.class));
                }
            });

            //Setup Selected Country Name
            TextView countryNameTxt = (TextView) headerLayout.findViewById(R.id.countryTextView);
            if (countryNameTxt != null && !TextUtils.isEmpty(countryInfo.getDescription())) {
                countryNameTxt.setText(countryInfo.getDescription());
            }

            //Setup Selected Country Image
            ImageView countryImageView = (ImageView) headerLayout.findViewById(R.id.countryImageView);

            int rid = getResources().getIdentifier(countryInfo.getAlias().toLowerCase(), "raw", getPackageName());
            try {
                countryImageView.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(rid)));
                countryImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.putExtra("first_launch",false);
                        ActivityFactory.openCountrySelectionScreenForResult(OlympicsActivity.this, intent,REQUEST_CODE_COUNTRY);
                    }
                });
            } catch (Exception ex) {
                System.out.println("Exeptipn == " + ex);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_COUNTRY)
        {
            if(resultCode == RESULT_OK)
            {
                String countryCode =data.getStringExtra("COUNTRY_CODE");
                setUpData();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.nav_medal:
                ActivityFactory.openMedalActivity(this, null);
                break;
            case R.id.nav_athletes:
                ActivityFactory.openAthleteActivity(this, null);
                break;
            case R.id.nav_info:
                ActivityFactory.openAppInfoActivity(this);
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSuccess(IResponseModel responseModel) {
        if (responseModel instanceof CountryEventUnitModel) {
            if(mSectionsPagerAdapter == null) {
                mSectionsPagerAdapter = new DateEventAdapter(getSupportFragmentManager(),
                        (CountryEventUnitModel) responseModel);

                // Set up the ViewPager with the sections adapter.
                mViewPager = (ViewPager) findViewById(R.id.container);
                mViewPager.setAdapter(mSectionsPagerAdapter);

                TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
                tabLayout.setupWithViewPager(mViewPager);
            }
            else{
                if(mSectionsPagerAdapter != null)
                mSectionsPagerAdapter.updateModel((CountryEventUnitModel) responseModel);
                mSectionsPagerAdapter.notifyDataSetChanged();
                mViewPager.invalidate();
            }
        } else if (responseModel instanceof MedalTally)
        {
            //Display Medal info
            medalTallyObj = (MedalTally) responseModel;
            displayMedalInfoForCountry();
        }
    }

    /**
     * This method is responsible for displaying selected country's medal tally.
     */
    private void displayMedalInfoForCountry() {

        if (medalTallyObj != null && medalTallyObj.getOrganization() != null && !medalTallyObj
                .getOrganization().isEmpty()) {

            List<MedalTallyOrganization> medaltallyOrgList = medalTallyObj.getOrganization();

            Collections.sort(medaltallyOrgList, new MedalTallyComparator(OlympicsPrefs.getInstance(null)
                    .getUserSelectedCountry()));

            View headerLayout = ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);

            if (headerLayout != null) {
                if (!TextUtils.isEmpty(medaltallyOrgList.get(0).getGold())) {
                    ((TextView) headerLayout.findViewById(R.id.gold_medal_count)).setText(medaltallyOrgList.get(0).getGold());
                }
                if (!TextUtils.isEmpty(medaltallyOrgList.get(0).getSilver())) {
                    ((TextView) headerLayout.findViewById(R.id.silver_medal_count)).setText
                            (medaltallyOrgList.get(0).getSilver());
                }
                if (!TextUtils.isEmpty(medaltallyOrgList.get(0).getBronze())) {
                    ((TextView) headerLayout.findViewById(R.id.bronze_medal_count)).setText(medaltallyOrgList
                            .get
                            (0).getBronze());
                }
            }
        }
    }

    @Override
    public void onFailure(ErrorModel errorModel)
    {
        if(errorModel != null && !TextUtils.isEmpty(errorModel.getErrorCode()) && errorModel.getErrorCode().equalsIgnoreCase(UtilityMethods.ERROR_INTERNET))
        {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setMessage(getString(R.string.id_error_internet));
            dlgAlert.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                    setUpData();
                }
            });
            dlgAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            });
            dlgAlert.setCancelable(false);
            dlgAlert.create().show();

            Bundle params = new Bundle();
            params.putString("app_error", "event_result");
            params.putString("app_screen", "country_schedule");

            params.putString("error_reason", errorModel.getErrorMessage());
            FirebaseAnalytics.getInstance(OlympicsApplication.getAppContext()).logEvent("error", params);

        }
        else{
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setMessage(getString(R.string.id_error_server));
            dlgAlert.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                    setUpData();
                }
            });
            dlgAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            });
            dlgAlert.setCancelable(false);
            dlgAlert.create().show();

            Bundle params = new Bundle();
            params.putString("app_error", "event_result");
            params.putString("app_screen", "country_schedule");

            if(errorModel != null && !TextUtils.isEmpty(errorModel.getErrorMessage())) {
                params.putString("error_reason", errorModel.getErrorMessage());
            }
            else{
                params.putString("error_reason", "generic_error");
            }
            FirebaseAnalytics.getInstance(OlympicsApplication.getAppContext()).logEvent("error", params);
        }

    }

    @Override
    public void handleLoadingIndicator(boolean showLoadingInd)
    {
        if(showLoadingInd)
        {
            progress = new ProgressDialog(this);
            progress.setTitle("Loading");
            progress.setMessage("Wait while loading country details...");
            progress.show();

        }
        else{
            if(progress != null) {
                progress.dismiss();
            }
        }
    }
}
