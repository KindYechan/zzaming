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

// currentUser = 지금 로그인한 1명 (메모리에만 있음, 앱 끄면 사라짐)
// 저장소(DataProvider.users()) = 가입한 전체 유저 (기기/서버에 남음)
// currentUser를 고치면 saveCurrentUser()로 저장소에도 반영해야 함

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

    // 콜백을 사용 : 서버로부터 데이터를 받았을 때 해당 메소드가 호출될 수 있도록
    // DataProvide라는 임시로 서버를 대체하는 공간으로부터 user 저장소를 호출한다
    // user라는 저장소는 interface로 login, signup등이 구현되어 있다.
    // 그리고 이는 LocalUserRepository에서 implemetation 된다.
    // 구현 방식은 이전에 구현한 방식과 동일.
    // 변화한 점은 이전에는 각각 따로 계층구조가 만들어져있었다면 이를 병합하여
    // DataProvider을 루트로 한 트리구조를 완성
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
    // LocalStore을 통해 저장한 데이터를 다시 불러와 로그인 정보를 다시 저장하는 작업
    public static void restoreLogin() {
        currentUser = DataProvider.users().getSavedLoginUser();
    }

    // currentUser 를 고친 뒤에는 꼭 불러 주세요 (내 정보 수정, 진급일, 행정반 가입 등)

    // 바뀐 유저정보를 다시 저장소에 저장하기 위해 사용하는 함수
    // 주로 EditProfileActivity 및 AdminEntryActivity 에서 사용합니다.
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

    // 여기서 행정반 을 나가면 유저 정보가 바뀌니깐 이렇게
    // saveCurrentUser을 부른거요
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
