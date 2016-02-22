/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.duracloud.account.db.model.AccountInfo;
import org.duracloud.account.db.model.GlobalProperties;
import org.duracloud.account.db.model.StorageProviderAccount;
import org.duracloud.account.db.repo.DuracloudAccountRepo;
import org.duracloud.account.db.repo.GlobalPropertiesRepo;
import org.duracloud.common.rest.DuraCloudRequestContextUtil;
import org.duracloud.common.util.UserUtil;
import org.duracloud.security.impl.GlobalStore;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageAccountManager;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.domain.impl.StorageAccountImpl;
import org.duracloud.storage.provider.StatelessStorageProvider;
import org.duracloud.storage.util.StorageProviderFactory;

/**
 * This class is responsible for loading and caching global account information 
 * from a remote data store.
 * @author Daniel Bernstein
 */
public class GlobalStorageProviderStore implements GlobalStore {
    private Map<String,StorageProviderFactory> factoryMap;
    private DuracloudAccountRepo accountRepo;
    private GlobalPropertiesRepo globalPropertiesRepo;
    private DuraCloudRequestContextUtil contextUtil  = null;
    private StatelessStorageProvider statelessStorageProvider;
    private UserUtil userUtil;
    
    public GlobalStorageProviderStore(DuracloudAccountRepo accountRepo,
                                      GlobalPropertiesRepo globalPropertiesRepo,
                              DuraCloudRequestContextUtil contextUtil,
                              StatelessStorageProvider statelessStorageProvider,
                              UserUtil userUtil) {
        this.accountRepo = accountRepo;
        this.globalPropertiesRepo = globalPropertiesRepo;
        this.contextUtil = contextUtil;
        this.factoryMap = new HashMap<>();
        this.statelessStorageProvider = statelessStorageProvider;
        this.userUtil = userUtil;
    }
    
    
    
    public StorageProviderFactory getStorageProviderFactory(String accountId){
        ensureStorageProviderFactoryIsLoaded(accountId);
        return this.factoryMap.get(accountId);
    }
    
    @Override
    public void remove(String accountId){
        this.factoryMap.remove(accountId);
    }

    @Override
    public void removeAll(){
        this.factoryMap.clear();
    }


    private void ensureStorageProviderFactoryIsLoaded(String accountId) {
        if(!this.factoryMap.containsKey(accountId)){
            this.factoryMap.put(accountId, buildStorageProviderFactory(accountId));
        }
    }
    
    protected StorageProviderFactory buildStorageProviderFactory(String accountId) {
        
        //retrieve account info from db
        AccountInfo info = this.accountRepo.findBySubdomain(accountId);

        //build a storage account manager
        List<StorageAccount> sps = new LinkedList<>();
        sps.add(createStorageAccount(info.getPrimaryStorageProviderAccount(), true));
        StorageAccountManager storageAccountManager = new StorageAccountManager();
        for(StorageProviderAccount spa : info.getSecondaryStorageProviderAccounts()){
            sps.add(createStorageAccount(spa, false));
         }
        
        //initialize it
        storageAccountManager.initialize(sps);
        
        storageAccountManager.setEnvironment(this.contextUtil.getHost(),
                                             this.contextUtil.getPort()+"",
                                             this.contextUtil.getAccountId());
        
        StorageProviderFactoryImpl factory =
            new StorageProviderFactoryImpl(storageAccountManager,
                                           statelessStorageProvider,
                                           userUtil);
        return factory;
    }
    
    private StorageAccount createStorageAccount(StorageProviderAccount spa, boolean primary){
        StorageAccountImpl storageAccount =
            new StorageAccountImpl(spa.getId() + "",
                                   spa.getUsername(),
                                   spa.getPassword(),
                                   spa.getProviderType());
        storageAccount.setPrimary(primary);
        storageAccount.setOwnerId(null);
        Map<String,String> props = spa.getProperties();
        if(props != null){
            for(String key : props.keySet()){
                storageAccount.setOption(key, props.get(key));
            }
        }

        List<GlobalProperties> propslist = globalPropertiesRepo.findAll();
        if(propslist != null && propslist.size() > 0){
            GlobalProperties globalProps = propslist.get(0);
            StorageProviderType spType = spa.getProviderType();
            if(spType.equals(StorageProviderType.AMAZON_S3)){
                storageAccount.setOption(StorageAccount.OPTS.CF_ACCOUNT_ID.name(), globalProps.getCloudFrontAccountId());
                storageAccount.setOption(StorageAccount.OPTS.CF_KEY_ID.name(), globalProps.getCloudFrontKeyId());
                storageAccount.setOption(StorageAccount.OPTS.CF_KEY_PATH.name(), globalProps.getCloudFrontKeyPath());
            }
        }
        return storageAccount;
    }
}
