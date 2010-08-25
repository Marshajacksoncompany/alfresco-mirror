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
 * WorkflowForm component.
 *
 * The workflow details page form is actually a form display of the workflow's start task and data form the workflow itself.
 * To be able to display all this information the following approach is taken:
 *
 * 1. The page loads with a url containing the workflowId as an argument.
 * 2. Since we actually want to display the start task the data-loader component has been bound in to the bottom of the page,
 *    instructed to load detailed workflow data based on the workflowId url argument,
 *    so we can get the startTaskInstanceId needed to request the form.
 * 3. A dynamically/ajax loaded form is brought in using the startTaskInstanceId which gives us a start task form with the
 *    "More Info", "Roles" and "Items" sections.
 * 4. However we shall also display info from the workflow itsel, so once the form is loaded and inserted in to the Dom,
 *    the additional sections "Summary", "General", "Current Tasks" & "Workflow History" are inserted inside the form.
 *
 * @namespace Alfresco
 * @class Alfresco.WorkflowForm
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
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML,
      $siteURL = Alfresco.util.siteURL,
      $userProfileLink = Alfresco.util.userProfileLink;

   /**
    * WorkflowForm constructor.
    *
    * @param {String} htmlId The HTML id of the parent element
    * @return {Alfresco.WorkflowForm} The new WorkflowForm instance
    * @constructor
    */
   Alfresco.WorkflowForm = function WorkflowForm_constructor(htmlId)
   {

      Alfresco.WorkflowForm.superclass.constructor.call(this, "Alfresco.WorkflowForm", htmlId, ["button", "container", "datasource", "datatable"]);
      this.isReady = false;
      this.workflow = null;
      this.currentTasks = [];
      this.historyTasks = [];

      /* Decoupled event listeners */
      YAHOO.Bubbling.on("workflowDetailedData", this.onWorkflowDetailedData, this);

      return this;
   };

   YAHOO.extend(Alfresco.WorkflowForm, Alfresco.component.Base,
   {

      /**
       * Flag set after component is instantiated.
       *
       * @property isReady
       * @type {boolean}
       */
      isReady: false,

      /**
       * The workflow to display 
       *
       * @property workflow
       * @type {Object}
       */
      workflow: null,

      /**
       * Sorted list of current tasks
       *
       * @property currentTasks
       * @type {Array}
       */
      currentTasks: null,

      /**
       * Sorted list of workflow history
       *
       * @property historyTasks
       * @type {Array}
       */
      historyTasks: null,

      /**
       * Fired by YUI when parent element is available for scripting
       *
       * @method onReady
       */
      onReady: function WorkflowHistory_onReady()
      {
         // Display workflow history if data has been received
         this.isReady = true;
         this._loadWorkflowForm();
      },

      /**
       * Event handler called when the "onWorkflowDetailedData" event is received
       *
       * @method: onWorkflowDetailedData
       */
      onWorkflowDetailedData: function TDH_onWorkflowDetailedData(layer, args)
      {
         // Save workflow info
         this.workflow = args[1];
         this._loadWorkflowForm();
      },

      /**
       * @method _displayWorkflowForm
       * @private
       */
      _loadWorkflowForm: function WF__loadWorkflowForm()
      {
         if (this.isReady && this.workflow)
         {
            // Split the task list in current and history tasks and save the most recent one
            var tasks = this.workflow.tasks, recentTask;
            for (var i = 0, il = tasks.length; i < il; i++)
            {
               if (tasks[i].state == "COMPLETED")
               {
                  this.historyTasks.push(tasks[i]);
               }
               else
               {
                  this.currentTasks.push(tasks[i]);
               }
            }

            var sortByDate = function(dateStr1, dateStr2)
            {
               var date1 = Alfresco.util.fromISO8601(dateStr1),
                  date2 = Alfresco.util.fromISO8601(dateStr2);
               if (date1 && date2)
               {
                  return date1 < date2 ? 1 : -1;
               }
               else
               {
                  return !date1 ? 1 : -1;
               }
            };

            // Sort tasks by completion date
            this.currentTasks.sort(function(task1, task2)
            {
               return sortByDate(task1.properties.bpm_dueDate, task2.properties.bpm_dueDate);
            });

            // Sort tasks by completion date
            this.historyTasks.sort(function(task1, task2)
            {
               return sortByDate(task1.properties.bpm_completionDate, task2.properties.bpm_completionDate);
            });
            // Save the most recent task
            recentTask = this.historyTasks.length > 0 ? this.historyTasks[0] : { properties: {} };

            // Set values in the "Summary" & "General" form sections
            Dom.get(this.id + "-recentTaskTitle").innerHTML = $html(recentTask.title || "");

            Dom.get(this.id + "-title").innerHTML = $html(this.workflow.title);
            Dom.get(this.id + "-description").innerHTML = $html(this.workflow.description);
            
            Dom.get(this.id + "-recentTaskOwnersComment").innerHTML = $html(recentTask.properties.bpm_comment || this.msg("label.noComment"));

            var taskOwner = recentTask.owner || {},
               taskOwnerAvatar = taskOwner.avatar,
               taskOwnerLink = Alfresco.util.userProfileLink(taskOwner.userName, taskOwner.firstName + " " + taskOwner.lastName, null, !taskOwner.firstName);
            Dom.get(this.id + "-recentTaskOwnersAvatar").setAttribute("src", taskOwnerAvatar ? Alfresco.constants.PROXY_URI + taskOwnerAvatar  + "?c=force" : Alfresco.constants.URL_CONTEXT + "components/images/no-user-photo-64.png")
            Dom.get(this.id + "-recentTaskOwnersCommentLink").innerHTML = this.msg("label.recentTaskOwnersCommentLink", taskOwnerLink);

            var initiator = this.workflow.initiator || {};
            Dom.get(this.id + "-startedBy").innerHTML = Alfresco.util.userProfileLink(
                  initiator.userName || this.msg("label.usernameDeleted"), initiator.firstName + " " + initiator.lastName, null, !initiator.firstName);

            var dueDate = Alfresco.util.fromISO8601(this.workflow.dueDate);
            if (dueDate)
            {
               Dom.get(this.id + "-dueSummary").innerHTML = Alfresco.util.formatDate(dueDate);
               Dom.get(this.id + "-due").innerHTML = Alfresco.util.formatDate(dueDate);
            }
            else
            {
               Dom.get(this.id + "-dueSummary").innerHTML = this.msg("label.none");
               Dom.get(this.id + "-due").innerHTML = this.msg("label.none");
            }

            var taskCompletionDate = Alfresco.util.fromISO8601(recentTask.properties.bpm_completionDate);
            Dom.get(this.id + "-recentTaskCompletedOn").innerHTML = $html(taskCompletionDate ? Alfresco.util.formatDate(taskCompletionDate) : this.msg("label.notCompleted"));

            Dom.get(this.id + "-recentTaskOutcome").innerHTML = $html(recentTask.outcome || "");

            var workflowCompletedDate = Alfresco.util.fromISO8601(this.workflow.endDate);
            Dom.get(this.id + "-completed").innerHTML = $html(workflowCompletedDate ? Alfresco.util.formatDate(workflowCompletedDate) : this.msg("label.notCompleted"));

            var startDate = Alfresco.util.fromISO8601(this.workflow.startDate);
            if (startDate)
            {
               Dom.get(this.id + "-started").innerHTML = Alfresco.util.formatDate(startDate);
            }

            var priorityMap = { "1": "high", "2": "medium", "3": "low" },
               priorityKey = priorityMap[this.workflow.priority + ""],
               priority = this.msg("priority." + priorityKey),
               priorityLabel = this.msg("label.priority", priority);
            var prioritySummaryEl = Dom.get(this.id + "-prioritySummary");
            Dom.addClass(prioritySummaryEl, priorityKey);
            prioritySummaryEl.innerHTML = priorityLabel;
            Dom.get(this.id + "-priority").innerHTML = priority;

            var status = this.workflow.isActive ? this.msg("label.inProgress") : this.msg("label.completed");
            Dom.get(this.id + "-statusSummary").innerHTML = $html(status);
            Dom.get(this.id + "-status").innerHTML = $html(status);

            // Load workflow's start task which "represents" the workflow
            Alfresco.util.Ajax.request(
            {
               url: Alfresco.constants.URL_SERVICECONTEXT + "components/form",
               dataObj:
               {
                  htmlid: this.id + "-WorkflowForm-" + Alfresco.util.generateDomId(),
                  itemKind: "task",
                  itemId: this.workflow.startTaskInstanceId,
                  mode: "view",
                  formUI: false
               },
               successCallback:
               {
                  fn: this.onWorkflowFormLoaded,
                  scope: this
               },
               failureMessage: this.msg("message.failure"),
               scope: this,
               execScripts: true
            });
         }
      },

      /**
       * Called when a workflow form has been loaded.
       * Will insert the form in the Dom.
       *
       * @method onWorkflowFormLoaded
       * @param response {Object}
       */
      onWorkflowFormLoaded: function WorkflowForm_onWorkflowFormLoaded(response)
      {
         // Insert the form html
         var formEl = Dom.get(this.id + "-body");
         formEl.innerHTML = response.serverResponse.responseText;

         // Insert the summary & general sections in the top of the form
         var formFieldsEl = Selector.query(".form-fields", this.id, true),
            workflowSummaryEl = Dom.get(this.id + "-summary-form-section"),
            generalSummaryEl = Dom.get(this.id + "-general-form-section");

         formFieldsEl.insertBefore(generalSummaryEl, Dom.getFirstChild(formFieldsEl));
         formFieldsEl.insertBefore(workflowSummaryEl, generalSummaryEl);

         var me = this;

         /**
          * Render task type as link
          */
         var renderCellType = function WorkflowHistory_onReady_renderCellType(elCell, oRecord, oColumn, oData)
         {
            elCell.innerHTML = '<a href="task-details?taskId=' + oRecord.getData("id") + '" title="' + me.msg("link.title.task-details") + '">' + $html(oRecord.getData("title")) + '</a>';
         };

         /**
          * Render task owner as link
          */
         var renderCellOwner = function WorkflowHistory_onReady_renderCellOwner(elCell, oRecord, oColumn, oData)
         {
            var owner = oRecord.getData("owner");
            if (owner != null && owner.userName)
            {
               var displayName = $html(me.msg("field.owner", owner.firstName, owner.lastName));
               elCell.innerHTML = $userProfileLink(owner.userName, displayName, null, !owner.firstName);
            }
         };

         /**
          * Render task completed date
          */
         var renderCellDateCompleted = function WorkflowHistory_onReady_renderCellDateCompleted(elCell, oRecord, oColumn, oData)
         {
            var completionDate = Alfresco.util.fromISO8601(oRecord.getData("properties").bpm_completionDate);
            elCell.innerHTML = Alfresco.util.formatDate(completionDate);
         };

         /**
          * Render task due date
          */
         var renderCellDueDate = function WorkflowHistory_onReady_renderCellDueDate(elCell, oRecord, oColumn, oData)
         {
            var completionDate = Alfresco.util.fromISO8601(oRecord.getData("properties").bpm_dueDate);
            elCell.innerHTML = Alfresco.util.formatDate(completionDate);
         };

         /**
          * Render task status
          */
         var renderCellStatus = function WorkflowHistory_onReady_renderCellStatus(elCell, oRecord, oColumn, oData)
         {
            elCell.innerHTML = $html(oRecord.getData("properties").bpm_status);
         };

         /**
          * Render task outcome
          */
         var renderCellOutcome = function WorkflowHistory_onReady_renderCellOutcome(elCell, oRecord, oColumn, oData)
         {
            elCell.innerHTML = $html(oRecord.getData("outcome"));
         };

         /**
          *  Render task comment
          */
         var renderCellComment = function WorkflowHistory_onReady_renderCellComment(elCell, oRecord, oColumn, oData)
         {
            elCell.innerHTML = $html(oRecord.getData("properties").bpm_comment);
         };

         /**
          * Render actions available for current tasks 
          */
         var renderCellCurrentTasksActions = function WorkflowHistory_onReady_renderCellCurrentTasksActions(elCell, oRecord, oColumn, oData)
         {
            var task = oRecord.getData();
            if (task.isEditable)
            {
               elCell.innerHTML = '<a href="task-edit?taskId=' + task.id + '" class="edit-task" title="' + me.msg("link.title.task-edit") + '">' + me.msg("actions.edit") + '</a>';
            }
         };

         // Create header and data table elements
         var currentTasksContainerEl = Dom.get(this.id + "-currentTasks-form-section"),
            currentTasksTasksEl = Selector.query("div", currentTasksContainerEl, true);

         // DataTable column definitions for current tasks
         var currentTasksColumnDefinitions =
         [
            { key: "type", label: this.msg("column.type"), formatter: renderCellType },
            { key: "owner", label: this.msg("column.assignedTo"), formatter: renderCellOwner },
            { key: "id", label: this.msg("column.dueDate"), formatter: renderCellDueDate },
            { key: "state", label: this.msg("column.status"), formatter: renderCellStatus },
            { key: "properties", label: this.msg("column.actions"), formatter: renderCellCurrentTasksActions }
         ];

         // Create current tasks data table filled with current tasks
         var currentTasksDS = new YAHOO.util.DataSource(this.currentTasks);
         currentTasksDS.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
         currentTasksDS.responseSchema =
         {
            fields: [ "title", "type", "owner", "id", "state", "isPooled", "isEditable", "properties"]
         };
         this.widgets.dataTable = new YAHOO.widget.DataTable(currentTasksTasksEl, currentTasksColumnDefinitions, currentTasksDS,
         {
            MSG_EMPTY: this.msg("label.noTasks")
         });

         // DataTable column definitions workflow history
         var historyColumnDefinitions =
         [
            { key: "type", label: this.msg("column.type"), formatter: renderCellType },
            { key: "owner", label: this.msg("column.userGroup"), formatter: renderCellOwner },
            { key: "id", label: this.msg("column.dateCompleted"), formatter: renderCellDateCompleted },
            { key: "state", label: this.msg("column.outcome"), formatter: renderCellOutcome },
            { key: "properties", label: this.msg("column.comment"), formatter: renderCellComment }
         ];

         // Create header and data table elements
         var historyContainerEl = Dom.get(this.id + "-workflowHistory-form-section"),
            historyTasksEl = Selector.query("div", historyContainerEl, true);

         // Create workflow history data table filled with history tasks
         var workflowHistoryDS = new YAHOO.util.DataSource(this.historyTasks);
         workflowHistoryDS.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
         workflowHistoryDS.responseSchema =
         {
            fields: [ "title", "type", "owner", "id", "state", "properties", "outcome"]
         };
         this.widgets.dataTable = new YAHOO.widget.DataTable(historyTasksEl, historyColumnDefinitions, workflowHistoryDS,
         {
            MSG_EMPTY: this.msg("label.noTasks")
         });

         // Display tables
         Selector.query(".form-fields", this.id, true).appendChild(currentTasksContainerEl);
         Selector.query(".form-fields", this.id, true).appendChild(historyContainerEl);

         // Fire event so other components knows the form finally has been loaded
         YAHOO.Bubbling.fire("workflowFormReady", this);         
      }

   });

})();
