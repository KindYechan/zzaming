package com.example.jjamyechan.data;

import android.content.Context;

import com.example.jjamyechan.community.CommunityRepository;
import com.example.jjamyechan.community.MockServerDataSource;

/**
 * 앱 전체에서 데이터 창구(Repository)를 꺼내 쓰는 곳.
 *
 * ★ 서버를 붙일 때 바꿀 곳은 여기 init() 안의 new Local...(...) 줄들뿐이에요. ★
 *   예) users = new FirebaseUserRepository();
 * 화면 코드는 DataProvider.users() 처럼 인터페이스만 쓰기 때문에 고칠 필요가 없어요.
 */
public class DataProvider {
    private static UserRepository users;
    private static UnitRepository units;
    private static PromotionRuleRepository promotionRules;
    private static QuestRepository quests;
    private static CommunityRepository community;

    // JjamApp(앱이 켜질 때 가장 먼저 실행되는 클래스)에서 한 번 불러요
    public static void init(Context context) {
        Context app = context.getApplicationContext();
        users = new LocalUserRepository(app);
        units = new LocalUnitRepository(app);
        promotionRules = new LocalPromotionRuleRepository(app);
        quests = new LocalQuestRepository(app);
        community = MockServerDataSource.getInstance(app);
    }

    public static UserRepository users() {
        return users;
    }

    public static UnitRepository units() {
        return units;
    }

    public static PromotionRuleRepository promotionRules() {
        return promotionRules;
    }

    public static QuestRepository quests() {
        return quests;
    }

    public static CommunityRepository community() {
        return community;
    }
}
