function testGetFormForNonExistentContentNode()
{
	// Replace all the digits in the ID with an 'x'.
	// Surely that node will not exist...
	var corruptedTestDoc = testDoc.replace(/\d/g, "x");
	var form = formService.getForm(corruptedTestDoc);
	test.assertNull(form, "Form should have not been found: " + testDoc);
}

function testGetFormForContentNode()
{
	// Get a known form and check its various attributes/properties.
	var form = formService.getForm(testDoc);
	test.assertNotNull(form, "Form should have been found: " + testDoc);
	
	test.assertEquals(testDoc, form.item);
	test.assertEquals('cm:content', form.type);
	
	test.assertNull(form.fieldGroups, "form.fieldGroups should be null.");
	
	var fieldDefs = form.fieldDefinitions;
	test.assertNotNull(fieldDefs, "field definitions should not be null.");
	test.assertEquals(23, fieldDefs.length);
	
	// as we know there are no duplicates we can safely create a map of the 
	// field definitions for the purposes of this test
	var fieldDefnDataHash = {};
	var fieldDef = null;
	for (var x = 0; x < fieldDefs.length; x++)
	{
		fieldDef = fieldDefs[x];
		fieldDefnDataHash[fieldDef.name] = fieldDef;
	}
	
	var nameField = fieldDefnDataHash['cm:name'];
	var titleField = fieldDefnDataHash['cm:title'];
    var descField = fieldDefnDataHash['cm:description'];
    var originatorField = fieldDefnDataHash['cm:originator'];
    var addresseeField = fieldDefnDataHash['cm:addressee'];
    var addresseesField = fieldDefnDataHash['cm:addressees'];
    var subjectField = fieldDefnDataHash['cm:subjectline'];
    var sentDateField = fieldDefnDataHash['cm:sentdate'];
    var referencesField = fieldDefnDataHash['cm:references'];

    test.assertNotNull(nameField, "Expecting to find the cm:name field");
    test.assertNotNull(titleField, "Expecting to find the cm:title field");
    test.assertNotNull(descField, "Expecting to find the cm:description field");
    test.assertNotNull(originatorField, "Expecting to find the cm:originator field");
    test.assertNotNull(addresseeField, "Expecting to find the cm:addressee field");
    test.assertNotNull(addresseesField, "Expecting to find the cm:addressees field");
    test.assertNotNull(subjectField, "Expecting to find the cm:subjectline field");
    test.assertNotNull(sentDateField, "Expecting to find the cm:sentdate field");
    test.assertNotNull(referencesField, "Expecting to find the cm:references field");

    // check the labels of all the fields
    test.assertEquals("Name", nameField.label);
    test.assertEquals("Title", titleField.label);
    test.assertEquals("Description", descField.label);
    test.assertEquals("Originator", originatorField.label);
    test.assertEquals("Addressee", addresseeField.label);
    test.assertEquals("Addressees", addresseesField.label);
    test.assertEquals("Subject", subjectField.label);
    test.assertEquals("Sent Date", sentDateField.label);
    test.assertEquals("References", referencesField.label);
    
    // check details of name field
    test.assertEquals("d:text", nameField.dataType);
    test.assertTrue(nameField.mandatory);
    // Expecting cm:name to be single-valued.
    test.assertFalse(nameField.repeating, "nameField.repeating was not false.");
    
    // get the constraint for the name field and check
    var constraints = nameField.constraints;
    test.assertEquals(1, constraints.size());
    var constraint = constraints.get(0);
    test.assertEquals("REGEX", constraint.type);
    var params = constraint.params;
    test.assertNotNull(params, "params should not be null.");
    test.assertEquals(2, params.length);
    test.assertNotNull(params["expression"], "params['expression'] should not be null.");
    test.assertNotNull(params["requiresMatch"], "params['requiresMatch'] should not be null.");
    
    // check details of the addressees field
    test.assertEquals("d:text", addresseesField.dataType);
    test.assertFalse(addresseesField.mandatory, "addresseesField.mandatory was not false.");
    // Expecting cm:addressees to be multi-valued.
    test.assertTrue(addresseesField.repeating);
  
    // check the details of the association field
    test.assertEquals("cm:content", referencesField.endpointType);
    
    //TODO A raw comparison fails. Is this a JS vs. Java string?
    test.assertEquals("TARGET", "" + referencesField.endpointDirection);
    test.assertFalse(referencesField.endpointMandatory, "referencesField.endpointMandatory was not false.");
    test.assertTrue(referencesField.endpointMany, "referencesField.endpointMany was not true.");
    
    // check the form data
    var formData = form.formData;
    test.assertNotNull(formData, "formData should not be null.");
    var fieldData = formData.data;
    test.assertNotNull(fieldData, "fieldData should not be null.");
    test.assertNotNull(fieldData.length, "fieldData.length should not be null.");
    
    test.assertEquals("This is the title for the test document", fieldData["prop:cm:title"].value);
    test.assertEquals("This is the description for the test document", fieldData["prop:cm:description"].value);
    test.assertEquals("fred@customer.com", fieldData["prop:cm:originator"].value);
    test.assertEquals("bill@example.com", fieldData["prop:cm:addressee"].value);
    test.assertEquals("The subject is...", fieldData["prop:cm:subjectline"].value);
    
    var addressees = fieldData["prop:cm:addressees"].value;
    test.assertNotNull(addressees);
    test.assertTrue(addressees.indexOf(",") != -1);
    var addresseesArr = addressees.split(",");
    test.assertEquals(2, addresseesArr.length);
    test.assertEquals("harry@example.com", addresseesArr[0]);
    test.assertEquals("jane@example.com", addresseesArr[1]);

    //TODO Fix up the date-testing here.
    //Old comment: Might add the equivalent of the VALUE_SENT_DATE testing here.
    // In the meantime I'll use JavaScript's own Date object to assert that it is a valid date.
	//    var sentDate = fieldData["prop:cm:sentdate"].value;
	//    test.assertFalse(isNaN(Date.parse(sentDate)), "sentDate was not a legal date.");
    
    var targets = fieldData["assoc:cm:references"].value;
    
    test.assertNotNull(targets, "targets should not be null.");
    test.assertEquals(testAssociatedDoc, targets);
}

// Execute tests
testGetFormForNonExistentContentNode();
testGetFormForContentNode();