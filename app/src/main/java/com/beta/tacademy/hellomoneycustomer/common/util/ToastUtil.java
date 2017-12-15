package com.beta.tacademy.hellomoneycustomer.common.util;

import android.content.Context;
import android.widget.Toast;

import com.beta.tacademy.hellomoneycustomer.activity.IntroActivity;

/**
 * Created by user on 2017. 12. 8..
 */

public class ToastUtil {

    public static void permissionCheckError(Context mContext){
        Toast.makeText(mContext, "진행중 오류가 발생했습니다. 다시 한번 시도해주세요.", Toast.LENGTH_SHORT).show();
    }

    public static void networkError(Context mContext){
        Toast.makeText(mContext, "네트워크 처리 도중 에러가 발생하였습니다.", Toast.LENGTH_LONG).show();
    }

    public static void moreThanTwoQuotation(Context mContext){
        Toast.makeText(mContext,"진행 중인 견적이 2개 이상이셔서 더 이상 견적 요청을 할 수 없습니다.",Toast.LENGTH_SHORT).show();
    }
}
