package com.example.jjamyechan.data;

import com.example.jjamyechan.AdminRoon;

/**
 * 행정반(부대) 정보를 주고받는 창구.
 *
 * 서버 담당이 만들 것: FirebaseUnitRepository implements UnitRepository
 *   - createUnit → Firestore "units/{unitCode}" 저장
 *   - findByNickname → "units" 에서 nickname 이 같은 문서 찾기 (whereEqualTo)
 *   - getUnit → "units/{unitCode}" 읽기
 */
public interface UnitRepository {

    // 새 행정반 만들기. unitCode 를 만들어 채운 뒤 돌려줘요. 같은 닉네임이 있으면 onFailure
    void createUnit(AdminRoon unit, DataCallback<AdminRoon> callback);

    // 가입할 때: 닉네임으로 행정반 찾기. 없으면 onFailure
    void findByNickname(String nickname, DataCallback<AdminRoon> callback);

    // 부대 코드로 행정반 정보 읽기. 없으면 onFailure
    void getUnit(String unitCode, DataCallback<AdminRoon> callback);

    // 행정반 정보 저장 (인원 수, 기능 켜고 끄기 등)
    void saveUnit(AdminRoon unit, DataCallback<Void> callback);
}
