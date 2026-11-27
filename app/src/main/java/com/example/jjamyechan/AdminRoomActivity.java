package com.example.jjamyechan;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.jjamyechan.data.DataCallback;
import com.example.jjamyechan.data.DataProvider;
import com.example.jjamyechan.databinding.ActivityAdminRoomBinding;
import com.example.jjamyechan.databinding.DialogBroadcastBinding;
import com.example.jjamyechan.databinding.DialogLetterBinding;
import com.example.jjamyechan.databinding.PopupRoomMenuBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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

        // intent로 전달된 데이터 저장부
        String unitName = getIntent().getStringExtra("unit_name");
        boolean[] feature = getIntent().getBooleanArrayExtra("feature");

        // [병합 시 추가] 가입한 행정반이 있으면 저장소(나중에는 서버)에서 이름과 기능 설정을 읽어 와요
        AdminRoon unit = loadMyUnit();
        if (unit != null) {
            unitName = (unit.getNickname() != null && !unit.getNickname().isEmpty()) ? unit.getNickname() : unit.getUnitName();
            feature = unit.getFeatures();
        }
        if (feature == null) {
            feature = new boolean[]{true, true, true, true, true, true};
        }
        binding.tvRoomTitle.setText(getString(R.string.unit_name, unitName));
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

        // [병합 시 추가] 카드에서 팀원 화면으로 가기
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

    // [병합 시 추가] 내가 가입한 행정반 정보 읽기 (가입 안 했거나 못 찾으면 null)
    private AdminRoon loadMyUnit() {
        User user = UserManager.getCurrentUser();
        if (user == null || user.getUnitCode() == null) return null;
        final AdminRoon[] found = {null};   // 람다 안에서 값을 넣으려고 배열로 (effectively final 규칙)
        DataProvider.units().getUnit(user.getUnitCode(), new DataCallback<AdminRoon>() {
            @Override
            public void onSuccess(AdminRoon result) {
                found[0] = result;
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
        // 주의: 지금 로컬 저장소는 결과를 바로 줘서 이렇게 써도 돼요.
        // 서버를 붙이면 결과가 늦게 오니까, onSuccess 안에서 화면을 그리도록 옮겨야 해요.
        return found[0];
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
