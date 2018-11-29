package me.kagura.kcrawler.service;

import me.kagura.kcrawler.entity.CrawlerTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CrawlerServiceTest {

    @Autowired
    CrawlerService crawlerService;

    @Test
    public void doCrawler() {

        CrawlerTask crawlerTask = new CrawlerTask();
        crawlerTask.setStartUrl("https://www.kagura.me/");
        crawlerTask.setPageSelector("li.next-page a");
        crawlerTask.setTargetSelector("article header h2 a");
        crawlerTask.setTargetPageCount(3);

        crawlerService.doCrawler(crawlerTask);
    }
}