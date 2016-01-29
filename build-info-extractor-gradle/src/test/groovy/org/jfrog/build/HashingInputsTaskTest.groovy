package org.jfrog.build

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.jfrog.gradle.plugin.artifactory.ArtifactoryPlugin
import org.jfrog.gradle.plugin.artifactory.ArtifactoryPluginBase
import org.jfrog.gradle.plugin.artifactory.task.hashing.HashingInputsTask

import static org.jfrog.gradle.plugin.artifactory.task.BuildInfoBaseTask.HASHING_INPUTS_TASK_NAME

/**
 *
 */
class HashingInputsTaskTest extends PluginTestBase {

    @Override
    ArtifactoryPluginBase createPlugin() {
        return new ArtifactoryPlugin()
    }

    def 'hashingInputsTask get applied on compileJava and processResources'() {
        URL resource = getClass().getResource('/org/jfrog/build/hashingInputsTaskDslTest/build.gradle')
        def projDir = new File(resource.toURI()).getParentFile()

        Project project = ProjectBuilder.builder().withProjectDir(projDir).build()
        project.plugins.apply(JavaPlugin)
        project.plugins.apply(ArtifactoryPlugin)

        HashingInputsTask hashingInputsTask = project.tasks.findByName(HASHING_INPUTS_TASK_NAME)
        project.evaluate()
        projectEvaluated(project)

        expect:
        hashingInputsTask.tasksName.contains 'compileJava'
        and:
        Task compileJava = project.tasks.findByName('compileJava')
        compileJava.taskDependencies.getDependencies().find { Task task -> task.name == HASHING_INPUTS_TASK_NAME }
        and:
        Task processResources = project.tasks.findByName('processResources')
        compileJava.taskDependencies.getDependencies().find { Task task -> task.name == HASHING_INPUTS_TASK_NAME }
    }




}
