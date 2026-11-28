package com.example.jjamyechan.data;

import com.example.jjamyechan.AdminRoon;

/**
 * 행정반(부대) 정보를 주고받는 창구.
 *
 * 부대 코드(unitCode): 행정반을 만들 때 정해지는 6자리 코드. 대원들은 이 코드를 입력해서 가입해요.
 *
 * 서버 담당이 만들 것: FirebaseUnitRepository implements UnitRepository
 *   - createUnit → 겹치지 않는 6자리 코드를 만들어 Firestore "units/{unitCode}" 에 저장
 *   - getUnit    → "units/{unitCode}" 읽기 (가입할 때도 이걸로 코드가 맞는지 확인해요)
 *   - saveUnit   → "units/{unitCode}" 덮어쓰기
 */

// 행정반 생성, 가입, 정보 수정시 저장에 사용되는 인터페이스입니다.
// 구현은 LocalUnitRepository에서 이뤄졌습니다. 아마 서버 구현되면 사라질듯

public interface UnitRepository {

    // 새 행정반 만들기. 부대 코드를 만들어 채운 뒤 돌려줘요
    void createUnit(AdminRoon unit, DataCallback<AdminRoon> callback);

    // 부대 코드로 행정반 정보 읽기. 없는 코드면 onFailure
    void getUnit(String unitCode, DataCallback<AdminRoon> callback);

    // 행정반 정보 저장 (인원 수, 기능 켜고 끄기 등)
    void saveUnit(AdminRoon unit, DataCallback<Void> callback);
}
