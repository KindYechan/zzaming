package com.example.jjamyechan.data;

import com.example.jjamyechan.PromotionRule;

import java.util.Map;

/**
 * 부대별 진급 심사 규칙을 주고받는 창구. 규칙은 행정반(unitCode)마다 따로 저장돼요.
 *
 * 서버 담당이 만들 것: Firestore "units/{unitCode}/promotionRules/{rank}"
 */
public interface PromotionRuleRepository {

    // 그 부대의 계급별 규칙 전부 (저장된 게 없으면 빈 Map)
    void loadRules(String unitCode, DataCallback<Map<String, PromotionRule>> callback);

    void saveRule(String unitCode, String rank, PromotionRule rule, DataCallback<Void> callback);
}
