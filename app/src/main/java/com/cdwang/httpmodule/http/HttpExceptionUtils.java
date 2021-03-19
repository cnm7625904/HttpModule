package com.cdwang.httpmodule.http;


import com.example.basemodule.base.baseView.BaseView;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import retrofit2.HttpException;

/**
 * copyright (C), 2020, 运达科技有限公司
 * fileName HttpExceptionUtils
 *
 * @author 王玺权
 * date 2020-11-27 15:29
 * description 常见http eroor https://blog.csdn.net/qq_37819347/article/details/79971435
 * history
 */
public class HttpExceptionUtils {
    private BaseView baseView;
    /**
     *  获取异常
     * @param throwable 异常
     */
    public static void serverException(BaseView mView, Throwable e){
        if (mView == null) {
            return;
        }
        if (e instanceof ApiException) {
            mView.showErrorMsg(((ApiException) e).getMsg());
        }
//        else if (e instanceof HttpException) {
//            HttpException httpException = (HttpException) e;
//            String errorMsg=HttperrorMsg.getInstance().getErrorMsg(httpException.code());
//            switch (errorMsg){
//                case ErrormsgConstant.MSG_302:
//                    LoginProviderImpl.getInstance().reStart(AppManager.getAppManager().currentActivity());
//
//                    break;
//                default:
//                    break;
//            }
//            mView.showErrorMsg(errorMsg);
//            e.printStackTrace();
//        }
        else  if (e instanceof SocketTimeoutException) {
            mView.showErrorMsg("网络连接超时");
            e.printStackTrace();
        }else if(e instanceof ConnectException){
            mView.showErrorMsg("网络连接错误");
        }else if(e instanceof SocketException){
            mView.showErrorMsg("无法连接到网络");
        }
        else {
            mView.showErrorMsg("暂无数据");
        }
        mView.hidePro();
    }

    public static void showErrorInfo(HttpException exception){
        switch (exception.code()){

            default:
                break;
        }
    }


}
