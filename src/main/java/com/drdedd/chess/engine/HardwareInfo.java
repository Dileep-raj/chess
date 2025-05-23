package com.drdedd.chess.engine;

import com.drdedd.chess.misc.MiscMethods;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class HardwareInfo {

    private final HashMap<String, String> infoMap;
    public static final String UNKNOWN = "?", OS = "os", LOGICAL_CORES = "logicalCores", FREE_MEMORY = "freeMemory", MAX_MEMORY = "maxMemory", TOTAL_MEMORY = "totalMemory";
    private final long maxMemory, freeMemory, totalMemory;
    private final int availableProcessors;
    private final String OSName, OSArch, vendor, username;

    public HardwareInfo() {
        Runtime runtime = Runtime.getRuntime();

        maxMemory = runtime.maxMemory();
        freeMemory = runtime.freeMemory();
        totalMemory = runtime.totalMemory();
        availableProcessors = runtime.availableProcessors();
        OSName = System.getProperty("os.name");
        OSArch = System.getProperty("os.arch");
        vendor = System.getProperty("java.vendor");
        username = System.getProperty("user.name");

        infoMap = new HashMap<>();
        infoMap.put(MAX_MEMORY, (maxMemory == Long.MAX_VALUE ? "no limit" : convertBytes(maxMemory)));
        infoMap.put(OS, OSName);
        infoMap.put(LOGICAL_CORES, String.valueOf(availableProcessors));
        infoMap.put(FREE_MEMORY, convertBytes(freeMemory));
        infoMap.put(TOTAL_MEMORY, convertBytes(totalMemory));
    }

    public String getProperty(String propertyName) {
        return infoMap.getOrDefault(propertyName, UNKNOWN);
    }

    public void printInfo() {
        System.out.println("OS name: " + infoMap.getOrDefault(OS, UNKNOWN));
        System.out.println("Available processors (cores): " + infoMap.getOrDefault(LOGICAL_CORES, UNKNOWN));
        System.out.println("Free memory: " + infoMap.getOrDefault(FREE_MEMORY, UNKNOWN));
        System.out.println("Maximum memory: " + infoMap.getOrDefault(MAX_MEMORY, UNKNOWN));
        System.out.println("Total memory available to JVM: " + infoMap.getOrDefault(TOTAL_MEMORY, UNKNOWN));
    }

    public int maximumSafeThreads() {
        try {
            return availableProcessors > 2 ? availableProcessors - 1 : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public long getFreeMemory() {
        return freeMemory;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public int getAvailableProcessors() {
        return availableProcessors;
    }

    public String getOSName() {
        return OSName;
    }

    public String getOSArch() {
        return OSArch;
    }

    public String getVendor() {
        return vendor;
    }

    public String getUsername() {
        return username;
    }

    private static String convertBytes(long bytes) {
        return MiscMethods.convertBytes(bytes) + "B";
    }

    public static void main(String[] args) {
        HardwareInfo hardwareInfo = new HardwareInfo();
        hardwareInfo.printInfo();

        System.out.println("-".repeat(50));
        Properties properties = System.getProperties();
        properties.list(System.out);
        System.out.println("-".repeat(50));

        File[] roots = File.listRoots();
        System.out.printf("%-5s %-15s %-15s %-15s%n", "Root", "Total space", "Free space", "Usable space");
        for (File root : roots)
            System.out.printf("%-5s %-15s %-15s %-15s%n", root.getAbsolutePath(), convertBytes(root.getTotalSpace()), convertBytes(root.getFreeSpace()), convertBytes(root.getUsableSpace()));

        System.out.println("\n\nTest bytes:");
        List<Long> testBytes = List.of(0L, 1L, 512L, 1023L, 1024L, 1048576L, 1073741824L, 1099511627776L, 1125899906842624L, 1152921504606846976L, Long.MAX_VALUE);
        for (long test : testBytes) System.out.printf("%,25d B: %10s%n", test, convertBytes(test));
    }
}