package com.example.jjamyechan.data;

import android.content.Context;

import com.example.jjamyechan.PromotionRule;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

/** 서버 대신 기기 안에 진급 규칙을 저장하는 구현. 키: "rules_{unitCode}" */
public class LocalPromotionRuleRepository implements PromotionRuleRepository {
    private final LocalStore store;

    public LocalPromotionRuleRepository(Context context) {
        store = new LocalStore(context, "jjam_promotion_rules");
    }

    @Override
    public void loadRules(String unitCode, DataCallback<Map<String, PromotionRule>> callback) {
        callback.onSuccess(read(unitCode));
    }

    @Override
    public void saveRule(String unitCode, String rank, PromotionRule rule, DataCallback<Void> callback) {
        HashMap<String, PromotionRule> rules = read(unitCode);
        rules.put(rank, rule);
        store.put(key(unitCode), rules);
        if (callback != null) callback.onSuccess(null);
    }

    private HashMap<String, PromotionRule> read(String unitCode) {
        HashMap<String, PromotionRule> rules = store.get(key(unitCode), new TypeToken<HashMap<String, PromotionRule>>() {}.getType());
        return (rules != null) ? rules : new HashMap<>();
    }

    // 행정반에 가입하지 않은 사람은 "none" 칸을 써요
    private String key(String unitCode) {
        return "rules_" + (unitCode == null ? "none" : unitCode);
    }
}
