/**
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing
 */
 
/**
 * Document Library "Global Folder" picker module for Document Library.
 * 
 * @namespace Alfresco.module
 * @class Alfresco.module.DoclibGlobalFolder
 */
(function()
{
   /**
   * YUI Library aliases
   */
   var Dom = YAHOO.util.Dom,
      KeyListener = YAHOO.util.KeyListener,
      Selector = YAHOO.util.Selector;

   /**
    * Alfresco Slingshot aliases
    */
    var $html = Alfresco.util.encodeHTML,
       $combine = Alfresco.util.combinePaths,
       $hasEventInterest = Alfresco.util.hasEventInterest;

   Alfresco.module.DoclibGlobalFolder = function(htmlId)
   {
      Alfresco.module.DoclibGlobalFolder.superclass.constructor.call(this, "Alfresco.module.DoclibGlobalFolder", htmlId, ["button", "container", "connection", "json", "treeview"]);

      // Initialise prototype properties
      this.containers = {};

      // Decoupled event listeners
      if (htmlId != "null")
      {
         this.eventGroup = htmlId;
         YAHOO.Bubbling.on("siteChanged", this.onSiteChanged, this);
         YAHOO.Bubbling.on("containerChanged", this.onContainerChanged, this);
      }

      return this;
   };

   /**
   * Alias to self
   */
   var DLGF = Alfresco.module.DoclibGlobalFolder;
      
   /**
   * View Mode Constants
   */
   YAHOO.lang.augmentObject(DLGF,
   {
      /**
       * "Site" view mode constant.
       *
       * @property VIEW_MODE_SITE
       * @type integer
       * @final
       * @default 0
       */
      VIEW_MODE_SITE: 0,

      /**
       * "Repository" view mode constant.
       *
       * @property VIEW_MODE_REPOSITORY
       * @type integer
       * @final
       * @default 1
       */
      VIEW_MODE_REPOSITORY: 1
   });

   YAHOO.extend(Alfresco.module.DoclibGlobalFolder, Alfresco.component.Base,
   {
      /**
       * Object container for initialization options
       */
      options:
      {
         /**
          * Current siteId for site view mode.
          * 
          * @property siteId
          * @type string
          */
         siteId: "",

         /**
          * ContainerId representing root container in site view mode
          *
          * @property containerId
          * @type string
          * @default "documentLibrary"
          */
         containerId: "documentLibrary",

         /**
          * ContainerType representing root container in site view mode
          *
          * @property containerType
          * @type string
          * @default "cm:folder"
          */
         containerType: "cm:folder",

         /**
          * NodeRef representing root container in repository view mode
          *
          * @property nodeRef
          * @type string
          * @default "alfresco://company/home"
          */
         nodeRef: "alfresco://company/home",

         /**
          * Initial path to expand on module load
          *
          * @property path
          * @type string
          * @default ""
          */
         path: "",

         /**
          * Width for the dialog
          *
          * @property width
          * @type integer
          * @default 40em
          */
         width: "60em",
         
         /**
          * Files to action
          *
          * @property files
          * @type object
          * @default null
          */
         files: null,

         /**
          * Template URL
          *
          * @property templateUrl
          * @type string
          * @example "Alfresco.constants.URL_SERVICECONTEXT + modules/documentlibrary/copy-to"
          * @default null,
          */
         templateUrl: null,

         /**
          * Dialog view mode: site or repository
          *
          * @property viewMode
          * @type integer
          * @default Alfresco.modules.DoclibGlobalFolder.VIEW_MODE_SITE
          */
         viewMode: DLGF.VIEW_MODE_SITE,

         /**
          * Allowed dialog view modes
          *
          * @property allowedViewModes
          * @type array
          * @default [VIEW_MODE_SITE, VIEW_MODE_REPOSITORY]
          */
         allowedViewModes:
         [
            DLGF.VIEW_MODE_SITE,
            DLGF.VIEW_MODE_REPOSITORY
         ],

         /**
          * Evaluate child folders flag
          *
          * @property evaluateChildFolders
          * @type boolean
          * @default true
          */
         evaluateChildFolders: true
      },
      
      /**
       * Container element for template in DOM.
       * 
       * @property containerDiv
       * @type DOMElement
       */
      containerDiv: null,

      /**
       * Paths we have to expand as a result of a deep navigation event.
       * 
       * @property pathsToExpand
       * @type array
       */
      pathsToExpand: null,

      /**
       * Selected tree node.
       * 
       * @property selectedNode
       * @type {YAHOO.widget.Node}
       */
      selectedNode: null,

      /**
       * Current list of containers.
       * 
       * @property containers
       * @type {object}
       */
      containers: null,

      /**
       * Main entry point
       * @method showDialog
       */
      showDialog: function DLGF_showDialog()
      {
         if (!this.containerDiv)
         {
            // Load the UI template from the server
            Alfresco.util.Ajax.request(
            {
               url: this.options.templateUrl,
               dataObj:
               {
                  htmlid: this.id
               },
               successCallback:
               {
                  fn: this.onTemplateLoaded,
                  scope: this
               },
               failureMessage: "Could not load Document Library template:" + this.options.templateUrl,
               execScripts: true
            });
         }
         else
         {
            // Show the dialog
            this._showDialog();
         }
      },
      
      /**
       * Event callback when dialog template has been loaded
       *
       * @method onTemplateLoaded
       * @param response {object} Server response from load template XHR request
       */
      onTemplateLoaded: function DLGF_onTemplateLoaded(response)
      {
         // Reference to self - used in inline functions
         var me = this;
         
         // Inject the template from the XHR request into a new DIV element
         this.containerDiv = document.createElement("div");
         this.containerDiv.setAttribute("style", "display:none");
         this.containerDiv.innerHTML = response.serverResponse.responseText;

         // The panel is created from the HTML returned in the XHR request, not the container
         var dialogDiv = Dom.getFirstChild(this.containerDiv);
         
         // Create and render the YUI dialog
         this.widgets.dialog = Alfresco.util.createYUIPanel(dialogDiv,
         {
            width: this.options.width
         });
         
         // OK button
         this.widgets.okButton = Alfresco.util.createYUIButton(this, "ok", this.onOK);

         // Cancel button
         this.widgets.cancelButton = Alfresco.util.createYUIButton(this, "cancel", this.onCancel);

         // Mode buttons
         var modeButtons = new YAHOO.widget.ButtonGroup(this.id + "-modeGroup");
         modeButtons.on("checkedButtonChange", this.onViewModeChange, this.widgets.modeButtons, this);
         this.widgets.modeButtons = modeButtons;

         // Make user enter-key-strokes also trigger a change
         var buttons = this.widgets.modeButtons.getButtons(),
            fnEnterListener = function(e)
            {
               if (KeyListener.KEY.ENTER == e.keyCode)
               {
                  this.set("checked", true);
               }
            };

         for (var i = 0; i < buttons.length; i++)
         {
            buttons[i].addListener("keydown", fnEnterListener);
         }
         
         /**
          * Dynamically loads TreeView nodes.
          * This MUST be inline in order to have access to the parent class.
          * @method fnLoadNodeData
          * @param node {object} Parent node
          * @param fnLoadComplete {function} Expanding node's callback function
          */
         this.fnLoadNodeData = function DLGF_oR_fnLoadNodeData(node, fnLoadComplete)
         {
            // Get the path this node refers to
            var nodePath = node.data.path;

            // Prepare URI for XHR data request
            var uri = me._buildTreeNodeUrl.call(me, nodePath);

            // Prepare the XHR callback object
            var callback =
            {
               success: function DLGF_lND_success(oResponse)
               {
                  var results = Alfresco.util.parseJSON(oResponse.responseText);
                  
                  if (results.parent && typeof node.data.userAccess == "undefined")
                  {
                     node.data.userAccess = results.parent.userAccess;
                     node.setUpLabel(
                     {
                        label: node.label,
                        style: results.parent.userAccess.create ? "" : "no-permission"
                     });
                  }

                  if (results.items)
                  {
                     var item, tempNode;
                     for (var i = 0, j = results.items.length; i < j; i++)
                     {
                        item = results.items[i];
                        tempNode = new YAHOO.widget.TextNode(
                        {
                           label: $html(item.name),
                           path: $combine(nodePath, item.name),
                           nodeRef: item.nodeRef,
                           description: item.description,
                           userAccess: item.userAccess,
                           style: item.userAccess.create ? "" : "no-permission"
                        }, node, false);

                        if (!item.hasChildren)
                        {
                           tempNode.isLeaf = true;
                        }
                     }
                  }
                  
                  /**
                  * Execute the node's loadComplete callback method which comes in via the argument
                  * in the response object
                  */
                  oResponse.argument.fnLoadComplete();
               },

               // If the XHR call is not successful, fire the TreeView callback anyway
               failure: function DLGF_lND_failure(oResponse)
               {
                  try
                  {
                     var response = YAHOO.lang.JSON.parse(oResponse.responseText);
                     
                     // Show the error in place of the root node
                     var rootNode = this.widgets.treeview.getRoot();
                     var docNode = rootNode.children[0];
                     docNode.isLoading = false;
                     docNode.isLeaf = true;
                     docNode.label = response.message;
                     docNode.labelStyle = "ygtverror";
                     rootNode.refresh();
                  }
                  catch(e)
                  {
                  }
               },
               
               // Callback function scope
               scope: me,

               // XHR response argument information
               argument:
               {
                  "node": node,
                  "fnLoadComplete": fnLoadComplete
               },

               // Timeout -- abort the transaction after 7 seconds
               timeout: 7000
            };

            // Make the XHR call using Connection Manager's asyncRequest method
            YAHOO.util.Connect.asyncRequest("GET", uri, callback);
         };

         // Show the dialog
         this._showDialog();
      },
      
      /**
       * Internal show dialog function
       * @method _showDialog
       */
      _showDialog: function DLGF__showDialog()
      {
         // Must have list of files
         if (this.options.files === null)
         {
            Alfresco.util.PopupManager.displayMessage(
            {
               text: this.msg("message.no-files")
            });
            return;
         }

         // Enable buttons
         this.widgets.okButton.set("disabled", false);
         this.widgets.cancelButton.set("disabled", false);

         // Dialog title
         var titleDiv = Dom.get(this.id + "-title");
         if (YAHOO.lang.isArray(this.options.files))
         {
            titleDiv.innerHTML = this.msg("title.multi", this.options.files.length);
         }
         else
         {
            titleDiv.innerHTML = this.msg("title.single", '<span class="light">' + $html(this.options.files.displayName) + '</span>');
         }

         // Dialog view mode
         var allowedViewModes = Alfresco.util.arrayToObject(this.options.allowedViewModes),
            modeButtons = this.widgets.modeButtons.getButtons(),
            modeButton, viewMode;

         if (!(this.options.viewMode in allowedViewModes))
         {
            this.options.viewMode = this.options.allowedViewModes[0];
         }
         for (var i = 0, ii = modeButtons.length; i < ii; i++)
         {
            modeButton = modeButtons[i];
            viewMode = parseInt(modeButton.get("name"), 10);
            modeButton.set("disabled", !(viewMode in allowedViewModes));
            if (viewMode == this.options.viewMode)
            {
               if (modeButton.get("checked"))
               {
                  this.setViewMode(viewMode);
               }
               else
               {
                  modeButton.set("checked", true);
               }
            }
         }
         
         // Register the ESC key to close the dialog
         var escapeListener = new KeyListener(document,
         {
            keys: KeyListener.KEY.ESCAPE
         },
         {
            fn: function(id, keyEvent)
            {
               this.onCancel();
            },
            scope: this,
            correctScope: true
         });
         escapeListener.enable();

         // Show the dialog
         this.widgets.dialog.show();
      },
      
      /**
       * Public function to set current dialog view mode
       *  
       * @method setViewMode
       * @param mode {integer} New dialog view mode constant
       */
      setViewMode: function DLGF_setViewMode(viewMode)
      {
         this.options.viewMode = viewMode;
         
         if (viewMode == DLGF.VIEW_MODE_SITE)
         {
            Dom.removeClass(this.id + "-wrapper", "repository-mode");
            this._populateSitePicker();
         }
         else
         {
            Dom.addClass(this.id + "-wrapper", "repository-mode");
            // Build the TreeView widget
            this._buildTree(this.options.nodeRef);
            this.onPathChanged("/");
         }
      },


      /**
       * BUBBLING LIBRARY EVENT HANDLERS
       * Disconnected event handlers for event notification
       */

      /**
       * Site Changed event handler
       *
       * @method onSiteChanged
       * @param layer {object} Event fired
       * @param args {array} Event parameters (depends on event type)
       */
      onSiteChanged: function DLGF_onSiteChanged(layer, args)
      {
         // Check the event is directed towards this instance
         if ($hasEventInterest(this, args))
         {
            var obj = args[1];
            if (obj !== null)
            {
               // Should be a site in the arguments
               if (obj.site !== null)
               {
                  this.options.siteId = obj.site;
                  this._populateContainerPicker();
                  var sites = Selector.query("a", this.id + "-sitePicker"), site, i, j,
                     picker = Dom.get(this.id + "-sitePicker");

                  for (i = 0, j = sites.length; i < j; i++)
                  {
                     site = sites[i];
                     if (site.getAttribute("rel") == obj.site)
                     {
                        Dom.addClass(site, "selected");
                        if (obj.scrollTo)
                        {
                           picker.scrollTop = Dom.getY(site) - Dom.getY(picker);
                        }
                     }
                     else
                     {
                        Dom.removeClass(site, "selected");
                     }
                  }
               }
            }
         }
      },

      /**
       * Container Changed event handler
       *
       * @method onContainerChanged
       * @param layer {object} Event fired
       * @param args {array} Event parameters (depends on event type)
       */
      onContainerChanged: function DLGF_onContainerChanged(layer, args)
      {
         // Check the event is directed towards this instance
         if ($hasEventInterest(this, args))
         {
            var obj = args[1];
            if (obj !== null)
            {
               // Should be a container in the arguments
               if (obj.container !== null)
               {
                  this.options.containerId = obj.container;
                  this.options.containerType = this.containers[obj.container].type;
                  this._buildTree(this.containers[obj.container].nodeRef);
                  // Kick-off navigation to current path
                  this.onPathChanged(this.options.path);
                  var containers = Selector.query("a", this.id + "-containerPicker"), container, i, j,
                     picker = Dom.get(this.id + "-containerPicker");

                  for (i = 0, j = containers.length; i < j; i++)
                  {
                     container = containers[i];
                     if (container.getAttribute("rel") == obj.container)
                     {
                        Dom.addClass(container, "selected");
                        if (obj.scrollTo)
                        {
                           picker.scrollTop = Dom.getY(container) - Dom.getY(picker);
                        }
                     }
                     else
                     {
                        Dom.removeClass(container, "selected");
                     }
                  }
               }
            }
         }
      },


      /**
       * YUI WIDGET EVENT HANDLERS
       * Handlers for standard events fired from YUI widgets, e.g. "click"
       */

      /**
       * Dialog OK button event handler
       *
       * @method onOK
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onOK: function DLGF_onOK(e, p_obj)
      {
         this.widgets.dialog.hide();
      },

      /**
       * Dialog Cancel button event handler
       *
       * @method onCancel
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onCancel: function DLGF_onCancel(e, p_obj)
      {
         this.widgets.dialog.hide();
      },

      /**
       * Mode change buttongroup event handler
       *
       * @method onViewModeChange
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onViewModeChange: function DLGF_onViewModeChange(e, p_obj)
      {
         var viewMode = this.options.viewMode;
         try
         {
            viewMode = parseInt(e.newValue.get("name"), 10);
            this.setViewMode(viewMode);
         }
         catch(ex)
         {
            // Remain in current view mode
         }
      },
      
      /**
       * Fired by YUI TreeView when a node has finished expanding
       * @method onExpandComplete
       * @param oNode {YAHOO.widget.Node} the node recently expanded
       */
      onExpandComplete: function DLGF_onExpandComplete(oNode)
      {
         Alfresco.logger.debug("DLGF_onExpandComplete");

         // Make sure the tree's DOM has been updated
         this.widgets.treeview.render();
         // Redrawing the tree will clear the highlight
         this._showHighlight(true);
         
         if (this.pathsToExpand && this.pathsToExpand.length > 0)
         {
            var node = this.widgets.treeview.getNodeByProperty("path", this.pathsToExpand.shift());
            if (node !== null)
            {
               var el = node.getContentEl(),
                  container = Dom.get(this.id + "-treeview");
               
               container.scrollTop = Dom.getY(el) - (container.scrollHeight / 3);

               if (node.data.path == this.currentPath)
               {
                  this._updateSelectedNode(node);
               }
               node.expand();
            }
         }
      },

      /**
       * Fired by YUI TreeView when a node label is clicked
       * @method onNodeClicked
       * @param args.event {HTML Event} the event object
       * @param args.node {YAHOO.widget.Node} the node clicked
       * @return allowExpand {boolean} allow or disallow node expansion
       */
      onNodeClicked: function DLGF_onNodeClicked(args)
      {
         Alfresco.logger.debug("DLGF_onNodeClicked");

         var node = args.node,
            userAccess = node.data.userAccess;
         
         if ((userAccess && userAccess.create) || (node.data.nodeRef == "") || (node.data.nodeRef == "alfresco://company/home"))
         {
            this.onPathChanged(node.data.path);
            this._updateSelectedNode(node);
         }
         return false;
      },

      
      /**
       * Update tree when the path has changed
       * @method onPathChanged
       * @param path {string} new path
       */
      onPathChanged: function DLGF_onPathChanged(path)
      {
         Alfresco.logger.debug("DLGF_onPathChanged");

         // ensure path starts with leading slash if not the root node
         if (path.charAt(0) != "/")
         {
            path = "/" + path;
         }
         this.currentPath = path;
         
         // Search the tree to see if this path's node is expanded
         var node = this.widgets.treeview.getNodeByProperty("path", path);
         if (node !== null)
         {
            // Node found
            this._updateSelectedNode(node);
            node.expand();
            while (node.parent !== null)
            {
               node = node.parent;
               node.expand();
            }
            return;
         }
         
         /**
          * The path's node hasn't been loaded into the tree. Create a stack
          * of parent paths that we need to expand one-by-one in order to
          * eventually display the current path's node
          */
         var paths = path.split("/"),
            expandPath = "/";
         // Check for root path special case
         if (path === "/")
         {
            paths = [""];
         }
         this.pathsToExpand = [];
         
         for (var i = 0, j = paths.length; i < j; i++)
         {
            // Push the path onto the list of paths to be expanded
            expandPath = $combine(expandPath, paths[i]);
            this.pathsToExpand.push(expandPath);
         }

         // Kick off the expansion process by expanding the first unexpanded path
         do
         {
            node = this.widgets.treeview.getNodeByProperty("path", this.pathsToExpand.shift());
         } while (this.pathsToExpand.length > 0 && node.expanded);
         
         if (node !== null)
         {
            node.expand();
         }
      },
      

      /**
       * PRIVATE FUNCTIONS
       */

      /**
       * Creates the Site Picker control.
       * @method _populateSitePicker
       * @private
       */
      _populateSitePicker: function DLGF__populateSitePicker()
      {
         var sitePicker = Dom.get(this.id + "-sitePicker"),
            me = this;
         
         sitePicker.innerHTML = "";
         
         var fnSuccess = function DLGF__pSP_fnSuccess(response, sitePicker)
         {
            var sites = response.json, element, site, onclick, i, j, firstSite = null;
            
            if (sites.length > 0)
            {
               firstSite = sites[0].shortName;
            }
            
            for (i = 0, j = sites.length; i < j; i++)
            {
               site = sites[i];
               element = document.createElement("div");
               if (i == j - 1)
               {
                  Dom.addClass(element, "last");
               }

               onclick = function DLGF_pSP_onclick(shortName)
               {
                  return function()
                  {
                     YAHOO.Bubbling.fire("siteChanged",
                     {
                        site: shortName,
                        eventGroup: me
                     });
                     return false;
                  };
               }(site.shortName);

               element.innerHTML = '<a rel="' + site.shortName + '" href="#""><h4>' + $html(site.title) + '</h4>' + '<span>' + $html(site.description) + '</span></a>';
               element.onclick = onclick;
               sitePicker.appendChild(element);
            }
            
            // Select current site, or first site retrieved
            YAHOO.Bubbling.fire("siteChanged",
            {
               site: (this.options.siteId.length > 0) ? this.options.siteId : firstSite,
               eventGroup: this,
               scrollTo: true
            });
         };
         
         var config =
         {
            url: Alfresco.constants.PROXY_URI + "api/sites",
            responseContentType: Alfresco.util.Ajax.JSON,
            successCallback:
            {
               fn: fnSuccess,
               scope: this,
               obj: sitePicker
            },
            failureCallback: null
         };
         
         Alfresco.util.Ajax.request(config);
      },

      /**
       * Creates the Container Picker control.
       * @method _populateContainerPicker
       * @private
       */
      _populateContainerPicker: function DLGF__populateContainerPicker()
      {
         var containerPicker = Dom.get(this.id + "-containerPicker"),
            me = this;
         
         containerPicker.innerHTML = "";
         
         var fnSuccess = function DLGF__pSP_fnSuccess(response, containerPicker)
         {
            var containers = response.json.containers, element, container, onclick, i, j;
            this.containers = {};
            
            for (i = 0, j = containers.length; i < j; i++)
            {
               container = containers[i];
               this.containers[container.name] = container;
               element = document.createElement("div");
               if (i == j - 1)
               {
                  Dom.addClass(element, "last");
               }

               onclick = function DLGF_pCP_onclick(containerName)
               {
                  return function()
                  {
                     YAHOO.Bubbling.fire("containerChanged",
                     {
                        container: containerName,
                        eventGroup: me
                     });
                     return false;
                  };
               }(container.name);

               element.innerHTML = '<a rel="' + container.name + '" href="#"><h4>' + container.name + '</h4>' + '<span>' + container.description + '</span></a>';
               element.onclick = onclick;
               containerPicker.appendChild(element);
            }

            // Select current container
            YAHOO.Bubbling.fire("containerChanged",
            {
               container: this.options.containerId,
               eventGroup: this,
               scrollTo: true
            });
         };
         
         var config =
         {
            url: Alfresco.constants.PROXY_URI + "slingshot/doclib/containers/" + this.options.siteId,
            responseContentType: Alfresco.util.Ajax.JSON,
            successCallback:
            {
               fn: fnSuccess,
               scope: this,
               obj: containerPicker
            },
            failureCallback: null
         };
         
         Alfresco.util.Ajax.request(config);
      },

      /**
       * Creates the TreeView control and renders it to the parent element.
       * @method _buildTree
       * @param p_rootNodeRef {string} NodeRef of root node for this tree
       * @private
       */
      _buildTree: function DLGF__buildTree(p_rootNodeRef)
      {
         Alfresco.logger.debug("DLGF__buildTree");

         // Create a new tree
         var tree = new YAHOO.widget.TreeView(this.id + "-treeview");
         this.widgets.treeview = tree;

         // Having both focus and highlight are just confusing (YUI 2.7.0 addition)
         YAHOO.widget.TreeView.FOCUS_CLASS_NAME = "";

         // Turn dynamic loading on for entire tree
         tree.setDynamicLoad(this.fnLoadNodeData);

         var rootLabel = (this.options.viewMode == DLGF.VIEW_MODE_SITE ? (this.options.containerType == "dod:filePlan" ? "node.root-filePlan" : "node.root-sites") : "node.root-repository");

         // Add default top-level node
         var tempNode = new YAHOO.widget.TextNode(
         {
            label: this._msg(rootLabel), // Note: private _msg() function
            path: "/",
            nodeRef: p_rootNodeRef
         }, tree.getRoot(), false);

         // Register tree-level listeners
         tree.subscribe("clickEvent", this.onNodeClicked, this, true);
         tree.subscribe("expandComplete", this.onExpandComplete, this, true);

         // Render tree with this one top-level node
         tree.render();
      },

      /**
       * Highlights the currently selected node.
       * @method _showHighlight
       * @param isVisible {boolean} Whether the highlight is visible or not
       * @private
       */
      _showHighlight: function DLGF__showHighlight(isVisible)
      {
         Alfresco.logger.debug("DLGF__showHighlight");

         if (this.selectedNode !== null)
         {
            if (isVisible)
            {
               Dom.addClass(this.selectedNode.getEl(), "selected");
            }
            else
            {
               Dom.removeClass(this.selectedNode.getEl(), "selected");
            }
         }
      },
      
      /**
       * Updates the currently selected node.
       * @method _updateSelectedNode
       * @param node {object} New node to set as currently selected one
       * @private
       */
      _updateSelectedNode: function DLGF__updateSelectedNode(node)
      {
         Alfresco.logger.debug("DLGF__updateSelectedNode");

         this._showHighlight(false);
         this.selectedNode = node;
         this._showHighlight(true);
      },

      /**
       * Build URI parameter string for treenode JSON data webscript
       *
       * @method _buildTreeNodeUrl
       * @param path {string} Path to query
       */
       _buildTreeNodeUrl: function DLGF__buildTreeNodeUrl(path)
       {
          var uriTemplate = Alfresco.constants.PROXY_URI;
          if (this.options.viewMode == DLGF.VIEW_MODE_SITE)
          {
             if (this.options.containerType == "dod:filePlan")
             {
                uriTemplate += "slingshot/doclib/dod5015/treenode/site/{site}/{container}{path}";
             }
             else
             {
                uriTemplate += "slingshot/doclib/treenode/site/{site}/{container}{path}";
             }
          }
          else
          {
             uriTemplate += "slingshot/doclib/treenode/node/{nodeRef}{path}";
          }

          uriTemplate += "?children=" + this.options.evaluateChildFolders;

          var url = YAHOO.lang.substitute(uriTemplate,
          {
             site: encodeURIComponent(this.options.siteId),
             container: encodeURIComponent(this.options.containerId),
             nodeRef: this.options.nodeRef.replace(":/", ""),
             path: Alfresco.util.encodeURIPath(path)
          });

          return url;
       },

       /**
        * Gets a custom message regardless of current view mode
        *
        * @method _msg
        * @param messageId {string} The messageId to retrieve
        * @return {string} The custom message
        * @private
        */
       _msg: function DLGF__msg(messageId)
       {
          return Alfresco.util.message.call(this, messageId, this.name, Array.prototype.slice.call(arguments).slice(1));
       }
   });

   /* Dummy instance to load optional YUI components early */
   var dummyInstance = new Alfresco.module.DoclibGlobalFolder("null");
})();
