package javax0.maventools.download;

public enum ArtifactType {
    JAR(".jar"), SOURCES("-sources.jar"), JAVADOC("-javadoc.jar"), POM(".pom");

    public final String postfix;

    ArtifactType(final String postfix) {
        this.postfix = postfix;
    }

    public static ArtifactType type(final String symbolicName) {
        switch (symbolicName) {
            case "jar":
                return JAR;
            case "source":
            case "sources":
            case "src":
                return SOURCES;
            case "javadoc":
                return JAVADOC;
            default:
                throw new RuntimeException(symbolicName + " is not a known artifact type");
        }
    }
}
