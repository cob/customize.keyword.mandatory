package com.cultofbits.customize.mandatory.validators;

import com.cultofbits.recordm.core.model.Definition;
import com.cultofbits.recordm.core.model.FieldDefinition;
import com.cultofbits.recordm.core.model.Instance;
import com.cultofbits.recordm.core.model.InstanceField;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

public class MandatoryValidatorTest {

    private MandatoryValidator validator = new MandatoryValidator();


    @Test
    public void can_build_expressions() {
        assertEquals(MandatoryValidator.buildExpression(null),
                new MandatoryValidator.Expr());

        assertEquals(MandatoryValidator.buildExpression(""),
                new MandatoryValidator.Expr());

        assertEquals(MandatoryValidator.buildExpression("     "),
                new MandatoryValidator.Expr());

        assertEquals(MandatoryValidator.buildExpression("akjbdf % akjfcb"),
                new MandatoryValidator.Expr("akjbdf % akjfcb", null, null));

        assertEquals(MandatoryValidator.buildExpression("field name"),
                new MandatoryValidator.Expr("field name", null, null));

        assertEquals(MandatoryValidator.buildExpression("field name = test"),
                new MandatoryValidator.Expr("field name", "=", "test"));

        assertEquals(MandatoryValidator.buildExpression("field!"),
                new MandatoryValidator.Expr("field", "!", null));

        assertEquals(MandatoryValidator.buildExpression("field=1"),
                new MandatoryValidator.Expr("field", "=", "1"));
    }

    @Test
    public void pass_validation_if_condition_fails() {
        Instance instance = anInstance(
                aField("User Type", "$[Robot,User]", "Robot"),
                aField("Address", "$mandatory(User Type=User)", null));

        assertTrue(validator.validateInstanceFields(instance.getFields()).isEmpty());
    }

    @Test
    public void pass_validation_if_field_is_not_empty() {
        Instance instance = anInstance(
                aField("User Type", "$[Robot,User]", "Robot"),
                aField("Address", "$mandatory(User Type=)", null));

        assertTrue(validator.validateInstanceFields(instance.getFields()).isEmpty());
    }

    @Test
    public void pass_validation_if_condition_true_and_field_has_value() {
        Instance instance = anInstance(
                aField("User Type", "$[Robot,User]", "User"),
                aField("Address", "$mandatory(User Type=User)", "an address"));

        assertTrue(validator.validateInstanceFields(instance.getFields()).isEmpty());
    }

    @Test
    public void fail_validation_when_condition_is_true() {
        Instance instance = anInstance(
                aField("User Type", "$[Robot,User]", "User"),
                aField("Address", "$mandatory(User Type=User)", null));

        assertTrue(validator.validateInstanceFields(instance.getFields()).size() > 0);
    }

    @Test
    public void fail_validation_when_target_field_is_not_empty() {
        Instance instance = anInstance(
                aField("User Type", "$[Robot,User]", "User"),
                aField("Address", "$mandatory(User Type!)", null));

        assertTrue(validator.validateInstanceFields(instance.getFields()).size() > 0);
    }

    @Test
    public void fail_validation_if_no_condition_and_value_is_null() {
        Instance instance = anInstance(
                aField("Address", "$mandatory", null));

        assertTrue(validator.validateInstanceFields(instance.getFields()).size() > 0);
    }

    @Test
    public void fail_validation_if_range_condition_is_false() {
        Instance instance = anInstance(
                aField("Distance", "$number", "0"),
                aField("Message", "$mandatory(Distance > 10)", null));

        assertTrue(validator.validateInstanceFields(instance.getFields()).isEmpty());
    }

    @Test
    public void fail_validation_if_range_condition_is_true() {
        Instance instance = anInstance(
                aField("Distance", "$number", "0"),
                aField("Message", "$mandatory(Distance < 10)", null));

        assertTrue(validator.validateInstanceFields(instance.getFields()).size() > 0);
    }

    @Test
    public void pass_validation_if_not_greater_than() {
        Instance instance = anInstance(
                aField("Number", "$[1,2,3,4,5]", null),
                aField("Message", "$mandatory(Number < 2)", null));

        assertTrue(validator.validateInstanceFields(instance.getFields()).isEmpty());
    }

    @Test
    public void can_validate_instances_with_groups() {
        InstanceField field1 = aField("User Type", "$[Robot,User]", "User");
        InstanceField field2 = aField("Address", "$mandatory(User Type!)", null);

        InstanceField groupField = aField("Group", "$group", "3");
        groupField.children = Arrays.asList(field1, field2);

        Instance instance = anInstance(groupField, field1, field2);

        assertTrue(validator.validateInstanceFields(instance.getFields()).size() > 0);
    }

    public static Instance anInstance(InstanceField... fields) {
        Instance instance = new Instance();
        instance.definition = new Definition(1, "A Definition");

        instance.fields = Arrays.asList(fields);
        instance.definition.setFieldDefinitions(new ArrayList<>(
                instance.fields.stream()
                        .peek(f -> f.instance = instance)
                        .map(f -> f.fieldDefinition)
                        .peek(fd -> fd.definition = instance.definition)
                        .collect(Collectors.toSet())
        ));

        return instance;
    }

    public static InstanceField aField(String name, String description, String value) {
        FieldDefinition fieldDefinition = new FieldDefinition(null, name, null, description, null);
        InstanceField instanceField = new InstanceField();
        instanceField.fieldDefinition = fieldDefinition;
        instanceField.setValue(value);

        return instanceField;
    }

}