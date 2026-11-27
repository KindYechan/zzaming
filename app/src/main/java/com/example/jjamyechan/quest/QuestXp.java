package com.example.jjamyechan.quest;

import com.example.jjamyechan.data.QuestRepository;

/**
 * 퀘스트 경험치(XP)와 레벨 계산을 한 곳에 모은 클래스.
 *
 * 퀘스트 화면(QuestActivity), 홈의 퀘스트 카드, 행정반 화면이 모두 이 계산을 써요.
 * 그래서 숫자를 바꾸고 싶으면 이 파일만 고치면 세 화면이 같이 바뀌어요.
 *
 * 시작값(START_XP, START_LEVEL)은 퀘스트 담당이 QuestActivity 에 적어 둔 값을 그대로 옮겨 왔어요.
 * 유저마다 XP 를 따로 저장하게 되면 이 값은 0 / 1 로 바꾸면 돼요.
 */
public class QuestXp {
    public static final long START_XP = 320;      // 처음부터 가지고 있는 XP
    public static final long START_LEVEL = 4;     // 처음 레벨
    public static final long XP_PER_LEVEL = 500;  // 레벨 하나 올리는 데 필요한 XP

    // 완료한 퀘스트의 보상 XP 를 전부 더하기
    public static long getCompletedXp(QuestRepository.QuestData data) {
        long sum = 0;
        for (Quest quest : data.quests) {
            if (isCompleted(data, quest)) {
                sum += quest.getRewardXp();
            }
        }
        return sum;
    }

    // 전체 XP = 시작 XP + 완료한 퀘스트 XP
    public static long getTotalXp(long completedXp) {
        return START_XP + completedXp;
    }

    // 레벨. 예) 전체 1,320 XP → 4 + (1320 / 500) = 4 + 2 = 6
    public static long getLevel(long totalXp) {
        return START_LEVEL + totalXp / XP_PER_LEVEL;
    }

    // 지금 레벨 안에서 모은 XP. 예) 1,320 XP → 1320 % 500 = 320
    public static long getXpInLevel(long totalXp) {
        return totalXp % XP_PER_LEVEL;
    }

    // 그 퀘스트를 완료했는지 (완료 기록이 없으면 아직 안 한 것)
    public static boolean isCompleted(QuestRepository.QuestData data, Quest quest) {
        for (QuestProgress progress : data.progresses) {
            if (progress.getQuestId().equals(quest.getQuestId())) {
                return progress.isCompleted();
            }
        }
        return false;
    }

    // 완료 표시 바꾸기 (기록이 없으면 새로 만들어요)
    public static void setCompleted(QuestRepository.QuestData data, Quest quest, boolean completed) {
        for (QuestProgress progress : data.progresses) {
            if (progress.getQuestId().equals(quest.getQuestId())) {
                progress.setCompleted(completed);
                return;
            }
        }
        data.progresses.add(new QuestProgress(quest.getQuestId(), completed));
    }

    // 퀘스트가 들어 있는 묶음 찾기 (못 찾으면 null)
    public static QuestBundle findBundle(QuestRepository.QuestData data, Quest quest) {
        for (QuestBundle bundle : data.bundles) {
            if (bundle.getBundleId().equals(quest.getBundleId())) {
                return bundle;
            }
        }
        return null;
    }

    // 유저의 계급 코드 → 퀘스트 화면에서 쓰는 계급 (간부 등은 null)
    public static QuestRank toQuestRank(String rank) {
        if (rank == null) return null;
        switch (rank) {
            case "private":             return QuestRank.PRIVATE;
            case "pfc":
            case "private-first-class": return QuestRank.FIRST_CLASS;
            case "corporal":            return QuestRank.CORPORAL;
            case "sergeant":            return QuestRank.SERGEANT;
            default:                    return null;
        }
    }
}
