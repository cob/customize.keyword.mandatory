package com.cultofbits.customizations.utils;

import com.cultofbits.recordm.core.model.FieldDefinition;
import org.apache.commons.lang.math.RandomUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class FieldDefinitionBuilder {
    private final FieldDefinition fieldDefinition = new FieldDefinition();

    public static FieldDefinitionBuilder aFieldDefinition() {
        return new FieldDefinitionBuilder();
    }

    public static FieldDefinitionBuilder aFieldDefinition(Integer id, String name, String description, boolean required, boolean duplicable, FieldDefinitionBuilder... childFields) {
        FieldDefinitionBuilder builder = new FieldDefinitionBuilder();
        builder.fieldDefinition.id = id;
        builder.fieldDefinition.name = name;
        builder.fieldDefinition.setDescription(description);
        builder.fieldDefinition.required = required ? "mandatory" : null;
        builder.fieldDefinition.duplicable = duplicable;

        builder.fieldDefinition.setFields(
            Arrays.stream(childFields)
                .map(FieldDefinitionBuilder::build)
                .peek(cf -> cf.parentField = builder.fieldDefinition)
                .collect(Collectors.toList())
        );
        return builder;
    }

    public FieldDefinitionBuilder id(Integer id) {
        fieldDefinition.id = id;
        return this;
    }

    public FieldDefinitionBuilder name(String name) {
        fieldDefinition.name = name;
        return this;
    }

    public FieldDefinitionBuilder description(String description) {
        this.fieldDefinition.setDescription(description);
        return this;
    }

    public FieldDefinitionBuilder required(boolean required) {
        fieldDefinition.required = "mandatory";
        return this;
    }

    public FieldDefinitionBuilder duplicable(boolean duplicable) {
        fieldDefinition.duplicable = duplicable;
        return this;
    }

    public FieldDefinitionBuilder childFields(FieldDefinition... childFields) {
        fieldDefinition.setFields(Arrays.asList(childFields));
        Arrays.stream(childFields).forEach(f -> f.parentField = this.fieldDefinition);
        return this;
    }

    public FieldDefinitionBuilder childFields(FieldDefinitionBuilder... childFields) {
        fieldDefinition.setFields(Arrays.stream(childFields)
                .map(FieldDefinitionBuilder::build)
                .peek(f -> f.parentField = this.fieldDefinition)
                .collect(Collectors.toList()));
        return this;
    }

    public FieldDefinition build() {
        if (this.fieldDefinition.id == null) this.fieldDefinition.id = RandomUtils.nextInt();
        if (this.fieldDefinition.name == null) this.fieldDefinition.name = "field-definition-" + this.fieldDefinition.id;
        return this.fieldDefinition;
    }
}
