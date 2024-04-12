package com.example.LandMarkUpload;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.LandMarkUpload.bean.CaseInfo;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private Button btnSearch;
    private EditText edtYear,edtNum;
    private ProgressBar progressBar;
    private Spinner spnOffice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { //sdk 33以上
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_MEDIA_IMAGES},100);
            }
        }
        else{ //sdk 32以下
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},100);
            }
        }

//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
//            if (!Environment.isExternalStorageManager()){
//                Toast toast = Toast.makeText( MainActivity.this, "請開啟存取檔案權限", Toast.LENGTH_SHORT);
//                toast.show();
//                startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
//            }
//        }

        btnSearch = findViewById(R.id.btnSearch);
        edtYear = findViewById(R.id.edtYear);
        edtNum = findViewById(R.id.edtNum);
        progressBar = findViewById(R.id.progressBar);

        //設定初始年度
        edtYear.setText( String.valueOf(Calendar.getInstance().get(Calendar.YEAR)-1911) );

        //地所選單
        spnOffice = findViewById(R.id.spnOffice);
        SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
        String curOffice = sharedPreferences.getString("Office","10");
        ArrayAdapter<CharSequence> adapter;
        adapter = ArrayAdapter.createFromResource(this,
                R.array.office,
                android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnOffice.setAdapter(adapter);
        spnOffice.setSelection( Integer.parseInt(curOffice) );

        btnSearch.setOnClickListener( v -> {
            progressBar.setVisibility(v.VISIBLE);
            btnSearch.setEnabled(false);

            //存現在選擇地所Position
            SharedPreferences sharedPreferences1 = getSharedPreferences("Data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences1.edit();
            editor.putString("Office", String.valueOf(spnOffice.getSelectedItemPosition()));
            editor.apply();

            String OfficeCode;
            switch(spnOffice.getSelectedItemPosition()) {
                case 0: //中山
                    OfficeCode = "BA";
                    break;
                case 1: //中正
                    OfficeCode = "BB";
                    break;
                case 2: //中興
                    OfficeCode = "BC";
                    break;
                case 3: //豐原
                    OfficeCode = "BD";
                    break;
                case 4: //大甲
                    OfficeCode = "BE";
                    break;
                case 5: //清水
                    OfficeCode = "BF";
                    break;
                case 6: //東勢
                    OfficeCode = "BG";
                    break;
                case 7: //雅潭
                    OfficeCode = "BH";
                    break;
                case 8: //大里
                    OfficeCode = "BI";
                    break;
                case 9: //太平
                    OfficeCode = "BJ";
                    break;
                default: //龍井
                    OfficeCode = "BK";
            }

            String Year = edtYear.getText().toString();
            StringBuilder Num = new StringBuilder(edtNum.getText().toString());
            while(Num.length() < 6) Num.insert(0, "0");
            getHttpData(Year, Num.toString() ,OfficeCode);
        });
    }

    //permissin處理
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        Log.d("PhotoPicker", "取得權限判斷:" + grantResults);
        if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            btnSearch.setEnabled(true);
        }
        else {
            Toast toast = Toast.makeText( MainActivity.this, "請開啟圖片存取權限", Toast.LENGTH_SHORT);
            toast.show();
            btnSearch.setEnabled(false);
        }
    }

    //取得案件資料:get請求
    public void getHttpData(String Year,String Num,String OfficeCode) {

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://lohas.taichung.gov.tw/LANDWA/api/CaseOverdues/4849?BCD="+OfficeCode+"&M123="+Year+OfficeCode+"56"+Num)
                .get()
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
            if(msg.what == 100 && !msg.obj.equals("[]")){
                //response(json)轉為java bean
                String data = ((String) msg.obj);
                CaseInfo caseTest = new Gson().fromJson(data.substring(1,data.length()-1),CaseInfo.class);

                Intent intent = new Intent(MainActivity.this, UploadActivity.class);
                intent.putExtra("caseName",caseTest.getMM0123());
                intent.putExtra("caseReason",caseTest.getMM06());
                intent.putExtra("caseLocation",caseTest.getMM08());
                intent.putExtra("caseLand",caseTest.getMM09());
                intent.putExtra("caseSurvey",caseTest.getMD04());

                //關閉進度條、跳轉頁面
                progressBar.setVisibility(View.GONE);
                btnSearch.setEnabled(true);
                startActivity(intent);
            }
            else
            {
                progressBar.setVisibility(View.GONE);
                btnSearch.setEnabled(true);
                Toast toast = Toast.makeText( MainActivity.this, "取得案件錯誤", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    };

}