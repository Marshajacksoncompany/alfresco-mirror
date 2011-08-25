/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.encryption;

import java.io.File;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryBootstrap;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

public class EncryptionTests extends TestCase
{
	private static final String TEST_MODEL = "org/alfresco/encryption/reencryption_model.xml";
//    private static final String TEST_BUNDLE = "org/alfresco/encryption/encryptiontest_model";

	private static int NUM_PROPERTIES = 500;
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private static QName NODE_TYPE = QName.createQName("http://www.alfresco.org/test/reencryption_test/1.0", "base");
    private static QName PROP = QName.createQName("http://www.alfresco.org/test/reencryption_test/1.0", "prop1");
    
    private NodeRef rootNodeRef;
    
    private TransactionService transactionService;
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private MetadataEncryptor metadataEncryptor;
	private ReEncryptor reEncryptor;
	private String cipherAlgorithm = "DESede/CBC/PKCS5Padding";
	private KeyStoreParameters keyStoreParameters;
	private KeyStoreParameters backupKeyStoreParameters;
	private KeyResourceLoader keyResourceLoader;
	private EncryptionKeysRegistryImpl encryptionKeysRegistry;
	private KeyStoreChecker keyStoreChecker;

    private AuthenticationComponent authenticationComponent;
	private DictionaryDAO dictionaryDAO;
	private TenantService tenantService;

	private String keyAlgorithm;
	private Map<String, Key> newKeys = new HashMap<String, Key>();
	private List<NodeRef> before = new ArrayList<NodeRef>();
	private List<NodeRef> after = new ArrayList<NodeRef>();

	private UserTransaction tx;

	public void setUp() throws Exception
	{
        dictionaryService = (DictionaryService)ctx.getBean("dictionaryService");
        nodeService = (NodeService)ctx.getBean("nodeService");
        transactionService = (TransactionService)ctx.getBean("transactionService");
        tenantService = (TenantService)ctx.getBean("tenantService");
        dictionaryDAO = (DictionaryDAO)ctx.getBean("dictionaryDAO");
        metadataEncryptor = (MetadataEncryptor)ctx.getBean("metadataEncryptor");
        authenticationComponent = (AuthenticationComponent)ctx.getBean("authenticationComponent");
        keyResourceLoader = (KeyResourceLoader)ctx.getBean("springKeyResourceLoader");
        reEncryptor = (ReEncryptor)ctx.getBean("reEncryptor");
        backupKeyStoreParameters = (KeyStoreParameters)ctx.getBean("backupKeyStoreParameters");
        keyStoreChecker = (KeyStoreChecker)ctx.getBean("keyStoreChecker");
        keyStoreParameters = (KeyStoreParameters)ctx.getBean("keyStoreParameters");
        encryptionKeysRegistry = (EncryptionKeysRegistryImpl)ctx.getBean("encryptionKeysRegistry");

        // reencrypt in one txn (since we don't commit the model, the qnames won't be available across transactions)
        reEncryptor.setSplitTxns(false);

        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        tx = transactionService.getUserTransaction();
        tx.begin();

        StoreRef storeRef = nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE,
                "ReEncryptor_" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);

        keyAlgorithm = "DESede";
        newKeys.put(KeyProvider.ALIAS_METADATA, generateSecretKey(keyAlgorithm));
        
        // Load models
        DictionaryBootstrap bootstrap = new DictionaryBootstrap();
        List<String> bootstrapModels = new ArrayList<String>();
        bootstrapModels.add(TEST_MODEL);
//        List<String> labels = new ArrayList<String>();
//        labels.add(TEST_BUNDLE);
        bootstrap.setModels(bootstrapModels);
//        bootstrap.setLabels(labels);
        bootstrap.setDictionaryDAO(dictionaryDAO);
        bootstrap.setTenantService(tenantService);
        bootstrap.bootstrap();
	}
	
	protected KeyProvider getKeyProvider(KeyStoreParameters keyStoreParameters)
	{
		KeyProvider backupKeyProvider = new KeystoreKeyProvider(keyStoreParameters, keyResourceLoader);
		return backupKeyProvider;
	}

    public void setBackupKeyStoreParameters(KeyStoreParameters backupKeyStoreParameters)
	{
		this.backupKeyStoreParameters = backupKeyStoreParameters;
	}

	@Override
    protected void tearDown() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
        tx.rollback();
        super.tearDown();
    }

	protected KeyProvider getKeyProvider(final Map<String, Key> keys)
	{
		KeyProvider keyProvider = new KeyProvider()
		{
			@Override
			public Key getKey(String keyAlias)
			{
				return keys.get(keyAlias);
			}
		};
		return keyProvider;
	}

	protected Encryptor getEncryptor(KeyProvider keyProvider)
	{
		DefaultEncryptor encryptor = new DefaultEncryptor();
		encryptor.setCipherAlgorithm(cipherAlgorithm);
		encryptor.setCipherProvider(null);
		encryptor.setKeyProvider(keyProvider);
		
		return encryptor;
	}

	protected MetadataEncryptor getMetadataEncryptor(Encryptor encryptor)
	{
		MetadataEncryptor metadataEncryptor = new MetadataEncryptor();
		metadataEncryptor.setDictionaryService(dictionaryService);
		metadataEncryptor.setEncryptor(encryptor);
		
		return metadataEncryptor;
	}

	protected void createEncryptedProperties(List<NodeRef> nodes, MetadataEncryptor metadataEncryptor)
	{
		for(int i = 0; i < NUM_PROPERTIES; i++)
		{
			NodeRef nodeRef = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("assoc1"), NODE_TYPE).getChildRef();
			nodes.add(nodeRef);

			Map<QName, Serializable> props = new HashMap<QName, Serializable>();
			props.put(PROP, nodeRef.toString());
			props = metadataEncryptor.encrypt(props);
            nodeService.setProperties(nodeRef, props);
		}
	}

	public byte[] generateKeyData() throws NoSuchAlgorithmException
	{
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		random.setSeed(System.currentTimeMillis());
		byte bytes[] = new byte[DESedeKeySpec.DES_EDE_KEY_LEN];
		random.nextBytes(bytes);
		return bytes;
	}

	protected Key generateSecretKey(String keyAlgorithm) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException
	{
		DESedeKeySpec keySpec = new DESedeKeySpec(generateKeyData());
		SecretKeyFactory kf = SecretKeyFactory.getInstance(keyAlgorithm);
    	SecretKey secretKey = kf.generateSecret(keySpec);
    	return secretKey;		
	}

	public void testReEncrypt()
	{
		try
		{
			// Create encrypted properties using the configured encryptor and key provider
	        createEncryptedProperties(before, metadataEncryptor);
	        
	        // Create encrypted properties using the new encryptor and key provider
	        KeyProvider newKeyProvider = getKeyProvider(newKeys);
	        Encryptor newEncryptor = getEncryptor(newKeyProvider);
	        MetadataEncryptor newMetadataEncryptor = getMetadataEncryptor(newEncryptor);
	        createEncryptedProperties(after, newMetadataEncryptor);
	
	        // re-encrypt
	        long start = System.currentTimeMillis();
			reEncryptor.reEncrypt(newKeyProvider);
			System.out.println("Re-encrypted " + NUM_PROPERTIES*2 + " properties in " + (System.currentTimeMillis() - start) + "ms");
	
			// check that the nodes have been re-encrypted properly i.e. check that the properties
			// decrypted using the new keys match the expected values.
			for(NodeRef nodeRef : before)
			{
				Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
				props = newMetadataEncryptor.decrypt(props);
				assertNotNull("", props.get(PROP));
				assertEquals("", nodeRef.toString(), props.get(PROP));
			}
	
			for(NodeRef nodeRef : after)
			{
				Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
				props = newMetadataEncryptor.decrypt(props);
				assertNotNull("", props.get(PROP));
				assertEquals("", nodeRef.toString(), props.get(PROP));			
			}
		}
		catch(AlfrescoRuntimeException e)
		{
			if(e.getCause() instanceof InvalidKeyException)
			{
				e.printStackTrace();
				fail();
			}
		}
	}
	
	public void testBootstrapReEncrypt()
	{
		try
		{
			reEncryptor.reEncrypt();
			fail("Should have caught missing backup key store");
		}
		catch(MissingKeyException e)
		{
			fail("");
		}
		catch(MissingKeyStoreException e)
		{

		}
	}
	
	public void testKeyStoreCreation()
	{
		String keyStoreLocation = System.getProperty("user.dir") + File.separator + "encryption-tests.keystore";
		File keyStoreFile = new File(keyStoreLocation);
		if(keyStoreFile.exists())
		{
			assertTrue("", keyStoreFile.delete());
		}

		KeyStoreParameters keyStoreParameters = new KeyStoreParameters();
		keyStoreParameters.setLocation(keyStoreLocation);
		keyStoreParameters.setKeyMetaDataFileLocation("classpath:org/alfresco/encryption/keystore-parameters.properties");
		keyStoreParameters.setType("JCEKS");
		AlfrescoKeyStore keyStore = new AlfrescoKeyStoreImpl(keyStoreParameters, keyResourceLoader);

		encryptionKeysRegistry.removeRegisteredKeys(keyStore);

		keyStoreChecker.checkKeyStore(keyStore);

		assertNotNull("", keyStore.getKey("test"));
	}
}