package javax0.maventools.download;

public class Repo {
    public final String name;
    public final String url;

    public Repo(final String name, final String url) {
        if (url.endsWith("/")) {
            this.url = url;
        } else {
            this.url = url + "/";
        }
        this.name = name;
    }

    // snippet repos
    final public static Repo MAVEN_CENTRAL = new Repo("central", "https://repo.maven.apache.org/maven2/");
    final public static Repo JCENTER_BINTRAY = new Repo("jcenter-bintray", "https://jcenter.bintray.com/");
    final public static Repo GOOGLE_ANDROID = new Repo("google-android", "https://dl.google.com/dl/android/maven2/");
    final public static Repo GOOGLE_MAVEN_CENTRAL_AMERICAS = new Repo("google-maven-central", "https://maven-central.storage-download.googleapis.com/repos/central/data/");
    final public static Repo GOOGLE_MAVEN_CENTRAL_EUROPE = new Repo("google-maven-central", "https://maven-central-eu.storage-download.googleapis.com/repos/central/data/");
    final public static Repo GOOGLE_MAVEN_CENTRAL_ASIA = new Repo("google-maven-central", "https://maven-central-asia.storage-download.googleapis.com/repos/central/data/");
    // end snippet
    final public static Repo[] REPOS = {
            MAVEN_CENTRAL, JCENTER_BINTRAY, GOOGLE_ANDROID, GOOGLE_MAVEN_CENTRAL_AMERICAS, GOOGLE_MAVEN_CENTRAL_EUROPE, GOOGLE_MAVEN_CENTRAL_ASIA
    };
}
