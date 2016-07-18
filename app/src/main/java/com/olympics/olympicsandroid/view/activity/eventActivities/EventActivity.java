package com.olympics.olympicsandroid.view.activity.eventActivities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.olympics.olympicsandroid.R;
import com.olympics.olympicsandroid.model.ErrorModel;
import com.olympics.olympicsandroid.model.IResponseModel;
import com.olympics.olympicsandroid.model.presentationModel.EventResultsViewModel;
import com.olympics.olympicsandroid.model.presentationModel.EventUnitModel;
import com.olympics.olympicsandroid.model.presentationModel.UnitResultsViewModel;
import com.olympics.olympicsandroid.networkLayer.cache.database.DBUnitStatusHelper;
import com.olympics.olympicsandroid.networkLayer.controller.EventResultsController;
import com.olympics.olympicsandroid.networkLayer.controller.IUIListener;
import com.olympics.olympicsandroid.utility.SportsUtility;
import com.olympics.olympicsandroid.view.fragment.ExpandableListAdapter;
import com.olympics.olympicsandroid.view.fragment.IItemClickListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sarnab.poddar on 7/16/16.
 */
public class EventActivity extends Activity implements IUIListener
{
    private String eventID;
    private String unitID;
    private String unitName;
    private String disciplineName;

    RecyclerView eventunitView;

    private EventResultsController eventResultsController;
    private LinearLayoutManager mLayoutManager;

    private EventListAdapter eventListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_results);

        eventListAdapter = new EventListAdapter(createItemClickListener());


        if(!TextUtils.isEmpty(getIntent().getStringExtra("event_id")))
        {
            eventID = getIntent().getStringExtra("event_id");
        }
        if(!TextUtils.isEmpty(getIntent().getStringExtra("event_unit_name")))
        {
            unitName = getIntent().getStringExtra("event_unit_name");
        }
        if(!TextUtils.isEmpty(getIntent().getStringExtra("discipline_name")))
        {
            disciplineName = getIntent().getStringExtra("discipline_name");
        }
        if(!TextUtils.isEmpty(getIntent().getStringExtra("event_unit_id")))
        {
            unitID = getIntent().getStringExtra("event_unit_id");
        }
        eventunitView = (RecyclerView)findViewById(R.id.event_list);

        mLayoutManager = new LinearLayoutManager(this);
        eventunitView.setPadding(10, 10, 10, 10);
        eventunitView.setAdapter(eventListAdapter);
        mLayoutManager.requestSimpleAnimationsInNextLayout();

        eventunitView.setLayoutManager(mLayoutManager);
        eventunitView.setHasFixedSize(true);

        handleUnit();
    }

    private IItemClickListener createItemClickListener() {
        return new IItemClickListener() {
            @Override
            public void handleItemClick(ExpandableListAdapter.Item itemClicked) {

            }
        };
    }

    private void handleUnit() {

        String unitStatus = EventUnitModel.UNIT_STATUS_CLOSED;

        if(!TextUtils.isEmpty(unitID))
        {
            DBUnitStatusHelper dbUnitStatusHelper = new DBUnitStatusHelper();
            unitStatus  = dbUnitStatusHelper.getStatusofUnit(unitID);
        }
//
//        if(!unitStatus.equalsIgnoreCase(EventUnitModel.UNIT_STATUS_CLOSED))
//        {
            if(eventResultsController == null)
            {
                eventResultsController = new EventResultsController(new WeakReference<IUIListener>(this),this);
            }
            eventResultsController.getEventResults(eventID);
//        }

    }

    @Override
    public void onSuccess(IResponseModel responseModel)
    {
        UnitResultsViewModel resultsViewModel = (UnitResultsViewModel) responseModel;

        List<EventListAdapter.Result> results = prepareData(resultsViewModel);

        eventListAdapter.updateData(results);
        eventListAdapter.notifyDataSetChanged();
    }

    private List<EventListAdapter.Result> prepareData(UnitResultsViewModel resultsViewModel)
    {
        if(resultsViewModel != null)
        {
            List<EventListAdapter.Result> results = new ArrayList<>();

            switch(resultsViewModel.getEventType())
            {
                case SportsUtility.TYPE_INDIVUDUAL:
                    for(EventResultsViewModel eventResultsViewModel:resultsViewModel.getEventResultsViewModels())
                    {
                        if(eventResultsViewModel != null && eventResultsViewModel.getCompetitorViewModelList() != null
                                && eventResultsViewModel.getCompetitorViewModelList().size() > 0) {
                            EventListAdapter.Result eventResult = new EventListAdapter.Result(EventListAdapter.TYPE_UNIT_HEADER, eventResultsViewModel);
                            results.add(eventResult);

                            for(EventResultsViewModel.CompetitorViewModel competitorViewModel:eventResultsViewModel.getCompetitorViewModelList())
                            {
                                EventListAdapter.Result eventCompetitorResult = new EventListAdapter.Result(EventListAdapter.TYPE_IND_COMPETITOR, competitorViewModel);
                                results.add(eventCompetitorResult);
                            }

                        }
                    }
                    break;

                case SportsUtility.TYPE_TEAM:
                    for(EventResultsViewModel eventResultsViewModel:resultsViewModel.getEventResultsViewModels())
                    {
                        if(eventResultsViewModel != null && eventResultsViewModel.getCompetitorViewModelList() != null
                                && eventResultsViewModel.getCompetitorViewModelList().size() > 0) {
                            EventListAdapter.Result eventResult = new EventListAdapter.Result(EventListAdapter.TYPE_UNIT_HEADER, eventResultsViewModel);
                            results.add(eventResult);

                            for(EventResultsViewModel.CompetitorViewModel competitorViewModel:eventResultsViewModel.getCompetitorViewModelList())
                            {
                                EventListAdapter.Result eventCompetitorResult = new EventListAdapter.Result(EventListAdapter.TYPE_TEAM_COMPETITOR, competitorViewModel);
                                results.add(eventCompetitorResult);
                            }

                        }
                    }
                    break;

                case SportsUtility.TYPE_INDIVUDUAL_HEAD2HEAD:

                    String unitName = null;

                    for(EventResultsViewModel eventResultsViewModel:resultsViewModel.getEventResultsViewModels())
                    {
                        if(unitName == null || !unitName.equalsIgnoreCase(eventResultsViewModel.getUnit_name()))
                        {
                            EventListAdapter.Result eventResult = new EventListAdapter.Result(EventListAdapter.TYPE_UNIT_HEADER, eventResultsViewModel);
                            results.add(eventResult);
                        }

                        EventListAdapter.Result competh2hResult = new EventListAdapter.Result(EventListAdapter.TYPE_IND_HEAD2HEAD_COMPET,eventResultsViewModel.getCompetitorH2HViewModel());
                        results.add(competh2hResult);
                    }
                    break;
                case SportsUtility.TYPE_TEAM_HEAD2HEAD:

                    String teamUnitName = null;

                    for(EventResultsViewModel eventResultsViewModel:resultsViewModel.getEventResultsViewModels())
                    {
                        if(teamUnitName == null || !teamUnitName.equalsIgnoreCase(eventResultsViewModel.getUnit_name()))
                        {
                            EventListAdapter.Result eventResult = new EventListAdapter.Result(EventListAdapter.TYPE_UNIT_HEADER, eventResultsViewModel);
                            results.add(eventResult);
                        }

                        EventListAdapter.Result competh2hResult = new EventListAdapter.Result(EventListAdapter.TYPE_TEAM_HEAD2HEAD_COMPET,eventResultsViewModel.getCompetitorH2HViewModel());
                        results.add(competh2hResult);
                    }
                    break;
            }

            return results;
        }

        return null;
    }

    @Override
    public void onFailure(ErrorModel errorModel) {

    }

    @Override
    public void handleLoadingIndicator(boolean showLoadingInd) {

    }
}
