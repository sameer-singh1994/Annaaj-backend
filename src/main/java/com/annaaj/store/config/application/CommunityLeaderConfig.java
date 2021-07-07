package com.annaaj.store.config.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("communityleader")
@Component
public class CommunityLeaderConfig {

  private double incentivePercentage;

  public double getIncentivePercentage() {
    return incentivePercentage;
  }

  public void setIncentivePercentage(double incentivePercentage) {
    this.incentivePercentage = incentivePercentage;
  }
}
