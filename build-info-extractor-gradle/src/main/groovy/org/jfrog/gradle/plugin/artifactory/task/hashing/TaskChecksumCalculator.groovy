package org.jfrog.gradle.plugin.artifactory.task.hashing

import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Task
import org.gradle.api.tasks.TaskInputs
import org.jfrog.build.api.util.FileChecksumCalculator

/**
 * Created by alexistual on 27/01/2016.
 */
class TaskChecksumCalculator {

    String hash(Task task) {
        def bigCheckSum = ''
        def taskInputs = task.getInputs()
        taskInputs.getFiles().files.each { File file ->
            String checksum = DigestUtils.sha256Hex(file.newInputStream())
            println "Hashing file : $file : $checksum"
            bigCheckSum <<= checksum
        }
        if (taskInputs.getProperties()) {
            String propertyValuesToHash = ''
            taskInputs.getProperties().each { String key, Object value ->
                if (value instanceof String) {
                    propertyValuesToHash <<= value
                }
            }
            String propertyCheckSum = DigestUtils.sha256Hex(propertyValuesToHash)
            println "Hashing properties string : $propertyValuesToHash : $propertyCheckSum"
            bigCheckSum <<= propertyCheckSum
        }
        DigestUtils.sha256Hex(bigCheckSum.toString())
    }


}
