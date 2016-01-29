/*
 * Copyright (C) 2011 JFrog Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jfrog.gradle.plugin.artifactory

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention
import org.jfrog.gradle.plugin.artifactory.extractor.listener.ProjectsEvaluatedBuildListener
import org.jfrog.gradle.plugin.artifactory.task.BuildInfoBaseTask
import org.jfrog.gradle.plugin.artifactory.task.hashing.HashingInputsTask
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.jfrog.gradle.plugin.artifactory.task.BuildInfoBaseTask.BUILD_INFO_TASK_NAME
import static org.jfrog.gradle.plugin.artifactory.task.BuildInfoBaseTask.HASHING_INPUTS_TASK_NAME

abstract class ArtifactoryPluginBase implements Plugin<Project> {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryPluginBase.class)
    static final String PUBLISH_TASK_GROUP = "publishing"

    def void apply(Project project) {
        if ("buildSrc".equals(project.name)) {
            log.debug("Artifactory Plugin disabled for ${project.path}")
            return
        }
        // Add a singleton artifactory plugin convention to the root project if needed
        ArtifactoryPluginConvention conv = getArtifactoryPluginConvention(project)
        // Then add the build info task
        addArtifactoryPublishTask(project)
        addHashingInputsTask(project)
        if (!conv.clientConfig.info.buildStarted) {
            conv.clientConfig.info.setBuildStarted(System.currentTimeMillis())
        }
        log.debug("Using Artifactory Plugin for ${project.path}")


        //Add build listener that responsible to override the resolve repositories
        if (!conv.clientConfig.isBuildListernerAdded()) {
            def gradle = project.getGradle()
            gradle.addBuildListener(new ProjectsEvaluatedBuildListener())
            conv.clientConfig.setBuildListernerAdded(true)
        }
    }

    protected abstract BuildInfoBaseTask createArtifactoryPublishTask(Project project)
    protected abstract ArtifactoryPluginConvention createArtifactoryPluginConvention(Project project)
    protected abstract HashingInputsTask createHashingInputsTask(Project project)

    /**
    *  Set the plugin convention closure object
    *  artifactory {
    *      ...
    *  }
    */
    private ArtifactoryPluginConvention getArtifactoryPluginConvention(Project project) {
        if (project.rootProject.convention.plugins.artifactory == null) {
            project.rootProject.convention.plugins.artifactory = createArtifactoryPluginConvention(project)
        }
        return project.rootProject.convention.plugins.artifactory
    }

    /**
     * Add the "ArtifactoryPublish" gradle task (under "publishing" task group)
     */
    private BuildInfoBaseTask addArtifactoryPublishTask(Project project) {
        BuildInfoBaseTask buildInfo = project.tasks.findByName(BUILD_INFO_TASK_NAME)
        if (buildInfo == null) {
            def isRoot = project.equals(project.getRootProject())
            log.debug("Configuring buildInfo task for project ${project.path}: is root? ${isRoot}")
            buildInfo = createArtifactoryPublishTask(project)
            buildInfo.setGroup(PUBLISH_TASK_GROUP)
        }
        buildInfo
    }

    private HashingInputsTask addHashingInputsTask(Project project) {
        HashingInputsTask inputsTask = project.tasks.findByName(HASHING_INPUTS_TASK_NAME)
        if (!inputsTask) {
            def isRoot = project.equals(project.getRootProject())
            log.debug("Configuring buildInfo task for project ${project.path}: is root? ${isRoot}")
            inputsTask = createHashingInputsTask(project)
            inputsTask.setGroup(PUBLISH_TASK_GROUP)
        }
        inputsTask
    }

}

