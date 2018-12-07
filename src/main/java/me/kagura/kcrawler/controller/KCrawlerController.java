package me.kagura.kcrawler.controller;

import com.alibaba.fastjson.JSON;
import me.kagura.JJsoup;
import me.kagura.kcrawler.entity.CrawlerTask;
import me.kagura.kcrawler.service.KCrawlerService;
import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Controller
public class KCrawlerController {

    private static Logger logger = LoggerFactory.getLogger(KCrawlerController.class);

    @Autowired
    JJsoup jJsoup;
    @Autowired
    KCrawlerService crawlerService;
    @Value("${crawler.result.xls.dir}")
    String dir;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("traceId", UUID.randomUUID().toString());
        return "index";
    }


    @GetMapping("/select")
    public String select(Model model, String traceId, String url) throws IOException {
        Document document = crawlerService.getDoument(url);
        document.select("script").remove();
        model.addAttribute("srcdoc", document.html());
        model.addAttribute("url", url);
        model.addAttribute("traceId", traceId);
        return "select";
    }


    @PostMapping("/select/detail")
    public String selectDetail(
            Model model,
            String targetSelector,
            String pageSelector,
            String sampleUrl,
            String mainUrl,
            String traceId
    ) throws IOException {
        Document document = crawlerService.getDoument(sampleUrl);
        document.select("script").remove();
        model.addAttribute("srcdoc", document.html());
        model.addAttribute("sampleUrl", sampleUrl);
        model.addAttribute("mainUrl", mainUrl);
        model.addAttribute("targetSelector", targetSelector);
        model.addAttribute("pageSelector", pageSelector);
        model.addAttribute("traceId", traceId);
        return "select-detail";
    }


    @PostMapping("/docrawler")
    @ResponseBody
    public String doCrawler(@RequestBody String body) {
        logger.info("收到爬取任务JSON：{}", body);
        CrawlerTask task = JSON.parseObject(body, CrawlerTask.class);
        crawlerService.doCrawler(task);
        return "OK";
    }


    @GetMapping("/loading")
    public String loading(Model model, String traceId) {
        model.addAttribute("traceId", traceId);
        return "loading";
    }


    @GetMapping("/status")
    @ResponseBody
    public ResponseEntity<Map> status(String traceId) {
        Map<String, Object> status = crawlerService.getStatus(traceId);
        return ResponseEntity.ok(status);
    }


    @GetMapping("/success")
    public String success(Model model, String traceId) {
        model.addAttribute("traceId", traceId);
        return "success";
    }


    @RequestMapping("/download")
    public ResponseEntity<byte[]> download(String traceId) throws Exception {
        String filename = "KCrawler爬取结果-" + traceId + ".xls";
        File xls = new File(dir + "/" + traceId + ".xls");
        HttpHeaders headers = new HttpHeaders();
        //下载显示的文件名，解决中文名称乱码问题
        String downloadFielName = new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        //通知浏览器以attachment（下载方式）打开图片
        headers.setContentDispositionFormData("attachment", downloadFielName);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(FileUtils.readFileToByteArray(xls), headers, HttpStatus.CREATED);
    }

}
