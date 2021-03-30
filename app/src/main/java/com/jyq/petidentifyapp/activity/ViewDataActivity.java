package com.jyq.petidentifyapp.activity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.SearchView;
import android.widget.TextView;

import com.jyq.petidentifyapp.R;
import com.jyq.petidentifyapp.adapter.PetAdapter;
import com.jyq.petidentifyapp.db.DatabaseHelper;
import com.jyq.petidentifyapp.db.PetInfo;
import com.jyq.petidentifyapp.util.ToastUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.List;

import static com.jyq.petidentifyapp.util.DateUtil.dateToStr;
import static com.jyq.petidentifyapp.util.DateUtil.dateToStrLong;
import static com.jyq.petidentifyapp.util.DateUtil.getNowDate;
import static com.jyq.petidentifyapp.util.DateUtil.strToDate;

public class ViewDataActivity extends Activity {

    DatabaseHelper helper;
    PetAdapter mAdapter;
    List<PetInfo> pets;
    List<PetInfo> tempList;
    RecyclerView recyclerView;
    SearchView searchView;
    ImageView imageView;

    ImageView detailPetDailyPic;
    Bitmap petDailyPicBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_data);

        //修改状态栏背景与字体颜色
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        imageView = findViewById(R.id.nullTipImg);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        helper = new DatabaseHelper(this);
        pets = helper.query();
//        helper.close();
        if (pets.size() == 0) {
            imageView.setVisibility(View.VISIBLE);
            ToastUtil.showToast(getApplicationContext(), "暂无宠物数据", 0);
        } else {
            imageView.setVisibility(View.INVISIBLE);

            mAdapter = new PetAdapter(pets);
            recyclerView.setAdapter(mAdapter);

            //设置每个Item的单击事件
            mAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(RecyclerView parent, View view, int position, PetInfo pet) {
                    initPopWindow(view, pet);
                }
            });
        }

        //搜索框监听事件
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                pets.clear();
                tempList = helper.find(query);
                if(tempList.size() == 0){
                    imageView.setVisibility(View.VISIBLE);
                    ToastUtil.showToast(getApplicationContext(), "暂未查找到该宠物", 0);
                }else{
                    imageView.setVisibility(View.INVISIBLE);
                    pets.addAll(tempList);
                }
                mAdapter.notifyDataSetChanged();
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                pets.clear();
                tempList = helper.find(newText);
                if(tempList.size() == 0){
                    imageView.setVisibility(View.VISIBLE);
                    ToastUtil.showToast(getApplicationContext(), "暂未查找到该宠物", 0);
                }else{
                    imageView.setVisibility(View.INVISIBLE);
                    pets.addAll(tempList);
                }
                mAdapter.notifyDataSetChanged();
                return false;
            }
        });

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
        detailPetDailyPic = view.findViewById(R.id.detailPetDailyPic);
        TextView detailPetID = view.findViewById(R.id.detailPetID);
        TextView detailPetName = view.findViewById(R.id.detailPetName);
        final EditText detailPetType = view.findViewById(R.id.detailPetType);
        final EditText detailPetSex = view.findViewById(R.id.detailPetSex);
        final EditText detailPetSterilization = view.findViewById(R.id.detailPetSterilization);
        final EditText detailPetBirth = view.findViewById(R.id.detailPetBirth);
        final EditText detailPetState = view.findViewById(R.id.detailPetState);
        final EditText detailPetOwner = view.findViewById(R.id.detailPetOwner);
        final EditText detailPetOwnerPhone = view.findViewById(R.id.detailPetOwnerPhone);
        final EditText detailPetInfo = view.findViewById(R.id.detailPetInfo);
        TextView detailPetRegistLocation = view.findViewById(R.id.detailPetRegistLocation);
        final TextView detailPetHistLocation = view.findViewById(R.id.detailPetHistLocation);
        TextView detailPetRegistTime = view.findViewById(R.id.detailPetRegistTime);
        TextView detailPetUpdateTime = view.findViewById(R.id.detailPetUpdateTime);
        Button detailDeleteBtn = view.findViewById(R.id.detailDeleteBtn);
        Button detailUpdateBtn = view.findViewById(R.id.detailUpdateBtn);

        //构造一个PopupWindow，参数依次是加载的View，宽高
        final PopupWindow popWindow = new PopupWindow(view,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popWindow.setAnimationStyle(R.style.PopupAnimation);  //设置加载动画

        detailPetFace.setImageBitmap(BitmapFactory.decodeFile(pet.getPetPicPath()));
        detailPetDailyPic.setImageBitmap(BitmapFactory.decodeFile(pet.getPetDailyPicPath()));
        petDailyPicBitmap = BitmapFactory.decodeFile(pet.getPetDailyPicPath());
        detailPetID.setText(pet.getPetID());
        detailPetName.setText(pet.getPetName());
        detailPetType.setText(pet.getPetType());
        detailPetSex.setText(pet.getPetSex());
        detailPetSterilization.setText(pet.getPetSterilization());
        detailPetBirth.setText(dateToStr(pet.getPetBirth()));
        detailPetState.setText(pet.getPetState());
        detailPetOwner.setText(pet.getPetOwner());
        detailPetOwnerPhone.setText(pet.getPetOwnerPhone());
        detailPetRegistLocation.setText(pet.getPetRegistLocation());
        detailPetHistLocation.setText(pet.getPetHistLocation());
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
                DatabaseHelper updateHelper = new DatabaseHelper(ViewDataActivity.this);

                pet.setPetType(detailPetType.getText().toString());
                pet.setPetSex(detailPetSex.getText().toString());
                pet.setPetSterilization(detailPetSterilization.getText().toString());
                pet.setPetBirth(strToDate(detailPetBirth.getText().toString()));
                pet.setPetState(detailPetState.getText().toString());
                pet.setPetOwner(detailPetOwner.getText().toString());
                pet.setPetOwnerPhone(detailPetOwnerPhone.getText().toString());
                pet.setPetHistLocation(detailPetHistLocation.getText().toString());

                String petDailyPicPath = updateHelper.saveBitmapToLocal(petDailyPicBitmap, pet.getPetID() + "dailypic");
                pet.setPetDailyPicPath(petDailyPicPath);

                pet.setPetInfo(detailPetInfo.getText().toString());
                pet.setPetUpdateTime(getNowDate());

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


        detailPetType.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    String[] list =getResources().getStringArray(R.array.pet_type);
                    showListPopupWindow(list, detailPetType);
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

        detailPetSterilization.setInputType(InputType.TYPE_NULL);
        detailPetSterilization.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    String[] list = {"已绝育", "未绝育"};//要填充的数据
                    showListPopupWindow(list, detailPetSterilization);
                }
            }
        });

        detailPetState.setInputType(InputType.TYPE_NULL);
        detailPetState.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    String[] list = {"正常", "走失"};//要填充的数据
                    showListPopupWindow(list, detailPetState);
                }
            }
        });

        detailPetDailyPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //读取相册图片
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });

    }


    //相册Intent回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //获取图片路径
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String petDailyPicPath = c.getString(columnIndex);
            c.close();

            petDailyPicBitmap = BitmapFactory.decodeFile(petDailyPicPath);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            petDailyPicBitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
            petDailyPicBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(baos.toByteArray()), null, null);
            detailPetDailyPic.setImageBitmap(petDailyPicBitmap);
        }
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