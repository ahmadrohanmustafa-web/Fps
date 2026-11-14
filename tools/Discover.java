import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.spi.ToolProvider;
import java.util.stream.*;
import java.util.zip.*;

/**
 * VOID FPS target discovery. Reads the REAL Minecraft jar that Loom downloaded and writes
 * voidfps-target-report.txt: class lists + method signatures for the rendering classes that
 * optimizations would target, plus checks for the Minecraft APIs this mod already uses.
 *
 * Run (JDK 25, from the project folder):   java tools/Discover.java [path-to-minecraft.jar]
 * Needs no network and changes nothing.
 */
public class Discover {
    static final String[] EXACT = {
        "net/minecraft/client/Minecraft",
        "net/minecraft/world/level/block/Block",
        "net/minecraft/world/level/block/state/BlockBehaviour",
        "net/minecraft/client/renderer/block/ModelBlockRenderer",
        "net/minecraft/client/renderer/block/BlockRenderDispatcher",
        "net/minecraft/client/renderer/chunk/SectionCompiler",
        "net/minecraft/client/renderer/LevelRenderer",
        "net/minecraft/client/renderer/entity/EntityRenderDispatcher",
        "net/minecraft/client/renderer/culling/Frustum",
    };
    static final String[] KEYWORDS = {
        "SectionCompiler", "SectionRender", "ChunkRender", "SectionMesh", "ModelBlockRenderer",
        "BlockRenderDispatcher", "LevelRenderer", "Frustum", "Occlusion", "VisibilitySet",
        "FaceCulling", "BlockModelPart", "QuadCollection", "TerrainRender", "EntityRenderDispatcher"
    };
    static final String[] DIR_LISTS = {
        "net/minecraft/client/renderer/chunk/",
        "net/minecraft/client/renderer/block/",
    };

    public static void main(String[] args) throws Exception {
        Path jar = args.length > 0 ? Path.of(args[0]) : findMinecraftJar();
        if (jar == null) {
            System.err.println("Could not find the Minecraft jar. Run 'gradle build' (or genSources) once first,\n"
                + "then re-run, or pass the jar path:  java tools/Discover.java <path-to-minecraft.jar>");
            System.exit(2);
        }
        System.out.println("Using jar: " + jar);

        TreeSet<String> classes = new TreeSet<>();
        try (ZipFile z = new ZipFile(jar.toFile())) {
            z.stream().map(ZipEntry::getName).filter(n -> n.endsWith(".class"))
                .forEach(n -> classes.add(n.substring(0, n.length() - 6)));
        }

        ToolProvider javap = ToolProvider.findFirst("javap").orElse(null);
        if (javap == null) {
            System.err.println("javap not available: run with a full JDK (java 25), not a JRE.");
            System.exit(3);
        }

        StringBuilder r = new StringBuilder();
        r.append("VOID FPS target report\njar: ").append(jar).append("\nclasses in jar: ").append(classes.size()).append("\n\n");

        r.append("== Existence of expected classes ==\n");
        for (String c : EXACT) r.append(classes.contains(c) ? "FOUND   " : "MISSING ").append(c).append('\n');

        r.append("\n== Keyword matches under net/minecraft/client (rendering-related names) ==\n");
        for (String c : classes) {
            if (!c.startsWith("net/minecraft/client/")) continue;
            for (String k : KEYWORDS) if (c.contains(k)) { r.append(c).append('\n'); break; }
        }

        r.append("\n== Direct contents of key packages ==\n");
        for (String d : DIR_LISTS) {
            r.append("-- ").append(d).append('\n');
            for (String c : classes) if (c.startsWith(d) && c.indexOf('/', d.length()) < 0) r.append(c).append('\n');
        }

        r.append("\n== API checks for code already in this mod ==\n");
        String mc = javap(javap, jar, "net.minecraft.client.Minecraft", "-p");
        r.append(mc.contains(" getFps()") ? "OK      " : "MISSING ").append("Minecraft.getFps()  (used by BenchmarkRecorder)\n");
        r.append(mc.matches("(?s).*\\blevel;.*") ? "OK      " : "MISSING ").append("Minecraft.level field  (used by BenchmarkRecorder)\n");

        r.append("\n== Method signatures (javap -p) of key classes ==\n");
        LinkedHashSet<String> dump = new LinkedHashSet<>(Arrays.asList(EXACT));
        for (String c : classes) if (c.startsWith("net/minecraft/client/renderer/chunk/") && c.indexOf('/', 37) < 0) dump.add(c);
        int n = 0;
        for (String c : dump) {
            if (!classes.contains(c) || ++n > 60) continue;
            if (c.equals("net/minecraft/client/Minecraft")) continue; // huge; getFps/level already checked above
            r.append("\n#### ").append(c).append('\n').append(javap(javap, jar, c.replace('/', '.'), "-p"));
        }

        r.append("\n== Bytecode of Block.shouldRenderFace (vanilla hidden-face culling) ==\n");
        String blk = javap(javap, jar, "net.minecraft.world.level.block.Block", "-p", "-c");
        r.append(extractMethod(blk, "shouldRenderFace"));

        Path out = Path.of("voidfps-target-report.txt");
        Files.writeString(out, r.toString(), StandardCharsets.UTF_8);
        System.out.println("Wrote " + out.toAbsolutePath() + " (" + r.length() + " chars). Send that file back.");
    }

    static String javap(ToolProvider javap, Path jar, String cls, String... flags) {
        List<String> a = new ArrayList<>(Arrays.asList(flags));
        a.add("-cp"); a.add(jar.toString()); a.add(cls);
        StringWriter out = new StringWriter(), err = new StringWriter();
        int rc = javap.run(new PrintWriter(out), new PrintWriter(err), a.toArray(new String[0]));
        return rc == 0 ? out.toString() : "(javap failed for " + cls + ": " + err + ")\n";
    }

    static String extractMethod(String javapC, String name) {
        StringBuilder sb = new StringBuilder();
        boolean in = false;
        for (String line : javapC.split("\n")) {
            boolean header = line.startsWith("  ") && !line.startsWith("   ") && line.contains("(");
            if (header) in = line.contains(" " + name + "(");
            if (in) sb.append(line).append('\n');
        }
        return sb.length() == 0 ? "(method " + name + " not found in Block)\n" : sb.toString();
    }

    static Path findMinecraftJar() throws IOException {
        List<Path> roots = List.of(Path.of(System.getProperty("user.home"), ".gradle", "caches"),
                                   Path.of(".gradle"), Path.of("build"), Path.of("run"));
        Path best = null; long bestSize = -1;
        for (Path root : roots) {
            if (!Files.isDirectory(root)) continue;
            try (Stream<Path> s = Files.walk(root, 12)) {
                for (Path p : (Iterable<Path>) s.filter(Files::isRegularFile)::iterator) {
                    String f = p.getFileName().toString().toLowerCase(Locale.ROOT);
                    if (!f.endsWith(".jar") || f.contains("sources") || !f.contains("minecraft")) continue;
                    try (ZipFile z = new ZipFile(p.toFile())) {
                        if (z.getEntry("net/minecraft/client/Minecraft.class") == null) continue;
                    } catch (IOException e) { continue; }
                    long size = Files.size(p);
                    if (size > bestSize) { best = p; bestSize = size; }
                }
            }
        }
        return best;
    }
}
