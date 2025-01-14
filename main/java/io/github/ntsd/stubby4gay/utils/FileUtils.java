/*
A Java-based HTTP stub server

Copyright (C) 2012 Alexander Zagniotov, Isa Goksu and Eric Mrak

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.ntsd.stubby4gay.utils;

import io.github.ntsd.stubby4gay.annotations.CoberturaIgnore;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Alexander Zagniotov
 * @since 11/8/12, 8:30 AM
 */
@SuppressWarnings("serial")
public final class FileUtils {

    public static final String BR = System.lineSeparator();
    private static final Set<String> ASCII_TYPES = Collections.unmodifiableSet(
            new HashSet<>(
                    Arrays.asList(
                            ".ajx", ".am", ".asa", ".asc", ".asp", ".aspx", ".awk", ".bat",
                            ".c", ".cdf", ".cf", ".cfg", ".cfm", ".cgi", ".cnf", ".conf", ".cpp",
                            ".css", ".csv", ".ctl", ".dat", ".dhtml", ".diz", ".file", ".forward",
                            ".grp", ".h", ".hpp", ".hqx", ".hta", ".htaccess", ".htc", ".htm", ".html",
                            ".htpasswd", ".htt", ".htx", ".in", ".inc", ".info", ".ini", ".ink", ".java",
                            ".js", ".json", ".jsp", ".log", ".logfile", ".m3u", ".m4", ".m4a", ".mak",
                            ".map", ".model", ".msg", ".nfo", ".nsi", ".info", ".old", ".pas", ".patch",
                            ".perl", ".php", ".php2", ".php3", ".php4", ".php5", ".php6", ".phtml", ".pix",
                            ".pl", ".pm", ".po", ".pwd", ".py", ".qmail", ".rb", ".rbl", ".rbw", ".readme",
                            ".reg", ".rss", ".rtf", ".ruby", ".session", ".setup", ".sh", ".shtm", ".shtml",
                            ".sql", ".ssh", ".stm", ".style", ".svg", ".tcl", ".text", ".threads", ".tmpl",
                            ".tpl", ".txt", ".ubb", ".vbs", ".xhtml", ".xml", ".xrc", ".xsl", ".yaml", ".yml"
                    )
            )
    );
    private static final String LINE_SEPARATOR_UNIX = "\n";
    private static final String LINE_SEPARATOR_MAC_OS_PRE_X = "\r";
    private static final String LINE_SEPARATOR_WINDOWS = "\r\n";
    private static final String LINE_SEPARATOR_TOKEN = "[_T_O_K_E_N_]";

    private FileUtils() {

    }

    public static File uriToFile(final String dataYamlConfigParentDir, final String relativePath) throws IOException {
        final File contentFile = new File(dataYamlConfigParentDir, relativePath);

        if (!contentFile.isFile()) {
            throw new IOException(String.format("Could not load file from path: %s", relativePath));
        }

        return contentFile;
    }

    public static File uriToFile(final String absolutePath) throws IOException {
        final File contentFile = new File(absolutePath);

        if (!contentFile.isFile()) {
            throw new IOException(String.format("Could not load file from path: %s", absolutePath));
        }

        return contentFile;
    }

    @CoberturaIgnore
    public static File tempFileFromString(final String content) throws IOException {
        final File temp = File.createTempFile("tmp" + System.currentTimeMillis(), ".txt");
        temp.deleteOnExit();

        try (final FileWriter fileWriter = new FileWriter(temp);
             final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(content);
        }

        return temp;
    }

    public static boolean isTemplateFile(final File file) throws IOException {
        return isCharacterFile(file) && StringUtils.isTokenized(characterFileToString(file));
    }

    public static boolean isFilePathContainTemplateTokens(final File file) {
        return StringUtils.isTokenized(file.getAbsolutePath());
    }

    public static byte[] fileToBytes(final File file) throws IOException {
        if (isCharacterFile(file)) {
            return characterFileToUtf8Bytes(file);
        }
        return binaryFileToBytes(file);
    }


    @CoberturaIgnore
    static byte[] binaryFileToBytes(final String dataYamlConfigParentDir, final String relativePath) throws IOException {
        final File contentFile = new File(dataYamlConfigParentDir, relativePath);

        if (!contentFile.isFile()) {
            throw new IOException(String.format("Could not load file from path: %s", relativePath));
        }

        return Files.readAllBytes(Paths.get(contentFile.toURI()));
    }


    @CoberturaIgnore
    static byte[] binaryFileToBytes(final File file) throws IOException {
        return Files.readAllBytes(Paths.get(file.toURI()));
    }


    public static String enforceSystemLineSeparator(final String loadedContent) {
        if (!StringUtils.isSet(loadedContent)) {
            return "";
        }

        return loadedContent
                .replace(LINE_SEPARATOR_WINDOWS, LINE_SEPARATOR_TOKEN)
                .replace(LINE_SEPARATOR_MAC_OS_PRE_X, LINE_SEPARATOR_TOKEN)
                .replace(LINE_SEPARATOR_UNIX, LINE_SEPARATOR_TOKEN)
                .replace(LINE_SEPARATOR_TOKEN, BR);
    }

    public static InputStream constructInputStream(final File file) throws IOException {
        return makeBuffered(Files.newInputStream(Paths.get(file.toURI())));
    }

    public static InputStream constructInputStream(final String content) throws IOException {
        return makeBuffered(new ByteArrayInputStream(content.getBytes(StringUtils.charsetUTF8())));
    }

    static InputStream makeBuffered(final InputStream inputStream) {
        return new BufferedInputStream(inputStream);
    }

    private static String characterFileToString(final File file) throws IOException {
        try (InputStream inputStream = constructInputStream(file)) {
            final String loadedContent = StringUtils.inputStreamToString(inputStream);
            return enforceSystemLineSeparator(loadedContent);
        }
    }

    private static byte[] characterFileToUtf8Bytes(final File file) throws IOException {
        final String loadedContent = characterFileToString(file);

        return StringUtils.getBytesUtf8(loadedContent);
    }

    private static boolean isCharacterFile(final File file) throws IOException {
        return ASCII_TYPES.contains(StringUtils.extractFilenameExtension(file.getName()));
    }
}
