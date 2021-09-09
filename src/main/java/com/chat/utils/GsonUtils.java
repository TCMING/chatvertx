package com.chat.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @Author tianchengming
 * @Date 2021年4月1日 21:07
 * @Version 1.0
 */
public class GsonUtils {

    private static final Logger logger = LoggerFactory.getLogger(GsonUtils.class);

    private static Gson gson = null;

    static {
        gson = new Gson();
    }

    /**
     * bean to json
     *
     * @param obj
     * @return
     */
    public static String toJsonString(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * 将gsonString转成泛型bean
     *
     * @param gsonString
     * @param cls
     * @return
     */
    public static <T> T jsonToBean(String gsonString, Class<T> cls) {
        T t = null;
        if (gson != null) {
            t = gson.fromJson(gsonString, cls);
        }
        return t;
    }

    /**
     * json对象转class
     * @param jsonObject
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T jsonObjectToBean(JsonObject jsonObject, Class<T> cls) {
        T t = null;
        if (gson != null) {
            t = gson.fromJson(jsonObject, cls);
        }

        return t;
    }

    /**
     * 转成list
     * 解决泛型问题
     * @param json
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> List<T> jsonToList(String json, Class<T> cls) {
        Gson gson = new Gson();
        List<T>   list  = new ArrayList<T>();
        JsonArray array = new JsonParser().parse(json).getAsJsonArray();
        for(final JsonElement elem : array){
            list.add(gson.fromJson(elem, cls));
        }
        return list;
    }




    /**
     * 转成list中有map的
     *
     * @param gsonString
     * @return
     */
    public static <T> List<Map<String, T>> jsonToListMaps(String gsonString) {
        List<Map<String, T>> list = null;
        if (gson != null) {
            list = gson.fromJson(gsonString,
                    new TypeToken<List<Map<String, T>>>() {
                    }.getType());
        }
        return list;
    }


    /**
     * 转成map的
     *
     * @param gsonString
     * @return
     */
    public static <T> Map<String, T> jsonToMaps(String gsonString) {
        Map<String, T> map = null;
        if (gson != null) {
            map = gson.fromJson(gsonString, new TypeToken<Map<String, T>>() {
            }.getType());
        }
        return map;
    }

    /**
     * Bean对象转换Map（基于JDK实现）
     * @param bean
     * @return
     */
    public static <T> Map<String,String> bean2Map(T bean){
        if(bean == null) {
            return null;
        }
        try {
            JsonElement jsonElement = gson.toJsonTree(bean);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> sets = jsonObject.entrySet();
            Map<String,String> map = new HashMap<>();
            sets.forEach(entry -> {
                map.put(entry.getKey(),entry.getValue().toString());
            });
            return map;
        }catch (Exception e){
            return null;
        }

    }

}
