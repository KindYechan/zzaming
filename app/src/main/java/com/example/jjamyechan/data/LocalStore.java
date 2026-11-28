package com.example.jjamyechan.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * 기기 안에 객체를 JSON 글자로 저장하는 작은 도우미 (SharedPreferences + Gson).
 * Local...Repository 들이 같이 써요. 서버를 붙이면 이 클래스는 필요 없어져요.
 */

// local로 데이터를 저장하기 위해 사용

public class LocalStore {
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public LocalStore(Context context, String fileName) {
        prefs = context.getApplicationContext().getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }

    // 객체 → JSON 글자로 저장
    public void put(String key, Object value) {
        prefs.edit().putString(key, gson.toJson(value)).apply();
    }

    // JSON 글자 → 객체 (저장된 게 없으면 null)
    public <T> T get(String key, Type type) {
        String json = prefs.getString(key, null);
        if (json == null) return null;
        return gson.fromJson(json, type);
    }

    public void putString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    public String getString(String key) {
        return prefs.getString(key, null);
    }

    public void remove(String key) {
        prefs.edit().remove(key).apply();
    }
}
