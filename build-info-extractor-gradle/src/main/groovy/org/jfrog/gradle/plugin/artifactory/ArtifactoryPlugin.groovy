package org.jfrog.gradle.plugin.artifactory

import org.gradle.api.Project
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention
import org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask
import org.jfrog.gradle.plugin.artifactory.task.BuildInfoBaseTask
import org.jfrog.gradle.plugin.artifactory.task.hashing.HashingInputsTask

import static org.jfrog.gradle.plugin.artifactory.task.BuildInfoBaseTask.BUILD_INFO_TASK_NAME
import static org.jfrog.gradle.plugin.artifactory.task.BuildInfoBaseTask.HASHING_INPUTS_TASK_NAME

/**
 * @author Lior Hasson  
 */
class ArtifactoryPlugin extends ArtifactoryPluginBase{
    @Override
    protected ArtifactoryPluginConvention createArtifactoryPluginConvention(Project project) {
        return new ArtifactoryPluginConvention(project)
    }

    @Override
    protected HashingInputsTask createHashingInputsTask(Project project) {
        def result = project.getTasks().create(HASHING_INPUTS_TASK_NAME, HashingInputsTask.class)
        result.setDescription('''Generates hash from inputs of a task to attach them to published artifacts
                                 so we can optimize future builds''')
        return result
    }

    @Override
    protected BuildInfoBaseTask createArtifactoryPublishTask(Project project) {
        def result = project.getTasks().create(BUILD_INFO_TASK_NAME, ArtifactoryTask.class)
        result.setDescription('''Deploys artifacts + generated build-info metadata to Artifactory,
                                 using project configurations.''')
        return result
    }
}
