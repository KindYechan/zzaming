package com.example.jjamyechan.data;

import android.content.Context;

import com.example.jjamyechan.User;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * 서버 대신 기기 안(SharedPreferences)에 유저를 저장하는 구현.
 * 앱을 껐다 켜도 가입한 계정과 로그인 상태가 남아 있어요.
 *
 * 주의: 비밀번호를 기기에 그대로 저장하는 건 테스트용이에요. 서버(Firebase Auth)를 붙이면 지워집니다.
 */
public class LocalUserRepository implements UserRepository {
    private static final String KEY_USERS = "users";          // 아이디 → User
    private static final String KEY_PASSWORDS = "passwords";  // 아이디 → 비밀번호
    private static final String KEY_LOGIN = "login_email";    // 로그인 유지용

    private final LocalStore store;
    private final HashMap<String, User> users;
    private final HashMap<String, String> passwords;

    public LocalUserRepository(Context context) {
        store = new LocalStore(context, "jjam_users");

        Type userMap = new TypeToken<HashMap<String, User>>() {}.getType();
        Type passwordMap = new TypeToken<HashMap<String, String>>() {}.getType();
        HashMap<String, User> savedUsers = store.get(KEY_USERS, userMap);
        HashMap<String, String> savedPasswords = store.get(KEY_PASSWORDS, passwordMap);
        users = (savedUsers != null) ? savedUsers : new HashMap<>();
        passwords = (savedPasswords != null) ? savedPasswords : new HashMap<>();

        addTestAccounts();
    }

    // 테스트용 계정 (test / 12345678, test2 / 12345678). 제출 전에는 지우기
    private void addTestAccounts() {
        if (!users.containsKey("test")) {
            User test = new User("test", "테스트", "soldier", "private", "2026-01-01", "2027-07-01");
            test.setPfcDate("2026-04-01");
            users.put("test", test);
            passwords.put("test", "12345678");
        }
        if (!users.containsKey("test2")) {
            User test2 = new User("test2", "간부테스트", "officer", null, "2026-01-01", "2027-07-01");
            users.put("test2", test2);
            passwords.put("test2", "12345678");
        }
        saveAll();
    }

    @Override
    public void signup(User user, String password, DataCallback<User> callback) {
        if (users.containsKey(user.getEmail())) {
            callback.onFailure(new DuplicateException());
            return;
        }
        users.put(user.getEmail(), user);
        passwords.put(user.getEmail(), password);
        saveAll();
        store.putString(KEY_LOGIN, user.getEmail());
        callback.onSuccess(user);
    }

    @Override
    public void login(String email, String password, DataCallback<User> callback) {
        User user = users.get(email);
        if (user == null || !password.equals(passwords.get(email))) {
            callback.onFailure(new Exception("아이디 또는 비밀번호가 틀렸어요"));
            return;
        }
        store.putString(KEY_LOGIN, email);
        callback.onSuccess(user);
    }

    @Override
    public void saveUser(User user, DataCallback<Void> callback) {
        users.put(user.getEmail(), user);
        saveAll();
        if (callback != null) callback.onSuccess(null);
    }


    // 처음 로그인하면 저장된 local데이터에서 email 정보를 가져온다.
    // 저장된 정보가 없으면 x 있으면 복원
    @Override
    public User getSavedLoginUser() {
        String email = store.getString(KEY_LOGIN);
        return (email == null) ? null : users.get(email);
    }

    @Override
    public void logout() {
        store.remove(KEY_LOGIN);
    }

    private void saveAll() {
        store.put(KEY_USERS, users);
        store.put(KEY_PASSWORDS, passwords);
    }
}
