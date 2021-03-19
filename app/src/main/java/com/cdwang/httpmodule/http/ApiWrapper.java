package com.cdwang.httpmodule.http;


/**
 * copyright (C), 2021, 运达科技有限公司
 * fileName ApiWrapper
 *
 * @author 王玺权
 * date 2021-03-10 13:14
 * description 接口代理类
 * history
 */
public class ApiWrapper {

    public static <T> T getInstance(Class<T> type){
        T t = RetrofitWrapper
                .getInstance("", null)
                .create(type);
        return t;
    }

//    public <T> T getParcelable(@NonNull final String key,
//                               @NonNull final Parcelable.Creator<T> creator) {
}
//    public static <T> T fromJson(final String json, final Class<T> type) {
//        return GSON.fromJson(json, type);
//    }
