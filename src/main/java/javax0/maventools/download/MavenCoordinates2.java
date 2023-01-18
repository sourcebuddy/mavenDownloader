package javax0.maventools.download;

import java.util.Objects;

public class MavenCoordinates2 {
    public final String groupId;
    public final String artifactId;

    public MavenCoordinates2(final String groupId, final String artifactId) {
        this.groupId = Objects.requireNonNull(groupId);
        this.artifactId = Objects.requireNonNull(artifactId);
    }

    @Override
    public String toString() {
        return String.format("%s:%s", groupId, artifactId);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MavenCoordinates2 that = (MavenCoordinates2) o;
        return groupId.equals(that.groupId) && artifactId.equals(that.artifactId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId);
    }

}
