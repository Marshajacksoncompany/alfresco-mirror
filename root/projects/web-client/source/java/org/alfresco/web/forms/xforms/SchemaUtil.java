/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.web.forms.xforms;

import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.alfresco.web.forms.XMLUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.xs.*;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.*;
import org.w3c.dom.*;

/**
 * Provides utility functions for xml schema parsing.
 */
public class SchemaUtil
{

   ////////////////////////////////////////////////////////////////////////////

   public static class Occurance
   {
      public final static int UNBOUNDED = -1;
	
      public final int minimum;
      public final int maximum;

      public Occurance(final XSParticle particle)
      {
         if (particle == null)
         {
            this.minimum = 1;
            this.maximum = 1;
         }
         else
         {
            this.minimum = particle.getMinOccurs();
            this.maximum = (particle.getMaxOccursUnbounded()
                            ? Occurance.UNBOUNDED
                            : particle.getMaxOccurs());
         }
      }

      public Occurance(final int minimum)
      {
         this(minimum, UNBOUNDED);
      }

      public Occurance(final int minimum, final int maximum)
      {
         this.minimum = minimum;
         this.maximum = maximum;
      }

      public boolean isRepeated()
      {
         return this.isUnbounded() || this.maximum > 1;
      }
      
      public boolean isUnbounded()
      {
         return this.maximum == UNBOUNDED;
      }

      public String toString()
      {
         return "minimum=" + minimum + ", maximum=" + maximum;
      }
   }

   ////////////////////////////////////////////////////////////////////////////

   private final static Comparator TYPE_EXTENSION_SORTER = new Comparator() 
   {
      public int compare(Object obj1, Object obj2) 
      {
         if (obj1 == null && obj2 != null)
            return -1;
         else if (obj1 != null && obj2 == null)
            return 1;
         else if (obj1 == obj2 || (obj1 == null && obj2 == null))
            return 0;
         else 
         {
            try
            {
               final XSTypeDefinition type1 = (XSTypeDefinition) obj1;
               final XSTypeDefinition type2 = (XSTypeDefinition) obj2;
               return (type1.derivedFromType(type2, XSConstants.DERIVATION_EXTENSION)
                       ? 1
                       : (type2.derivedFromType(type1, XSConstants.DERIVATION_EXTENSION)
                          ? -1
                          : 0));
            }
            catch (ClassCastException ex) 
            {
               String s = "ClassCastException in typeExtensionSorter: one of the types is not a type !";
               s = s + "\n obj1 class = " + obj1.getClass().getName() + ", toString=" + obj1.toString();
               s = s + "\n obj2 class = " + obj2.getClass().getName() + ", toString=" + obj2.toString();
               SchemaUtil.LOGGER.error(s, ex);
               return 0;
            }
         }
      }
   };

   ////////////////////////////////////////////////////////////////////////////

   private final static Log LOGGER = LogFactory.getLog(SchemaUtil.class);

   private final static HashMap<Short, String> DATA_TYPE_TO_NAME = 
      new HashMap<Short, String>();
   static
   {
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.ANYSIMPLETYPE_DT, "anyType");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.ANYURI_DT, "anyURI");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.BASE64BINARY_DT, "base64Binary");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.BOOLEAN_DT, "boolean");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.BYTE_DT, "byte");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.DATETIME_DT, "dateTime");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.DATE_DT, "date");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.DECIMAL_DT, "decimal");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.DOUBLE_DT, "double");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.DURATION_DT, "duration");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.ENTITY_DT, "ENTITY");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.FLOAT_DT, "float");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.GDAY_DT, "gDay");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.GMONTHDAY_DT, "gMonthDay");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.GMONTH_DT, "gMonth");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.GYEARMONTH_DT, "gYearMonth");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.GYEAR_DT, "gYear");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.IDREF_DT, "IDREF");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.ID_DT, "ID");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.INTEGER_DT, "integer");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.INT_DT, "int");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.LANGUAGE_DT, "language");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.LONG_DT, "long");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.NAME_DT, "Name");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.NCNAME_DT, "NCName");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.NEGATIVEINTEGER_DT, "negativeInteger");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.NMTOKEN_DT, "NMTOKEN");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.NONNEGATIVEINTEGER_DT, "nonNegativeInteger");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.NONPOSITIVEINTEGER_DT, "nonPositiveInteger");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.NORMALIZEDSTRING_DT, "normalizedString");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.NOTATION_DT, "NOTATION");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.POSITIVEINTEGER_DT, "positiveInteger");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.QNAME_DT, "QName");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.SHORT_DT, "short");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.STRING_DT, "string");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.TIME_DT, "time");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.TOKEN_DT, "TOKEN");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.UNSIGNEDBYTE_DT, "unsignedByte");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.UNSIGNEDINT_DT, "unsignedInt");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.UNSIGNEDLONG_DT, "unsignedLong");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.UNSIGNEDSHORT_DT, "unsignedShort");
   };

   /**
    * Returns the most-specific built-in base type for the provided type.
    */
   public static short getBuiltInType(final XSTypeDefinition type) 
   {
      // type.getName() may be 'null' for anonymous types, so compare against
      // static string (see bug #1172541 on sf.net)
      if (SchemaUtil.DATA_TYPE_TO_NAME.get(XSConstants.ANYSIMPLETYPE_DT).equals(type.getName())) 
      {
         return XSConstants.ANYSIMPLETYPE_DT;
      }
      final XSSimpleTypeDefinition simpleType = (XSSimpleTypeDefinition) type;
      final short result = simpleType.getBuiltInKind();
      return (result == XSConstants.LIST_DT 
              ? SchemaUtil.getBuiltInType(simpleType.getItemType()) 
              : result);
   }

   public static String getBuiltInTypeName(final XSTypeDefinition type)
   {
      final short s = SchemaUtil.getBuiltInType(type);
      return SchemaUtil.getBuiltInTypeName(s);
   }

   public static String getBuiltInTypeName(final short type)
   {
      return SchemaUtil.DATA_TYPE_TO_NAME.get(type);
   }

   public static XSModel parseSchema(final Document schemaDocument,
                                     final boolean failOnError)
      throws FormBuilderException
   {
      try
      {
         // Get DOM Implementation using DOM Registry
         System.setProperty(DOMImplementationRegistry.PROPERTY,
                            "org.apache.xerces.dom.DOMXSImplementationSourceImpl");

         final DOMImplementationRegistry registry =
            DOMImplementationRegistry.newInstance();

         final DOMImplementationLS lsImpl = (DOMImplementationLS)
            registry.getDOMImplementation("XML 1.0 LS 3.0");
         if (lsImpl == null)
         {
            throw new FormBuilderException("unable to create DOMImplementationLS using " + registry);
         }
         final LSInput in = lsImpl.createLSInput();
         in.setStringData(XMLUtil.toString(schemaDocument));

         final XSImplementation xsImpl = (XSImplementation)
            registry.getDOMImplementation("XS-Loader");
         final XSLoader schemaLoader = xsImpl.createXSLoader(null);
         final DOMConfiguration config = schemaLoader.getConfig();
         final LinkedList<DOMError> errors = new LinkedList<DOMError>();
         config.setParameter("error-handler", new DOMErrorHandler()
         {
            public boolean handleError(final DOMError domError)
            {
               errors.add(domError);
               return true;
            }
         });

         final XSModel result = schemaLoader.load(in);
         if (failOnError && errors.size() != 0)
         {
            final HashSet<String> messages = new HashSet<String>();
            StringBuilder message = null;
            for (DOMError e : errors)
            {
               message = new StringBuilder();
               final DOMLocator dl = e.getLocation();
               if (dl != null)
               {
                  message.append("at line ").append(dl.getLineNumber())
                     .append(" column ").append(dl.getColumnNumber());
                  if (dl.getRelatedNode() != null)
                  {
                     message.append(" node ").append(dl.getRelatedNode().getNodeName());
                  }
                  message.append(": ").append(e.getMessage());
               }
               messages.add(message.toString());
            }
            
            message = new StringBuilder();
            message.append(messages.size() > 1 ? "errors" : "error").append(" parsing schema: \n");
            for (final String s : messages)
            {
               message.append(s).append("\n");
            }

            throw new FormBuilderException(message.toString());
         }

         if (result == null)
         {
            throw new FormBuilderException("invalid schema");
         }
         return result;
      } 
      catch (ClassNotFoundException x) 
      {
         throw new FormBuilderException(x);
      } 
      catch (InstantiationException x) 
      {
         throw new FormBuilderException(x);
      }
      catch (IllegalAccessException x) 
      {
         throw new FormBuilderException(x);
      }
   }

   private static void buildTypeTree(final XSTypeDefinition type, 
                                     final TreeSet<XSTypeDefinition> descendents,
                                     final TreeMap<String, TreeSet<XSTypeDefinition>> typeTree) 
   {
      if (type == null) 
         return;

      if (descendents.size() > 0) 
      {
         //TreeSet compatibleTypes = (TreeSet) typeTree.get(type.getName());
         TreeSet<XSTypeDefinition> compatibleTypes = typeTree.get(type.getName());
	    
         if (compatibleTypes == null) 
         {
            //compatibleTypes = new TreeSet(descendents);
            compatibleTypes = new TreeSet<XSTypeDefinition>(TYPE_EXTENSION_SORTER);
            typeTree.put(type.getName(), compatibleTypes);
         }
         compatibleTypes.addAll(descendents);
      }

      final XSTypeDefinition parentType = type.getBaseType();
	
      if (parentType == null ||
          type.getTypeCategory() != parentType.getTypeCategory()) 
      {
         return;
      }
      if (type != parentType && 
          (parentType.getName() == null || !parentType.getName().equals("anyType"))) 
      {
	    
         //TreeSet newDescendents=new TreeSet(descendents);
         final TreeSet<XSTypeDefinition> newDescendents = 
            new TreeSet<XSTypeDefinition>(TYPE_EXTENSION_SORTER);
         newDescendents.addAll(descendents);
         
         //extension (we only add it to "newDescendants" because we don't want
         //to have a type descendant to itself, but to consider it for the parent
         if (type.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) 
         {
            final XSComplexTypeDefinition complexType = (XSComplexTypeDefinition)type;
            if (complexType.getDerivationMethod() == XSConstants.DERIVATION_EXTENSION && 
                !complexType.getAbstract() && 
                !descendents.contains(type)) 
               newDescendents.add(type);
         }

         //note: extensions are impossible on simpleTypes !
         SchemaUtil.buildTypeTree(parentType, newDescendents, typeTree);
      }
   }

   public static TreeMap<String, TreeSet<XSTypeDefinition>>
      buildTypeTree(final XSModel schema) 
   {
      final TreeMap<String, TreeSet<XSTypeDefinition>> result = new
         TreeMap<String, TreeSet<XSTypeDefinition>>();
      LOGGER.debug("buildTypeTree " + schema);
      // build the type tree for complex types
      final XSNamedMap types = schema.getComponents(XSConstants.TYPE_DEFINITION);
      for (int i = 0; i < types.getLength(); i++) 
      {
         final XSTypeDefinition t = (XSTypeDefinition)types.item(i);
         if (t.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) 
         {
            final XSComplexTypeDefinition type = (XSComplexTypeDefinition)t;
            SchemaUtil.buildTypeTree(type, 
                                      new TreeSet<XSTypeDefinition>(TYPE_EXTENSION_SORTER),
                                      result);
         }
      }

      // build the type tree for simple types
      for (int i = 0; i < types.getLength(); i++) 
      {
         final XSTypeDefinition t = (XSTypeDefinition)types.item(i);
         if (t.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) 
         {
            SchemaUtil.buildTypeTree((XSSimpleTypeDefinition)t, 
                                      new TreeSet<XSTypeDefinition>(TYPE_EXTENSION_SORTER),
                                      result);
         }
      }

      // print out type hierarchy for debugging purposes
      if (LOGGER.isDebugEnabled()) 
      {
         for (String typeName : result.keySet())
         {
            TreeSet descendents = result.get(typeName);
            LOGGER.debug(">>>> for " + typeName + " Descendants=\n ");
            Iterator it = descendents.iterator();
            while (it.hasNext()) 
            {
               XSTypeDefinition desc = (XSTypeDefinition) it.next();
               LOGGER.debug("      " + desc.getName());
            }
         }
      }
      return result;
   }

   public static XSParticle findCorrespondingParticleInComplexType(final XSElementDeclaration elDecl) 
   {
      final XSComplexTypeDefinition complexType = elDecl.getEnclosingCTDefinition();
      if (complexType == null)
      {
         return null;
      }

      final XSParticle particle = complexType.getParticle();
      final XSTerm term = particle.getTerm();
      if (! (term instanceof XSModelGroup)) 
      {
         return null;
      }

      final XSModelGroup group = (XSModelGroup) term;
      final XSObjectList particles = group.getParticles();
      if (particles == null)
      {
         return null;
      }

      for (int i = 0; i < particles.getLength(); i++) 
      {
         final XSParticle part = (XSParticle) particles.item(i);
         //test term
         final XSTerm thisTerm = part.getTerm();
         if (thisTerm == elDecl)
         {
            return part;
         }
      }
      return null;
   }
    
   /**
    * check that the element defined by this name is declared directly in the type
    */
   public static boolean isElementDeclaredIn(String name, 
                                             XSComplexTypeDefinition type, 
                                             boolean recursive) 
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("isElement " + name + " declared in " + type.getName());
      }

      //test if extension + declared in parent + not recursive -> NOK
      if (!recursive && type.getDerivationMethod() == XSConstants.DERIVATION_EXTENSION) 
      {
         XSComplexTypeDefinition parent = (XSComplexTypeDefinition) type.getBaseType();
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("testing if it is not on parent " + parent.getName());
         if (SchemaUtil.isElementDeclaredIn(name, parent, true))
            return false;
      }

      boolean found = false;
      XSParticle particle = type.getParticle();
      if (particle != null) 
      {
         XSTerm term = particle.getTerm();
         if (term instanceof XSModelGroup) 
         {
            XSModelGroup group = (XSModelGroup) term;
            found = SchemaUtil.isElementDeclaredIn(name, group);
         }
      }

      if (LOGGER.isDebugEnabled())
         LOGGER.debug("isElement " + name + 
                      " declared in " + type.getName() + ": " + found);

      return found;
   }

   /**
    * private recursive method called by isElementDeclaredIn(String name, XSComplexTypeDefinition type)
    */
   public static boolean isElementDeclaredIn(String name, XSModelGroup group) 
   {
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("isElement " + name + " declared in group " + group.getName());

      boolean found = false;
      XSObjectList particles = group.getParticles();
      for (int i = 0; i < particles.getLength(); i++) 
      {
         XSParticle subPart = (XSParticle)particles.item(i);
         XSTerm subTerm = subPart.getTerm();
         if (subTerm instanceof XSElementDeclaration) 
         {
            XSElementDeclaration elDecl = (XSElementDeclaration) subTerm;
            if (name.equals(elDecl.getName()))
               found = true;
         } 
         else if (subTerm instanceof XSModelGroup)
         {
            found = SchemaUtil.isElementDeclaredIn(name, (XSModelGroup) subTerm);
         }
      }

      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("isElement " + name + " declared in group " + group.getName() + ": " + found);
      }
      return found;
   }

   public static boolean doesElementComeFromExtension(final XSElementDeclaration element, 
                                                      final XSComplexTypeDefinition controlType) 
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("doesElementComeFromExtension for " + element.getName() + 
                      " and controlType=" + controlType.getName());
      }
      if (controlType.getDerivationMethod() == XSConstants.DERIVATION_EXTENSION) 
      {
         final XSTypeDefinition baseType = controlType.getBaseType();
         if (baseType.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) 
         {
            final XSComplexTypeDefinition complexType = (XSComplexTypeDefinition) baseType;
            if (SchemaUtil.isElementDeclaredIn(element.getName(), complexType, true)) 
            {
               if (LOGGER.isDebugEnabled())
               {
                  LOGGER.debug("doesElementComeFromExtension: yes");
               }
               return true;
            } 
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("doesElementComeFromExtension: recursive call on previous level");
            }
            return SchemaUtil.doesElementComeFromExtension(element, complexType);
         }
      }
      return false;
   }

   /**
    * check that the element defined by this name is declared directly in the type
    */
   public static boolean isAttributeDeclaredIn(final XSAttributeUse attr, 
                                               final XSComplexTypeDefinition type, 
                                               final boolean recursive) 
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("is Attribute " + attr.getAttrDeclaration().getName() + " declared in " + type.getName());
      }

      //check on parent if not recursive
      if (!recursive && type.getDerivationMethod() == XSConstants.DERIVATION_EXTENSION) 
      {
         XSComplexTypeDefinition parent = (XSComplexTypeDefinition) type.getBaseType();
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("testing if it is not on parent " + parent.getName());
         }
         if (SchemaUtil.isAttributeDeclaredIn(attr, parent, true))
         {
            return false;
         }
      }

      //check on this type  (also checks recursively)
      boolean found = false;
      final XSObjectList attrs = type.getAttributeUses();
      for (int i = 0; i < attrs.getLength(); i++) 
      {
         if ((XSAttributeUse)attrs.item(i) == attr)
         {
            found = true;
            break;
         }
      }

      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("is Attribute " + attr.getName() +
                      " declared in " + type.getName() + ": " + found);
      }

      return found;
   }

   /**
    * check that the element defined by this name is declared directly in the type
    * -> idem with string
    */
   public static boolean isAttributeDeclaredIn(String attrName, XSComplexTypeDefinition type, boolean recursive) 
   {
      boolean found = false;

      if (LOGGER.isDebugEnabled())
         LOGGER.debug("is Attribute " + attrName + " declared in " + type.getName());

      if (attrName.startsWith("@"))
         attrName = attrName.substring(1);

      //check on parent if not recursive
      if (!recursive && type.getDerivationMethod() == XSConstants.DERIVATION_EXTENSION) 
      {
         XSComplexTypeDefinition parent = (XSComplexTypeDefinition) type.getBaseType();
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("testing if it is not on parent " + parent.getName());
         if (SchemaUtil.isAttributeDeclaredIn(attrName, parent, true))
            return false;
      }

      //check on this type (also checks recursively)
      final XSObjectList attrs = type.getAttributeUses();
      for (int i = 0; i < attrs.getLength() && !found; i++) 
      {
         final XSAttributeUse anAttr = (XSAttributeUse) attrs.item(i);
         if (anAttr != null) 
         {
            String name = anAttr.getName();
            if (name == null || name.length() == 0)
               name = anAttr.getAttrDeclaration().getName();
            if (attrName.equals(name))
               found = true;
         }
      }

      if (LOGGER.isDebugEnabled())
         LOGGER.debug("is Attribute " + attrName + " declared in " + type.getName() + ": " + found);

      return found;
   }

   public static boolean doesAttributeComeFromExtension(XSAttributeUse attr, 
                                                        XSComplexTypeDefinition controlType) 
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("doesAttributeComeFromExtension for " + attr.getAttrDeclaration().getName() + 
                      " and controlType=" + controlType.getName());
      }

      if (controlType.getDerivationMethod() != XSConstants.DERIVATION_EXTENSION) 
      {
         return false;
      }

      final XSTypeDefinition baseType = controlType.getBaseType();
      if (baseType.getTypeCategory() != XSTypeDefinition.COMPLEX_TYPE) 
      {
         return false;
      }

      final XSComplexTypeDefinition complexType = (XSComplexTypeDefinition) baseType;
      if (SchemaUtil.isAttributeDeclaredIn(attr, complexType, true)) 
      {
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("doesAttributeComeFromExtension: yes");
         return true;
      }

      //recursive call
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("doesAttributeComeFromExtension: recursive call on previous level");
      return SchemaUtil.doesAttributeComeFromExtension(attr, complexType);
   }

   /**
    * finds the minOccurs and maxOccurs of an element declaration
    *
    * @return a table containing minOccurs and MaxOccurs
    */
   public static Occurance getOccurance(final XSElementDeclaration elDecl) 
   {
      //get occurance on encosing element declaration
      final XSParticle particle =
         SchemaUtil.findCorrespondingParticleInComplexType(elDecl);
      final Occurance result = new Occurance(particle);

      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("getOccurance for " + elDecl.getName() + 
                      ", " + result);
      }
      return result;
   }
}
