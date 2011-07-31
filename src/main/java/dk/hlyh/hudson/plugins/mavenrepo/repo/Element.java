/*
 * The MIT License
 *
 * Copyright 2011 Henrik Lynggaard Hansen.
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

import hudson.model.Hudson;
import java.io.IOException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 *
 * @author henrik
 */
public abstract class Element {
    
    private static final String PLUGIN_URL="plugin/maven-repository-server";
    private String name;
    private Element parent;
        
    @Override
    public String toString() {
        return name;
    }
    
    protected Element(String name,Element parent) {
        this.name = name;        
        this.parent = parent;
    }

    protected String getName() {
        return name;
    }

    protected Element getParent() {
        return parent;
    }
            
    protected String getPath() {
        return parent != null ? parent.getPath() +"/"+ name : "";
    }    
    
    
    protected String buildHref(StaplerRequest req,String relativePath,String text) {
        String baseUrl = Hudson.getInstance().getRootUrl()+req.getContextPath()+PLUGIN_URL;
        return "<a href=\""+baseUrl + relativePath + "\">"+text+"</a>";
    }
    
    public abstract void sendResponse(StaplerRequest req, StaplerResponse rsp) throws IOException;
    
}
