package com.itheima.utils;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.util.Properties;

public class SettingCenterUtil extends PropertyPlaceholderConfigurer implements ApplicationContextAware {

    XmlWebApplicationContext xmlWebApplicationContext;
    //重新父类的加载配置文件方法
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) throws BeansException {
        //1.调用zk加载节点数据设置properties对象中
        loadZk(props);
        //2.添加节点监听 当配置改变的时候，自动刷新spring容器，达到不重启tomcat配置也能生效
        addWacth(props);
        //3.调用父类的processProperties
        super.processProperties(beanFactoryToProcess,props);
    }

    private void addWacth(Properties props) {
        try {
            //创建重试策略
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000,1);
            //创建客户端
            CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", 3000, 3000, retryPolicy);
            client.start();

            TreeCache treeCache = new TreeCache(client,"/config");
            treeCache.start();

            treeCache.getListenable().addListener(new TreeCacheListener() {
                @Override
                public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent treeCacheEvent) throws Exception {
                    if(treeCacheEvent.getType().equals(TreeCacheEvent.Type.NODE_UPDATED)){
                        xmlWebApplicationContext.refresh();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadZk(Properties props) {
        //创建重试策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000,1);
        //创建客户端
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", 3000, 3000, retryPolicy);
        client.start();
        try {
            byte[] urlBytes = client.getData().forPath("/config/jdbc.url");
            props.setProperty("jdbc.url",new String(urlBytes));
            byte[] userBytes = client.getData().forPath("/config/jdbc.user");
            props.setProperty("jdbc.user",new String(userBytes));
            byte[] pwdBytes = client.getData().forPath("/config/jdbc.password");
            props.setProperty("jdbc.password",new String(pwdBytes));
            byte[] driverBytes = client.getData().forPath("/config/jdbc.driver");
            props.setProperty("jdbc.driver",new String(driverBytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
        client.close();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        xmlWebApplicationContext = (XmlWebApplicationContext)applicationContext;
    }
}
