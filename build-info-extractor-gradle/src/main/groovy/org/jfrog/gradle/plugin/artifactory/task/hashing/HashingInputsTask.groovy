package org.jfrog.gradle.plugin.artifactory.task.hashing

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask
import org.jfrog.gradle.plugin.artifactory.task.BuildInfoBaseTask

/**
 * Created by alexistual on 26/01/2016.
 */
class HashingInputsTask extends DefaultTask {

    @Input
    List<String> tasksName
    List<Task> tasksToHash = []

    @Override
    Task configure(Closure closure) {
        Task task = super.configure(closure)
        tasksName.each { String taskName ->
            Task taskToHash = getProject().tasks.findByName(taskName)
            tasksToHash << taskToHash
            if (!taskToHash) {
                throw new IllegalArgumentException("Task \"$taskName\" couldn't be found on the project")
            }
            taskToHash.dependsOn this
        }
        return task
    }

    @TaskAction
    void hashInputsFromTask() {
        tasksToHash.each { Task taskToHash ->
            String checksum = new TaskChecksumCalculator().hash(taskToHash)
            if (checksum) {
                BuildInfoBaseTask artifactoryTask = getProject().tasks.findByName(ArtifactoryTask.BUILD_INFO_TASK_NAME)
                // TODO : limit the properties to some defined patterns passed to the task (output files)
                artifactoryTask.setProperties(['gradle.tasks.input': "$taskToHash.path=$checksum"])
            }
        }
    }

}
