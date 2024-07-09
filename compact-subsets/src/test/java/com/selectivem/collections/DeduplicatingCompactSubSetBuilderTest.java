package com.selectivem.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Enclosed.class)
public class DeduplicatingCompactSubSetBuilderTest {

    @RunWith(Parameterized.class)
    public static class Randomized {
        final int size;
        final Random random;
        final Set<String> superSet;
        final ArrayList<SubSet> subSets = new ArrayList<>(100);

        @Test
        public void test() {
            DeduplicatingCompactSubSetBuilder<String> subject =
                    new DeduplicatingCompactSubSetBuilder<String>(this.superSet);

            int countSubSets = (int) (size * (random.nextFloat() + 0.5) / 3);

            if (countSubSets == 0) {
                countSubSets = 1;
            }

            if (countSubSets > 333) {
                countSubSets = 333;
            }

            for (int i = 0; i < countSubSets; i++) {
                subSets.add(new SubSet(subject.createSetBuilder(), subject.createSetBuilder(), random.nextFloat()));
            }

            for (int i = 0; i < countSubSets; i++) {
                subSets.add(
                        new SubSet(subject.createSetBuilder(), subject.createSetBuilder(), random.nextFloat() * 0.1f));
            }

            for (int i = 0; i < countSubSets; i++) {
                subSets.add(new SubSet(
                        subject.createSetBuilder(), subject.createSetBuilder(), random.nextFloat() * 0.1f + 0.09f));
            }

            for (int i = 0; i < 9; i++) {
                subSets.add(new SubSet(subject.createSetBuilder(), subject.createSetBuilder(), "a" + i));
            }

            for (String element : superSet) {
                for (SubSet subSet : subSets) {
                    if (subSet.include(element, random)) {
                        if (random.nextFloat() < 0.5) {
                            subSet.builder.add(element);
                            subSet.builder2.add(element);
                        } else {
                            subSet.builder2.add(element);
                            subSet.builder.add(element);
                        }
                        subSet.reference.add(element);
                    }
                }

                subject.finished(element);
            }

            DeduplicatingCompactSubSetBuilder.Completed<String> result = subject.completelyFinished();

            for (SubSet subSet : subSets) {
                subSet.result = subSet.builder.build(result);
                subSet.result2 = subSet.builder2.build(result);
            }

            for (SubSet subSet : subSets) {
                Assert.assertEquals(subSet.reference, subSet.result);
                Assert.assertTrue(subSet.result == subSet.result2);
            }
        }

        public Randomized(int seed, int size) {
            this.size = size;
            this.random = new Random(seed);
            this.superSet = createSuperSet(size);
        }

        @Parameterized.Parameters(name = "seed: {0}; size: {1}")
        public static Collection<Object[]> seeds() {
            ArrayList<Object[]> result = new ArrayList<>();
            Random random = new Random(1);

            int testsPerGroup = 10;

            for (int size = 1; size < 10; size++) {
                for (int i = 101; i < 111; i++) {
                    result.add(new Object[] {i, size});
                }
            }

            for (int size = 10; size < 100; size += 10) {
                for (int i = 101; i < 101 + testsPerGroup; i++) {
                    result.add(new Object[] {i, size});
                }

                for (int i = 201; i < 201 + testsPerGroup; i++) {
                    result.add(new Object[] {i, size + random.nextInt(8) + 1});
                }
            }

            for (int size = 100; size < 200; size += 20) {
                for (int i = 101; i < 101 + testsPerGroup; i++) {
                    result.add(new Object[] {i, size});
                }

                for (int i = 201; i < 201 + testsPerGroup; i++) {
                    result.add(new Object[] {i, size + random.nextInt(18) + 1});
                }
            }

            for (int size = 200; size < 1000; size += 50) {
                for (int i = 101; i < 101 + testsPerGroup; i++) {
                    result.add(new Object[] {i, size});
                }

                for (int i = 201; i < 201 + testsPerGroup; i++) {
                    result.add(new Object[] {i, size + random.nextInt(48) + 1});
                }
            }

            for (int size = 1000; size < 10000; size += 500) {
                for (int i = 101; i < 101 + testsPerGroup; i++) {
                    result.add(new Object[] {i, size});
                }

                for (int i = 201; i < 201 + testsPerGroup; i++) {
                    result.add(new Object[] {i, size + random.nextInt(498) + 1});
                }
            }

            for (int size = 10000; size <= 100000; size += 10000) {
                for (int i = 101; i < 105; i++) {
                    result.add(new Object[] {i, size});
                }
            }

            return result;
        }

        static class SubSet {
            final DeduplicatingCompactSubSetBuilder.SetBuilder<String> builder;
            final DeduplicatingCompactSubSetBuilder.SetBuilder<String> builder2;
            final Set<String> reference = new HashSet<>();
            final float inclusionProbability;
            final String includeStringPrefix;

            Set<String> result;
            Set<String> result2;

            public SubSet(
                    DeduplicatingCompactSubSetBuilder.SetBuilder<String> builder,
                    DeduplicatingCompactSubSetBuilder.SetBuilder<String> builder2,
                    float inclusionProbability) {
                this.builder = builder;
                this.builder2 = builder2;
                this.inclusionProbability = inclusionProbability;
                this.includeStringPrefix = null;
            }

            public SubSet(
                    DeduplicatingCompactSubSetBuilder.SetBuilder<String> builder,
                    DeduplicatingCompactSubSetBuilder.SetBuilder<String> builder2,
                    String includeStringPrefix) {
                this.builder = builder;
                this.builder2 = builder2;
                this.inclusionProbability = 0;
                this.includeStringPrefix = includeStringPrefix;
            }

            boolean include(String e, Random random) {
                if (this.inclusionProbability == 0) {
                    if (this.includeStringPrefix != null) {
                        return e.startsWith(this.includeStringPrefix);
                    } else {
                        return false;
                    }
                }

                if (this.inclusionProbability >= 1) {
                    return true;
                }

                float f = random.nextFloat();

                return f <= this.inclusionProbability;
            }
        }

        static Set<String> createSuperSet(int size) {
            Set<String> superSet = new HashSet<>();

            for (int i = 0; i < size; i++) {
                superSet.add("a" + i);
            }

            return superSet;
        }
    }
}
