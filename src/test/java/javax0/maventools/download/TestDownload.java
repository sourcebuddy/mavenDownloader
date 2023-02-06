package javax0.maventools.download;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;

public class TestDownload {

    void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        //noinspection ResultOfMethodCallIgnored
        directoryToBeDeleted.delete();
    }

    // snippet testSample

    private static final int NUMER_OF_FILES_IN_JAMAL_ALL_1_12_5 = 169;

    @DisplayName("Download Jamal 1.12.5 and check that all files are downloaded")
    @Test
    void testDownload() throws Exception {
        final var HOME = System.getProperty("user.home");
        final var downloader = new Downloader(
                Paths.get(HOME + "/.m2/repository"),
                Repo.MAVEN_CENTRAL);
        downloader.exclude("com.javax0.jamal:jamal-api:*");
        final var files = downloader.fetch("com.javax0.jamal:jamal-all:1.12.5");
        Assertions.assertEquals(NUMER_OF_FILES_IN_JAMAL_ALL_1_12_5-1, files.length);
        Arrays.sort(files);
        for (final var file : files) {
            Assertions.assertTrue(file.exists());
        }
    }
    // end snippet

    @DisplayName("Download Jamal 1.12.5 and check that all files are downloaded except the one excluded")
    @Test
    void testDownloadWithExclude() throws Exception {
        final var HOME = System.getProperty("user.home");
        final var downloader = new Downloader(
                Paths.get(HOME + "/.m2/repository"),
                Repo.MAVEN_CENTRAL);
        final var files = downloader.fetch("com.javax0.jamal:jamal-all:1.12.5");
        Assertions.assertEquals(NUMER_OF_FILES_IN_JAMAL_ALL_1_12_5 , files.length);
        Arrays.sort(files);
        for (final var file : files) {
            Assertions.assertTrue(file.exists());
        }
    }

    @DisplayName("Download some arbitrary artifact after deleted from the local repo using two remote repos")
    @Test
    void testDownload0() throws Exception {
        final var HOME = System.getProperty("user.home");
        final var dir = HOME + "/.m2/repository/com/squareup/tools/build/maven-archeologist/0.0.3.1/";
        deleteDirectory(new File(dir));
        final var downloader = new Downloader(Paths.get(HOME + "/.m2/repository"), Repo.GOOGLE_MAVEN_CENTRAL_ASIA, Repo.MAVEN_CENTRAL);
        final var files = downloader.fetch("com.squareup.tools.build:maven-archeologist:0.0.3.1");
        Assertions.assertTrue(new File(dir + "maven-archeologist-0.0.3.1.jar").exists());
        Assertions.assertEquals(15, files.length);
    }

    @DisplayName("Download some arbitrary artifact after deleted from the local repo using maven central only")
    @Test
    void testDownload1() throws Exception {
        final var HOME = System.getProperty("user.home");
        final var dir = HOME + "/.m2/repository/com/squareup/tools/build/maven-archeologist/0.0.3.1/";
        deleteDirectory(new File(dir));
        new Downloader(Paths.get(HOME + "/.m2/repository"), Repo.MAVEN_CENTRAL)
                .fetch(new MavenCoordinates("com.squareup.tools.build", "maven-archeologist", "0.0.3.1"),
                        Set.of(ArtifactType.JAR),
                        Set.of(Pom.DependencyScope.COMPILE));
        Assertions.assertTrue(new File(dir + "maven-archeologist-0.0.3.1.jar").exists());
    }

}
