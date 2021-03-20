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
                .getInstance("http://127.0.0.1", null)
                .create(type);
        return t;
    }

}

