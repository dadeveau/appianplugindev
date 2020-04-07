/*
 * @author Daniel DeVeau
 */
package com.deveau.pluginsrcanalyzer;

import com.appiancorp.services.ServiceContext;
import com.appiancorp.suiteapi.common.exceptions.AppianException;
import com.appiancorp.suiteapi.common.exceptions.InvalidOperationException;
import com.appiancorp.suiteapi.content.ContentConstants;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.expression.annotations.AppianScriptingFunctionsCategory;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import com.appiancorp.suiteapi.knowledge.Document;
import com.appiancorp.suiteapi.knowledge.DocumentDataType;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipException;

/* Open Questions and action
Every class file must have a java file.
Support Zip.
 */


@AppianScriptingFunctionsCategory
public class findpluginsrc {
    private static final Logger LOG = Logger.getLogger(findpluginsrc.class);

    @Function
    public boolean doesPluginHaveSourceCode(ContentService cs, ServiceContext sc, @Parameter @DocumentDataType Long pluginJarID)
            throws AppianException {

        Document pluginDocument = cs.download(pluginJarID, ContentConstants.VERSION_CURRENT, false)[0];
        String pluginFilePath = pluginDocument.getInternalFilename();
        String pluginDocumentExtension = pluginDocument.getExtension().toLowerCase();
        LOG.debug("Processing file: " + pluginFilePath + " with extension: " + pluginDocumentExtension);

        if (!pluginDocumentExtension.equals("jar")) {
            throw new InvalidOperationException("pluginJarID passed in is not a JAR file.");
        }
        //throws AppianException
        return srcCodeFinder(pluginFilePath);
    }

    public boolean srcCodeFinder(String jarPath)
    //throws AppianException
    {
        try (JarInputStream pluginJarStream = new JarInputStream(new FileInputStream(jarPath), false)) {
            JarEntry pluginJar;
            while (true) {
                pluginJar = pluginJarStream.getNextJarEntry();
                if (pluginJar == null) {
                    break;
                }
                LOG.debug("Processing File: " + pluginJar.getName());
                if (pluginJar.getName().endsWith(".java")) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            LOG.error("FileNotFoundException thrown while trying to open a FileInputStream to process JAR file.");
            //throw new AppianException(e);
            return true; //Moving on rather than throwing an error to avoid public facing error messages.
        } catch (ZipException e) {
            LOG.error("ZipException thrown while trying to get the next entry from the JAR file. Printing exception and moving on:", e);
            return true; //Moving on rather than throwing an error to avoid public facing error messages.
        } catch (IOException e) {
            LOG.error("IOException thrown while trying to process the JAR file. Failing:");
            //throw new AppianException(e);
            return true; //Moving on rather than throwing an error to avoid public facing error messages.
        }
        return false;
    }

}
