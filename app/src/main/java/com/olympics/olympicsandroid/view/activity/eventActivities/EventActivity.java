package com.olympics.olympicsandroid.view.activity.eventActivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.olympics.olympicsandroid.OlympicsApplication;
import com.olympics.olympicsandroid.R;
import com.olympics.olympicsandroid.model.ErrorModel;
import com.olympics.olympicsandroid.model.IResponseModel;
import com.olympics.olympicsandroid.model.presentationModel.EventResultsViewModel;
import com.olympics.olympicsandroid.model.presentationModel.EventUnitModel;
import com.olympics.olympicsandroid.model.presentationModel.UnitResultsViewModel;
import com.olympics.olympicsandroid.networkLayer.cache.database.DBUnitStatusHelper;
import com.olympics.olympicsandroid.networkLayer.controller.EventResultsController;
import com.olympics.olympicsandroid.networkLayer.controller.IUIListener;
import com.olympics.olympicsandroid.utility.DateUtils;
import com.olympics.olympicsandroid.utility.SportsUtility;
import com.olympics.olympicsandroid.utility.UtilityMethods;
import com.olympics.olympicsandroid.view.fragment.IScheduleListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sarnab.poddar on 7/16/16.
 */
public class EventActivity extends AppCompatActivity implements IUIListener {
    private String eventID;
    private String unitID;
    private String unitName;
    private String disciplineName;

    RecyclerView eventunitView;

    private EventResultsController eventResultsController;
    private LinearLayoutManager mLayoutManager;

    private EventListAdapter eventListAdapter;
    private CircleProgressBar mProgressView;
    private TextView mNoItemsView;
    private SwipeRefreshLayout event_refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_results);

        eventListAdapter = new EventListAdapter(createItemClickListener());


        if (!TextUtils.isEmpty(getIntent().getStringExtra("event_id"))) {
            eventID = getIntent().getStringExtra("event_id");
        }
        if (!TextUtils.isEmpty(getIntent().getStringExtra("event_unit_name"))) {
            unitName = getIntent().getStringExtra("event_unit_name");
        }
        if (!TextUtils.isEmpty(getIntent().getStringExtra("discipline_name"))) {
            disciplineName = getIntent().getStringExtra("discipline_name");
        }
        if (!TextUtils.isEmpty(getIntent().getStringExtra("event_unit_id"))) {
            unitID = getIntent().getStringExtra("event_unit_id");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(disciplineName);
        toolbar.setNavigationOnClickListener(navigationClickListener);


        mProgressView = (CircleProgressBar)findViewById(R.id.task_progress);
        mNoItemsView = (TextView)findViewById(R.id.tv_no_units);


        eventunitView = (RecyclerView) findViewById(R.id.eventlist_recycler_view);

        event_refresh = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        event_refresh.setColorSchemeColors(R.color.colorPrimary,R.color.AthletecolorPrimary,R.color.SchedulecolorPrimary);
        event_refresh.setOnRefreshListener(createRefreshListener());

        mLayoutManager = new LinearLayoutManager(this);
        eventunitView.setPadding(10, 10, 10, 10);
        eventunitView.setAdapter(eventListAdapter);
        mLayoutManager.requestSimpleAnimationsInNextLayout();

        eventunitView.setLayoutManager(mLayoutManager);
        eventunitView.setHasFixedSize(true);

        Bundle params = new Bundle();
        params.putString("user_refresh", "false");
        params.putString("event_id", eventID);
        params.putString("event_unit_name", unitName);
        FirebaseAnalytics.getInstance(OlympicsApplication.getAppContext()).logEvent("refresh", params);

        handleUnit();
    }

    private IScheduleListener createItemClickListener() {
        return new IScheduleListener() {
            @Override
            public void handleItemClick(EventResultsViewModel itemClicked) {
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra(CalendarContract.Events.TITLE, itemClicked.getUnit_name());
                intent.putExtra(CalendarContract.Events.DESCRIPTION, itemClicked.getUnit_name());
                intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "");
                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, DateUtils.getUnitDateWithTime(itemClicked.getStart_date()));
                intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, itemClicked.getStart_date() + 60*60*15);
                intent.putExtra(CalendarContract.Events.ALL_DAY, false);
                intent.putExtra(CalendarContract.Events.STATUS, 1);
                intent.putExtra(CalendarContract.Events.VISIBLE, 0);
                intent.putExtra(CalendarContract.Events.HAS_ALARM, 1);
                startActivity(intent);
            }
        };
    }

    private SwipeRefreshLayout.OnRefreshListener createRefreshListener() {
        return new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                Bundle params = new Bundle();
                params.putString("user_refresh", "true");
                params.putString("event_id", eventID);
                params.putString("event_unit_name", unitName);
                FirebaseAnalytics.getInstance(OlympicsApplication.getAppContext()).logEvent("refresh", params);


                handleUnit();
            }
        };
    }

    private View.OnClickListener navigationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
           finish();
        }
    };


    private void handleUnit() {

        String unitStatus = EventUnitModel.UNIT_STATUS_CLOSED;

        if (!TextUtils.isEmpty(unitID)) {
            DBUnitStatusHelper dbUnitStatusHelper = new DBUnitStatusHelper();
            unitStatus = dbUnitStatusHelper.getStatusofUnit(unitID);
        }
//
//        if(!unitStatus.equalsIgnoreCase(EventUnitModel.UNIT_STATUS_CLOSED))
//        {
        if (eventResultsController == null) {
            eventResultsController = new EventResultsController(new WeakReference<IUIListener>(this), this);
        }
        eventResultsController.getEventResults(eventID);
//        }

    }

    @Override
    public void onSuccess(IResponseModel responseModel) {
        UnitResultsViewModel resultsViewModel = (UnitResultsViewModel) responseModel;

        List<EventListAdapter.Result> results = prepareData(resultsViewModel);

        if(results == null || results.size() == 0)
        {
            mNoItemsView.setVisibility(View.VISIBLE);


            if(!DateUtils.isCurrentDateInOlympics())
            {
                mNoItemsView.setText(getString(R.string.list_tasks_olymperror_msg));
            }
        }
        else{
            mNoItemsView.setVisibility(View.GONE);
        }

        eventListAdapter.updateData(results);
        eventListAdapter.notifyDataSetChanged();
    }

    private List<EventListAdapter.Result> prepareData(UnitResultsViewModel resultsViewModel) {
        if (resultsViewModel != null) {
            List<EventListAdapter.Result> results = new ArrayList<>();

            switch (resultsViewModel.getEventType()) {
                case SportsUtility.TYPE_INDIVUDUAL:
                    for (EventResultsViewModel eventResultsViewModel : resultsViewModel.getEventResultsViewModels()) {
                        if (eventResultsViewModel != null && eventResultsViewModel.getCompetitorViewModelList() != null
                                && eventResultsViewModel.getCompetitorViewModelList().size() > 0) {
                            EventListAdapter.Result eventResult = new EventListAdapter.Result(EventListAdapter.TYPE_UNIT_HEADER, eventResultsViewModel);
                            results.add(eventResult);

                            for (EventResultsViewModel.CompetitorViewModel competitorViewModel : eventResultsViewModel.getCompetitorViewModelList()) {
                                EventListAdapter.Result eventCompetitorResult = new EventListAdapter.Result(EventListAdapter.TYPE_IND_COMPETITOR, competitorViewModel);
                                results.add(eventCompetitorResult);
                            }

                        }
                    }
                    break;

                case SportsUtility.TYPE_TEAM:
                    for (EventResultsViewModel eventResultsViewModel : resultsViewModel.getEventResultsViewModels()) {
                        if (eventResultsViewModel != null && eventResultsViewModel.getCompetitorViewModelList() != null
                                && eventResultsViewModel.getCompetitorViewModelList().size() > 0) {
                            EventListAdapter.Result eventResult = new EventListAdapter.Result(EventListAdapter.TYPE_UNIT_HEADER, eventResultsViewModel);
                            results.add(eventResult);

                            for (EventResultsViewModel.CompetitorViewModel competitorViewModel : eventResultsViewModel.getCompetitorViewModelList()) {
                                EventListAdapter.Result eventCompetitorResult = new EventListAdapter.Result(EventListAdapter.TYPE_TEAM_COMPETITOR, competitorViewModel);
                                results.add(eventCompetitorResult);
                            }

                        }
                    }
                    break;

                case SportsUtility.TYPE_INDIVUDUAL_HEAD2HEAD:

                    String unitName = null;

                    for (EventResultsViewModel eventResultsViewModel : resultsViewModel.getEventResultsViewModels()) {
                        if (unitName == null || !unitName.equalsIgnoreCase(eventResultsViewModel.getUnit_name())) {
                            EventListAdapter.Result eventResult = new EventListAdapter.Result(EventListAdapter.TYPE_UNIT_HEADER, eventResultsViewModel);
                            results.add(eventResult);
                        }
                        unitName = eventResultsViewModel.getUnit_name();
                        EventListAdapter.Result competh2hResult = new EventListAdapter.Result(EventListAdapter.TYPE_IND_HEAD2HEAD_COMPET, eventResultsViewModel.getCompetitorH2HViewModel());
                        results.add(competh2hResult);
                    }
                    break;
                case SportsUtility.TYPE_TEAM_HEAD2HEAD:

                    String teamUnitName = null;

                    for (EventResultsViewModel eventResultsViewModel : resultsViewModel.getEventResultsViewModels()) {
                        if (teamUnitName == null || !teamUnitName.equalsIgnoreCase(eventResultsViewModel.getUnit_name())) {
                            EventListAdapter.Result eventResult = new EventListAdapter.Result(EventListAdapter.TYPE_UNIT_HEADER, eventResultsViewModel);
                            results.add(eventResult);
                        }
                        teamUnitName = eventResultsViewModel.getUnit_name();
                        EventListAdapter.Result competh2hResult = new EventListAdapter.Result(EventListAdapter.TYPE_TEAM_HEAD2HEAD_COMPET, eventResultsViewModel.getCompetitorH2HViewModel());
                        results.add(competh2hResult);
                    }
                    break;

                case SportsUtility.TYPE_MIXED:

                    String uniqueUnitName = null;

                    for (EventResultsViewModel eventResultsViewModel : resultsViewModel.getEventResultsViewModels()) {
                        switch (eventResultsViewModel.getUnit_type()) {

                            case SportsUtility.TYPE_INDIVUDUAL:
                                if (eventResultsViewModel != null && eventResultsViewModel.getCompetitorViewModelList() != null
                                        && eventResultsViewModel.getCompetitorViewModelList().size() > 0) {
                                    EventListAdapter.Result eventResult = new EventListAdapter.Result(EventListAdapter.TYPE_UNIT_HEADER, eventResultsViewModel);
                                    results.add(eventResult);

                                    for (EventResultsViewModel.CompetitorViewModel competitorViewModel : eventResultsViewModel.getCompetitorViewModelList()) {
                                        EventListAdapter.Result eventCompetitorResult = new EventListAdapter.Result(EventListAdapter.TYPE_IND_COMPETITOR, competitorViewModel);
                                        results.add(eventCompetitorResult);
                                    }

                                }
                                break;

                            case SportsUtility.TYPE_TEAM:
                                if (eventResultsViewModel != null && eventResultsViewModel.getCompetitorViewModelList() != null
                                        && eventResultsViewModel.getCompetitorViewModelList().size() > 0) {
                                    EventListAdapter.Result eventResult = new EventListAdapter.Result(EventListAdapter.TYPE_UNIT_HEADER, eventResultsViewModel);
                                    results.add(eventResult);

                                    for (EventResultsViewModel.CompetitorViewModel competitorViewModel : eventResultsViewModel.getCompetitorViewModelList()) {
                                        EventListAdapter.Result eventCompetitorResult = new EventListAdapter.Result(EventListAdapter.TYPE_TEAM_COMPETITOR, competitorViewModel);
                                        results.add(eventCompetitorResult);
                                    }

                                }
                                break;

                            case SportsUtility.TYPE_INDIVUDUAL_HEAD2HEAD:

                                if (uniqueUnitName == null || !uniqueUnitName.equalsIgnoreCase(eventResultsViewModel.getUnit_name())) {
                                    EventListAdapter.Result eventResult = new EventListAdapter.Result(EventListAdapter.TYPE_UNIT_HEADER, eventResultsViewModel);
                                    results.add(eventResult);
                                }
                                uniqueUnitName = eventResultsViewModel.getUnit_name();
                                EventListAdapter.Result competh2hResult = new EventListAdapter.Result(EventListAdapter.TYPE_IND_HEAD2HEAD_COMPET, eventResultsViewModel.getCompetitorH2HViewModel());
                                results.add(competh2hResult);
                                break;

                            case SportsUtility.TYPE_TEAM_HEAD2HEAD:


                                if (uniqueUnitName == null || !uniqueUnitName.equalsIgnoreCase(eventResultsViewModel.getUnit_name())) {
                                    EventListAdapter.Result eventResult = new EventListAdapter.Result(EventListAdapter.TYPE_UNIT_HEADER, eventResultsViewModel);
                                    results.add(eventResult);
                                }
                                uniqueUnitName = eventResultsViewModel.getUnit_name();
                                EventListAdapter.Result competteamh2hResult = new EventListAdapter.Result(EventListAdapter.TYPE_TEAM_HEAD2HEAD_COMPET, eventResultsViewModel.getCompetitorH2HViewModel());
                                results.add(competteamh2hResult);
                                break;
                        }
                    }


                    break;
            }

            return results;
        }

        return null;
    }

    @Override
    public void onFailure(ErrorModel errorModel) {
        mNoItemsView.setVisibility(View.VISIBLE);


        if(errorModel != null && !TextUtils.isEmpty(errorModel.getErrorCode()) && errorModel.getErrorCode().equalsIgnoreCase(UtilityMethods.ERROR_INTERNET))
        {

            Bundle params = new Bundle();
            params.putString("app_error", "event_result");
            params.putString("app_screen","event_result");
            params.putString("event_id", eventID);
            params.putString("event_unit_name", unitName);
            params.putString("error_reason", errorModel.getErrorMessage());
            FirebaseAnalytics.getInstance(OlympicsApplication.getAppContext()).logEvent("error", params);


            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setMessage(getString(R.string.id_error_internet));
            dlgAlert.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                    handleUnit();
                }
            });
            dlgAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            });
            dlgAlert.setCancelable(false);
            dlgAlert.create().show();
        }
        else{

            Bundle params = new Bundle();
            params.putString("app_error", "event_result");
            params.putString("event_id", eventID);
            params.putString("event_unit_name", unitName);
            if(errorModel != null && !TextUtils.isEmpty(errorModel.getErrorMessage())) {
                params.putString("error_reason", errorModel.getErrorMessage());
            }
            else{
                params.putString("error_reason", "generic_error");
            }
            FirebaseAnalytics.getInstance(OlympicsApplication.getAppContext()).logEvent("error", params);


            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setMessage(getString(R.string.id_error_server));
            dlgAlert.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                    handleUnit();
                }
            });
            dlgAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            });
            dlgAlert.setCancelable(false);
            dlgAlert.create().show();
        }
    }

    @Override
    public void handleLoadingIndicator(boolean showLoadingInd)
    {
        if(showLoadingInd)
        {
            mProgressView.setVisibility(View.VISIBLE);
        }
        else{
            mProgressView.setVisibility(View.GONE);
        }
        event_refresh.setRefreshing(false);

    }
}
