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
import java.util.concurrent.*;

@Service
public class CrawlerService {

    @Autowired
    JJsoup jJsoup;

    public void doCrawler(CrawlerTask task) {
        //用于存储需要爬取的url
        List<String> listTargetUrls = getTargetUrls(task);

    }

    public List<String> doCrawler(CrawlerTask task, List<String> listTargetUrls) {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<String[]>[] futures = new Future[listTargetUrls.size()];
        for (int i = 0; i < listTargetUrls.size(); i++) {
            String url = listTargetUrls.get(i);
            futures[i] = executorService.submit(new Callable<String[]>() {
                @Override
                public String[] call() throws Exception {
                    List<String> result = new ArrayList<>();
                    Connection.Response response = jJsoup.connect(url).method(Connection.Method.GET).execute();
                    Document document = response.parse();
                    String title = document.title();
                    result.add(title);
                    result.add(url);
                    task.getTargetNodes().forEach((k, v) -> {
                        Elements select = document.select(k);
                        for (String type : v) {
                            switch (type) {
                                case "code":
                                    result.add(select.html());
                                    break;
                                case "text":
                                    result.add(select.text());
                                    break;
                            }
                        }
                    });

                    String[] resultArray = new String[result.size()];
                    result.toArray(resultArray);
                    return resultArray;
                }
            });
        }
        executorService.shutdown();

        for (Future<String[]> future : futures) {
            try {
                String[] vals = future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }


        return null;

    }

    public List<String> getTargetUrls(CrawlerTask task) {
        //用于存储需要爬取的url
        List<String> listTargetUrls = Collections.synchronizedList(new ArrayList<>());
        try {
            Connection.Response response = jJsoup.connect(task.getStartUrl()).method(Connection.Method.GET).execute();
            Document document = response.parse();

            if (StringUtil.isBlank(task.getTargetSelector())) {
                //无链接选择器
                listTargetUrls.add(task.getStartUrl());
            } else {
                //有链接选择器
                document.select(task.getTargetSelector()).forEach(target -> listTargetUrls.add(target.attr("abs:href")));
            }

            List<String> listPageUrls = Collections.synchronizedList(new ArrayList<>());
            listPageUrls.add(task.getStartUrl().replaceAll("#$", ""));

            boolean onePage = StringUtil.isBlank(task.getPageSelector());

            //计算循环总次数，i<0无限循环，i=0循环一次，i>0则循环i次
            int loopCount = (onePage ? 1 : (task.getTargetPageCount() < 0 ? Integer.MAX_VALUE : task.getTargetPageCount()) - 1);
            for (int i = 0; i < loopCount; i++) {
                document.select(task.getPageSelector()).forEach(e -> {
                    String absurl = e.attr("abs:href").replaceAll("#$", "");
                    if (!listPageUrls.contains(absurl)) {
                        listPageUrls.add(absurl);
                    }
                });
                document.select(task.getTargetSelector()).forEach(e -> listTargetUrls.add(e.attr("abs:href")));

                if (i >= listPageUrls.size() - 1) {
                    break;
                }
                document = jJsoup.connect(listPageUrls.get(i + 1)).method(Connection.Method.GET).execute().parse();
            }
            listPageUrls.forEach(url -> System.err.println(url));
            System.err.println("==========================");
            System.err.println(listPageUrls.size());
            System.err.println(listTargetUrls.size());

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("爬取出错");
        }
        return listTargetUrls;
    }

}
