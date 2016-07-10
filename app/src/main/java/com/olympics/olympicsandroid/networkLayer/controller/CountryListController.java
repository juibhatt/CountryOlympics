package com.olympics.olympicsandroid.networkLayer.controller;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.olympics.olympicsandroid.model.CountryModel;
import com.olympics.olympicsandroid.model.ErrorModel;
import com.olympics.olympicsandroid.model.IResponseModel;
import com.olympics.olympicsandroid.networkLayer.CustomXMLRequest;
import com.olympics.olympicsandroid.networkLayer.OlympicRequestQueries;
import com.olympics.olympicsandroid.networkLayer.RequestPolicy;
import com.olympics.olympicsandroid.networkLayer.VolleySingleton;
import com.olympics.olympicsandroid.networkLayer.parse.IParseListener;
import com.olympics.olympicsandroid.networkLayer.parse.ParseTask;
import com.olympics.olympicsandroid.utility.UtilityMethods;

import java.lang.ref.WeakReference;

/**
 * Created by sarnab.poddar on 7/8/16.
 */

public class CountryListController
{


    protected WeakReference<IUIListener> listenerWeakReference;
    protected Context mCtx;

    public CountryListController(WeakReference<IUIListener> listenerWeakReference, Context mCtx)
    {
        this.listenerWeakReference = listenerWeakReference;
        this.mCtx = mCtx;
    }


    public synchronized void  getCountryData()
    {
        // Set Request Policy
        RequestPolicy requestPolicy = new RequestPolicy();
        requestPolicy.setForceCache(true);
        requestPolicy.setMaxAge(60 * 60 * 24);

        if(UtilityMethods.isSimulated)
        {
                String configString =
                        UtilityMethods.loadDataFromAsset(mCtx,
                                "country_list.xml");
                ParseTask parseTask = new ParseTask(CountryModel.class, configString, new IParseListener() {
                    @Override
                    public void onParseSuccess(Object responseModel) {
                        listenerWeakReference.get().onSuccess((IResponseModel)responseModel);
                    }

                    @Override
                    public void onParseFailure(ErrorModel errorModel) {
                        listenerWeakReference.get().onFailure(errorModel);
                    }
                },ParseTask.XML_DATA);
                parseTask.startParsing();
        }

        else {
            CustomXMLRequest<CountryModel> countryRequest = new CustomXMLRequest<CountryModel>(OlympicRequestQueries.COUNTRY_LIST, CountryModel.class,
                    createSuccessListener(), createFailureListener() , requestPolicy);
            VolleySingleton.getInstance(null).addToRequestQueue(countryRequest);
        }
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

    protected Response.Listener<CountryModel> createSuccessListener() {
        return new Response.Listener<CountryModel>() {
            @Override
            public void onResponse(CountryModel response) {
                listenerWeakReference.get().onSuccess(response);
            }
        };
    }


}
