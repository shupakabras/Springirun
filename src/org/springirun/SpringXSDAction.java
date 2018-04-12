/*
 * Copyright 2012 Andrii Borovyk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springirun;

import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.javaee.ExternalResourceConfigurable;
import com.intellij.javaee.ExternalResourceManager;
import com.intellij.javaee.ExternalResourceManagerEx;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.vfs.VirtualFile;
import org.springirun.completion.SpringirunCompletionUtils;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

/**
 * Auto-import resources from spring.schema files.
 *
 * @author Andrii Borovyk
 */
public class SpringXSDAction extends AnAction {

    private static final String DEFAULT_SCHEMA_VERSION = "DEFAULT_SCHEMA_VERSION";

    private static final Pattern versionPattern = Pattern.compile("(\\d+.\\d+)");

    private String retrieveSchemaVersion(VirtualFile file) {
        VirtualFile mfFile = file.findFileByRelativePath("META-INF/MANIFEST.MF");

        if (mfFile != null) {
            try {
                Manifest manifest = new Manifest(mfFile.getInputStream());
                Attributes mainAttributes = manifest.getMainAttributes();
                return mainAttributes.getValue("Implementation-Version");
            }
            catch (IOException e) {

            }
        }
        return DEFAULT_SCHEMA_VERSION;
    }

    public void actionPerformed(AnActionEvent e) {

        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                Map<String, Map<String, VirtualFile>> schemasRouteMap = new HashMap<String, Map<String, VirtualFile>>();
                Project[] openedProjects = ProjectManager.getInstance().getOpenProjects();
                for (Project myProject : openedProjects) {
                    for (VirtualFile fileOrDir : OrderEnumerator.orderEntries(myProject).withoutSdk()
                                                                .getAllLibrariesAndSdkClassesRoots()) {
                        VirtualFile virtualFile = fileOrDir.findFileByRelativePath("META-INF/spring.schemas");
                        if (virtualFile != null) {
                            try {
                                Properties springFileSchemas = new Properties();
                                springFileSchemas.load(virtualFile.getInputStream());
                                Enumeration<String> schemas = (Enumeration<String>) springFileSchemas.propertyNames();
                                while (schemas.hasMoreElements()) {
                                    String schemaName = schemas.nextElement();
                                    String schemaPath = springFileSchemas.getProperty(schemaName);
                                    String version = retrieveSchemaVersion(fileOrDir);
                                    VirtualFile schemaFile = fileOrDir.findFileByRelativePath(schemaPath);

                                    Map<String, VirtualFile> schemaVersionMap = schemasRouteMap.get(schemaName);
                                    if (schemaVersionMap == null) {
                                        schemaVersionMap = new HashMap<String, VirtualFile>();
                                        schemasRouteMap.put(schemaName, schemaVersionMap);
                                    }

                                    schemaVersionMap.put(version, schemaFile);

                                }
                            }
                            catch (IOException e) {
                            }
                        }
                    }
                }
                ExternalResourceManagerEx.getInstanceEx().addIgnoredResource(SpringirunCompletionUtils.P_NAMESPACE);
                for (Map.Entry<String, Map<String, VirtualFile>> schemaRouteEntry : schemasRouteMap.entrySet()) {
                    VirtualFile schemaFile = getLastVersion(schemaRouteEntry.getValue());
                    if (schemaFile != null) {
                        ExternalResourceManager.getInstance().addResource(schemaRouteEntry.getKey(),
                            schemaFile.getCanonicalPath());
                    }
                }
            }

            private VirtualFile getLastVersion(Map<String, VirtualFile> schemaVersionMap) {
                VirtualFile file = null;
                double majorVersion = -1;
                for (Map.Entry<String, VirtualFile> schemaVersionEntry : schemaVersionMap.entrySet()) {
                    double version = 0;
                    try {
                        version = Double.parseDouble(versionPattern.matcher(schemaVersionEntry.getKey()).group(1));
                    }
                    catch (Exception e) {
                    }
                    if (version > majorVersion) {
                        majorVersion = version;
                        file = schemaVersionEntry.getValue();
                    }
                }
                return file;
            }
        });

        ShowSettingsUtil.getInstance().showSettingsDialog(e.getProject(), ExternalResourceConfigurable.class);
    }

}
