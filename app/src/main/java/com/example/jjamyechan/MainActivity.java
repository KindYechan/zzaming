package com.example.jjamyechan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.jjamyechan.community.CommunityRepository;
import com.example.jjamyechan.community.model.ScheduleItem;
import com.example.jjamyechan.data.DataProvider;
import com.example.jjamyechan.data.QuestRepository;
import com.example.jjamyechan.databinding.ActivityMainBinding;
import com.example.jjamyechan.quest.Quest;
import com.example.jjamyechan.quest.QuestBundle;
import com.example.jjamyechan.quest.QuestRank;
import com.example.jjamyechan.quest.QuestViewModel;
import com.example.jjamyechan.quest.QuestXp;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 로그인 안 된 상태 -> 로그인 화면
        if (!UserManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        WindowCompat.enableEdgeToEdge(getWindow());
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        User currentUser = UserManager.getCurrentUser();
        // 카드에 값 넣기는 onResume 에서 해요 (onCreate 다음에 항상 onResume 이 불려요)

        // 설정 화면 들어가기
        binding.topBar.btnSettings.setOnClickListener(view -> {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        });

        // [병합 시 추가] 팀원 화면으로 가기
        binding.btnCalendarAll.setOnClickListener(view -> FeatureNavigator.openCalendar(this));
        binding.btnQuestAll.setOnClickListener(view -> FeatureNavigator.openQuest(this));

        // 바텀 네비게이션
        BottomNavigationView nav = binding.bottomNav.getRoot();
        nav.setSelectedItemId(R.id.nav_home);
        nav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_admin) {
                if (currentUser.getUnitCode() == null) {
                    Intent intent = new Intent(this, AdminEntryActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else {
                    Intent intent = new Intent(this, AdminRoomActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
            }
            return true;
        });
    }
    // 화면이 보일 때마다 (처음 열 때 + 다른 화면에서 돌아올 때) 최신 값으로 다시 그려요.
    // 캘린더에서 일정을 추가하거나 퀘스트를 완료하고 돌아오면 바로 반영돼요.
    @Override
    protected void onResume() {
        super.onResume();
        showUserCard();
        showWeekCalendar();
        showQuestCard();
    }

    private void showUserCard() {
        // User 카드 설정
        User currentUser = UserManager.getCurrentUser();
        binding.tvRank.setText(User.rankToKorean(currentUser.getRank()));   // "pfc" → "일병"
        binding.tvName.setText(currentUser.getName());
        binding.tvDaysLeft.setText(Long.toString(currentUser.getLeftDays()));
        binding.tvDischargeDate.setText("전역일 " + currentUser.getDischargeDate());

        binding.tvPromotionPercent.setText(Double.toString(currentUser.getServiceProgress()) + '%');
        binding.progressPromotion.setProgress((int) (currentUser.getServiceProgress() * 10));

        // 월급 / 적금 카드
        binding.tvSalaryValue.setText(String.format(Locale.KOREA, "%,d", currentUser.getCurrentSalary()));
        binding.tvSalarySub.setText(getString(R.string.home_salary_dday, currentUser.getSalaryDay()));
        binding.tvSavingValue.setText(String.format(Locale.KOREA, "%,d", currentUser.getTotalSalary()));
        binding.tvSavingSub.setText(getString(R.string.home_saving_progress, currentUser.getSavingAccountPercent()));
    }

    // ───────────── ④ 이번 주 캘린더 카드 ─────────────

    // 요일 칸 7개에 이번 주 날짜를 쓰고, 오늘 칸만 진하게 칠해요
    private void showWeekCalendar() {

        // 칸 7개를 배열로 묶어 두면 반복문 하나로 처리할 수 있어요 (월 ~ 일 순서)
        LinearLayout[] cells = {binding.weekMon, binding.weekTue, binding.weekWed, binding.weekThu,
                binding.weekFri, binding.weekSat, binding.weekSun};
        TextView[] labels = {binding.tvWeekMonLabel, binding.tvWeekTueLabel, binding.tvWeekWedLabel, binding.tvWeekThuLabel,
                binding.tvWeekFriLabel, binding.tvWeekSatLabel, binding.tvWeekSunLabel};
        TextView[] dates = {binding.tvWeekMonDate, binding.tvWeekTueDate, binding.tvWeekWedDate, binding.tvWeekThuDate,
                binding.tvWeekFriDate, binding.tvWeekSatDate, binding.tvWeekSunDate};
        View[] dots = {binding.dotWeekMonEvent, binding.dotWeekTueEvent, binding.dotWeekWedEvent, binding.dotWeekThuEvent,
                binding.dotWeekFriEvent, binding.dotWeekSatEvent, binding.dotWeekSunEvent};

        LocalDate today = LocalDate.now();
        LocalDate monday = getMonday(today);

        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);   // 월요일 + i일
            dates[i].setText(String.valueOf(day.getDayOfMonth()));

            if (day.equals(today)) {
                // 오늘: 진한 배경 + 흰 글자
                cells[i].setBackgroundResource(R.drawable.bg_week_today);
                labels[i].setTextColor(ContextCompat.getColor(this, R.color.today_sub));
                dates[i].setTextColor(ContextCompat.getColor(this, R.color.on_dark));
                dots[i].setBackgroundResource(R.drawable.bg_dot_event_today);
            } else {
                // 다른 날: 배경 없음 + 기본 글자색
                cells[i].setBackground(null);
                labels[i].setTextColor(ContextCompat.getColor(this, R.color.text_muted));
                dates[i].setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                dots[i].setBackgroundResource(R.drawable.bg_dot_event);
            }
            // 점은 일정을 읽어 온 뒤에 켜요. 일단 숨기기 (INVISIBLE = 자리는 남기고 안 보이게)
            dots[i].setVisibility(View.INVISIBLE);
        }

        // 일정 읽어 오기. 결과는 onSuccess 로 와요 → 화면 그리기는 꼭 그 안에서!
        DataProvider.community().fetchServerSchedules(new CommunityRepository.OnScheduleLoadListener() {
            @Override
            public void onSuccess(List<ScheduleItem> scheduleList) {
                // 이번 주 각 날짜에 일정이 하나라도 있으면 점 켜기
                for (int i = 0; i < 7; i++) {
                    String day = monday.plusDays(i).toString();   // "2026-11-27" 모양
                    if (hasScheduleOn(scheduleList, day)) {
                        dots[i].setVisibility(View.VISIBLE);
                    }
                }
                showTodaySchedule(scheduleList, today.toString());
            }

            @Override
            public void onFailure(Exception e) {
                showTodaySchedule(new ArrayList<>(), today.toString());
            }
        });
    }

    // 그 주의 월요일 구하기. 예) 목요일이면 3일 전
    private LocalDate getMonday(LocalDate day) {
        int daysFromMonday = day.getDayOfWeek().getValue() - 1;   // 월=1 … 일=7 이라서 1을 빼요
        return day.minusDays(daysFromMonday);
    }

    // 목록에 그 날짜("yyyy-MM-dd") 일정이 있는지
    private boolean hasScheduleOn(List<ScheduleItem> list, String date) {
        for (ScheduleItem item : list) {
            if (date.equals(item.getDate())) {
                return true;
            }
        }
        return false;
    }

    // 카드 아래쪽 칸: 오늘 일정 하나 보여 주기 (없으면 안내 문구)
    private void showTodaySchedule(List<ScheduleItem> list, String today) {
        ScheduleItem todayItem = null;
        for (ScheduleItem item : list) {
            if (today.equals(item.getDate())) {
                todayItem = item;
                break;   // 첫 번째 것만 쓰고 반복 끝내기
            }
        }

        if (todayItem == null) {
            binding.tvScheduleTime.setVisibility(View.GONE);
            binding.tvScheduleTitle.setText(R.string.home_schedule_empty_title);
            binding.tvScheduleDesc.setText(R.string.home_schedule_empty_desc);
            return;
        }

        // 시작 시간이 비어 있으면 "종일"
        String time = todayItem.getStartTime();
        if (time == null || time.isEmpty()) {
            time = getString(R.string.home_schedule_all_day);
        }
        binding.tvScheduleTime.setVisibility(View.VISIBLE);
        binding.tvScheduleTime.setText(time);
        binding.tvScheduleTitle.setText(todayItem.getTitle());
        binding.tvScheduleDesc.setText(todayItem.getLocation());
    }

    // ───────────── ⑤ 퀘스트 카드 ─────────────

    // 경험치 줄 + 아직 안 한 퀘스트 2개 보여 주기
    private void showQuestCard() {

        // 퀘스트 화면이 쓰는 QuestViewModel 을 그대로 써요.
        // 만들 때 저장소에서 읽어 오고, 처음이면 예시 퀘스트를 채워 줘요.
        QuestViewModel questModel = new QuestViewModel();
        QuestRepository.QuestData data = questModel.getData();
        User user = UserManager.getCurrentUser();

        // 1. 경험치 줄 (layout_xp_preview.xml)
        long totalXp = QuestXp.getTotalXp(QuestXp.getCompletedXp(data));
        binding.xpPerview.tvLevel.setText(String.valueOf(QuestXp.getLevel(totalXp)));
        binding.xpPerview.tvXpName.setText(getString(R.string.xp_name_format, user.getName(), User.rankToKorean(user.getRank())));
        binding.xpPerview.tvXpValue.setText(getString(R.string.xp_value_format, QuestXp.getXpInLevel(totalXp), QuestXp.XP_PER_LEVEL));
        binding.xpPerview.progressXp.setMax((int) QuestXp.XP_PER_LEVEL);
        binding.xpPerview.progressXp.setProgress((int) QuestXp.getXpInLevel(totalXp));

        // 2. 아직 안 한 퀘스트 고르기 (내 계급 퀘스트만. 간부는 전부)
        QuestRank myRank = QuestXp.toQuestRank(user.getRank());
        List<Quest> todo = new ArrayList<>();
        for (Quest quest : data.quests) {
            QuestBundle bundle = QuestXp.findBundle(data, quest);
            boolean rightRank = (myRank == null) || (bundle != null && bundle.getRank() == myRank);
            if (rightRank && !QuestXp.isCompleted(data, quest)) {
                todo.add(quest);
            }
        }

        // 3. 줄 두 개에 채우기
        LinearLayout[] rows = {binding.rowQuest1, binding.rowQuest2};
        View[] checks = {binding.checkQuest1, binding.checkQuest2};
        TextView[] titles = {binding.tvQuest1Title, binding.tvQuest2Title};
        TextView[] dues = {binding.tvQuest1Due, binding.tvQuest2Due};
        TextView[] ddays = {binding.tvQuest1Dday, binding.tvQuest2Dday};
        TextView[] xps = {binding.tvQuest1Xp, binding.tvQuest2Xp};

        for (int i = 0; i < 2; i++) {
            // 퀘스트에는 마감일이 아직 없어서 D-day 배지는 숨겨요
            ddays[i].setVisibility(View.GONE);

            if (i >= todo.size()) {
                rows[i].setVisibility(View.GONE);   // 보여 줄 퀘스트가 모자라면 줄 숨기기
                continue;                           // 아래는 건너뛰고 다음 줄로
            }

            Quest quest = todo.get(i);
            QuestBundle bundle = QuestXp.findBundle(data, quest);
            rows[i].setVisibility(View.VISIBLE);
            checks[i].setVisibility(View.VISIBLE);
            dues[i].setVisibility(View.VISIBLE);
            xps[i].setVisibility(View.VISIBLE);
            titles[i].setText(quest.getTitle());
            dues[i].setText(bundle != null ? bundle.getName() : "");   // 마감일 자리에 묶음 이름
            xps[i].setText(getString(R.string.quest_xp_reward_format, quest.getRewardXp()));

            // 동그라미를 누르면 완료 → 저장 → 카드 다시 그리기
            checks[i].setOnClickListener(view -> {
                QuestXp.setCompleted(data, quest, true);
                questModel.save();
                Toast.makeText(this, getString(R.string.home_quest_done, quest.getRewardXp()), Toast.LENGTH_SHORT).show();
                showQuestCard();
            });
            rows[i].setOnClickListener(view -> FeatureNavigator.openQuest(this));
        }

        // 할 퀘스트가 하나도 없으면 첫 줄에 안내 문구만
        if (todo.isEmpty()) {
            binding.rowQuest1.setVisibility(View.VISIBLE);
            binding.checkQuest1.setVisibility(View.GONE);
            binding.tvQuest1Due.setVisibility(View.GONE);
            binding.tvQuest1Xp.setVisibility(View.GONE);
            binding.tvQuest1Title.setText(R.string.home_quest_empty);
            binding.rowQuest1.setOnClickListener(view -> FeatureNavigator.openQuest(this));
        }
    }
}
