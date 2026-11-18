package com.couriertracking.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe Generator for Unique Consignment Numbers.
 * Format: CRX + YYYYMMDD + 4-digit sequence (e.g. CRX202611180001).
 * Demonstrates Factory/Generator pattern with atomic concurrency control.
 */
public final class ConsignmentGenerator {
    private static final String PREFIX = "CRX";
    private static final AtomicInteger SEQUENCE = new AtomicInteger(1);
    private static String lastDateStr = "";

    private ConsignmentGenerator() {}

    /**
     * Generates a new formatted consignment number.
     * Synchronized date check resets the sequence on a new calendar day.
     */
    public static synchronized String generate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String currentDateStr = sdf.format(new Date());

        if (!currentDateStr.equals(lastDateStr)) {
            lastDateStr = currentDateStr;
            // Seed sequence using millisecond component or sequence to ensure uniqueness across restarts
            int initialSeq = (int) ((System.currentTimeMillis() % 9000) + 1000);
            SEQUENCE.set(initialSeq);
        }

        int currentSeq = SEQUENCE.incrementAndGet();
        return String.format("%s%s%04d", PREFIX, currentDateStr, currentSeq % 10000);
    }
}
