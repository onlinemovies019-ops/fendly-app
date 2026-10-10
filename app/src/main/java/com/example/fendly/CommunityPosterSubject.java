package com.example.fendly;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

final class CommunityPosterSubject {
    private static final Map<String, String> ANIMAL_NAMES = createAnimalNames();

    private CommunityPosterSubject() {}

    static String homeSubject(String title, String category) {
        String normalizedTitle = title == null ? "" : title.toLowerCase(Locale.ROOT);
        String normalizedCategory = category == null ? "" : category.trim().toLowerCase(Locale.ROOT);
        if (normalizedCategory.contains("people") || normalizedCategory.contains("person")) {
            return "Person";
        }
        if (Pattern.compile(
                "\\b(missing person|missing people|missing child|missing children|person|people)\\b"
        ).matcher(normalizedTitle).find()) {
            return "Person";
        }
        String matchedAnimal = null;
        int firstAnimalIndex = Integer.MAX_VALUE;
        int matchedLength = 0;
        for (Map.Entry<String, String> animal : ANIMAL_NAMES.entrySet()) {
            java.util.regex.Matcher matcher = Pattern.compile(
                    "(?<![a-z])" + Pattern.quote(animal.getKey()) + "(?![a-z])"
            ).matcher(normalizedTitle);
            if (matcher.find() && (matcher.start() < firstAnimalIndex
                    || (matcher.start() == firstAnimalIndex && matcher.group().length() > matchedLength))) {
                matchedAnimal = animal.getValue();
                firstAnimalIndex = matcher.start();
                matchedLength = matcher.group().length();
            }
        }
        if (matchedAnimal != null) return matchedAnimal;
        if (normalizedCategory.equals("animal") || normalizedCategory.equals("animals")
                || normalizedCategory.equals("pet") || normalizedCategory.equals("pets")) {
            return "Animal";
        }
        return "Item";
    }

    static String reportType(String reportType, String title, String category) {
        String status = "FOUND".equalsIgnoreCase(reportType) ? "Found" : "Lost";
        return status + " report · View details in Fendly";
    }

    private static Map<String, String> createAnimalNames() {
        Map<String, String> names = new LinkedHashMap<>();
        addAnimal(names, "guinea pig", "Guinea pig");
        addAnimal(names, "goldfish", "Goldfish");
        addAnimal(names, "cockatiel", "Cockatiel");
        addAnimal(names, "lovebird", "Lovebird");
        addAnimal(names, "parakeet", "Parakeet");
        addAnimal(names, "hamster", "Hamster");
        addAnimal(names, "tortoise", "Tortoise");
        addAnimal(names, "turtle", "Turtle");
        addAnimal(names, "rabbit", "Rabbit");
        addAnimal(names, "kitten", "Kitten");
        addAnimal(names, "puppy", "Puppy");
        addAnimal(names, "parrot", "Parrot");
        addAnimal(names, "pigeon", "Pigeon");
        addAnimal(names, "sparrow", "Sparrow");
        addAnimal(names, "chicken", "Chicken");
        addAnimal(names, "duck", "Duck");
        addAnimal(names, "goose", "Goose");
        addAnimal(names, "horse", "Horse");
        addAnimal(names, "pony", "Pony");
        addAnimal(names, "cow", "Cow");
        addAnimal(names, "calf", "Calf");
        addAnimal(names, "buffalo", "Buffalo");
        addAnimal(names, "goat", "Goat");
        addAnimal(names, "sheep", "Sheep");
        addAnimal(names, "lamb", "Lamb");
        addAnimal(names, "piglet", "Piglet");
        addAnimal(names, "pig", "Pig");
        addAnimal(names, "donkey", "Donkey");
        addAnimal(names, "snake", "Snake");
        addAnimal(names, "lizard", "Lizard");
        addAnimal(names, "gecko", "Gecko");
        addAnimal(names, "iguana", "Iguana");
        addAnimal(names, "budgie", "Budgie");
        addAnimal(names, "peacock", "Peacock");
        addAnimal(names, "owl", "Owl");
        addAnimal(names, "eagle", "Eagle");
        addAnimal(names, "crow", "Crow");
        addAnimal(names, "squirrel", "Squirrel");
        addAnimal(names, "chipmunk", "Chipmunk");
        addAnimal(names, "fox", "Fox");
        addAnimal(names, "deer", "Deer");
        addAnimal(names, "monkey", "Monkey");
        addAnimal(names, "macaque", "Macaque");
        addAnimal(names, "otter", "Otter");
        addAnimal(names, "hedgehog", "Hedgehog");
        addAnimal(names, "ferret", "Ferret");
        addAnimal(names, "rat", "Rat");
        addAnimal(names, "mouse", "Mouse");
        addAnimal(names, "mice", "Mouse");
        addAnimal(names, "rabbit", "Rabbit");
        addAnimal(names, "bat", "Bat");
        addAnimal(names, "raccoon", "Raccoon");
        addAnimal(names, "hamster", "Hamster");
        addAnimal(names, "falcon", "Falcon");
        addAnimal(names, "hawk", "Hawk");
        addAnimal(names, "heron", "Heron");
        addAnimal(names, "kingfisher", "Kingfisher");
        addAnimal(names, "woodpecker", "Woodpecker");
        addAnimal(names, "seagull", "Seagull");
        addAnimal(names, "swallow", "Swallow");
        addAnimal(names, "crane", "Crane");
        addAnimal(names, "bird", "Bird");
        addAnimal(names, "fish", "Fish");
        addAnimal(names, "cat", "Cat");
        addAnimal(names, "dog", "Dog");
        return Collections.unmodifiableMap(names);
    }

    private static void addAnimal(Map<String, String> names, String singular, String displayName) {
        names.put(singular, displayName);
        if (singular.equals("sheep") || singular.equals("fish") || singular.equals("goldfish")
                || singular.equals("deer") || singular.equals("mice")) {
            return;
        }
        if (singular.endsWith("y")) {
            names.put(singular.substring(0, singular.length() - 1) + "ies", displayName);
        } else if (singular.endsWith("s") || singular.endsWith("x") || singular.endsWith("ch")
                || singular.endsWith("sh")) {
            names.put(singular + "es", displayName);
        } else {
            names.put(singular + "s", displayName);
        }
        if (singular.equals("goose")) names.put("geese", displayName);
        if (singular.equals("calf")) names.put("calves", displayName);
        if (singular.equals("mouse")) names.put("mice", displayName);
    }
}
