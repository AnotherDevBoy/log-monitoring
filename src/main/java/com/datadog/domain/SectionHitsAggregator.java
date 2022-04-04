package com.datadog.domain;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SectionHitsAggregator {
  private final Map<String, Integer> hitsPerSection;

  public SectionHitsAggregator() {
    this.hitsPerSection = new HashMap<>();
  }

  public void addHitToSection(String section, int hits) {
    if (!this.hitsPerSection.containsKey(section)) {
      this.hitsPerSection.put(section, 0);
    }

    this.hitsPerSection.put(section, this.hitsPerSection.get(section) + hits);
  }

  public Optional<Map.Entry<String, Integer>> getSectionWithMostHits() {
    var maxEntry = this.hitsPerSection.entrySet()
            .stream()
            .max(Comparator.comparing(Map.Entry::getValue));
    return maxEntry;
  }
}
