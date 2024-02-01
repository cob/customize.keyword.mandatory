package com.cultofbits.customizations.validators;

import com.cultofbits.recordm.core.model.FieldKeyArguments;
import com.cultofbits.recordm.core.model.Instance;
import com.cultofbits.recordm.core.model.InstanceField;
import com.cultofbits.recordm.core.visibility.VisibilityConditionParser;
import com.cultofbits.recordm.core.visibility.conditions.Fail;
import com.cultofbits.recordm.core.visibility.conditions.VisibilityCondition;
import com.cultofbits.recordm.customvalidators.api.AbstractOnCreateValidator;
import com.cultofbits.recordm.customvalidators.api.ErrorType;
import com.cultofbits.recordm.customvalidators.api.OnUpdateValidator;
import com.cultofbits.recordm.customvalidators.api.ValidationError;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.cultofbits.recordm.customvalidators.api.ValidationError.custom;
import static com.cultofbits.recordm.customvalidators.api.ValidationError.standard;

public class MandatoryIfValidator extends AbstractOnCreateValidator implements OnUpdateValidator {

    public static final String KEYWORD = "$mandatoryIf";

    @SuppressWarnings("UnstableApiUsage")
    private static final LoadingCache<String, VisibilityCondition> EXPRESSION_CACHE_BUILDER = CacheBuilder.newBuilder()
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
            if (!instanceField.isVisible()) {
                continue;
            }

            if (instanceField.getValue() == null && instanceField.fieldDefinition.containsExtension(KEYWORD)) {
                FieldKeyArguments keywordArgs = instanceField.fieldDefinition.argsFor(KEYWORD);

                if(keywordArgs instanceof FieldKeyArguments.None || Strings.isNullOrEmpty(keywordArgs.get().get(0))){
                    // we'll treat as normal mandatory
                    if(instanceField.getValue() == null){
                        errors.add(standard(instanceField, ErrorType.MANDATORY));
                    }
                    continue;
                }

                String mandatoryArg = keywordArgs.get().get(0);

                //noinspection UnstableApiUsage
                VisibilityCondition vc = EXPRESSION_CACHE_BUILDER.getUnchecked(mandatoryArg);

                try {
                    if (vc.satisfiedAt(instanceField)) {
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

    protected static VisibilityCondition buildExpression(String arg) {
        if (arg == null || arg.trim().isEmpty()) {
            throw new IllegalArgumentException("cannot parse empty expression");
        }

        VisibilityCondition visibilityCondition = VisibilityConditionParser.parse(arg);
        if(visibilityCondition instanceof Fail){
            throw new IllegalArgumentException(
                "Error parsing expression{{ expression: " + arg
                    + ", unparseable: " + ((Fail) visibilityCondition).getUnparsed() + "}}"
            );
        }

        return visibilityCondition;
    }

}
