package com.beta.tacademy.hellomoneycustomer.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
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
import android.widget.Toast;

import com.beta.tacademy.hellomoneycustomer.R;
import com.beta.tacademy.hellomoneycustomer.common.CommonBaseActivity;
import com.beta.tacademy.hellomoneycustomer.module.httpConnectionModule.OKHttp3ApplyCookieManager;
import com.beta.tacademy.hellomoneycustomer.recyclerViews.mainRecyclerView.MainRecyclerViewAdapter;
import com.beta.tacademy.hellomoneycustomer.recyclerViews.mainRecyclerView.MainValueObject;
import com.beta.tacademy.hellomoneycustomer.recyclerViews.quotationDetailRecyclerView.QuotationDetailHeaderObject;
import com.beta.tacademy.hellomoneycustomer.recyclerViews.quotationDetailRecyclerView.QuotationDetailObject;
import com.beta.tacademy.hellomoneycustomer.recyclerViews.quotationDetailRecyclerView.QuotationDetailRecyclerViewAdapter;
import com.beta.tacademy.hellomoneycustomer.viewPagers.mainViewpager.MainPageViewPagerObject;
import com.beta.tacademy.hellomoneycustomer.viewPagers.myQuotationViewPager.MyQuotationFragmentPagerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.beta.tacademy.hellomoneycustomer.common.util.ToastUtil.networkError;
import static com.beta.tacademy.hellomoneycustomer.module.httpConnectionModule.OKHttp3ApplyCookieManager.NETWORK_FAIL;
import static com.beta.tacademy.hellomoneycustomer.module.httpConnectionModule.OKHttp3ApplyCookieManager.NETWORK_SUCCESS;

public class QuotationDetailActivity extends CommonBaseActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private QuotationDetailRecyclerViewAdapter quotationDetailRecyclerViewAdapter;
    private ProgressBar progressBar;
    private ConstraintLayout errorView;
    private int quotationDetailId;
    private QuotationDetailHeaderObject quotationDetailHeaderObject;
    private ArrayList<QuotationDetailObject> quotationDetailObjectArrayList;
    private Activity activity;
    private QuotationFeedback quotationFeedback;
    private QuotationDetail quotationDetail;
    private int position;
    private int myPage;
    private Button networkNotWorkingButton;


    private boolean isQuotationFeedback;
    private boolean isQuotationDetail;
    public Timer timer;
    public TimerTask timerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotation_detail);

        initView();
        initFirstVariable();
        initListener();
        initNetwork();

        startNetwork();
   }

    public void update(){
        quotationDetailObjectArrayList = new ArrayList<>();

        if (quotationDetail.getStatus() != AsyncTask.Status.RUNNING) {
            if (quotationDetail.getStatus() == AsyncTask.Status.FINISHED) {
                quotationDetail = new QuotationDetail();
            }
            quotationDetail.execute();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            intent.putExtra("position",this.position);
            intent.putExtra("myPage",this.myPage);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private class QuotationDetail extends AsyncTask<Void, Void, Integer> {
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

            try{
                toServer = OKHttp3ApplyCookieManager.getOkHttpNormalClient();

                Request request = new Request.Builder()
                        .url(String.format(getResources().getString(R.string.my_quotation_detail_url),String.valueOf(quotationDetailId)))
                        .get()
                        .build();

                //동기 방식
                response = toServer.newCall(request).execute();

                flag = response.isSuccessful();

                String returedJSON;

                if(flag){ //성공했다면
                    returedJSON = response.body().string();
                    jsonObject = new JSONObject(returedJSON);
                }else{
                    return NETWORK_FAIL;
                }

                if(jsonObject.get(getResources().getString(R.string.url_message)).equals(getResources().getString(R.string.url_success))){
                    JSONObject data= jsonObject.getJSONObject(getResources().getString(R.string.url_data));

                    quotationDetailHeaderObject = new QuotationDetailHeaderObject(data.optInt("request_id"),data.optString("status"),data.optString("end_time"),data.optString("loan_type"),data.optString("region_1"),data.optString("region_2"),data.optString("region_3"),data.getString("apt_name"),data.optDouble("apt_size_supply") + "(" + data.optDouble("apt_size_exclusive") +")",data.optInt("loan_amount"),data.optString("interest_rate_type"),data.optString("scheduled_time"),data.getString("job_type"),data.getString("phone_number"),data.optBoolean("is_reviewed"),data.optInt("selected_estimate_id"),data.optString("content"),data.optDouble("score"),data.optString("review_register_time"),data.optString("name"),data.optString("photo"),data.optString("company_name"),data.optString("agent_id"));

                    return NETWORK_SUCCESS;
                }else if(jsonObject.get(getResources().getString(R.string.url_message)).equals(getResources().getString(R.string.url_no_data))){
                    return NETWORK_SUCCESS;
                }else{
                    return NETWORK_FAIL;
                }
            }catch (Exception e) {
                return NETWORK_FAIL;
            } finally{
                if(response != null) {
                    response.close(); //3.* 이상에서는 반드시 닫아 준다.
                }
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            progressOnGoing(false);

            if(result == NETWORK_SUCCESS){
                isQuotationDetail = true;
                isNetworkSuccess();
            }else{
                networkNotWorking();
            }
        }
    }

    private class QuotationFeedback extends AsyncTask<Void, Void, Integer> {
        @Override
        protected void onPreExecute() {
            progressOnGoing(true);

            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            boolean flag;
            Response response = null;
            OkHttpClient toServer;
            JSONObject jsonObject = null;

            try{
                toServer =  OKHttp3ApplyCookieManager.getOkHttpNormalClient();

                Request request = new Request.Builder()
                        .url(String.format(getResources().getString(R.string.quotation_detail_counselor_feedback_url),String.valueOf(quotationDetailId)))
                        .get()
                        .build();

                //동기 방식
                response = toServer.newCall(request).execute();

                flag = response.isSuccessful();

                String returedJSON;

                if(flag){ //성공했다면
                    returedJSON = response.body().string();
                    jsonObject = new JSONObject(returedJSON);
                }else{
                    return NETWORK_FAIL;
                }

                if(jsonObject.get(getResources().getString(R.string.url_message)).equals(getResources().getString(R.string.url_success))){
                    JSONArray data= jsonObject.getJSONArray(getResources().getString(R.string.url_data));

                    for(int i = 0 ; i < data.length(); i++){
                        JSONObject jsonData = (JSONObject)data.get(i);
                        quotationDetailObjectArrayList.add(new QuotationDetailObject(jsonData.getInt("estimate_id"),jsonData.getString("company_name"),jsonData.getString("name"),jsonData.getString("interest_rate_type"),jsonData.getDouble("interest_rate"),jsonData.getString("photo")));
                        quotationDetailRecyclerViewAdapter.initItem(quotationDetailObjectArrayList);
                    }

                    return NETWORK_SUCCESS;
                }else if(jsonObject.get(getResources().getString(R.string.url_message)).equals(getResources().getString(R.string.url_no_data))){
                    return NETWORK_SUCCESS;
                }else{
                    return NETWORK_FAIL;
                }
            } catch (Exception e) {
                return NETWORK_FAIL;
            } finally{
                if(response != null) {
                    response.close(); //3.* 이상에서는 반드시 닫아 준다.
                }
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            progressOnGoing(false);

            if(result == NETWORK_SUCCESS){

                //isReviewed == true -> Yes
               /* if(quotationDetailHeaderObject.getOngoingStatus().equals("대출실행완료")){
                    if(!quotationDetailHeaderObject.isReviewed()) {
                        quotationDetailRecyclerViewAdapter = new QuotationDetailRecyclerViewAdapter(activity, QuotationDetailRecyclerViewAdapter.YES_WRITE_COMMENT, quotationDetailHeaderObject);
                        recyclerView.setAdapter(quotationDetailRecyclerViewAdapter);
                    }else {
                        quotationDetailRecyclerViewAdapter = new QuotationDetailRecyclerViewAdapter(activity, QuotationDetailRecyclerViewAdapter.NO_WRITE_DONE_COMMENT, quotationDetailHeaderObject);
                        recyclerView.setAdapter(quotationDetailRecyclerViewAdapter);
                    }
                }else{
                    if(quotationDetailHeaderObject.getSelectedEstimateId() == 0){
                        if(quotationDetailObjectArrayList.size() == 0){
                            quotationDetailRecyclerViewAdapter = new QuotationDetailRecyclerViewAdapter(activity,QuotationDetailRecyclerViewAdapter.NO_WRITE_ONGOING_COMMENT_NO_FEED,quotationDetailHeaderObject);
                            recyclerView.setAdapter(quotationDetailRecyclerViewAdapter);
                        }else{
                            quotationDetailRecyclerViewAdapter = new QuotationDetailRecyclerViewAdapter(activity,QuotationDetailRecyclerViewAdapter.NO_WRITE_ONGOING_COMMENT,quotationDetailHeaderObject);
                            recyclerView.setAdapter(quotationDetailRecyclerViewAdapter);
                        }
                    }else{
                        if(quotationDetailObjectArrayList.size() == 1){
                            quotationDetailRecyclerViewAdapter = new QuotationDetailRecyclerViewAdapter(activity,QuotationDetailRecyclerViewAdapter.NO_WRITE_ONGOING_SELECTED_COMMENT_NO_FEED,quotationDetailHeaderObject);
                            recyclerView.setAdapter(quotationDetailRecyclerViewAdapter);
                        }else{
                            quotationDetailRecyclerViewAdapter = new QuotationDetailRecyclerViewAdapter(activity,QuotationDetailRecyclerViewAdapter.NO_WRITE_ONGOING_SELECTED_COMMENT,quotationDetailHeaderObject);
                            recyclerView.setAdapter(quotationDetailRecyclerViewAdapter);
                        }
                    }
                }*/

                quotationDetailRecyclerViewAdapter = new QuotationDetailRecyclerViewAdapter(activity,quotationDetailHeaderObject);
                quotationDetailRecyclerViewAdapter.initItem(quotationDetailObjectArrayList);
                for(QuotationDetailObject tmp : quotationDetailObjectArrayList){
                    if(tmp.getId() == quotationDetailHeaderObject.getSelectedEstimateId()){
                        quotationDetailRecyclerViewAdapter.addSubHeader(tmp);
                    }else{
                        quotationDetailRecyclerViewAdapter.addItem(tmp);
                    }
                }
                /*if(quotationDetailHeaderObject.getSelectedEstimateId() == 0){
                    quotationDetailRecyclerViewAdapter.initItem(quotationDetailObjectArrayList);
                }else{
                    for(QuotationDetailObject tmp : quotationDetailObjectArrayList){
                        if(tmp.getId() == quotationDetailHeaderObject.getSelectedEstimateId()){
                            quotationDetailRecyclerViewAdapter.addSubHeader(tmp);
                        }else{
                            quotationDetailRecyclerViewAdapter.addItem(tmp);
                        }
                    }
                }*/

                isQuotationFeedback = true;
                isNetworkSuccess();
            }else{
                networkNotWorking();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("position",this.position);
        intent.putExtra("myPage",this.myPage);
        setResult(RESULT_OK, intent);
        finish();
    }

    /*
    @Override
    protected void onPause(){
        super.onPause();
        if(timer == null){

        }else{
            timer.cancel();
        }
    }
    */

    @Override
    protected void onDestroy(){
        super.onDestroy();

        if (quotationDetail.getStatus() == AsyncTask.Status.RUNNING) {
            quotationDetail.cancel(true);
        }

        if (quotationFeedback.getStatus() == AsyncTask.Status.RUNNING) {
            quotationFeedback.cancel(true);
        }

        if(timer != null){
            timer.cancel();
        }
    }

    @Override
    protected void initView() {
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        errorView = (ConstraintLayout) findViewById(R.id.errorView);
        networkNotWorkingButton = (Button) findViewById(R.id.networkNotWorking);

        setSupportActionBar(toolbar); //Toolbar를 현재 Activity의 Actionbar로 설정.

        //Toolbar 설정
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setTitle(getResources().getString(R.string.quotation_detail));
        toolbar.setTitleTextColor(ResourcesCompat.getColor(getApplicationContext().getResources(),R.color.normalTypo,null));
    }

    @Override
    protected void initVariable() {
        quotationDetailObjectArrayList = new ArrayList<>();

        isQuotationFeedback = false;
        isQuotationDetail = false;

    }

    private void initFirstVariable(){
        activity = this;
        Intent intent = getIntent();
        quotationDetailId = intent.getIntExtra("id",-1);
        position = intent.getIntExtra("position",0);
        myPage = intent.getIntExtra("myPage",0);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false); //RecyclerView에 설정 할 LayoutManager 초기화

        recyclerView.setLayoutManager(linearLayoutManager);
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
        quotationFeedback = new QuotationFeedback();
        quotationDetail = new QuotationDetail();
    }

    @Override
    protected void stopNetWork() {
        if (quotationFeedback.getStatus() == AsyncTask.Status.RUNNING) {
            quotationFeedback.cancel(true);
        }

        if (quotationDetail.getStatus() == AsyncTask.Status.RUNNING) {
            quotationDetail.cancel(true);
        }
    }

    @Override
    public void progressOnGoing(boolean isOngoing) {
        if(isOngoing){
            progressBar.setVisibility(View.VISIBLE);
        }else{
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void startNetwork() {
        initVariable();

        if (quotationDetail.getStatus() != AsyncTask.Status.RUNNING) {
            if (quotationDetail.getStatus() == AsyncTask.Status.FINISHED) {
                quotationDetail = new QuotationDetail();
            }
            quotationDetail.execute();
        }

        if (quotationFeedback.getStatus() != AsyncTask.Status.RUNNING) {
            if (quotationFeedback.getStatus() == AsyncTask.Status.FINISHED) {
                quotationFeedback = new QuotationFeedback();
            }
            quotationFeedback.execute();
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
        if(isQuotationDetail && isQuotationFeedback){
            errorView.setVisibility(View.GONE);
        }
    }
}
