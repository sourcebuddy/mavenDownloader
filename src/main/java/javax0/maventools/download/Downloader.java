package javax0.maventools.download;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Downloader {
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;

    final private List<Repo> repos;
    final private Path local;

    final protected Map<MavenCoordinates, Pom> poms = new HashMap<>();

    public Downloader(final Path local, Repo... repos) {
        this.local = local;
        this.repos = Arrays.asList(repos);

    }

    public File[] fetch(final String coordinates) throws IOException, XPathExpressionException, ParserConfigurationException, SAXException {
        final var c = coordinates.split(":", 3);
        if (c.length != 3) {
            throw new IllegalArgumentException(coordinates + " is not a valid maven coordinate, must have two : in it.");
        }
        return fetch(new MavenCoordinates(c[0], c[1], c[2]), Set.of(ArtifactType.JAR), Set.of(Pom.DependencyScope.COMPILE));
    }

    public File[] fetch(final MavenCoordinates coordinates,
                        final Set<ArtifactType> types,
                        final Set<Pom.DependencyScope> scopes) throws IOException, XPathExpressionException, ParserConfigurationException, SAXException {
        final var files = new ArrayList<File>();
        dl(coordinates, files, types, scopes);
        return files.toArray(File[]::new);
    }


    private void dl(final MavenCoordinates coordinates,
                    final ArrayList<File> files,
                    final Set<ArtifactType> types,
                    final Set<Pom.DependencyScope> scopes) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        final var backlog = new ArrayList<MavenCoordinates>();
        backlog.add(coordinates);

        final var fetched = new HashSet<MavenCoordinates>();
        while (!backlog.isEmpty()) {
            final var current = backlog.get(0);
            backlog.remove(0);
            if (!fetched.contains(current)) {
                for (final var type : types) {
                    files.add(download(current, type));
                }
                final var pomFile = Pom.load(current, this);
                for (final var key : pomFile.dependencies()) {
                    final var dependency = pomFile.get(key).effective();
                    if (scopes.contains(dependency.scope())) {
                        if (!fetched.contains(dependency)) {
                            backlog.add(dependency);
                        }
                    }
                }
                fetched.add(current);
            }
        }
    }

    public File download(final MavenCoordinates coordinates, final ArtifactType type) throws IOException {
        final var cache = coordinates.file(local, type);
        if (!cache.exists()) {
            for (final var repo : repos) {
                try (final var is = getStream(coordinates.url(repo, type))) {
                    Files.createDirectories(cache.getParentFile().toPath());
                    try (final var os = new FileOutputStream(cache)) {
                        is.transferTo(os);
                        return cache;
                    }
                } catch (IOException ignore) {
                }
            }
            throw new IOException("Cannot download " + coordinates);
        }
        return cache;
    }

    private static InputStream getStream(URL url) throws IOException {
        final var con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(CONNECT_TIMEOUT);
        con.setReadTimeout(READ_TIMEOUT);
        con.setInstanceFollowRedirects(true);
        final int status = con.getResponseCode();
        if (status != 200) {
            throw new IOException("GET url '" + url + "' returned " + status);
        }
        return con.getInputStream();
    }

}
