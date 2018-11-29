package me.kagura.kcrawler.service;

import me.kagura.JJsoup;
import me.kagura.kcrawler.entity.CrawlerTask;
import org.jsoup.Connection;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class CrawlerService {

    @Autowired
    JJsoup jJsoup;

    public void doCrawler(CrawlerTask task) {
        //用于存储需要爬取的url
        Set<String> listTargetUrls = Collections.synchronizedSet(new TreeSet<>());
        try {
            Connection.Response response = jJsoup.connect(task.getStartUrl()).method(Connection.Method.GET).execute();
            Document document = response.parse();

            if (StringUtil.isBlank(task.getTargetSelector())) {
                //无链接选择器
                listTargetUrls.add(task.getStartUrl());
            } else {
                //有链接选择器
                Elements targetHref = document.select(task.getTargetSelector());
                targetHref.forEach(target -> listTargetUrls.add(target.attr("abs:href")));
            }

            Set<String> listPageUrls = new TreeSet<>();

            boolean onePage = StringUtil.isBlank(task.getPageSelector());
            if (!onePage) {
                document.select(task.getPageSelector()).forEach(e -> listPageUrls.add(e.attr("abs:href")));
                listPageUrls.remove(task.getStartUrl());
            }


            for (int i = 0; i < (onePage ? 1 : task.getTargetPageCount() - 1); i++) {
                if (i >= listPageUrls.size()) {
                    break;
                }
                if (onePage) {
                    document.select(task.getTargetSelector()).forEach(e -> listTargetUrls.add(e.attr("abs:href")));
                } else {
                    if (i == 0) {
                        document.select(task.getTargetSelector()).forEach(e -> listTargetUrls.add(e.attr("abs:href")));
                    }
                    document = jJsoup.connect((String) listPageUrls.toArray()[i]).method(Connection.Method.GET).execute().parse();
                    document.select(task.getTargetSelector()).forEach(e -> listTargetUrls.add(e.attr("abs:href")));
                    document.select(task.getPageSelector()).forEach(e -> listPageUrls.add(e.attr("abs:href")));
                }
            }


            listPageUrls.forEach(url-> System.err.println(url));
            System.err.println("==========================");
            System.err.println(listTargetUrls.size());
            listTargetUrls.forEach(url-> System.err.println(url));


        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("爬取出错");
        }


    }

}
