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

import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import java.util.List;

/**
 *
 * @author henrik
 */
public class Jobs extends Directory{

    public Jobs(Directory parent) {
        super("jobs",parent);
    }    
    
    @Override
    protected void loadChildren() {

        Hudson hudson = Hudson.getInstance();
        List<FreeStyleProject> freeStyleProjects = hudson.getItems(FreeStyleProject.class);
        for (FreeStyleProject freeStyleProject : freeStyleProjects) {
            if (freeStyleProject.getLastStableBuild() != null) {
                Job project = new Job(freeStyleProject, this);
                children.put(freeStyleProject.getName(), project);
            }
        }
    }
}
