<?php
/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * http://www.alfresco.com/legal/licensing"
 */
 
require_once ('BaseTest.php');
require_once ('../Alfresco/Service/ContentData.php');

class ContentTest extends BaseTest 
{
	const CONTENT = "this is some test content.  And this is some more content.";

	private static $_contentNode;
	
	private function getContentNode()
	{
		if (self::$_contentNode == null)
		{
			// Create a new content node in the company home
			$this->fileName = "1myDoc_" . time() . ".txt";
			self::$_contentNode = $this->getCompanyHome()->createChild(
																"cm_content", 
																"cm_contains", 
																"app_" .$this->fileName);
			self :: $_contentNode->cm_name = $this->fileName;
	
			// Add titled aspect for UI				
			self::$_contentNode->addAspect("cm_titled");
			self::$_contentNode->cm_title = "This is my new document.";
			self::$_contentNode->cm_description = "This describes what is in the document.";
	
			// Save new content
			$this->getSession()->save();	
		}
		return self::$_contentNode;
	}

	public function checkCreatedNode() 
	{
		$this->assertNotNull($this->getContentNode());
		// TODO ... maybe should have a standard check for a newly created node!	
	}

	public function testCheckNullContent() 
	{
		$this->assertNull($this->getContentNode()->cm_content);
	}

	public function testSetContent() 
	{
		$contentData = new ContentData("text/plain", "UTF-8");
		$contentData->content = ContentTest :: CONTENT;
		$this->getContentNode()->cm_content = $contentData;
		$this->getSession()->save();
	}

	public function testReadContentDetails() 
	{
		$this->assertEquals("text/plain", $this->getContentNode()->cm_content->mimetype);
		$this->assertEquals("UTF-8", $this->getContentNode()->cm_content->encoding);
		$this->assertEquals(strlen(ContentTest::CONTENT), $this->getContentNode()->cm_content->size);
	}

	public function testGetUrls() 
	{
		$url = $this->getContentNode()->cm_content->url;
		$this->assertNotNull($url);
		if (strpos($url, "ticket") === false) 
		{
			$this->fail("Invalid content URL");
		}

		$guestUrl = $this->getContentNode()->cm_content->guestUrl;
		$this->assertNotNull($guestUrl);
		if (strpos($guestUrl, "guest") === false) 
		{
			$this->fail("Invalid guest URL");
		}
	}

	public function testGetContent() 
	{
		$content = $this->getContentNode()->cm_content->content;
		$this->assertNotNull($content);
		$this->assertEquals(strlen($content), $this->getContentNode()->cm_content->size);
		$this->assertEquals(ContentTest::CONTENT, $content);
	}
	
	public function testWriteContentFromFile()
	{
		$contentData = $this->getContentNode()->cm_content;
		$contentData->mimetype = "image/jpeg";
		$contentData->encoding = "UTF-8";
		$contentData->writeContentFromFile("alfresco/resources/quick.jpg");
		
		$this->getContentNode()->cm_content = $contentData;
		$this->getContentNode()->cm_name = "1myDoc_" . time() . ".jpg";
		$this->getSession()->save();
	}
	
	public function testReadContentToFile()
	{
		$contentData = $this->getContentNode()->cm_content;
		$contentData->readContentToFile("alfresco/resources/temp.jpg");	
	}
}
?>

