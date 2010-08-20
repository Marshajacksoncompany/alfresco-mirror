/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Workflow details actions
 *
 * Displays a cancel workflow button
 *
 * @namespace Alfresco
 * @class Alfresco.WorkflowDetailsActions
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event,
      Selector = YAHOO.util.Selector;

   /**
    * WorkflowDetailsActions constructor.
    *
    * @param htmlId {String} The HTML id of the parent element
    * @return {Alfresco.WorkflowDetailsActions} The new Toolbar instance
    * @constructor
    */
   Alfresco.WorkflowDetailsActions = function(htmlId)
   {
      Alfresco.WorkflowDetailsActions.superclass.constructor.call(this, htmlId, ["button"]);

      // Re-register with our own name
      this.name = "Alfresco.WorkflowDetailsActions";
      Alfresco.util.ComponentManager.reregister(this);

      // Decoupled event listeners
      YAHOO.Bubbling.on("workflowDetailedData", this.onWorkflowDetailedData, this);
      YAHOO.Bubbling.on("workflowFormReady", this.onWorkflowFormReady, this);
      YAHOO.Bubbling.on("workflowCancelled", this.onWorkflowCancelled, this);

      return this;
   };

   /**
    * Extend from Alfresco.component.Base to reuse functionality to decide where to navigate after cancel
    */
   YAHOO.extend(Alfresco.WorkflowDetailsActions, Alfresco.FormManager)

   /**
    * Augment prototype with Common Workflow actions to reuse cancel workflow
    */
   YAHOO.lang.augmentProto(Alfresco.WorkflowDetailsActions, Alfresco.action.WorkflowActions);

   /**
    * Augment prototype with main class implementation, ensuring overwrite is enabled
    */
   YAHOO.lang.augmentObject(Alfresco.WorkflowDetailsActions.prototype,
   {

      /**
       * The workflow 
       *
       * @property workflow
       * @type Object
       */
      workflow: null,

      /**
       * Event handler called when the "onWorkflowDetailedData" event is received
       *
       * @method: onWorkflowDetailedData
       * @param layer {object} Event fired
       * @param args {array} Event parameters (depends on event type)
       */
      onWorkflowDetailedData: function TDH_onWorkflowDetailedData(layer, args)
      {
         this.workflow = args[1];
         Alfresco.util.createYUIButton(this, "cancel", function WDA_onWorkflowDetailedData_onCancelButtonClick()
         {
            this.cancelWorkflow(this.workflow.id, this.workflow.title)
         });
      },

      /**
       * @method onWorkflowFormReady
       * @param layer {object} Event fired
       * @param args {array} Event parameters (depends on event type)
       */
      onWorkflowFormReady: function WDA_onWorkflowFormReady(layer, args)
      {
         Dom.removeClass(Selector.query(".actions", this.id), "hidden");
      },

      /**
       * Selected Items Changed event handler.
       * Determines whether to enable or disable the multi-item action drop-down
       *
       * @method onWorkflowCancelled
       * @param layer {object} Event fired
       * @param args {array} Event parameters (depends on event type)
       */
      onWorkflowCancelled: function WorkflowDetailsActions_onWorkflowCancelled(layer, args)
      {         
         this._navigateForward();
      }

   }, true);
})();