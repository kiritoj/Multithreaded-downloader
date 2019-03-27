# 一个简易的多线程下载器
**先上演示图片吧**\
![](https://github.com/kiritoj/Multithreaded-downloader/blob/master/pictures/picture1.gif)
![](https://github.com/kiritoj/Multithreaded-downloader/blob/master/pictures/picture2.png)\

**思路**\
首先获取到要下载的文件的长度，目前默认是开3个线程去下载，长度分为3分，每个线程各自下载一部分，下载过程用回调接口展示进度，完成后通过回调接口提示下载完成

**存在问题**\
下载完后偶尔会出现app卡住的现象，下载完成后会提示很多次下载成功。。。
