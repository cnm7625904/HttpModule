package com.cdwang.httpmodule.global;

import com.example.basemodule.base.basebean.BaseResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * copyright (C), 2020, 运达科技有限公司
 * fileName SessionApi
 *
 * @author 王玺权
 * date 2020-12-11 14:15
 * description
 * history
 */
public interface SessionApi {
    /**
     * 获取新的session
     * @param userId 用户名
     * @param passWord 密码
     * @return
     */
    @FormUrlEncoded
    @POST("app/appLogin.action")
    Call<ResponseBody> getNewSession(@Field("userId") String userId, @Field("passWord") String passWord);

    @FormUrlEncoded
    @POST("app/appLogin.action")
    Call<BaseResponse> getTest(@Field("userId") String userId, @Field("passWord") String passWord, @Field("IMEI") String IMEI);
}
