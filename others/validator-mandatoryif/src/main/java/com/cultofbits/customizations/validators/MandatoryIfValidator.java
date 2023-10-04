package com.cultofbits.customizations.validators;

import com.cultofbits.recordm.core.model.FieldKeyArguments;
import com.cultofbits.recordm.core.model.Instance;
import com.cultofbits.recordm.core.model.InstanceField;
import com.cultofbits.recordm.customvalidators.api.AbstractOnCreateValidator;
import com.cultofbits.recordm.customvalidators.api.ErrorType;
import com.cultofbits.recordm.customvalidators.api.OnUpdateValidator;
import com.cultofbits.recordm.customvalidators.api.ValidationError;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cultofbits.recordm.customvalidators.api.ValidationError.custom;
import static com.cultofbits.recordm.customvalidators.api.ValidationError.standard;

public class MandatoryIfValidator extends AbstractOnCreateValidator implements OnUpdateValidator {

    public static final String KEYWORD = "$mandatoryIf";

    protected static final Pattern EXPRESSION_PATTERN = Pattern.compile("(.*?)(=|!=|>=|<=|>|<|$)(.*)");

    @SuppressWarnings("UnstableApiUsage")
    private static final LoadingCache<String, Expr> EXPRESSION_CACHE_BUILDER = CacheBuilder.newBuilder()
        .maximumSize(100)
        .expireAfterWrite(24, TimeUnit.HOURS)
        .build(CacheLoader.from(MandatoryIfValidator::buildExpression));

    @Override
    public Collection<ValidationError> onCreate(Instance instance) {
        return validateInstanceFields(instance.getRootFields());
    }

    @Override
    public Collection<ValidationError> onUpdate(Instance persistedInstance, Instance updatedInstance) {
        return validateInstanceFields(updatedInstance.getRootFields());
    }

    public Collection<ValidationError> validateInstanceFields(List<InstanceField> instanceFields) {
        List<ValidationError> errors = new ArrayList<>();

        for (InstanceField instanceField : instanceFields) {
            if ((!instanceField.isVisible() || instanceField.getValue() != null)
                && instanceField.children.isEmpty()) {
                continue;
            }

            if (instanceField.fieldDefinition.containsExtension(KEYWORD)) {
                FieldKeyArguments keywordArgs = instanceField.fieldDefinition.argsFor(KEYWORD);
                String mandatoryArg = !(keywordArgs instanceof FieldKeyArguments.None) ? keywordArgs.get().get(0) : "";

                //noinspection UnstableApiUsage
                Expr expr = EXPRESSION_CACHE_BUILDER.getUnchecked(mandatoryArg);

                try {
                    if (expr.fieldName == null || expr.isTrue(instanceField.getClosest(expr.fieldName))) {
                        errors.add(standard(instanceField, ErrorType.MANDATORY));
                    }
                } catch (Exception e) {
                    errors.add(custom(instanceField, "Error validating mandatory expression: " + mandatoryArg));
                }
            }

            if (!instanceField.children.isEmpty()) {
                errors.addAll(validateInstanceFields(instanceField.children));
            }
        }

        return errors;
    }

    protected static Expr buildExpression(String arg) {
        if (arg == null || arg.trim().isEmpty()) {
            return new Expr();
        }

        Matcher expMatcher = EXPRESSION_PATTERN.matcher(arg);
        if (!expMatcher.matches()) {
            throw new IllegalStateException("The expression pattern should have matched. {{"
                                                + "expression:" + arg + "}}");
        }

        return new Expr(expMatcher.group(1), expMatcher.group(2), expMatcher.group(3));
    }

    protected static class Expr {
        private String fieldName;
        private String operation;
        private String value;

        public Expr() {
        }

        public Expr(String fieldName, String operation, String value) {
            this.fieldName = fieldName.trim();
            this.operation = operation != null && !operation.isEmpty() ? operation.trim() : null;
            this.value = value != null && !value.isEmpty() ? value.trim() : null;
        }

        public boolean isTrue(InstanceField sourceField) {
            String fieldValue = sourceField.getValue();

            if ("=".equals(operation)) {
                return (value == null && fieldValue == null) // both are null
                    || (value != null && value.equals(fieldValue));

            } else if ("!=".equals(operation)) {
                return (value == null && fieldValue != null) // both are null
                    || (value != null && !value.equals(fieldValue));

            } else if (value != null && fieldValue != null) {
                try {
                    return numericComparison(Float.parseFloat(fieldValue), operation, Float.parseFloat(value));
                } catch (NumberFormatException e) {
                    return textComparison(fieldValue, operation, value);
                }
            }

            return false;
        }

        private boolean numericComparison(Float fieldValue, String operation, Float exprValue) {
            if (">".equals(operation)) {
                return fieldValue > exprValue;

            } else if (">=".equals(operation)) {
                return fieldValue >= exprValue;

            } else if ("<".equals(operation)) {
                return fieldValue < exprValue;

            } else if ("<=".equals(operation)) {
                return fieldValue <= exprValue;
            }

            return false;
        }

        private boolean textComparison(String fieldValue, String operation, String exprValue) {

            if (">".equals(operation)) {
                return fieldValue.compareTo(exprValue) > 0;

            } else if (">=".equals(operation)) {
                return fieldValue.compareTo(exprValue) >= 0;

            } else if ("<".equals(operation)) {
                return fieldValue.compareTo(exprValue) < 0;

            } else if ("<=".equals(operation)) {
                return fieldValue.compareTo(exprValue) <= 0;
            }

            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Expr expr = (Expr) o;
            return Objects.equals(fieldName, expr.fieldName)
                && Objects.equals(operation, expr.operation)
                && Objects.equals(value, expr.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fieldName, operation, value);
        }

        @Override
        public String toString() {
            return "Expr{" +
                "fieldName='" + fieldName + '\'' +
                ", operation='" + operation + '\'' +
                ", value='" + value + '\'' +
                '}';
        }
    }
}
