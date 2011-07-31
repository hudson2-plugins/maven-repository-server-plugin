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

import hudson.model.AbstractProject;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.util.RunList;
import dk.hlyh.hudson.plugins.mavenrepo.MavenRespositoryServerPlugin;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author henrik
 */
class Job extends Directory {

    private static final Logger log = LoggerFactory.getLogger("maven-repository-server");
    private AbstractProject hudsonProject;

    Job(FreeStyleProject hudsonProject, Directory root) {
        super(hudsonProject.getName(), root);
        this.hudsonProject = hudsonProject;
        children = new TreeMap<String, Element>(new NumberComparator());
    }

    @Override
    public Element getChild(String name) {
        String effectiveName=name;
        if (name.equals("latest")) {
            effectiveName = "" + hudsonProject.getLastStableBuild().getNumber();
        }
        String cacheName = hudsonProject.getName()+"@"+effectiveName;
        Element result = MavenRespositoryServerPlugin.buildCache.get(cacheName);
        log.info("result of cache lookup: "+cacheName+"= "+result);
        
        return result != null ? result : super.getChild(effectiveName);
    }

    
    
    @Override
    protected void loadChildren() {
        RunList<FreeStyleBuild> freeStyleBuilds = hudsonProject.getBuilds();

        for (FreeStyleBuild freeStyleBuild : freeStyleBuilds) {
            if (!freeStyleBuild.isBuilding() && freeStyleBuild.getResult().isBetterOrEqualTo(Result.SUCCESS)) {
                Build build = new Build(freeStyleBuild, this);
                children.put("" + freeStyleBuild.getNumber(), build);
            }
        }
        FreeStyleBuild latestFreeStyle = (FreeStyleBuild) hudsonProject.getLastStableBuild();
        Build latest = new Build(latestFreeStyle, this);
        children.put("latest", latest);
        
    }
    private static class NumberComparator implements java.util.Comparator<String> {

        public NumberComparator() {
        }

        public int compare(String o1, String o2) {
            if (o1.equals("latest")) {
                return -1;
            }
            if (o2.equals("latest")) {
                return 1;
            }            
            int i1 = Integer.parseInt(o1);
            int i2 = Integer.parseInt(o2);
            return i2 - i1;
        }
    }    
    
}
