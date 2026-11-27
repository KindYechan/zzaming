package com.example.jjamyechan.quest;

import com.example.jjamyechan.R;
import com.example.jjamyechan.User;
import com.example.jjamyechan.UserManager;
import com.example.jjamyechan.data.DataCallback;
import com.example.jjamyechan.data.DataProvider;
import com.example.jjamyechan.data.QuestRepository;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class QuestViewModel extends ViewModel{
    private final List<QuestBundle> questBundles = new ArrayList<>();
    private final List<Quest> quests = new ArrayList<>();
    private final List<QuestProgress> questProgresses = new ArrayList<>();
    private MutableLiveData<QuestRank> selectedRank = new MutableLiveData<>(QuestRank.FIRST_CLASS);


    // [병합 시 추가] 저장소(나중에는 서버)에서 퀘스트를 읽어 와요. 저장된 게 없으면 아래 예시 데이터로 시작해요.
    public QuestViewModel() {
        DataProvider.quests().load(unitCode(), userEmail(), new DataCallback<QuestRepository.QuestData>() {
            @Override
            public void onSuccess(QuestRepository.QuestData data) {
                if (data == null) {
                    addSampleData();
                    return;
                }
                questBundles.addAll(data.bundles);
                quests.addAll(data.quests);
                questProgresses.addAll(data.progresses);
                // 새로 생긴 퀘스트에는 완료 기록이 없으니 만들어 줘요
                for (Quest quest : quests) {
                    if (findProgress(quest.getQuestId()) == null) {
                        questProgresses.add(new QuestProgress(quest.getQuestId()));
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                addSampleData();
            }
        });
    }

    // 지금 목록을 저장소에 저장 (QuestActivity 의 onPause 에서 불러요)
    public void save() {
        QuestRepository.QuestData data = new QuestRepository.QuestData();
        data.bundles.addAll(questBundles);
        data.quests.addAll(quests);
        data.progresses.addAll(questProgresses);
        DataProvider.quests().save(unitCode(), userEmail(), data, null);
    }

    private QuestProgress findProgress(String questId) {
        for (QuestProgress progress : questProgresses) {
            if (progress.getQuestId().equals(questId)) return progress;
        }
        return null;
    }

    private static String unitCode() {
        User user = UserManager.getCurrentUser();
        return (user == null) ? null : user.getUnitCode();
    }

    private static String userEmail() {
        User user = UserManager.getCurrentUser();
        return (user == null) ? "guest" : user.getEmail();
    }

    // 퀘스트 담당이 만든 예시 데이터 (처음 실행할 때만 들어가요)
    private void addSampleData() {
        questBundles.add(new QuestBundle("testBundleId001", "취사반 적응 미션", "testDescription", QuestRank.FIRST_CLASS));
        questBundles.add(new QuestBundle("testBundleId002", "행정 업무 익히기", "testDescription2", QuestRank.FIRST_CLASS));
        questBundles.add(new QuestBundle("testBundleId003", "전입 처음", "testDescription3", QuestRank.PRIVATE));


        quests.add(new Quest("testId001", questBundles.get(0).getBundleId(), "취사장 마감 점검", "testDescription", 30, QuestCategory.LIFE));
        quests.add(new Quest("testId002", questBundles.get(0).getBundleId(), "취사장 기구 정리", "testDescription", 20, QuestCategory.ADMIN));
        quests.add(new Quest("testId003", questBundles.get(0).getBundleId(), "취사장 뒷정리", "testDescription", 50, QuestCategory.DUTY));
        quests.add(new Quest("testId004", questBundles.get(0).getBundleId(), "취사장 뒷정리2", "testDescription", 50, QuestCategory.OTHER));

        quests.add(new Quest("testId005", questBundles.get(1).getBundleId(), "취사장 마감 점검22", "testDescription22", 22, QuestCategory.LIFE));
        quests.add(new Quest("testId006", questBundles.get(1).getBundleId(), "취사장 기구 정리22", "testDescription22", 20, QuestCategory.ADMIN));

        quests.add(new Quest("testId007", questBundles.get(2).getBundleId(), "관물대 정리", "testDescription3", 30, QuestCategory.LIFE));
        quests.add(new Quest("testId008", questBundles.get(2).getBundleId(), "침대 정리", "testDescription3", 30, QuestCategory.DUTY));
        quests.add(new Quest("testId009", questBundles.get(2).getBundleId(), "책상 정리", "testDescription3", 30, QuestCategory.ADMIN));


        for (Quest quest : quests) {
            questProgresses.add(new QuestProgress(quest.getQuestId()));
        }


    }


    public List<QuestBundle> getQuestBundles() {




        return questBundles;
    }


    public List<Quest> getQuests() {
        return quests;
    }


    public List<QuestProgress> getQuestProgresses() {
        return questProgresses;
    }


    public LiveData<QuestRank> getSelectedRank() {
        return selectedRank;
    }

    public void setSelectedRank(QuestRank selectedRank) {
        if(this.selectedRank.getValue() == selectedRank)
            return;
        this.selectedRank.setValue(selectedRank);
    }

    public long getCompletedQuestXp() {
        long completedQuestXp = 0L;

        for(Quest quest : quests) {
            for(QuestProgress questProgress : questProgresses) {
                if(quest.getQuestId().equals(questProgress.getQuestId())) {
                    if(questProgress.isCompleted())
                        completedQuestXp += quest.getRewardXp();
                    break;
                }
            }
        }
        return completedQuestXp;
    }
}
