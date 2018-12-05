package me.kagura.kcrawler.service;

import com.alibaba.fastjson.JSON;
import me.kagura.kcrawler.entity.CrawlerTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CrawlerServiceTest {

    @Autowired
    CrawlerService crawlerService;

    @Test
    public void doCrawler() {


        String json = "{\"targetSelector\":\"\",\"pageSelector\":\"\",\"targetPageType\":\"0\",\"targetPageCount\":\"1\",\"startUrl\":\"https://www.jb51.net/softs/62027.html\",\"targetNodes\":{\"div#page div#mainBody dl#param dt#main h1\":[\"text\",\"code\"],\"div#page div#mainBody div.wrap970.bgf.clearfix div.w750.fl dl#content dd#soft-info div.main-info div#soft-intro p\":[\"text\",\"code\"]}}\n";

        CrawlerTask crawlerTask = JSON.parseObject(json,CrawlerTask.class);

//        CrawlerTask crawlerTask = new CrawlerTask();
//        crawlerTask.setStartUrl("https://www.kagura.me/");
//        crawlerTask.setPageSelector("li.next-page a");
//        crawlerTask.setTargetSelector("article header h2 a");
//        crawlerTask.setTargetPageCount(3);

        crawlerTask.setStartUrl("https://www.jb51.net/softs/list38_1.html");
        crawlerTask.setPageSelector(".plist a");
        crawlerTask.setTargetSelector(".top-tit .tit a");
//        crawlerTask.setTargetPageCount(-1);//无限翻页
//        crawlerTask.setTargetPageCount(0);//不翻页
        crawlerTask.setTargetPageCount(3);//指定页数

        crawlerService.doCrawler(crawlerTask);
    }
}