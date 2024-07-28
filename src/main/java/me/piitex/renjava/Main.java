package me.piitex.renjava;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.archiver.util.DefaultFileSet;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class Main {
    static String color = "orange";
    static boolean clean = true;
    static boolean noconsole = false;
    static boolean next = false;
    static boolean dev = false;
    static boolean distribution = false;

    public static void main(String[] args) {
        System.out.println();
        System.out.println();
        System.out.println("The project uses the Amazon Corretto Java Distribution. By using this application you herby agree to all terms and conditions of using and distributing this software.");
        System.out.println();
        System.out.println();
        System.out.println("RenJava and all utilities and tools are maintained by piitex. Thank you for using the project.");
        System.out.println();
        System.out.println();
        for (String arg : args) {

            if (next) {
                color = arg;
                next = false;
            }

            if (arg.equalsIgnoreCase("--noclean")) {
                System.out.println("No clean argument passed. All downloaded zip files will not be automatically deleted after install.");
                System.out.println("Warn: It is recommended to remove these files before distributing the game as they will increase download size.");
                clean = false;
            }

            if (arg.equalsIgnoreCase("--noconsole")) {
                noconsole = true;
            }

            if (arg.equalsIgnoreCase("--color")) {
                next = true;
            }
            if (arg.equalsIgnoreCase("--distribution") || arg.equalsIgnoreCase("--dist")) {
                distribution = true;
            }
        }

        new Main().init();

    }

    public Main() {

    }

    void init() {
        String noc = "";
        if (noconsole) {
            System.out.println("No console argument passed. Using 'javaw' command instead.");
            noc = "w";
        }

        System.out.println("Checking project environment...");
        File baseDir = new File(System.getProperty("user.dir"));
        File pomFile = new File(baseDir, "pom.xml");
        if (!pomFile.exists()) {
            System.out.println("Could not find project pom file. Please ensure you are running the jar file in the root directory of your project.");
            return;
        }

        String version = "";
        String artifact = "";
        try {
            List<String> lines = Files.readAllLines(pomFile.toPath());
            for (String line : lines) {
                if (line.trim().startsWith("<version>")) {
                    version = line.trim().replace("<version>", "").replace("</version>", "");
                }
                if (line.trim().startsWith("<artifactId>")) {
                    artifact = line.trim().replace("<artifactId>", "").replace("</artifactId>", "");
                }

                if (!artifact.isEmpty() && !version.isEmpty()) {
                    break;
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (version.isEmpty() || artifact.isEmpty()) {
            System.out.println("ERROR: Could not find project information.");
            return;
        }

        File workingDirectory = new File(baseDir, artifact + "-" + version + "/");
        workingDirectory.mkdir();

        File gameDirectory = new File(workingDirectory, "/game/");
        gameDirectory.mkdir();

        File renJavaDirectory = new File(workingDirectory, "/renjava/");
        renJavaDirectory.mkdir();

        File imageDirectory = new File(gameDirectory, "/images/");
        imageDirectory.mkdir();

        File guiDirectory = new File(imageDirectory, "/gui/");
        guiDirectory.mkdir();

        File cssDirectory = new File(gameDirectory, "/css/");
        cssDirectory.mkdir();

        File audioDirectory = new File(gameDirectory, "/audio/");
        audioDirectory.mkdir();

        File fontDirectory = new File(gameDirectory, "/fonts/");
        fontDirectory.mkdir();

        File savesDirectory = new File(gameDirectory, "/saves/");
        savesDirectory.mkdir();

        System.out.println("Created game directory. Please place assets into the appropriate folders.");

        System.out.println(artifact + "-" + version + ".jar");
        File jarFile = new File(baseDir, "/target/" + artifact + "-" + version + ".jar");
        if (!jarFile.exists()) {
            System.out.println("Could not locate jar file. Please place jar file into the new project folder.");
        } else {
            try {
                copyFile(jarFile, new File(workingDirectory, jarFile.getName()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Installing Linux JDK...");
        File linuxFile = new File(workingDirectory, "amazon-corretto-17-x64-linux-jdk.tar.gz");
        if (!linuxFile.exists()) {
            System.out.println("Downloading...");
            try (BufferedInputStream in = new BufferedInputStream(new URL("https://corretto.aws/downloads/latest/amazon-corretto-21-x64-linux-jdk.tar.gz").openStream());
                FileOutputStream fileOutputStream = new FileOutputStream(new File(workingDirectory, "amazon-corretto-17-x64-linux-jdk.tar.gz"))) {
                byte dataBuffer[] = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
            } catch (IOException e) {
                // handle exception
                System.out.println("Could not download: " + e.getMessage());
            }
        } else {
            System.out.println("Linux file exists. TODO: Check to see if the file is completely downloaded.");
        }

        System.out.println("Installing Windows JDK...");
        File windowsFile = new File(workingDirectory, "amazon-corretto-17-x64-windows-jdk.zip");
        if (!windowsFile.exists()) {
            try (BufferedInputStream in = new BufferedInputStream(new URL("https://corretto.aws/downloads/latest/amazon-corretto-21-x64-windows-jdk.zip").openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(new File(workingDirectory, "amazon-corretto-17-x64-windows-jdk.zip"))) {
                byte dataBuffer[] = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
            } catch (IOException e) {
                // handle exception
            }
        }

        System.out.println("Installing MacOS JDK...");
        File macFile = new File(workingDirectory, "amazon-corretto-17-x64-macos-jdk.tar.gz");
        if (!macFile.exists()) {
            try (BufferedInputStream in = new BufferedInputStream(new URL("https://corretto.aws/downloads/latest/amazon-corretto-21-x64-macos-jdk.tar.gz").openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(new File(workingDirectory, "amazon-corretto-17-x64-macos-jdk.tar.gz"))) {
                byte dataBuffer[] = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
            } catch (IOException e) {
                // handle exception
            }
        }

        if (!dev) {
            System.out.println("Unzipping Linux jdk...");
            File jdkDirectory = new File(workingDirectory, "jdk/linux/");
            jdkDirectory.mkdirs();
            TarGZipUnArchiver ua = new TarGZipUnArchiver();
            ua.setSourceFile(linuxFile);
            ua.setDestDirectory(jdkDirectory);
            ua.extract();
            System.out.println("Finished unzip.");
            System.out.println("Cleaning up...");
            if (clean)
                linuxFile.delete();
            try {
                // Instead of using static names retirve the name from the list of directory.
                String extractedDirectoryName = jdkDirectory.list((dir, name) -> name.startsWith("amazon"))[0];
                System.out.println("Detected directory: " + extractedDirectoryName);
                copyDirectory(new File(jdkDirectory, extractedDirectoryName), jdkDirectory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            deleteDirectory(new File(jdkDirectory, "amazon-corretto-17.0.11.9.1-linux-x64"));
            System.out.println("Creating executable files...");
            File startSH = new File(workingDirectory, "start.sh");
            try {
                FileWriter writer = new FileWriter(startSH, false);
                writer.write("\"jdk\\linux\\bin\\java\"" + noc + " -jar " + jarFile.getName());
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Unzipping Windows JDK...");
            jdkDirectory = new File(workingDirectory, "jdk/windows/");
            jdkDirectory.mkdir();
            ZipUnArchiver zipUnArchiver = new ZipUnArchiver();
            zipUnArchiver.setSourceFile(windowsFile);
            zipUnArchiver.setDestDirectory(jdkDirectory);
            zipUnArchiver.extract();
            System.out.println("Finished unzip.");
            System.out.println("Cleaning up...");
            if (clean)
                windowsFile.delete();
            try {
                String extractedDirectoryName = jdkDirectory.list((dir, name) -> name.startsWith("amazon") || name.startsWith("jdk"))[0];
                System.out.println("Detected directory: " + extractedDirectoryName);
                copyDirectory(new File(jdkDirectory, extractedDirectoryName), jdkDirectory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            deleteDirectory(new File(jdkDirectory, "jdk17.0.11_9"));
            System.out.println("Creating executable files...");
            File startBat = new File(workingDirectory, "start.bat");
            try {
                FileWriter writer = new FileWriter(startBat, false);
                writer.write("\"jdk\\windows\\bin\\java" + noc + ".exe\" -jar " + jarFile.getName());
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Unzipping MacOS JDK...");
            jdkDirectory = new File(workingDirectory, "jdk/macos/");
            jdkDirectory.mkdir();
            ua = new TarGZipUnArchiver();
            ua.setSourceFile(macFile);
            ua.setDestDirectory(jdkDirectory);
            ua.extract();
            System.out.println("Finished unzip.");
            System.out.println("Cleaning up...");
            if (clean)
                macFile.delete();
            try {
                String extractedDirectoryName = jdkDirectory.list((dir, name) -> name.startsWith("amazon"))[0];
                System.out.println("Detected directory: " + extractedDirectoryName);
                copyDirectory(new File(jdkDirectory, extractedDirectoryName), jdkDirectory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            deleteDirectory(new File(jdkDirectory, "amazon-corretto-17.jdk"));
        }

        System.out.println("Extracting GUI...");
        System.out.println("Setting color to " + color);
        // Check if the gui directory is empty
        Collection<String> files = getResources(Pattern.compile(".*"));
        if (guiDirectory.listFiles().length == 0) {
            System.out.println("Directory is empty extracting...");
            for (String s : files) {
                if (s.startsWith("gui/" + color)) {
                    System.out.println(s);
                    File file;
                    if (s.endsWith("/")) {
                        file = new File(imageDirectory, s.replace("/" + color, "") + "/");
                    } else {
                        file = new File(imageDirectory, s.replace("/" + color, ""));
                    }
                    if (file.isDirectory() || file.getName().endsWith("/") || !file.getName().contains(".")) {
                        file.mkdir();
                    } else {
                        try {
                            IOUtils.copy(Main.class.getClassLoader().getResourceAsStream(s), new FileOutputStream(file));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }

        System.out.println("Extracting CSS files...");
        if (cssDirectory.listFiles().length == 0) {
            for (String s : files) {
                if (s.startsWith("css/")) {
                    System.out.println(s);
                    File file;
                    if (s.endsWith("/")) {
                        file = new File(gameDirectory, s + "/");
                    } else {
                        file = new File(gameDirectory, s);
                    }
                    if (file.isDirectory() || file.getName().endsWith("/") || !file.getName().contains(".")) {
                        file.mkdir();
                    } else {
                        try {
                            IOUtils.copy(Main.class.getClassLoader().getResourceAsStream(s), new FileOutputStream(file));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }

        System.out.println("Finished extracting default assets.");
        if (distribution) {
            System.out.println("Creating distributable(s)...");
            System.out.println("Windows distributable...");
            createDistributable("windows", workingDirectory, baseDir, linuxFile, macFile, windowsFile, version, artifact);
            System.out.println("Linux distributable...");
            createDistributable("linux", workingDirectory, baseDir, linuxFile, macFile, windowsFile, version, artifact);
            System.out.println("MacOS distributable...");
            createDistributable("macos", workingDirectory, baseDir, linuxFile, macFile, windowsFile, version, artifact);
        }

        System.out.println("Done.");
    }

    private void createDistributable(String os, File workingDirectory, File baseDir, File linuxFile, File macFile, File windowsFile, String version, String artifact) {
        File currentDistribution = new File(baseDir, os + "-distribution/" + workingDirectory.getName() + "/");
        currentDistribution.mkdirs();
        try {
            copyDirectory(workingDirectory, currentDistribution);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (os.equalsIgnoreCase("windows") || os.equalsIgnoreCase("macos")) {
            File linuxJDK = new File(currentDistribution, "/jdk/linux/");
            deleteDirectory(linuxJDK);
        }

        // Always delete
        File linuxZip = new File(currentDistribution, linuxFile.getName());
        linuxZip.delete();


        if (os.equalsIgnoreCase("linux") || os.equalsIgnoreCase("macos")) {
            File windowsJDK = new File(currentDistribution, "/jdk/windows/");
            deleteDirectory(windowsJDK);
            File batFile = new File(currentDistribution, "start.bat");
            batFile.delete();
        }

        File windowsZip = new File(currentDistribution, windowsFile.getName());
        windowsZip.delete();

        if (os.equalsIgnoreCase("windows") || os.equalsIgnoreCase("linux")) {
            File macOS = new File(currentDistribution, "/jdk/macos/");
            deleteDirectory(macOS);
        }

        File macZip = new File(currentDistribution, macFile.getName());
        macZip.delete();

        if (os.equalsIgnoreCase("windows")) {
            File shFile = new File(currentDistribution, "start.sh");
            shFile.delete();
        }

        ZipArchiver archiver = new ZipArchiver();
        archiver.setDestFile(new File(baseDir, artifact + "-" + version + "-" + os + ".zip"));
        archiver.setCompress(true);
        archiver.setIncludeEmptyDirs(true);
        archiver.addFileSet(new DefaultFileSet(new File(os + "-distribution/")));
        try {
            archiver.createArchive();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Cleaning up...");
        deleteDirectory(new File(os + "-distribution/"));
    }

    private static void copyDirectory(File sourceDirectory, File destinationDirectory) throws IOException {
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdir();
        }
        for (String f : sourceDirectory.list()) {
            copyDirectoryCompatibityMode(new File(sourceDirectory, f), new File(destinationDirectory, f));
        }
    }

    public static void copyDirectoryCompatibityMode(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            copyDirectory(source, destination);
        } else {
            copyFile(source, destination);
        }
    }

    private static void copyFile(File sourceFile, File destinationFile)
            throws IOException {
        try (InputStream in = new FileInputStream(sourceFile);
             OutputStream out = new FileOutputStream(destinationFile)) {
            byte[] buf = new byte[1024];
            int length;
            while ((length = in.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
        }
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    /**
     * for all elements of java.class.path get a Collection of resources Pattern
     * pattern = Pattern.compile(".*"); gets all resources
     *
     * @param pattern the pattern to match
     * @return the resources in the order they are found
     */
    public static Collection<String> getResources(final Pattern pattern) {
        final ArrayList<String> retval = new ArrayList<String>();
        final String classPath = System.getProperty("java.class.path", ".");
        final String[] classPathElements = classPath.split(System.getProperty("path.separator"));
        for (final String element : classPathElements) {
            retval.addAll(getResources(element, pattern));
        }
        return retval;
    }

    private static Collection<String> getResources(final String element, final Pattern pattern) {
        final ArrayList<String> retval = new ArrayList<String>();
        final File file = new File(element);
        if (file.isDirectory()) {
            retval.addAll(getResourcesFromDirectory(file, pattern));
        } else {
            retval.addAll(getResourcesFromJarFile(file, pattern));
        }
        return retval;
    }

    private static Collection<String> getResourcesFromJarFile(final File file, final Pattern pattern) {
        final ArrayList<String> retval = new ArrayList<String>();
        ZipFile zf;
        try {
            zf = new ZipFile(file);
        } catch (final ZipException e) {
            throw new Error(e);
        } catch (final IOException e) {
            throw new Error(e);
        }
        final Enumeration e = zf.entries();
        while (e.hasMoreElements()) {
            final ZipEntry ze = (ZipEntry) e.nextElement();
            final String fileName = ze.getName();
            final boolean accept = pattern.matcher(fileName).matches();
            if (accept) {
                retval.add(fileName);
            }
        }
        try {
            zf.close();
        } catch (final IOException e1) {
            throw new Error(e1);
        }
        return retval;
    }

    private static Collection<String> getResourcesFromDirectory(final File directory, final Pattern pattern) {
        final ArrayList<String> retval = new ArrayList<String>();
        final File[] fileList = directory.listFiles();
        for (final File file : fileList) {
            if (file.isDirectory()) {
                retval.addAll(getResourcesFromDirectory(file, pattern));
                file.mkdir();
            } else {
                try {
                    final String fileName = file.getCanonicalPath();
                    final boolean accept = pattern.matcher(fileName).matches();
                    if (accept) {
                        retval.add(fileName);
                    }
                } catch (final IOException e) {
                    throw new Error(e);
                }
            }
        }
        return retval;
    }
}