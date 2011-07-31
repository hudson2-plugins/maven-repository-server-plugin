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
import java.util.Collection;
import org.hudsonci.maven.model.MavenCoordinatesDTO;
import org.hudsonci.maven.plugin.dependencymonitor.ArtifactsExtractor;
import dk.hlyh.hudson.plugins.mavenrepo.MavenRespositoryServerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;    
/**
 *
 * @author henrik
 */
public class Build extends Directory {

    private static final Logger log = LoggerFactory.getLogger("maven-repository-server");
    
    private FreeStyleBuild freeStyleBuild;
    private static ArtifactsExtractor extractor;

    Build(FreeStyleBuild freeStyleBuild, Job parent) {
        super("" + freeStyleBuild.getNumber(), parent);
        this.freeStyleBuild = freeStyleBuild;
    }

    public static void setExtractor(ArtifactsExtractor value) {
        extractor = value;
    }

    public FreeStyleBuild getFreeStyleBuild() {
        return freeStyleBuild;
    }

    @Override
    protected void loadChildren() {
        log.debug("loading children for "+freeStyleBuild.getFullDisplayName());
        Collection<MavenCoordinatesDTO> producedArtifacts = extractor.extract(freeStyleBuild).produced;
        
        NamedDirectory repoDir = new NamedDirectory("repository", this);
        this.children.put("repository", repoDir);
        for (MavenCoordinatesDTO artifact : producedArtifacts) {
            try {
                String groupId = artifact.getGroupId();
                String artifactId = artifact.getArtifactId();
                String version = artifact.getVersion();
                String type = artifact.getType();
                String prefixedClassifier = (artifact.getClassifier() != null && artifact.getClassifier().trim().length() > 0) ? "-" + artifact.getClassifier() : "";
                
                String[] splitGroupId = groupId.split("\\.");
                
                log.debug("found artifact: "+groupId+":"+artifactId+":"+version+":"+prefixedClassifier+":"+type);
                
                // convert group name to nested folders;
                Directory groupParent = repoDir;
                for (int i=0; i <splitGroupId.length; i++) {
                    String folderName = splitGroupId[i];
                    
                    Directory child = (Directory) groupParent.getChild(folderName);
                    if (child == null ) {
                        child = new ArtifactDirectory(folderName, artifact, groupParent);
                        groupParent.children.put(folderName, child);
                    }
                    groupParent = child;
                }
                
                //Handle the artifactID folder
                Directory artifactIdDirectory = (Directory) groupParent.getChild(artifactId);
                if (artifactIdDirectory == null) {
                      artifactIdDirectory = new ArtifactDirectory(artifactId, artifact, groupParent);
                      groupParent.children.put(artifactId, artifactIdDirectory);
                }
                
                // handle version folder
                Directory versionDirectory = (Directory) artifactIdDirectory.getChild(version);
                if (versionDirectory == null) {
                      versionDirectory = new ArtifactDirectory(version, artifact, artifactIdDirectory);
                        artifactIdDirectory.children.put(version, versionDirectory);

                }
                
                String arfifactFilename = artifactId + "-" + version + prefixedClassifier + "." + type;
                log.debug("artifact filename should be "+arfifactFilename);
                FilePath basePath = new FilePath(freeStyleBuild.getArtifactsDir());
                FilePath target = basePath.child(groupId).child(artifactId).child(version).child(arfifactFilename);
                log.debug("artifact archived? : "+target.exists()+", "+target);

                if (target.exists()) {
                    ArtifactItem artifactElement = new ArtifactItem(arfifactFilename, versionDirectory, artifact, this);
                    ChecksumFile shaChecksum = new ChecksumFile(arfifactFilename+".sha1", versionDirectory, "SHA-1", target.toString());
                    ChecksumFile md5Checksum = new ChecksumFile(arfifactFilename+".md5", versionDirectory, "MD5", target.toString());
                    
                    versionDirectory.children.put(arfifactFilename, artifactElement);
                    versionDirectory.children.put(arfifactFilename+".sha1", shaChecksum);
                    versionDirectory.children.put(arfifactFilename+".md5", md5Checksum);                    
                } else {
                    log.warn("artifact not found (maybe not archived): "+target);
                }
                String cacheName = freeStyleBuild.getParent().getName()+"@"+freeStyleBuild.getNumber();
                Element result = MavenRespositoryServerPlugin.buildCache.put(cacheName,this);
                log.debug("added to cache: "+cacheName+"= "+result);
                        
            } catch (IOException ex) {
                log.warn("IOException: ",ex);
                ex.printStackTrace();
            } catch (InterruptedException ex) {
                log.warn("IOException: ",ex);
                ex.printStackTrace();
            }
        }

    }
}
