package com.example.jjamyechan;

import android.app.Application;

import com.example.jjamyechan.data.DataProvider;

/**
 * 앱이 켜질 때 어떤 Activity 보다도 먼저 한 번 실행되는 클래스.
 * (AndroidManifest.xml 의 application 태그에 android:name=".JjamApp" 으로 등록돼 있어요)
 * 여기서 데이터 창구를 준비하고, 지난번에 로그인했던 사람을 다시 로그인시켜요.
 */

// 앱이 실행되면 일단 얘부터
// 데이터 저장고를 불러오고, Local로 저장된 로그인 정보를 다시 불러온다
public class JjamApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DataProvider.init(this);
        UserManager.restoreLogin();
    }
}
