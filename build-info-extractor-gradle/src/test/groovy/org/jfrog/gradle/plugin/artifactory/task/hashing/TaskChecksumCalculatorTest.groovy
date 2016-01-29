package org.jfrog.gradle.plugin.artifactory.task.hashing

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskInputs
import spock.lang.Specification

/**
 *
 */
class TaskChecksumCalculatorTest extends Specification {

    def "Simple hashing task inputs"() {

        setup:
        Task task = mockTask()

        when:
        String checksum = new TaskChecksumCalculator().hash(task)

        then:
        checksum == 'ac448e5547744e6c55430bb902b096fb6b5d5261d344c2f4c8fb6d8e1d0ab1e3'

    }

    def "Simple hashing task inputs with properties"() {

        setup:
        Task task = mockTask([ 'someKey' : 'someValue', 'someKey2' : new byte[0] ])

        when:
        String checksum = new TaskChecksumCalculator().hash(task)

        then:
        checksum == '16d209cdce69b11bd6fb78ee4da7487a9b864fcbd24c05c6cc9fc650a7ec4d9a'

    }

    def "Simple hashing task inputs with changed properties"() {
        setup:
        Task task = mockTask([ 'someKey' : 'someValueChanged', 'someKey2' : new byte[0] ])

        when:
        String checksum = new TaskChecksumCalculator().hash(task)

        then:
        checksum != '16d209cdce69b11bd6fb78ee4da7487a9b864fcbd24c05c6cc9fc650a7ec4d9a'
    }

    private Task mockTask(Map properties = [:]) {
        Task task = Mock(Task)
        TaskInputs taskInputs = Mock(TaskInputs)
        FileCollection fileCollection = Mock(FileCollection)
        def sourceFilesDir = new File(getClass().getResource('/org/jfrog/gradle/plugin/artifactory/task/hashing/SomeClass.java').toURI()).parentFile

        task.getInputs() >> taskInputs
        fileCollection.getFiles() >> (sourceFilesDir.listFiles() as Set<File>)
        taskInputs.getFiles() >> fileCollection
        taskInputs.getProperties() >> properties
        task
    }

}
