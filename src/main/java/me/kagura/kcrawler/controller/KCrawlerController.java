package me.kagura.kcrawler.controller;

import com.alibaba.fastjson.JSON;
import me.kagura.JJsoup;
import me.kagura.kcrawler.entity.CrawlerTask;
import me.kagura.kcrawler.service.CrawlerService;
import org.apache.commons.io.FileUtils;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class KCrawlerController {

    @Autowired
    JJsoup jJsoup;
    @Autowired
    CrawlerService crawlerService;
    @Value("${crawler.result.xls.dir}")
    String dir;

    public static Document convertToAbsUrlDocument(Document document) {
        Validate.notEmpty(document.baseUri(), "document.baseUri() must not be empty");
        Elements relativePathElements = document.select("[src],[href]");
        for (Element element : relativePathElements) {
            if (element.hasAttr("href")) {
                String href = element.attr("href");
                if (!href.matches("^.*:[\\d\\D]*") && !href.equals("#")) {
                    element.attr("href", element.attr("abs:href"));
                }
            }
            if (element.hasAttr("src")) {
                String src = element.attr("src");
                if (!src.matches("^.*:[\\d\\D]*")) {
                    element.attr("src", element.attr("abs:src"));
                }

            }

        }
        return document;
    }

    /**
     * 1.选出所有的img
     * 2.判断是否有.jog结尾的属性，有则继续
     * 3.如果没有src属性则直接增加src，如果有src，则判断src是否重复出现，如果重复则替换src
     */
    public static Document lazyloadImage(Document document) {
        Map<String, Integer> map = new HashMap<>();
        Elements imgs = document.select("img");
        for (Element img : imgs) {
            if (img.hasAttr("src")) {
                String src = img.attr("src");
                if (map.containsKey(src)) {
                    map.put(src, map.get(src) + 1);
                } else {
                    map.put(src, 0);
                }
            }
        }
        for (Element img : imgs) {
            Attributes attributes = img.attributes();
            for (Attribute attribute : attributes) {
                if (attribute.getKey().equals("src") || attribute.getValue() == null) {
                    continue;
                }
                //判断该属性是否是图片链接
                if (attribute.getValue().matches("^[\\d\\D]*(\\.gif|\\.jpeg|\\.png|\\.jpg|\\.bmp)$")) {
                    img.attr("src", img.attr("abs:" + attribute.getKey()));
                    break;
                }
            }
        }
        return document;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("traceId", UUID.randomUUID().toString());
        return "index";
    }

    @GetMapping("/loading")
    public String loading(Model model, String traceId) {
        model.addAttribute("traceId", traceId);
        return "loading";
    }

    @GetMapping("/success")
    public String success(Model model, String traceId) {
        model.addAttribute("traceId", traceId);
        return "success";
    }

    @GetMapping("/select")
    public String select(Model model, String traceId, String url) throws IOException {
        Document document = jJsoup.connect(url).get();
        convertToAbsUrlDocument(document);
        lazyloadImage(document);
        document.select("script").remove();
        model.addAttribute("srcdoc", document.html());
        model.addAttribute("url", url);
        model.addAttribute("traceId", traceId);
        return "select";
    }

    @GetMapping("/status")
    @ResponseBody
    public ResponseEntity<Map> status(String traceId) {
        Map<String, Object> status = crawlerService.getStatus(traceId);
        return ResponseEntity.ok(status);
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

    @PostMapping("/select/detail")
    public String selectDetail(
            Model model,
            String targetSelector,
            String pageSelector,
            String sampleUrl,
            String mainUrl,
            String traceId
    ) throws IOException {
        Document document = jJsoup.connect(sampleUrl).get();
        convertToAbsUrlDocument(document);
        lazyloadImage(document);
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
        CrawlerTask task = JSON.parseObject(body, CrawlerTask.class);
        crawlerService.doCrawler(task);
        return "OK";
    }

}
