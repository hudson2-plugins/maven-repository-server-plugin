/*
 * The MIT License
 *
 * Copyright 2011 Hudson.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package dk.hlyh.hudson.plugins.mavenrepo.repo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 *
 * @author henrik
 */
public abstract class Directory extends Element {

    protected SortedMap<String, Element> children = new TreeMap<String, Element> ();

    protected Directory(String name, Directory parent) {
        super(name, parent);
    }

    @Override
    public void sendResponse(StaplerRequest req, StaplerResponse rsp) throws IOException {
        printHtmlHeader(req, rsp);
        printInformationArea(req, rsp);
        printDirectoryIndex(req, rsp);
        printHtmlFooter(req, rsp);
    }

    public Map<String, Element> getChildren() {
        if (children.isEmpty()) {
            loadChildren();
        }
        return children;
    }
    
    public Element getChild(String name) {
        if (children.isEmpty()) {
            loadChildren();
        }        
        return children.get(name);        
    }

    protected abstract void loadChildren();
    

    protected void printHtmlHeader(StaplerRequest req, StaplerResponse rsp) throws IOException {
        rsp.setContentType("text/html;charset=UTF-8");
        OutputStream os = rsp.getOutputStream();
        String title = "<html>\n"
                + "  <head>\n"
                + "    <title>Index of " + getPath() + "</title>\n"
                + "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>\n"
                + "  </head>\n"
                + "  <body>\n";
        os.write(title.getBytes("UTF-8"));
    }

    protected void printHtmlFooter(StaplerRequest req, StaplerResponse rsp) throws IOException {
        OutputStream os = rsp.getOutputStream();
        os.write("</body></html>".getBytes("UTF-8"));
    }

    
    protected void printInformationArea(StaplerRequest req, StaplerResponse rsp) throws IOException {
        OutputStream os = rsp.getOutputStream();
        os.write("<div>".getBytes("UTF-8"));
        printInformation(req, rsp);
        os.write("</div>".getBytes("UTF-8"));
    }

    protected void printInformation(StaplerRequest req, StaplerResponse rsp) throws IOException {
        /* empty in default implementation */
    }

    protected void printDirectoryIndex(StaplerRequest req, StaplerResponse rsp) throws IOException {
        OutputStream os = rsp.getOutputStream();
        
        String indexHeader = "<h1>Index of " + getPath() + "</h1>\n"
                + "    <table cellspacing=\"10\">\n"
                + "      <tr>\n"
                + "        <th align=\"left\">Name</th>\n"
                + "      </tr>";

        os.write(indexHeader.getBytes("UTF-8"));

        if (getParent() != null) {
            String parentText = "<tr><td>"+buildHref(req,getParent().getPath(),"Parent Directory")+"</td></tr>\n";
            os.write(parentText.getBytes("UTF-8"));
        }          
        for (String child : getChildren().keySet()) {
            String link = "<tr><td>" + buildHref(req, getPath() + "/" + child, child) + "</td></tr>\n";
            os.write(link.getBytes("UTF-8"));
        }
        os.write("</table>".getBytes("UTF-8"));       
    }

}
