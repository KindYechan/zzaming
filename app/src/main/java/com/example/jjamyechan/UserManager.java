package com.example.jjamyechan;

import com.example.jjamyechan.data.DataCallback;
import com.example.jjamyechan.data.DataProvider;

/**
 * "지금 로그인한 사람"을 기억하고, 로그인·회원가입을 처리하는 곳.
 *
 * [병합 시 변경]
 * 예전에는 이 클래스 안의 HashMap 에 유저를 보관해서 앱을 끄면 다 사라졌어요.
 * 이제 실제 보관은 DataProvider.users() (지금은 기기 저장소, 나중에는 서버)가 하고,
 * 이 클래스는 지금 로그인한 사람(currentUser)만 기억해요.
 * 서버는 결과가 나중에 오기 때문에 login / signup 은 boolean 대신 콜백으로 결과를 돌려줘요.
 */
public class UserManager {

    // 지금 로그인한 유저. 로그인 전이거나 로그아웃했으면 null
    private static User currentUser = null;

    // 회원가입 (성공하면 바로 로그인된 상태가 돼요)
    public static void signup(User user, String password, DataCallback<User> callback) {
        DataProvider.users().signup(user, password, new DataCallback<User>() {
            @Override
            public void onSuccess(User result) {
                currentUser = result;
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // 로그인
    public static void login(String username, String password, DataCallback<User> callback) {
        DataProvider.users().login(username, password, new DataCallback<User>() {
            @Override
            public void onSuccess(User result) {
                currentUser = result;
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // 앱을 다시 켰을 때 지난번 로그인 이어가기 (JjamApp 에서 불러요)
    public static void restoreLogin() {
        currentUser = DataProvider.users().getSavedLoginUser();
    }

    // currentUser 를 고친 뒤에는 꼭 불러 주세요 (내 정보 수정, 진급일, 행정반 가입 등)
    public static void saveCurrentUser() {
        if (currentUser != null) {
            DataProvider.users().saveUser(currentUser, null);
        }
    }

    // 로그아웃
    public static void logout() {
        currentUser = null;
        DataProvider.users().logout();
    }

    // 행정반 나가기
    public static void leaveUnit() {
        getCurrentUser().setUnitCode(null);
        getCurrentUser().setRole(null);
        saveCurrentUser();
    }

    // 지금 로그인한 유저 리턴
    public static User getCurrentUser() {
        return currentUser;
    }

    // 지금 로그인 상태인지
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
