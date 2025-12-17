package uk.gov.hmcts.opal.openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OpenApiBundler {

    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    // All OpenAPI component sections that can contain $ref targets
    private static final List<String> COMPONENT_SECTIONS = List.of(
        "schemas", "responses", "parameters", "headers",
        "requestBodies", "examples", "links", "callbacks", "securitySchemes", "pathItems"
    );

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: OpenApiBundler <inputDir> <outputFile>");
            System.exit(1);
        }

        final Path inputDir = Paths.get(args[0]);
        final Path outputFile = Paths.get(args[1]);

        Map<String, Object> bundled = createBundleTemplate();
        Map<String, Object> bundledComponents = getComponents(bundled);
        Map<String, Object> bundledPaths = getPaths(bundled);

        List<Path> yamlFiles = loadYamlFiles(inputDir);
        List<YamlSource> sources = loadSources(yamlFiles);
        Map<String, Map<String, Map<String, String>>> renameLookup = planComponentRenames(sources);
        sources.forEach(source -> processSource(source, renameLookup, bundledPaths, bundledComponents));

        mapper.writeValue(outputFile.toFile(), bundled);
    }

    private static Map<String, Object> createBundleTemplate() {
        Map<String, Object> bundled = new LinkedHashMap<>();
        bundled.put("openapi", "3.1.1");
        bundled.put("info", Map.of("title", "Bundled API", "version", "1.0.0"));
        bundled.put("paths", new LinkedHashMap<>());

        Map<String, Object> components = new LinkedHashMap<>();
        bundled.put("components", components);
        COMPONENT_SECTIONS.forEach(section -> components.put(section, new LinkedHashMap<String, Object>()));

        return bundled;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getComponents(Map<String, Object> bundled) {
        return (Map<String, Object>) bundled.get("components");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getPaths(Map<String, Object> bundled) {
        return (Map<String, Object>) bundled.get("paths");
    }

    private static List<Path> loadYamlFiles(Path inputDir) throws IOException {
        try (var stream = Files.list(inputDir)) {
            return stream
                .filter(f -> f.toString().endsWith(".yaml"))
                .sorted()
                .toList();
        }
    }

    @SuppressWarnings("unchecked")
    private static List<YamlSource> loadSources(List<Path> yamlFiles) throws IOException {
        List<YamlSource> sources = new ArrayList<>();
        for (Path file : yamlFiles) {
            Map<String, Object> yaml = mapper.readValue(file.toFile(), Map.class);
            String baseName = stripYaml(file.getFileName().toString());
            String suffix = capitalize(baseName);
            sources.add(new YamlSource(file, baseName, suffix, yaml));
        }
        return sources;
    }

    private static void processSource(
        YamlSource source,
        Map<String, Map<String, Map<String, String>>> renameLookup,
        Map<String, Object> bundledPaths,
        Map<String, Object> bundledComponents
    ) {
        try {
            Map<String, Object> yaml = source.yaml();
            mergePaths(yaml, source, renameLookup, bundledPaths);
            mergeComponents(yaml, source, renameLookup, bundledComponents);
        } catch (Exception e) {
            throw new RuntimeException("While processing " + source.path().getFileName() + ": "
                + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void mergePaths(
        Map<String, Object> yaml,
        YamlSource source,
        Map<String, Map<String, Map<String, String>>> renameLookup,
        Map<String, Object> bundledPaths
    ) {
        Map<String, Object> paths = (Map<String, Object>) yaml.get("paths");
        if (paths == null) {
            return;
        }

        paths.replaceAll((k, v) -> rewriteRefs(v, source.baseName(), renameLookup));
        bundledPaths.putAll(paths);
    }

    @SuppressWarnings("unchecked")
    private static void mergeComponents(
        Map<String, Object> yaml,
        YamlSource source,
        Map<String, Map<String, Map<String, String>>> renameLookup,
        Map<String, Object> bundledComponents
    ) {
        Map<String, Object> components = (Map<String, Object>) yaml.get("components");
        if (components == null) {
            return;
        }

        for (String section : COMPONENT_SECTIONS) {
            Map<String, Object> src = (Map<String, Object>) components.get(section);
            if (src == null) {
                continue;
            }
            mergeComponentSection(source, section, src, renameLookup, bundledComponents);
        }
    }

    private static void mergeComponentSection(
        YamlSource source,
        String section,
        Map<String, Object> src,
        Map<String, Map<String, Map<String, String>>> renameLookup,
        Map<String, Object> bundledComponents
    ) {
        Map<String, Object> dst = getOrCreateSection(bundledComponents, section);
        Map<String, String> renamesForSection = renameLookup
            .getOrDefault(source.baseName(), Map.of())
            .getOrDefault(section, Map.of());

        for (Map.Entry<String, Object> entry : src.entrySet()) {
            String oldName = entry.getKey();
            String newName = renamesForSection.getOrDefault(oldName, oldName);
            Object valueWithRewrites = rewriteRefs(entry.getValue(), source.baseName(), renameLookup);

            if (dst.containsKey(newName)) {
                throw new IllegalStateException(
                    "Name collision in components/" + section + ": " + newName + " (from "
                        + source.path().getFileName() + ")"
                );
            }
            dst.put(newName, valueWithRewrites);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object rewriteRefs(
        Object node,
        String currentFileBase,
        Map<String, Map<String, Map<String, String>>> renameLookup
    ) {
        if (node instanceof Map) {
            Map<String, Object> map = new LinkedHashMap<>((Map<String, Object>) node);

            Object refVal = map.get("$ref");
            if (refVal instanceof String ref) {
                // Case 1: external file reference: ./file.yaml#/components/<section>/<name>[...]
                if (ref.startsWith("./")) {
                    String[] fileAndPath = ref.split("#", 2);
                    String fileName = stripYaml(new File(fileAndPath[0]).getName());

                    if (fileAndPath.length == 2 && fileAndPath[1].startsWith("/components/")) {
                        String componentPath = fileAndPath[1].replaceFirst("^/components/", "");
                        map.put("$ref", "#/components/"
                            + rewriteComponentPath(fileName, componentPath, renameLookup));
                    }
                // Case 2: local reference: #/components/<section>/<name>[...]
                } else if (ref.startsWith("#/components/")) {
                    String componentPath = ref.replaceFirst("^#/components/", "");
                    map.put("$ref", "#/components/"
                        + rewriteComponentPath(currentFileBase, componentPath, renameLookup));
                }
            }

            // Recurse
            map.replaceAll((k, v) -> rewriteRefs(v, currentFileBase, renameLookup));
            return map;
        } else if (node instanceof List) {
            List<Object> list = new ArrayList<>();
            for (Object item : (List<Object>) node) {
                list.add(rewriteRefs(item, currentFileBase, renameLookup));
            }
            return list;
        }
        return node;
    }

    private static String rewriteComponentPath(
        String fileBase,
        String componentPath,
        Map<String, Map<String, Map<String, String>>> renameLookup
    ) {
        int slash = componentPath.indexOf('/');
        if (slash < 0) {
            return componentPath;
        }

        String section = componentPath.substring(0, slash);
        String rest = componentPath.substring(slash + 1);

        int next = rest.indexOf('/');
        String name = next == -1 ? rest : rest.substring(0, next);
        String tail = next == -1 ? "" : rest.substring(next);

        String resolvedName = resolveRenamedComponent(fileBase, section, name, renameLookup);
        return section + "/" + resolvedName + tail;
    }

    private static String resolveRenamedComponent(
        String fileBase,
        String section,
        String originalName,
        Map<String, Map<String, Map<String, String>>> renameLookup
    ) {
        Map<String, Map<String, String>> fileEntries = renameLookup.get(fileBase);
        if (fileEntries == null) {
            return originalName;
        }

        Map<String, String> sectionEntries = fileEntries.get(section);
        if (sectionEntries == null) {
            return originalName;
        }

        return sectionEntries.getOrDefault(originalName, originalName);
    }

    // Only add the suffix if it's not already there
    private static String maybeSuffix(String name, String suffix) {
        return name.endsWith(suffix) ? name : name + suffix;
    }

    private static String stripYaml(String fileName) {
        return fileName.endsWith(".yaml") ? fileName.substring(0, fileName.length() - 5) : fileName;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getOrCreateSection(Map<String, Object> components, String section) {
        return (Map<String, Object>) components.computeIfAbsent(section, k -> new LinkedHashMap<>());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Map<String, Map<String, String>>> planComponentRenames(List<YamlSource> sources) {
        Map<String, Map<String, Map<String, String>>> renameLookup = new LinkedHashMap<>();
        Map<String, Set<String>> usedNamesBySection = new LinkedHashMap<>();

        for (YamlSource source : sources) {
            Map<String, Object> components = (Map<String, Object>) source.yaml().get("components");
            if (components == null) {
                continue;
            }

            Map<String, Map<String, String>> renamesForFile =
                renameLookup.computeIfAbsent(source.baseName(), key -> new LinkedHashMap<>());

            for (String section : COMPONENT_SECTIONS) {
                Map<String, Object> src = (Map<String, Object>) components.get(section);
                if (src == null) {
                    continue;
                }

                Set<String> usedNames = usedNamesBySection.computeIfAbsent(section, key -> new LinkedHashSet<>());
                Map<String, String> renamesForSection =
                    renamesForFile.computeIfAbsent(section, key -> new LinkedHashMap<>());

                for (String name : src.keySet()) {
                    String resolved = selectComponentName(name, source.suffix(), usedNames, section, source.path());
                    renamesForSection.put(name, resolved);
                    usedNames.add(resolved);
                }
            }
        }

        return renameLookup;
    }

    private static String selectComponentName(
        String originalName,
        String suffix,
        Set<String> usedNames,
        String section,
        Path file
    ) {
        if (!usedNames.contains(originalName)) {
            return originalName;
        }

        String suffixed = maybeSuffix(originalName, suffix);
        if (!usedNames.contains(suffixed)) {
            return suffixed;
        }

        throw new IllegalStateException(
            "Name collision in components/" + section + ": " + originalName + " (from " + file.getFileName() + ")"
        );
    }

    private record YamlSource(Path path, String baseName, String suffix, Map<String, Object> yaml) {}
}
