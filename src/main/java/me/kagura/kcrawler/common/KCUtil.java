package me.kagura.kcrawler.common;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

public class KCUtil {
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

    /**
     * 半智障自动翻页算法
     *
     * @param document
     * @return
     */
    public static String getRetardedTurnSelector(Document document) {
        Elements select = document.select("a:matchesOwn(^\\d+$)");
        if (select.size() > 0) {
            return select.first().cssSelector().replaceAll(":nth-child\\(\\d+\\)", "");
        }
        select = document.select("a:containsOwn(下一页)");
        if (select.size() > 0) {
            return select.first().cssSelector().replaceAll(":nth-child\\(\\d+\\)", "");
        }
        select = document.select("a:containsOwn(下页)");
        if (select.size() > 0) {
            return select.first().cssSelector().replaceAll(":nth-child\\(\\d+\\)", "");
        }
        return "";
    }

}
