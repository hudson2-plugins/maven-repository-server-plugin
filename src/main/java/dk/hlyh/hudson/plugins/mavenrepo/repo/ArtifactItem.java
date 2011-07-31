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

import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import org.hudsonci.maven.model.MavenCoordinatesDTO;
import org.hudsonci.maven.plugin.builder.MavenBuilderService;
import org.hudsonci.maven.plugin.dependencymonitor.ArtifactsExtractor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 *
 * @author henrik
 */
public class ArtifactItem extends Element {

    private static MavenBuilderService builderService;
    private static ArtifactsExtractor extractor;
    private final MavenCoordinatesDTO mavenCoordinates;
    private final Build build;

    public static void setBuilderService(MavenBuilderService value) {
        builderService = value;
    }

    public static void setExtractor(ArtifactsExtractor value) {
        extractor = value;
    }

    public ArtifactItem(String name, Directory parent, MavenCoordinatesDTO mavenCoordinates, Build build) {
        super(name, parent);
        this.mavenCoordinates = mavenCoordinates;
        this.build = build;
    }

    @Override
    public void sendResponse(StaplerRequest req, StaplerResponse rsp) throws IOException {
        InputStream is = null;
        //value
        FreeStyleBuild freeStyleBuild = build.getFreeStyleBuild();
        FilePath basePath = new FilePath(freeStyleBuild.getArtifactsDir());
        FilePath target = basePath.child(mavenCoordinates.getGroupId()).child(mavenCoordinates.getArtifactId()).child(mavenCoordinates.getVersion()).child(getName());
        String contentType = "application/octet-stream";
        
        if (mavenCoordinates.getType().equals("pom")) {
            contentType = "text/xml";
        }
        if (mavenCoordinates.getType().equals("jar") || mavenCoordinates.getType().equals("war") || mavenCoordinates.getType().equals("ear")) {
            contentType = "application/java-archive";
        }
        if (mavenCoordinates.getType().equals("zip")) {
            contentType = "application/x-zip-compressed";
        }
        
        try {
            if (target.exists()) {
                rsp.setContentType(contentType);
                target.copyTo(rsp.getOutputStream());
            } else {
                rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (InterruptedException ex) {
            System.out.println("interrupted while copying");
        }
    }
}
