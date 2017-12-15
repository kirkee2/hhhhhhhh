package com.beta.tacademy.hellomoneycustomer.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.beta.tacademy.hellomoneycustomer.R;
import com.beta.tacademy.hellomoneycustomer.common.CommonBaseActivity;
import com.beta.tacademy.hellomoneycustomer.listView.faqListView.FaqExpandableListViewAdapter;
import com.beta.tacademy.hellomoneycustomer.module.httpConnectionModule.OKHttp3ApplyCookieManager;
import com.beta.tacademy.hellomoneycustomer.recyclerViews.FAQRecyclerView.FAQRecyclerViewAdapter;
import com.beta.tacademy.hellomoneycustomer.recyclerViews.FAQRecyclerView.FAQValueObject;

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

public class FAQActivity extends CommonBaseActivity {
    private Toolbar toolbar;
    private ConstraintLayout errorView;
    private Button networkNotWorkingButton;
    private ProgressBar progressBar;
    private ArrayList<FAQValueObject> faqValueObjectArrayList;
    private FAQGet faqGet;

    private ExpandableListView mExpandableListView;
    private ArrayList<String> mGroupList = null;
    private ArrayList<ArrayList<String>> mChildList = null;
    private ArrayList<String> mChildListContent = null;
    private FaqExpandableListViewAdapter mFaqExpandableListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        initView();
        initListener();
        initNetwork();
        startNetwork();
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private class FAQGet extends AsyncTask<Void, Void, Integer> {
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
                        .url(getResources().getString(R.string.faq_url))
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
                        JSONObject jsonData = (JSONObject) data.get(i);
                        faqValueObjectArrayList.add(new FAQValueObject(String.valueOf(jsonData.get("question")), String.valueOf(jsonData.get("answer"))));
                    }
                    return NETWORK_SUCCESS;
                } else if (jsonObject.get(getResources().getString(R.string.url_message)).equals(getResources().getString(R.string.url_no_data))) {
                    return NETWORK_FAIL;
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
                initItems(faqValueObjectArrayList);

                isNetworkSuccess();
            } else {
                networkNotWorking();
            }
        }
    }

    public void initItems(ArrayList<FAQValueObject> faqValueObjectArrayList) {
        mFaqExpandableListViewAdapter.initItem(faqValueObjectArrayList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopNetWork();
    }

    @Override
    protected void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        errorView = (ConstraintLayout) findViewById(R.id.errorView);
        networkNotWorkingButton = (Button) findViewById(R.id.networkNotWorking);
        mExpandableListView = (ExpandableListView) findViewById(R.id.expandableListView);

        setSupportActionBar(toolbar); //Toolbar를 현재 Activity의 Actionbar로 설정.

        //Toolbar 설정
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
/*

        toolbar.setTitle(getResources().getString(R.string.faq));
        toolbar.setTitleTextColor(ResourcesCompat.getColor(getApplicationContext().getResources(), R.color.normalTypo, null));
*/
    }

    @Override
    protected void initVariable() {
        faqValueObjectArrayList = new ArrayList<>();

        mFaqExpandableListViewAdapter = new FaqExpandableListViewAdapter(this,faqValueObjectArrayList);
        mExpandableListView.setAdapter(mFaqExpandableListViewAdapter);

        mExpandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                for(int i = 0 ; i < mFaqExpandableListViewAdapter.getGroupCount() ; i++) {
                    if(i != groupPosition){
                        mExpandableListView.collapseGroup(i);
                    }
                }
            }
        });
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
        faqGet = new FAQGet();
    }

    @Override
    protected void stopNetWork() {
        if (faqGet.getStatus() == AsyncTask.Status.RUNNING) {
            faqGet.cancel(true);
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

        if (faqGet.getStatus() != AsyncTask.Status.RUNNING) {
            if (faqGet.getStatus() == AsyncTask.Status.FINISHED) {
                faqGet = new FAQGet();
            }
            faqGet.execute();
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
}
