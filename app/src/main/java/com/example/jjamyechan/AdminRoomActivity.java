package com.example.jjamyechan;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.jjamyechan.community.CommunityRepository;
import com.example.jjamyechan.community.model.Post;
import com.example.jjamyechan.data.DataCallback;
import com.example.jjamyechan.data.DataProvider;
import com.example.jjamyechan.data.QuestRepository;
import com.example.jjamyechan.quest.Quest;
import com.example.jjamyechan.quest.QuestViewModel;
import com.example.jjamyechan.quest.QuestXp;
import com.example.jjamyechan.databinding.ActivityAdminRoomBinding;
import com.example.jjamyechan.databinding.DialogBroadcastBinding;
import com.example.jjamyechan.databinding.DialogLetterBinding;
import com.example.jjamyechan.databinding.PopupRoomMenuBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

// 실제 행정반 화면입니다. 각 기능들을 사용할 지 여부에 따라 화면 구성이 달라집니다.
// Todo: 사용 기능을 키고 끌 수 있는 버튼을 추가합니다
// Todo: 행정반 전파사항 노티피케이션

public class AdminRoomActivity extends AppCompatActivity {
    private ActivityAdminRoomBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        binding = ActivityAdminRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // 행정반 이름과 기능 켜고 끄기 (저장소에서 읽어 온 뒤에 그려요)
        showUnitInfo();

        // 행정반 전파사항
        binding.cardBroadcast.setOnClickListener(view -> {
            // dialog 생성부
            Dialog dialog = new Dialog(this);
            DialogBroadcastBinding dialogBinding = DialogBroadcastBinding.inflate(getLayoutInflater());
            dialog.setContentView(dialogBinding.getRoot());
            // dialog 배경 투명색으로, 크기 xml파일에 맞추기
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.show();

            // x, 취소 버튼 누르면
            dialogBinding.btnClose.setOnClickListener(view2 -> {
                dialog.dismiss();
            });
            dialogBinding.btnCancel.setOnClickListener(view2 -> {
                dialog.dismiss();
            });

            // 제출 버튼 누르면 (노티피케이션을 구현합시다!)
            dialogBinding.btnSubmit.setOnClickListener(view2 -> {
                String broadcast = dialogBinding.etBroadcast.getText().toString().trim();
                int broadcastType = dialogBinding.rgBroadcastType.getCheckedRadioButtonId();
                if (broadcast.isEmpty()) {
                    dialogBinding.etBroadcast.setError("전파 사항을 입력해주세요");
                    return;
                }
                // 식사의 경우
                if (broadcastType == R.id.rb_type_meal) {
                    Toast.makeText(this, "대원들에게 식사 알림을 전파하였습니다", Toast.LENGTH_LONG).show();
                }
                // 호출의 경우
                else if (broadcastType == R.id.rb_type_call) {
                    Toast.makeText(this, "대원들에게 호출 알림을 전파하였습니다", Toast.LENGTH_LONG).show();
                }
                // 긴급의 경우
                else {
                    Toast.makeText(this, "대원들에게 긴급 알림을 전파하였습니다", Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
            });
        });

        // 진급계산기
        binding.cardPromotion.setOnClickListener(view -> {
            Intent intent = new Intent(this, PromotionActivity.class);
            startActivity(intent);
        });

        // 마음의 편지
        binding.cardLetter.setOnClickListener(view -> {
            showLettetDialog();
        });

        // 팝업 햄버거버튼
        binding.btnRoomMenu.setOnClickListener(view -> {
            // 팝업 생성부
            PopupRoomMenuBinding menuBinding = PopupRoomMenuBinding.inflate(getLayoutInflater());
            // 화면이 먹어서 dimen에 있는 width를 불러와서 화면에 적용
            int width = getResources().getDimensionPixelSize(R.dimen.popup_menu_width);
            PopupWindow popup = new PopupWindow(menuBinding.getRoot(), width, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            // 그림자
            popup.setElevation(16);
            popup.showAsDropDown(binding.btnRoomMenu);

            // TODO: 남은 메뉴 연결 (편집, 설정(로그)) + feature 값에 따라 메뉴 항목 숨기기
            menuBinding.menuItemLetter.setOnClickListener(view2 -> {
                popup.dismiss();
                showLettetDialog();
            });
            // [병합 시 추가] 팀원 화면 연결
            menuBinding.menuItemQuest.setOnClickListener(view2 -> {
                popup.dismiss();
                FeatureNavigator.openQuest(this);
            });
            menuBinding.menuItemCalendar.setOnClickListener(view2 -> {
                popup.dismiss();
                FeatureNavigator.openCalendar(this);
            });
            menuBinding.menuItemBoard.setOnClickListener(view2 -> {
                popup.dismiss();
                FeatureNavigator.openBoard(this);
            });
            menuBinding.menuItemCalculator.setOnClickListener(view2 -> {
                popup.dismiss();
                startActivity(new Intent(this, PromotionActivity.class));
            });
        });

        // FeatureNavigator.java에서 구현해두었습니다
        // 카드에서 팀원 화면으로 가기
        binding.btnQuestDetail.setOnClickListener(view -> FeatureNavigator.openQuest(this));
        binding.btnCalendarDetail.setOnClickListener(view -> FeatureNavigator.openCalendar(this));
        binding.cardBoard.setOnClickListener(view -> FeatureNavigator.openBoard(this));

        // 바텀 네비게이션
        BottomNavigationView nav = binding.bottomNav.getRoot();
        nav.setSelectedItemId(R.id.nav_admin);
        nav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return true;
        });
    }

    // 화면이 보일 때마다 (처음 + 다른 화면에서 돌아올 때) 최신 값으로 다시 그려요
    @Override
    protected void onResume() {
        super.onResume();
        showSummaryDate();
        showMiniWeek();
        showRoomQuests();
        showBoardInfo();
        showPromotionInfo();
    }

    // ───────────── 행정반 이름 + 기능 켜고 끄기 ─────────────

    // 가입한 행정반을 저장소(나중에는 서버)에서 읽어 와서 그려요.
    // 결과는 onSuccess 로 오니까, 화면 그리기(applyUnit)도 그 안에서 불러요.
    private void showUnitInfo() {
        User user = UserManager.getCurrentUser();

        // 가입 정보가 없으면 (예전 방식) Intent 로 받은 값으로 그리기
        if (user.getUnitCode() == null) {
            applyUnit(getIntent().getStringExtra("unit_name"), getIntent().getBooleanArrayExtra("feature"), 0);
            return;
        }

        DataProvider.units().getUnit(user.getUnitCode(), new DataCallback<AdminRoon>() {
            @Override
            public void onSuccess(AdminRoon unit) {
                // 닉네임이 있으면 닉네임, 없으면 부대 이름
                String name = unit.getNickname();
                if (name == null || name.isEmpty()) {
                    name = unit.getUnitName();
                }
                applyUnit(name, unit.getFeatures(), unit.getMemberCount());
                showUnitCode(unit);
            }

            @Override
            public void onFailure(Exception e) {
                applyUnit(getIntent().getStringExtra("unit_name"), getIntent().getBooleanArrayExtra("feature"), 0);
            }
        });
    }

    // 제목, 인원 수, 카드 보이기/숨기기
    // feature 순서: (캘린더, 퀘스트, 진급계산, 게시판, 전파사항, 마편)
    private void applyUnit(String unitName, boolean[] feature, int memberCount) {
        if (feature == null) {
            feature = new boolean[]{true, true, true, true, true, true};
        }
        binding.tvRoomTitle.setText(getString(R.string.unit_name, unitName));
        binding.tvParticipants.setText(getString(R.string.room_participants_format, memberCount));
        binding.btnUnreadNotice.setVisibility(feature[3] ? View.VISIBLE : View.GONE);
        binding.btnUrgentQuest.setVisibility(feature[1] ? View.VISIBLE : View.GONE);

        // 캘린더 카드 보이게할지
        binding.cardRoomCalendar.setVisibility(feature[0] ? View.VISIBLE : View.GONE);
        // 퀘스트 카드 보이게할지
        binding.cardRoomQuest.setVisibility(feature[1] ? View.VISIBLE : View.GONE);
        // 진급 카드 보이게할지
        binding.cardPromotion.setVisibility(feature[2] ? View.VISIBLE : View.GONE);
        // 게시판 카드 보이게할지
        binding.cardBoard.setVisibility(feature[3] ? View.VISIBLE : View.GONE);
        // 전파사항 카드 보이게할지
        binding.cardBroadcast.setVisibility(feature[4] ? View.VISIBLE : View.GONE);
        // 마음의 편지 카드 보이게할지
        binding.cardLetter.setVisibility(feature[5] ? View.VISIBLE : View.GONE);
    }

    // 머리의 작은 글자: "제3보병사단 · 부대 코드 A7K2QX"
    // 누르면 코드가 복사돼서, 대원들에게 메신저로 바로 보낼 수 있어요
    private void showUnitCode(AdminRoon unit) {
        binding.tvRoomUnit.setText(getString(R.string.room_unit_with_code, unit.getUnitName(), unit.getUnitCode()));
        binding.tvRoomUnit.setOnClickListener(view -> {
            // ClipboardManager = 폰의 "복사하기/붙여넣기" 저장 공간
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("unit code", unit.getUnitCode()));
            Toast.makeText(this, R.string.room_code_copied, Toast.LENGTH_SHORT).show();
        });
    }

    // ───────────── 요약 카드: 오늘 날짜와 시간 ─────────────

    private void showSummaryDate() {
        // "M월 d일 EEEE" → "11월 27일 금요일" (EEEE = 요일 전체 이름)
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN);
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
        binding.tvSummaryDate.setText(LocalDate.now().format(dateFormat));
        binding.tvSummaryTime.setText(LocalTime.now().format(timeFormat));
    }

    // ───────────── 캘린더 카드: 이번 주 월~금 ─────────────

    private void showMiniWeek() {
        LinearLayout[] cells = {binding.miniMon, binding.miniTue, binding.miniWed, binding.miniThu, binding.miniFri};
        TextView[] labels = {binding.tvMiniMonLabel, binding.tvMiniTueLabel, binding.tvMiniWedLabel, binding.tvMiniThuLabel, binding.tvMiniFriLabel};
        TextView[] dates = {binding.tvMiniMonDate, binding.tvMiniTueDate, binding.tvMiniWedDate, binding.tvMiniThuDate, binding.tvMiniFriDate};

        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);   // 월=1 … 일=7

        for (int i = 0; i < 5; i++) {
            LocalDate day = monday.plusDays(i);
            dates[i].setText(String.valueOf(day.getDayOfMonth()));

            if (day.equals(today)) {
                cells[i].setBackgroundResource(R.drawable.bg_mini_week_active);
                labels[i].setTextColor(ContextCompat.getColor(this, R.color.mini_week_sub));
                dates[i].setTextColor(ContextCompat.getColor(this, R.color.on_dark));
            } else {
                cells[i].setBackground(null);
                labels[i].setTextColor(ContextCompat.getColor(this, R.color.text_muted));
                dates[i].setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            }
        }
        // 주말에는 오늘 칸이 없어서 아무 칸도 진하게 칠해지지 않아요
    }

    // ───────────── 퀘스트: 요약 카드의 "긴박한 퀘스트" + 퀘스트 카드 ─────────────

    private void showRoomQuests() {
        // 홈 화면과 같은 방법: 퀘스트 화면의 QuestViewModel 을 그대로 써요
        QuestViewModel questModel = new QuestViewModel();
        QuestRepository.QuestData data = questModel.getData();

        // 아직 완료하지 않은 첫 번째 퀘스트 찾기
        Quest next = null;
        for (Quest quest : data.quests) {
            if (!QuestXp.isCompleted(data, quest)) {
                next = quest;
                break;
            }
        }

        // 퀘스트에는 마감일이 아직 없어서 D-day 배지는 숨겨요
        binding.tvUrgentDday.setVisibility(View.GONE);

        if (next == null) {
            binding.tvUrgentQuest.setText(R.string.room_quest_empty);
            binding.tvRoomQuestName.setText(R.string.room_quest_empty);
            binding.tvRoomQuestDesc.setText("");
            binding.tvRoomQuestReward.setText("");
            return;
        }

        binding.tvUrgentQuest.setText(next.getTitle());
        binding.tvRoomQuestName.setText(next.getTitle());
        binding.tvRoomQuestDesc.setText(next.getDescription());
        binding.tvRoomQuestReward.setText(getString(R.string.quest_xp_reward_format, next.getRewardXp()));
    }

    // ───────────── 게시판: 공지 수 + 글 수 ─────────────

    private void showBoardInfo() {
        DataProvider.community().fetchServerPosts(new CommunityRepository.OnPostLoadListener() {
            @Override
            public void onSuccess(List<Post> postList) {
                // 공지 글만 세기
                int noticeCount = 0;
                for (Post post : postList) {
                    if ("공지".equals(post.getCategory())) {
                        noticeCount++;
                    }
                }
                // 누가 무엇을 읽었는지는 아직 저장하지 않아서, "미확인"이 아니라 공지 전체 수예요
                binding.tvUnreadCount.setText(getString(R.string.room_unread_count_format, noticeCount));
                binding.tvBoardSub.setText(getString(R.string.room_board_sub_format, postList.size()));
            }

            @Override
            public void onFailure(Exception e) {
                binding.tvUnreadCount.setText(getString(R.string.room_unread_count_format, 0));
                binding.tvBoardSub.setText(getString(R.string.room_board_sub_format, 0));
            }
        });
    }

    // ───────────── 진급계산기 카드: 다음 진급까지 D-day ─────────────

    private void showPromotionInfo() {
        User user = UserManager.getCurrentUser();

        // 간부는 계급 진급이 없어서 안내 문구만
        if ("officer".equals(user.getMemberType())) {
            binding.tvPromotionSub.setText(R.string.room_promotion_sub_officer);
            return;
        }

        LocalDate today = LocalDate.now();
        String next = User.nextRank(user.getRank());
        // 진급일이 이미 저장된 계급은 건너뛰기 (예: 계급은 이병인데 일병 진급일이 저장돼 있으면 → 상병)
        while (next != null && user.isPromotedTo(next)) {
            next = User.nextRank(next);
        }

        // 병장이면 다음 진급이 없으니 전역까지
        if (next == null) {
            long daysLeft = ChronoUnit.DAYS.between(today, LocalDate.parse(user.getDischargeDate()));
            binding.tvPromotionSub.setText(getString(R.string.room_promotion_sub_discharge, daysLeft));
            return;
        }

        long days = ChronoUnit.DAYS.between(today, user.getPromotionDate(next));
        if (days < 0) {
            days = 0;   // 예정일이 지났는데 진급일이 저장 안 된 경우 (진급 누락) → D-0 으로
        }
        binding.tvPromotionSub.setText(getString(R.string.room_promotion_sub_format, User.rankToKorean(next), days));
    }

    // 마음의 편지 창 띄우기 (카드, 메뉴 팝업 두 곳에서 사용)
    private void showLettetDialog() {
        // dialog 생성부
        Dialog dialog = new Dialog(this);
        DialogLetterBinding dialogBinding = DialogLetterBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        // 몇 시간 후 예약전송할지 고르는거
        RadioButton[] delays = {dialogBinding.rbDelayNow, dialogBinding.rbDelay1h, dialogBinding.rbDelay3h,
                dialogBinding.rbDelay6h, dialogBinding.rbDelay1d, dialogBinding.rbDelayCustom};
        // for문 돌면서
        for (RadioButton delay : delays) {
            delay.setOnClickListener(view2 -> {
                for (RadioButton allDelay : delays) {
                    allDelay.setChecked(false);
                }
                delay.setChecked(true);

                // 사용자 지정시간 뷰 보여줌
                if (delay.equals(dialogBinding.rbDelayCustom)) {
                    dialogBinding.tvCustomTime.setVisibility(ViewGroup.VISIBLE);
                } else {
                    dialogBinding.tvCustomTime.setVisibility(ViewGroup.GONE);
                }

                // OO시간 후 예약전송 메세지 세팅
                if (delay.equals(dialogBinding.rbDelayNow)) {
                    dialogBinding.btnSubmit.setText(getString(R.string.letter_submit));
                } else {
                    dialogBinding.btnSubmit.setText(getString(R.string.letter_submit_later, delay.getText()));
                }
            });
        }

        // x버튼 누르면
        dialogBinding.btnClose.setOnClickListener(view2 -> {
            dialog.dismiss();
        });

        // 보내기 버튼 누르면 (서버랑 연동 예정)
        dialogBinding.btnSubmit.setOnClickListener(view2 -> {
            String letter = dialogBinding.etLetter.getText().toString().trim();
            if (letter.isEmpty()) {
                dialogBinding.etLetter.setError("내용을 입력해주세요");
                return;
            }
            Toast.makeText(this, letter, Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });
    }
}
