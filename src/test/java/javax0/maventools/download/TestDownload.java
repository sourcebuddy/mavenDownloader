package javax0.maventools.download;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;

public class TestDownload {

    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    // snippet testSample
    @Test
    void testDownload() throws Exception {
        final var HOME = System.getProperty("user.home");
        final var downloader = new Downloader(
                Paths.get(HOME + "/.m2/repository"),
                Repo.MAVEN_CENTRAL);
        final var files = downloader.fetch("com.javax0.jamal:jamal-all:1.12.5");
        Assertions.assertEquals(169, files.length);
        Arrays.sort(files);
        for (final var file : files) {
            Assertions.assertTrue(file.exists());
        }
    }
    // end snippet

    @Test
    void testDownload0() throws Exception {
        final var HOME = System.getProperty("user.home");
        final var dir = HOME + "/.m2/repository/com/squareup/tools/build/maven-archeologist/0.0.3.1/";
        deleteDirectory(new File(dir));
        final var downloader = new Downloader(Paths.get(HOME + "/.m2/repository"), Repo.MAVEN_CENTRAL);
        final var files = downloader.fetch("com.squareup.tools.build:maven-archeologist:0.0.3.1");
        Assertions.assertTrue(new File(dir + "maven-archeologist-0.0.3.1.jar").exists());
        Assertions.assertEquals(22, files.length);
    }

    @Test
    void testDownload1() throws Exception {
        final var HOME = System.getProperty("user.home");
        final var dir = HOME + "/.m2/repository/com/squareup/tools/build/maven-archeologist/0.0.3.1/";
        deleteDirectory(new File(dir));
        new Downloader(Paths.get(HOME + "/.m2/repository"), Repo.MAVEN_CENTRAL)
                .fetch(new MavenCoordinates("com.squareup.tools.build", "maven-archeologist", "0.0.3.1"), Set.of(ArtifactType.JAR), Set.of(Pom.DependencyScope.COMPILE));
        Assertions.assertTrue(new File(dir + "maven-archeologist-0.0.3.1.jar").exists());
    }

}
