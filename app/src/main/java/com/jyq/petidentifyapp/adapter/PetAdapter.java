package com.jyq.petidentifyapp.adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.jyq.petidentifyapp.R;
import com.jyq.petidentifyapp.activity.ViewDataActivity;
import com.jyq.petidentifyapp.db.PetInfo;
import com.jyq.petidentifyapp.util.ToastUtil;

import java.util.List;

import static com.jyq.petidentifyapp.util.DateUtil.dateToStr;
import static com.jyq.petidentifyapp.util.DateUtil.dateToStrLong;

/**
 * 宠物信息适配器
 */

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.ViewHolder> {

    private List<PetInfo> pets;
    private RecyclerView rv;
    private Context mContext;

    public PetAdapter(List<PetInfo> pets) {
        this.pets = pets;
    }

    private ViewDataActivity.OnItemClickListener onItemClickListener;//声明一下这个接口

    //提供setter方法
    public void setOnItemClickListener(ViewDataActivity.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView petFace;
        TextView petID;
        TextView petName;
        TextView petType;
        TextView petSex;
        TextView petBirth;


        private ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            petFace = (ImageView) itemView.findViewById(R.id.petFace);
            petID = (TextView) itemView.findViewById(R.id.petID);
            petName = (TextView) itemView.findViewById(R.id.petName);
            petType = (TextView) itemView.findViewById(R.id.petType);
            petSex = (TextView) itemView.findViewById(R.id.petSex);
            petBirth = (TextView) itemView.findViewById(R.id.petBirth);

            /**
             * 描述：将监听传递给自定义接口
             */
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(rv, v, getAdapterPosition(), pets.get(getAdapterPosition()));
                    }
                }
            });

        }
    }


    @NonNull
    @Override
    public PetAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_pet,
                parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetAdapter.ViewHolder holder, int position) {
        final PetInfo pet = pets.get(position);
        holder.petID.setText("编号: " + pet.getPetID().toString());
        holder.petName.setText("昵称: " + pet.getPetName());
        holder.petType.setText("品种: " + pet.getPetType());
        holder.petSex.setText("性别: " + pet.getPetSex());
        holder.petBirth.setText("出生日期: " + dateToStr(pet.getPetBirth()));
        Bitmap bitmap = BitmapFactory.decodeFile(pet.getPetPicPath());
        holder.petFace.setImageBitmap(bitmap);

    }

    @Override
    public int getItemCount() {
        return pets.size();
    }

}



