package com.jyq.petidentifyapp.adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jyq.petidentifyapp.R;
import com.jyq.petidentifyapp.db.PetInfo;

import java.util.List;

/**
 * 宠物信息适配器
 */

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.ViewHolder> {

    private List<PetInfo> pets;
    private Context mContext;


    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        ImageView petFace;
        TextView petName;
        TextView petType;
        TextView petSex;
        TextView petAge;

        private ViewHolder(View itemView) {
            super(itemView);
            linearLayout = (LinearLayout) itemView;
            petFace = (ImageView) itemView.findViewById(R.id.petFace);
            petName = (TextView) itemView.findViewById(R.id.petName);
            petType = (TextView) itemView.findViewById(R.id.pet_type);
            petSex = (TextView) itemView.findViewById(R.id.registerPetSex);
            petAge = (TextView) itemView.findViewById(R.id.petAge);
        }
    }

    public PetAdapter(List<PetInfo> pets) {
        this.pets = pets;
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
        PetInfo pet = pets.get(position);
        holder.petName.setText("昵称: " + pet.getPetName());
        holder.petType.setText("品种: " + pet.getPetType());
        holder.petSex.setText("性别: " + pet.getPetSex());
        holder.petAge.setText("年龄: " + pet.getPetAge());
        Bitmap bitmap = BitmapFactory.decodeFile(pet.getPetPicPath());
        holder.petFace.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return pets.size();
    }
}
