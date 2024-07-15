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
import java.util.Collections;
import java.util.Random;

public class TestValues {
    private final Random random;
    private final String[] ipAddresses;
    private final String[] locationNames;

    public TestValues(Random random) {
        this.random = random;
        this.ipAddresses = createRandomIpAddresses(this.random);
        this.locationNames = createRandomLocationNames(this.random);
    }

    public TestValues() {
        this(new Random(9));
    }

    public String randomString(Random random) {
        if (random.nextFloat() < 0.5) {
            return randomIpAddress(random);
        } else {
            return randomLocationName(random);
        }
    }

    public String randomIpAddress(Random random) {
        return ipAddresses[random.nextInt(ipAddresses.length)];
    }

    public String randomLocationName(Random random) {
        return locationNames[random.nextInt(locationNames.length)];
    }

    private static String[] createRandomIpAddresses(Random random) {
        String[] result = new String[10000];

        for (int i = 0; i < result.length; i++) {
            result[i] = (random.nextInt(10) + 100)
                    + "."
                    + (random.nextInt(5) + 100)
                    + "."
                    + random.nextInt(255)
                    + "."
                    + random.nextInt(255);
        }

        return result;
    }

    private static String[] createRandomLocationNames(Random random) {
        String[] p1 = new String[] {null, "Klein-", "Groß-", "Alt-", "Neu-"};

        String[] p2 = new String[] {null, "Nieder", "Hohen", "Ober"};

        String[] p3 = new String[] {
            "Schön",
            "Schöner",
            "Tempel",
            "Friedens",
            "Friedrichs",
            "Blanken",
            "Rosen",
            "Charlotten",
            "Malch",
            "Lichten",
            "Lichter",
            "Hasel",
            "Kreuz",
            "Pank",
            "Marien",
            "Adlers",
            "Zehlen",
            "Haken",
            "Witten",
            "Jungfern",
            "Hellers",
            "Finster",
            "Birken",
            "Falken",
            "Freders",
            "Karls",
            "Grün",
            "Wilmers",
            "Heiners",
            "Lieben",
            "Marien",
            "Wiesen",
            "Biesen",
            "Schmachten",
            "Rahns",
            "Rangs",
            "Herms",
            "Rüders",
            "Wuster",
            "Hoppe",
            "Waidmanns",
            "Wolters",
            "Schmargen",
            "Bohns",
            "Schulzen",
            "Lank",
            "Halen",
            "Mahls",
            "Neuen",
            "Alten",
            "Heiligen",
            "Konrads",
            "Rummels",
            "Müggel"
        };
        String[] p4 = new String[] {
            "au",
            "ow",
            "berg",
            "feld",
            "felde",
            "tal",
            "thal",
            "höhe",
            "burg",
            "horst",
            "hausen",
            "dorf",
            "hof",
            "heide",
            "weide",
            "hain",
            "walde",
            "linde",
            "hagen",
            "eiche",
            "witz",
            "rade",
            "werder",
            "see",
            "fließ",
            "krug",
            "mark",
            "lust",
            "glienicke",
            "zahn",
            "ruh"
        };

        ArrayList<String> result = new ArrayList<>(p1.length * p2.length);

        for (int i1 = 0; i1 < p1.length; i1++) {
            for (int i2 = 0; i2 < p2.length; i2++) {
                for (int i3 = 0; i3 < p3.length; i3++) {
                    for (int i4 = 0; i4 < p4.length; i4++) {
                        result.add(concat(p1[i1], p2[i2], p3[i3], p4[i4]));
                    }
                }
            }
        }

        Collections.shuffle(result, random);

        return result.toArray(new String[result.size()]);
    }

    private static String concat(String... parts) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (parts[i] == null) {
                continue;
            }

            if (i == 2 && parts[1] != null) {
                result.append(parts[2].toLowerCase());
            } else {
                result.append(parts[i]);
            }
        }

        return result.toString();
    }
}
