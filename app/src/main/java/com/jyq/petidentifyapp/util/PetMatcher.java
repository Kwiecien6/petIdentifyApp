package com.jyq.petidentifyapp.util;

import android.graphics.Bitmap;
import android.util.Log;

import com.jyq.petidentifyapp.db.PetInfo;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PetMatcher {
    private static final String TAG = "petMatcher";
    private static int counter;
    public final int UNFINISHED = -2;
    public final int NO_MATCHER = -1;
    private final int MAX_COUNTER = 64;
    private final double MY_SIMILARITY = 0.8;
    private float nndrRatio = 0.7f;
    private List<String> mPathList;

    public PetMatcher(List<PetInfo> pets) {
        counter = 0;
        mPathList = new ArrayList<>();
        for (PetInfo pet : pets) {
            mPathList.add(pet.getPetPicPath());
        }
    }

    public int histogramMatch(Bitmap bitmap) {
        if (counter < MAX_COUNTER) {
            Mat testMat = new Mat();
            Utils.bitmapToMat(bitmap, testMat);
            // 转灰度矩阵
            Imgproc.cvtColor(testMat, testMat, Imgproc.COLOR_RGB2GRAY);
            // 把矩阵的类型转换为Cv_32F，因为在c++代码中会判断类型
            testMat.convertTo(testMat, CvType.CV_32F);
            for (int i = 0; i < mPathList.size(); i++) {
                String path = mPathList.get(i);
                Mat mat = Imgcodecs.imread(path);
                Imgproc.resize(mat, mat, new Size(200, 200));
                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
                mat.convertTo(mat, CvType.CV_32F);
                // 直方图比较
                double similarity = Imgproc.compareHist(mat, testMat,
                        Imgproc.CV_COMP_CORREL);
                Log.e(TAG, "histogramMatch: " + similarity);
                if (similarity >= MY_SIMILARITY) {
                    Log.e(TAG, "histogramMatch: " + similarity + ", " + i);
                    return i;
                }
                if (similarity < MY_SIMILARITY && i == mPathList.size() - 1) {
                    Log.e(TAG, "histogramMatch: " + counter);
                    counter++;
                }
            }
            return UNFINISHED;
        } else {
            Log.e(TAG, "histogramMatch: 匹配结束");
            return NO_MATCHER;
        }
    }


    public int matchImage(Bitmap bitmap) {
        if (counter < MAX_COUNTER) {
            // 初始化待匹配图像
            Mat tempMat = new Mat();
            Utils.bitmapToMat(bitmap, tempMat);
            Imgproc.cvtColor(tempMat, tempMat, Imgproc.COLOR_RGBA2GRAY);

            for (int i = 0; i < mPathList.size(); i++) {
                String path = mPathList.get(i);
                Mat petsMat = Imgcodecs.imread(path);
                Imgproc.resize(petsMat, petsMat, new Size(200, 200));
                Imgproc.cvtColor(petsMat, petsMat, Imgproc.COLOR_RGBA2GRAY);

                boolean similarity = ORBMatch(tempMat,petsMat);

                if (similarity == true) {
                    return i;
                }
                if (similarity == false && i == mPathList.size() - 1) {
                    counter++;
                }
            }
            return UNFINISHED;
        } else {
            return NO_MATCHER;
        }
    }

    public boolean ORBMatch(Mat tempMat,Mat petsMat){
        Imgproc.cvtColor(tempMat, petsMat, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(tempMat, petsMat, Imgproc.COLOR_RGBA2RGB);

        //指定特征点检测器、描述子与匹配算法
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.AKAZE);
        DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.AKAZE);
        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        //关键点检测
        MatOfKeyPoint tempKeyPoint = new MatOfKeyPoint();
        MatOfKeyPoint petsKeyPoint = new MatOfKeyPoint();
        detector.detect(tempMat,tempKeyPoint);
        detector.detect(petsMat,petsKeyPoint);

        //描述子生成
        Mat tempDescriptors = new Mat();
        Mat petsDescriptors = new Mat();
        descriptorExtractor.compute(tempMat,tempKeyPoint,tempDescriptors);
        descriptorExtractor.compute(petsMat,petsKeyPoint,petsDescriptors);

        Features2d.drawKeypoints(tempMat,tempKeyPoint,tempMat);
        Features2d.drawKeypoints(petsMat,petsKeyPoint,petsMat);

        //特征匹配检测
        MatOfDMatch matches = new MatOfDMatch();
        descriptorMatcher.match(tempDescriptors,petsDescriptors,matches);

        List<DMatch> l = matches.toList();
        List<DMatch> goodMatch = new ArrayList<DMatch>();
        for (int i = 0; i < l.size(); i++) {
            DMatch dmatch = l.get(i);
            if (Math.abs(dmatch.queryIdx - dmatch.trainIdx) < 7f) {
                goodMatch.add(dmatch);
            }

        }

//        List<MatOfDMatch> matches = new LinkedList();
//        LinkedList<DMatch> goodMatchesList = new LinkedList();
//        //使用KNN-matching算法，在给定特征描述集合中寻找最佳匹配
//        // 令K=2，则每个match得到两个最接近的descriptor，然后计算最接近距离和次接近距离之间的比值，当比值大于既定值时，才作为最终match。
//        descriptorMatcher.knnMatch(tempDescriptors,petsDescriptors,matches,2);
//
//        //对匹配结果进行筛选，依据distance进行筛选
//        for(int i = 0; i < matches.size(); i++){
//            DMatch[] dmatchArray = matches.get(i).toArray();
//            DMatch m1 = dmatchArray[0];
//            DMatch m2 = dmatchArray[1];
//
//            if (m1.distance <= m2.distance * nndrRatio) {
//                goodMatchesList.addLast(m1);
//            }
//        }

        //释放内存
        tempKeyPoint.release();
        petsKeyPoint.release();
        tempDescriptors.release();
        petsDescriptors.release();
        matches.release();

        //当匹配后的特征点大于等于 10 个，则认为2图匹配成功
        if(goodMatch.size() >= 10 ){
            return true;
        }else return false;

    }

}
