package com.cdwang.httpmodule.http;

/**
 * copyright (C), 2021, 运达科技有限公司
 * fileName ModelWrapper
 *
 * @author 王玺权
 * date 2021-03-10 13:36
 * description
 * history
 */
public class ModelWrapper {

    public static <T> T getInstance(Class<T> type) throws InstantiationException, IllegalAccessException {
        if(type == null) {
            synchronized ((T)type.newInstance()){
                if(type == null){
                    type = (Class<T>) type.newInstance();
                }
            }
        }
          return (T) type;
    }
}
