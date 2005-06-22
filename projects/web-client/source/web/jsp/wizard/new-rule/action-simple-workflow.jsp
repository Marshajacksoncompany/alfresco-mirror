<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page>

<script language="JavaScript1.2">
   
   window.onload = pageLoaded;
   
   function pageLoaded()
   {
      document.getElementById("new-rule-simple-workflow:approve-step-name").focus();
      checkButtonState();
   }
   
   function checkButtonState()
   {
      if (document.getElementById("new-rule-simple-workflow:approve-step-name").value.length == 0 ||
          document.getElementById("new-rule-simple-workflow:client-approve-folder").value.length == 0 ||
          rejectValid() == false)
      {
         document.getElementById("new-rule-simple-workflow:next-button").disabled = true;
         document.getElementById("new-rule-simple-workflow:finish-button").disabled = true;
      }
      else
      {
         document.getElementById("new-rule-simple-workflow:next-button").disabled = false;
         document.getElementById("new-rule-simple-workflow:finish-button").disabled = false;
      }
   }
   
   function rejectValid()
   {
      var result = true;
      
      if (document.forms['new-rule-simple-workflow']['new-rule-simple-workflow:reject-step-present'][0].checked && 
          (document.getElementById("new-rule-simple-workflow:reject-step-name").value.length == 0 ||
           document.getElementById("new-rule-simple-workflow:client-reject-folder").value.length == 0))
      {
         result = false;
      }
      
      return result;
   }
</script>

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="messages" var="msg"/>
   
   <h:form id="new-rule-simple-workflow">
   
   <%-- add the approve and reject folder ids as hidden fields --%>
   <h:inputHidden id="client-approve-folder" value="#{NewRuleWizard.actionProperties.approveFolder}" />
   <h:inputHidden id="client-reject-folder" value="#{NewRuleWizard.actionProperties.rejectFolder}" />
   
   <%-- Main outer table --%>
   <table cellspacing="0" cellpadding="2">
      
      <%-- Title bar --%>
      <tr>
         <td colspan="2">
            <%@ include file="../../parts/titlebar.jsp" %>
         </td>
      </tr>
      
      <%-- Main area --%>
      <tr valign="top">
         <%-- Shelf --%>
         <td>
            <%@ include file="../../parts/shelf.jsp" %>
         </td>
         
         <%-- Work Area --%>
         <td width="100%">
            <table cellspacing="0" cellpadding="0" width="100%">
               <%-- Breadcrumb --%>
               <%@ include file="../../parts/breadcrumb.jsp" %>
               
               <%-- Status and Actions --%>
               <tr>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_4.gif)" width="4"></td>
                  <td bgcolor="#EEEEEE">
                  
                     <%-- Status and Actions inner contents table --%>
                     <%-- Generally this consists of an icon, textual summary and actions for the current object --%>
                     <table cellspacing="4" cellpadding="0" width="100%">
                        <tr valign="top">
                           <td width="35">
                              <h:graphicImage id="wizard-logo" url="/images/icons/file_large.gif" />
                           </td>
                           <td>
                              <div class="mainSubTitle"/><h:outputText value='#{NavigationBean.nodeProperties["name"]}' /></div>
                              <div class="mainTitle"><h:outputText value="#{NewRuleWizard.wizardTitle}" /></div>
                              <div class="mainSubText"><h:outputText value="#{NewRuleWizard.wizardDescription}" /></div>
                           </td>
                        </tr>
                     </table>
                     
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_6.gif)" width="4"></td>
               </tr>
               
               <%-- separator row with gradient shadow --%>
               <tr>
                  <td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_7.gif" width="4" height="9"></td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_8.gif)"></td>
                  <td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_9.gif" width="4" height="9"></td>
               </tr>
               
               <%-- Details --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width="4"></td>
                  <td>
                     <table cellspacing="0" cellpadding="3" border="0" width="100%">
                        <tr>
                           <td width="20%" valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "blue", "#D3E6FE"); %>
                              <h:outputText styleClass="mainSubTitle" value="Steps"/><br>
                              <a:modeList itemSpacing="3" iconColumnWidth="2" selectedStyleClass="statusListHighlight" 
                                          value="5" disabled="true">
                                 <a:listItem value="1" label="1. Details" />
                                 <a:listItem value="2" label="2. Condition" />
                                 <a:listItem value="3" label="3. Condition Settings" />
                                 <a:listItem value="4" label="4. Action" />
                                 <a:listItem value="5" label="5. Action Settings" />
                                 <a:listItem value="6" label="6. Summary" />
                              </a:modeList>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "blue"); %>
                           </td>
                           
                           <td width="100%" valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
                              <table cellpadding="2" cellspacing="2" border="0" width="100%">
                                 <tr>
                                    <td colspan="2" class="mainSubTitle"><h:outputText value="#{NewRuleWizard.stepTitle}" /></td>
                                 </tr>
                                 <tr><td colspan="2" class="paddingRow"></td></tr>
                                 <tr>
                                    <td colspan="2" class="wizardSectionHeading">Approve Flow</td>
                                 </tr>
                                 <tr>
                                    <td>Name&nbsp;for&nbsp;approve&nbsp;step:</td>
                                    <td width="90%">
                                       <h:inputText id="approve-step-name" value="#{NewRuleWizard.actionProperties.approveStepName}" 
                                                    onkeyup="javascript:checkButtonState();" />
                                    </td>
                                 </tr>
                                 <tr><td colspan="2" class="paddingRow"></td></tr>
                                 <tr><td colspan="2">Choose whether you want to move or copy the content and also the location.</td>
                                 <tr>
                                    <td colspan="2">
                                       <table cellpadding="2" cellspacing="2" border="0">
                                          <tr>
                                             <td valign="top">
                                                <h:selectOneRadio value="#{NewRuleWizard.actionProperties.approveAction}">
                                                   <f:selectItem itemValue="move" itemLabel="Move" />
                                                   <f:selectItem itemValue="copy" itemLabel="Copy" />
                                                </h:selectOneRadio>
                                             </td>
                                             <td style="padding-left:6px;"></td>
                                             <td valign="top" style="padding-top:10px;">To:</td>
                                             <td style="padding-left:6px;"></td>
                                             <td style="padding-top:6px;">
                                                <r:spaceSelector label="Click here to select the destination" 
                                                        value="#{NewRuleWizard.actionProperties.approveFolder}" 
                                                        style="border: 1px dashed #cccccc; padding: 6px;"/>
                                             </td>
                                          </tr>
                                       </table>
                                    </td>
                                 </tr>
                                 <tr><td colspan="2" class="paddingRow"></td></tr>
                                 <tr>
                                    <td colspan="2" class="wizardSectionHeading">Reject Flow</td>
                                 </tr>
                                 <tr>
                                    <td colspan="2">Do you want to provide a reject step?</td>
                                 </tr>
                                 <tr>
                                    <td>
                                       <h:selectOneRadio id="reject-step-present" value="#{NewRuleWizard.actionProperties.rejectStepPresent}"
                                                         onclick="javascript:checkButtonState();" >
                                          <f:selectItem itemValue="yes" itemLabel="Yes" />
                                          <f:selectItem itemValue="no" itemLabel="No" />
                                       </h:selectOneRadio>
                                    </td>
                                 </tr>
                                 <tr>
                                    <td colspan="2">
                                       <table cellpadding="0" cellspacing="0" border="0">
                                          <tr>
                                             <td style="padding-left:24px;"></td>
                                             <td>
                                                <table cellpadding="2" cellspacing="2" border="0">
                                                   <tr>
                                                      <td>
                                                         Name&nbsp;for&nbsp;reject&nbsp;step:&nbsp;
                                                         <h:inputText id="reject-step-name" value="#{NewRuleWizard.actionProperties.rejectStepName}" 
                                                                      onkeyup="javascript:checkButtonState();" />
                                                      </td>
                                                   </tr>
                                                   <tr><td class="paddingRow"></td></tr>
                                                   <tr><td>Choose whether you want to move or copy the content and also the location.</td>
                                                   <tr>
                                                      <td>
                                                         <table cellpadding="2" cellspacing="2" border="0">
                                                            <tr>
                                                               <td valign="top">
                                                                  <h:selectOneRadio value="#{NewRuleWizard.actionProperties.rejectAction}">
                                                                     <f:selectItem itemValue="move" itemLabel="Move" />
                                                                     <f:selectItem itemValue="copy" itemLabel="Copy" />
                                                                  </h:selectOneRadio>
                                                               </td>
                                                               <td style="padding-left:6px;"></td>
                                                               <td valign="top" style="padding-top:10px;">To:</td>
                                                               <td style="padding-left:6px;"></td>
                                                               <td style="padding-top:6px;">
                                                                  <r:spaceSelector label="Click here to select the destination" 
                                                                          value="#{NewRuleWizard.actionProperties.rejectFolder}" 
                                                                          style="border: 1px dashed #cccccc; padding: 6px;"/>
                                                               </td>
                                                            </tr>
                                                         </table>
                                                      </td>
                                                   </tr>
                                                </table>
                                             </td>
                                          </tr>
                                       </table>
                                    </td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td colspan="2"><h:outputText value="#{NewRuleWizard.stepInstructions}" /></td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "blue", "#D3E6FE"); %>
                              <table cellpadding="1" cellspacing="1" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton id="next-button" value="Next" action="#{NewRuleWizard.next}" styleClass="wizardButton" 
                                                        disabled="true" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="Back" action="#{NewRuleWizard.back}" styleClass="wizardButton" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton id="finish-button" value="Finish" action="#{NewRuleWizard.finish}" styleClass="wizardButton" 
                                                        disabled="true" />
                                    </td>
                                 </tr>
                                 <tr><td class="wizardButtonSpacing"></td></tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="Cancel" action="#{NewRuleWizard.cancel}" styleClass="wizardButton" />
                                    </td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "blue"); %>
                           </td>
                        </tr>
                     </table>
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width="4"></td>
               </tr>
                              
               <%-- separator row with bottom panel graphics --%>
               <tr>
                  <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_7.gif" width="4" height="4"></td>
                  <td width="100%" align="center" style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_8.gif)"></td>
                  <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_9.gif" width="4" height="4"></td>
               </tr>
               
            </table>
          </td>
       </tr>
    </table>
    
    
    </h:form>
    
</f:view>

</r:page>