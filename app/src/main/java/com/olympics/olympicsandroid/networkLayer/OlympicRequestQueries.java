package com.olympics.olympicsandroid.networkLayer;

import android.text.TextUtils;

import com.android.volley.Request;
import com.olympics.olympicsandroid.networkLayer.cache.database.OlympicsPrefs;

/**
 * Created by sarnab.poddar on 7/9/16.
 */
public enum OlympicRequestQueries
{

    COUNTRY_LIST(Request.Method.GET,"/organization/list.xml", true , true),
    COUNTRY_CONFIG(Request.Method.GET,"/organization/2016/{countryID}/profile.xml", true , true),
    COMPLETE_SCHEDULE(Request.Method.GET,"/2016/schedule.xml", true , true),
    MEDAL_TALLY(Request.Method.GET, "/2016/medals.xml",true, true),
    EVENT_RESULTS(Request.Method.GET,"/event/{eventID}/results.xml", true , true),
    SPORTS_TYPE(Request.Method.GET, "https://olympics.mybluemix.net/config/getSportsData", null,
                true, true),
    APP_VERSION_DATA(Request.Method.GET, "https://olympics.mybluemix.net/config/getAppVersion",
            null,
            true, false);

    private int httpRequestType;
    private String relativeURL;
    private boolean isCacheable;
    private boolean needAPIKey;

    private String apiKey = "5hkjft4mvnbzc26875u6c2zv";
    private String baseURL = "https://api.sportradar.us/oly-testing2";

    OlympicRequestQueries(int httpRequestType, String relativeURL , boolean isCacheable, boolean needAPIKey)
    {
        this.httpRequestType = httpRequestType;
        this.relativeURL = relativeURL;
        this.isCacheable = isCacheable;
        this.needAPIKey = needAPIKey;

        //Setup baseURL
        String urlStr = OlympicsPrefs.getInstance(null).getBaseURL();
        this.baseURL = TextUtils.isEmpty(urlStr) ? baseURL : urlStr;

        //Setup API Key
        String apiKeyFromServer = OlympicsPrefs.getInstance(null).getAPIKey();
        this.apiKey = TextUtils.isEmpty(apiKeyFromServer) ? apiKey : apiKeyFromServer;

    }

    OlympicRequestQueries(int httpRequestType, String baseURL, String relativeURL , boolean
            isCacheable, boolean needAPIKey)
    {
        this.baseURL = baseURL;
        this.httpRequestType = httpRequestType;
        this.relativeURL = relativeURL;
        this.isCacheable = isCacheable;
        this.needAPIKey = needAPIKey;
    }

    public int getHttpRequestType() {
        return httpRequestType;
    }

    public String getURL(String urlReplacement) {
        StringBuilder finalRequestString = new StringBuilder(baseURL);
        if(!TextUtils.isEmpty(relativeURL)) {
            String relativeTempURL = new String(relativeURL);
            relativeTempURL = editURLIfReqd(relativeTempURL,urlReplacement);
            finalRequestString.append(relativeTempURL);
        }
        if(needAPIKey)
        {
            finalRequestString.append("?");
            finalRequestString.append("api_key=" + apiKey);
        }

        return finalRequestString.toString();
    }

    private String editURLIfReqd(String relativeTempURL,String urlReplacement) {
        String[] allRelativeURL = relativeTempURL.split("/");
        String stringToReplace = null;
        for(String data:allRelativeURL)
        {
            if(data.contains("{"))
            {
                stringToReplace = data;
                break;
            }
        }
        if(!TextUtils.isEmpty(stringToReplace) && !TextUtils.isEmpty(urlReplacement)) {
            relativeTempURL = relativeTempURL.replace(stringToReplace, urlReplacement);
        }
        return relativeTempURL;
    }


}
