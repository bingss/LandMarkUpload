package com.example.LandMarkUpload;

import static com.example.LandMarkUpload.utils.FileHelper.getRealPathFromURI;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LandMarkUpload.bean.PointInfo;
import com.example.LandMarkUpload.utils.CountingRequestBody;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class UploadActivity extends AppCompatActivity {

    private Button btnBack,btnUpload,btnAdd;
    private String uploadApi;
    private ProgressBar progressBar;
    private TextView textProgress;
    private RecyclerView listRv;
    private PointAdapter mPointAdapter;
    private TextView textName,textReason,textLocation,textLand,textSurvey;



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
        
        //案件資訊&上傳API
        textName = findViewById(R.id.textName);
        textReason = findViewById(R.id.textReason);
        textLocation = findViewById(R.id.textLocation);
        textLand = findViewById(R.id.textLand);
        textSurvey = findViewById(R.id.textSurvey);
        Intent intent = getIntent();
        textName.setText( intent.getStringExtra("caseName") );
        textReason.setText( intent.getStringExtra("caseReason") );
        textLocation.setText( intent.getStringExtra("caseLocation") );
        textLand.setText( intent.getStringExtra("caseLand") );
        textSurvey.setText( intent.getStringExtra("caseSurvey") );
        uploadApi = "https://lohas.taichung.gov.tw/SurveyFile/Upload.aspx?folder=AllFile/"+textLocation.getText()
                +"&keyword="+textName.getText()+"_"+textLocation.getText()+textLand.getText()
                +"&fname="+textName.getText()+"_"+textLocation.getText()+textLand.getText();
        textProgress = findViewById(R.id.textProgress);
        progressBar = findViewById(R.id.progressBar);
        btnUpload = findViewById(R.id.btnUpload);
        btnBack = findViewById(R.id.btnBack);
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            selectPhotos.launch("image/*");
        });
        btnUpload.setOnClickListener(v -> {
            if( mPointAdapter.getItemCount() > 0 ){
                //非同步壓縮產製pdf並上傳
                ImageTask task=new ImageTask();
                task.execute( this.getFilesDir().getPath() );

                btnUpload.setVisibility(View.GONE);
                btnBack.setVisibility(View.GONE);
                textProgress.setVisibility(View.VISIBLE);
                textProgress.setText("檔案處理中...");
                progressBar.setVisibility(View.VISIBLE);
            }
            else{
                Toast toast = Toast.makeText( UploadActivity.this, "請新增至少1個界址點", Toast.LENGTH_SHORT);
                toast.show();
            }

        });
        btnBack.setOnClickListener(v -> finish());

        //初始化控件RecycleView
        listRv = findViewById(R.id.listRv);
        //初始化Adapter
        mPointAdapter = new PointAdapter(UploadActivity.this,new ArrayList<PointInfo>());
        //綁定Adapter
        listRv.setAdapter(mPointAdapter);

//        pointEdit = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),new ActivityResultCallback<ActivityResult>() {
//                    @Override
//                    public void onActivityResult(ActivityResult result) {
//                        if (result.getResultCode() == Activity.RESULT_OK) {
//                            // There are no request codes
//                            Intent data = result.getData();
//                            Log.d("PointActivity返回","----成功");
//
//                        }
//                        else Log.d("PointActivity返回","----未取得資料");
//                    }
//                });

        //RecycleList中button點擊事件
        mPointAdapter.setOnItemClickListener(new PointAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, PointAdapter.ViewName viewName, int position) {
                //點號編輯
                if(R.id.btnEdit == v.getId()){
                    Intent intent = new Intent(UploadActivity.this, PointEditActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("CaseName",textName.getText().toString());
                    bundle.putInt("position",position);
                    bundle.putSerializable("PointInfo",mPointAdapter.getPoint(position));
                    intent.putExtras(bundle);
                    pointEdit.launch(intent);
                }
                //刪除點號
                if(R.id.btnDel == v.getId()){
                    mPointAdapter.removePoint(position);
                }
            }

        });
    }

    //編輯頁面返回
    private final ActivityResultLauncher<Intent> pointEdit = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        int position = data.getIntExtra("position",-1);
                        PointInfo NumReturn= (PointInfo)data.getSerializableExtra("PointInfo");
                        mPointAdapter.editPointNum(NumReturn.getNum(),position);
                    }
                    else {Log.d("PointActivity返回","----未取得資料");}
                }
            });;
    private final ActivityResultLauncher<String> selectPhotos =
            registerForActivityResult(new ActivityResultContracts.GetMultipleContents() , uris -> {
                if (uris != null && !uris.isEmpty()) {
                    List<String> imgPaths = new ArrayList<>();
                    for(Uri uri:uris){
                        imgPaths.add( getRealPathFromURI(this,uri) );
                    }
                    //點號從最大點號續編
                    mPointAdapter.addPoint( imgPaths);
                }
                else {
                    Toast toast = Toast.makeText( UploadActivity.this, "未選取照片", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });

    public class ImageTask extends AsyncTask<String,Integer, Void>{
        @Override
        protected Void doInBackground(String... params) {
            String ZipPath =  params[0] + "/LP_1.zip";
            try {
                //PDF產製
                Document pdfDocument = new Document();
                String pdfPath = params[0] + "/"+textName.getText()+".pdf";
                PdfWriter.getInstance(pdfDocument, new FileOutputStream(pdfPath));
                pdfDocument.open();
                Font font = new Font( BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED),12, Font.NORMAL);

                //PDF表格初始化-標題表格
                PdfPTable tableTitle = new PdfPTable(1);
                tableTitle.setWidthPercentage(100);
                PdfPCell cellTitle = new PdfPCell(new Phrase("案號:"+textName.getText(),font));
                cellTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
                tableTitle.addCell(cellTitle);
                pdfDocument.add(tableTitle);
                //點號、照片表格
                PdfPTable table = new PdfPTable(15);
                table.setWidthPercentage(100);
                //小標儲存格
                PdfPCell cellNum = new PdfPCell(new Phrase("點號",font));
                cellNum.setColspan(2);
                cellNum.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellNum.setVerticalAlignment(Element.ALIGN_CENTER);
                PdfPCell cellImg = new PdfPCell(new Phrase("照片",font));
                cellImg.setColspan(13);
                cellImg.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellNum);
                table.addCell(cellImg);

                for(int PointIdx = 0;PointIdx < mPointAdapter.getItemCount();PointIdx++){

                    List<String> imgPaths = mPointAdapter.getPoint(PointIdx).getimgPath();

                    for(int imgIdx = 0; imgIdx < mPointAdapter.getPoint(PointIdx).getimgPath().size();imgIdx++){
                        File compressedFile = new File(params[0] + "/"+PointIdx+"_"+imgIdx+".jpg");
                        //圖片壓縮
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2;       // 採樣率
                        // 在將文件以Bitmap形式加載到內存的時候，加入Option參數
                        Bitmap bitmap = BitmapFactory.decodeFile(imgPaths.get(imgIdx), options);
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream( compressedFile ));
                        bitmap.compress(Bitmap.CompressFormat.JPEG,100,bos);
                        bos.close();

                        //圖片壓縮完畢加入PDF
                        Image image = Image.getInstance(compressedFile.getPath());
                        // 圖片縮放
                        float scaler = (pdfDocument.getPageSize().getWidth() / image.getWidth()) * 50;
                        image.scalePercent(scaler);
                        image.setRotationDegrees(-90);
                        //PDF表格處理
                        cellNum = new PdfPCell(new Phrase( mPointAdapter.getPoint(PointIdx).getNum()+"("+(imgIdx+1)+"/"+imgPaths.size()+")",font));
                        cellNum.setColspan(2);
                        cellNum.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cellNum.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        table.addCell(cellNum);
                        cellImg = new PdfPCell(image);
                        cellImg.setPadding(10);
                        cellImg.setColspan(13);
                        cellImg.setHorizontalAlignment(Element.ALIGN_CENTER);
                        table.addCell(cellImg);
                    }
                    publishProgress(PointIdx+1);
                }
                pdfDocument.add(table);
                pdfDocument.close();

                //PDF加入ZIP檔
                File pdfFile = new File(pdfPath);
                FileOutputStream fos = new FileOutputStream(ZipPath);
                ZipOutputStream zop = new ZipOutputStream(fos);
                byte[] bytes = new byte[1024*8];
                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(pdfFile));
                zop.putNextEntry(new ZipEntry(pdfFile.getName()));
                int length;
                while ((length = bufferedInputStream.read(bytes)) > 0) {
                    zop.write(bytes, 0, length);
                }
                zop.closeEntry();
                bufferedInputStream.close();
                zop.close();
                fos.close();

            }catch (Exception e) {
                Log.e("Pdf測試",e.toString());
                Toast toast = Toast.makeText( UploadActivity.this, "圖片壓縮失敗", Toast.LENGTH_SHORT);
                toast.show();
                ZipPath = "0";
            }
            postZipData(ZipPath);
            return null;
        }

        protected void onProgressUpdate(Integer... values) {
            final int progress = (int) (((double) values[0]/mPointAdapter.getItemCount()) * 100);
            textProgress.setText(String.format("檔案處理中%d%%", progress));
            progressBar.setProgress(progress);
        }

    }
    private void postZipData(String ZipPath) {
        if(ZipPath.equals("0")) {
            btnUpload.setVisibility(View.VISIBLE);
            btnBack.setVisibility(View.VISIBLE);
            textProgress.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            return;
        }
        File zipFile = new File( ZipPath );
        File deleteFile = new File(this.getFilesDir().getPath()); //上傳成功則清空此資料夾
        // RequestBody放要傳的參數和值
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("File", zipFile.getName(),
                        RequestBody.create(zipFile, MediaType.parse("application/octet-stream")))
                .build();

        //上傳進度監聽
        final CountingRequestBody.Listener progressListener = (bytesRead, contentLength) -> {
            if (bytesRead >= contentLength) { //檔案Post完成
                if (progressBar != null){
                    UploadActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast toast = Toast.makeText( UploadActivity.this, "上傳成功", Toast.LENGTH_SHORT);
                            toast.show();
                            finish();
                        }
                    });
                }

            } else { //檔案Post中
                if (contentLength > 0) {
                    final int progress = (int) (((double) bytesRead / contentLength) * 100);
                    if (progressBar != null) {
                        UploadActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                textProgress.setText(String.format("開始上傳%d%%", progress));
                                progressBar.setProgress(progress);
                            }
                        });
                    }
                }
            }
        };
        // 建立OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request originalRequest = chain.request();

                        if (originalRequest.body() == null) {
                            return chain.proceed(originalRequest);
                        }
                        Request progressRequest = originalRequest.newBuilder()
                                .method(originalRequest.method(),
                                        new CountingRequestBody(originalRequest.body(), progressListener))
                                .build();

                        return chain.proceed(progressRequest);
                    }
                })
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
                // 連線成功，則清空資料夾
                if(deleteFile.isDirectory()){
                    for(File file : Objects.requireNonNull(deleteFile.listFiles())) file.delete();
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                // 連線失敗
                Log.d("OkHttp", "傳送失敗"+e);
            }
        });
    }


}