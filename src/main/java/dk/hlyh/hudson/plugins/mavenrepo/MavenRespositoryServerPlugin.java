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
package dk.hlyh.hudson.plugins.mavenrepo;

import hudson.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.hudsonci.maven.plugin.builder.MavenBuilderService;
import org.hudsonci.maven.plugin.dependencymonitor.ArtifactsExtractor;
import dk.hlyh.hudson.plugins.mavenrepo.repo.Build;
import dk.hlyh.hudson.plugins.mavenrepo.repo.Directory;
import dk.hlyh.hudson.plugins.mavenrepo.repo.Element;
import dk.hlyh.hudson.plugins.mavenrepo.repo.Root;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

@Named
@Singleton
public class MavenRespositoryServerPlugin extends Plugin {

    private static final Logger log = LoggerFactory.getLogger("maven-repository-server");
    private ArtifactsExtractor extractor;
    private static final LinkedHashMap<String, Build> innerCache = new LinkedHashMap<String, Build>(100) {
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > 100;
        }
    };
    
    public static final Map<String, Build> buildCache = Collections.synchronizedMap(innerCache);

    @Inject
    @Override
    public void start() {
    }

    @Inject
    public void setExtractor(ArtifactsExtractor extractor) {
        this.extractor = extractor;
    }


    @Override
    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        Build.setExtractor(extractor);
        String path = req.getRestOfPath();
        log.info("requested url: " + path);
        if (path.length() == 0) {
            path = "/";
        }

        if (path.indexOf("..") != -1 || path.length() < 1) {
            log.warn("path is invalid sending error: ");
            rsp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Element currentItem = new Root();

        // Split into sections
        String[] pathElements = path.substring(1).split("/");
        try {
            // Ignore breakdown case if '/'
            if (pathElements.length > 1 || pathElements[0].length() > 0) {
                for (String element : pathElements) {
                    log.debug("path element: " + element);
                    if (currentItem instanceof Directory) {
                        Directory currentDirectory = (Directory) currentItem;
                        currentItem = currentDirectory.getChild(element);
                        log.debug("changing current item from " + currentDirectory + " to " + currentItem);
                    }

                }
            }
            if (currentItem != null) {
                log.info("Sending back response: " + currentItem);
                currentItem.sendResponse(req, rsp);
            } else {
                log.warn("did not find item, sending 404 ");
                rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } catch (Exception ex) {
            log.warn("System error: ", ex);
            rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            super.doDynamic(req, rsp);
        }
    }
}
