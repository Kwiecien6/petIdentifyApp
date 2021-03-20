2020.11.21
Android Studio配置openCV环境，使用openCV3.3.0 Android SDK（参考方法链接）
通过git与github进行版本管理
调节图像灰度测试成功

存在问题：调节灰度图片较大（4.95MB）时程序崩溃，换用较小图片（769KB）后通过，所涉及代码段如下
    //调节图像灰度（功能测试）
    private void convert2Gray(){
        Bitmap image = BitmapFactory.decodeResource(getResources(),R.drawable.samplepic);
        Mat src = new Mat();
        Mat dst = new Mat();
        Utils.bitmapToMat(image,src);//把image转化为Mat
        Imgproc.cvtColor(src,dst,Imgproc.COLOR_BGRA2GRAY);
        Utils.matToBitmap(dst,image);//把mat转化为bitmap
        ImageView imageView = findViewById(R.id.imgView);
        imageView.setImageBitmap(image);
        //release
        src.release();
        dst.release();
    }



2020.11.22
测试调用OpenCV android SDK中的API打开相机以及实时图像信息数据的获取。
测试实时图像处理功能如灰化、Canny边缘检测、Hist直方图计算、Sobel边缘检测、SEPIA(色调变换)、ZOOM放大镜、PIXELIZE像素化。
打开相机—>获取图像流 —> 模式识别 —> 对象跟踪 —> 绘制模型


问题：
open CV环境下不可使用BindView方法绑定控件，否则会引起程序崩溃。
借助JavaCameraView控件实时获取到的初始图像信息方向颠倒，因为JavaCameraView获取系统camera的帧数据就是横屏的，OpenCv并没有给我们经过处理。解决方法：
方法1（修改为横屏显示） https://www.cnblogs.com/carl2380/p/4193194.html
方法2 Android OpenCV使用3_使用OpenCV并进行人脸检测，转竖屏



2020.11.23
借助opencv自带的xml人脸模型文件(OpenCV3.3.0\opencv\build\etc\haarcascades)，测试实现实时物体检测与矩形框选（物体检测目前写在onCameraFrame函数中，FPS较低，待改进。可用多线程改善，一个线程渲染，一个线程后台识别，返回结果画出来，可能会存在一定的延迟）Android—基于OpenCV+Android实现人脸检测

//安装Anaconda3，配置python开发环境，用于训练得到模型文件。




2020.11.24
下载opencv3.3.0 win pack，为VS2017配置OpenCV3开发环境，使用C++与openCV进行样本图片预处理
预处理内容：正样本图像灰度化，统一尺寸为200*200；负样本不变；
使用opencv自带程序opencv_createsamples.exe&opencv_traincascade.exe来获得训练集和训练xml文件（opencv安装路径\opencv\build\x64\vc14\bin中）；

负样本来源：
数据集 -- PASCAL Visual Object Classes Challenge 2012 图像数据(VOC2012) | 聚数力平台 | 大数据应用要素托管与交易平台 (dataju.cn)
数据集 -- MIT Places2 图像数据 | 聚数力平台 | 大数据应用要素托管与交易平台 (dataju.cn)
数据集 -- STL-10 数据集 | 聚数力平台 | 大数据应用要素托管与交易平台 (dataju.cn)

正样本描述文件生成代码：
opencv_createsamples.exe -info positive.txt -vec pos.vec -num 12500 -w 20 -h 20
pause

训练分类器代码：（注意大小写，否则不会识别）
opencv_traincascade.exe -data xml -vec pos.vec -bg negative.txt -numPos 7000 -numNeg 21000 -featureType HAAR -numStages 15 -w 20 -h 20 -mode ALL -mem 3000 -minHitRate 0.95 -maxFalseAlarmRate 0.5 -weightTrimRate 0.9
pause

正样本数目numPos，应小于vec文件中正样本的数目，具体数目限制条件为：
numPos+（numStages-1）*numPos*（1-minHitRate）<= vec文件中正样本的数目

－minhitrate<min_hit_rate>
    每个阶段分类器需要的最小的命中率。总的命中率为min_hit_rate的number_of_stages次方。
－maxfalsealarm<max_false_alarm_rate> (第一次训练使用的0.95^15=0.4632…，不合适，应改为0.995或更高)
    没有阶段分类器的最大错误报警率。总的错误警告率为max_false_alarm_rate的number_of_stages次方。


流程：
1.准备训练样本图片，包括正例及反例样本
2.生成样本描述文件
3.训练样本
4.目标识别



2020.11.25
使用opencv_traincascade.exe训练xml文件，测试训练识别猫脸。
（此次训练所设置的minHitRate参数有误）

猫脸识别训练
记录：
POS count:consumed 7000:7000
NEG count:acceptanceRatio 21000:1

0-stage
N	HR	FA
26	0.95	0.489286

2 hours 40 min
POS count:consumed 7000:7376
NEG count:acceptanceRatio 21000:0.525289

1-stage
N	HR	FA
58	0.95	0.499524

5 hours 53 min
POS count:consumed 7000:7780
NEG count:acceptanceRatio 21000:0.262835



2020.11.26
2-stage
N	HR	FA
81	0.95	0.499524

7 hours 58 min
POS count:consumed 7000:8216
NEG count:acceptanceRatio 21000:0.13145



2020.11.27
配置腾讯云服务器，用于训练分类器
如何将本地文件拷贝到云服务器
Windows 系统通过 MSTSC 上传文件到 Windows 云服务器
拷贝训练数据后，注意修改样本路径，并重新生成正样本描述文件VAC。

猫脸识别第3级训练陷入死循环，traincascade输出参数中FA在1与0.996间循环，原因暂未查清。且由于训练时长过久，决定缩减训练样本数量，测试训练狗脸识别。

－minhitrate<min_hit_rate>
    每个阶段分类器需要的最小的命中率。总的命中率为min_hit_rate的number_of_stages次方。
－maxfalsealarm<max_false_alarm_rate>(第一次测试使用的0.95^15=0.4632…，不合适，应改为0.995或更高)
    没有阶段分类器的最大错误报警率。总的错误警告率为max_false_alarm_rate的number_of_stages次方。

修改训练分类器代码：（注意大小写，否则不会识别）
opencv_traincascade.exe -data xml -vec pos.vec -bg negative.txt -numPos 1000 -numNeg 3000 -featureType HAAR -numStages 15 -w 20 -h 20 -mode ALL -mem 3000 -minHitRate 0.995 -maxFalseAlarmRate 0.5 -weightTrimRate 0.9
pause

狗脸识别训练
记录：
POS count:consumed 1000:1000
NEG count:acceptanceRatio 3000:1

0-stage
N	HR	FA
33	0.996	0.427

0 hours 59 min
POS count:consumed 1000:1004
NEG count:acceptanceRatio 3000:0.485358

1-stage
N	HR	FA
48	0.996	0.469333

2 hours 34 min
POS count:consumed 1000:1008
NEG count:acceptanceRatio 3000:0.283473

2-stage
N	HR	FA
63	0.996	0.492333

4 hours 40 min
POS count:consumed 1000:1013
NEG count:acceptanceRatio 3000:0.191436

……

用时 2 days 2 hours 27 minutes 44 seconds

2020.11.28
traincascade训练失败，训练结果xml文件无法识别狗脸，考虑样本是否出现问题。

查阅到可以使用roi（region of interest）进行图像区域分割与提取。
宠物档案：
宠物昵称、宠物品种、宠物年龄、宠物性别、照片


2020.11.29---2020.12.1
添加软件图标；
为列表添加卡片式布局；
暂借opencv自带的人脸模型文件，实现目标物体检测、识别、存储（物体识别与匹配算法准确度较低，待改进。数据目前存储在本地SQLite）


2020.12.2
添加约束使宠物昵称唯一；
增加注册时的昵称校验；
优化UI与软件图标，宠物档案使用CardView；

待优化：
识别界面待改进，可增加开始检测按钮，避免以刚摄像头刚启动时传送模糊图像message。
身份验证成功后信息详情界面未写；
识别匹配算法待改进；


2020.12.3
按钮美化；
检测界面添加开始按钮；
考虑将宠物年龄改为出生年月；

存在问题：
应用相机权限为询问时，相机无法调用，且未弹出申请权限界面。
解决方法：未主动申请权限，调用相机前添加判断，唤起申请权限
//申请相机权限
if(ContextCompat.checkSelfPermission(DetectActivity.this,
android.Manifest.permission.CAMERA)==PackageManager.PERMISSION_DENIED){
//判断为没有权限，唤起权限申请询问
ActivityCompat.requestPermissions(DetectActivity.this,newString[]{android.Manifest.permission.CAMERA},1);
}else{
//判断已经获取权限后的操作
}


2020.12.4
宠物档案：
照片、编号（主键）、宠物昵称（唯一）、宠物品种、宠物性别、出生日期、介绍、注册时间、更新时间。
Android Date时间工具类_白鲸-CSDN博客_android date
注册界面添加日期选择器：点击EditText 弹出日期选择器DatePickerDialog_薛瑄的博客-CSDN博客

宠物档案界面改进：点击弹出详情（使用PopupWindow显示详情，添加显示消失动画效果）
为RecyclerView的item设置点击事件 - 是可乐呀 - 博客园 (cnblogs.com)
android开发：给RecyclerView的item添加点击事件_wangxiaojia42121的博客-CSDN博客_recyclerview设置点击事件
2.6.1 PopupWindow(悬浮框)的基本使用 | 菜鸟教程 (runoob.com)
用setAnimationStyle来设置popwindow显示消失的动画效果_aiguoguo000的博客-CSDN博客



2020.12.5
实现宠物信息的删改功能
主要实现代码：
 public void deleteID(Integer id) {
        db.delete("pet_data", "id=?", new String[]{id.toString()});
    }

 public void updatePet(PetInfo pet) {
        ContentValues values = new ContentValues();

        values.put("type",pet.getPetType());
        values.put("sex",pet.getPetSex());
        values.put("birth",dateToStr(pet.getPetBirth()));
        values.put("info",pet.getPetInfo());
        values.put("updateTime", dateToStrLong(getNowDate()));

        db.update("pet_data", values,"id=?",new String[]{pet.getPetID().toString()});
    }











2021.01.20
基于特征点的 SURF 匹配
要解决旋转缩放后的模板图片再匹配原图的问题，就用到了计算机视觉处理算法中的特征变换匹配算法。其思路是先找到图像中的一些“稳定点”，这些点不会因为视角的改变、光照的变化、噪音的干扰而消失，比如角点、边缘点、暗区域的亮点以及亮区域的暗点。这样如果两幅图中有相同的景物，那么稳定点就会在两幅图像的相同景物上同时出现，这样就能实现匹配。
OpenCV 中针对特征点匹配问题已经提供了很多算法，包括 FAST 、SIFT 、SURF 、ORB 等，这里不赘述这些算法之间的区别，直接以 SURF 为例，看下 OpenCV 里面如何应用的。

来自 <https://www.jianshu.com/p/13d94f2a8f64?hmsr=toutiao.io&utm_medium=toutiao.io&utm_source=toutiao.io> 



宠物身份验证功能：验证成功后可添加类似的特征点匹配效果图

由上面的特征点匹配的效果来看，匹配的效果还是相当糟糕的，如果我们拿着这样子的匹配结果去实现图像拼接或者物体追踪，效果肯定是极差的。所以我们需要进一步筛选匹配点，来获取优秀的匹配点，这就是所谓的“去粗取精”。这里我们采用了Lowe’s算法来进一步获取优秀匹配点。
为了排除因为图像遮挡和背景混乱而产生的无匹配关系的关键点，SIFT的作者Lowe提出了比较最近邻距离与次近邻距离的SIFT匹配方式：取一幅图像中的一个SIFT关键点，并找出其与另一幅图像中欧式距离最近的前两个关键点，在这两个关键点中，如果最近的距离除以次近的距离得到的比率ratio少于某个阈值T，则接受这一对匹配点。因为对于错误匹配，由于特征空间的高维性，相似的距离可能有大量其他的错误匹配，从而它的ratio值比较高。显然降低这个比例阈值T，SIFT匹配点数目会减少，但更加稳定，反之亦然。
Lowe推荐ratio的阈值为0.8，但作者对大量任意存在尺度、旋转和亮度变化的两幅图片进行匹配，结果表明ratio取值在0. 4~0. 6 之间最佳，小于0. 4的很少有匹配点，大于0. 6的则存在大量错误匹配点，所以建议ratio的取值原则如下:
ratio=0. 4：对于准确度要求高的匹配；
ratio=0. 6：对于匹配点数目要求比较多的匹配；
ratio=0. 5：一般情况下。

来自 <https://www.cnblogs.com/skyfsm/p/7401523.html> 



问题：byte[] buffer=new byte[1024000]; byte[]过小时无法正常读取分类器文件


 
2021.01.22
问题代码：
FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.SURF );
opencv的features2d包中提供了surf,sift和orb等特征点算法，根据测试结果发现在opencv3.0的java版本中存在一些bug，导致surf算法无法使用，会抛出如下异常：
error: (-5) Specified feature detector type is not supported.

https://blog.csdn.net/woshishui6501/article/details/78869931
https://stackoverflow.com/questions/30657774/surf-and-sift-algorithms-doesnt-work-in-opencv-3-0-java



引用其他用户修改完善的SDK
https://github.com/gloomyfish1998/opencv4android


2021.02.04
添加宠物匹配成功后的信息界面，提供信息修改功能，匹配方法与匹配过程图形化展示待添加与改进


2021.03.06
实现宠物注册界面下宠物面部图像的ORB特征点检测与展示
特征点绘制前，输入的MAT图像类型错误时会导致闪退，通过Imgproc.cvtColor方法进行转换
https://vimsky.com/examples/detail/java-method-org.opencv.features2d.Features2d.drawKeypoints.html
//ORB特征点检测与绘制
FeatureDetectordetector=FeatureDetector.create(FeatureDetector.ORB);
MatOfKeyPointkeyPoints=newMatOfKeyPoint();
MatpetFaceMat=newMat();
Utils.bitmapToMat(petFace,petFaceMat);
Imgproc.cvtColor(petFaceMat,petFaceMat,Imgproc.COLOR_RGBA2RGB);
detector.detect(petFaceMat,keyPoints);
Features2d.drawKeypoints(petFaceMat,keyPoints,petFaceMat);
Imgproc.cvtColor(petFaceMat,petFaceMat,Imgproc.COLOR_RGB2RGBA);
Utils.matToBitmap(petFaceMat,petFace);


2021.03.07
为宠物匹配成功后的界面添加特征点匹配过程展示图；
经比较，AKAZE检测器与描述子 比 ORB检测器与描述子 准确率更高；

问题：DescriptorMatcher.FLANNBASED无法使用，改为DescriptorMatcher.BRUTEFORCE_HAMMING

参考案例：
https://www.programcreek.com/java-api-examples/index.php?api=org.opencv.features2d.Features2d


主要实现代码：
/**
*绘制两图像间的特征点匹配过程
*@paramsrc
*@paramdst
*@return绘制完成后的bitmap
*/
privateBitmapdrawMatches(Bitmapsrc,Bitmapdst){
//初始化bitmap
MatsrcMat=newMat();
MatdstMat=newMat();
Utils.bitmapToMat(src,srcMat);
Utils.bitmapToMat(dst,dstMat);
Imgproc.cvtColor(srcMat,srcMat,Imgproc.COLOR_RGBA2RGB);
Imgproc.cvtColor(dstMat,dstMat,Imgproc.COLOR_RGBA2RGB);

//指定特征点检测器、描述子与匹配算法
FeatureDetectordetector=FeatureDetector.create(FeatureDetector.AKAZE);
DescriptorExtractordescriptorExtractor=DescriptorExtractor.create(DescriptorExtractor.AKAZE);
DescriptorMatcherdescriptorMatcher=DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

//关键点检测
MatOfKeyPointsrcKeyPoint=newMatOfKeyPoint();
MatOfKeyPointdstKeyPoint=newMatOfKeyPoint();
detector.detect(srcMat,srcKeyPoint);
detector.detect(dstMat,dstKeyPoint);

//描述子生成
MatsrcDescriptor=newMat();
MatdstDescriptor=newMat();
descriptorExtractor.compute(srcMat,srcKeyPoint,srcDescriptor);
descriptorExtractor.compute(dstMat,dstKeyPoint,dstDescriptor);

Features2d.drawKeypoints(srcMat,srcKeyPoint,srcMat);
Features2d.drawKeypoints(dstMat,dstKeyPoint,dstMat);

//特征匹配检测
MatOfDMatchmatches=newMatOfDMatch();
descriptorMatcher.match(srcDescriptor,dstDescriptor,matches);

//对匹配结果进行筛选
List<DMatch>list=matches.toList();
List<DMatch>goodMatch=newArrayList<DMatch>();
for(inti=0;i<list.size();i++){
DMatchdmatch=list.get(i);
if(Math.abs(dmatch.queryIdx-dmatch.trainIdx)<7f){
goodMatch.add(dmatch);
}

}
matches.fromList(goodMatch);

MatresultMat=newMat();
Features2d.drawMatches(srcMat,srcKeyPoint,dstMat,dstKeyPoint,matches,resultMat);
BitmapresultBitmap=Bitmap.createBitmap(resultMat.width(),resultMat.height(),Bitmap.Config.ARGB_8888);
Utils.matToBitmap(resultMat,resultBitmap);

//释放内存
srcKeyPoint.release();
dstKeyPoint.release();
srcDescriptor.release();
dstDescriptor.release();
matches.release();

returnresultBitmap;
}


2021.03.08
优化系统界面，适配手机状态栏，修改图标。
存在问题：闪光灯控件存在兼容性等问题，无法在识别时使用闪光灯；
https://blog.csdn.net/jingzz1/article/details/105787463


2021.03.11
添加宠物数据搜索功能，优化相机界面
计划：搜索功能与相机界面美化，可在相机右下角添加返回按钮，参考微信拍照界面。


2021.03.15
为系统添加位置功能与宠物行动轨迹记录；
https://blog.csdn.net/u014714188/article/details/98209772
private Integer petID;
private String petName;
private String petType;
private String petSex;
private Date petBirth;
private String petInfo;
private String petRegistLocation;
private String petHistLocation;
private Date petRegistTime;
private Date petUpdateTime;
private String petPicPath;

2021.03.16
存在问题：获取位置信息所使用的方法locationManager.getLastKnownLocation有时会返回null，无法获取当前位置信息。

解决方法：
经过调试发现，由于手机硬件、室内等原因，GPS定位时间较久，特别是在刚开启手机位置信息时。

定位存在三种Location Provider
network（网络定位，通常是利用手机基站和WIFI节点的地址来大致定位位置）、
passive（被动定位，当其他应用使用定位更新了定位信息，系统会保存下来）、
gps（GPS定位，手机内部GPS芯片利用卫星获得自己的位置信息）

解决方案选择遍历三种方法，取其中精确度较高者。

//获取LocationManager的实例对象
locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
//获取支持的provider列表
List<String> providers = locationManager.getProviders(true);
Location bestLocation = null;
//遍历provider列表
for (String provider : providers) {
    //通过getLastKnowLocation方法来获取
    Location l = locationManager.getLastKnownLocation(provider);
    if (l == null) {
         continue;
    }
    if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
         // Found best last known location
         bestLocation = l;
     }
}


参考方法1：
https://blog.csdn.net/u013334392/article/details/91971758?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-2.baidujs&dist_request_id=1328656.12883.16159057549867333&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-2.baidujs

参考方法2：
https://cloud.tencent.com/developer/article/1198192


2021.03.20
完善定位功能，在检测界面中添加从手机相册上传宠物图片进行检测识别的功能。

参考：
https://blog.csdn.net/dbzzcz/article/details/105517904
https://riptutorial.com/opencv/topic/6377/using-cascade-classifiers-in-java
https://blog.csdn.net/weixin_43742354/article/details/103850125


问题：
获取到相册图片路径后，使用bitmapfactory.decodefile返回值为null
解决：
https://blog.csdn.net/ErrorNam/article/details/104363700?utm_medium=distribute.pc_relevant_bbs_down.none-task-blog-baidujs-1.nonecase&depth_1-utm_source=distribute.pc_relevant_bbs_down.none-task-blog-baidujs-1.nonecase
