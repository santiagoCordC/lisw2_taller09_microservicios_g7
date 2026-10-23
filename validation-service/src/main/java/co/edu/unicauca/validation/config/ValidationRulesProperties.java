package co.edu.unicauca.validation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/** Reglas leídas de application.yml (prefijo validation.rules). */
@ConfigurationProperties(prefix = "validation.rules")
public class ValidationRulesProperties {

    private int optionsCount = 4;
    private int minStatementLength = 15;
    private int maxOptionLength = 300;
    private double lengthWarningRatio = 1.8;
    private List<String> prohibitedExpressions = new ArrayList<>();

    public int getOptionsCount() { return optionsCount; }
    public void setOptionsCount(int optionsCount) { this.optionsCount = optionsCount; }
    public int getMinStatementLength() { return minStatementLength; }
    public void setMinStatementLength(int minStatementLength) { this.minStatementLength = minStatementLength; }
    public int getMaxOptionLength() { return maxOptionLength; }
    public void setMaxOptionLength(int maxOptionLength) { this.maxOptionLength = maxOptionLength; }
    public double getLengthWarningRatio() { return lengthWarningRatio; }
    public void setLengthWarningRatio(double lengthWarningRatio) { this.lengthWarningRatio = lengthWarningRatio; }
    public List<String> getProhibitedExpressions() { return prohibitedExpressions; }
    public void setProhibitedExpressions(List<String> prohibitedExpressions) { this.prohibitedExpressions = prohibitedExpressions; }
}
