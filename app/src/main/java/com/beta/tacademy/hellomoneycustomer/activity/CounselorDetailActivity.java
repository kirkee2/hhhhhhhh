package com.beta.tacademy.hellomoneycustomer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.beta.tacademy.hellomoneycustomer.R;
import com.beta.tacademy.hellomoneycustomer.common.CommonBaseActivity;
import com.beta.tacademy.hellomoneycustomer.module.httpConnectionModule.OKHttp3ApplyCookieManager;
import com.beta.tacademy.hellomoneycustomer.recyclerViews.counselorDetailRecyclerView.CounselorDetailHeaderObject;
import com.beta.tacademy.hellomoneycustomer.recyclerViews.counselorDetailRecyclerView.CounselorDetailRecyclerViewAdapter;
import com.beta.tacademy.hellomoneycustomer.recyclerViews.mainRecyclerView.MainValueObject;
import com.beta.tacademy.hellomoneycustomer.recyclerViews.quotationDetailRecyclerView.QuotationDetailHeaderObject;
import com.beta.tacademy.hellomoneycustomer.recyclerViews.quotationDetailRecyclerView.QuotationDetailRecyclerViewAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.beta.tacademy.hellomoneycustomer.common.util.ToastUtil.networkError;
import static com.beta.tacademy.hellomoneycustomer.module.httpConnectionModule.OKHttp3ApplyCookieManager.NETWORK_FAIL;
import static com.beta.tacademy.hellomoneycustomer.module.httpConnectionModule.OKHttp3ApplyCookieManager.NETWORK_SUCCESS;

public class CounselorDetailActivity extends CommonBaseActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private CounselorDetailRecyclerViewAdapter counselorDetailRecyclerViewAdapter;
    private CounselorDetailHeaderObject counselorDetailHeaderObject;
    private ArrayList<MainValueObject> mainValueObjectArrayList;
    private ProgressBar progressBar;
    private String agentId;
    private Activity activity;
    private CounselorHeaderDetail counselorHeaderDetail;
    private CounselorPostscriptList counselorPostscriptList;
    private ConstraintLayout errorView;
    private Button networkNotWorkingButton;

    private boolean isCounselorHeaderDetail;
    private boolean isCounselorPostscriptList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counselor_detail);

        initView();
        initListener();
        initNetwork();
        initFirstVariable();

        startNetwork();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private class CounselorHeaderDetail extends AsyncTask<Void, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressOnGoing(true);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            boolean flag;
            Response response = null;
            OkHttpClient toServer;
            JSONObject jsonObject;

            try {
                toServer = OKHttp3ApplyCookieManager.getOkHttpNormalClient();

                Request request = new Request.Builder()
                        .url(String.format(getResources().getString(R.string.detail_counselor_url), agentId))
                        .get()
                        .build();

                //동기 방식
                response = toServer.newCall(request).execute();

                flag = response.isSuccessful();

                String returedJSON;

                if (flag) { //성공했다면
                    returedJSON = response.body().string();
                    jsonObject = new JSONObject(returedJSON);
                } else {
                    return NETWORK_FAIL;
                }

                if (jsonObject.get(getResources().getString(R.string.url_message)).equals(getResources().getString(R.string.url_success))) {
                    JSONObject data = jsonObject.getJSONObject(getResources().getString(R.string.url_data));
                    counselorDetailHeaderObject = new CounselorDetailHeaderObject(data.getString("agent_id"), data.getString("photo"), data.getString("company_name"), data.getString("name"), data.getString("greeting"));

                    return NETWORK_SUCCESS;
                } else if (jsonObject.get(getResources().getString(R.string.url_message)).equals(getResources().getString(R.string.url_no_data))) {
                    return NETWORK_SUCCESS;
                } else {
                    return NETWORK_FAIL;
                }
            } catch (Exception e) {
                return NETWORK_FAIL;
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            progressOnGoing(false);

            if (result == NETWORK_SUCCESS) {
                counselorDetailRecyclerViewAdapter = new CounselorDetailRecyclerViewAdapter(activity, counselorDetailHeaderObject);
                recyclerView.setAdapter(counselorDetailRecyclerViewAdapter);

                isCounselorHeaderDetail = true;

                isNetworkSuccess();
            } else {
                networkNotWorking();
            }
        }
    }

    private class CounselorPostscriptList extends AsyncTask<Void, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressOnGoing(true);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            boolean flag;
            Response response = null;
            OkHttpClient toServer;
            JSONObject jsonObject;

            try {
                toServer = OKHttp3ApplyCookieManager.getOkHttpNormalClient();

                Request request = new Request.Builder()
                        .url(String.format(getResources().getString(R.string.detail_counselor_post_script_list_url), agentId))
                        .get()
                        .build();

                //동기 방식
                response = toServer.newCall(request).execute();

                flag = response.isSuccessful();

                String returedJSON;

                if (flag) { //성공했다면
                    returedJSON = response.body().string();
                    jsonObject = new JSONObject(returedJSON);
                } else {
                    return NETWORK_FAIL;
                }

                if (jsonObject.get(getResources().getString(R.string.url_message)).equals(getResources().getString(R.string.url_success))) {
                    JSONArray data = jsonObject.getJSONArray(getResources().getString(R.string.url_data));

                    for (int i = 0; i < data.length(); i++) {
                        try {
                            JSONObject jsonData = (JSONObject) data.get(i);
                            mainValueObjectArrayList.add(new MainValueObject((int) jsonData.get("review_id"), String.valueOf(jsonData.get("loan_type")), String.valueOf(jsonData.get("register_time")), String.valueOf(jsonData.get("region_1")), String.valueOf(jsonData.get("region_2")), String.valueOf(jsonData.get("region_3")), String.valueOf(jsonData.get("apt_name")), (int) jsonData.get("score"), String.valueOf(jsonData.get("content")), jsonData.getInt("benefit")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    return NETWORK_SUCCESS;
                } else if (jsonObject.get(getResources().getString(R.string.url_message)).equals(getResources().getString(R.string.url_no_data))) {
                    return NETWORK_SUCCESS;
                } else {
                    return NETWORK_FAIL;
                }
            } catch (Exception e) {
                return NETWORK_FAIL;
            } finally {
                if (response != null) {
                    response.close(); //3.* 이상에서는 반드시 닫아 준다.
                }
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            progressOnGoing(false);

            if (result == NETWORK_SUCCESS) {
                counselorDetailRecyclerViewAdapter.initItem(mainValueObjectArrayList);

                isCounselorPostscriptList = true;

                isNetworkSuccess();
            } else {
                networkNotWorking();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (counselorHeaderDetail.getStatus() == AsyncTask.Status.RUNNING) {
            counselorHeaderDetail.cancel(true);
        }

        if (counselorPostscriptList.getStatus() == AsyncTask.Status.RUNNING) {
            counselorPostscriptList.cancel(true);
        }
    }


    @Override
    protected void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        errorView = (ConstraintLayout) findViewById(R.id.errorView);
        networkNotWorkingButton = (Button) findViewById(R.id.networkNotWorking);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

    }

    private void initFirstVariable() {
        activity = this;
        Intent intent = getIntent();
        agentId = intent.getStringExtra("agentId");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false); //RecyclerView에 설정 할 LayoutManager 초기화

        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void initVariable() {
        mainValueObjectArrayList = new ArrayList<>();

        isCounselorHeaderDetail = false;
        isCounselorPostscriptList = false;
    }

    @Override
    protected void initListener() {
        networkNotWorkingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNetwork();
            }
        });
    }

    @Override
    protected void initNetwork() {
        counselorHeaderDetail = new CounselorHeaderDetail();
        counselorPostscriptList = new CounselorPostscriptList();
    }

    @Override
    protected void stopNetWork() {
        if (counselorHeaderDetail.getStatus() == AsyncTask.Status.RUNNING) {
            counselorHeaderDetail.cancel(true);
        }

        if (counselorPostscriptList.getStatus() == AsyncTask.Status.RUNNING) {
            counselorPostscriptList.cancel(true);
        }
    }

    @Override
    public void progressOnGoing(boolean isOngoing) {
        if (isOngoing) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void startNetwork() {
        initVariable();

        if (counselorHeaderDetail.getStatus() != AsyncTask.Status.RUNNING) {
            if (counselorHeaderDetail.getStatus() == AsyncTask.Status.FINISHED) {
                counselorHeaderDetail = new CounselorHeaderDetail();
            }
            counselorHeaderDetail.execute();
        }

        if (counselorPostscriptList.getStatus() != AsyncTask.Status.RUNNING) {
            if (counselorPostscriptList.getStatus() == AsyncTask.Status.FINISHED) {
                counselorPostscriptList = new CounselorPostscriptList();
            }
            counselorPostscriptList.execute();
        }
    }

    @Override
    protected void networkNotWorking() {
        networkError(getApplicationContext());
        stopNetWork();

        errorView.setVisibility(View.VISIBLE);
    }

    @Override
    protected synchronized void isNetworkSuccess() {
        if(isCounselorHeaderDetail && isCounselorPostscriptList){
            errorView.setVisibility(View.GONE);
        }
    }

}

