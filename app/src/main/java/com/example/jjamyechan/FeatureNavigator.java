package com.example.jjamyechan;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.example.jjamyechan.community.CalendarActivity;
import com.example.jjamyechan.community.PostActivity;
import com.example.jjamyechan.quest.QuestActivity;

import java.time.LocalDate;

/**
 * [병합 시 추가] 팀원들이 만든 화면(캘린더, 게시판, 퀘스트)으로 가는 길을 한 곳에 모았어요.
 * 홈 화면과 행정반 화면 여러 곳에서 똑같이 쓰기 때문이에요.
 * 팀원 화면이 원래 Intent 로 받던 값(memberType, 복무 기간)을 로그인한 유저 정보로 채워 줘요.
 */
public class FeatureNavigator {

    // 팀원 화면은 회원 유형을 한글("병사"/"간부")로 받아요
    private static String memberTypeKorean() {
        User user = UserManager.getCurrentUser();
        return (user != null && "officer".equals(user.getMemberType())) ? "간부" : "병사";
    }

    // [추가] 행정반에 들어가 있는지
    public static boolean hasUnit() {
        User user = UserManager.getCurrentUser();
        return user != null && user.getUnitCode() != null;
    }

    // [추가] 행정반 입장 화면(가입/생성)으로 가기
    public static void openUnitEntry(Activity from) {
        from.startActivity(new Intent(from, AdminEntryActivity.class));
        from.finish();   // 하단 탭으로 갈 때와 똑같이 지금 화면은 닫아요
    }

    // [추가] 행정반이 없으면 안내 토스트를 띄우고 입장 화면으로 보내요.
    // 보냈으면 true → 부르는 쪽은 그냥 return 하면 돼요
    private static boolean sendToEntryIfNoUnit(Activity from) {
        if (hasUnit()) {
            return false;
        }
        Toast.makeText(from, R.string.no_unit_toast, Toast.LENGTH_SHORT).show();
        openUnitEntry(from);
        return true;
    }

    // 캘린더: 입대 달 ~ 전역 달까지 보여 줘요 (월은 0부터 세요: 1월 = 0)
    public static void openCalendar(Activity from) {
        if (sendToEntryIfNoUnit(from)) return;
        Intent intent = new Intent(from, CalendarActivity.class);
        intent.putExtra("memberType", memberTypeKorean());
        User user = UserManager.getCurrentUser();
        if (user != null && user.getEnlistmentDate() != null && user.getDischargeDate() != null) {
            LocalDate start = LocalDate.parse(user.getEnlistmentDate());
            LocalDate end = LocalDate.parse(user.getDischargeDate());
            intent.putExtra("startYear", start.getYear());
            intent.putExtra("startMonth", start.getMonthValue() - 1);
            intent.putExtra("endYear", end.getYear());
            intent.putExtra("endMonth", end.getMonthValue() - 1);
        }
        from.startActivity(intent);
    }

    // 부대 게시판
    public static void openBoard(Activity from) {
        if (sendToEntryIfNoUnit(from)) return;
        Intent intent = new Intent(from, PostActivity.class);
        intent.putExtra("memberType", memberTypeKorean());
        from.startActivity(intent);
    }

    // 퀘스트
    public static void openQuest(Activity from) {
        if (sendToEntryIfNoUnit(from)) return;
        from.startActivity(new Intent(from, QuestActivity.class));
    }
}
