package dev.arozaakk.booklendingapi.configuration;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Setter
@Getter
@Component
@Validated
@ConfigurationProperties(prefix = "loan.rules")
public class LoanRulesProperties {

  @Min(1)
  private long maxActiveLoansPerMember = 3L;

  @Min(1)
  private long durationDays = 14L;
}
