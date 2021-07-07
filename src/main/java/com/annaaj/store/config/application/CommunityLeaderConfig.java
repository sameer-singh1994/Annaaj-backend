package com.annaaj.store.config.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommunityLeaderConfig {

  @Value("${communityleader.incentive_percentage}")
  private double incentivePercentage;

  public double getIncentivePercentage() {
    return incentivePercentage;
  }

  public void setIncentivePercentage(double incentivePercentage) {
    this.incentivePercentage = incentivePercentage;
  }
}
