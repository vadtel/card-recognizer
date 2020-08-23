package com.vadtel.recognizer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    static final Integer grey = new Color(120, 120, 120).getRGB();
    static final Integer white = -1;
    static final Map<String, String> sample = new HashMap<>();

    static { //initializing map with sample data
        try {
            for (File valueFile : getSetFilesFromFolder("artifacts/values")) {
                parseSampleImage(34, 24, valueFile);
            }
            for (File suitFile : getSetFilesFromFolder("artifacts/suit")) {
                parseSampleImage(39, 32, suitFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        for (String path : getFiles(args[0])) {
            try {
                parseImage(new File(path));
            } catch (IOException e) {
                System.out.println("File error " + path);
            }
        }
    }

    private static void parseSampleImage(int sizeX, int sizeY, File file) throws IOException {
        BufferedImage img = ImageIO.read(file);
        StringBuilder binaryString = new StringBuilder();
        for (int y = 1; y < sizeY; y++)
            for (int x = 1; x < sizeX; x++) {
                int rgb = img.getRGB(x, y);
                binaryString.append((rgb == white || rgb == grey) ? " " : "*");
            }
        sample.put(file.getName().replaceFirst("[.][^.]+$", ""), binaryString.toString());
    }

    private static Set<File> getSetFilesFromFolder(String folder) throws IOException {

        return Stream.of(Paths.get(folder).toAbsolutePath().toFile().listFiles())
                .filter(file -> !file.isDirectory())
                .collect(Collectors.toSet());
    }

    private static Set<String> getFiles(String dir) {
        return Stream.of(new File(dir).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getAbsolutePath)
                .collect(Collectors.toSet());
    }

    static void parseImage(File file) throws IOException {
        BufferedImage img = ImageIO.read(file);
        int cardCount = getCardCount(img);
        System.out.printf("%s ", file.getName());
        for (int i = 1; i <= cardCount; i++) {
            System.out.printf("%s%s", getValue(img, i), getSuit(img, i));
        }
        System.out.println();
    }

    private static int getCardCount(BufferedImage img) {
        int count = 0;
        for (int x = 184; x < 472; x += 72) {
            int colour = img.getRGB(x, 630);
            if (colour == white || colour == grey) count++;
        }
        return count;
    }

    static String getValue(BufferedImage img, int pos) {
        String s = stringFromImg(img, 74 + pos * 72, 591, 34, 24);
        return checkSymbol(s);
    }

    static String getSuit(BufferedImage img, int pos) {
        String s = stringFromImg(img, 93 + pos * 72, 634, 39, 32);
        return checkSymbol(s);
    }

    static String stringFromImg(BufferedImage img, int coordX, int coordY, int sizeX, int sizeY) {
        StringBuilder binaryString = new StringBuilder();
        for (int y = coordY; y < coordY + sizeY; y++)
            for (int x = coordX; x < coordX + sizeX; x++) {
                int rgb = img.getRGB(x, y);
                binaryString.append((rgb == white || rgb == grey) ? " " : "*");
            }
        return binaryString.toString();
    }

    static String checkSymbol(String symbol) {
        int min = 1000000;
        String findSymbol = "";
        for (Map.Entry<String, String> entry : Main.sample.entrySet()) {
            int levenshtein = levenshtein(symbol, entry.getValue());
            if (levenshtein < min) {
                min = levenshtein;
                findSymbol = entry.getKey();
            }
        }
        return findSymbol;
    }

    static int levenshtein(String targetStr, String sourceStr) {
        int m = targetStr.length(), n = sourceStr.length();
        int[][] delta = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++)
            delta[i][0] = i;
        for (int j = 1; j <= n; j++)
            delta[0][j] = j;
        for (int j = 1; j <= n; j++)
            for (int i = 1; i <= m; i++) {
                if (targetStr.charAt(i - 1) == sourceStr.charAt(j - 1))
                    delta[i][j] = delta[i - 1][j - 1];
                else
                    delta[i][j] = Math.min(delta[i - 1][j] + 1,
                            Math.min(delta[i][j - 1] + 1, delta[i - 1][j - 1] + 1));
            }
        return delta[m][n];
    }
}
