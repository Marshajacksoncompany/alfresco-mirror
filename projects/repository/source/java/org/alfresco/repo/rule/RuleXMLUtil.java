/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.rule;

import java.io.Serializable;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.rule.common.RuleImpl;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.ValueConverter;
import org.alfresco.service.cmr.rule.ParameterDefinition;
import org.alfresco.service.cmr.rule.ParameterType;
import org.alfresco.service.cmr.rule.RuleAction;
import org.alfresco.service.cmr.rule.RuleActionDefinition;
import org.alfresco.service.cmr.rule.RuleCondition;
import org.alfresco.service.cmr.rule.RuleConditionDefinition;
import org.alfresco.service.cmr.rule.RuleItem;
import org.alfresco.service.cmr.rule.RuleItemDefinition;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleServiceException;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.QName;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Utility methods used by the rules store to convert rules to XML and back again.
 * 
 * @author Roy Wetherall
 */
/*package*/ class RuleXMLUtil
{
    /**
     * Property values delimiter
     */
    private static final String PROPERTY_VALUES_DELIMITER = "~";
    
    /**
     * XML nodes and attribute names
     */
    private static final String ATT_ID = "id";
    private static final String ATT_RULE_TYPE = "ruleType";
    private static final String NODE_TITLE = "title";
    private static final String NODE_DESCRIPTION = "description";
    private static final String NODE_CONDITIONS = "conditions";
    private static final String NODE_CONDITION = "condition";
    private static final String ATT_NAME = "name";
    private static final String NODE_ACTIONS = "actions";
    private static final String NODE_ACTION = "action";
    private static final String NODE_PARAMETER = "parameter";
    private static final String NODE_IS_APPLIED_TO_CHILDREN = "isAppliedToChildren";    
    private static final String NODE_RULE = "rule";
    
    /**
     * Converts XML into a rule
     * 
     * @param ruleService	the rule service
     * @param ruleXML		the rule XML
     */
    public static RuleImpl XMLToRule(RuleService ruleService, String ruleXML, DictionaryService dictionaryService)
    {
        try
        {
            // Get the root element
            SAXReader reader = new SAXReader();
            StringReader stringReader = new StringReader(ruleXML);            
            Document document = reader.read(stringReader);
            Element rootElement = document.getRootElement();

            // Get the id
            Attribute idAttribute = rootElement.attribute(ATT_ID);
            String id = idAttribute.getValue();
            
            // Get the rule type
            Attribute ruleTypeAttribtue = rootElement.attribute(ATT_RULE_TYPE);
            String ruleTypeName = ruleTypeAttribtue.getValue();
            RuleType ruleType = ruleService.getRuleType(ruleTypeName);
            if (ruleType == null)
            {   
                throw new RuleServiceException(
                        MessageFormat.format("Invalid rule type.  {0}", new Object[]{ruleTypeName}));
            }
            
            // Create the rule
            RuleImpl rule = new RuleImpl(id, ruleType);
            
            // Get the title
            String title = rootElement.elementText(NODE_TITLE);
            rule.setTitle(title);
            
            // Get the description     
            String description = rootElement.elementText(NODE_DESCRIPTION);
            rule.setDescription(description);
            
            // Determine whether the rule should be applied to the nodes children
            String isAppliedToChildrenString = rootElement.elementText(NODE_IS_APPLIED_TO_CHILDREN);
            if (isAppliedToChildrenString != null && isAppliedToChildrenString.length() != 0)
            {
                boolean isAppliedToChildren = Boolean.parseBoolean(isAppliedToChildrenString);
                rule.applyToChildren(isAppliedToChildren);
            }

            // Get the conditions
            Element conditionsElement = rootElement.element(NODE_CONDITIONS);
            for (Object obj : conditionsElement.elements(NODE_CONDITION))
            {
                Element conditionElement = (Element)obj;
                
                // Get the name of condition
                Attribute conditionNameAttribute = conditionElement.attribute(ATT_NAME);
                String conditionName = conditionNameAttribute.getValue();
                
                // Create the condition
                RuleConditionDefinition ruleConditionDefinition = ruleService.getConditionDefinition(conditionName);
                if (ruleConditionDefinition == null)
                {
                    throw new RuleServiceException(
                            MessageFormat.format("Invalid rule condition.  {0}", new Object[]{conditionName}));
                }                
                
                // Get the parameter values
                Map<String, Serializable> params = XMLToParameters(ruleConditionDefinition, conditionElement, dictionaryService);
                
                // Add the condition to the rule
                rule.addRuleCondition(ruleConditionDefinition, params);
            } 
            
            // Get the actions
            Element actionsElement = rootElement.element(NODE_ACTIONS);
            for (Object obj : actionsElement.elements(NODE_ACTION))
            {
                Element actionElement = (Element)obj;
                
                // Get the name of action
                Attribute actionNameAttribute = actionElement.attribute(ATT_NAME);
                String actionName = actionNameAttribute.getValue();
                
                // Get the action definition
                RuleActionDefinition ruleActionDefinition = ruleService.getActionDefinition(actionName);
                if (ruleActionDefinition == null)
                {
                    throw new RuleServiceException(
                            MessageFormat.format("Invalid rule action.  {0}", new Object[]{actionName}));
                }                
                
                // Get the parameter values
                Map<String, Serializable> params = XMLToParameters(ruleActionDefinition, actionElement, dictionaryService);
                
                // Add the condition to the rule
                rule.addRuleAction(ruleActionDefinition, params);
            }
            
            return rule;
        }
        catch (Throwable e)
        {
            throw new RuleServiceException("Unable to parse rule XML.", e);
        }
    }
    
    /**
     * Converts XML into a map of parameters
     * 
     * @param ruleItemDefinition	the rule item definition
     * @param itemElement			the item element 
     * @return						map containing the parameters
     */
    private static Map<String, Serializable> XMLToParameters(
            RuleItemDefinition ruleItemDefinition, 
            Element itemElement,
            DictionaryService dictionaryService)
    {
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        
        for (Object obj : itemElement.elements(NODE_PARAMETER))
        {
            Element paramElement = (Element)obj;
            
            // Get the name of the parameter
            Attribute nameAttribute = paramElement.attribute(ATT_NAME);
            String name = nameAttribute.getValue();
            
            // Get the parameter definition
            ParameterDefinition paramDef = ruleItemDefinition.getParameterDefintion(name);
            if (paramDef == null)
            {
                throw new RuleServiceException(
                        MessageFormat.format(
                                "The parameter {0} is not defined for the rule item {1}.",
                                new Object[]{name, ruleItemDefinition.getName()}));
            }
            
            // Get the value of the parameter
            String valueAsString = paramElement.getStringValue();
            Serializable value = getValueFromString(valueAsString, paramDef.getType(), dictionaryService);
            
            params.put(name, value);
        }
        
        return params;
    }

    /**
     * Generates XML to represent a rule.
     * 
     * @param rule  the rule
     * @return      XML string
     */
    public static String ruleToXML(RuleImpl rule)
    {
        StringBuilder builder = new StringBuilder();
        
        // Output the basic rule details
        builder.
            append("<").append(NODE_RULE).append(" ").append(ATT_ID).append("='").append(rule.getId()).append("' ").append(ATT_RULE_TYPE).append("='").append(rule.getRuleType().getName()).append("'>");
        
		String title = rule.getTitle();
		if (title != null)
		{
			builder.append("<").append(NODE_TITLE).append("><![CDATA[").append(rule.getTitle()).append("]]></").append(NODE_TITLE).append(">");
		}
		
		String description = rule.getDescription();
		if (description != null)
		{
            builder.append("<").append(NODE_DESCRIPTION).append("><![CDATA[").append(rule.getDescription()).append("]]></").append(NODE_DESCRIPTION).append(">");
		}
        
        builder.append("<").append(NODE_IS_APPLIED_TO_CHILDREN).append(">").append(rule.isAppliedToChildren()).append("</").append(NODE_IS_APPLIED_TO_CHILDREN).append(">");
        
        // Output the details of the conditions
        builder.append("<conditions>");
        for (RuleCondition ruleCondition : rule.getRuleConditions())
        {
            builder.
               append("<condition name='").append(ruleCondition.getRuleConditionDefinition().getName()).append("'>").
               append(parametersToXML(ruleCondition, ruleCondition.getRuleConditionDefinition())).
               append("</condition>");
        }
        builder.append("</conditions>");
        
        // Output the details of the actions
        builder.append("<actions>");
        for (RuleAction ruleAction : rule.getRuleActions())
        {
            builder.
               append("<action name='").append(ruleAction.getRuleActionDefinition().getName()).append("'>").
               append(parametersToXML(ruleAction, ruleAction.getRuleActionDefinition())).
               append("</action>");
        }
        builder.append("</actions>");
        
        // Close and return the generated XML string
        builder.append("</").append(NODE_RULE).append(">");        
        return builder.toString();
    }
    
    /**
     * Generates XML to represent a rule items parameter values.
     * 
     * @param ruleItem  the rule item
     * @return          the XML string
     */
    private static String parametersToXML(RuleItem ruleItem, RuleItemDefinition ruleItemDefinition)
    {
        StringBuilder builder = new StringBuilder();
        
        for (Map.Entry<String, Serializable> entry : ruleItem.getParameterValues().entrySet())
        {         
            // Get the parameter definition
            String name = entry.getKey();
            ParameterDefinition paramDef = ruleItemDefinition.getParameterDefintion(name);
            if (paramDef == null)
            {
                throw new RuleServiceException(
                        MessageFormat.format(
                                "The parameter {0} is not defined for the rule item {1}.",
                                new Object[]{name, ruleItemDefinition.getName()}));
            }
            
            builder.
                append("<parameter name='").append(entry.getKey()).append("'><![CDATA[").
                append(getStringFromValue(entry.getValue(), paramDef.getType())).
                append("]]></parameter>");
        }
        
        return builder.toString();
    }
    
    /**
     * Get the value from string
     * 
     * @param valueAsString		the value as string
     * @param type				the parameter type
     * @return					the value
     */
    private static Serializable getValueFromString(
            String valueAsString, 
            ParameterType type,
            DictionaryService dictionaryService)
    {
        Serializable result = null;
        switch (type)
        {
            case PROPERTY_VALUES:
                Map<QName, Serializable> map = new HashMap<QName, Serializable>();
                String[] values = valueAsString.split(PROPERTY_VALUES_DELIMITER);
                for (int i = 0; i < values.length; i++)
                {
                    String qnameString = values[i];
                    String valueString = values[i+1];
                    
                    QName propertyName = ValueConverter.convert(QName.class, qnameString);
                    PropertyTypeDefinition propertyTypeDefinition = dictionaryService.getProperty(propertyName).getPropertyType();
                    Serializable value = (Serializable)ValueConverter.convert(propertyTypeDefinition, valueString);
                    
                    map.put(propertyName, value);
                    
                    i++;
                }
                result = (Serializable)map;
                break;
            case DATE:
                result = ValueConverter.convert(Date.class, valueAsString);
                break;
            case QNAME:
                result = ValueConverter.convert(QName.class, valueAsString);
                break;
            case NODE_REF:
                result = ValueConverter.convert(NodeRef.class, valueAsString);
                break;
            case INT:
                result = ValueConverter.convert(Integer.class, valueAsString);
                break;
            case BOOLEAN:
                result = ValueConverter.convert(Boolean.class, valueAsString);
				break;
            default:
                result = valueAsString;
                break;
        }
        return result;
    }           
    
    /**
     * Get the string value 
     * @param value
     * @param type
     * @return
     */
    private static String getStringFromValue(
            Serializable value,
            ParameterType type)
    {
        String result = null;
        switch (type)
        {
            case PROPERTY_VALUES:
                boolean bFirstTime = true;
                StringBuilder builder = new StringBuilder();
                Map<QName, Serializable> propertyMap = (Map<QName, Serializable>)value;
                for (Map.Entry<QName, Serializable> entry : propertyMap.entrySet())
                {
                    if (bFirstTime == true)
                    {
                        bFirstTime = false;
                    }
                    else
                    {
                        builder.append(PROPERTY_VALUES_DELIMITER);
                    }
                    
                    builder.append(ValueConverter.convert(String.class, entry.getKey()));
                    builder.append(PROPERTY_VALUES_DELIMITER);
                    builder.append(ValueConverter.convert(String.class, entry.getValue()));
                }
                result = builder.toString();
                break;
            case DATE:
            case QNAME:
            case NODE_REF:
            case INT:
            case BOOLEAN:
            default:
                result = ValueConverter.convert(String.class, value);
                break;
        }
        return result;
    }
}
