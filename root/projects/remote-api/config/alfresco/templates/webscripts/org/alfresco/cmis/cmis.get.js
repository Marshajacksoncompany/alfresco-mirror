var tckRunner = Packages.org.apache.chemistry.tck.atompub.tools.TCKRunner();
model.tckTests = tckRunner.getTestNames();
model.tckOptions = tckRunner.getOptions();
model.cmisVersion = cmis.version;
model.querySupport = cmis.querySupport.label;
model.joinSupport = cmis.joinSupport.label;
model.pwcSearchable = cmis.pwcSearchable;
model.allVersionsSearchable = cmis.allVersionsSearchable;
