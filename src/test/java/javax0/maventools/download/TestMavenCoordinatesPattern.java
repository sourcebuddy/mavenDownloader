package javax0.maventools.download;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestMavenCoordinatesPattern {

    @DisplayName("Test some matching patterns")
    @Test
    void testMatchingPatterns(){
        Assertions.assertTrue(new MavenCoordinatesPattern("javax0","maventools","*").matches(new MavenCoordinates("javax0","maventools","1.0.0")));
        Assertions.assertTrue(new MavenCoordinatesPattern("javax0","*","*").matches(new MavenCoordinates("javax0","bibara numara hip hopp","1.0.1")));
    }

    @Test
    void testNonMatchingPaterns(){
        Assertions.assertFalse(new MavenCoordinatesPattern("javax0","NOT maventools","*").matches(new MavenCoordinates("javax0","maventools","1.0.0-SNAPSHOT")));
        Assertions.assertFalse(new MavenCoordinatesPattern("NOT javax0","*","*").matches(new MavenCoordinates("javax0","maventools","1.0.0")));
    }

    @DisplayName("Constructor throws IllegalArgumentException if the group id is '*'")
    @Test
    void testIllegalConstructor1(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> new MavenCoordinatesPattern("*","*","*"));
    }
    @DisplayName("Constructor throws IllegalArgumentException if the artifact id is '*' but version is not")
    @Test
    void testIllegalConstructor2(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> new MavenCoordinatesPattern("mukk","*","1.0.0"));
    }


}
