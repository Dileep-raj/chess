package com.drdedd.chess.engine;

import com.drdedd.chess.misc.MiscMethods;
import lombok.Getter;

import java.util.HashMap;

public class HardwareInfo {

    private final HashMap<String, String> infoMap;
    public static final String UNKNOWN = "?", OS = "os", LOGICAL_CORES = "logicalCores", FREE_MEMORY = "freeMemory", MAX_MEMORY = "maxMemory", TOTAL_MEMORY = "totalMemory";
    @Getter
    private final long maxMemory, freeMemory, totalMemory;
    @Getter
    private final int availableProcessors;
    @Getter
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

    private static String convertBytes(long bytes) {
        return MiscMethods.convertBytes(bytes) + "B";
    }
}
