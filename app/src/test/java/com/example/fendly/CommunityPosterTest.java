package com.example.fendly;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CommunityPosterTest {
    @Test
    public void homeSubjectUsesSpeciesFromReportTitle() {
        assertEquals("Cat", CommunityPosterSubject.homeSubject("White brown cat", "Animals"));
        assertEquals("Dog", CommunityPosterSubject.homeSubject("Found dogs near park", "Animals"));
        assertEquals("Rabbit", CommunityPosterSubject.homeSubject("Missing rabbit", "pet"));
        assertEquals("Guinea pig", CommunityPosterSubject.homeSubject("Small guinea pig", "Animals"));
        assertEquals("Dog", CommunityPosterSubject.homeSubject("Dog chased by cat", "Animals"));
        assertEquals("Squirrel", CommunityPosterSubject.homeSubject("Found squirrel", "Animals"));
    }

    @Test
    public void homeSubjectUsesAnimalFallbackForUnrecognizedPetSpecies() {
        assertEquals("Animal", CommunityPosterSubject.homeSubject("Missing axolotl", "Pets"));
    }

    @Test
    public void reportTypeUsesSpeciesInsteadOfItemForLostAndFoundAnimals() {
        assertEquals("Lost Squirrel", CommunityPosterSubject.reportType("LOST", "Squirrel", "Animals"));
        assertEquals("Found Cat", CommunityPosterSubject.reportType("FOUND", "Black cat", "Animals"));
        assertEquals("Lost Animal", CommunityPosterSubject.reportType("LOST", "Axolotl", "Animals"));
    }

    @Test
    public void homeSubjectKeepsItemWordingForOrdinaryItems() {
        assertEquals("Item", CommunityPosterSubject.homeSubject("Blue backpack", "Electronics"));
    }
}
