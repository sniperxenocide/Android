package com.cgd.cvm_support_app.data.utils;

import android.app.ProgressDialog;
import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cgd.cvm_support_app.CommonUtil;
import com.cgd.cvm_support_app.view.ui.LoginActivity;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

public class VolleyUtil {
    private final int method;  // GET=0 , POST=1
    private final String url;
    private final JSONObject body;
    private final JSONObject param;
    private final RequestQueue requestQueue;
    private final ProgressDialog progressDialog;
    private String apiResponse;
    DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(
            10000,0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);


    public VolleyUtil(Context context, int method, String url, JSONObject param, JSONObject body) {
        this.method = method;
        this.url = url;
        this.body = body;
        this.param = param;
        requestQueue = Volley.newRequestQueue(context);
        progressDialog  = new ProgressDialog(context);
        progressDialog.setMessage("Loading....");
    }

    public void callApi(Callable<Integer> callback){
        try {
            System.out.println("URL: "+url);
            StringRequest stringRequest = new StringRequest(
                    this.method, this.url,
                    response -> {
                        try {
                            progressDialog.dismiss();
                            this.apiResponse = response;
                            System.out.println(apiResponse);
                            callback.call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        try {
                            progressDialog.dismiss();
                            error.printStackTrace();
                            callback.call();
                        } catch (Exception e){e.printStackTrace();}

                    }){
                @Override
                public Map<String, String> getHeaders()
                {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json");
                    if(CommonUtil.currentAuthToken !=null)
                        params.put("Authorization","Bearer "+ CommonUtil.currentAuthToken);
                    return params;
                }

                @Override
                protected Map<String,String> getParams(){
                    if(param==null) return null;
                    Map<String,String> params = new HashMap<>();
                    try {
                        for (Iterator<String> it = param.keys(); it.hasNext(); ) {
                            String k = it.next();
                            params.put(k,param.get(k).toString());
                        }
                    }catch (Exception e){ System.out.println("Parameter Exception"); }
                    return params;
                }

                @Override
                public byte[] getBody() {
                    if(body==null) return null;
                    return body.toString().getBytes(StandardCharsets.UTF_8);
                }
            };
            stringRequest.setRetryPolicy(this.retryPolicy);
            this.requestQueue.add(stringRequest);
            progressDialog.show();
        }catch (Exception e){e.printStackTrace();}
    }

    public String getApiResponse(){
        return this.apiResponse;
    }
}
