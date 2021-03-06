package crawling;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import shared.CalendarUtility;
import shared.Feed;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by SAEED on 2016-01-07
 * for project BusinessSearchEngine .
 */

/**
 * this class has been write to
 * crawl Aftabir's business feed
 * url is: http://www.aftabir.com/advertising/category/82/%D8%A7%D8%B3%D8%AA%D8%AE%D8%AF%D8%A7%D9%85
 * seed url : http://www.aftabir.com/advertising/job.php
 */
public class AftabirCrawler extends WebCrawler {
    Pattern filter1 = Pattern.compile
            ("http://www\\.aftabir\\.com/advertising/referrer\\.php\\?id=[\\d]+");

    Pattern filter2 = Pattern.compile
            ("http://www\\.aftabir\\.com/advertising/view/[\\d]+/.+");
    private ArrayList<Feed> feeds = new ArrayList<Feed>();

    @Override
    public boolean shouldVisit(Page page, WebURL url)  {
        try {
            String href = url.getURL().toLowerCase();
            String decodedString = URLDecoder.decode(href, "UTF8");
            return filter1.matcher(decodedString).matches() ||
                    filter2.matcher(decodedString).matches();
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            return false;
        }

    }

    @Override
    public void visit(Page page) {

        if (page.getWebURL().getURL().equals("http://www.aftabir.com/advertising/job.php"))
            return;

        else if (filter1.matcher(page.getWebURL().getURL()).matches()) {
            return;
        }

        else {
            Document doc = Jsoup.parse(((HtmlParseData) page.getParseData()).getHtml());
            Elements elements = doc.select
                    (".ad-body p:nth-child(4) , .pics+ div , .text-info+ span , h1 , .spacer+ div");
            if (elements.size() == 5) // the feed has picture so we must remove it from elements
                elements.remove(2); // remove picture elements
            String title = elements.get(0).text();
            String date = elements.get(1).text();
            String body = elements.get(2).text();
            String city = elements.get(3).text();

            // date normalization
            date = date.split(" : ")[1]; // آخرین بروزرسانی : شنبه ۲۸ آذر ۱۳۹۴
            // بعضی جاها تاریخ اینجوریه :
            // آخرین بروزرسانی : سه شنبه ۲۸ آذر ۱۳۹۴
            // همین باعث مشکل شده بود محبور شدیم تاریخو از آخر بخونیم بیایم اول

            final int lastTokenIndex = date.split(" ").length - 1;
            date = date.split(" ")[lastTokenIndex] + "/" +
                    CalendarUtility.getNumericMonth(date.split(" ")[lastTokenIndex-1]) + "/" +
                    (date.split(" ")[lastTokenIndex-2].length() == 1? "0" : "") +
                    date.split(" ")[lastTokenIndex-2];
            date = CalendarUtility.getEnglishDate(date);

            // city normalization
            city = city.split("،")[1]; // استان تهران، تهران,...


            try {
                feeds.add(new Feed(
                    title,
                    body,
                    city,
                    URLDecoder.decode(page.getWebURL().toString(), "UTF8"),
                    date
                ));
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onBeforeExit() {
        //ExcelUtility.writeToExcel(feeds, Params.SHEET_AFTABIR);
    }
}
