package com.cdwang.httpmodule.global;

import android.text.TextUtils;
import android.util.Log;


import com.blankj.utilcode.util.CacheDiskUtils;
import com.blankj.utilcode.util.SPUtils;
import com.example.basemodule.base.baseConstant.HttpConstant;
import com.example.basemodule.base.utils.Md5Utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

/**
 * @author  王玺权
 * @date 2020年12月14日11:47:18
 * 展示 {@link GlobalHttpHandler} 的用法
 * 以前上传方法有2种不同的写法 1、使用根据上传进度 更新 realm 表中数据，再监听 表的数据变化展现在UI上  2、使用第三方框架 监听
 * http://github.com/JessYanCoding/ProgressManager/blob/master/README-zh.md   AtomicBoolean res = new AtomicBoolean(false);
 */
public class GlobalHttpHandlerImpl implements GlobalHttpHandler {
    /** 与服务器约定session过期 */
    int code=302;
    private static volatile  GlobalHttpHandlerImpl globalHttpHandler=null;
    public static GlobalHttpHandlerImpl getInstance(){
        if(globalHttpHandler==null){
            synchronized (GlobalHttpHandler.class){
                if(globalHttpHandler==null){
                    globalHttpHandler=new GlobalHttpHandlerImpl();
                }
            }
        }
        return globalHttpHandler;
    }

    /**
     * 这里可以先客户端一步拿到每一次 Http 请求的结果, 可以先解析成 Json, 再做一些操作, 如检测到 token 过期后
     * 重新请求 token, 并重新执行请求
     *
     *   httpResult 服务器返回的结果 (已被框架自动转换为字符串)
     * @param chain      {@link Interceptor.Chain}
     * @param response   {@link Response}
     * @return
     */
    @Override
    public Response onHttpResultResponse( Interceptor.Chain chain, Response response) {

        //token失效
        if(isTokenExpired(response)) {
            Log.e("session已过期","重新获取，重新获取");
            //清除过期session
            CookieInfo.cookieStore.clear();
            //获取最新token
            getNewSession();
            //使用新的Session，创建新的请求
            Request newRequest = chain.request()
                    .newBuilder()
                    .build();
            //重新请求
            try {
                return chain.proceed(newRequest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return response;
    }

    /**
     * 这里可以在请求服务器之前拿到 {@link Request}, 做一些操作比如给 {@link Request} 统一添加 token 或者 header 以及参数加密等操作
     *
     * @param chain   {@link Interceptor.Chain}
     * @param request {@link Request}
     * @return
     */
    @Override
    public Request onHttpRequestBefore(Interceptor.Chain chain, Request request) {
        /* 如果需要再请求服务器之前做一些操作, 则重新返回一个做过操作的的 Request 如增加 Header, 不做操作则直接返回参数 request
        return chain.request().newBuilder().header("token", tokenId)
                              .build(); */
        return request;
    }


    private boolean isTokenExpired(Response response) {
        if (response.code() == code) {
            return true;
        }
        return false;
    }
    private void getNewSession()  {
        // 通过一个特定的接口获取新的token，此处要用到同步的retrofit请求
        String username = "";
        String password = "";

        String jsonStr= CacheDiskUtils.getInstance().getString("uNameAndPwd");
        JSONObject jsonObject=null;
        try {
            jsonObject =new JSONObject(jsonStr);
            username=jsonObject.getString("uName");
            password=jsonObject.getString("pwd");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.e("获取的账号密码",username+"==="+password);

        String url = SPUtils.getInstance().getString(HttpConstant.SP_TAG_BASE_URL);

        OkHttpClient.Builder builder;
        builder =new OkHttpClient.Builder();


        builder.cookieJar(new CookieJar() {
            @Override
            public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                if (CookieInfo.cookieStore.get(httpUrl.host()) == null || CookieInfo.cookieStore.get(httpUrl.host()).size() == 0) {
                    CookieInfo.cookieStore.put(httpUrl.host(), list);
                }
            }

            @NotNull
            @Override
            public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                synchronized (CookieInfo.class){
                    List<Cookie> cookies = CookieInfo.cookieStore.get(httpUrl.host());
                    return cookies != null ? cookies : new ArrayList<Cookie>();
                }

            }
        });

        builder.addInterceptor(new Interceptor() {
            @NotNull
            @Override
            public Response intercept(@NotNull Chain chain) throws IOException {
                Request.Builder builder = chain.request().newBuilder();
                Request request=builder.build();
                Response response=chain.proceed(request);
                return response;
            }
        });
        OkHttpClient build = builder.build();
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl(TextUtils.isEmpty(url)? HttpConstant.BASE_URL:url)
                .client(build)
                .build();
        SessionApi sessionApi= retrofit.create(SessionApi.class);
        Call<ResponseBody> call = sessionApi.getNewSession(Md5Utils.convertMD5(username), Md5Utils.convertMD5(password));
        try {
            call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
