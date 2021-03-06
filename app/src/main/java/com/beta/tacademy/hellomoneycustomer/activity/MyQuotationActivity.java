package com.beta.tacademy.hellomoneycustomer.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.beta.tacademy.hellomoneycustomer.R;
import com.beta.tacademy.hellomoneycustomer.common.CommonBaseActivity;
import com.beta.tacademy.hellomoneycustomer.common.util.SharedReferenceUtil;
import com.beta.tacademy.hellomoneycustomer.module.httpConnectionModule.OKHttp3ApplyCookieManager;
import com.beta.tacademy.hellomoneycustomer.viewPagers.mainViewpager.MainPageViewPagerObject;
import com.beta.tacademy.hellomoneycustomer.viewPagers.myQuotationViewPager.MyQuotationFragmentPagerAdapter;

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

public class MyQuotationActivity extends CommonBaseActivity {
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private MyQuotationFragmentPagerAdapter myQuotationFragmentPagerAdapter;
    private ArrayList<MainPageViewPagerObject> mainPageViewPagerObjectOneM;
    private ArrayList<MainPageViewPagerObject> mainPageViewPagerObjectTwoM;
    private boolean firstCome;
    private int myPage;
    private ConstraintLayout errorView;
    private Button networkNotWorkingButton;

    private MyQuotationList myQuotationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_quotation);

        initView();
        initListener();
        initNetwork();
    }

    @Override
    protected void initView() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        errorView = (ConstraintLayout) findViewById(R.id.errorView);
        networkNotWorkingButton = (Button) findViewById(R.id.networkNotWorking);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        /*toolbar.setTitle(getResources().getString(R.string.my_quotation));
        toolbar.setTitleTextColor(ResourcesCompat.getColor(getApplicationContext().getResources(), R.color.title_color, null));*/
    }

    @Override
    protected void initVariable() {
        mainPageViewPagerObjectOneM = new ArrayList<>();
        mainPageViewPagerObjectTwoM = new ArrayList<>();

        myPage = -1;

        firstCome = true;
    }

    @Override
    protected void initNetwork(){
        myQuotationList = new MyQuotationList();
    }

    @Override
    protected void onStart() {
        super.onStart();

        initVariable();

        startNetwork();
    }


    @Override
    protected void startNetwork(){
        if(myQuotationList.getStatus() != AsyncTask.Status.RUNNING){
            if (myQuotationList.getStatus() == AsyncTask.Status.FINISHED) {
                myQuotationList = new MyQuotationList();
            }
            myQuotationList.execute();
        }
    }

    private class MyQuotationList extends AsyncTask<Void, Void, Integer> {
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
                        .url(String.format(getResources().getString(R.string.my_request_quotation_no_limit_url), SharedReferenceUtil.getUUID(), "false"))
                        .get()
                        .build();

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
                        JSONObject jsonData = (JSONObject) data.get(i);

                        if (!String.valueOf(jsonData.get("status")).equals("대출실행완료")) {
                            mainPageViewPagerObjectOneM.add(new MainPageViewPagerObject((int) jsonData.get("request_id"), String.valueOf(jsonData.get("status")), String.valueOf(jsonData.get("loan_type")), String.valueOf(jsonData.get("end_time")), String.valueOf(jsonData.get("region_1")), String.valueOf(jsonData.get("region_2")), String.valueOf(jsonData.get("region_3")), String.valueOf(jsonData.get("apt_name")), String.valueOf(jsonData.get("apt_size_supply") + "(" + jsonData.get("apt_size_exclusive") + ")"), (int) jsonData.get("estimate_count")));
                        } else {
                            mainPageViewPagerObjectTwoM.add(new MainPageViewPagerObject((int) jsonData.get("request_id"), String.valueOf(jsonData.get("status")), String.valueOf(jsonData.get("loan_type")), String.valueOf(jsonData.get("end_time")), String.valueOf(jsonData.get("region_1")), String.valueOf(jsonData.get("region_2")), String.valueOf(jsonData.get("region_3")), String.valueOf(jsonData.get("apt_name")), String.valueOf(jsonData.get("apt_size_supply") + "(" + jsonData.get("apt_size_exclusive") + ")"), (int) jsonData.get("estimate_count")));
                        }
                    }

                    //myQuotationFragmentPagerAdapter.init(mainPageViewPagerObjectOne,mainPageViewPagerObjectTwo);
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
                myQuotationFragmentPagerAdapter = new MyQuotationFragmentPagerAdapter(getSupportFragmentManager(), mainPageViewPagerObjectOneM, mainPageViewPagerObjectTwoM);
                //myQuotationFragmentPagerAdapter.init(mainPageViewPagerObjectOneM,mainPageViewPagerObjectTwoM);

                viewPager.setAdapter(myQuotationFragmentPagerAdapter);
                tabLayout.setupWithViewPager(viewPager, true);
                myQuotationFragmentPagerAdapter.notifyDataSetChanged();

                if (firstCome) {
                    Intent intent = getIntent();
                    viewPager.setCurrentItem(intent.getIntExtra("page", 0));
                    firstCome = false;
                } else if (myPage != -1) {
                    viewPager.setCurrentItem(myPage);
                }else{
                    viewPager.setCurrentItem(0);
                }

                isNetworkSuccess();
            }else{
                networkNotWorking();
            }
        }
    }

    public void update() {
        mainPageViewPagerObjectOneM = new ArrayList<>();
        mainPageViewPagerObjectTwoM = new ArrayList<>();

        myQuotationList = new MyQuotationList();
        myQuotationList.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                update();
                myPage = data.getIntExtra("myPage", -1);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopNetWork();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void stopNetWork() {
        if (myQuotationList.getStatus() == AsyncTask.Status.RUNNING) {
            myQuotationList.cancel(true);
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
    protected void networkNotWorking() {
        networkError(getApplicationContext());
        stopNetWork();

        errorView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void isNetworkSuccess() {
        errorView.setVisibility(View.GONE);
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

}
