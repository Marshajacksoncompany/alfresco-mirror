<%
	String overlayPath = org.alfresco.web.studio.OverlayUtil.getOriginalURL(request, "/proxy/alfresco-web-studio/overlay/default");
	String iconsPath = overlayPath + "/images/icons";
%>

WebStudio.Applets.Abstract = new Class({
  initialize: function(id, title, description) {
	this.id = id;
	this.title = title;
	this.description = description;
	
	this.isInitialized = false;
  }
});

WebStudio.Applets.Abstract.prototype.getDependenciesConfig = function()
{
	return { };
}

WebStudio.Applets.Abstract.prototype.getId = function()
{
	return this.id;
}

WebStudio.Applets.Abstract.prototype.getTitle = function()
{
	var title = this.title;
	if(!title)
	{
		title = this.getId();
	}
	return title;
}

WebStudio.Applets.Abstract.prototype.getDescription = function()
{
	var desc = this.description;
	if(!desc)
	{
		desc = this.getTitle();
	}
	return desc;
}

WebStudio.Applets.Abstract.prototype.toString = function()
{
	return this.id + "," + this.title + "," + this.description;
}

WebStudio.Applets.Abstract.prototype.init = function(onInit)
{
	var dependenciesConfig = this.getDependenciesConfig();
	
	if(!dependenciesConfig)
	{
		// skip this
		this.isInitialized = true;
	}
	else
	{
		// load all dependencies
		this.bootstrapDependencies(
		{		
			onSuccess: (function() {
			
				// flag that initialization was successful
				this.isInitialized = true;
				
				// call to onInit method
				if(onInit)
				{
					onInit.bind(this).attempt();
				}			
				
			}).bind(this)
			,
			onFailure: (function() 
			{			
				// TODO
				
			}).bind(this)
		});
	}
}

/**
 * Loads all applet dependencies
 */
WebStudio.Applets.Abstract.prototype.bootstrapDependencies = function(options)
{
	var dependenciesConfig = this.getDependenciesConfig();
	
	var bootstrap = new WebStudio.Bootstrap(dependenciesConfig);
	bootstrap.onSuccess = (function() 
	{	
		// mark success
		this.dependenciesBootstrapped = true;
		
		if(options.onSuccess)
		{
			options.onSuccess.bind(this).attempt();
		}		
						
	}).bind(this);
	bootstrap.onFailure = (function() 
	{	
		// mark failure
		this.dependenciesBootstrapped = false;
		
		if(options.onFailure)
		{
			options.onFailure.bind(this).attempt();
		}	
		else
		{
			alert('failed to bootstrap applet dependencies');
		}					
	}).bind(this);
	bootstrap.load();
}


WebStudio.Applets.Abstract.prototype.mountSlider = function(slider, container) 
{
	this.container = container;
	
	this.container.setStyle('overflow', 'auto');
	this.container.setHTML('');

	var tElement = this.container.getParent().getElementsBySelector(".ASSHeaderTitle");
	if(tElement != null)
	{
		tElement.setHTML(this.getTitle());
	}
	
	// bind in the control to the container;
	try
	{
		this.bindSliderControl(container);
	}
	catch(err)
	{
		alert("error while mounting applet '" + this.getId() + "' : " + err);
	}	
	
	// bind onShowSlider
	if(this.onShowSlider)
	{
		slider.onShowSlider = this.onShowSlider.bind(this);
	}

	// bind onHideSlider
	if(this.onHideSlider)
	{
		slider.onHideSlider = this.onHideSlider.bind(this);
	}
}

// useful for creating new dom control templates
WebStudio.Applets.Abstract.prototype.instantiateControlTemplate = function(controlId, templateId)
{
	// clone a template
	var newTemplate = $(templateId).clone(true);
	newTemplate.id = templateId + "_" + controlId;
	newTemplate.injectInside($('ControlInstances'));

	return newTemplate;
}

WebStudio.Applets.Abstract.prototype.bindSliderControl = function(container)
{
	// TODO: implement
}

WebStudio.Applets.Abstract.prototype.getApplication = function()
{
	return this.app;
}

WebStudio.Applets.Abstract.prototype.getContainer = function()
{
	return WebStudio.app;
}