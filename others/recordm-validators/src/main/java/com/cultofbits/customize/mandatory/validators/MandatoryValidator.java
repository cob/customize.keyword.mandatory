package com.cultofbits.customize.mandatory.validators;

import com.cultofbits.recordm.core.model.Instance;
import com.cultofbits.recordm.core.model.InstanceField;
import com.cultofbits.recordm.customvalidators.api.AbstractOnCreateValidator;
import com.cultofbits.recordm.customvalidators.api.OnUpdateValidator;
import com.cultofbits.recordm.customvalidators.api.ValidationError;

import java.util.Collection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.cultofbits.recordm.customvalidators.api.ValidationError.custom;

public class MandatoryValidator extends AbstractOnCreateValidator implements OnUpdateValidator {

    //protected static final Pattern KEYWORD_PATTERN = Pattern.compile(".*\\$mandatory\\(([^\\)].*)\\).*");
    protected static final Pattern KEYWORD_PATTERN = Pattern.compile(".*\\$mandatory(\\(.*\\))?.*");
    //    protected static final Pattern EXPRESSION_PATTERN = Pattern.compile("(.*?)(=|!|>|<|$)(.*)");
    protected static final Pattern EXPRESSION_PATTERN = Pattern.compile("(.*?)(=|!|>|<|$)(.*)");

    @Override
    public Collection<ValidationError> onCreate(Instance instance) {
        return validateInstance(instance);
    }

    @Override
    public Collection<ValidationError> onUpdate(Instance instance, Instance instance1) {
        return validateInstance(instance);
    }

    public Collection<ValidationError> validateInstance(Instance instance) {
        return instance.getFields()
            .stream()
            .map(f -> {
                Expr expr = buildExpressionIfMatching(f.fieldDefinition.getDescription());
                if (expr == null) {
                    // no expression means it doesn't have the keyword
                    return null;
                }

                if (expr.fieldName == null && f.getValue() == null) {
                    return custom(f, "mandatory");
                }

                InstanceField target = findTarget(f, expr.fieldName);
                if (target.getValue() == null) {
                    return null;
                }

                return null;

            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    protected Expr buildExpressionIfMatching(String fieldDefinitonDescription) {
        if (fieldDefinitonDescription == null) return null;

        Matcher keywordMatcher = KEYWORD_PATTERN.matcher(fieldDefinitonDescription);
        if (!keywordMatcher.matches()) return null;

        String expression = keywordMatcher.group(1);
        if (expression == null) {
            // The field is just mandatory
            return new Expr();
        }

        expression = expression.substring(1, expression.length() - 1).trim(); // remove the parenthesis
        if (expression.length() == 0) {
            // The field is just mandatory
            return new Expr();
        }

        Matcher expMatcher = EXPRESSION_PATTERN.matcher(expression);
        if (!expMatcher.matches()) {
            throw new IllegalStateException("The expression pattern should have matched. {{"
                                                + "expression:" + keywordMatcher.group(1) + "}}");
        }

        return new Expr(expMatcher.group(1), expMatcher.group(2), expMatcher.group(3));
    }

    /**
     * TODO remover quando o novo recordm-core estiver disponivel
     */
    public InstanceField findTarget(InstanceField startField, String targetFieldName) {
        InstanceField sourceField = startField.getClosestAncestorOrSiblingNamed(targetFieldName);
        if (sourceField == null) sourceField = startField.getClosestDescendantNamed(targetFieldName);
        if (sourceField == null) sourceField = startField.instance.getField(targetFieldName);
        return sourceField;
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
