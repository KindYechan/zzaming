package com.example.jjamyechan.data;

import android.content.Context;

import com.example.jjamyechan.quest.Quest;
import com.example.jjamyechan.quest.QuestBundle;
import com.example.jjamyechan.quest.QuestProgress;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/** 서버 대신 기기 안에 퀘스트를 저장하는 구현 */
public class LocalQuestRepository implements QuestRepository {
    private final LocalStore store;

    public LocalQuestRepository(Context context) {
        store = new LocalStore(context, "jjam_quests");
    }

    @Override
    public void load(String unitCode, String userEmail, DataCallback<QuestData> callback) {
        List<QuestBundle> bundles = store.get("bundles_" + unit(unitCode), new TypeToken<List<QuestBundle>>() {}.getType());
        List<Quest> quests = store.get("quests_" + unit(unitCode), new TypeToken<List<Quest>>() {}.getType());
        if (bundles == null || quests == null) {
            callback.onSuccess(null);   // 아직 저장된 게 없음
            return;
        }
        List<QuestProgress> progresses = store.get(progressKey(unitCode, userEmail), new TypeToken<List<QuestProgress>>() {}.getType());

        QuestData data = new QuestData();
        data.bundles.addAll(bundles);
        data.quests.addAll(quests);
        if (progresses != null) data.progresses.addAll(progresses);
        callback.onSuccess(data);
    }

    @Override
    public void save(String unitCode, String userEmail, QuestData data, DataCallback<Void> callback) {
        store.put("bundles_" + unit(unitCode), data.bundles);
        store.put("quests_" + unit(unitCode), data.quests);
        store.put(progressKey(unitCode, userEmail), data.progresses);
        if (callback != null) callback.onSuccess(null);
    }

    private String unit(String unitCode) {
        return unitCode == null ? "none" : unitCode;
    }

    private String progressKey(String unitCode, String userEmail) {
        return "progress_" + unit(unitCode) + "_" + userEmail;
    }
}
