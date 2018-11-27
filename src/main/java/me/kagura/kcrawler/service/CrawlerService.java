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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CrawlerService {

    @Autowired
    JJsoup jJsoup;

    public void doCrawler(CrawlerTask task) {
        //用于存储需要爬取的url
        List<String> listUrl = Collections.synchronizedList(new ArrayList<>());
        try {
            Connection.Response response = jJsoup.connect(task.getStartUrl()).method(Connection.Method.GET).execute();
            //判断是否有翻页
            if (StringUtil.isBlank(task.getPageSelector())) {
                //无分页
                if (StringUtil.isBlank(task.getTargetSelector())) {
                    //无链接选择器
                    listUrl.add(task.getStartUrl());
                } else {
                    //有链接选择器
                    Document document = response.parse();
                    Elements targetHref = document.select(task.getTargetSelector());
                    targetHref.forEach(target -> listUrl.add(target.attr("abs:href")));
                }
            } else {
                //有分页
                //执行单独方法先获取所有url添加到list
//                response.parse()
            }


        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("爬取出错");
        }


    }

}
