package com.olympics.olympicsandroid.networkLayer;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.UnsupportedEncodingException;
import java.util.Map;


/**
 * Simple Volley Class for doing XML HTTP Requests which are parsed 
 * into Java objects by Simple @see {{@link http://simple.sourceforge.net/}
 */
public class CustomXMLRequest<T> extends Request<T> {

    private static final Serializer serializer = new Persister();
    private final Class<T> clazz;
    private final Map<String, String> headers;
    private final Listener<T> listener;
    private RequestPolicy requestPolicy;


    /**
     * Make HTTP request and return a parsed object from Response
     * Invokes the other constructor. 
     *
     * @see CustomXMLRequest#CustomXMLRequest(OlympicRequestQueries, Class, Map, Listener, ErrorListener, RequestPolicy)
     */
    public CustomXMLRequest(OlympicRequestQueries requestQueries, Class<T> clazz,
                            Listener<T> listener, ErrorListener errorListener) {
        this(requestQueries, clazz, null, listener, errorListener,null);
    }

    /**
     * Make HTTP request and return a parsed object from Response
     * Invokes the other constructor.
     *
     * @see CustomXMLRequest#CustomXMLRequest(OlympicRequestQueries, Class, Map, Listener, ErrorListener, RequestPolicy)
     */
    public CustomXMLRequest(OlympicRequestQueries requestQueries, Class<T> clazz,
                            Listener<T> listener, ErrorListener errorListener , RequestPolicy requestPolicy) {
        this(requestQueries, clazz, null, listener, errorListener,requestPolicy);
    }

    /**
     * Make HTTP request and return a parsed object from XML Response
     *
     * @param url URL of the request to make
     * @param clazz Relevant class object
     * @param headers Map of request headers
     * @param listener
     * @param errorListener
     * @param requestPolicy Cache policy
     *
     */
    public CustomXMLRequest(OlympicRequestQueries requestQueries, Class<T> clazz, Map<String, String> headers,
                            Listener<T> listener, ErrorListener errorListener, RequestPolicy requestPolicy) {
        super(requestQueries.getHttpRequestType(), requestQueries.getURL(requestPolicy.getUrlReplacement()), errorListener);
        setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        if(requestPolicy != null) {
            setShouldCache(requestPolicy.isForceCache());
        }
        this.clazz = clazz;
        this.headers = headers;
        this.listener = listener;
        this.requestPolicy = requestPolicy;
    }


    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }


    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }


    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response)
    {
        try {

            String data = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            Cache.Entry entries = HttpHeaderParser.parseCacheHeaders(response);
            if(this.requestPolicy != null && this.requestPolicy.isForceCache())
            {
                long expireTime = System.currentTimeMillis() + this.requestPolicy.getMaxAge() * 1000;
                entries.softTtl = expireTime;
                entries.ttl = expireTime;
            }
            return Response.success(serializer.read(clazz, data),entries
                    );
        }
        catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
        catch (Exception e) {
            return Response.error(new VolleyError(e.getMessage()));
        }
    }

}