/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.crawler;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author bradley
 */
public class WebCrawler {
    //Sets allow for no duplicate websites reducing the size of the array
    private final Set<String> pagesVisited = new HashSet<String>();
    private List<String> pagesToVisit = new LinkedList<String>();
    private String directory;
    private String basePrefix;
    
    public WebCrawler (String dir) {
        directory = dir;
    }
    
    public void search(String url, String domain) throws IOException {
        basePrefix = domain;
        this.pagesToVisit.add(url);
        while(!(this.pagesToVisit.isEmpty())) {
            String currentUrl;
            Page page = new Page(directory);
            currentUrl = this.nextUrl();
            if(page.crawl(currentUrl)) {
                page.getContent();
            }
            
            this.pagesToVisit.addAll(page.getLinks());
        }
        JFrame prompt = new JFrame();
        JOptionPane.showMessageDialog(prompt,
            "Web Crawl Completed\nExported " + this.pagesVisited.size() + " web page(s).",
            "Web Crawler",
            JOptionPane.PLAIN_MESSAGE);
        System.out.println("\n*******DONE******** Visited " + this.pagesVisited.size() + " web page(s).");
    }
    
    private String nextUrl() {
        String nextUrl;
        do {
            nextUrl = this.pagesToVisit.remove(0);
        } while (this.pagesVisited.contains(nextUrl) || !(nextUrl.toLowerCase().contains(basePrefix.toLowerCase())));
        this.pagesVisited.add(nextUrl);
        return nextUrl;
    }
    
    public static void main(String[] args) {
        // Creating Window Frame for selecting directory
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("Web Crawler");
        frame.setLayout(new FlowLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JButton button = new JButton("Select Directory");
        button.addActionListener((ActionEvent ae) -> {
            //Opens file exporer/finder to select directory
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            chooser.setDialogTitle("select folder");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            int returnValue = chooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                //After selected do this...
                String chosenDirectory = chooser.getSelectedFile().getPath();
                WebCrawler crawler = new WebCrawler(chosenDirectory);
                try {
                    crawler.search("http://www.sacog.org/", "sacog.org");
                } catch (IOException ex) {
                    Logger.getLogger(WebCrawler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        frame.add(button);
        frame.pack();
        frame.setVisible(true);
    }
    
}
