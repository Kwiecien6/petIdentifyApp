package com.jyq.petidentifyapp.activity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jyq.petidentifyapp.R;
import com.jyq.petidentifyapp.adapter.PetAdapter;
import com.jyq.petidentifyapp.db.DatabaseHelper;
import com.jyq.petidentifyapp.db.PetInfo;
import com.jyq.petidentifyapp.util.ToastUtil;

import java.util.Calendar;
import java.util.List;

import static com.jyq.petidentifyapp.util.DateUtil.dateToStr;
import static com.jyq.petidentifyapp.util.DateUtil.dateToStrLong;
import static com.jyq.petidentifyapp.util.DateUtil.getNowDate;
import static com.jyq.petidentifyapp.util.DateUtil.strToDate;

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
     * 刷新当前页面
     */
    public static void restartActivity(Activity activity) {
        Intent intent = new Intent();
        intent.setClass(activity, activity.getClass());
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
        activity.finish();
    }


    /**
     * 定义RecyclerView选项单击事件的回调接口
     */
    public interface OnItemClickListener {
        //参数（父组件，当前单击的View,单击的View的位置，数据）
        void onItemClick(RecyclerView parent, View view, int position, PetInfo pet);
    }


    private void initPopWindow(View v, final PetInfo pet) {
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_pet_detail,
                null, false);

        ImageView detailPetFace = view.findViewById(R.id.detailPetFace);
        TextView detailPetID = view.findViewById(R.id.detailPetID);
        TextView detailPetName = view.findViewById(R.id.detailPetName);
        final EditText detailPetType = view.findViewById(R.id.detailPetType);
        final EditText detailPetSex = view.findViewById(R.id.detailPetSex);
        final EditText detailPetBirth = view.findViewById(R.id.detailPetBirth);
        final EditText detailPetInfo = view.findViewById(R.id.detailPetInfo);
        TextView detailPetRegistTime = view.findViewById(R.id.detailPetRegistTime);
        TextView detailPetUpdateTime = view.findViewById(R.id.detailPetUpdateTime);
        Button detailDeleteBtn = view.findViewById(R.id.detailDeleteBtn);
        Button detailUpdateBtn = view.findViewById(R.id.detailUpdateBtn);

        //构造一个PopupWindow，参数依次是加载的View，宽高
        final PopupWindow popWindow = new PopupWindow(view,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popWindow.setAnimationStyle(R.style.PopupAnimation);  //设置加载动画

        detailPetFace.setImageBitmap(BitmapFactory.decodeFile(pet.getPetPicPath()));
        detailPetID.setText(pet.getPetID().toString());
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

        // 这里如果返回true的话，touch事件将被拦截
        // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
        popWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;

            }
        });

        popWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                darkenBackground(1f);
            }
        });

        //设置popupWindow显示的位置，参数依次是参照View，x轴的偏移量，y轴的偏移量
        popWindow.showAtLocation(ViewDataActivity.this.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        darkenBackground(0.3f);

        detailDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper delHelper = new DatabaseHelper(ViewDataActivity.this);
                delHelper.deleteID(pet.getPetID());
                delHelper.close();

                ToastUtil.showToast(getApplicationContext(), "宠物 " + pet.getPetName() + " 已删除", 1);
                popWindow.dismiss();
                restartActivity(ViewDataActivity.this);
            }
        });

        detailUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pet.setPetType(detailPetType.getText().toString());
                pet.setPetSex(detailPetSex.getText().toString());
                pet.setPetBirth(strToDate(detailPetBirth.getText().toString()));
                pet.setPetInfo(detailPetInfo.getText().toString());
                pet.setPetUpdateTime(getNowDate());

                DatabaseHelper updateHelper = new DatabaseHelper(ViewDataActivity.this);
                updateHelper.updatePet(pet);
                updateHelper.close();

                ToastUtil.showToast(getApplicationContext(), "宠物 " + pet.getPetName() + " 已更新", 1);
                restartActivity(ViewDataActivity.this);
            }
        });

        detailPetBirth.setInputType(InputType.TYPE_NULL);
        detailPetBirth.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Calendar c = Calendar.getInstance();
                    new DatePickerDialog(ViewDataActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            detailPetBirth.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                        }
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
                }
            }
        });

        detailPetSex.setInputType(InputType.TYPE_NULL);
        detailPetSex.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    String[] list = {"公", "母"};//要填充的数据
                    showListPopupWindow(list, detailPetSex);
                }
            }
        });

    }


    /**
     * 改变背景颜色
     */
    private void darkenBackground(Float bgcolor) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgcolor;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setAttributes(lp);
    }

    /**
     * EditText下拉菜单ListPopupWindow
     */
    private void showListPopupWindow(final String[] list, final EditText editText) {
        final ListPopupWindow listPopupWindow;
        listPopupWindow = new ListPopupWindow(ViewDataActivity.this);
        listPopupWindow.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, list));//用android内置布局，或设计自己的样式
        listPopupWindow.setAnchorView(editText);//以哪个控件为基准，在该处以logId为基准
        listPopupWindow.setModal(true);

        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {//设置项点击监听
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                editText.setText(list[i]);//把选择的选项内容展示在EditText上
                listPopupWindow.dismiss();//如果已经选择了，隐藏起来
            }
        });
        listPopupWindow.show();//把ListPopWindow展示出来
    }

}