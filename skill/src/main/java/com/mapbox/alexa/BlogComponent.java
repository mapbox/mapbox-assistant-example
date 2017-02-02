package com.mapbox.alexa;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Reads the Mapbox blog for the latest news
 */
public class BlogComponent {

    private final static String FEED_URL = "http://www.mapbox.com/blog/blog.rss";

    private SyndFeed feed;

    public BlogComponent() {
        feed = null;
    }

    public String lastWeek(boolean markdown) {
        StringBuilder sb = new StringBuilder();

        try {
            int total = 1; // or stories.size() for all
            ArrayList<SyndEntry> stories = getRecentEntries(7);
            sb.append(String.format("Over the last week we've published %d stories, our latest is that ", stories.size()));
            for (int i = 0; i < total; i++) {
                int days = getDaysFromPublication(stories.get(i).getPublishedDate());
                String readableDate = getReadableDate(days);
                String suffix = getSuffix(i, total);
                if (markdown) {
                    sb.append(String.format("%s published %s [%s](%s)%s",
                            stories.get(i).getAuthor(), readableDate, stories.get(i).getTitle(),
                            stories.get(i).getLink(), suffix));
                } else {
                    sb.append(String.format("%s published %s %s%s",
                            stories.get(i).getAuthor(), readableDate, stories.get(i).getTitle(),
                            suffix));
                }
            }
        } catch (IOException | FeedException e) {
            e.printStackTrace();
            return "Sorry, I couldn't check the Mapbox news now. Please try again in a few.";
        }

        return sb.toString();
    }

    /*
     * Internal utils
     */

    private ArrayList<SyndEntry> getRecentEntries(int daysSpan) throws IOException, FeedException {
        ArrayList<SyndEntry> entries = new ArrayList<>();

        SyndFeed feed = getFeed();
        for (SyndEntry entry : feed.getEntries()) {
            if (getDaysFromPublication(entry.getPublishedDate()) <= daysSpan) {
                entries.add(entry);
            } else {
                break;
            }
        }

        return entries;
    }

    private int getDaysFromPublication(Date date) {
        return Days.daysBetween(new DateTime(date), new DateTime()).getDays();
    }

    private String getReadableDate(int days) {
        if (days == 0) return "today";
        if (days == 1) return "yesterday";
        return String.format("%d days ago", days);
    }

    private String getSuffix(int index, int total) {
        if (index == total - 1) return ".";
        if (index == total - 2) return ", and ";
        return ", ";
    }

    private SyndFeed getFeed() throws IOException, FeedException {
        if (feed == null) {
            // Only download once per instance
            SyndFeedInput input = new SyndFeedInput();
            feed = input.build(new XmlReader(download()));
        }

        return feed;
    }

    private InputStream download() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(FEED_URL).build();
        Response response = client.newCall(request).execute();
        return response.body().byteStream();
    }
}