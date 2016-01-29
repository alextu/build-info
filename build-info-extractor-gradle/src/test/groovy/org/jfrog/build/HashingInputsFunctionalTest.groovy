package org.jfrog.build

import org.gradle.testkit.runner.GradleRunner
import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.ArtifactoryClient
import org.jfrog.artifactory.client.model.RepoPath
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

/**
 *
 */
class HashingInputsFunctionalTest extends Specification {

    @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder()

    private File buildFile
    List<File> pluginClasspath
    private String artUrl = 'http://localhost:8081/artifactory'
    private String artLogin = 'admin'
    private String artPassword = 'password'

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')

        def pluginClasspathResource = getClass().classLoader.findResource("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }

        pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }
    }

    def "build task should cause the simple hashing of compileJava"() {
        given:
        buildFile << """
        plugins {
            id 'com.jfrog.artifactory'
            id 'java'
        }

         hashingInputs {
            tasksName = ['compileJava', 'processResources']
         }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('build')
                .withPluginClasspath(pluginClasspath)
                .build()

        then:
        result.task(":hashingInputs").outcome == SUCCESS
    }

    def "publish task should cause the checksum to be attached to artifacts"() {
        given:
        buildFile << """

        plugins {
            id 'com.jfrog.artifactory'
            id 'java'
            id 'maven-publish'
        }

        group = 'org.jfrog.test.gradle.publish'
        version = '0.0.1-SNAPSHOT'

        hashingInputs {
            tasksName = ['compileJava', 'processResources']
        }

        publishing {
            publications {
                mavenJava(MavenPublication) {
                    from components.java
                }
            }
        }

        artifactory {
            contextUrl = '$artUrl'
            publish {
                repository {
                    repoKey = 'libs-snapshot-local' //The Artifactory repository key to publish to
                    username = "$artLogin" //The publisher user name
                    password = "$artPassword" //The publisher password
                }
                defaults {
                    publications('mavenJava')
                    publishBuildInfo = false
                }
            }
        }
        """
        and:
        File srcFile = file('src/main/java/org/gradle/api/PersonList.java')
        srcFile << """
            package org.gradle.api;

            public class PersonList {
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('build', 'artifactoryPublish', '--stacktrace')
                .withPluginClasspath(pluginClasspath)
                .build()

        then:
        println "Output : $result.output"
        result.task(":hashingInputs")?.outcome == SUCCESS
        and:
        result.task(":artifactoryPublish")?.outcome == SUCCESS
        and:
        Artifactory artifactory = ArtifactoryClient.create(artUrl, artLogin, artPassword)
        List<RepoPath> items = artifactory.searches().itemsByProperty().property(
                'gradle.tasks.input', ':compileJava=ed1b3b167f95e914bac4fc50aa50ecdcb72d6c45c5251904d4843a4e19768824').doSearch()
        items.size() > 0
    }

//    def "build task should replace compileJava outputs if the hashing is the same"() {
//
//
//
//
//    }

    private File file(String filepath) {
        File srcFile = new File(testProjectDir.getRoot(), filepath)
        srcFile.getParentFile().mkdirs()
        srcFile.createNewFile()
        return srcFile
    }


}
