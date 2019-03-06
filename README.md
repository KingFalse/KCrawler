# KCrawler
KCrawler-开源云爬虫

[测试机器：http://140.143.139.131:8080/](http://140.143.139.131:8080/)

## 为什么搞这个？
15-16年的时候写过一个exe的可视化爬虫[https://www.bilibili.com/video/av8405354](https://www.bilibili.com/video/av8405354)，最近看见了造数。然后我就把我原来写的也搬到了网页上。

## 视频教程：
* [https://www.bilibili.com/video/av37748401/](https://www.bilibili.com/video/av37748401/)
* 涉及链接：https://www.jb51.net/list/list_3_1.htm

## 借物表：
* success.html页面的猫：[https://blog.csdn.net/qq_32584661/article/details/55260288](https://blog.csdn.net/qq_32584661/article/details/55260288)
* loading.html页面动画：[https://www.html5tricks.com/svg-spiral-loading.html](https://www.html5tricks.com/svg-spiral-loading.html)
* 目前想到的就这些

## 适用性：
* 适用于各种伪静态页面。
* 动态页面暂不支持，可搭配WebDriver实现。

## 如何运行：
1. mvn clean package
2. java -jar KCrawler.jar
3. 浏览器访问：http://127.0.0.1:8080
