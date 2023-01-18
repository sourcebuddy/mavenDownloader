package javax0.maventools.download;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

public class MavenCoordinates extends MavenCoordinates2{

    public final String version;

    @Override
    public String toString() {
        return String.format("%s:%s:%s", groupId, artifactId, version);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MavenCoordinates that = (MavenCoordinates) o;
        return groupId.equals(that.groupId) && artifactId.equals(that.artifactId) && version.equals(that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version);
    }

    private final String groupIdDir;

    public MavenCoordinates(final String groupId, final String artifactId, final String version) {
        super(groupId,artifactId);
        this.groupIdDir = groupId.replaceAll("\\.", "/") + "/";
        this.version = Objects.requireNonNull(version);
        if (groupId.length() == 0 ||
                artifactId.length() == 0 ) {
            throw new RuntimeException(String.format("'%s:%s:%s' is invalid",groupId,artifactId,version));
        }
    }

    public File file(final Path local, final ArtifactType type) throws MalformedURLException {
        return new File(local.toFile(),
                groupIdDir +
                        artifactId + "/" +
                        version + "/" +
                        artifactId + "-" +
                        version +
                        type.postfix);
    }

    public URL url(final Repo repo, final ArtifactType type) throws MalformedURLException {
        return new URL(repo.url +
                groupIdDir +
                artifactId + "/" +
                version + "/" +
                artifactId + "-" +
                version +
                type.postfix);
    }
}
