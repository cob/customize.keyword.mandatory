package com.cultofbits.customize.mandatory.validators;

import org.testng.annotations.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static com.cultofbits.customize.mandatory.validators.MandatoryValidator.EXPRESSION_PATTERN;
import static com.cultofbits.customize.mandatory.validators.MandatoryValidator.KEYWORD_PATTERN;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

public class MandatoryValidatorTest {

    private MandatoryValidator mandatoryIfValidator = new MandatoryValidator();

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

        if (matches) {
            IntStream.range(0, matcher.groupCount() + 1)
                .forEach(i -> System.out.println("group " + i + ": " + matcher.group(i)));
            System.out.println();
        }

        assertEquals("text:" + text, expectedResult, matches);
    }


    @Test
    public void can_build_expressions() {
        assertNull(mandatoryIfValidator.buildExpressionIfMatching("$text"));

        assertEquals(mandatoryIfValidator.buildExpressionIfMatching("$text $help $mandatory(akjbdf % akjfcb) teste"),
                     new MandatoryValidator.Expr("akjbdf % akjfcb", null, null));

        assertEquals(mandatoryIfValidator.buildExpressionIfMatching("$text $help $mandatory teste"),
                     new MandatoryValidator.Expr());

        assertEquals(mandatoryIfValidator.buildExpressionIfMatching("$text $help $mandatory() teste"),
                     new MandatoryValidator.Expr());

        assertEquals(mandatoryIfValidator.buildExpressionIfMatching("$text $help $mandatory(   )"),
                     new MandatoryValidator.Expr());

        assertEquals(mandatoryIfValidator.buildExpressionIfMatching("$text $help $mandatory(field name) teste"),
                     new MandatoryValidator.Expr("field name", null, null));

        assertEquals(mandatoryIfValidator.buildExpressionIfMatching("$text $help $mandatory(field name = test) teste"),
                     new MandatoryValidator.Expr("field name", "=", "test"));

        assertEquals(mandatoryIfValidator.buildExpressionIfMatching("$text $help $mandatory(field!) teste"),
                     new MandatoryValidator.Expr("field", "!", null));

        assertEquals(mandatoryIfValidator.buildExpressionIfMatching("$text $help $mandatory(field=1) teste"),
                     new MandatoryValidator.Expr("field", "=", "1"));
    }

}