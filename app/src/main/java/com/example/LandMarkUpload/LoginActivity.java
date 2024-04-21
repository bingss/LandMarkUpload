package com.example.LandMarkUpload;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.LandMarkUpload.bean.UserInfo;
import com.example.LandMarkUpload.utils.EncryptedSharedHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsrName,edtPassword;
    private Button btnLogin;
    private CheckBox chkLogin;
    private ProgressBar progressBar;
    private EncryptedSharedHelper encryptedShared;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        edtUsrName = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        CheckBox chkLogin = findViewById(R.id.chkLogin);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        try {
            encryptedShared = new EncryptedSharedHelper(this,"Encrypted_Data");
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
        //取得前次有無記住帳密
        SharedPreferences sharedPreferences =getSharedPreferences("Data",MODE_PRIVATE);
        if( sharedPreferences.getBoolean("remember",false) ){
            edtUsrName.setText(encryptedShared.getString("Account"));
            edtPassword.setText(encryptedShared.getString("Password"));
            chkLogin.setChecked(true);
        }


        btnLogin.setOnClickListener(v->{
            HttpLogin(edtUsrName.getText().toString(), edtPassword.getText().toString());
            progressBar.setVisibility(v.VISIBLE);
            btnLogin.setEnabled(false);
        });
    }

    //取得案件資料:get請求
    public void HttpLogin(String username, String password) {
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody= new FormBody.Builder()
                .build();
        Request request = new Request.Builder()
                .url("https://ltgis.info/SurveyDB/api/users?帳號="+username+"&密碼="+password)
                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                String data = e.toString();
                Message msg = new Message();
                msg.what = 404;
                msg.obj = data;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                String data = response.body().string();
                Message msg = new Message();
                msg.what = 100;
                msg.obj = data;
                mHandler.sendMessage(msg);
            }
        });
    }

    //response資料結果處理
    private final Handler mHandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what == 100 && !msg.obj.equals("{\"Message\":\"發生錯誤。\"}") ){

                String data = ((String) msg.obj);
                List<UserInfo> userList = new Gson().fromJson(data, new TypeToken<List<UserInfo>>(){}.getType());


                UserInfo userNow = userList.stream().filter(x ->
                                x.getAccount().equals(edtUsrName.getText().toString()) && x.getPassword().equals(edtPassword.getText().toString()))
                        .findFirst()
                        .orElse(null);

                //取得登入資料
                if(userNow!=null){
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    sdf.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
                    String nowTime = sdf.format(new Date());

                    //不知道call這個有何作用
                    OkHttpClient okHttpClient = new OkHttpClient();
                    RequestBody requestBody= new FormBody.Builder()
                            .add("ObjID", "0")
                            .add("用戶帳號", userNow.getAccount())
                            .add("登入時間", nowTime)
                            .add("登入設備", "行動裝置")
                            .build();

                    Request request = new Request.Builder()
                            .url("https://ltgis.info/SurveyDB/api/userloginlogs")
                            .post(requestBody)
                            .build();
                    Call call = okHttpClient.newCall(request);
                    call.enqueue(new okhttp3.Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.e("Http登入","登入後錯誤"+e);
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            assert response.body() != null;
                            Log.d("Http登入","登入後成功:"+response.body().string());
                        }
                    });

                    //更新登入時間
                    requestBody= new FormBody.Builder()
                            .add("ObjID", userNow.getObjID().toString())
                            .add("帳號", userNow.getAccount())
                            .add("密碼", userNow.getPassword())
                            .add("姓名", userNow.getName())
                            .add("機關", userNow.getOffice())
                            .add("權限", userNow.getAuthorization())
                            .add("信箱", userNow.getEmail())
                            .add("最後登入時間", nowTime)
                            .build();
                    request = new Request.Builder()
                            .url("https://ltgis.info/SurveyDB/api/users/" + userNow.getObjID().toString())
                            .put(requestBody)
                            .build();
                    call = okHttpClient.newCall(request);
                    call.enqueue(new okhttp3.Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.e("Http登入","更新登入時間錯誤"+e);
                        }
                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            assert response.body() != null;
                            Log.d("Http登入","更新登入時間成功");
                        }
                    });

                    chkLogin = findViewById(R.id.chkLogin);
                    //儲存其他登入狀態
                    SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    //勾選記住則存帳密
                    if(chkLogin.isChecked()){
                        encryptedShared.putString("Account",userNow.getAccount());
                        encryptedShared.putString("Password",userNow.getPassword());
                        editor.putBoolean("remember",true);
                    }
                    else{
                        encryptedShared.remove("Account");
                        encryptedShared.remove("Password");
                        editor.putBoolean("remember",false);
                    }
                    editor.putString("LoginOffice",userNow.getOffice().substring(0,2));
                    editor.apply();
                    encryptedShared.apply();

                    btnLogin.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    Toast toast = Toast.makeText( LoginActivity.this, userNow.getName()+"，登入成功!", Toast.LENGTH_SHORT);
                    toast.show();

                }
            }
            else
            {
                btnLogin.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                Toast toast = Toast.makeText( LoginActivity.this, "帳號密碼錯誤", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    };
}