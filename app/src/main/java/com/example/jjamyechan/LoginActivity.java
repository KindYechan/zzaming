package com.example.jjamyechan;

import com.example.jjamyechan.data.DataCallback;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jjamyechan.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 회원가입 하러가기 버튼 누르면
        binding.btnGoSignup.setOnClickListener(view -> {
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
        });

        // 아이디랑 비밀번호 입력하고 로그인하기
        binding.btnLogin.setOnClickListener(view -> {
            String username = binding.etUsername.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (username.isEmpty()) {
                binding.etUsername.setError("ID를 입력해주세요");
                return;
            }
            if (password.isEmpty()) {
                binding.etPassword.setError("비밀번호를 입력해주세요");
                return;
            }
            // 결과는 콜백으로 와요 (서버를 붙여도 이 모양 그대로)
            UserManager.login(username, password, new DataCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    binding.etPassword.setError(getString(R.string.auth_error_login));
                }
            });
        });
    }
}
