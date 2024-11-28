package com.example.server;

import lombok.Getter;

import java.util.*;

@Getter
public class TimeTest {
    private static final Map<Integer, Long> timeMap = new HashMap<>();

    public static void addTime(int num, long time) {
        timeMap.put(num, time);
    }

    public static void prtMap() {
        List<Integer> nums = timeMap.keySet().stream().sorted().toList();
        for (int i = 0; i < nums.size()-1; i++) {
            System.out.printf("%d-%d: %d\n", nums.get(i),nums.get(i+1), timeMap.get(nums.get(i+1))-timeMap.get(nums.get(i)));
        }
    }

}
