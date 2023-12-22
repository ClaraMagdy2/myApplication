package com.example.myapplication;
public class NewsArticle {

    // the properties/attributes
    // of the ArticleModel
    String author;
    String title;
    String description;
    String url;
    String urlToImage;
    String publishedAt;
    String content;

    // ArticleModel empty constructor
    public NewsArticle() {
    }

    // get method : returns the name of the author
    public String getAuthor() {
        return author;
    }

    // set method : sets the author
    public void setAuthor(String author) {
        this.author = author;
    }

    // get method : returns the title of the article
    public String getTitle() {
        return title;
    }

    // set method : sets the title
    public void setTitle(String title) {
        this.title = title;
    }

    // get method : returns the description of the article
    public String getDescription() {
        return description;
    }

    // set method : sets the description
    public void setDescription(String description) {
        this.description = description;
    }

    // get method : returns url of the article
    public String getUrl() {
        return url;
    }

    // set method : sets the url of the article
    public void setUrl(String url) {
        this.url = url;
    }

    // get method : returns the urlToImage of the article
    public String getUrlToImage() {
        return urlToImage;
    }

    // set method : sets the UrlToImage
    public void setUrlToImage(String urlToImage) {
        this.urlToImage = urlToImage;
    }

    // get method : returns the date
    public String getPublishedAt() {
        return publishedAt;
    }

    // set method : sets the date
    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    // get method : returns the content of the article
    public String getContent() {
        return content;
    }

    // set method : sets the title
    public void setContent(String content) {
        this.content = content;
    }
}
