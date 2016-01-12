package crawling;

import com.coremedia.iso.Utf8;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import shared.Feed;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by Mostafa on 1/11/2016.
 */

/**
 * this class has been write to
 * crawl mazandkar's business feed
 * url is: http://mazandkar.ir/
 */
public class MazandkarCrawler extends WebCrawler{

    private final String DATE_SPLITER = "-";
    private final String MazanKarDateFormat = "yyyy/mm/dd";
    private final ArrayList<Feed> feeds = new ArrayList<Feed>();

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        try {
            String href = url.getURL().toLowerCase();
            String decodeString = URLDecoder.decode(href, "UTF8");
            return decodeString.startsWith("http://mazandkar.ir/index.php/user/viewj/") ||
            decodeString.matches("http://mazandkar\\.ir/index\\.php/site/index?Addjob_page=[\\d]+&ajax=yw0&language=fa");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void visit(Page page) {
        try {
            if (URLDecoder.decode(page.getWebURL().getURL(), "UTF8").toLowerCase().equals("http://mazandkar.ir")){
                System.out.println("123");
                return;}
            if (page.getWebURL().getURL().toLowerCase().matches("http://mazandkar\\.ir/index\\.php/site/index?Addjob_page=[\\d]+&ajax=yw0&language=fa")){
                System.out.println("");
                return;}
            else {
                Document doc = Jsoup.parse(((HtmlParseData) page.getParseData()).getHtml());
                Elements elements = doc.select(".odd:nth-child(13) td , .odd:nth-child(9) td , .even:nth-child(2) td , .odd:nth-child(1) td");

                //title
                String title = elements.get(0).text().trim();
                elements.remove(elements.get(0));

                //date
                String[] date = elements.get(elements.size()-1).text().split(DATE_SPLITER);
                elements.remove(elements.get(elements.size()-1));

                //body
                String body = elements.get(0).text().trim();
                elements.remove(elements.get(0));

                //city
                String city = elements.get(0).text().trim();

                try {
                    Feed feed = new Feed(
                            title,
                            body,
                            city,
                            page.getWebURL().toString(),
                            date[2]+"/"+date[1]+"/"+date[0],
                            MazanKarDateFormat
                    );
                    feeds.add(feed);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
