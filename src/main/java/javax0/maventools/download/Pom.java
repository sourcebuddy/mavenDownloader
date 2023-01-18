package javax0.maventools.download;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

public class Pom {

    private final Downloader dl;

    private final Map<Dependency, Dependency> dependencies = new HashMap<>();
    private final Map<Dependency, Dependency> dependencyManagement = new HashMap<>();
    private final Pom parent;

    private final Set<Pom> imported = new HashSet<>();

    final private MavenCoordinates coordinates;

    private final Properties properties = new Properties();

    private static final String HOME = System.getProperty("user.home");

    @Override
    public String toString() {
        final var local = Paths.get(HOME + "/.m2/repository");
        if (parent == null) {
            return String.format(coordinates.file(local, ArtifactType.POM).getAbsolutePath());
        } else {
            return String.format(String.format("%s\n%s", coordinates.file(local, ArtifactType.POM).getAbsolutePath(), parent));
        }
    }

    public String resolve(final String value) {
        var t = value.trim();
        if (t.length() == 0 || t.charAt(0) != '$') {
            return value;
        }
        t = t.substring(1).trim();
        if (t.length() < 2 || t.charAt(0) != '{' || t.charAt(t.length() - 1) != '}') {
            return value;
        }
        t = t.substring(1, t.length() - 1);
        return property(t).orElse(value);
    }

    public Optional<String> property(final String key) {
        if (properties.containsKey(key)) {
            return Optional.ofNullable(properties.getProperty(key));
        }
        if (parent != null) {
            return parent.property(key);
        }
        return Optional.empty();
    }

    public Set<Dependency> dependencies() {
        return dependencies.keySet();
    }

    public void dependency(Dependency dependency) {
        dependencies.put(dependency, dependency);
    }

    public void dependencyManagement(Dependency dependency) {
        dependencyManagement.put(dependency, dependency);
    }

    public Dependency get(Dependency dependency) {
        return dependencies.get(dependency);
    }

    public Dependency getManagement(Dependency dependency) {
        return dependencyManagement.get(dependency);
    }

    public Pom(final Downloader dl, final String groupId, final String artifactId, final String version, final Pom parent) {
        this.dl = dl;
        this.coordinates = new MavenCoordinates(groupId, artifactId, version);
        this.parent = parent;
        this.dl.poms.put(this.coordinates, this);
    }

    public enum DependencyScope {
        COMPILE, PROVIDED, RUNTIME, TEST, SYSTEM, IMPORT;


        public static DependencyScope fromString(final String s) {
            if (s == null || s.length() == 0) {
                return COMPILE;
            }
            return DependencyScope.valueOf(s.trim().toUpperCase());
        }
    }

    private class DepencencyCollector {
        final String groupId;
        final String artifactId;

        private DepencencyCollector(final String groupId, final String artifactId) {
            this.groupId = groupId;
            this.artifactId = artifactId;
        }

        String version = "";
        String scope = "";

        DepencencyCollector version(String version) {
            if (this.version.length() == 0) {
                this.version = resolve(version);
            }
            return this;
        }

        DepencencyCollector scope(String scope) {
            if (this.scope.length() == 0) {
                this.scope = resolve(scope);
            }
            return this;
        }

        boolean complete() {
            return version.length() > 0 && scope.length() > 0;
        }

        Dependency toDependency() {
            return new Dependency(groupId, artifactId, version, scope);
        }
    }

    public class Dependency extends MavenCoordinates {
        final public String scope;

        public DependencyScope scope() {
            if (scope.length() == 0) {
                return DependencyScope.COMPILE;
            }
            return DependencyScope.fromString(scope);
        }

        public Dependency effective() {
            final String groupId = resolve(this.groupId);
            final String artifactId = resolve(this.artifactId);
            String version = resolve(this.version);
            String scope = resolve(this.scope);
            final var collector = new DepencencyCollector(groupId, artifactId).version(version).scope(scope);
            collect(collector, Pom.this);
            if (collector.version.length() == 0) {
                throw new RuntimeException(String.format("There is no version for '%s:%s' in %s", groupId, artifactId, Pom.this));
            }
            return collector.toDependency();
        }

        private void collect(final DepencencyCollector collector, Pom from) {
            if (!collector.complete()) {
                for (Pom p = from; p != null; p = p.parent) {
                    final var d = p.getManagement(this);
                    if (d != null) {
                        collector.version(d.version).scope(d.scope);
                        if (collector.complete()) {
                            return;
                        }
                    }
                    for (final Pom imported : from.imported) {
                        collect(collector, imported);
                    }
                }
            }
        }

        public Dependency(final String groupId, final String artifactId, final String version, final String scope) {
            super(groupId, artifactId, version);
            this.scope = scope;
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


    private static final String DEPENDENCY = "//project/dependencies/dependency";
    private static final String MANAGEMENT = "//project/dependencyManagement/dependencies/dependency";

    private static final String G = "[%d]/groupId/text()";
    private static final String A = "[%d]//artifactId/text()";
    private static final String V = "[%d]//version/text()";
    private static final String S = "[%d]//scope/text()";

    public static Pom load(MavenCoordinates coords, final Downloader dl) throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {
        if (dl.poms.containsKey(coords)) {
            return dl.poms.get(coords);
        }
        final var dbFactory = DocumentBuilderFactory.newInstance();
        final var dBuilder = dbFactory.newDocumentBuilder();

        final var pomFile = dl.download(coords, ArtifactType.POM);
        final var doc = dBuilder.parse(pomFile);

        final var xPath = XPathFactory.newInstance().newXPath();
        final var parentGroupId = (String) xPath.evaluate("//project/parent/groupId/text()", doc, XPathConstants.STRING);
        final var parentArtifactId = (String) xPath.evaluate("//project/parent/artifactId/text()", doc, XPathConstants.STRING);
        final var parentVersion = (String) xPath.evaluate("//project/parent/version/text()", doc, XPathConstants.STRING);
        final Pom parentPom;
        if (parentGroupId != null && parentGroupId.length() > 0 &&
                parentArtifactId != null && parentArtifactId.length() > 0 &&
                parentVersion != null && parentVersion.length() > 0) {
            parentPom = load(new MavenCoordinates(parentGroupId, parentArtifactId, parentVersion), dl);
        } else {
            parentPom = null;
        }

        final var meG = (String) xPath.evaluate("//project/groupId/text()", doc, XPathConstants.STRING);
        final String meGroupId;
        if (meG.length() == 0 && parentPom != null) {
            meGroupId = parentPom.coordinates.groupId;
        } else {
            meGroupId = meG;
        }
        final var meArtifactId = (String) xPath.evaluate("//project/artifactId/text()", doc, XPathConstants.STRING);
        final var meV = (String) xPath.evaluate("//project/version/text()", doc, XPathConstants.STRING);
        final String meVersion;
        if (meV.length() == 0 && parentPom != null) {
            meVersion = parentPom.coordinates.version;
        } else {
            meVersion = meV;
        }

        final var thisPom = new Pom(dl, meGroupId, meArtifactId, meVersion, parentPom);

        final var propertiesNode = (NodeList) xPath.evaluate("//project/properties", doc, XPathConstants.NODESET);
        if (propertiesNode.getLength() != 0) {
            final var properties = propertiesNode.item(0).getChildNodes();
            for (int i = 0; i < properties.getLength(); i++) {
                final var property = properties.item(i);
                final var key = property.getNodeName();
                if (property.getChildNodes().getLength() > 0) {
                    final var value = property.getChildNodes().item(0).getNodeValue();
                    thisPom.properties.put(key, value);
                }
            }
        }
        thisPom.properties.put("project.groupId", meGroupId);
        thisPom.properties.put("project.artifactId", meArtifactId);
        thisPom.properties.put("project.version", meVersion);

        final var dependencies = (NodeList) xPath.evaluate(DEPENDENCY, doc, XPathConstants.NODESET);
        for (int i = 1; i <= dependencies.getLength(); i++) {
            final Dependency d = thisPom.getDependency(doc, xPath, i, DEPENDENCY);
            if (d.scope() == DependencyScope.IMPORT) {
                thisPom.imported.add(load(d, dl));
            } else {
                thisPom.dependency(d);
            }
        }
        final var managements = (NodeList) xPath.evaluate(MANAGEMENT, doc, XPathConstants.NODESET);
        for (int i = 1; i <= managements.getLength(); i++) {
            final var d = thisPom.getDependency(doc, xPath, i, MANAGEMENT);
            if (d.scope() == DependencyScope.IMPORT) {
                thisPom.imported.add(load(d, dl));
            } else {
                thisPom.dependencyManagement(d);
            }
        }
        return thisPom;
    }

    private Dependency getDependency(final Document doc, final XPath xPath, final int i, final String what) throws XPathExpressionException {
        final var groupId = (String) xPath.evaluate(String.format(what + G, i), doc, XPathConstants.STRING);
        final var artifactId = (String) xPath.evaluate(String.format(what + A, i), doc, XPathConstants.STRING);
        final var version = (String) xPath.evaluate(String.format(what + V, i), doc, XPathConstants.STRING);
        final var scope = (String) xPath.evaluate(String.format(what + S, i), doc, XPathConstants.STRING);
        return new Dependency(resolve(groupId), resolve(artifactId), resolve(version), resolve(scope));
    }
}
