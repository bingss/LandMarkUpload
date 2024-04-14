package com.example.LandMarkUpload;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.LandMarkUpload.bean.PointInfo;

import java.util.ArrayList;
import java.util.List;

class PointAdapter extends RecyclerView.Adapter<PointAdapter.MyHolder> implements View.OnClickListener {

    private List<PointInfo> mPointList = new ArrayList<>();
    private final Context mContext;

    public PointAdapter(Context context,List<PointInfo> list){
        mContext = context;
        mPointList = list;
    }

    public void addPoint(List<String> imgPaths){
        if(getItemCount()==0) mPointList.add( new PointInfo(1, imgPaths ) );
        else mPointList.add( new PointInfo(mPointList.get(getItemCount()-1).getNum()+1, imgPaths ) );
        notifyDataSetChanged();
    }

    public void editPointNum(int num,int position){
        if(mPointList.stream().filter(x -> x.getNum()== num).findFirst().orElse(null) != null){
            Toast.makeText( mContext, "編輯失敗，已有相同界址點號", Toast.LENGTH_SHORT).show();
            return;
        }
        mPointList.get(position).setNum(num);
        mPointList.sort((p1,p2) -> p1.getNum().compareTo(p2.getNum()));
        notifyDataSetChanged();
    }

    public void removePoint(int position){
        mPointList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,mPointList.size() - position);
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
        holder.text_sub.setText("共"+Point.getimgPath().size() + "張照片");
        //圖片加載
        Glide.with(mContext).load(Point.getimgPath().get(0))
                .error("@android:drawable/ic_delete")
                .into(holder.imageView);
        //設定buttonTAG
        holder.btnEdit.setTag(position);
        holder.btnDel.setTag(position);

    }

    @Override
    public int getItemCount() {
        return mPointList.size();
    }
    public PointInfo getPoint(int position){return mPointList.get(position);}

    public class MyHolder extends RecyclerView.ViewHolder{
       private TextView textNumber;
       private TextView text_sub;
       private ImageView imageView;
       private Button btnEdit,btnDel;
        public MyHolder(@NonNull View itemView)  {
            super(itemView);

            textNumber = itemView.findViewById(R.id.textNumber);
            text_sub = itemView.findViewById(R.id.textView_sub);
            imageView = itemView.findViewById(R.id.imageView);

            //點擊事件
            btnEdit = (Button) itemView.findViewById(R.id.btnEdit);
            btnDel = (Button)itemView.findViewById(R.id.btnDel);
            btnEdit.setOnClickListener(PointAdapter.this);
            btnDel.setOnClickListener(PointAdapter.this);
        }
    }

    //====Item中Button點擊事件處理====
    public enum ViewName{ Edit,Delete }
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener{
        void OnItemClick(View v,ViewName viewName,int position);
//        void OnItemLongClick(View v);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mOnItemClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        if(mOnItemClickListener != null){
            if(R.id.btnEdit == v.getId()) mOnItemClickListener.OnItemClick(v,ViewName.Edit,position);
            else if(R.id.btnDel == v.getId()) mOnItemClickListener.OnItemClick(v,ViewName.Delete,position);
        }
    }


}

