package com.cultofbits.customizations.utils;

import com.cultofbits.recordm.core.model.Definition;
import com.cultofbits.recordm.core.model.FieldDefinition;
import org.apache.commons.lang.math.RandomUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DefinitionBuilder {

    private Definition definition;

    public static DefinitionBuilder aDefinition() {
        DefinitionBuilder builder = new DefinitionBuilder();
        builder.definition = new Definition();
        return builder;
    }

    public static DefinitionBuilder aDefinition(Integer id, String name, String description, Integer version, FieldDefinitionBuilder... fieldDefinitions) {
        DefinitionBuilder builder = new DefinitionBuilder();
        builder.definition = new Definition();
        builder.definition.id = id;
        builder.definition.name = name;
        builder.definition.version = version;
        builder.definition.description = description;
        builder.definition.setFieldDefinitions(Arrays.stream(fieldDefinitions).map(FieldDefinitionBuilder::build).collect(Collectors.toList()));
        return builder;
    }

    public DefinitionBuilder id(Integer id) {
        definition.id = id;
        return this;
    }

    public DefinitionBuilder name(String name) {
        definition.name = name;
        return this;
    }

    public DefinitionBuilder description(String description) {
        definition.description = description;
        return this;
    }

    public DefinitionBuilder version(Integer version) {
        definition.version = version;
        return this;
    }

    public DefinitionBuilder fieldDefinitions(FieldDefinition... fieldDefinitions) {
        definition.setFieldDefinitions(Arrays.asList(fieldDefinitions));
        return this;
    }

    public DefinitionBuilder fieldDefinitions(FieldDefinitionBuilder... fieldDefinitions) {
        definition.setFieldDefinitions(Arrays.stream(fieldDefinitions).map(FieldDefinitionBuilder::build).collect(Collectors.toList()));
        return this;
    }

    public Definition build() {
        if (definition.id == null) definition.id = RandomUtils.nextInt();
        if (definition.name == null) definition.name = "definition-" + definition.id;
        if (definition.version == null) definition.version = 1;
        for (FieldDefinition fieldDefinition : definition.getFieldDefinitions()) {
            fieldDefinition.parentField = null;
        }

        return definition;
    }
}
