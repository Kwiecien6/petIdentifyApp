package com.jyq.petidentifyapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.jyq.petidentifyapp.R;
import com.jyq.petidentifyapp.db.DatabaseHelper;
import com.jyq.petidentifyapp.db.PetInfo;
import com.jyq.petidentifyapp.util.LocationUtil;
import com.jyq.petidentifyapp.util.ToastUtil;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import static com.jyq.petidentifyapp.util.DateUtil.dateToStr;
import static com.jyq.petidentifyapp.util.DateUtil.dateToStrLong;
import static com.jyq.petidentifyapp.util.DateUtil.getNowDate;
import static com.jyq.petidentifyapp.util.DateUtil.strToDate;

public class MatcherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matcher);

        //修改状态栏背景与字体颜色
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        final PetInfo matcherPet = (PetInfo) getIntent().getSerializableExtra("matcherPet");
        final Bitmap petFace2Match = (Bitmap) getIntent().getParcelableExtra("petFace2Match");

        ImageView matcherPetFace = findViewById(R.id.matcherPetFace);
        ImageView matcherFeaturesPetFace = findViewById(R.id.matcherFeaturesPetFace);
        TextView matcherPetID = findViewById(R.id.matcherPetID);
        TextView matcherPetName = findViewById(R.id.matcherPetName);
        final EditText matcherPetType = findViewById(R.id.matcherPetType);
        final EditText matcherPetSex = findViewById(R.id.matcherPetSex);
        final EditText matcherPetBirth = findViewById(R.id.matcherPetBirth);
        final EditText matcherPetInfo = findViewById(R.id.matcherPetInfo);
        TextView matcherPetRegistLocation = findViewById(R.id.matcherPetRegistLocation);
        final TextView matcherPetHistLocation = findViewById(R.id.matcherPetHistLocation);
        final TextView matcherPetNowLocation = findViewById(R.id.matcherPetNowLocation);
        TextView matcherPetRegistTime = findViewById(R.id.matcherPetRegistTime);
        final TextView matcherPetUpdateTime = findViewById(R.id.matcherPetUpdateTime);
        Button matcherUpdateBtn = findViewById(R.id.matcherUpdateBtn);
        Button matcherFunctionBtn = findViewById(R.id.matcherFunctionBtn);

        Bitmap petFaceMatcher = BitmapFactory.decodeFile(matcherPet.getPetPicPath());
        matcherPetFace.setImageBitmap(petFaceMatcher);

        matcherFeaturesPetFace.setImageBitmap(drawMatches(petFace2Match,petFaceMatcher));

        matcherPetID.setText(matcherPet.getPetID().toString());
        matcherPetName.setText(matcherPet.getPetName());
        matcherPetType.setText(matcherPet.getPetType());
        matcherPetSex.setText(matcherPet.getPetSex());
        matcherPetBirth.setText(dateToStr(matcherPet.getPetBirth()));
        matcherPetRegistLocation.setText(matcherPet.getPetRegistLocation());
        matcherPetHistLocation.setText(matcherPet.getPetHistLocation());
        matcherPetNowLocation.setText(LocationUtil.getLocationAddress(LocationUtil.getMyLocation(MatcherActivity.this),MatcherActivity.this));
        matcherPetInfo.setText(matcherPet.getPetInfo());
        matcherPetRegistTime.setText(dateToStrLong(matcherPet.getPetRegistTime()));
        matcherPetUpdateTime.setText(dateToStrLong(matcherPet.getPetUpdateTime()));


        matcherFunctionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper updateHelper1 = new DatabaseHelper(MatcherActivity.this);
                matcherPet.setPetUpdateTime(getNowDate());
                matcherPetUpdateTime.setText(dateToStrLong(getNowDate()));
                matcherPet.setPetHistLocation(matcherPetHistLocation.getText().toString() + "\n\n" + matcherPetNowLocation.getText().toString());
                updateHelper1.updatePet(matcherPet);
                updateHelper1.close();
                ToastUtil.showToast(getApplicationContext(), "宠物 " + matcherPet.getPetName() + " 行动轨迹已更新", 1);
            }
        });


        matcherUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                matcherPet.setPetType(matcherPetType.getText().toString());
                matcherPet.setPetSex(matcherPetSex.getText().toString());
                matcherPet.setPetBirth(strToDate(matcherPetBirth.getText().toString()));
                matcherPet.setPetInfo(matcherPetInfo.getText().toString());
                matcherPet.setPetUpdateTime(getNowDate());

                DatabaseHelper updateHelper2 = new DatabaseHelper(MatcherActivity.this);
                updateHelper2.updatePet(matcherPet);

                matcherPetUpdateTime.setText(dateToStrLong(getNowDate()));
                updateHelper2.close();

                ToastUtil.showToast(getApplicationContext(), "宠物 " + matcherPet.getPetName() + " 已更新", 1);
            }
        });

        matcherPetBirth.setInputType(InputType.TYPE_NULL);
        matcherPetBirth.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Calendar c = Calendar.getInstance();
                    new DatePickerDialog(MatcherActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            matcherPetBirth.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                        }
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
                }
            }
        });

        matcherPetSex.setInputType(InputType.TYPE_NULL);
        matcherPetSex.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    String[] list = {"公", "母"};//要填充的数据
                    showListPopupWindow(list, matcherPetSex);
                }
            }
        });

    }

    /**
     * EditText下拉菜单ListPopupWindow
     */
    private void showListPopupWindow(final String[] list, final EditText editText) {
        final ListPopupWindow listPopupWindow;
        listPopupWindow = new ListPopupWindow(MatcherActivity.this);
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

    /**
     * 绘制两图像间的特征点匹配过程
     * @param src
     * @param dst
     * @return 绘制完成后的bitmap
     */
    private Bitmap drawMatches(Bitmap src,Bitmap dst){
        //初始化bitmap
        Mat srcMat = new Mat();
        Mat dstMat = new Mat();
        Utils.bitmapToMat(src, srcMat);
        Utils.bitmapToMat(dst, dstMat);
        Imgproc.cvtColor(srcMat, srcMat, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(dstMat, dstMat, Imgproc.COLOR_RGBA2RGB);

        //指定特征点检测器、描述子与匹配算法
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.AKAZE);
        DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.AKAZE);
        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        //关键点检测
        MatOfKeyPoint srcKeyPoint = new MatOfKeyPoint();
        MatOfKeyPoint dstKeyPoint = new MatOfKeyPoint();
        detector.detect(srcMat,srcKeyPoint);
        detector.detect(dstMat,dstKeyPoint);

        //描述子生成
        Mat srcDescriptor = new Mat();
        Mat dstDescriptor = new Mat();
        descriptorExtractor.compute(srcMat,srcKeyPoint,srcDescriptor);
        descriptorExtractor.compute(dstMat,dstKeyPoint,dstDescriptor);

        Features2d.drawKeypoints(srcMat,srcKeyPoint,srcMat);
        Features2d.drawKeypoints(dstMat,dstKeyPoint,dstMat);

        //特征匹配检测
        MatOfDMatch matches = new MatOfDMatch();
////        descriptorMatcher.match(srcDescriptor,dstDescriptor,matches);
////
////        //对匹配结果进行筛选
////        List<DMatch> list = matches.toList();
////        List<DMatch> goodMatch = new ArrayList<DMatch>();
////        for (int i = 0; i < list.size(); i++) {
////            DMatch dmatch = list.get(i);
////            if (Math.abs(dmatch.queryIdx - dmatch.trainIdx) < 0.5f) {
////                goodMatch.add(dmatch);
////            }
////
////        }
        List<MatOfDMatch> matchesList = new LinkedList();
        LinkedList<DMatch> goodMatch = new LinkedList();
        //使用KNN-matching算法，在给定特征描述集合中寻找最佳匹配
        // 令K=2，则每个match得到两个最接近的descriptor，然后计算最接近距离和次接近距离之间的比值，当比值大于既定值时，才作为最终match。
        descriptorMatcher.knnMatch(srcDescriptor,dstDescriptor,matchesList,2);

        //对匹配结果进行筛选，依据distance进行筛选
        for(int i = 0; i < matchesList.size(); i++){
            DMatch[] dmatchArray = matchesList.get(i).toArray();
            DMatch m1 = dmatchArray[0];
            DMatch m2 = dmatchArray[1];

            if (m1.distance <= m2.distance * 0.5f) {
                goodMatch.addLast(m1);
            }
        }
        matches.fromList(goodMatch);

        Mat resultMat = new Mat();
        Features2d.drawMatches(srcMat,srcKeyPoint,dstMat,dstKeyPoint,matches,resultMat);
        Bitmap resultBitmap = Bitmap.createBitmap(resultMat.width(), resultMat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(resultMat,resultBitmap);

        //释放内存
        srcKeyPoint.release();
        dstKeyPoint.release();
        srcDescriptor.release();
        dstDescriptor.release();
        matches.release();

        return resultBitmap;
    }
}