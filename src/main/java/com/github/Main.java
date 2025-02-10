package com.github;

import com.github.extractor.ResourcesExtractor;

public class Main {

    public static void main(String[] args) {
        final ResourcesExtractor resourcesExtractor = new ResourcesExtractor();
        resourcesExtractor.extractPages();
    }
}