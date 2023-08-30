package com.cultofbits.customizations.utils;

import com.cultofbits.recordm.core.model.Definition;
import com.cultofbits.recordm.core.model.FieldDefinition;
import com.cultofbits.recordm.core.model.Instance;

public class InstanceBuilder {

    private Instance instance;

    public static InstanceBuilder anInstance(Definition definition) {
        InstanceBuilder builder = new InstanceBuilder();
        builder.instance = new Instance(definition, true, true);
        return builder;
    }

    public InstanceBuilder id(Integer id) {
        instance.id = id;
        return this;
    }

    public InstanceBuilder version(Integer version) {
        instance.version = version;
        return this;
    }

    public InstanceBuilder fieldValue(String field, String value) {
        instance.getFieldsByName(field).forEach(f -> f.setValue(value));
        return this;
    }

    public InstanceBuilder fieldValue(FieldDefinition field, String value) {
        instance.getFields(field).forEach(f -> f.setValue(value));
        return this;
    }

    public Instance build() {
        return instance;
    }
}
