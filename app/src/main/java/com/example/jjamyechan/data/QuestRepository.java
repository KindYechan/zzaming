package com.example.jjamyechan.data;

import com.example.jjamyechan.quest.Quest;
import com.example.jjamyechan.quest.QuestBundle;
import com.example.jjamyechan.quest.QuestProgress;

import java.util.ArrayList;
import java.util.List;

/**
 * 퀘스트(묶음, 퀘스트, 완료 여부)를 주고받는 창구.
 * 묶음·퀘스트는 행정반(unitCode)마다, 완료 여부는 유저마다 따로 저장돼요.
 *
 * 서버 담당이 만들 것:
 *   - "units/{unitCode}/questBundles", "units/{unitCode}/quests"
 *   - "users/{email}/questProgress"
 */
public interface QuestRepository {

    // 저장된 게 하나도 없으면 null 을 돌려줘요 (그때는 QuestViewModel 이 예시 데이터를 채워요)
    void load(String unitCode, String userEmail, DataCallback<QuestData> callback);

    void save(String unitCode, String userEmail, QuestData data, DataCallback<Void> callback);

    /** 한 번에 주고받는 퀘스트 데이터 묶음 */
    class QuestData {
        public List<QuestBundle> bundles = new ArrayList<>();
        public List<Quest> quests = new ArrayList<>();
        public List<QuestProgress> progresses = new ArrayList<>();
    }
}
