package com.example.jjamyechan.data;

/**
 * 서버(또는 로컬 저장소)에 요청한 결과를 돌려받는 통로.
 *
 * Firebase 같은 서버는 결과가 "나중에" 와요. 그래서 메서드가 값을 바로 return 하지 않고,
 * 결과가 준비되면 onSuccess / onFailure 를 불러 줘요.
 * 지금 쓰는 로컬 저장소는 바로 결과를 주지만, 모양을 미리 서버와 똑같이 맞춰 둔 거예요.
 * 그래서 나중에 서버를 붙여도 화면(Activity) 코드는 바꿀 필요가 없어요.
 */
public interface DataCallback<T> {
    void onSuccess(T result);

    void onFailure(Exception e);
}
