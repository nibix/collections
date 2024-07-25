/*
 * Copyright 2024 Nils Bandener
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.selectivem.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Enclosed.class)
public class HashingTest {

    static final TestValues testValues = new TestValues();

    @RunWith(Parameterized.class)
    public static class ByTableSizeAndTestData {
        final int tableSize;
        final short maxProbingDistance;
        final TestDataFactory testDataFactory;

        @Test
        public void hashingDistribution() {
            System.out.println("## hashingDistribution; table size: " + tableSize + "; test data: " + testDataFactory);

            Random random = new Random(1);
            int[] entries = new int[tableSize];
            int elementCount = tableSize * 100;

            List<String> testData = testDataFactory.getTestData(elementCount, random);

            for (String element : testData) {
                int pos = Hashing.hashPosition(this.tableSize, element);
                entries[pos]++;
            }

            double expectedElementsPerBucket = (double) elementCount / (double) tableSize;
            double varianceSum = 0.0;

            StringBuilder info = new StringBuilder();

            int greater150Count = 0;

            for (int i = 0; i < tableSize; i++) {
                double relativeElementsPerBucket = ((double) entries[i]) / expectedElementsPerBucket;

                if (relativeElementsPerBucket >= 1.5) {
                    greater150Count++;
                }

                varianceSum += Math.pow(relativeElementsPerBucket - 1.0, 2.0);

                info.append(i)
                        .append(": ")
                        .append(entries[i])
                        .append("; ")
                        .append(relativeElementsPerBucket)
                        .append("\n");
            }

            double variance = varianceSum / (double) tableSize;
            double stdDev = Math.sqrt(variance);

            System.out.println("var: " + variance + "; stdDev: " + stdDev + "; g150: " + greater150Count);

            if (greater150Count > 0) {
                Assert.fail("Bad distribution; more than 150 elements per bucket in  " + greater150Count + " cases:\n"
                        + info);
            }
        }

        @Test
        public void avgCapacity() {
            System.out.println("## avgCapacity; table size: " + tableSize + "; test data: " + testDataFactory);

            Random random = new Random(1);
            int sampleCount = 1000;
            int sumCount = 0;
            int sumProbingOverhead = 0;
            List<Integer> countList = new ArrayList<>(sampleCount);
            List<Double> probingOverheadList = new ArrayList<>(sampleCount);

            for (int i = 0; i < sampleCount; i++) {
                String[] table = new String[tableSize + maxProbingDistance];
                int count = 0;
                List<String> testValues =
                        testDataFactory.getTestData(tableSize + maxProbingDistance + 1, random);

                int probingOverhead = 0;

                for (String value : testValues) {
                    int pos = Hashing.hashPosition(tableSize, value);
                    int check = Hashing.checkTable(table, value, pos, maxProbingDistance);

                    if (check == Hashing.NO_SPACE) {
                        sumCount += count;
                        sumProbingOverhead += probingOverhead;
                        countList.add(count);
                        probingOverheadList.add((double) probingOverhead / count);
                        break;
                    } else if (check >= 0) {
                        table[check] = value;
                        count++;
                        probingOverhead += check - pos;
                    }
                }
            }

            double avgCapacity = (double) sumCount / (double) sampleCount;
            double avgRatio = avgCapacity / (double) tableSize;

            Collections.sort(countList);
            Collections.sort(probingOverheadList);

            int medianCapacity = countList.get(countList.size() / 2);
            double medianCapacityRatio = medianCapacity / (double) tableSize;

            System.out.println("avg capacity: " + avgCapacity + ", median capacity: " + medianCapacity + ", avg ratio: "
                    + avgRatio);

            for (int i = 10; i <= 100; i += 10) {
                int c = countList.get((int) ((double) i / 100 * countList.size()) - 1);
                System.out.print("p" + i + ": " + c + "; ");
            }

            System.out.println("\nprobing overhead");

            double medianProbingOverhead = probingOverheadList.get(probingOverheadList.size() / 2);

            for (int i = 10; i <= 100; i += 10) {
                double c = probingOverheadList.get((int) ((double) i / 100 * probingOverheadList.size()) - 1);
                System.out.print("p" + i + ": " + c + "; ");
            }

            System.out.println("\n\n");

            if (medianCapacityRatio < 0.5) {
                Assert.fail("Median capacity ratio < 0.5: " + medianCapacityRatio + " (" + medianCapacity + ")");
            }

            if (tableSize == 16) {
                if (medianProbingOverhead > 2) {
                    Assert.fail("Median probing overhead > 2: " + medianProbingOverhead);
                }
            } else if (tableSize == 64) {
                if (medianProbingOverhead > 1.3) {
                    Assert.fail("Median probing overhead > 1.3: " + medianProbingOverhead);
                }
            } else {
                if (medianProbingOverhead > 1) {
                    Assert.fail("Median probing overhead > 1: " + medianProbingOverhead);
                }
            }
        }

        public ByTableSizeAndTestData(int tableSize, TestDataFactory testDataFactory) {
            this.tableSize = tableSize;
            this.maxProbingDistance = Hashing.maxProbingDistance(tableSize);
            this.testDataFactory = testDataFactory;
        }

        @Parameterized.Parameters(name = "{0}/{1}")
        public static Collection<Object[]> params() {
            List<Object[]> result = new ArrayList<>();

            for (int tableSize : Arrays.asList(0x10, 0x40, 0x100, 0x200, 0x400, 0x800, 0x1000, 0x2000, 0x4000, 0x8000, 0x10000, 0x20000)) {
                for (TestDataFactory testDataFactory : TestDataFactory.ALL) {
                    result.add(new Object[] {tableSize, testDataFactory});
                }
            }

            return result;
        }

        abstract static class TestDataFactory {
            static final List<TestDataFactory> ALL = Arrays.asList(
                    new RandomHumanReadableStrings(), new LowEntropyRandomStrings(), new LowEntropySequentialStrings());

            final String name;

            TestDataFactory(String name) {
                this.name = name;
            }

            abstract List<String> getTestData(int size, Random random);

            @Override
            public String toString() {
                return name;
            }

            static class RandomHumanReadableStrings extends TestDataFactory {

                RandomHumanReadableStrings() {
                    super("Human-readable strings");
                }

                @Override
                List<String> getTestData(int size, Random random) {
                    List<String> result = new ArrayList<>(size);
                    Set<String> resultAsSet = new HashSet<>(size);

                    for (int i = 0; i < size; i++) {
                        String value = testValues.randomLocationName(random);

                        for (int k = 0; resultAsSet.contains(value) && k < 10; k++) {
                            value = testValues.randomLocationName(random);
                        }

                        for (int k = 2; resultAsSet.contains(value) && k < 10000; k++) {
                            value = testValues.randomLocationName(random) + " " + k;
                        }

                        resultAsSet.add(value);
                        result.add(value);
                    }

                    return result;
                }
            }

            static class LowEntropyRandomStrings extends TestDataFactory {

                LowEntropyRandomStrings() {
                    super("Low entropy randomized strings");
                }

                @Override
                List<String> getTestData(int size, Random random) {
                    List<String> result = new ArrayList<>(size * 2);
                    for (int k = 0; k < size * 2; k++) {
                        result.add("a" + k);
                    }

                    Collections.shuffle(result, random);

                    return result.subList(0, size);
                }
            }

            static class LowEntropySequentialStrings extends TestDataFactory {

                LowEntropySequentialStrings() {
                    super("Low entropy sequential strings");
                }

                @Override
                List<String> getTestData(int size, Random random) {
                    int start = random.nextInt(100);

                    List<String> result = new ArrayList<>(size);
                    for (int k = 0; k < size; k++) {
                        result.add("a" + (k + start));
                    }

                    return result;
                }
            }
        }
    }
}
