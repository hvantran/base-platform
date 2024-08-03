package com.hoatv.fwk.common.services;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.regex.Pattern;

public enum HtmlDocumentService {

    INSTANCE;

    public Document getRootDocument(String url) {
        if (url.startsWith("https")) {
            trustAllCertificates();
        }

        CheckedSupplier<Document> supplier = () -> Jsoup.connect(url).get();
        return supplier.get();
    }

    @SneakyThrows
    private void trustAllCertificates() {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
    
    public Elements selectXpath(Element document, String xpath) {
        return document.selectXpath(xpath);
    }

    public Element getSelectNode(Element document, String selector) {
        return document.selectFirst(selector);
    }

    public Element getSelectNode(String url, String selector) {
        return getRootDocument(url).selectFirst(selector);
    }

    public Elements getElementsByTag(Element element, String tagName) {
        return element.getElementsByTag(tagName);
    }

    public Element nextElementSibling(Element element) {
        return element.nextElementSibling();
    }

    public Element firstElementSibling(Element element) {
        return element.firstElementSibling();
    }

    public Element lastElementSibling(Element element) {
        return element.lastElementSibling();
    }

    public Elements getElementByClass(Element element, String className) {
        return element.getElementsByClass(className);
    }

    public Element getElementById(Element element, String id) {
        return element.getElementById(id);
    }

    public Elements getElementsByAttribute(Element element, String key) {
        return element.getElementsByAttribute(key);
    }

    public Elements getElementsByAttributeValue(Element element, String key, String value) {
        return element.getElementsByAttributeValue(key, value);
    }

    public Elements getElementsByAttributeValueNot(Element element, String key, String value) {
        return element.getElementsByAttributeValueNot(key, value);
    }

    public Elements getElementsByAttributeValueStarting(Element element, String key, String valuePrefix) {
        return element.getElementsByAttributeValueStarting(key, valuePrefix);
    }

    public Elements getElementsByAttributeValueEnding(Element element, String key, String valueSuffix) {
        return element.getElementsByAttributeValueEnding(key, valueSuffix);
    }

    public Elements getElementsByAttributeValueContaining(Element element, String key, String match) {
        return element.getElementsByAttributeValueContaining(key, match);
    }

    public Elements getElementsByAttributeValueMatching(Element element, String key, Pattern pattern) {
        return element.getElementsByAttributeValueMatching(key, pattern);
    }

    public Elements getElementsByAttributeValueMatching(Element element, String key, String regex) {
        return element.getElementsByAttributeValueMatching(key, regex);
    }

    public Elements getElementsContainingText(Element element, String searchText) {
        return element.getElementsContainingText(searchText);
    }

    public Elements getElementsContainingOwnText(Element element, String searchText) {
        return element.getElementsContainingOwnText(searchText);
    }

    public Elements getElementsMatchingText(Element element, Pattern pattern) {
        return element.getElementsMatchingText(pattern);
    }

    public Elements getElementsMatchingText(Element element, String searchText) {
        return element.getElementsMatchingText(searchText);
    }

}
