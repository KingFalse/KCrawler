package me.kagura.kcrawler.service;

import me.kagura.JJsoup;
import me.kagura.kcrawler.common.KCUtil;
import me.kagura.kcrawler.entity.CrawlerTask;
import me.kagura.util.JJsoupUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jsoup.Connection;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@Service
public class KCrawlerService {

    private static Logger logger = LoggerFactory.getLogger(KCrawlerService.class);

    @Autowired
    JJsoup jJsoup;
    @Value("${crawler.result.xls.dir}")
    String dir;

    @Async
    public void doCrawler(CrawlerTask task) {
        logger.info("开始爬取任务：{}", task.getTraceId());
        //用于存储需要爬取的url
        List<String> listTargetUrls = getTargetUrls(task);
        doCrawler(task, listTargetUrls);
        logger.info("结束爬取任务：{}", task.getTraceId());
    }

    /**
     * 执行爬取跟保存
     *
     * @param task
     * @param listTargetUrls
     */
    public void doCrawler(CrawlerTask task, List<String> listTargetUrls) {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        Future<String[]>[] futures = new Future[listTargetUrls.size()];
        for (int i = 0; i < listTargetUrls.size(); i++) {
            String url = listTargetUrls.get(i);
            futures[i] = executorService.submit(new Callable<String[]>() {
                @Override
                public String[] call() throws Exception {
                    List<String> result = new ArrayList<>();
                    logger.info("任务：{},开始获取页面：{}", task.getTraceId(), url);
                    Connection.Response response = jJsoup.connect(url).method(Connection.Method.GET).execute();
                    logger.info("任务：{},结束获取页面：{},返回状态码：{}", task.getTraceId(), url, response.statusCode());
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

        logger.info("开始写入xls：{}", task.getTraceId());
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("KCrawler爬取结果");
        for (int i = 0; i < futures.length; i++) {
            try {
                String[] vals = futures[i].get();
                Row row = sheet.createRow(i);
                for (int i1 = 0; i1 < vals.length; i1++) {
                    String val = vals[i1]
                            .replaceAll("\n", "")
                            .replaceAll("\r", "")
                            .replaceAll("\t", "");
                    row.createCell(i1).setCellValue(val);
                }
            } catch (InterruptedException e) {
                logger.error("写入xls异常：{}", task.getTraceId(), e);
            } catch (ExecutionException e) {
                logger.error("写入xls异常：{}", task.getTraceId(), e);
            }
        }
        logger.info("结束写入xls：{}", task.getTraceId());

        try {
            logger.info("开始输出xls到文件：{}", task.getTraceId());
            FileOutputStream out = new FileOutputStream(dir + "/" + task.getTraceId() + ".xls");
            wb.write(out);
            out.close();
            logger.info("完成输出xls到文件：{}", task.getTraceId());
        } catch (FileNotFoundException e) {
            logger.error("输出xls到文件异常：{}", task.getTraceId(), e);
        } catch (IOException e) {
            logger.error("输出xls到文件异常：{}", task.getTraceId(), e);
        }

    }

    /**
     * 便利翻页获取所有需要爬取的链接
     *
     * @param task
     * @return
     */
    public List<String> getTargetUrls(CrawlerTask task) {
        logger.info("开始获取任务：{}的所有翻页", task.getTraceId());
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

        } catch (IOException e) {
            logger.error("任务：{}处理翻页异常", task.getTraceId(), e);
            e.printStackTrace();
        }
        logger.info("结束获取任务：{}的所有翻页,总链接数：{}", task.getTraceId(), listTargetUrls.size());
        return listTargetUrls;
    }

    /**
     * 获取指定任务是否爬取完成
     *
     * @param traceId
     * @return
     */
    public Map<String, Object> getStatus(String traceId) {
        Map<String, Object> map = new HashMap<String, Object>() {{
            put("status", false);
        }};
        File target = new File(dir + "/" + traceId + ".xls");
        if (target.exists()) {
            map.put("status", true);
            return map;
        }
        return map;
    }

    /**
     * 获取处理好的页面
     *
     * @param url
     * @return
     * @throws IOException
     */
    public Document getDoument(String url) throws IOException {
        Document document = jJsoup.connect(url).get();
        JJsoupUtil.convertToAbsUrlDocument(document);
        KCUtil.lazyloadImage(document);
        return document;
    }

}
