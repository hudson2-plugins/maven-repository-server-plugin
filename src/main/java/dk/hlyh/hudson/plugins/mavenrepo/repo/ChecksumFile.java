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

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Hex;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 *
 * @author henrik
 */
public class ChecksumFile extends Element {

    private final int STREAMING_BUFFER_SIZE = 32768;
    private String algorithm;
    private final String filename;

    public ChecksumFile(String name, Element parent, String algorithm, String filename) {
        super(name, parent);
        this.algorithm = algorithm;
        this.filename = filename;
    }

    @Override
    public void sendResponse(StaplerRequest req, StaplerResponse rsp) throws IOException {
        FileInputStream fis = new FileInputStream(filename);
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.reset();


            // Stream the file contents to the MessageDigest.
            byte[] buffer = new byte[STREAMING_BUFFER_SIZE];
            int size = fis.read(buffer, 0, STREAMING_BUFFER_SIZE);
            while (size >= 0) {
                messageDigest.update(buffer, 0, size);
                size = fis.read(buffer, 0, STREAMING_BUFFER_SIZE);
            }

            Hex encoder = new Hex();
            byte[] encodeded = encoder.encode(messageDigest.digest());

            rsp.setContentType("text/plain");
            rsp.getOutputStream().write(encodeded);

        } catch (NoSuchAlgorithmException ex) {
            rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                /* ignored */
            }
        }
    }
}
