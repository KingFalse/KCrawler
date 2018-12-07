package me.kagura.kcrawler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;
import java.io.File;

@EnableAsync
@SpringBootApplication
public class KCrawler {

    @Value("${crawler.result.xls.dir}")
    String dir;

    public static void main(String[] args) {
        SpringApplication.run(KCrawler.class, args);
    }

    @PostConstruct
    public void mkdirs() {
        new File(dir).mkdirs();
    }

}