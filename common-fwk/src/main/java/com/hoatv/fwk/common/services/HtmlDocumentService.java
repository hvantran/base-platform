package com.hoatv.fwk.common.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public enum HtmlDocumentService {

    INSTANCE;

    public Document getRootDocument(String url) {
        CheckedSupplier<Document> supplier = () -> Jsoup.connect(url).get();
        return supplier.get();
    }

    public Elements getSelectNodes(Element document, String selector) {
        return document.select(selector);
    }

    public Elements getSelectNodes(String url, String selector) {
        return getRootDocument(url).select(selector);
    }

    public Element getSelectNode(Element document, String selector) {
        return document.selectFirst(selector);
    }

    public Element getSelectNode(String url, String selector) {
        return getRootDocument(url).selectFirst(selector);
    }
}
