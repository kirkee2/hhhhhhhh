package com.beta.tacademy.hellomoneycustomer.common;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.beta.tacademy.hellomoneycustomer.R;

/**
 * Created by user on 2017. 12. 8..
 */

public abstract class CommonBaseActivity extends AppCompatActivity {
    abstract protected void initView();
    abstract protected void initVariable();
    abstract protected void initListener();
    abstract protected void initNetwork();
    abstract protected void stopNetWork();
    abstract public void progressOnGoing(boolean isOngoing);
    abstract protected void startNetwork();
    abstract protected void networkNotWorking();
    abstract protected void isNetworkSuccess();
}
