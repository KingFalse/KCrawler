package me.kagura.kcrawler.controller;

import me.kagura.JJsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    JJsoup jJsoup;


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

        System.err.println(map);

        for (Element img : imgs) {
            //判断是否包含src属性
//            String src = img.attr("src");
//            if (img.hasAttr("src") && map.containsKey(src) && map.get(src) <= 1) {
//                continue;
//            }
            Attributes attributes = img.attributes();
            for (Attribute attribute : attributes) {
                if (attribute.getKey().equals("src")) {
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


    @GetMapping("/index/index")
    public String index() {
        return "/index22";
    }

    @GetMapping("/select")
    public String select(
            Model model
    ) throws IOException {
//        String url = "http://sports.sohu.com/s2015/chuangye/";
//        String url = "http://sports.qq.com/articleList/rolls/";
//        String url = "https://news.qq.com";
//        String url = "https://www.kagura.me";
        String url = "https://www.ithome.com";
//        String url = "https://anqing.meituan.com";
//        String url = "https://www.oschina.net";
        Document document = jJsoup.connect(url).get();
        convertToAbsUrlDocument(document);
        lazyloadImage(document);
        document.select("script").remove();
        model.addAttribute("srcdoc", document.html());
        model.addAttribute("url", url);
        return "select";
    }

    @PostMapping("/select/detail")
    public String selectDetail(
            Model model,
            @RequestParam String targetSelector,
            @RequestParam String pageSelector,
            @RequestParam String sampleUrl
    ) throws IOException {
        String url = "https://www.ithome.com/0/395/763.htm";
        Document document = jJsoup.connect(url).get();
        convertToAbsUrlDocument(document);
        lazyloadImage(document);
        document.select("script").remove();
        model.addAttribute("srcdoc", document.html());
        model.addAttribute("url", url);
        return "select";
    }

    @GetMapping("/select/detail")
    public String selectDetail2(
            Model model
    ) throws IOException {
        String url = "https://www.ithome.com/0/395/763.htm";
        Document document = jJsoup.connect(url).get();
        convertToAbsUrlDocument(document);
        lazyloadImage(document);
        document.select("script").remove();
        model.addAttribute("srcdoc", document.html());
        model.addAttribute("url", url);
        return "select-detail";
    }

}
