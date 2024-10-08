package org.example.model;

import org.example.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MapOfIntToInt extends StateVar {
    public ConcurrentHashMap<Long, AtomicLong> hashMap;
    public ArrayList<Long> keys;

    public ArrayList<SimpleFuncOfMapIntToIntRead> simpleFuncOfMapIntToIntReads;

    public MapOfIntToInt() {
        this.hashMap = new ConcurrentHashMap<>();
        this.name = Util.getRandomString();
        this.keys = new ArrayList<>();
        this.simpleFuncOfMapIntToIntReads = new ArrayList<>();
        HashSet<Long> set = new HashSet<>();

        while (keys.size() < 2) {
            long randomNumber = Util.random.nextInt(1000);
            if (!set.contains(randomNumber)) {
                keys.add(randomNumber);
                set.add(randomNumber);
                hashMap.put(randomNumber, new AtomicLong());
            }
        }
    }

    public void addReadFunc(SimpleFuncOfMapIntToIntRead func) {
        simpleFuncOfMapIntToIntReads.add(func);
    }


    public String init() {
        return String.format("    mapping(int => int) public %s;\n", this.name);
    }

    @Override
    public void clear() {
        hashMap = new ConcurrentHashMap<>();
        for (Long key : keys) {
            hashMap.put(key, new AtomicLong());
        }
    }

    /*
    modify(a, b) => map[a] += b
     */
    public void modify(long key, long val) {
        hashMap.compute(key, (k, v) -> {
            if (v == null) {
                return new AtomicLong(val);
            } else {
                v.addAndGet(val);
                return v;
            }
        });
    }

    @Override
    public boolean check() {
        HashMap<Long, Long> map = new HashMap<>();
        for (SimpleFuncOfMapIntToIntRead func : simpleFuncOfMapIntToIntReads) {
            try {
                map.put(func.key, func.getVal());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (map.size() != hashMap.size()) {
            System.out.printf(
                    "error of inconsistent size of mapping %s , expected %d, but real is %d\n",
                    name, hashMap.size(), map.size()
            );
            return false;
        }
        for (Map.Entry<Long, AtomicLong> longAtomicLongEntry : hashMap.entrySet()) {
            Long key = longAtomicLongEntry.getKey();
            if (!map.containsKey(key)) {
                System.out.printf(
                        "error of inconsistent key of mapping %s , expected not %d, but real has %d\n",
                        name, key, key
                );
                return false;
            }
            if (map.get(key) != longAtomicLongEntry.getValue().longValue()) {
                System.out.printf(
                        "error of inconsistent value of mapping %s's key %d, expected %d, but real is %d\n",
                        name, key,  longAtomicLongEntry.getValue().longValue(), map.get(key)
                );
                return false;
            }
        }
        for (Map.Entry<Long, Long> longLongEntry : map.entrySet()) {
            Long key = longLongEntry.getKey();
            if (!hashMap.containsKey(key)) {
                System.out.printf(
                        "error of inconsistent key of mapping %s , expected has %d, but real has not %d\n",
                        name, key, key
                );
                return false;
            }
            if (hashMap.get(key).longValue() != longLongEntry.getValue()) {
                System.out.printf(
                        "error of inconsistent value of mapping %s's key %d, expected %d, but real is %d\n",
                        name, key, hashMap.get(key).longValue(), longLongEntry.getValue()
                );
                return false;
            }
        }
        return true;
    }

}
