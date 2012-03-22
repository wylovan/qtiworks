/* $Id:SAXErrorHandler.java 2824 2008-08-01 15:46:17Z davemckain $
 *
 * Copyright (c) 2012, The University of Edinburgh.
 * All Rights Reserved
 */
package dave;

import uk.ac.ed.ph.jqtiplus.exception2.RuntimeValidationException;
import uk.ac.ed.ph.jqtiplus.internal.util.DumpMode;
import uk.ac.ed.ph.jqtiplus.internal.util.ObjectDumper;
import uk.ac.ed.ph.jqtiplus.node.ModelRichness;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlObjectReader;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.resolution.AssessmentObjectManager;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.ResponseData;
import uk.ac.ed.ph.jqtiplus.types.StringResponseData;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ClassPathResourceLocator;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChoiceRunningTest {
    
    public static void main(String[] args) throws RuntimeValidationException {
        URI inputUri = URI.create("classpath:/choice.xml");
        
        System.out.println("Reading");
        QtiXmlReader qtiXmlReader = new QtiXmlReader();
        QtiXmlObjectReader objectReader = qtiXmlReader.createQtiXmlObjectReader(new ClassPathResourceLocator());
        AssessmentObjectManager objectManager = new AssessmentObjectManager(objectReader);
        ResolvedAssessmentItem resolvedAssessmentItem = objectManager.resolveAssessmentItem(inputUri, ModelRichness.FULL_ASSUMED_VALID);
        
        ItemSessionState itemState = new ItemSessionState();
        ItemSessionController itemController = new ItemSessionController(resolvedAssessmentItem, itemState);
        
        System.out.println("\nInitialising");
        itemController.initialize();
        System.out.println("Item state after init: " + ObjectDumper.dumpObject(itemState, DumpMode.DEEP));
        
        System.out.println("\nBinding & validating responses");
        Map<String, ResponseData> responseMap = new HashMap<String, ResponseData>();
        responseMap.put("RESPONSE", new StringResponseData("ChoiceA"));
        List<Identifier> badResponses = itemController.bindResponses(responseMap);
        List<Identifier> invalidResponses = itemController.validateResponses();
        System.out.println("Bad responses: " + badResponses);
        System.out.println("Invalid responses:" + invalidResponses);
        
        System.out.println("\nInvoking response processing");
        itemController.processResponses();
        System.out.println("Item state after RP1: " + ObjectDumper.dumpObject(itemState, DumpMode.DEEP));
    }
}