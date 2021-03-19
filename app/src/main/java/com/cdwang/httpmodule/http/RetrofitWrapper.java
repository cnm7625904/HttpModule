package com.cdwang.httpmodule.http;

import android.text.TextUtils;

import com.cdwang.httpmodule.global.CookieInfo;
import com.cdwang.httpmodule.global.GlobalHttpHandlerImpl;
import com.example.basemodule.base.utils.InterceptorUtils;
import com.example.basemodule.base.utils.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * copyright (C), 2021, 运达科技有限公司
 * fileName RetrofitWrapper
 *
 * @author 王玺权
 * date 2021-03-09 19:16
 * description
 * history
 */
public class RetrofitWrapper {
    private Retrofit mRetrofit;
    private OkHttpClient.Builder builder;

    /**
     * 获取实例，使用单利模式
     * 这里传递url参数，是因为项目中需要访问不同基类的地址
     * @param url               baseUrl
     * @return                  实例对象
     */

    public static RetrofitWrapper getInstance(String url){
        return getInstance(url,null);
    }
    private static volatile RetrofitWrapper instance = null;
    /**
     * 获取实例，使用单利模式  Android单例推荐双重校验锁，不推荐枚举单例
     * 这里传递url参数，是因为项目中需要访问不同基类的地址
     * @param url               baseUrl
     * @return                  实例对象
     */
    public static RetrofitWrapper getInstance(String url, ArrayList<Interceptor> interceptors){
        //synchronized 避免同时调用多个接口，导致线程并发

        if(instance == null){
            synchronized (RetrofitWrapper.class){
                if(instance == null){
                    instance = new RetrofitWrapper(url,interceptors);
                }
            }
        }
        return instance;


//        RetrofitWrapper instance;
//        synchronized (RetrofitWrapper.class){
//            instance = new RetrofitWrapper(url,interceptors);
//        }
//        return instance;
    }

    public void resetRetrofit(){
        builder=null;
        mRetrofit=null;
    }

    /**
     * 创建Retrofit
     */
    public RetrofitWrapper(final String url , ArrayList<Interceptor> interceptors) {
        builder =new OkHttpClient.Builder();
        //cookie 设置
        builder.cookieJar(new CookieJar() {
            @Override
            public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                //加锁 防止 多个请求 数据错误
//                synchronized (CookieStateUtil.class){
                if (CookieInfo.cookieStore.get(httpUrl.host()) == null || CookieInfo.cookieStore.get(httpUrl.host()).size() == 0) {
//                            Logger.d("cookie");
//                            Logger.json(new Gson().toJson(cookies));
                    CookieInfo.cookieStore.put(httpUrl.host(), list);

//                        CookieStateUtil.cacheOkhttpCookie(CookieInfo.cookieStore);
                }
//                }


            }

            @NotNull
            @Override
            public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                synchronized (CookieInfo.class){
//                synchronized (CookieStateUtil.class){
//                    HashMap<String, List<Cookie>> cookieMap = CookieStateUtil.getCachedOkhttpCookie();
                    List<Cookie> cookies = CookieInfo.cookieStore.get(httpUrl.host());
                    return cookies != null ? cookies : new ArrayList<Cookie>();
                }

            }
        });

//        builder.cookieJar(new JavaNetCookieJar(getCookieManager()));
        //禁止重定向 拿到最终返回结果
        builder.followRedirects(false);
        builder.followSslRedirects(false);
        /**
         * 拦截日志，重定向后拦截出错
         */
        builder.addInterceptor(new Interceptor() {
            @NotNull
            @Override
            public Response intercept(@NotNull Chain chain) throws IOException {
//                String baseUrl = SPUtils.getInstance().getString(HttpConstant.SP_TAG_BASE_URL);
                String baseUrl = "";
                CacheControl cacheControl = new CacheControl.Builder().build();
                //不使用缓存
                Request.Builder builder = chain.request().newBuilder().cacheControl(cacheControl);
                Request request=null;
                if(!TextUtils.isEmpty(baseUrl)){
                    if(!url.equals(baseUrl)){
                        request = builder.url(baseUrl+chain.request().url().toString().replaceAll(url,"").trim()).cacheControl(cacheControl)
                                .build();
                    }
                }
                Response response=chain.proceed(request==null?builder.build():request);
//                Log.e("返回码",response.code()+"???");
//                return response;

                return GlobalHttpHandlerImpl.getInstance().onHttpResultResponse(chain,response);

//                return chain.proceed(request==null?builder.build():request);
            }
        });
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(loggingInterceptor);
        //设置httpClient

        OkHttpClient build = builder.build();
        builder.addInterceptor(InterceptorUtils.getCacheInterceptor());
        if (interceptors!=null && interceptors.size()>0){
            for (Interceptor interceptor : interceptors){
                builder.addInterceptor(interceptor);
            }
        }



        initBuilder(url,build);
    }


    /**
     * 获取 client
     * @return
     */
    public  OkHttpClient.Builder getOkHttpClientBuilder(){

        return builder;
    }

    private void initBuilder(String url, OkHttpClient build) {
        initTimeOut();
        if(!false){
            //不需要错误重连
            builder.retryOnConnectionFailure(false);
        }else {
            //错误重连
            builder.retryOnConnectionFailure(true);
        }
        //获取实例
        mRetrofit = new Retrofit
                //设置OKHttpClient,如果不设置会提供一个默认的
                .Builder()
                //设置baseUrl
                .baseUrl(url)
                //添加转换器Converter(将json 转为JavaBean)，用来进行响应数据转化(反序列化)的ConvertFactory
                .addConverterFactory(GsonConverterFactory.create(JsonUtils.getJson()))
                //添加自定义转换器
                //添加rx转换器，用来生成对应"Call"的CallAdapter的CallAdapterFactory   //.addConverterFactory(buildGsonConverterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(build)
                .build();
    }


    public <T> T create(final Class<T> service) {
        return mRetrofit.create(service);
    }



    /**
     * 初始化完全信任的信任管理器
     */
    private static CookieManager cookieManager = new CookieManager();
    private static CookieManager getCookieManager() {
        if(cookieManager==null){
            synchronized (CookieManager.class){
                if(cookieManager==null){
                    cookieManager=new CookieManager();
                }
            }

        }
        return cookieManager;
    }

    /**
     * 设置读取超时时间，连接超时时间，写入超时时间值
     */
    private void initTimeOut() {
        builder.readTimeout(20000, TimeUnit.SECONDS);
        builder.connectTimeout(10000, TimeUnit.SECONDS);
        builder.writeTimeout(20000, TimeUnit.SECONDS);
        //错误重连
        builder.retryOnConnectionFailure(false);
    }


    /**
     * 构建GSON转换器
     * 这里，你可以自己去实现
     * @return GsonConverterFactory
     */
    private static GsonConverterFactory buildGsonConverterFactory(){
        GsonBuilder builder = new GsonBuilder();
        builder.setLenient();
        // 注册类型转换适配器
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)  {
                return null == json ? null : new Date(json.getAsLong());
            }
        });

        Gson gson = builder.create();
        return GsonConverterFactory.create(gson);
    }

}
