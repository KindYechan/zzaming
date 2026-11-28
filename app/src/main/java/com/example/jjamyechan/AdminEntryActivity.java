package com.example.jjamyechan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.jjamyechan.data.DataCallback;
import com.example.jjamyechan.data.DataProvider;
import com.example.jjamyechan.databinding.ActivityAdminEntryBinding;
import com.example.jjamyechan.databinding.SheetCreateStep1Binding;
import com.example.jjamyechan.databinding.SheetCreateStep2Binding;
import com.example.jjamyechan.databinding.SheetJoinBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

// 행정반 가입 및 생성에 사용되는 Activity
// 초기 버전은 가입에 이름, 별명 등이 사용되었지만 이를 부대코드로 변경했습니다.
//

public class AdminEntryActivity extends AppCompatActivity {
    private ActivityAdminEntryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        binding = ActivityAdminEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // 설정 화면 들어가기
        binding.topBar.btnSettings.setOnClickListener(view -> {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        });

        // 이미 만들어진 행정반 들어가기
        binding.btnJoin.setOnClickListener(view -> {
            // 바텀시트 생성부
            BottomSheetDialog sheet = new BottomSheetDialog(this);
            SheetJoinBinding sheetBinding = SheetJoinBinding.inflate(getLayoutInflater());
            sheet.setContentView(sheetBinding.getRoot());
            sheet.show();

            // x 버튼 누르면
            sheetBinding.btnClose.setOnClickListener(view2 -> {
                sheet.dismiss();
            });

            // 제출 버튼 누르면
            // [변경] 이름·계급은 회원가입 정보를 쓰니까 부대 코드만 받아요
            sheetBinding.btnSubmit.setOnClickListener(view2 -> {
                // 앞뒤 공백 지우고, 소문자로 적어도 되게 대문자로 바꾸기
                String code = sheetBinding.etUnitCode.getText().toString().trim().toUpperCase();

                // 6자리가 아니면 에러
                if (code.length() != 6) {
                    sheetBinding.etUnitCode.setError(getString(R.string.join_error_code));
                    return;
                }

                // 코드로 행정반 찾기 (지금은 기기 저장소, 나중에는 서버)
                DataProvider.units().getUnit(code, new DataCallback<AdminRoon>() {
                    @Override
                    public void onSuccess(AdminRoon unit) {
                        // 찾았으면 "○○ 행정반에 가입할까요?" 한 번 물어보기
                        confirmJoin(unit, sheet);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        sheetBinding.etUnitCode.setError(getString(R.string.join_error_not_found));
                    }
                });
            });
        });

        // 새로운 행정반 생성하기
        binding.btnCreate.setOnClickListener(view -> {
            // 바텀시트 생성부
            BottomSheetDialog sheet = new BottomSheetDialog(this);
            SheetCreateStep1Binding sheetBinding1 = SheetCreateStep1Binding.inflate(getLayoutInflater());
            sheet.setContentView(sheetBinding1.getRoot());
            sheet.show();
            // default 설정값
            sheetBinding1.spUnitType.setSelection(1);

            // x 버튼 누르면
            sheetBinding1.btnClose.setOnClickListener(view2 -> {
                sheet.dismiss();
            });

            // 다음 버튼 누르면
            sheetBinding1.btnNext.setOnClickListener(view2 -> {
                // edit text 데이터 가져오기
                String unitName = sheetBinding1.etUnitName.getText().toString().trim();
                String unitNickname = sheetBinding1.etUnitNickname.getText().toString().trim();
                String unitType = sheetBinding1.spUnitType.getSelectedItem().toString();
                String unitDesc = sheetBinding1.etUnitDesc.getText().toString().trim();

                // 데이터 입력 안했으면 이렇게
                if (unitName.isEmpty()) {
                    sheetBinding1.etUnitName.setError("부대명을 입력해주세요");
                    return;
                }

                // 두 번째 바텀시트 생성부
                sheet.dismiss();
                BottomSheetDialog sheet2 = new BottomSheetDialog(this);
                SheetCreateStep2Binding sheetBinding2 = SheetCreateStep2Binding.inflate(getLayoutInflater());
                sheet2.setContentView(sheetBinding2.getRoot());
                sheet2.show();

                // x 버튼 누르면
                sheetBinding2.btnClose.setOnClickListener(view3 -> {
                    sheet2.dismiss();
                });

                // 이전 버튼 누르면
                sheetBinding2.btnPrev.setOnClickListener(view3 -> {
                    sheet2.dismiss();
                    sheet.show();
                });

                // 제출 버튼 누르면
                sheetBinding2.btnSubmit.setOnClickListener(view3 -> {
                    // 입력 데이터 가져오기
                    boolean[] features = new boolean[6];
                    features[0] = sheetBinding2.cbFeatureCalendar.isChecked();
                    features[1] = sheetBinding2.cbFeatureQuest.isChecked();
                    features[2] = sheetBinding2.cbFeaturePromotion.isChecked();
                    features[3] = sheetBinding2.cbFeatureNotice.isChecked();
                    features[4] = sheetBinding2.cbFeatureAlert.isChecked();
                    features[5] = sheetBinding2.cbFeatureLetter.isChecked();
                    String editPermission = sheetBinding2.spEditPermission.getSelectedItem().toString();
                    boolean activityLog = sheetBinding2.swActivityLog.isChecked();

                    // [병합 시 추가] 행정반을 저장소(나중에는 서버)에 만들고, 만든 사람을 관리자로 가입시켜요
                    User me = UserManager.getCurrentUser();
                    AdminRoon unit = new AdminRoon(unitName, unitNickname, unitType, unitDesc, editPermission, me.getEmail(),
                            features[0], features[1], features[2], features[3], features[4], features[5]);
                    unit.setActivityLog(activityLog);   // [추가] 예전에는 저장하지 않던 값
                    DataProvider.units().createUnit(unit, new DataCallback<AdminRoon>() {
                        @Override
                        public void onSuccess(AdminRoon created) {
                            me.setUnitCode(created.getUnitCode());
                            me.setRole("owner");
                            UserManager.saveCurrentUser();

                            // [변경] 행정반 화면은 저장소에서 정보를 읽어서, Intent 로 값을 넘길 필요가 없어요
                            sheet2.dismiss();
                            startActivity(new Intent(AdminEntryActivity.this, AdminRoomActivity.class));
                            finish();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(AdminEntryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        });

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

    // 부대 코드로 찾은 행정반에 가입할지 한 번 물어보고, "가입하기"를 누르면 가입 처리

    // 이미 존재하는 부대에 가입할 때 사용하는 메소드
    // dialog를 통해 가입을 확인받습니다.
    private void confirmJoin(AdminRoon unit, BottomSheetDialog sheet) {
        // 닉네임이 있으면 닉네임, 없으면 부대 이름으로 보여 주기
        String name = unit.getNickname();
        if (name == null || name.isEmpty()) {
            name = unit.getUnitName();
        }

        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.join_confirm_message, getString(R.string.unit_name, name)))
                .setNegativeButton(R.string.action_cancel, null)   // null = 그냥 닫기
                .setPositiveButton(R.string.join_confirm_ok, (dialog, which) -> {
                    // 1. 내 정보에 부대 코드 저장 (이름·계급은 회원가입 정보 그대로)
                    User me = UserManager.getCurrentUser();
                    me.setUnitCode(unit.getUnitCode());
                    me.setRole("member");
                    UserManager.saveCurrentUser();

                    // 2. 행정반 인원 수 +1
                    unit.setMemberCount(unit.getMemberCount() + 1);
                    DataProvider.units().saveUnit(unit, null);

                    // 3. 행정반 화면으로
                    sheet.dismiss();
                    startActivity(new Intent(this, AdminRoomActivity.class));
                    finish();
                })
                .show();
    }
}
