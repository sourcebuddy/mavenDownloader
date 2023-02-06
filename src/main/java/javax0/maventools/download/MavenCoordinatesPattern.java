package javax0.maventools.download;

import java.util.Objects;

public class MavenCoordinatesPattern extends MavenCoordinates {
    public static MavenCoordinatesPattern fromString(final String coordinates) {
        final var s = coordinates.split(":", 3);
        if (s.length != 3) {
            throw new IllegalArgumentException(coordinates + " is not a valid maven coordinate, must have two : in it.");
        }
        return new MavenCoordinatesPattern(s[0], s[1],s[2]);
    }

    public MavenCoordinatesPattern(final String groupId, final String artifactId, final String version) {
        super(groupId, artifactId, version);
        if (groupId.equals("*")) {
            throw new IllegalArgumentException("The the group id must not be '*'");
        }
        if (artifactId.equals("*") && !version.equals("*")) {
            throw new IllegalArgumentException("The the artifact id must not be '*' if the version is not '*'");
        }
    }

    public boolean matches(final MavenCoordinates coords) {
        Objects.requireNonNull(coords);
        return groupId.equals(coords.groupId) &&
                (artifactId.equals("*") || artifactId.equals(coords.artifactId)) &&
                (version.equals("*") || version.equals(coords.version));
    }
}
