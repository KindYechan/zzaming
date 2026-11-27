package com.example.jjamyechan.data;

import android.content.Context;

import com.example.jjamyechan.AdminRoon;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

/**
 * 서버 대신 기기 안에 행정반을 저장하는 구현.
 * 같은 폰에서는 A 계정으로 만든 행정반에 B 계정으로 닉네임을 입력해 가입해 볼 수 있어요.
 */
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
        if (unit.getNickname() != null && !unit.getNickname().isEmpty() && findLocal(unit.getNickname()) != null) {
            callback.onFailure(new Exception("이미 있는 행정반 닉네임이에요"));
            return;
        }
        unit.setUnitCode("unit_" + System.currentTimeMillis());   // 서버라면 문서 id 를 쓰면 돼요
        unit.setMemberCount(1);
        units.put(unit.getUnitCode(), unit);
        store.put(KEY_UNITS, units);
        callback.onSuccess(unit);
    }

    @Override
    public void findByNickname(String nickname, DataCallback<AdminRoon> callback) {
        AdminRoon unit = findLocal(nickname);
        if (unit == null) {
            callback.onFailure(new Exception("그런 행정반이 없어요"));
        } else {
            callback.onSuccess(unit);
        }
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

    private AdminRoon findLocal(String nickname) {
        for (AdminRoon unit : units.values()) {
            if (nickname.equals(unit.getNickname())) return unit;
        }
        return null;
    }
}
