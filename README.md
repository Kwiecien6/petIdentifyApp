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
暂借opencv自带的人脸模型文件，实现目标物体检测、识别、存储（物体识别与匹配算法准确度较低，待改进。数据目前存储在本地SQLite）


2020.12.2
添加约束使宠物昵称唯一；
增加注册时的昵称校验；
优化UI与软件图标，宠物档案使用CardView；

待优化：
识别界面待改进，可增加开始检测按钮，避免以刚摄像头刚启动时传送模糊图像message。
身份验证成功后信息详情界面未写；
识别匹配算法待改进；
