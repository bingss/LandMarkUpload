package com.example.LandMarkUpload;

import static com.example.LandMarkUpload.FileHelper.getRealPathFromURI;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class UploadActivity extends AppCompatActivity {

    private Button btnBack,btnChoose;
    private String uploadApi;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_pic);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        progressBar = findViewById(R.id.progressBar);
        TextView textName = findViewById(R.id.textName);
        TextView textReason = findViewById(R.id.textReason);
        TextView textLocation = findViewById(R.id.textLocation);
        TextView textLand = findViewById(R.id.textLand);
        TextView textSurvey = findViewById(R.id.textSurvey);

        Intent intent = getIntent();
        textName.setText( intent.getStringExtra("caseName") );
        textReason.setText( intent.getStringExtra("caseReason") );
        textLocation.setText( intent.getStringExtra("caseLocation") );
        textLand.setText( intent.getStringExtra("caseLand") );
        textSurvey.setText( intent.getStringExtra("caseSurvey") );

        uploadApi = "https://lohas.taichung.gov.tw/SurveyFile/Upload.aspx?folder=AllFile/"+textLocation.getText()
                +"&keyword="+textName.getText()+"_"+textLocation.getText()+textLand.getText()
                +"&fname="+textName.getText()+"_"+textLocation.getText()+textLand.getText();

        btnChoose = findViewById(R.id.btnChoose);
        btnBack = findViewById(R.id.btnBack);

        btnChoose.setOnClickListener(v -> {
            progressBar.setVisibility(v.VISIBLE);
            btnChoose.setEnabled(false);
            pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });
        btnBack.setOnClickListener(v -> finish());
    }

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(50), uris -> {
                // Callback is invoked after the user selects media items or closes the
                // photo picker.
                if (uris != null && !uris.isEmpty()) {
                    String zipPath = this.getFilesDir().getPath() + "/LP_1.zip";
                    List<File> imgFiles = new ArrayList<>();
                    String nowDate = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());

                    uris.forEach((uri) -> {
                        String imgPath = getRealPathFromURI(this,uri);


                        File compressedFile = new File(this.getFilesDir().getPath() + "/" + nowDate + "_" +uris.indexOf(uri)+".jpg");
                        try {
                            //圖片壓縮
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 2;       // 採樣率
                            // 在將文件以Bitmap形式加載到內存的時候，加入Option參數
                            Bitmap bitmap = BitmapFactory.decodeFile(imgPath, options);
                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream( compressedFile ));
                            bitmap.compress(Bitmap.CompressFormat.JPEG,100,bos);
                            imgFiles.add( compressedFile );
                            bos.close();
                        }catch (Exception e) {
                            Toast toast = Toast.makeText( UploadActivity.this, "圖片壓縮失敗", Toast.LENGTH_SHORT);
                            toast.show();
                            progressBar.setVisibility(View.GONE);
                            btnChoose.setEnabled(true);
                            Log.d("PhotoPicker", "圖片壓縮失敗:" + e);
                        }
                    });

                    try {
                        FileOutputStream fos = new FileOutputStream(zipPath);
                        ZipOutputStream zop = new ZipOutputStream(fos);
                        byte[] bytes = new byte[1024*8];

                        for (File imgFile : imgFiles) {
                            Log.d("PhotoPicker", imgFile.getPath());
                            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(imgFile));
                            zop.putNextEntry(new ZipEntry(imgFile.getName()));
                            int length;
                            while ((length = bufferedInputStream.read(bytes)) > 0) {
                                zop.write(bytes, 0, length);
                            }
                            zop.closeEntry();
                            bufferedInputStream.close();
                        }
                        zop.close();
                        fos.close();
                        for (File imgFile : imgFiles) {
                            boolean deleted = imgFile.delete();
                        }
                        postZipData(zipPath);
                        Toast toast = Toast.makeText( UploadActivity.this, "上傳成功", Toast.LENGTH_SHORT);
                        toast.show();
                        Log.d("PhotoPicker", "上傳完成:"+ zipPath);
                        finish();
                    } catch (Exception e) {
                        Log.d("PhotoPicker", "上傳失敗:" + e);
                    }finally {
                        progressBar.setVisibility(View.GONE);
                        btnChoose.setEnabled(true);
                    }

                }
                else {
                    Log.d("PhotoPicker", "未選取照片");
                }
            });
    private void postZipData(String zipPath) {
        File zipFile = new File(zipPath);
        // 建立OkHttpClient
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        // FormBody放要傳的參數和值
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("File", zipFile.getName(),
                        RequestBody.create(zipFile, MediaType.parse("application/octet-stream")))
                .build();
        // 建立Request，設置連線資訊
        Request request = new Request.Builder()
                .url(uploadApi)
                .post(formBody)
                .build();
        // 建立Call
        Call call = client.newCall(request);
        // 執行Call連線到網址
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                // 連線成功
                boolean deleted = zipFile.delete();
                Log.d("OkHttp", "傳送成功" + deleted);
            }
            @Override
            public void onFailure(Call call, IOException e) {
                // 連線失敗
                Log.d("OkHttp", "傳送失敗"+e);
            }
        });

    }


}