package com.example.LandMarkUpload;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.LandMarkUpload.bean.PointInfo;

import java.util.ArrayList;
import java.util.List;

class PointAdapter extends RecyclerView.Adapter<PointAdapter.MyHolder>{

    private List<PointInfo> mPointList = new ArrayList<>();
    private final Context mContext;

    public PointAdapter(Context context){
        mContext = context;
    }

    public void setListData(List<PointInfo> list){
        mPointList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_item,null);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PointAdapter.MyHolder holder, int position) {
        PointInfo Point = mPointList.get(position);

        //設置數據
        holder.textNumber.setText(String.format("界址點號：%d", Point.getNum()));
        holder.text_sub.setText(Point.getimgPath().size() + "張照片");
        //圖片加載
        Glide.with(mContext).load(Point.getimgPath().get(0))
                .error("@android:drawable/ic_delete")
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mPointList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{
        TextView textNumber;
        TextView text_sub;
        ImageView imageView;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            textNumber = itemView.findViewById(R.id.textNumber);
            text_sub = itemView.findViewById(R.id.textView_sub);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
