package web.crawler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.json.simple.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.PrintWriter;

/**
 *
 * @author bradley
 */
public class Page {
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.130 Safari/537.36";
    private List<String> links = new LinkedList<String>();
    private Document htmlDocument;
    private static int nId = 1;
    private static int nFolderIndex = 1;
    private String uri;
    private final String dir;
    JSONObject jsonContent = new JSONObject();
    
    public Page(String directory) {
        dir = directory;
    }
    
    public boolean crawl(String url) {
        try {
            Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
            Document htmlDoc = connection.get();
            this.htmlDocument = htmlDoc;
            if(connection.response().statusCode() == 200) {
                System.out.println("\n**Visiting** Received web page at " + url);
            }
            if(connection.response().contentType() == null || !connection.response().contentType().contains("text/html")) {
                System.out.println("**Failure** Retrieved something other than HTML");
                return false;
            }
            uri = url;
            return true;
        } catch(IOException ioe) {
            return false;
        }
    }
    
    public boolean getContent() throws IOException {
        if(this.htmlDocument == null) {
            System.out.println("ERROR! Call crawl() before performing analysis on the document");
            return false;
        }
        
        //Set node id and uri if htmlDocument isn't null
        jsonContent.put("nid", nId);
        jsonContent.put("uri", uri);
        
        Elements content = htmlDocument.select("td[width=540], td[width=510]");
        
        Elements titles = htmlDocument.select("h1");
        if (titles.size() > 0) {
            for(Element title: titles) {
                jsonContent.put("title", title.text());
            }
        } else {
            Elements titleTags = htmlDocument.select("title");
            for(Element tTag: titleTags) {
                jsonContent.put("title", tTag.text());
            }
        }
        
        String bodyContent = content.html();
        if (bodyContent.isEmpty()) {
            //selects body content if on SACOG external sites e.g. MTP/SCS and RUCS
            Elements mtpContent = htmlDocument.select("#ajax-content-wrap > .container-wrap > .main-content, #mainContentArea");
            jsonContent.put("body", mtpContent.html());
        } else {
            jsonContent.put("body", content.html());
        }
        
        System.out.println(toPrettyFormat(jsonContent.toJSONString()));
        
        this.createFile();
        nId++;
        if (nId%1000 == 0) {
            nFolderIndex++;
        }
        
        return true;
    }
    
    public static String toPrettyFormat(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }
    
    private boolean createFile() throws IOException {
        File file = new File(dir + "/node" + nFolderIndex + "/" + nId + ".json");
        if(file.getParentFile().exists()) {
            file.createNewFile();
        } else {
            if (file.getParentFile().mkdir()) {
                file.createNewFile();
            } else {
                throw new IOException("Failed to create directory " + file.getParent());
            }
        }
        PrintWriter writer = new PrintWriter(file);
        writer.println(toPrettyFormat(jsonContent.toJSONString()));
        writer.close();
        return true;
    }
    
    public List<String> getLinks() {
        return this.links;
    }
}
