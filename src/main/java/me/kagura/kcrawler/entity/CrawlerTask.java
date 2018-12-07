package me.kagura.kcrawler.entity;

import java.util.Map;

public class CrawlerTask {

    private String traceId;
    private String targetSelector;
    private String pageSelector;
    private String targetPageType;
    private int targetPageCount;
    private String startUrl;
    private Map<String, String[]> targetNodes;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Map<String, String[]> getTargetNodes() {
        return targetNodes;
    }

    public void setTargetNodes(Map<String, String[]> targetNodes) {
        this.targetNodes = targetNodes;
    }

    public String getTargetSelector() {
        return targetSelector;
    }

    public void setTargetSelector(String targetSelector) {
        this.targetSelector = targetSelector;
    }

    public String getPageSelector() {
        return pageSelector;
    }

    public void setPageSelector(String pageSelector) {
        this.pageSelector = pageSelector;
    }

    public String getTargetPageType() {
        return targetPageType;
    }

    public void setTargetPageType(String targetPageType) {
        this.targetPageType = targetPageType;
    }

    public int getTargetPageCount() {
        return targetPageCount;
    }

    public void setTargetPageCount(int targetPageCount) {
        this.targetPageCount = targetPageCount;
    }

    public String getStartUrl() {
        return startUrl;
    }

    public void setStartUrl(String startUrl) {
        this.startUrl = startUrl;
    }

    @Override
    public String toString() {
        return "CrawlerTask{" +
                "traceId='" + traceId + '\'' +
                ", targetSelector='" + targetSelector + '\'' +
                ", pageSelector='" + pageSelector + '\'' +
                ", targetPageType='" + targetPageType + '\'' +
                ", targetPageCount=" + targetPageCount +
                ", startUrl='" + startUrl + '\'' +
                ", targetNodes=" + targetNodes +
                '}';
    }
}