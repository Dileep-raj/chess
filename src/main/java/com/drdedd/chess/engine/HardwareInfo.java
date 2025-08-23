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
        infoMap.put(MAX_MEMORY, (maxMemory == Long.MAX_VALUE ? "no limit" : MiscMethods.formatBytes(maxMemory)));
        infoMap.put(OS, OSName);
        infoMap.put(LOGICAL_CORES, String.valueOf(availableProcessors));
        infoMap.put(FREE_MEMORY, MiscMethods.formatBytes(freeMemory));
        infoMap.put(TOTAL_MEMORY, MiscMethods.formatBytes(totalMemory));
    }

    public String getProperty(String propertyName) {
        return infoMap.getOrDefault(propertyName, UNKNOWN);
    }

    public int maximumSafeThreads() {
        try {
            return availableProcessors > 2 ? availableProcessors - 1 : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    @Override
    public String toString() {
        return ("OS name: %s\nAvailable processors (cores): %s\nFree memory: %s\nMaximum memory: %s\nTotal memory available to JVM: %s").formatted(infoMap.getOrDefault(OS, UNKNOWN), infoMap.getOrDefault(LOGICAL_CORES, UNKNOWN), infoMap.getOrDefault(FREE_MEMORY, UNKNOWN), infoMap.getOrDefault(MAX_MEMORY, UNKNOWN), infoMap.getOrDefault(TOTAL_MEMORY, UNKNOWN));
    }
}
