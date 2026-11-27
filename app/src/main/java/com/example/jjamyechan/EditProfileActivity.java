package com.example.jjamyechan;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.jjamyechan.databinding.ActivityEditProfileBinding;
import com.example.jjamyechan.databinding.ActivityPromotionBinding;

import java.time.LocalDate;

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;
    private User user;
    private LocalDate enlistDate, dischargeDate;
    private static final String[] RANK_CODES = {"private", "private-first-class", "corporal", "sergeant"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        User user = UserManager.getCurrentUser();
        binding.tvUsername.setText(user.getEmail());
        binding.etName.setText(user.getName());

        if (user.getMemberType().equals("officer")) {
            binding.layoutRank.setVisibility(View.GONE);    // 간부는 계급 칸 없음
        } else {
            binding.spRank.setSelection(rankToPosition(user.getRank()));
        }
        enlistDate = LocalDate.parse(user.getEnlistmentDate());
        dischargeDate = LocalDate.parse(user.getDischargeDate());
        updateDate();
        binding.btnBack.setOnClickListener(view -> {
            finish();   // 저장 안 하고 나가기
        });

        // 2) 날짜 고르기 (회원가입과 달리 입대일을 바꿔도 전역일은 그대로)
        binding.tvEnlistDate.setOnClickListener(view -> {
            new DatePickerDialog(this, (picker, year, month, day) -> {
                enlistDate = LocalDate.of(year, month + 1, day);
                updateDate();
            }, enlistDate.getYear(), enlistDate.getMonthValue() - 1, enlistDate.getDayOfMonth()).show();
        });
        binding.tvDischargeDate.setOnClickListener(view -> {
            new DatePickerDialog(this, (picker, year, month, day) -> {
                dischargeDate = LocalDate.of(year, month + 1, day);
                updateDate();
            }, dischargeDate.getYear(), dischargeDate.getMonthValue() - 1, dischargeDate.getDayOfMonth()).show();
        });

        // 3) 저장
        binding.btnSave.setOnClickListener(view -> {
            String name = binding.etName.getText().toString().trim();

            if (name.isEmpty()) {
                showError(R.string.edit_error_name);
                return;
            }
            if (dischargeDate.isBefore(enlistDate)) {
                showError(R.string.edit_error_dates);
                return;
            }

            // currentUser 객체를 직접 바꿈 (서버가 붙으면 여기서 Firestore 에도 저장)
            user.setName(name);
            if (!user.getMemberType().equals("officer")) {
                user.setRank(RANK_CODES[binding.spRank.getSelectedItemPosition()]);
            }
            user.setEnlistmentDate(enlistDate.toString());
            user.setDischargeDate(dischargeDate.toString());
            UserManager.saveCurrentUser();   // 바뀐 정보를 저장소(나중에는 서버)에 저장

            Toast.makeText(this, R.string.edit_saved, Toast.LENGTH_SHORT).show();
            finish();
        });

    }
    // 계급 코드 → Spinner 위치 (못 찾으면 0 = 이병)
    private int rankToPosition(String rank) {
        for (int i = 0; i < RANK_CODES.length; i++) {
            if (RANK_CODES[i].equals(rank)) return i;
        }
        return 0;
    }

    // 날짜를 바꿨을 때 칸의 글자도 바꾸기
    private void updateDate() {
        binding.tvEnlistDate.setText(enlistDate.toString());
        binding.tvDischargeDate.setText(dischargeDate.toString());
    }

    // 오류 상자에 문구 보이기
    private void showError(int messageId) {
        binding.tvEditError.setText(messageId);
        binding.tvEditError.setVisibility(View.VISIBLE);
    }
}