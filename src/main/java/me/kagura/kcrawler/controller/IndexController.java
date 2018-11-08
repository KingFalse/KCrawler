package me.kagura.kcrawler.controller;

import me.kagura.JJsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

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

    @GetMapping("/index/index")
    public String index() {
        return "/index22";
    }

    @GetMapping("/select")
    public String getMessage(Model model) throws IOException {
//        String url = "http://sports.sohu.com/s2015/chuangye/";
//        String url = "http://sports.qq.com/articleList/rolls/";
//        String url = "https://news.qq.com";
        String url = "https://www.kagura.me";
//        String url = "https://www.ithome.com";
//        String url = "https://anqing.meituan.com";
//        String url = "https://www.oschina.net";
        Document document = jJsoup.connect(url).get();
        document = convertToAbsUrlDocument(document);
        document.select("script").remove();
        model.addAttribute("srcdoc", document.html());
        model.addAttribute("url", url);
        return "select";
    }

}
