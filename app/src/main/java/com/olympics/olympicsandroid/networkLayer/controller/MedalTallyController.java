package com.olympics.olympicsandroid.networkLayer.controller;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.olympics.olympicsandroid.model.ErrorModel;
import com.olympics.olympicsandroid.model.IResponseModel;
import com.olympics.olympicsandroid.model.MedalTally;
import com.olympics.olympicsandroid.networkLayer.CustomXMLRequest;
import com.olympics.olympicsandroid.networkLayer.OlympicRequestQueries;
import com.olympics.olympicsandroid.networkLayer.RequestPolicy;
import com.olympics.olympicsandroid.networkLayer.VolleySingleton;
import com.olympics.olympicsandroid.networkLayer.cache.ICacheListener;
import com.olympics.olympicsandroid.networkLayer.cache.database.OlympicsPrefs;
import com.olympics.olympicsandroid.networkLayer.cache.file.DataCacheHelper;
import com.olympics.olympicsandroid.networkLayer.parse.IParseListener;
import com.olympics.olympicsandroid.networkLayer.parse.ParseTask;
import com.olympics.olympicsandroid.utility.DateUtils;
import com.olympics.olympicsandroid.utility.UtilityMethods;

import java.lang.ref.WeakReference;


/**
 * Created by sarnab.poddar on 7/8/16.
 */

public class MedalTallyController
{

    protected WeakReference<IUIListener> listenerWeakReference;
    protected Context mCtx;

    public MedalTallyController(WeakReference<IUIListener> listenerWeakReference, Context mCtx)
    {
        this.listenerWeakReference = listenerWeakReference;
        this.mCtx = mCtx;
    }

    public synchronized void  getMedalTallyData()
    {
//        if(DateUtils.isCurrentDateInOlympics()) {
//            //Retrieving medal tally data from server and cache
////            DataCacheHelper.getInstance().getDataModel(DataCacheHelper.CACHE_MEDALTALLY_MODEL,
////                    DataCacheHelper.CACHE_MEDALTALLY_KEY, createNewCacheListener());
//        }
        getLatestMedalDataFromServer();
    }

    private void getLatestMedalDataFromServer()
    {
        if(UtilityMethods.isConnectedToInternet()) {
            // Set Request Policy
            RequestPolicy requestPolicy = new RequestPolicy();
            requestPolicy.setForceCache(true);
            requestPolicy.setMaxAge(60 * 10);

            if (UtilityMethods.isSimulated) {
                String configString =
                        UtilityMethods.loadDataFromAsset(mCtx,
                                "medal_tally.xml");
                ParseTask parseTask = new ParseTask(MedalTally.class, configString, new IParseListener() {
                    @Override
                    public void onParseSuccess(Object responseModel) {
                        listenerWeakReference.get().onSuccess((IResponseModel) responseModel);
                    }

                    @Override
                    public void onParseFailure(ErrorModel errorModel) {
                        listenerWeakReference.get().onFailure(errorModel);
                    }
                }, ParseTask.XML_DATA);
                parseTask.startParsing();
            } else {

                CustomXMLRequest<MedalTally> medalTallyRequest = new CustomXMLRequest<MedalTally>
                        (OlympicRequestQueries.MEDAL_TALLY, MedalTally.class,
                                createSuccessListener(), createFailureListener(), requestPolicy);
                VolleySingleton.getInstance(null).addToRequestQueue(medalTallyRequest);
            }
        }
        else{
            ErrorModel errorModel = new ErrorModel();
            errorModel.setErrorCode(UtilityMethods.ERROR_INTERNET);
            errorModel.setErrorMessage(UtilityMethods.ERROR_INTERNET);
            listenerWeakReference.get().onFailure(errorModel);
        }
    }

    private ICacheListener createNewCacheListener() {
        return new ICacheListener() {
            @Override
            public void datafromCache(IResponseModel responseModel) {
                if(responseModel != null && (responseModel instanceof MedalTally))
                {
                    MedalTally medalTallyData = (MedalTally)responseModel;
                    listenerWeakReference.get().onSuccess(medalTallyData);
                }
            }
        };
    }

    protected Response.ErrorListener createFailureListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ErrorModel errorModel = new ErrorModel();
                errorModel.setErrorCode(error.getLocalizedMessage());
                errorModel.setErrorMessage(error.getMessage());
                listenerWeakReference.get().onFailure(errorModel);
            }
        };
    }

    protected Response.Listener<MedalTally> createSuccessListener() {
        return new Response.Listener<MedalTally>() {
            @Override
            public void onResponse(MedalTally response) {
                DataCacheHelper.getInstance().saveDataModel(DataCacheHelper.CACHE_MEDALTALLY_MODEL,response);
                OlympicsPrefs.getInstance(null).setMedalTallyRefreshTime(DateUtils.getCurrentTimeStamp());
                listenerWeakReference.get().onSuccess(response);
            }
        };
    }


}
