package com.cultofbits.customize.mandatory.validators;

import com.cultofbits.recordm.core.model.Definition;
import com.cultofbits.recordm.core.model.FieldDefinition;
import com.cultofbits.recordm.core.model.Instance;
import com.cultofbits.recordm.core.model.InstanceField;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.cultofbits.customize.mandatory.validators.MandatoryValidator.EXPRESSION_PATTERN;
import static com.cultofbits.customize.mandatory.validators.MandatoryValidator.KEYWORD_PATTERN;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

public class MandatoryValidatorTest {

    private MandatoryValidator validator = new MandatoryValidator();

    @Test
    public void validate_keyword_pattern() {
        expectedMatch(KEYWORD_PATTERN, "$text $help teste", false);

        expectedMatch(KEYWORD_PATTERN, "$mandatory", true);
        expectedMatch(KEYWORD_PATTERN, "$text $help $mandatory", true);
        expectedMatch(KEYWORD_PATTERN, "$text $help $mandatory teste", true);
        expectedMatch(KEYWORD_PATTERN, "$text $help $mandatory() teste", true);
        expectedMatch(KEYWORD_PATTERN, "$text $help $mandatory(    ) teste", true);
        expectedMatch(KEYWORD_PATTERN, "$text $help $mandatory(field) teste", true);
        expectedMatch(KEYWORD_PATTERN, "$text $help $mandatory(field=) teste", true);
        expectedMatch(KEYWORD_PATTERN, "$text $help $mandatory(field=1) teste", true);
    }

    @Test
    public void validate_expression_pattern() {
        expectedMatch(EXPRESSION_PATTERN, "field", true);
        expectedMatch(EXPRESSION_PATTERN, "field=", true);
        expectedMatch(EXPRESSION_PATTERN, "field=1", true);
    }

    private void expectedMatch(Pattern pattern, String text, boolean expectedResult) {
        Matcher matcher = pattern.matcher(text);
        boolean matches = matcher.matches();

        // if (matches) {
        //     IntStream.range(0, matcher.groupCount() + 1)
        //         .forEach(i -> System.out.println("group " + i + ": " + matcher.group(i)));
        //     System.out.println();
        // }

        assertEquals("text:" + text, expectedResult, matches);
    }


    @Test
    public void can_build_expressions() {
        assertNull(validator.buildExpressionIfMatching("$text"));

        assertEquals(validator.buildExpressionIfMatching("$text $help $mandatory(akjbdf % akjfcb) teste"),
                new MandatoryValidator.Expr("akjbdf % akjfcb", null, null));

        assertEquals(validator.buildExpressionIfMatching("$text $help $mandatory teste"),
                new MandatoryValidator.Expr());

        assertEquals(validator.buildExpressionIfMatching("$text $help $mandatory() teste"),
                new MandatoryValidator.Expr());

        assertEquals(validator.buildExpressionIfMatching("$text $help $mandatory(   )"),
                new MandatoryValidator.Expr());

        assertEquals(validator.buildExpressionIfMatching("$text $help $mandatory(field name) teste"),
                new MandatoryValidator.Expr("field name", null, null));

        assertEquals(validator.buildExpressionIfMatching("$text $help $mandatory(field name = test) teste"),
                new MandatoryValidator.Expr("field name", "=", "test"));

        assertEquals(validator.buildExpressionIfMatching("$text $help $mandatory(field!) teste"),
                new MandatoryValidator.Expr("field", "!", null));

        assertEquals(validator.buildExpressionIfMatching("$text $help $mandatory(field=1) teste"),
                new MandatoryValidator.Expr("field", "=", "1"));
    }

    @Test
    public void pass_validation_if_condition_fails() {
        Instance instance = anInstance(
                aField("User Type", "$[Robot,User]", "Robot"),
                aField("Address", "$mandatory(User Type=User)", null));

        assertTrue(validator.validateInstance(instance).isEmpty());
    }

    @Test
    public void pass_validation_if_field_is_not_empty() {
        Instance instance = anInstance(
                aField("User Type", "$[Robot,User]", "Robot"),
                aField("Address", "$mandatory(User Type=)", null));

        assertTrue(validator.validateInstance(instance).isEmpty());
    }

    @Test
    public void pass_validation_if_condition_true_and_field_has_value() {
        Instance instance = anInstance(
                aField("User Type", "$[Robot,User]", "User"),
                aField("Address", "$mandatory(User Type=User)", "an address"));

        assertTrue(validator.validateInstance(instance).isEmpty());
    }

    @Test
    public void fail_validation_when_condition_is_true() {
        Instance instance = anInstance(
                aField("User Type", "$[Robot,User]", "User"),
                aField("Address", "$mandatory(User Type=User)", null));

        assertTrue(validator.validateInstance(instance).size() > 0);
    }

    @Test
    public void fail_validation_when_target_field_is_not_empty() {
        Instance instance = anInstance(
                aField("User Type", "$[Robot,User]", "User"),
                aField("Address", "$mandatory(User Type!)", null));

        assertTrue(validator.validateInstance(instance).size() > 0);
    }

    @Test
    public void fail_validation_if_no_condition_and_value_is_null() {
        Instance instance = anInstance(
                aField("Address", "$mandatory", null));

        assertTrue(validator.validateInstance(instance).size() > 0);
    }

    @Test
    public void fail_validation_if_range_condition_is_false() {
        Instance instance = anInstance(
                aField("Distance", "$number", "0"),
                aField("Message", "$mandatory(Distance > 10)", null));

        assertTrue(validator.validateInstance(instance).size() > 0);
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