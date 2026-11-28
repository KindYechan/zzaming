package com.example.jjamyechan.data;

import android.content.Context;

import com.example.jjamyechan.AdminRoon;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Random;

/**
 * 서버 대신 기기 안에 행정반을 저장하는 구현.
 * 같은 폰에서는 A 계정으로 만든 행정반에 B 계정으로 부대 코드를 입력해 가입해 볼 수 있어요.
 */

// 지금은 Local로 행정반 정보가 저장되어 있어서
// 같은 핸드폰 다른계정이 아닌 이상 부대 코드를 통한 가입은 불가능합니다.

public class LocalUnitRepository implements UnitRepository {
    private static final String KEY_UNITS = "units";   // 부대코드 → AdminRoon

    private final LocalStore store;
    private final HashMap<String, AdminRoon> units;

    public LocalUnitRepository(Context context) {
        store = new LocalStore(context, "jjam_units");
        HashMap<String, AdminRoon> saved = store.get(KEY_UNITS, new TypeToken<HashMap<String, AdminRoon>>() {}.getType());
        units = (saved != null) ? saved : new HashMap<>();
    }

    @Override
    public void createUnit(AdminRoon unit, DataCallback<AdminRoon> callback) {
        unit.setUnitCode(makeNewCode());
        unit.setMemberCount(1);
        units.put(unit.getUnitCode(), unit);
        store.put(KEY_UNITS, units);
        callback.onSuccess(unit);
    }

    @Override
    public void getUnit(String unitCode, DataCallback<AdminRoon> callback) {
        AdminRoon unit = units.get(unitCode);
        if (unit == null) {
            callback.onFailure(new Exception("행정반 정보를 찾을 수 없어요"));
        } else {
            callback.onSuccess(unit);
        }
    }

    @Override
    public void saveUnit(AdminRoon unit, DataCallback<Void> callback) {
        units.put(unit.getUnitCode(), unit);
        store.put(KEY_UNITS, units);
        if (callback != null) callback.onSuccess(null);
    }

    // 대원들이 입력하기 쉬운 6자리 부대 코드 만들기. 예) "A7K2QX"
    // 헷갈리는 글자(0과 O, 1과 I)는 뺐어요. 이미 있는 코드가 나오면 다시 뽑아요.
    private String makeNewCode() {
        String letters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(letters.charAt(random.nextInt(letters.length())));
            }
            code = sb.toString();
        } while (units.containsKey(code));
        return code;
    }
}
