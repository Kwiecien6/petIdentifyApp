package com.jyq.petidentifyapp.activity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.jyq.petidentifyapp.R;
import com.jyq.petidentifyapp.adapter.PetAdapter;
import com.jyq.petidentifyapp.db.DatabaseHelper;
import com.jyq.petidentifyapp.db.PetInfo;
import com.jyq.petidentifyapp.util.ToastUtil;

import java.util.List;

import static com.jyq.petidentifyapp.util.DateUtil.dateToStr;
import static com.jyq.petidentifyapp.util.DateUtil.dateToStrLong;

public class ViewDataActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_data);

        ImageView imageView = findViewById(R.id.nullTipImg);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DatabaseHelper helper = new DatabaseHelper(this);
        List<PetInfo> pets = helper.query();
        helper.close();
        if (pets.size() == 0) {
            imageView.setVisibility(View.VISIBLE);
            ToastUtil.showToast(getApplicationContext(), "暂无宠物数据", 0);
        } else {
            imageView.setVisibility(View.INVISIBLE);

            PetAdapter mAdapter = new PetAdapter(pets);
            recyclerView.setAdapter(mAdapter);

            //设置每个Item的单击事件
            mAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(RecyclerView parent, View view, int position, PetInfo pet) {
                    initPopWindow(view, pet);
                }
            });
        }

    }


    /**
     * 定义RecyclerView选项单击事件的回调接口
     */
    public interface OnItemClickListener {
        //参数（父组件，当前单击的View,单击的View的位置，数据）
        void onItemClick(RecyclerView parent, View view, int position, PetInfo pet);
    }


    private void initPopWindow(View v, PetInfo pet) {
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_pet_detail, null, false);

        ImageView detailPetFace = view.findViewById(R.id.detailPetFace);
        TextView detailPetName = view.findViewById(R.id.detailPetName);
        EditText detailPetType = view.findViewById(R.id.detailPetType);
        EditText detailPetSex = view.findViewById(R.id.detailPetSex);
        EditText detailPetBirth = view.findViewById(R.id.detailPetBirth);
        EditText detailPetInfo = view.findViewById(R.id.detailPetInfo);
        TextView detailPetRegistTime = view.findViewById(R.id.detailPetRegistTime);
        TextView detailPetUpdateTime = view.findViewById(R.id.detailPetUpdateTime);

        //构造一个PopupWindow，参数依次是加载的View，宽高
        final PopupWindow popWindow = new PopupWindow(view,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popWindow.setAnimationStyle(R.style.PopupAnimation);  //设置加载动画

        detailPetFace.setImageBitmap(BitmapFactory.decodeFile(pet.getPetPicPath()));
        detailPetName.setText(pet.getPetName());
        detailPetType.setText(pet.getPetType());
        detailPetSex.setText(pet.getPetSex());
        detailPetBirth.setText(dateToStr(pet.getPetBirth()));
        detailPetInfo.setText(pet.getPetInfo());
        detailPetRegistTime.setText(dateToStrLong(pet.getPetRegistTime()));
        detailPetUpdateTime.setText(dateToStrLong(pet.getPetUpdateTime()));

        //这些为了点击非PopupWindow区域，PopupWindow会消失的，如果没有下面的
        //代码的话，你会发现，当你把PopupWindow显示出来了，无论你按多少次后退键
        //PopupWindow并不会关闭，而且退不出程序，加上下述代码可以解决这个问题
        popWindow.setTouchable(true);
        popWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });


        //设置popupWindow显示的位置，参数依次是参照View，x轴的偏移量，y轴的偏移量
        popWindow.showAtLocation(ViewDataActivity.this.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        darkenBackground(0.2f);

        popWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                darkenBackground(1f);
            }
        });

    }


    /**
     * 改变背景颜色
     */
    private void darkenBackground(Float bgcolor){
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgcolor;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setAttributes(lp);
    }

}