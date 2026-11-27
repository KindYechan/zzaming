package com.example.jjamyechan.data;

import com.example.jjamyechan.User;

/**
 * 유저 정보를 주고받는 창구. 화면 코드는 UserManager 를 통해 이 인터페이스만 써요.
 *
 * 지금 구현: LocalUserRepository (기기 안에 저장)
 * 서버 담당이 만들 것: FirebaseUserRepository implements UserRepository
 *   - signup → FirebaseAuth.createUserWithEmailAndPassword + Firestore "users/{email}" 저장
 *   - login  → FirebaseAuth.signInWithEmailAndPassword + Firestore 에서 User 읽기
 *   - saveUser → Firestore "users/{email}" 덮어쓰기
 * 그다음 DataProvider 에서 new LocalUserRepository(...) 한 줄만 바꾸면 돼요.
 */
public interface UserRepository {

    // 회원가입. 같은 아이디가 있으면 onFailure(DuplicateException)
    void signup(User user, String password, DataCallback<User> callback);

    // 로그인. 아이디가 없거나 비밀번호가 틀리면 onFailure
    void login(String email, String password, DataCallback<User> callback);

    // 바뀐 유저 정보 저장 (내 정보 수정, 진급일, 행정반 가입/탈퇴 등)
    void saveUser(User user, DataCallback<Void> callback);

    // 로그인 유지: 앱을 다시 켰을 때 마지막으로 로그인한 유저 (없으면 null)
    User getSavedLoginUser();

    void logout();

    /** 이미 있는 아이디로 가입하려 할 때 */
    class DuplicateException extends Exception {
        public DuplicateException() {
            super("이미 있는 아이디예요");
        }
    }
}
