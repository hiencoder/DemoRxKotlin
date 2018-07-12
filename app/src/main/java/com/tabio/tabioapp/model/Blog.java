package com.tabio.tabioapp.model;

import android.os.Bundle;
import android.text.format.DateFormat;

import com.tabio.tabioapp.topics.Topic;
import com.tabio.tabioapp.util.DateUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.Response;

import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/7/16.
 */
public class Blog extends AppModel implements Serializable {
    public static final String TAG = makeLogTag(Blog.class);

    private String date;
    private String title;
    private String link;
    private String description;
//    private String content;


    public Blog() {
    }

    public static List<Blog> getBlogs(Response response) throws Exception {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            XmlPullParser xpp = factory.newPullParser();
            InputStream is = response.body().byteStream();
            xpp.setInput(is, "UTF-8");
            int eventType = xpp.getEventType();
            boolean inItem = false;
            Blog blog = new Blog();
            String tag = "";

            List<Blog> blogs = new ArrayList<>();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                } else if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("item")) {
                        inItem = true;
                        blog = new Blog();
                    }
                    tag = xpp.getName();
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (xpp.getName().equals("item")) {
                        inItem = false;
                        blogs.add(blog);

                    }
                } else if (eventType == XmlPullParser.TEXT) {
                    if (inItem) {
                        if (tag.equals("dc:date")) {
                            blog.setDate(xpp.getText());
                        }
                        if (tag.equals("title")) {
                            blog.setTitle(xpp.getText());
                        }
                        if (tag.equals("link")) {
                            blog.setLink(xpp.getText());
                        }
                        if (tag.equals("description")) {
                            blog.setDescription(xpp.getText());
                        }
                        tag = "";
                    }
                }
                eventType = xpp.next();
            }
            return blogs;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    public String getDate() {
        return date;
    }

    public String getDateForDisplay(String lang) {
        return DateUtils.getDateFromFormat("yyyy-MM-dd'T'HH:mm:ssz", getDate(), lang);
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
