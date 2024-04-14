package com.example.LandMarkUpload;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.LandMarkUpload.R;
import com.example.LandMarkUpload.bean.PointInfo;

public class PointEditActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_point_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView textName = findViewById(R.id.textName);
        Button btnRevise=findViewById(R.id.btnRevise);
        Button btnCancel=findViewById(R.id.btnCancel);
        EditText edtNum = findViewById(R.id.edtNum);


        Bundle bundle = getIntent().getExtras();
        PointInfo pointinfo = (PointInfo)bundle.getSerializable("PointInfo");
        int position = bundle.getInt("position");
        textName.setText(bundle.getString("CaseName"));
        edtNum.setText(pointinfo.getNum().toString());

        Intent intent = new Intent();
        btnRevise.setOnClickListener(v -> {
            pointinfo.setNum( Integer.valueOf(edtNum.getText().toString()) );
            bundle.putSerializable("PointInfo",pointinfo);
            bundle.putInt("position",position);
            intent.putExtras(bundle);
            setResult(Activity.RESULT_OK,intent);
            this.finish();
        });

        btnCancel.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED,intent);
            this.finish();
        });



    }
}