package com.societegenerale.cidroid.extensions.actionToReplicate;

import com.societegenerale.cidroid.api.IssueProvidingContentException;
import lombok.extern.slf4j.Slf4j;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.societegenerale.cidroid.extensions.actionToReplicate.AddXmlElementAction.ELEMENT_TO_ADD;
import static com.societegenerale.cidroid.extensions.actionToReplicate.AddXmlElementAction.XPATH_UNDER_WHICH_ELEMENT_NEEDS_TO_BE_ADDED;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class AddXmlElementActionTest {

    private AddXmlElementAction addXmlElementAction = new AddXmlElementAction();

    private String initialXmlDoc = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><project>\n" +
            "\n" +
            "\t<dependencies>\n" +
            "\n" +
            "\t</dependencies>\n" +
            "\n" +
            "\t<build>\n" +
            "\n" +
            "\t\t<existingElementInBuild>\n" +
            "\t\t\tinteresting stuff !!\n" +
            "\t\t</existingElementInBuild>\n" +
            "\t\t\n" +
            "\t</build>\n" +
            "\n" +
            "</project>";

    private Map<String, String> additionalInfosForInstantiation = new HashMap<>();

    @Before
    public void setup() {

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);

        additionalInfosForInstantiation.put(ELEMENT_TO_ADD, "<element>Hello World</element>");
        additionalInfosForInstantiation.put(XPATH_UNDER_WHICH_ELEMENT_NEEDS_TO_BE_ADDED, "//project/build");

        addXmlElementAction.init(additionalInfosForInstantiation);

    }

    @Test
    public void shouldThrowExceptionIfInputDocumentIsNotValidXml() {

        assertThatThrownBy(() -> {
            addXmlElementAction.provideContent("<hello>");
        }).isInstanceOf(IssueProvidingContentException.class)
                .hasMessageContaining("original document");

    }

    @Test
    public void shouldThrowExceptionIfElementToAddIsNotValidXmlElement() {

        additionalInfosForInstantiation.put(ELEMENT_TO_ADD, "<element>Hello World");
        addXmlElementAction.init(additionalInfosForInstantiation);

        assertThatThrownBy(() -> {
            addXmlElementAction.provideContent(initialXmlDoc);
        }).isInstanceOf(IssueProvidingContentException.class)
                .hasMessageContaining("elementToAdd");
    }

    @Test
    public void shouldAddElementIfReferenceXPathIsFoundAndEmpty() throws IssueProvidingContentException, IOException, SAXException {

        String actualResult = addXmlElementAction.provideContent("<project><build/></project>");

        log.info("actual result: " + actualResult);

        String expectedResult = "<project><build><element>Hello World</element></build></project>";

        XMLAssert.assertXMLEqual(actualResult, expectedResult);
    }

    @Test
    public void shouldAddElementIfReferenceXPathIsFound() throws IssueProvidingContentException, IOException, SAXException {

        String actualResult = addXmlElementAction.provideContent(initialXmlDoc);

        String expectedResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><project>\n" +
                "\n" +
                "\t<dependencies>\n" +
                "\n" +
                "\t</dependencies>\n" +
                "\n" +
                "\t<build>\n" +
                "\n" +
                "\t\t<existingElementInBuild>\n" +
                "\t\t\tinteresting stuff !!\n" +
                "\t\t</existingElementInBuild>\n" +

                //expecting the element to be there
                "\t\t<element>Hello World</element>\n" +

                "\t</build>\n" +
                "\n" +
                "</project>";

        log.info("actual result: " + actualResult);

        XMLAssert.assertXMLEqual(actualResult, expectedResult);

    }

    @Test
    public void shouldReturnSameDocumentWhenXPathNotFound() throws IssueProvidingContentException, IOException, SAXException {

        additionalInfosForInstantiation.put(XPATH_UNDER_WHICH_ELEMENT_NEEDS_TO_BE_ADDED, "//project/unknownElement");

        addXmlElementAction.init(additionalInfosForInstantiation);

        String actualResult = addXmlElementAction.provideContent(initialXmlDoc);

        log.info("actual result: " + actualResult);

        XMLAssert.assertXMLEqual(actualResult, initialXmlDoc);

    }

}