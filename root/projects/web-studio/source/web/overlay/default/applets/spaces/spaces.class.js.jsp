<%
	String overlayPath = org.alfresco.web.studio.OverlayUtil.getOriginalURL(request, "/proxy/alfresco-web-studio/overlay/default");
	String iconsPath = overlayPath + "/images/icons";
%>

WebStudio.Applets.Spaces = WebStudio.Applets.Abstract.extend({});

WebStudio.Applets.Spaces.prototype.getDependenciesConfig = function()
{
	return {
		"webcomponents" : {
			"title" : "applet dependencies",
			"loader" : {
				"CSS" : {
					"name" : "CSS",
					"path" : "<%=overlayPath%>/applets/spaces/spaces.class.css.jsp"
				}
			}
		}
	};
}

WebStudio.Applets.Spaces.prototype.getTemplateDomId = function()
{
	return "ContentSpacesSlider";
}

WebStudio.Applets.Spaces.prototype.bindSliderControl = function(container) 
{
	if(!this.treeView)
	{
		var controlTemplate = this.instantiateControlTemplate(this.getId(), 'AlfrescoTreeViewSpacesTemplate');
		
		this.treeView = new WebStudio.TreeView('Control_'+this.getId());
		this.treeView.setTemplate(controlTemplate);
		this.treeView.setInjectObject(container);
		this.treeView.defaultElementsConfig = {
			TreeHolder: {
				selector: 'div[id=ATVTreeSpaces]'
			}
		}						
		this.treeView.activate();
		
		var rootNode = new YAHOO.widget.TextNode({id:'root', label: 'Spaces', nodeID: 'root', innerNodesSlyle: 'icon-spaces-folder', path: ''}, this.treeView.getRoot(), false);
		rootNode.labelStyle = 'icon-spaces-root';
		this.treeView.setDynamicLoad(this.loadData.bind(this.treeView));
		this.treeView.draw();
		this.treeView.addNodeLink('root', rootNode);
		
		// bind double-clicks for root page
		var div = rootNode.getEl();
		if(div != null)
		{
			div.ondblclick = (function(e){
			
				e = new Event(e);
				
				var url = "http://localhost:8080/alfresco";
				
				WebStudio.app.openBrowser('alfresco', url);

				e.stop();
			}).bind(rootNode);
		}
				
		// set up 'expand' listener
		this.treeView.tree.subscribe("expandComplete", function(node) 
		{
			for(var i = 0; i < node.children.length; i++)
			{
				var childNode = node.children[i];
				
				var childDiv = childNode.getEl();
				if(childDiv != null)
				{
					childDiv.ondblclick = (function(e) {
						e = new Event(e);

						var url = "http://localhost:8080/alfresco/navigate/browse/workspace/SpacesStore/" + this.data.id;
						
						WebStudio.app.openBrowser('alfresco', url);

						e.stop();					
					}).bind(childNode);
				}
			}
		});
		
		
		
		// add the application treeview drop handler
		this.treeView.dropFromTreeView = this.getApplication().dropFromTreeView.bind(this.getApplication());		
	}
		
	return this.treeView;
}

WebStudio.Applets.Spaces.prototype.loadData = function(node, fnLoadComplete)
{
	var time = new Date();
	var sUrl = WebStudio.ws.studio('/tree/spaces', {
		"path": node.data.path,
		"_dc": time.getSeconds()*1000 + time.getMilliseconds()
	});
	
	var callback = {
		success: (function(oResponse) {
			var oResults = eval("(" + oResponse.responseText + ")");
			for (var i = 0; oResults[i]; i++) 
			{
				var config = oResults[i];
				config["id"] = oResults[i].nodeId;
				config["label"] = oResults[i].text;
				config["nodeID"] = oResults[i].nodeId;
				config["mimetype"] = oResults[i].mimetype || null;
				
				var tempNode = new YAHOO.widget.TextNode(config, node, false);
								
				if (oResults[i].alfType == 'dmFile') 
				{
					// dmFile
					tempNode.labelStyle = 'icon-spaces-item';
				} 
				else
				{
					// dmSpace
					tempNode.labelStyle = 'icon-spaces-folder';
				}
				
				var leaf = false;
				if (oResults[i].leaf)
				{
					leaf = true;
				}
				tempNode.isLeaf = leaf;
				
				this.addNodeLink(oResults[i].nodeId, tempNode);				
			}
			this.createDraggables.delay(200, this);
			oResponse.argument.fnLoadComplete();
		}).bind(this),
		failure: function(oResponse) {
			oResponse.argument.fnLoadComplete();
			Alfresco.App.onFailure(oResponse);
		},
		argument: {
			"node": node,
			"fnLoadComplete": fnLoadComplete
		},
		timeout: 7000
	};
	YAHOO.util.Connect.asyncRequest('GET', sUrl, callback);
}

WebStudio.Applets.Spaces.prototype.onShowSlider = function()
{
	// hide all designers
	this.getApplication().hideAllDesigners();
	   
	// show the page editor
	this.getApplication().showPageEditor();
}

WebStudio.Applets.Spaces.prototype.onHideSlider = function()
{
	// hide the page editor
	this.getApplication().hidePageEditor();
}
