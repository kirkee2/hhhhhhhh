package com.beta.tacademy.hellomoneycustomer.fragment;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.beta.tacademy.hellomoneycustomer.R;
import com.beta.tacademy.hellomoneycustomer.module.httpConnectionModule.OKHttp3ApplyCookieManager;
import com.beta.tacademy.hellomoneycustomer.recyclerViews.selectRegionRecyclerView.SelectRegionRecyclerViewAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class AptSizeFragment extends Fragment {
    RecyclerView recyclerView;
    ProgressBar progressBar;
    SelectRegionRecyclerViewAdapter selectRegionRecyclerViewAdapter;
    ArrayList<String> stringArraylist;
    String region1;
    String region2;
    String region3;
    String apt;
    RequestAptSize requestAptSize;


    public AptSizeFragment() {
        // Required empty public constructor
    }

    public static AptSizeFragment newInstance(String region1, String region2, String region3, String apt) {
        AptSizeFragment fragment = new AptSizeFragment();
        Bundle args = new Bundle();
        args.putString("region1", region1);
        args.putString("region2", region2);
        args.putString("region3", region3);
        args.putString("apt", apt);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.region1 = getArguments().getString("region1");
            this.region2 = getArguments().getString("region2");
            this.region3 = getArguments().getString("region3");
            this.apt = getArguments().getString("apt");
        }else{
            this.region1 = null;
            this.region2 = null;
            this.region3 = null;
            this.apt = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_size, container, false);

        recyclerView = (RecyclerView)view.findViewById(R.id.recyclerView);
        progressBar = (ProgressBar)view.findViewById(R.id.progressBar);

        stringArraylist = new ArrayList<>();
        selectRegionRecyclerViewAdapter = new SelectRegionRecyclerViewAdapter(getActivity(),4);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);

        recyclerView.setLayoutManager(linearLayoutManager);

        requestAptSize = new RequestAptSize();
        requestAptSize.execute();

        return view;
    }

    private class RequestAptSize extends AsyncTask<Void, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //시작 전에 ProgressBar를 보여주어 사용자와 interact
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            boolean flag;
            Response response = null;
            OkHttpClient toServer;

            JSONObject jsonObject = null;

            try{
                toServer = OKHttp3ApplyCookieManager.getOkHttpNormalClient();


                Request request = new Request.Builder()
                        .url(String.format(getResources().getString(R.string.request_apt_size_url),region1,region2,region3,apt))
                        .get()
                        .build();

                //동기 방식
                response = toServer.newCall(request).execute();

                flag = response.isSuccessful();

                String returedJSON;

                if(flag){ //성공했다면
                    returedJSON = response.body().string();

                    try {
                        jsonObject = new JSONObject(returedJSON);
                    }catch(JSONException jsone){
                        Log.e("json에러", jsone.toString());
                    }
                }else{
                    return 2;
                }
            }catch (UnknownHostException une) {
            } catch (UnsupportedEncodingException uee) {
            } catch (Exception e) {
            } finally{
                if(response != null) {
                    response.close(); //3.* 이상에서는 반드시 닫아 준다.
                }
            }

            if(jsonObject != null){
                try {
                    if(jsonObject.get(getResources().getString(R.string.url_message)).equals(getResources().getString(R.string.url_success))){
                        JSONArray data= jsonObject.getJSONArray(getResources().getString(R.string.url_data));

                        for(int i = 0 ; i < data.length(); i++){
                            try {
                                JSONObject jsonData = (JSONObject)data.get(i);
                                stringArraylist.add(jsonData.getString("apt_size_supply") + "/" + jsonData.getString("apt_size_exclusive") + " (" + jsonData.getString("apt_price") + "만원)");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        return 0;
                    }else if(jsonObject.get(getResources().getString(R.string.url_message)).equals(getResources().getString(R.string.url_no_data))){
                        return 1;
                    }else{
                        return 3;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                return 4;
            }

            return 5;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if(result == 0){
                recyclerView.setAdapter(selectRegionRecyclerViewAdapter);

                selectRegionRecyclerViewAdapter.initItem(stringArraylist);
            }else if(result == 1){
            }else{
            }

            //마무리 된 이후에 ProgressBar 제거하고 SwipeRefreshLayout을 사용할 수 있게 설정
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if (requestAptSize.getStatus() == AsyncTask.Status.RUNNING) {
            requestAptSize.cancel(true);
        }
    }

}
